package cars

import util.flatStructure
import util.multinomialLogit
import util.times

data class NumberOfCarsParameters(
    val asc_0: Double = 0.0000,
    val asc_1: Double = 2.5656,
    val b_hhgro_1_on_1: Double = -0.9172,
    val b_hhgro_2_on_1: Double = 0.0000,
    val b_hhgro_567_on_1: Double = -0.9963,
    val b_anz_fs_0_on_1: Double = -2.9712,
    val b_anz_fs_2_on_1: Double = 0.0000,
    val b_hhtype_1_on_1: Double = -0.6062,
    val b_hhtype_2_on_1: Double = 0.0000,
    val b_eco_2_on_1: Double = 0.0000,
    val b_eco_3_on_1: Double = 0.2237,
    val b_eco_4_on_1: Double = 0.6463,
    val b_eco_5_on_1: Double = 0.7116,
    val b_regiotyp_1_on_1: Double = -1.2754,
    val b_regiotyp_3_on_1: Double = 0.0000,
    val b_regiotyp_567_on_1: Double = 0.7318,
    val asc_2: Double = 2.2696,
    val b_hhgro_1_on_2: Double = -2.8524,
    val b_hhgro_2_on_2: Double = 0.0000,
    val b_hhgro_3_on_2: Double = 0.6034,
    val b_hhgro_4_on_2: Double = 0.7991,
    val b_anz_fs_0_on_2: Double = -4.1570,
    val b_anz_fs_1_on_2: Double = -0.3134,
    val b_anz_fs_2_on_2: Double = 0.0000,
    val b_hhtype_1_on_2: Double = -0.3598,
    val b_hhtype_2_on_2: Double = 0.0000,
    val b_hhtype_3_on_2: Double = 0.4579,
    val b_eco_1_on_2: Double = -0.5506,
    val b_eco_2_on_2: Double = 0.0000,
    val b_eco_4_on_2: Double = 0.9709,
    val b_eco_5_on_2: Double = 1.5162,
    val b_regiotyp_1_on_2: Double = -2.3534,
    val b_regiotyp_3_on_2: Double = 0.0000,
    val b_regiotyp_567_on_2: Double = 1.0567,
    val asc_3: Double = -1.4692,
    val b_hhgro_1_on_3: Double = -2.2900,
    val b_hhgro_2_on_3: Double = 0.0000,
    val b_hhgro_3_on_3: Double = 1.4586,
    val b_hhgro_4_on_3: Double = 2.2219,
    val b_hhgro_567_on_3: Double = 1.9609,
    val b_anz_fs_0_on_3: Double = -3.5967,
    val b_anz_fs_2_on_3: Double = 0.0000,
    val b_anz_fs_3_on_3: Double = 1.5574,
    val b_hhtype_1_on_3: Double = 1.0676,
    val b_hhtype_2_on_3: Double = 0.0000,
    val b_hhtype_3_on_3: Double = 1.6504,
    val b_hhtype_4_on_3: Double = 1.4287,
    val b_eco_1_on_3: Double = -0.9656,
    val b_eco_2_on_3: Double = 0.0000,
    val b_eco_4_on_3: Double = 1.0234,
    val b_eco_5_on_3: Double = 2.1141,
    val b_regiotyp_1_on_3: Double = -2.5682,
    val b_regiotyp_3_on_3: Double = 0.0000,
    val b_regiotyp_567_on_3: Double = 1.3789
)

val carChoiceModel = flatStructure<Int, NumberOfCarsCharacteristics, NumberOfCarsParameters> {

    option(0) { choice, sit ->
        asc_0
    }

    option(1) { choice, sit ->
        (
                1.0 * (sit.regioStaRGem5Code in 54..55) +

                        asc_1 +
                        b_hhgro_1_on_1 * (sit.householdSize == 1) +
                        b_hhgro_2_on_1 * (sit.householdSize == 2) +
                        b_hhgro_567_on_1 * (sit.householdSize in 5..7) +
                        b_eco_2_on_1 * (sit.householdEconomicStatus == 2) +
                        b_eco_3_on_1 * (sit.householdEconomicStatus == 3) +
                        b_eco_4_on_1 * (sit.householdEconomicStatus == 4) +
                        b_eco_5_on_1 * (sit.householdEconomicStatus == 5) +
                        b_anz_fs_0_on_1 * (sit.householdNumberOfLicense == 0) +
                        b_anz_fs_2_on_1 * (sit.householdNumberOfLicense == 2) +
                        b_hhtype_1_on_1 * (sit.householdType == 1) +
                        b_hhtype_2_on_1 * (sit.householdType == 2) +
                        b_regiotyp_1_on_1 * (sit.homeRegionType == 1) +
                        b_regiotyp_3_on_1 * (sit.homeRegionType == 3) +
                        b_regiotyp_567_on_1 * (sit.homeRegionType in 5..7)
                )
    }

    option(2) { choice, sit ->
        (
                asc_2 +
                        b_hhgro_1_on_2 * (sit.householdSize == 1) +
                        b_hhgro_2_on_2 * (sit.householdSize == 2) +
                        b_hhgro_3_on_2 * (sit.householdSize == 3) +
                        b_hhgro_4_on_2 * (sit.householdSize == 4) +
                        b_eco_1_on_2 * (sit.householdEconomicStatus == 1) +
                        b_eco_2_on_2 * (sit.householdEconomicStatus == 2) +
                        b_eco_4_on_2 * (sit.householdEconomicStatus == 4) +
                        b_eco_5_on_2 * (sit.householdEconomicStatus == 5) +
                        b_anz_fs_0_on_2 * (sit.householdNumberOfLicense == 0) +
                        b_anz_fs_1_on_2 * (sit.householdNumberOfLicense == 1) +
                        b_anz_fs_2_on_2 * (sit.householdNumberOfLicense == 2) +
                        b_hhtype_1_on_2 * (sit.householdType == 1) +
                        b_hhtype_2_on_2 * (sit.householdType == 2) +
                        b_hhtype_3_on_2 * (sit.householdType == 3) +
                        b_regiotyp_1_on_2 * (sit.homeRegionType == 1) +
                        b_regiotyp_3_on_2 * (sit.homeRegionType == 3) +
                        b_regiotyp_567_on_1 * (sit.homeRegionType in 5..7)
                )
    }

    option(3) { choice, sit ->
        (
                asc_3 +
                        b_hhgro_1_on_3 * (sit.householdSize == 1) +
                        b_hhgro_2_on_3 * (sit.householdSize == 2) +
                        b_hhgro_3_on_3 * (sit.householdSize == 3) +
                        b_hhgro_4_on_3 * (sit.householdSize == 4) +
                        b_hhgro_567_on_3 * (sit.householdSize in 5..7) +
                        b_eco_1_on_3 * (sit.householdEconomicStatus == 1) +
                        b_eco_2_on_3 * (sit.householdEconomicStatus == 2) +
                        b_eco_4_on_3 * (sit.householdEconomicStatus == 4) +
                        b_eco_5_on_3 * (sit.householdEconomicStatus == 5) +
                        b_anz_fs_0_on_3 * (sit.householdNumberOfLicense == 0) +
                        b_anz_fs_2_on_3 * (sit.householdNumberOfLicense == 2) +
                        b_anz_fs_3_on_3 * (sit.householdNumberOfLicense == 3) +
                        b_hhtype_1_on_3 * (sit.householdType == 1) +
                        b_hhtype_2_on_3 * (sit.householdType == 2) +
                        b_hhtype_3_on_3 * (sit.householdType == 3) +
                        b_hhtype_4_on_3 * (sit.householdType == 4) +
                        b_regiotyp_1_on_3 * (sit.homeRegionType == 1) +
                        b_regiotyp_3_on_3 * (sit.homeRegionType == 3) +
                        b_regiotyp_567_on_3 * (sit.homeRegionType in 5..7)
                )
    }


}.multinomialLogit("ExampleCarOwnershipModel").build(NumberOfCarsParameters())
