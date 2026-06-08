package destination

import edu.kit.ifv.domain.simulation.behavior.carsPerAdult
import edu.kit.ifv.domain.simulation.behavior.isIntrazonal
import util.destinationChoiceStructure
import util.openMultinomialLogit
import util.times
import kotlin.math.ln
import kotlin.math.min


@Suppress("MagicNumber")
data class DestinationChoiceParameters(
    val asc_car_d: Double = 12.1687,
    val asc_car_p: Double = 11.0360,
    val asc_put: Double = 56.3941 - 46.1231,
    val asc_ped: Double = 12.9224,
    val asc_bike: Double = 11.2774,
    val asc_cs_sb: Double = -1.9785,
    val asc_cs_ff: Double = -0.1539,
    val asc_taxi: Double = 5.7729,
    val asc_bs: Double = 0.0000,
    val asc_rp: Double = 9.7748,
    val b_tt_car_d: Double = -0.0688 - 0.05,
    val b_tt_car_p: Double = -0.0981 - 0.05,
    val b_tt_put: Double = -0.0306 - 0.05 - 0.01,
    val b_tt_ped: Double = -0.1149 - 0.05,
    val b_tt_bike: Double = -0.0983 - 0.03,
    val b_tt_cs: Double = -0.0005 - 0.05,
    val b_tt_taxi: Double = -0.0785,
    val b_tt_rp: Double = -0.0450,
    val b_cost: Double = -0.1283,
    val b_logsum_acc_put: Double = -46.1231,
    val elasticity_acc_put: Double = -0.0109,
    val b_logsum_acc_cs: Double = 2.1892,
    val b_tt_acc_put: Double = -0.1,

    val b_logsum_pt: Double = 0.121207659519885 + 0.4 + 0.05,
    val b_logsum_drive: Double = 0.676141451176608 + 0.4 + 0.05,
    val b_logsum_pt_fix: Double = 0.0528052655209264 + 0.4 + 0.1,
    val b_logsum_drive_fix: Double = 1.17420861373612 + 0.1,
    val b_attr: Double = 0.0468260637125192 + 0.4 + 0.2 + 0.1 + 0.05 + 0.1,
    val b_0_1: Double = 0.573527127973948 - 0.2 - 0.1,
    val b_1_2: Double = 0.141529868206184 + 0.2,
    val b_intrazonal: Double = 1.78552214859153 - 0.4 - 0.4,
    val shift_b_0_1_on_logsum_attr: Double = 0.0423004157074064 - 0.02,
    val shift_b_1_2_on_logsum_attr: Double = 0.0399905192005718,
    val shift_intrazonal_on_attr: Double = -0.00134395543657435,
    val shift_purp_on_logsum_pt: Double = 0.0,
    val shift_purp_on_logsum_pt_fix: Double = -0.086506565532863,

    val shift_purp_on_logsum_drive: Double = -0.333772415392964, // -
    val shift_purp_on_logsum_drive_fix: Double = 0.0, //-
    val shift_purp_on_logsum_attr: Double = 0.223682400543822, //-

    val shift_age_1_on_logsum_pt: Double = 0.0197917514533555,
    val shift_age_1_on_logsum_drive: Double = -0.212214929364726,
    val shift_age_1_on_logsum_pt_fix: Double = 0.0,
    val shift_age_1_on_logsum_drive_fix: Double = 0.166617952051142,
    val shift_age_1_on_logsum_attr: Double = 0.0,
    val shift_age_56_on_logsum_pt: Double = 0 - 0.1,
    val shift_age_56_on_logsum_drive: Double = -0.00830418855417398 - 0.1,
    val shift_age_56_on_logsum_pt_fix: Double = 0 - 0.1,
    val shift_age_56_on_logsum_drive_fix: Double = -0.400267845906172,
    val shift_age_56_on_logsum_attr: Double = -0.0234450835628972,
    val shift_age_78_on_logsum_pt: Double = -0.0680740592576354 - 0.1,
    val shift_age_78_on_logsum_drive: Double = 0.573115561474273 - 0.05,
    val shift_age_78_on_logsum_pt_fix: Double = 0.0204348690149522 - 0.1,
    val shift_age_78_on_logsum_drive_fix: Double = -0.516742959319184,
    val shift_age_78_on_logsum_attr: Double = +0.05,
    val shift_educ_on_logsum_pt: Double = -0.0670975503303656 - 0.1,
    val shift_educ_on_logsum_drive: Double = 0.384057199912957 - 0.1,
    val shift_educ_on_logsum_pt_fix: Double = 0.0,
    val shift_educ_on_logsum_drive_fix: Double = -0.267470222054686,
    val shift_educ_on_logsum_attr: Double = 0.0160511342350424,
    val shift_home_on_logsum_pt: Double = -0.00207975059158103,
    val shift_home_on_logsum_drive: Double = 0.163879886055229,
    val shift_home_on_logsum_pt_fix: Double = 0.0,
    val shift_home_on_logsum_drive_fix: Double = -0.328854765928573,
    val shift_home_on_logsum_attr: Double = -0.0219782558948271,
    val shift_high_inc_on_logsum_pt: Double = -0.0446382645175651,
    val shift_high_inc_on_logsum_drive: Double = 0.274158837991419,
    val shift_high_inc_on_logsum_pt_fix: Double = 0.042487393274267,
    val shift_high_inc_on_logsum_drive_fix: Double = -0.236217936654311,
    val shift_high_inc_on_logsum_attr: Double = -0.0679476284749911 + 0.1,
    val shift_zk_on_logsum_pt: Double = 0.0,
    val shift_zk_on_logsum_drive: Double = -0.3715116326835,
    val shift_zk_on_logsum_pt_fix: Double = 0.0313030436765559,
    val shift_zk_on_logsum_drive_fix: Double = 0.0,
    val shift_zk_on_logsum_attr: Double = 0.0343308062606029 - 0.1,
    val shift_carav_on_logsum_pt: Double = -0.0310066987706901 - 0.1,
    val shift_carav_on_logsum_drive: Double = 0.179999442849862,
    val shift_carav_on_logsum_pt_fix: Double = 0.0,
    val shift_carav_on_logsum_drive_fix: Double = 0.0682756735553359,
    val shift_carav_on_logsum_attr: Double = 0.0300196660347988 - 0.1,
    val shift_nocar_on_logsum_pt: Double = 0 + 0.1 + 0.2,
    val shift_nocar_on_logsum_drive: Double = 0.0,
    val shift_nocar_on_logsum_pt_fix: Double = 0 + 0.2 + 0.1,
    val shift_nocar_on_logsum_drive_fix: Double = 0.0,

    val shift_uml_on_logsum_pt: Double = 0.05 + 0.1 + 0.2, //-
    val shift_uml_on_logsum_drive: Double = 0.05 + 0.1 + 0.2, //-
    val shift_uml_on_logsum_pt_fix: Double = 0.1 + 0.2 + 0.2, //-
    val shift_uml_on_logsum_drive_fix: Double = 0.1 + 0.2 + 0.2, //-
    val shift_uml_on_logsum_attr: Double = 0 + 0.2, //-

    val max_attractivity: Double = 1000000.0,
)


val tutorialDestinationChoiceModel = destinationChoiceStructure<DestinationChoiceParameters> { sit -> (

        (
                b_attr
                        + shift_age_1_on_logsum_attr * (sit.age in 0..17)
                        + shift_age_56_on_logsum_attr * (sit.age in 50..69)
                        + shift_age_78_on_logsum_attr * (sit.age in 70..120)
                        + shift_educ_on_logsum_attr * (sit.employmentIsSomeEducationType)
                        + shift_home_on_logsum_attr * (sit.isEmploymentHomekeeper || sit.isEmploymentUnemployed)
                        + shift_zk_on_logsum_attr * sit.hasCommuterTicket
                        + shift_carav_on_logsum_attr * (sit.carsPerAdult >= 1.0)
                        + shift_high_inc_on_logsum_attr * (sit.householdEconomicStatus in 4..5)
                        //+ shift_uml_on_logsum_attr * IS_UMLAND
                        + shift_b_0_1_on_logsum_attr * (sit.travelDistanceKm in 0.0..1.0)
                        + shift_b_1_2_on_logsum_attr * (sit.travelDistanceKm in 1.0 .. 2.0)
                        + shift_intrazonal_on_attr * sit.isIntrazonal
                ) * ln(min(max_attractivity, sit.attractivity)) +

                b_0_1 * (sit.travelDistanceKm in 0.0..1.0) +
                b_1_2 * (sit.travelDistanceKm in 1.0..2.0) +
                b_intrazonal * sit.isIntrazonal +

                ( // b_logsum_pt
                        b_logsum_pt
                                + shift_purp_on_logsum_pt
                                + shift_age_1_on_logsum_pt * (sit.age in 0..17)
                                + shift_age_56_on_logsum_pt * (sit.age in 50..69)
                                + shift_age_78_on_logsum_pt * (sit.age in 70..120)
                                + shift_educ_on_logsum_pt * (sit.employmentIsSomeEducationType)
                                + shift_home_on_logsum_pt * (sit.isEmploymentHomekeeper || sit.isEmploymentUnemployed)
                                + shift_zk_on_logsum_pt * sit.hasCommuterTicket
                                + shift_carav_on_logsum_pt * (sit.carsPerAdult >= 1.0)
                                + shift_nocar_on_logsum_pt * (sit.householdCars == 0)
                                + shift_high_inc_on_logsum_pt * (sit.householdEconomicStatus in 4..5)
                        //+ shift_uml_on_logsum_pt * IS_UMLAND
                        ) * sit.logsumDestPt +


                ( // b_logsum_drive
                        b_logsum_drive
                                + shift_age_1_on_logsum_drive * (sit.age in 0..17)
                                + shift_age_56_on_logsum_drive * (sit.age in 50..69)
                                + shift_age_78_on_logsum_drive * (sit.age in 70..120)
                                + shift_educ_on_logsum_drive * (sit.employmentIsSomeEducationType)
                                + shift_home_on_logsum_drive * (sit.isEmploymentHomekeeper || sit.isEmploymentUnemployed)
                                + shift_zk_on_logsum_drive * sit.hasCommuterTicket
                                + shift_carav_on_logsum_drive * (sit.carsPerAdult >= 1.0)
                                + shift_nocar_on_logsum_drive * (sit.householdCars == 0)
                                + shift_high_inc_on_logsum_drive * (sit.householdEconomicStatus in 4..5)
                        // + shift_uml_on_logsum_drive * IS_UMLAND
                        ) * sit.logsumDestDrive +


                ( // b_logsum_pt_fix
                        b_logsum_pt_fix
                                + shift_purp_on_logsum_pt_fix
                                + shift_age_1_on_logsum_pt_fix * (sit.age in 0..17)
                                + shift_age_56_on_logsum_pt_fix * (sit.age in 50..69)
                                + shift_age_78_on_logsum_pt_fix * (sit.age in 70..120)
                                + shift_educ_on_logsum_pt_fix * sit.employmentIsSomeEducationType
                                + shift_home_on_logsum_pt_fix * (sit.isEmploymentHomekeeper || sit.isEmploymentUnemployed)
                                + shift_zk_on_logsum_pt_fix * sit.hasCommuterTicket
                                + shift_carav_on_logsum_pt_fix * (sit.carsPerAdult >= 1.0)
                                + shift_nocar_on_logsum_pt_fix * (sit.householdCars == 0)
                                + shift_high_inc_on_logsum_pt_fix * (sit.householdEconomicStatus in 4..5)
                        //+ shift_uml_on_logsum_pt_fix * IS_UMLAND
                        ) * sit.logsumFixDestPt + // LOGSUM_FIX_DEST_PT


                ( // b_logsum_drive_fix
                        b_logsum_drive_fix
                                + shift_age_1_on_logsum_drive_fix * (sit.age in 0..17)
                                + shift_age_56_on_logsum_drive_fix * (sit.age in 50..69)
                                + shift_age_78_on_logsum_drive_fix * (sit.age in 70..120)
                                + shift_educ_on_logsum_drive_fix * sit.employmentIsSomeEducationType
                                + shift_home_on_logsum_drive_fix * (sit.isEmploymentHomekeeper || sit.isEmploymentUnemployed)
                                + shift_zk_on_logsum_drive_fix * sit.hasCommuterTicket
                                + shift_carav_on_logsum_drive_fix * (sit.carsPerAdult >= 1)
                                + shift_nocar_on_logsum_drive_fix * (sit.householdCars == 0)
                                + shift_high_inc_on_logsum_drive_fix * (sit.householdEconomicStatus in 4..5)
                        //+ shift_uml_on_logsum_drive_fix * IS_UMLAND
                        ) * sit.logsumFixDestPt
        // LOGSUM_FIX_DEST_DRIVE



        )}.openMultinomialLogit(
    "Example Destination Choice Model"
).build(
    DestinationChoiceParameters()
)
