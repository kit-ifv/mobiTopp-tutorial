package destination

import edu.kit.ifv.mobitopp.discretechoice.distribution.CumulateDistributionArray
import edu.kit.ifv.mobitopp.discretechoice.distribution.MultinomialLogitArray
import edu.kit.ifv.mobitopp.discretechoice.distribution.probabilities
import edu.kit.ifv.mobitopp.discretechoice.models.FixedChoiceModel
import edu.kit.ifv.mobitopp.discretechoice.selection.SelectionFunctionArray
import edu.kit.ifv.mobitopp.discretechoice.selection.WeightedSelection
import kotlin.random.Random

/*
A fixed choice model that calculates the utilities of all alternatives in one go instead of calling a utility function
per alternative. The utility array returned by [generateUtilitiesArray] must follow the iteration order of [choices].
The array is reused in place for the probabilities, so no maps are created during selection.
 */
abstract class BatchUtilityChoiceModel<C, P, A>(
    val parameters: P,
    val distributionFunction: CumulateDistributionArray<P> = MultinomialLogitArray(),
    val selectionFunction: SelectionFunctionArray = WeightedSelection(),
) : FixedChoiceModel<A, C> {

    private val alternatives: List<A> by lazy { choices.toList() }
    private val lookup: Map<A, Int> by lazy { alternatives.withIndex().associate { it.value to it.index } }

    context(characteristic: C)
    abstract fun P.generateUtilitiesArray(): DoubleArray

    private fun getIndex(alternative: A): Int {
        return lookup[alternative] ?: error("Alternative $alternative is not part of the choices of model $name")
    }

    context(_: C)
    private fun utilitiesArray(): DoubleArray = parameters.generateUtilitiesArray()

    // Alternatives that are not allowed get a utility of -Infinity, which results in a probability of 0
    private inline fun DoubleArray.mask(isAllowed: (A) -> Boolean): DoubleArray {
        for (i in indices) {
            if (!isAllowed(alternatives[i])) this[i] = Double.NEGATIVE_INFINITY
        }
        return this
    }

    context(random: Random)
    private inline fun selectInternal(utilities: DoubleArray, fallbackOptions: () -> Collection<A>): A {
        if (!distributionFunction.tryCumulateProbabilities(utilities, parameters)) {
            return fallbackOptions().random(random)
        }
        return alternatives[selectionFunction.calculateSelection(utilities, random)]
    }

    context(_: C, random: Random)
    override fun select(): A = selectInternal(utilitiesArray()) { choices }

    context(_: C, random: Random)
    override fun select(choices: Set<A>): A {
        if (choices.size == 1) return choices.first()
        return selectInternal(utilitiesArray().mask { it in choices }) { choices }
    }

    context(_: C, random: Random)
    override fun selectFiltered(filter: (A) -> Boolean): A {
        return selectInternal(utilitiesArray().mask(filter)) { choices.filter(filter) }
    }

    context(_: C, random: Random)
    override fun selectInjected(choices: Set<A>, injections: Map<A, (Double) -> Double>): A {
        if (choices.size == 1) return choices.first()
        val utilities = utilitiesArray().mask { it in choices }
        injections.forEach { (alternative, operation) ->
            val idx = getIndex(alternative)
            utilities[idx] = operation(utilities[idx])
        }
        return selectInternal(utilities) { choices }
    }

    context(_: C)
    override fun utility(alternative: A): Double = utilitiesArray()[getIndex(alternative)]

    context(_: C)
    override fun utilities(alternatives: Collection<A>): Map<A, Double> {
        val utilities = utilitiesArray()
        return alternatives.associateWith { utilities[getIndex(it)] }
    }

    override fun probabilities(utilities: Map<A, Double>): Map<A, Double> {
        val calculationArray = DoubleArray(alternatives.size) { Double.NEGATIVE_INFINITY }
        utilities.forEach { (alternative, utility) -> calculationArray[getIndex(alternative)] = utility }
        val probabilities = distributionFunction.probabilities(calculationArray, parameters)
        return utilities.keys.associateWith { probabilities[getIndex(it)] }
    }
}
