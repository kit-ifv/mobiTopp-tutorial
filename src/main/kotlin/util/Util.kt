package util

import edu.kit.ifv.domain.shared.location.StandardLocation
import edu.kit.ifv.domain.simulation.behavior.DestinationAlternative
import edu.kit.ifv.domain.simulation.behavior.DestinationChoiceCharacteristics
import edu.kit.ifv.domain.simulation.behavior.with
import edu.kit.ifv.mobitopp.discretechoice.distribution.NestedStructureDataBuilder
import edu.kit.ifv.mobitopp.discretechoice.structure.DiscreteStructure
import edu.kit.ifv.mobitopp.discretechoice.structure.EnumeratedStructureBuilder
import edu.kit.ifv.mobitopp.discretechoice.structure.NestedStructure
import edu.kit.ifv.mobitopp.discretechoice.structure.NestedTree
import edu.kit.ifv.mobitopp.discretechoice.structure.RuleBasedStructure
import edu.kit.ifv.mobitopp.discretechoice.structure.UtilityAssignmentBuilder
import edu.kit.ifv.mobitopp.discretechoice.structure.UtilityEnumerationBuilder
import edu.kit.ifv.mobitopp.discretechoice.utilityassignment.DiscreteModelBuilder
import edu.kit.ifv.mobitopp.discretechoice.utilityassignment.EnumeratedDiscreteModelBuilder
import edu.kit.ifv.units.Currency
import edu.kit.ifv.units.CurrencyUnit
import edu.kit.ifv.mobitopp.discretechoice.structure.forOptions as forOptionsImpl
import edu.kit.ifv.mobitopp.discretechoice.utilityassignment.multinomialLogit as multinomialLogitImpl
import edu.kit.ifv.mobitopp.discretechoice.utilityassignment.nestedLogit as nestedLogitImpl
import edu.kit.ifv.mobitopp.discretechoice.utilityassignment.openMultinomialLogit as openMultinomialLogitImpl

fun <C,P> binaryStructure(lambda: DiscreteStructure<Boolean, C, P>.() -> Unit) =
    DiscreteStructure<Boolean, C, P>(lambda)

fun <A,C,P> flatStructure(lambda: DiscreteStructure<A, C, P>.() -> Unit) =
    DiscreteStructure<A, C, P>(lambda)

fun <A,C,P> nestedStructure(lambda:  NestedTree<A, C, P>.() -> Unit) =
    NestedStructure<A, C, P>(lambda)


operator fun Double.times(boolean: Boolean): Double = if (boolean) this else 0.0
inline val Boolean.D get() = if (this) 1.0 else 0.0
inline val Boolean.I get() = if (this) 1 else 0

val Currency.euros
    get()= this.toDouble(CurrencyUnit.EUROS)

operator fun Boolean.times(double: Double): Double = if (this) double else 0.0
operator fun Double.minus(boolean: Boolean): Double = this - boolean.D
operator fun Int.minus(boolean: Boolean): Int = this - boolean.I

public fun <A, C, P> EnumeratedStructureBuilder<A, C, P>.forOptions(
    elements: Iterable<A>,
    utilityFunction: P.(A, C) -> Double
): Unit {
    forOptionsImpl(elements, utilityFunction)
}

public fun <A, C, P> UtilityEnumerationBuilder<A, C, P>.multinomialLogit(
    name: String
): EnumeratedDiscreteModelBuilder<A, C, P> {
    return multinomialLogitImpl(name)
}

public fun <A, C, P> UtilityAssignmentBuilder<A, C, P>.openMultinomialLogit(
    name: String
): DiscreteModelBuilder<A, C, P> {
    return openMultinomialLogitImpl(name)
}


val pedestrian = TutorialMode.PEDESTRIAN
val bike = TutorialMode.BIKE
val bikesharing = TutorialMode.BIKESHARING
val car = TutorialMode.CAR
val passenger = TutorialMode.PASSENGER
val publictransport = TutorialMode.PUBLICTRANSPORT

fun <P> destinationChoiceStructure(scope: P.(DestinationAlternative) -> Double) =
    RuleBasedStructure<StandardLocation, DestinationChoiceCharacteristics, P> {
        ruleForAll { loc, sit ->
            val it = sit.with(loc)
            scope(it)
        }
    }

typealias ModeChoiceCharacteristics = edu.kit.ifv.domain.simulation.behavior.ModeChoiceCharacteristics
typealias Mode = edu.kit.ifv.domain.shared.enums.Mode


public fun <A, C, P, B> B.nestedLogit(
    name: String
): EnumeratedDiscreteModelBuilder<A, C, P> where B : UtilityEnumerationBuilder<A, C, P>, B : NestedStructureDataBuilder<A, C, P> {
    return nestedLogitImpl(name)
}