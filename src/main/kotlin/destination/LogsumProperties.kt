package destination


import edu.kit.ifv.domain.simulation.behavior.DestinationAlternative
import util.bike
import util.bikesharing
import util.car
import util.passenger
import util.pedestrian
import util.publictransport
import util.times
import kotlin.math.exp
import kotlin.math.ln


val DestinationAlternative.logsumDest: Double get() =
    ln(
        exp(utilityPed) +
                exp(utilityBike) +
                exp(utilityCarPassenger) +
                isCarAvailable * exp(utilityCarDriver) +
                exp(utilityPut) +
                // kein TAXI, kein carsharing
                isBikesharingAvailable * exp(utilityBikeSharing) //kein RIDE_POOLING
    ) + if (
        isPedAvailable ||
        isBikeAvailable ||
        isPassengerAvailable ||
        isCarAvailable ||
        isPutAvailable ||
        isBikesharingAvailable
    ) {
        0.0
    } else {
        -50.0
    }


val DestinationAlternative.logsumDestPt: Double get() =
    ln(
        exp(utilityPed) +
                exp(utilityBike) +
                exp(utilityPut) +
                isBikesharingAvailable * exp(utilityBikeSharing)
    ) + if (isPedAvailable || isBikeAvailable || isPutAvailable) { //bikesharing missing here?
        0.0
    } else {
        -50.0
    }


val DestinationAlternative.logsumDestDrive: Double get() =
    ln(
        isCarAvailable * exp(utilityCarDriver)
        //KEIN CARSHARING
    ) + if (isCarAvailable) {
        0.0
    } else {
        -50.0
    }

val DestinationAlternative.logsumDestRide: Double get() =
    ln(
        exp(utilityCarPassenger) //kein TAXI, kein RIDEPOOLING
    ) + if (isPassengerAvailable) 0.0 else -50.0


val DestinationAlternative.logsumFixDest: Double get() =
    ln(
        exp(utilityFixPed) +
                exp(utilityFixBike) +
                exp(utilityFixCarPassenger) +
                isCarAvailable * exp(utilityFixCarDriver) +
                exp(utilityFixPut) +
                //kein Taxi, kein Carsharing
                isBikesharingAvailable * exp(utilityFixBikeSharing)
        //kein Ridepooling
    ) + if (
        isPedAvailable ||
        isBikeAvailable ||
        isPassengerAvailable ||
        isCarAvailable ||
        isPutAvailable ||
        isBikesharingAvailable
    ) {
        0.0
    } else {
        -50.0
    }


val DestinationAlternative.logsumFixDestPt: Double get() =
    ln(
        exp(utilityFixPed) +
                exp(utilityFixBike) +
                exp(utilityFixPut) +
                isBikesharingAvailable * exp(utilityFixBikeSharing)
    ) + if (isPedAvailable || isBikeAvailable || isPutAvailable || isBikesharingAvailable) {
        0.0
    } else {
        -50.0
    }

val DestinationAlternative.logsumFixDestDrive: Double get() =
    ln(
        isCarAvailable * exp(utilityFixCarDriver)
        //kein Carsharing
    ) + if (isCarAvailable) {
        0.0
    } else {
        -50.0
    }


val  DestinationAlternative.logsumFixDestRide: Double get() =
    ln(
        exp(utilityFixCarPassenger)
        //kein Taxi, kein Ridepooling
    ) + if (isPassengerAvailable) 0.0 else -50.0



private const val asc_car_d = 12.1687
private const val asc_car_p = 11.0360
private val asc_put = 56.3941 - 46.1231
private const val asc_ped = 12.9224
private const val asc_bike = 11.2774
private val asc_cs_sb = -1.9785
private val asc_cs_ff = -0.1539
private const val asc_bs = 0.0000
private val b_tt_car_d = -0.0688 - 0.05
private val b_tt_car_p = -0.0981 - 0.05
private val b_tt_put = -0.0306 - 0.05 - 0.01
private val b_tt_ped = -0.1149 - 0.05
private val b_tt_bike = -0.0983 - 0.03
private val b_tt_cs = -0.0005 - 0.05
private val b_cost = -0.1283


private val DestinationAlternative.utilityPed: Double get() =
    2.0 + asc_ped + b_tt_ped * travelTimeMinutes(pedestrian)

private val DestinationAlternative.utilityBike: Double get() =
    2.0 + asc_bike + b_tt_bike * travelTimeMinutes(bike)

private val DestinationAlternative.utilityPut: Double get() =
    2 + asc_put + b_tt_put * travelTimeMinutes(publictransport) + b_cost * travelCostEuro(publictransport)

private val DestinationAlternative.utilityCarPassenger: Double get() =
    2.0 + asc_car_p + b_tt_car_p * (3.0 + travelTimeMinutes(passenger))


private val DestinationAlternative.utilityCarDriver: Double get() =
    2 + asc_car_d + b_tt_car_d * travelTimeMinutes(car) + b_cost * travelCostEuro(car)

//private val DestinationAlternative.utilityCarSharingFree: Double get() =
//    2 + asc_cs_ff + b_tt_cs * (3.0 + travelTimeMinutes(csff)) + b_cost * travelCostEuro(csff)

//private val DestinationAlternative.utilityCarSharingStation: Double get() =
//    2 + asc_cs_sb + b_tt_cs * (3.0 + travelTimeMinutes(cssb)) + b_cost * travelCostEuro(cssb)

private val DestinationAlternative.utilityBikeSharing: Double get() =
    2 + asc_bs + b_tt_bike * travelTimeMinutes(bikesharing) + b_cost * travelCostEuro(bikesharing)



private val DestinationAlternative.utilityFixPed: Double get() =
    2.0 + asc_ped + b_tt_ped * travelTimeMinutesToFixed(pedestrian)

private val DestinationAlternative.utilityFixBike: Double get() =
    2.0 + asc_bike + b_tt_bike * travelTimeMinutesToFixed(bike)

private val DestinationAlternative.utilityFixPut: Double get() =
    2.0 + asc_put + b_tt_put * travelTimeMinutesToFixed(publictransport) + b_cost * travelCostEuroToFixed(publictransport)

private val DestinationAlternative.utilityFixCarPassenger: Double get() =
    2.0 + asc_car_p + b_tt_car_p * (3.0 + travelTimeMinutesToFixed(car))

private val DestinationAlternative.utilityFixCarDriver: Double get() =
    2.0 + asc_car_d + b_tt_car_d * travelTimeMinutesToFixed(car) + b_cost * travelCostEuroToFixed(car)

//private val DestinationAlternative.utilityFixCarSharingFree: Double get() =
//    2.0 + asc_cs_ff + b_tt_cs * (3.0 + travelTimeMinutesToFixed(csff)) + b_cost * travelCostEuroToFixed(csff)

//private val DestinationAlternative.utilityFixCarSharingStation: Double get() =
//    2.0 + asc_cs_sb + b_tt_cs * (3.0 + travelTimeMinutesToFixed(cssb)) + b_cost * travelCostEuroToFixed(cssb)

private val DestinationAlternative.utilityFixBikeSharing: Double get() =
    2 + asc_bs + b_tt_bike * travelTimeMinutesToFixed(bikesharing) + b_cost * travelCostEuroToFixed(bikesharing)

