package destination

import edu.kit.ifv.domain.shared.behavior.AttractivenessCacher
import edu.kit.ifv.domain.shared.behavior.AttractivenessModel
import edu.kit.ifv.domain.shared.enums.person.Employment
import edu.kit.ifv.domain.shared.location.ArrayBackedImpedance
import edu.kit.ifv.domain.shared.location.StandardLocation
import edu.kit.ifv.domain.shared.location.zone.ZoneId
import edu.kit.ifv.domain.simulation.behavior.availability.ModeAvailabilityModel
import edu.kit.ifv.domain.simulation.behavior.destinationchoice.*
import edu.kit.ifv.mobitopp.discretechoice.models.FixedChoiceModel
import edu.kit.ifv.units.Distance
import util.D
import util.TutorialMode
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.min

/*
In the utility function there are only 4 relevant outcomes for age: 0-17, 50-69, 70-120 and the rest.
This attribute is partitioned: There is exactly one outcome for each agent. The utility function benefits significantly
from getting the partitioning information by returning an encoded int
 */
private fun encodeAge(age: Int): Int {
    return when (age) {
        in 0..17 -> 0 // Category 1
        in 50..69 -> 1 // Cat 5,6
        in 70..120 -> 2 // Cat 7, 8
        else -> 3 // ALl else
    }
}
/*
Same for employment, there are many employments, but the utility function only cares for a broad subset.
 */
private fun encodeEmployment(employment: Employment): Int {

    return when (employment) {
        Employment.STUDENT_PRIMARY, Employment.STUDENT_SECONDARY, Employment.STUDENT_TERTIARY, Employment.EDUCATION, Employment.STUDENT -> 0
        Employment.HOMEKEEPER, Employment.RETIRED -> 1
        else -> 2
    }
}

private fun encodeDistance(distance: Distance): Int = encodeDistanceKm(distance.kilometers)
private fun encodeDistanceKm(distanceKm: Double): Int {
    return when (distanceKm) {
        in 0.0..1.0 -> 0
        in 1.0..2.0 -> 1
        else -> 2
    }
}

class WrappedDestinationChoice(
    // Because the short term config system tries to build things, i need to pass on all stuff as supplier lambdas and lazy constructors
    val impedanceSpawner: () -> ArrayBackedImpedance,
    val attractivenessModelSpawner: () -> AttractivenessModel,
    val identifiableMap: (StandardLocation) -> ZoneId = { it.zoneId },
    choicesSpawner: () -> Set<StandardLocation>,
    val modeAvailability: ModeAvailabilityModel,
    override val name: String,

    ) : CharacteristicMapperChoiceModel<StandardLocation, DestinationChoiceCharacteristics, AttributesAndArrays>(){

    val delegatedChoiceModel: FixedChoiceModel<StandardLocation, AttributesAndArrays> by lazy { FastDestinationChoice(
        parameters = DestinationChoiceParameters(),
        name = "MyIndexBasedSelectionModel",
        // Share the same set, so that the utility array indices match indexMap and contiguousArrays
        choices = choices,
    )}

    override val delegateChoiceModel: FixedChoiceModel<StandardLocation, AttributesAndArrays>
        get() = delegatedChoiceModel


    override val choices by lazy( choicesSpawner )

    val alternativeOrder: List<StandardLocation> by lazy { choices.toList() }

    val indexMap: Map<ZoneId, Int> by lazy {
        alternativeOrder.withIndex().associate { identifiableMap(it.value) to it.index }
    }

    val impedance by lazy(impedanceSpawner)

    val attractivenessModel by lazy(attractivenessModelSpawner)

    private val cacher: AttractivenessCacher by lazy {
        AttractivenessCacher(
            attractivenessModel, choices.map { it.zoneId }
        )
    }
    private val contiguousArrays: ImpedanceAsContiguousArrays by lazy {
        ImpedanceAsContiguousArrays.fromImpedance(
            impedance,
            choices.size
        )
    }

    override fun fixed(choices: Set<StandardLocation>): FixedChoiceModel<StandardLocation, DestinationChoiceCharacteristics> {
        return WrappedDestinationChoice(
            impedanceSpawner = impedanceSpawner,
            attractivenessModelSpawner = attractivenessModelSpawner,
            identifiableMap = identifiableMap,
            choicesSpawner = { choices },
            name = name,
            modeAvailability = modeAvailability,
        )
    }

    context(characteristic: DestinationChoiceCharacteristics)
    override fun characteristicConverter(): AttributesAndArrays {
        val originIdx: Int = indexMap.getValue(characteristic.origin.zoneId)
        val arrays = contiguousArrays.forOrigin(originIdx, cacher[purpose])

        // TODO this is not accurate at all, there may be differences in availability based on the target itself
        //    but for a proof of concept it worked. For release availabilities must probably be encoded for each
        //    alternative and not a global alternative.
        val availis = with(DestinationAlternative(characteristic, choices.first())) {
            DoubleArray(7) {
                // TODO right now i simply assume that bikesharing is on index 6
                if (it == 6) {
                    modeAvailability.asProviderAvailabilityFilter().filter(TutorialMode.BIKESHARING).D
                } else {
                    // TODO the other availabilities, except car are not even in the choice model, this needs fixing
                    1.0
                }
            }
        }
        return AttributesAndArrays(
            Attributes(
                ageCode = encodeAge(characteristic.person.age),
                employmentCode = encodeEmployment(characteristic.person.employment),
                hasCommuterTicket = characteristic.person.hasCommuterTicket.D,
                hasGuaranteedCar = (carsPerAdult >= 1.0).D,
                hasNoCar = (householdCars == 0).D,
                hasHighIncome = (householdEconomicalStatus in 4..5).D,
                availabilities = availis,
                originIdx = originIdx
            ), arrays
        )
    }
}

// This class holds all attributes that are relevant for the utility function as numeric elements to avoid frequent
// casting
class Attributes(
    var ageCode: Int,
    var employmentCode: Int,
    var hasCommuterTicket: Double,
    var hasGuaranteedCar: Double,
    var hasNoCar: Double,
    var hasHighIncome: Double,
    var availabilities: DoubleArray,
    var originIdx: Int,
    )


// Because i dont want 0 * Infinity to cause problems
inline fun clampedExp(input: Double): Double {
    return exp(input).coerceAtMost(Double.MAX_VALUE)
}

data class AttributesAndArrays(
    val attributes: Attributes,
    val globalTables: ImpedanceAsContiguousArrays.OriginArrays,
)

class FastDestinationChoice(
    parameters: DestinationChoiceParameters,
    override val name: String,
    override val choices: Set<StandardLocation>,
) : BatchUtilityChoiceModel<AttributesAndArrays, DestinationChoiceParameters, StandardLocation>(parameters = parameters) {
    val attrAge = parameters.run {
        doubleArrayOf(
            shift_age_1_on_logsum_attr,
            shift_age_56_on_logsum_attr,
            shift_age_78_on_logsum_attr,
            .0,
        )
    }
    val attrEmpl = parameters.run {
        doubleArrayOf(
            shift_educ_on_logsum_attr,
            shift_home_on_logsum_attr,
            .0
        )
    }
    val attrDist = parameters.run {
        doubleArrayOf(
            shift_b_0_1_on_logsum_attr,
            shift_b_1_2_on_logsum_attr,
            .0
        )
    }
    val staticDist = parameters.run {
        doubleArrayOf(
            b_0_1,
            b_1_2,
            .0
        )
    }
    val ptAge = parameters.run {
        doubleArrayOf(
            shift_age_1_on_logsum_pt,
            shift_age_56_on_logsum_pt,
            shift_age_78_on_logsum_pt,
            .0,
        )
    }
    val ptEmpl = parameters.run {
        doubleArrayOf(
            shift_educ_on_logsum_pt,
            shift_home_on_logsum_pt,
            .0
        )
    }

    val driveAge = parameters.run {
        doubleArrayOf(
            shift_age_1_on_logsum_drive,
            shift_age_56_on_logsum_drive,
            shift_age_78_on_logsum_drive,
            .0,
        )
    }
    val driveEmpl = parameters.run {
        doubleArrayOf(
            shift_educ_on_logsum_drive,
            shift_home_on_logsum_drive,
            .0
        )
    }

    val pt_fixAge = parameters.run {
        doubleArrayOf(
            shift_age_1_on_logsum_pt_fix,
            shift_age_56_on_logsum_pt_fix,
            shift_age_78_on_logsum_pt_fix,
            .0,
        )
    }
    val pt_fixEmpl = parameters.run {
        doubleArrayOf(
            shift_educ_on_logsum_pt_fix,
            shift_home_on_logsum_pt_fix,
            .0
        )
    }

    val drive_fixAge = parameters.run {
        doubleArrayOf(
            shift_age_1_on_logsum_drive_fix,
            shift_age_56_on_logsum_drive_fix,
            shift_age_78_on_logsum_drive_fix,
            .0,
        )
    }
    val drive_fixEmpl = parameters.run {
        doubleArrayOf(
            shift_educ_on_logsum_drive_fix,
            shift_home_on_logsum_drive_fix,
            .0
        )
    }

    context(characteristic: AttributesAndArrays)
    override fun DestinationChoiceParameters.generateUtilitiesArray(): DoubleArray {
        // Initialize the array for calculation purposes here
        val result = DoubleArray(choices.size)
        val attributes = characteristic.attributes
        val globalTables = characteristic.globalTables
        //Calculate all static parameters outside the loop

        val logsumPtBase: Double = (b_logsum_pt
                +shift_purp_on_logsum_pt
                +ptAge[attributes.ageCode]
                +ptEmpl[attributes.employmentCode]
                +shift_zk_on_logsum_pt * attributes.hasCommuterTicket
                +shift_carav_on_logsum_pt * attributes.hasGuaranteedCar
                +shift_nocar_on_logsum_pt * attributes.hasNoCar
                +shift_high_inc_on_logsum_pt * attributes.hasHighIncome)

        val logsumPtBaseFix: Double = (
                b_logsum_pt_fix
                        +pt_fixAge[attributes.ageCode]
                        +pt_fixEmpl[attributes.employmentCode]
                        +shift_zk_on_logsum_pt_fix * attributes.hasCommuterTicket
                        +shift_carav_on_logsum_pt_fix * attributes.hasGuaranteedCar
                        +shift_nocar_on_logsum_pt_fix * attributes.hasNoCar
                        +shift_high_inc_on_logsum_pt_fix * attributes.hasHighIncome)

        val logsumDriveBase: Double = (b_logsum_drive
                +driveAge[attributes.ageCode]
                +driveEmpl[attributes.employmentCode]
                +shift_zk_on_logsum_drive * attributes.hasCommuterTicket
                +shift_carav_on_logsum_drive * attributes.hasGuaranteedCar
                +shift_nocar_on_logsum_drive * attributes.hasNoCar
                +shift_high_inc_on_logsum_drive * attributes.hasHighIncome)


        val logsumDriveBaseFix: Double =( b_logsum_drive_fix
                +drive_fixAge[attributes.ageCode]
                +drive_fixEmpl[attributes.employmentCode]
                +shift_zk_on_logsum_drive_fix * attributes.hasCommuterTicket
                +shift_carav_on_logsum_drive_fix * attributes.hasGuaranteedCar
                +shift_nocar_on_logsum_drive_fix * attributes.hasNoCar
                +shift_high_inc_on_logsum_drive_fix * attributes.hasHighIncome)

        val attractivityStaticBase = (b_attr
                +attrAge[attributes.ageCode]
                +attrEmpl[attributes.employmentCode]
                +shift_zk_on_logsum_attr * attributes.hasCommuterTicket
                +shift_carav_on_logsum_attr * attributes.hasGuaranteedCar
                +shift_high_inc_on_logsum_attr * attributes.hasHighIncome)

        for (i in choices.indices) {
            // Calculate all variable parameters in the loop.
            val distanceCode = encodeDistanceKm(globalTables.travelDistanceCar(i))
            val isIntrazonal = (i == attributes.originIdx).D
            val attractivityBase: Double = (attractivityStaticBase
                    // Keep local calculations
                    +attrDist[distanceCode]
                    +shift_intrazonal_on_attr * isIntrazonal
                    )


            val attractivity: Double = ln(min(max_attractivity, globalTables.attractiveness(i)))
            val staticFactors: Double = staticDist[distanceCode] + b_intrazonal * isIntrazonal

            val logsumPt: Double = ln(
                clampedExp(2.0 + asc_ped + b_tt_ped * globalTables.travelTimePed(i))
                        + clampedExp(2.0 + asc_bike + b_tt_bike * globalTables.travelTimeBike(i))
                        + clampedExp(
                    2.0 + asc_put + b_tt_put * globalTables.travelTimePt(i) + b_cost * globalTables
                        .travelCostPt(i)
                ) + attributes.availabilities[6] * clampedExp(
                    2 + asc_bs + b_tt_bike * globalTables.travelTimeBikesharing(i) + b_cost * globalTables
                        .travelCostBikesharing(i)
                )
            )

            val logsumDrive: Double = ln(
                clampedExp(
                    2 + asc_car_d + b_tt_car_d * globalTables.travelTimeCar(i) + b_cost * globalTables
                        .travelCostCar(i)
                )
            )


            val logsumPtFix: Double = ln(
                clampedExp(2.0 + asc_ped + b_tt_ped * globalTables.travelTimePedFix(i))
                        + clampedExp(2.0 + asc_bike + b_tt_bike * globalTables.travelTimeBikeFix(i))
                        + clampedExp(
                    2.0 + asc_put + b_tt_put * globalTables.travelTimePtFix(i) + b_cost * globalTables
                        .travelCostPtFix(i)
                ) + attributes.availabilities[6] * clampedExp(
                    2 + asc_bs + b_tt_bike * globalTables.travelTimeBikesharingFix(i) + b_cost * globalTables
                        .travelCostBikesharingFix(i)
                )
            )

            val logsumDriveFix: Double = ln(
                clampedExp(
                    2 + asc_car_d + b_tt_car_d * globalTables.travelTimeCarFix(i) + b_cost * globalTables
                        .travelCostCarFix(i)
                )
            )

            result[i] = (staticFactors + attractivityBase * attractivity
                    + logsumPtBase * logsumPt
                    + logsumDriveBase * logsumDrive
                    + logsumPtBaseFix * logsumPtFix
                    + logsumDriveBaseFix * logsumDriveFix)

        }

        return result
    }
}