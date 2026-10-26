package destination

import edu.kit.ifv.mobitopp.discretechoice.models.FixedChoiceModel
import kotlin.random.Random

/*
A fixed choice model that converts the characteristics [C] of the choice situation into the characteristics [D] that the
[delegateChoiceModel] works on. This allows to precompute expensive attributes once per choice situation, instead of
once per alternative.
 */
abstract class CharacteristicMapperChoiceModel<A, C, D> : FixedChoiceModel<A, C> {

    abstract val delegateChoiceModel: FixedChoiceModel<A, D>

    context(characteristic: C)
    abstract fun characteristicConverter(): D

    context(_: C, random: Random)
    override fun select(): A = context(characteristicConverter()) { delegateChoiceModel.select() }

    context(_: C, random: Random)
    override fun select(choices: Set<A>): A =
        context(characteristicConverter()) { delegateChoiceModel.select(choices) }

    context(_: C, random: Random)
    override fun selectFiltered(filter: (A) -> Boolean): A =
        context(characteristicConverter()) { delegateChoiceModel.selectFiltered(filter) }

    context(_: C, random: Random)
    override fun selectInjected(choices: Set<A>, injections: Map<A, (Double) -> Double>): A =
        context(characteristicConverter()) { delegateChoiceModel.selectInjected(choices, injections) }

    context(_: C)
    override fun utility(alternative: A): Double =
        context(characteristicConverter()) { delegateChoiceModel.utility(alternative) }

    context(_: C)
    override fun utilities(alternatives: Collection<A>): Map<A, Double> =
        context(characteristicConverter()) { delegateChoiceModel.utilities(alternatives) }

    override fun probabilities(utilities: Map<A, Double>): Map<A, Double> =
        delegateChoiceModel.probabilities(utilities)
}
