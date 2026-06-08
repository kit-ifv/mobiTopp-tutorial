package bikesharing

import util.binaryStructure
import util.multinomialLogit
import util.times

data class BikesharingParameters(
    val asc_bs: Double = -4.0478,
    val b_male: Double = 0.2514,
    val b_age_4: Double = 0.0000,
    val b_age_5: Double = -0.2675,
    val b_age_6: Double = -0.5793,
    val b_age_789: Double = -0.6928,
    val b_taet_arbeit: Double = 0.0000,
    val b_taet_haus_arblos: Double = -0.8001,
    val b_eco_3: Double = 0.0000,
    val b_eco_5: Double = 0.4372,
    val b_pkwhh_0: Double = 0.5728,
    val b_pkwhh_1: Double = 0.0000,
    val b_pkwhh_2: Double = -0.3360,
    val b_pkwhh_3: Double = -0.8592,
    val b_regiotyp_1: Double = 0.4509,
    val b_regiotyp_3: Double = 0.0000,
    val b_regiotyp_567: Double = -0.4650,
    val b_carsharing: Double = 1.4487,
    val b_ticket: Double = 0.5278,
    val b_hasBike: Double = 0.5695
)

val bikesharingChoiceModel = binaryStructure<BikesharingCharacteristics, BikesharingParameters> {
    option(false) { choice, sit ->
        0.0 //reference category
    }

    option(true) { choice, sit ->
        (
                asc_bs +
                        b_male * sit.isMale +
                        b_age_4 * (sit.age in 30..39) +
                        b_age_5 * (sit.age in 40..49) +
                        b_age_6 * (sit.age in 50..59) +
                        b_age_789 * (sit.age >= 60) +
                        b_taet_arbeit * sit.isEmploymentFulltime +
                        b_taet_haus_arblos * (sit.isEmploymentHomekeeper || sit.isEmploymentUnemployed || sit.isEmploymentOther) +
                        b_eco_3 * (sit.householdEconomicStatus == 3) +
                        b_eco_5 * (sit.householdEconomicStatus == 5) +
                        b_pkwhh_0 * (sit.householdCars == 0) +
                        b_pkwhh_1 * (sit.householdCars == 1) +
                        b_pkwhh_2 * (sit.householdCars == 2) +
                        b_pkwhh_3 * (sit.householdCars == 3) +
                        b_regiotyp_1 * (sit.homeRegionType == 1) +
                        b_regiotyp_3 * (sit.homeRegionType == 3) +
                        b_regiotyp_567 * (sit.homeRegionType in 5..7) +
                        //        + b_carsharing * IS_CARSHARING_MEMBER
                        b_ticket * sit.hasCommuterTicket +
                        b_hasBike * sit.hasBike
                )
    }

}.multinomialLogit("Example Bikesharing Assignment Choice Model Structure").build(BikesharingParameters())
