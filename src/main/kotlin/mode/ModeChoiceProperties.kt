package mode

import destination.purpose
import edu.kit.ifv.domain.shared.behavior.AttractivenessFromCsv
import edu.kit.ifv.domain.shared.behavior.AttractivenessModel
import edu.kit.ifv.domain.shared.datastructure.matrix.VisumMatrixCreator
import edu.kit.ifv.domain.shared.datastructure.matrix.ZoneIdMatrix
import edu.kit.ifv.domain.shared.enums.ActivityType
import edu.kit.ifv.domain.shared.enums.household.EconomicStatus
import edu.kit.ifv.domain.shared.enums.person.Employment
import edu.kit.ifv.domain.shared.enums.person.Sex
import edu.kit.ifv.domain.shared.location.StandardLocation
import edu.kit.ifv.domain.shared.location.StandardLocation.Companion.LOCATIONUNKNOWN
import edu.kit.ifv.domain.shared.location.zone.MaximalZone
import edu.kit.ifv.domain.shared.location.zone.ZoneId
import edu.kit.ifv.domain.simulation.behavior.DestinationAlternative
import edu.kit.ifv.domain.simulation.behavior.minutes
import edu.kit.ifv.domain.simulation.data.person.isAdult
import edu.kit.ifv.utils.units.AbsoluteTime
import util.Mode
import util.ModeChoiceCharacteristics
import util.TutorialActivity
import util.bike
import util.bikesharing
import util.car
import util.euros
import util.passenger
import util.pedestrian
import util.publictransport
import java.time.DayOfWeek
import kotlin.io.path.Path
import kotlin.math.abs
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

private val access: ZoneIdMatrix by lazy { VisumMatrixCreator.createMatrix(Path("data/matrix/travel_times/PUT_ACT.mtx.bz2")) }
private val egress: ZoneIdMatrix by lazy { VisumMatrixCreator.createMatrix(Path("data/matrix/travel_times/PUT_EGT.mtx.bz2")) }
val ModeChoiceCharacteristics.accessTimeMinutesPut: Double get() = access[origin.zoneId, destination.zoneId].minutes.inWholeMinutes.toDouble()
val ModeChoiceCharacteristics.egressTimeMinutesPut: Double get() = egress[origin.zoneId, destination.zoneId].minutes.inWholeMinutes.toDouble()

//val ModeChoiceCharacteristics.topographyMeters: Double get() = max(origin.requireZone().relief.inMeters, destination.requireZone().relief.inMeters)

private val WEEKEND = listOf<DayOfWeek>(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
val ModeChoiceCharacteristics.isWeekend: Boolean get() = time.weekDay in WEEKEND
val ModeChoiceCharacteristics.isWorkday: Boolean get() = time.weekDay !in WEEKEND

private val attractiveness: AttractivenessModel by lazy {
    AttractivenessFromCsv(
        Path("data/zone-repository/attractivities.csv"),
        work = TutorialActivity.WORK,
        privateVisit = TutorialActivity.PRIVATE_VISIT,
        activityTypes = TutorialActivity.entries.toSet(),
    )
}

fun ModeChoiceCharacteristics.parkingPressureAtDestination(getZone: (ZoneId) -> MaximalZone): Double = destination.zoneId.let {
    val attr =  attractiveness.attractivenessFor(it, TutorialActivity.WORK).value +
                attractiveness.attractivenessFor(it, TutorialActivity.PRIVATE_VISIT).value

    val parking = getZone(it).parkingPlaces

    if (parking == 0) {
        if (abs(attr) < 1e-6f) {
            0.0
        } else {
            999.0
        }
    } else {
        attr / parking
    }
}


val ModeChoiceCharacteristics.age: Int get() = person.age
val ModeChoiceCharacteristics.hasBike: Boolean get() = person.hasBike
val ModeChoiceCharacteristics.hasCommuterTicket: Boolean get() = person.hasCommuterTicket
val ModeChoiceCharacteristics.hasLicense: Boolean get() = person.hasLicense
val ModeChoiceCharacteristics.sex: Int get() = person.sex.code
val ModeChoiceCharacteristics.isFemale: Boolean get() = (person.sex == Sex.FEMALE)
val ModeChoiceCharacteristics.isMale: Boolean get() = (person.sex == Sex.MALE)
val ModeChoiceCharacteristics.incomeEuro: Double  get() = person.income.euros

val ModeChoiceCharacteristics.graduation: Int  get() = person.graduation.code
//TODO more choice.graduation upon request

val ModeChoiceCharacteristics.employment: Int get() = person.employment.code
val ModeChoiceCharacteristics.isEmploymentUnknown: Boolean get() = (person.employment == Employment.UNKNOWN)
val ModeChoiceCharacteristics.isEmploymentFulltime: Boolean get() = (person.employment == Employment.FULLTIME)
val ModeChoiceCharacteristics.isEmploymentParttime: Boolean get() = (person.employment == Employment.PARTTIME)
val ModeChoiceCharacteristics.isEmploymentMarginal: Boolean get() = (person.employment == Employment.MARGINAL)
val ModeChoiceCharacteristics.isEmploymentUnemployed: Boolean get() = (person.employment == Employment.UNEMPLOYED)
val ModeChoiceCharacteristics.isEmploymentStudent: Boolean get() = (person.employment == Employment.STUDENT)
val ModeChoiceCharacteristics.isEmploymentStudentPrimary: Boolean get() = (person.employment == Employment.STUDENT_PRIMARY)
val ModeChoiceCharacteristics.isEmploymentStudentSecondary: Boolean get() = (person.employment == Employment.STUDENT_SECONDARY)
val ModeChoiceCharacteristics.isEmploymentStudentTertiary: Boolean get() = (person.employment == Employment.STUDENT_TERTIARY)
val ModeChoiceCharacteristics.isEmploymentEducation: Boolean get() = (person.employment == Employment.EDUCATION)
val ModeChoiceCharacteristics.isEmploymentHomekeeper: Boolean get() = (person.employment == Employment.HOMEKEEPER)
val ModeChoiceCharacteristics.isEmploymentRetired: Boolean get() = (person.employment == Employment.RETIRED)
val ModeChoiceCharacteristics.isEmploymentInfant: Boolean get() = (person.employment == Employment.INFANT)
val ModeChoiceCharacteristics.isEmploymentNone: Boolean get() = (person.employment == Employment.NONE)

val ModeChoiceCharacteristics.householdType: Int get() = person.household.type
val ModeChoiceCharacteristics.householdIncomeEuro: Double get() = person.household.incomePerMonth.inEuros
val ModeChoiceCharacteristics.householdEconomicStatus: Int get() = person.household.economicStatus.code
val ModeChoiceCharacteristics.isHouseholdEconomicStatusVeryLow: Boolean get() = (person.household.economicStatus == EconomicStatus.VERY_LOW)
val ModeChoiceCharacteristics.isHouseholdEconomicStatusLow: Boolean get() = (person.household.economicStatus == EconomicStatus.LOW)
val ModeChoiceCharacteristics.isHouseholdEconomicStatusMiddle: Boolean get() = (person.household.economicStatus == EconomicStatus.MIDDLE)
val ModeChoiceCharacteristics.isHouseholdEconomicStatusHigh: Boolean get() = (person.household.economicStatus == EconomicStatus.HIGH)
val ModeChoiceCharacteristics.isHouseholdEconomicStatusVeryHigh: Boolean get() = (person.household.economicStatus == EconomicStatus.VERY_HIGH)
//income class?
val ModeChoiceCharacteristics.householdSize: Int get() = person.household.members.size
val ModeChoiceCharacteristics.householdCars: Int get() = person.household.cars.size
val ModeChoiceCharacteristics.adultsInHousehold: Int get() = person.household.members.count { it.isAdult }


fun ModeChoiceCharacteristics.isAgeIn(from: Int, to: Int) = (age in from..to)
fun ModeChoiceCharacteristics.travelTimeMinutes(mode: Mode): Double = impedance.duration(origin, destination, mode, time).minutes
fun ModeChoiceCharacteristics.travelCostEuro(mode: Mode): Double = impedance.cost(origin, destination, mode, time).euros
fun ModeChoiceCharacteristics.travelDistanceKm(mode: Mode): Double = impedance.distance(origin, destination, mode).inKilometers

private val ModeChoiceCharacteristics.nextFixedActivity
    get() = person.schedule.activities().find { it.location != LOCATIONUNKNOWN }

private val ModeChoiceCharacteristics.nextFixedDestination: StandardLocation
    get() = nextFixedActivity?.location ?: person.household.location

private val ModeChoiceCharacteristics.nextFixedActivityEnd: AbsoluteTime
    get() = nextFixedActivity?.endTime ?: time.plus(7.hours)

fun ModeChoiceCharacteristics.travelTimeMinutesToFixed(mode: Mode): Double =
    impedance.duration(destination, nextFixedDestination, mode, nextFixedActivityEnd).minutes

fun ModeChoiceCharacteristics.travelCostEuroToFixed(mode: Mode): Double =
    impedance.cost(destination, nextFixedDestination, mode, nextFixedActivityEnd).euros

private val ModeChoiceCharacteristics.prevMode: Mode?
    get() = person.schedule.pastLegs().lastOrNull()?.transportType

val ModeChoiceCharacteristics.previousMode: Int get() = prevMode?.code ?: -1
val ModeChoiceCharacteristics.isPrevModeBike: Boolean get() = (prevMode == bike)
val ModeChoiceCharacteristics.isPrevModeCar: Boolean get() = (prevMode == car)
val ModeChoiceCharacteristics.isPrevModePassenger: Boolean get() = (prevMode == passenger)
val ModeChoiceCharacteristics.isPrevModePedestrian: Boolean get() = (prevMode == pedestrian)
val ModeChoiceCharacteristics.isPrevModePublicTransport: Boolean get() = (prevMode == publictransport)
val ModeChoiceCharacteristics.isPrevModePublicBikesharing: Boolean get() = (prevMode == bikesharing)

val ModeChoiceCharacteristics.purpose: ActivityType get() = person.schedule.activities().first().type
val ModeChoiceCharacteristics.isPurposeWork: Boolean get() = (purpose == TutorialActivity.WORK)
val ModeChoiceCharacteristics.isPurposeBusiness: Boolean get() = (purpose == TutorialActivity.BUSINESS)
val ModeChoiceCharacteristics.isPurposeEducationPrimary: Boolean get() = (purpose == TutorialActivity.EDUCATION_PRIMARY)
val ModeChoiceCharacteristics.isPurposeEducationSecondary: Boolean get() = (purpose == TutorialActivity.EDUCATION_SECONDARY)
val ModeChoiceCharacteristics.isPurposeEducationTertiary: Boolean get() = (purpose == TutorialActivity.EDUCATION_TERTIARY)
val ModeChoiceCharacteristics.isPurposeShoppingDaily: Boolean get() = (purpose == TutorialActivity.SHOPPING_DAILY)
val ModeChoiceCharacteristics.isPurposeShoppingOther: Boolean get() = (purpose == TutorialActivity.SHOPPING_OTHER)
val ModeChoiceCharacteristics.isPurposeLeisureIndoor: Boolean get() = (purpose == TutorialActivity.LEISURE_INDOOR)
val ModeChoiceCharacteristics.isPurposeLeisureOutdoor: Boolean get() = (purpose == TutorialActivity.LEISURE_OUTDOOR)
val ModeChoiceCharacteristics.isPurposeService: Boolean get() = (purpose == TutorialActivity.SERVICE)
val ModeChoiceCharacteristics.isPurposeHome: Boolean get() = (purpose == TutorialActivity.HOME)
val ModeChoiceCharacteristics.isPurposePrivateBusiness: Boolean get() = (purpose == TutorialActivity.PRIVATE_BUSINESS)
val ModeChoiceCharacteristics.isPurposePrivateVisit: Boolean get() = (purpose == TutorialActivity.PRIVATE_VISIT)
val ModeChoiceCharacteristics.isPurposeUndefined: Boolean get() = (purpose == TutorialActivity.UNDEFINED)


private val fixedPurposes = listOf(
    TutorialActivity.HOME,
    TutorialActivity.WORK,
    TutorialActivity.EDUCATION_PRIMARY,
    TutorialActivity.EDUCATION_SECONDARY,
    TutorialActivity.EDUCATION_TERTIARY
)
val DestinationAlternative.isFixedLocationPurpose: Boolean get() = (purpose in fixedPurposes)

private val shoppingPurposes = listOf(
    TutorialActivity.SHOPPING_OTHER,
    TutorialActivity.SHOPPING_DAILY,
)
val DestinationAlternative.isAnyShoppingPurpose: Boolean get() = (purpose in shoppingPurposes)

private val educationPurposes = listOf(
    TutorialActivity.SHOPPING_OTHER,
    TutorialActivity.SHOPPING_DAILY,
)
val DestinationAlternative.isAnyEducationPurpose: Boolean get() = (purpose in educationPurposes)

private val leisurePurposes = listOf(
    TutorialActivity.SHOPPING_OTHER,
    TutorialActivity.SHOPPING_DAILY,
)
val DestinationAlternative.isAnyLeisurePurpose: Boolean get() = (purpose in leisurePurposes)


//val ModeChoiceCharacteristics.age: Int get() = person.age
//val ModeChoiceCharacteristics.hasBike: Boolean get() = person.hasBike
//val ModeChoiceCharacteristics.hasCommuterTicket: Boolean get() = person.hasCommuterTicket
//val ModeChoiceCharacteristics.hasLicense: Boolean get() = person.hasLicense
//val ModeChoiceCharacteristics.sex: Int get() = person.sex.code
//val ModeChoiceCharacteristics.isFemale: Boolean get() = (person.sex == Sex.FEMALE)
//val ModeChoiceCharacteristics.isMale: Boolean get() = (person.sex == Sex.MALE)
//val ModeChoiceCharacteristics.incomeEuro: Double  get() = person.income.euros
//
//val ModeChoiceCharacteristics.graduation: Int  get() = person.graduation.code
////TODO more choice.graduation upon request
//
//val ModeChoiceCharacteristics.employment: Int get() = person.employment.code
//val ModeChoiceCharacteristics.isEmploymentUnknown: Boolean get() = (person.employment == Employment.UNKNOWN)
//val ModeChoiceCharacteristics.isEmploymentFulltime: Boolean get() = (person.employment == Employment.FULLTIME)
//val ModeChoiceCharacteristics.isEmploymentParttime: Boolean get() = (person.employment == Employment.PARTTIME)
//val ModeChoiceCharacteristics.isEmploymentMarginal: Boolean get() = (person.employment == Employment.MARGINAL)
//val ModeChoiceCharacteristics.isEmploymentUnemployed: Boolean get() = (person.employment == Employment.UNEMPLOYED)
//val ModeChoiceCharacteristics.isEmploymentStudent: Boolean get() = (person.employment == Employment.STUDENT)
//val ModeChoiceCharacteristics.isEmploymentStudentPrimary: Boolean get() = (person.employment == Employment.STUDENT_PRIMARY)
//val ModeChoiceCharacteristics.isEmploymentStudentSecondary: Boolean get() = (person.employment == Employment.STUDENT_SECONDARY)
//val ModeChoiceCharacteristics.isEmploymentStudentTertiary: Boolean get() = (person.employment == Employment.STUDENT_TERTIARY)
//val ModeChoiceCharacteristics.isEmploymentEducation: Boolean get() = (person.employment == Employment.EDUCATION)
//val ModeChoiceCharacteristics.isEmploymentHomekeeper: Boolean get() = (person.employment == Employment.HOMEKEEPER)
//val ModeChoiceCharacteristics.isEmploymentRetired: Boolean get() = (person.employment == Employment.RETIRED)
//val ModeChoiceCharacteristics.isEmploymentInfant: Boolean get() = (person.employment == Employment.INFANT)
//val ModeChoiceCharacteristics.isEmploymentNone: Boolean get() = (person.employment == Employment.NONE)
//
//val ModeChoiceCharacteristics.householdType: Int get() = person.household.type
//val ModeChoiceCharacteristics.householdIncomeEuro: Double get() = person.household.incomePerMonth.inEuros
//val ModeChoiceCharacteristics.householdEconomicStatus: Int get() = person.household.economicStatus.code
//val ModeChoiceCharacteristics.isHouseholdEconomicStatusVeryLow: Boolean get() = (person.household.economicStatus == EconomicStatus.VERY_LOW)
//val ModeChoiceCharacteristics.isHouseholdEconomicStatusLow: Boolean get() = (person.household.economicStatus == EconomicStatus.LOW)
//val ModeChoiceCharacteristics.isHouseholdEconomicStatusMiddle: Boolean get() = (person.household.economicStatus == EconomicStatus.MIDDLE)
//val ModeChoiceCharacteristics.isHouseholdEconomicStatusHigh: Boolean get() = (person.household.economicStatus == EconomicStatus.HIGH)
//val ModeChoiceCharacteristics.isHouseholdEconomicStatusVeryHigh: Boolean get() = (person.household.economicStatus == EconomicStatus.VERY_HIGH)
////income class?
//val ModeChoiceCharacteristics.householdSize: Int get() = person.household.members.size
//val ModeChoiceCharacteristics.householdCars: Int get() = person.household.cars.size
//val ModeChoiceCharacteristics.adultsInHousehold: Int get() = person.household.members.count { it.isAdult }
//
//
//fun ModeChoiceCharacteristics.isAgeIn(from: Int, to: Int) = (age in from..to)
//fun ModeChoiceCharacteristics.travelTimeMinutes(mode: Mode): Double = impedance.duration(origin, destination, mode, time).minutes
//fun ModeChoiceCharacteristics.travelCostEuro(mode: Mode): Double = impedance.cost(origin, destination, mode, time).euros
//fun ModeChoiceCharacteristics.travelDistanceKm(mode: Mode): Double = impedance.distance(origin, destination, mode).inKilometers
//
//private val ModeChoiceCharacteristics.nextFixedActivity
//    get() = person.schedule.activities().find { it.location != LOCATIONUNKNOWN }
//
//private val ModeChoiceCharacteristics.nextFixedDestination: StandardLocation
//    get() = nextFixedActivity?.location ?: person.household.location
//
//private val ModeChoiceCharacteristics.nextFixedActivityEnd: AbsoluteTime
//    get() = nextFixedActivity?.endTime ?: time.plus(7.hours)
//
//fun ModeChoiceCharacteristics.travelTimeMinutesToFixed(mode: Mode): Double =
//    impedance.duration(destination, nextFixedDestination, mode, nextFixedActivityEnd).minutes
//
//fun ModeChoiceCharacteristics.travelCostEuroToFixed(mode: Mode): Double =
//    impedance.cost(destination, nextFixedDestination, mode, nextFixedActivityEnd).euros
//
//private val ModeChoiceCharacteristics.prevMode: Mode?
//    get() = person.schedule.pastLegs().lastOrNull()?.transportType
//
//val ModeChoiceCharacteristics.previousMode: Int get() = prevMode?.code ?: -1
//val ModeChoiceCharacteristics.isPrevModeBike: Boolean get() = (prevMode == bike)
//val ModeChoiceCharacteristics.isPrevModeCar: Boolean get() = (prevMode == car)
//val ModeChoiceCharacteristics.isPrevModePassenger: Boolean get() = (prevMode == passenger)
//val ModeChoiceCharacteristics.isPrevModePedestrian: Boolean get() = (prevMode == pedestrian)
//val ModeChoiceCharacteristics.isPrevModePublicTransport: Boolean get() = (prevMode == publictransport)
//val ModeChoiceCharacteristics.isPrevModePublicBikesharing: Boolean get() = (prevMode == bikesharing)
//
//val ModeChoiceCharacteristics.purpose: ActivityType get() = person.schedule.activities().first().type
//val ModeChoiceCharacteristics.isPurposeWork: Boolean get() = (purpose == TutorialActivity.WORK)
//val ModeChoiceCharacteristics.isPurposeBusiness: Boolean get() = (purpose == TutorialActivity.BUSINESS)
//val ModeChoiceCharacteristics.isPurposeEducationPrimary: Boolean get() = (purpose == TutorialActivity.EDUCATION_PRIMARY)
//val ModeChoiceCharacteristics.isPurposeEducationSecondary: Boolean get() = (purpose == TutorialActivity.EDUCATION_SECONDARY)
//val ModeChoiceCharacteristics.isPurposeEducationTertiary: Boolean get() = (purpose == TutorialActivity.EDUCATION_TERTIARY)
//val ModeChoiceCharacteristics.isPurposeShoppingDaily: Boolean get() = (purpose == TutorialActivity.SHOPPING_DAILY)
//val ModeChoiceCharacteristics.isPurposeShoppingOther: Boolean get() = (purpose == TutorialActivity.SHOPPING_OTHER)
//val ModeChoiceCharacteristics.isPurposeLeisureIndoor: Boolean get() = (purpose == TutorialActivity.LEISURE_INDOOR)
//val ModeChoiceCharacteristics.isPurposeLeisureOutdoor: Boolean get() = (purpose == TutorialActivity.LEISURE_OUTDOOR)
//val ModeChoiceCharacteristics.isPurposeService: Boolean get() = (purpose == TutorialActivity.SERVICE)
//val ModeChoiceCharacteristics.isPurposeHome: Boolean get() = (purpose == TutorialActivity.HOME)
//val ModeChoiceCharacteristics.isPurposePrivateBusiness: Boolean get() = (purpose == TutorialActivity.PRIVATE_BUSINESS)
//val ModeChoiceCharacteristics.isPurposePrivateVisit: Boolean get() = (purpose == TutorialActivity.PRIVATE_VISIT)
//val ModeChoiceCharacteristics.isPurposeUndefined: Boolean get() = (purpose == TutorialActivity.UNDEFINED)
//
//
//private val fixedPurposes = listOf(
//    TutorialActivity.HOME,
//    TutorialActivity.WORK,
//    TutorialActivity.EDUCATION_PRIMARY,
//    TutorialActivity.EDUCATION_SECONDARY,
//    TutorialActivity.EDUCATION_TERTIARY
//)
//val DestinationAlternative.isFixedLocationPurpose: Boolean get() = (purpose in fixedPurposes)
//
//private val shoppingPurposes = listOf(
//    TutorialActivity.SHOPPING_OTHER,
//    TutorialActivity.SHOPPING_DAILY,
//)
//val DestinationAlternative.isAnyShoppingPurpose: Boolean get() = (purpose in shoppingPurposes)
//
//private val educationPurposes = listOf(
//    TutorialActivity.SHOPPING_OTHER,
//    TutorialActivity.SHOPPING_DAILY,
//)
//val DestinationAlternative.isAnyEducationPurpose: Boolean get() = (purpose in educationPurposes)
//
//private val leisurePurposes = listOf(
//    TutorialActivity.SHOPPING_OTHER,
//    TutorialActivity.SHOPPING_DAILY,
//)
//val DestinationAlternative.isAnyLeisurePurpose: Boolean get() = (purpose in leisurePurposes)
