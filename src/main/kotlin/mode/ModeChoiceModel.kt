package mode

import edu.kit.ifv.domain.shared.location.zone.MaximalZone
import edu.kit.ifv.domain.shared.location.zone.ZoneId
import edu.kit.ifv.domain.simulation.behavior.isFemale
import util.Mode
import util.ModeChoiceCharacteristics
import util.bike
import util.bikesharing
import util.car
import util.flatStructure
import util.minus
import util.multinomialLogit
import util.passenger
import util.pedestrian
import util.publictransport
import util.times

data class ModeChoiceParameters(
    val deactivated: Double = -9999.0,
    val asc_ped: Double = 1.2,
    val asc_bike: Double = -0.7,
    val asc_car_d: Double = 0.0 - 3.0,
    val asc_car_p: Double = -2.4,
    val asc_put: Double = -2.1,
    val asc_bs: Double = -2.0,
    val age_0_17_on_asc_car_d: Double = -4.4,
    val age_18_29_on_asc_car_d: Double = 0.3,
    val age_50_59_on_asc_car_d: Double = 0.5,
    val age_60_69_on_asc_car_d: Double = 0.8,
    val age_70_100_on_asc_car_d: Double = 1.0,
    val female_on_asc_car_d: Double = -0.5,
    val beruft_on_asc_car_d: Double = 0.3,
    val inc_high_on_asc_car_d: Double = 1.1,
    val zk_on_asc_car_d: Double = -1.2,
    val age_0_17_on_asc_put: Double = 1.4,
    val age_18_29_on_asc_put: Double = 0.8,
    val age_50_59_on_asc_put: Double = 0.3,
    val age_60_69_on_asc_put: Double = 0.4,
    val age_70_100_on_asc_put: Double = 0.9,
    val female_on_asc_put: Double = 0.2,
    val beruft_on_asc_put: Double = 0.0,
    val inc_high_on_asc_put: Double = 0.5,
    val zk_on_asc_put: Double = 1.7,
    val age_0_17_on_asc_bike: Double = -0.3,
    val age_18_29_on_asc_bike: Double = 0.5,
    val age_50_59_on_asc_bike: Double = 0.3,
    val age_60_69_on_asc_bike: Double = 0.0,
    val age_70_100_on_asc_bike: Double = 0.0,
    val female_on_asc_bike: Double = -0.1,
    val beruft_on_asc_bike: Double = 0.2,
    val inc_high_on_asc_bike: Double = 0.0,
    val zk_on_asc_bike: Double = -0.6,
    val age_0_17_on_asc_car_p: Double = 1.2,
    val age_18_29_on_asc_car_p: Double = 0.9,
    val age_50_59_on_asc_car_p: Double = 0.3,
    val age_60_69_on_asc_car_p: Double = 1.0,
    val age_70_100_on_asc_car_p: Double = 1.4,
    val female_on_asc_car_p: Double = 0.7,
    val beruft_on_asc_car_p: Double = 0.0,
    val inc_high_on_asc_car_p: Double = 1.0,
    val zk_on_asc_car_p: Double = -0.4,
    val b_tt_ped: Double = -0.1,
    val b_tt_bike: Double = -0.2,
    val b_tt_car_d: Double = -0.2,
    val b_tt_car_p: Double = -0.2,
    val b_tt_put: Double = -0.05,
    val b_acc_put: Double = -0.1,
    val b_u_put: Double = -0.6,
    val b_cost_car_d: Double = -0.2,
    val b_cost_put: Double = -0.5,
    val age_0_17_on_b_cost_car_d: Double = 0.0,
    val age_18_29_on_b_cost_car_d: Double = 0.0,
    val age_50_59_on_b_cost_car_d: Double = 0.0,
    val age_60_69_on_b_cost_car_d: Double = 0.0,
    val age_70_100_on_b_cost_car_d: Double = 0.0,
    val female_on_b_cost_car_d: Double = 0.0,
    val beruft_on_b_cost_car_d: Double = 0.06,
    val inc_high_on_b_cost_car_d: Double = 0.09,
    val zk_on_b_cost_car_d: Double = 0.0,
    val age_0_17_on_b_cost_put: Double = -0.2,
    val age_18_29_on_b_cost_put: Double = 0.0,
    val age_50_59_on_b_cost_put: Double = 0.0,
    val age_60_69_on_b_cost_put: Double = 0.0,
    val age_70_100_on_b_cost_put: Double = 0.0,
    val female_on_b_cost_put: Double = 0.0,
    val beruft_on_b_cost_put: Double = 0.0,
    val inc_high_on_b_cost_put: Double = 0.1,
    val zk_on_b_cost_put: Double = 0.0,
    val age_0_17_on_asc_ped: Double = 0.3,
    val age_18_29_on_asc_ped: Double = -0.1,
    val age_50_59_on_asc_ped: Double = 0.2,
    val age_60_69_on_asc_ped: Double = 0.2,
    val age_70_100_on_asc_ped: Double = -0.2,
    val beruft_on_asc_ped: Double = -0.1,
    val female_on_asc_ped: Double = 0.2,
    val inc_high_on_asc_ped: Double = -0.2,
    val zk_on_asc_ped: Double = 0.2
)

val modeChoiceScenario1 = ModeChoiceParameters(
    asc_bike = 25.5,
    b_tt_ped = 1.2,
)


fun tutorialModeChoiceModel(getZone: (ZoneId) -> MaximalZone) = flatStructure<Mode, ModeChoiceCharacteristics, ModeChoiceParameters> {

    option(pedestrian) { choice, sit ->
        (
                asc_ped +
                        age_0_17_on_asc_ped * (sit.age in 0..17) +
                        age_18_29_on_asc_ped * (sit.age in 18..29) +
                        age_50_59_on_asc_ped * (sit.age in 50..59) +
                        age_60_69_on_asc_ped * (sit.age in 60..69) +
                        age_70_100_on_asc_ped * (sit.age in 70..100) +
                        beruft_on_asc_ped * (sit.isEmploymentFulltime || sit.isEmploymentParttime || sit.isEmploymentMarginal) +
                        female_on_asc_ped * (sit.isFemale) +
                        inc_high_on_asc_ped * (sit.householdEconomicStatus in 4..5) +
                        zk_on_asc_ped * sit.hasCommuterTicket +
                        b_tt_ped * sit.travelTimeMinutes(pedestrian)
                )
    }

    option(bike) { choice, sit ->
        (
                asc_bike +
                        age_0_17_on_asc_bike * (sit.age in 0..17) +
                        age_18_29_on_asc_bike * (sit.age in 18..29) +
                        age_50_59_on_asc_bike * (sit.age in 50..59) +
                        age_60_69_on_asc_bike * (sit.age in 60..69) +
                        age_70_100_on_asc_bike * (sit.age in 70..100) +
                        beruft_on_asc_bike * (sit.isEmploymentFulltime || sit.isEmploymentParttime || sit.isEmploymentMarginal) +
                        female_on_asc_bike * (sit.isFemale) +
                        inc_high_on_asc_bike * (sit.householdEconomicStatus in 4..5) +
                        zk_on_asc_bike * sit.hasCommuterTicket +
                        b_tt_bike * sit.travelTimeMinutes(bike)
                )
    }

    option(bikesharing) { choice, sit ->
        (
                asc_bs +
                        age_0_17_on_asc_bike * (sit.age in 0..17) +
                        age_18_29_on_asc_bike * (sit.age in 18..29) +
                        age_50_59_on_asc_bike * (sit.age in 50..59) +
                        age_60_69_on_asc_bike * (sit.age in 60..69) +
                        age_70_100_on_asc_bike * (sit.age in 70..100) +
                        beruft_on_asc_bike * (sit.isEmploymentFulltime || sit.isEmploymentParttime || sit.isEmploymentMarginal) +
                        female_on_asc_bike * (sit.isFemale) +
                        inc_high_on_asc_bike * (sit.householdEconomicStatus in 4..5) +
                        zk_on_asc_bike * sit.hasCommuterTicket +
                        b_tt_bike * sit.travelTimeMinutes(bikesharing) +
                        (
                                b_cost_put +
                                        age_0_17_on_b_cost_put * (sit.age in 0..17) +
                                        age_18_29_on_b_cost_put * (sit.age in 18..29) +
                                        age_50_59_on_b_cost_put * (sit.age in 50..59) +
                                        age_60_69_on_b_cost_put * (sit.age in 60..69) +
                                        age_70_100_on_b_cost_put * (sit.age in 70..100) +
                                        beruft_on_b_cost_put    * (sit.isEmploymentFulltime || sit.isEmploymentParttime || sit.isEmploymentMarginal) +
                                        female_on_b_cost_put * (sit.isFemale) +
                                        inc_high_on_b_cost_put  * (sit.householdEconomicStatus in 4..5) +
                                        zk_on_b_cost_put        * sit.hasCommuterTicket
                                ) * sit.travelCostEuro(bikesharing)
                )
    }

    option(car) { choice, sit ->
        (
                asc_car_d +
                        age_0_17_on_asc_car_d * (sit.age in 0..17) +
                        age_18_29_on_asc_car_d * (sit.age in 18..29) +
                        age_50_59_on_asc_car_d * (sit.age in 50..59) +
                        age_60_69_on_asc_car_d * (sit.age in 60..69) +
                        age_70_100_on_asc_car_d * (sit.age in 70..100) +
                        beruft_on_asc_car_d * (sit.isEmploymentFulltime || sit.isEmploymentParttime || sit.isEmploymentMarginal) +
                        female_on_asc_car_d * (sit.isFemale) +
                        inc_high_on_asc_car_d  * (sit.householdEconomicStatus in 4..5) +
                        zk_on_asc_car_d        * sit.hasCommuterTicket +
                        b_tt_car_d * sit.travelTimeMinutes(car) +
                        sit.parkingPressureAtDestination(getZone) +
                        (
                                b_cost_car_d +
                                        age_0_17_on_b_cost_car_d * (sit.age in 0..17) +
                                        age_18_29_on_b_cost_car_d * (sit.age in 18..29) +
                                        age_50_59_on_b_cost_car_d * (sit.age in 50..59) +
                                        age_60_69_on_b_cost_car_d * (sit.age in 60..69) +
                                        age_70_100_on_b_cost_car_d * (sit.age in 70..100) +
                                        beruft_on_b_cost_car_d    * (sit.isEmploymentFulltime || sit.isEmploymentParttime || sit.isEmploymentMarginal) +
                                        female_on_b_cost_car_d * (sit.isFemale) +
                                        inc_high_on_b_cost_car_d  * (sit.householdEconomicStatus in 4..5) +
                                        zk_on_b_cost_car_d        * sit.hasCommuterTicket
                                ) * sit.travelCostEuro(car)
                )
    }

    option(passenger) { choice, sit ->
        (
                asc_car_p +
                        age_0_17_on_asc_car_p * (sit.age in 0..17) +
                        age_18_29_on_asc_car_p * (sit.age in 18..29) +
                        age_50_59_on_asc_car_p * (sit.age in 50..59) +
                        age_60_69_on_asc_car_p * (sit.age in 60..69) +
                        age_70_100_on_asc_car_p * (sit.age in 70..100) +
                        beruft_on_asc_car_p * (sit.isEmploymentFulltime || sit.isEmploymentParttime || sit.isEmploymentMarginal) +
                        female_on_asc_car_p * (sit.isFemale) +
                        inc_high_on_asc_car_p  * (sit.householdEconomicStatus in 4..5) +
                        zk_on_asc_car_p        * sit.hasCommuterTicket +
                        b_tt_car_p * sit.travelTimeMinutes(passenger)
                )
    }

    option(publictransport) { choice, sit ->
        (
                asc_put +
                        age_0_17_on_asc_put * (sit.age in 0..17) +
                        age_18_29_on_asc_put * (sit.age in 18..29) +
                        age_50_59_on_asc_put * (sit.age in 50..59) +
                        age_60_69_on_asc_put * (sit.age in 60..69) +
                        age_70_100_on_asc_put * (sit.age in 70..100) +
                        beruft_on_asc_put * (sit.isEmploymentFulltime || sit.isEmploymentParttime || sit.isEmploymentMarginal) +
                        female_on_asc_put * (sit.isFemale) +
                        inc_high_on_asc_put  * (sit.householdEconomicStatus in 4..5) +
                        zk_on_asc_put        * sit.hasCommuterTicket +
                        b_tt_put * (sit.travelTimeMinutes(passenger) + sit.accessTimeMinutesPut + sit.egressTimeMinutesPut) +

                        // NO PUT COST IF OWNS TICKET
                        (1 - sit.hasCommuterTicket) * (
                        b_cost_put +
                                age_0_17_on_b_cost_put * (sit.age in 0..17) +
                                age_18_29_on_b_cost_put * (sit.age in 18..29) +
                                age_50_59_on_b_cost_put * (sit.age in 50..59) +
                                age_60_69_on_b_cost_put * (sit.age in 60..69) +
                                age_70_100_on_b_cost_put * (sit.age in 70..100) +
                                beruft_on_b_cost_put    * (sit.isEmploymentFulltime || sit.isEmploymentParttime || sit.isEmploymentMarginal) +
                                female_on_b_cost_put * (sit.isFemale) +
                                inc_high_on_b_cost_put  * (sit.householdEconomicStatus in 4..5) +
                                zk_on_b_cost_put        * sit.hasCommuterTicket
                        ) * sit.travelCostEuro(publictransport)
                )

    }

}.multinomialLogit(
    name = "Example Mode Choice Model"
).build(
    parameters = ModeChoiceParameters()
)
