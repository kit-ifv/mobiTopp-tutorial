package destination

import edu.kit.ifv.domain.shared.enums.ActivityType
import edu.kit.ifv.domain.shared.enums.household.EconomicStatus
import edu.kit.ifv.domain.shared.enums.person.Employment
import edu.kit.ifv.domain.shared.enums.person.Sex
import edu.kit.ifv.domain.shared.location.StandardLocation
import edu.kit.ifv.domain.simulation.behavior.DestinationAlternative
import edu.kit.ifv.domain.simulation.behavior.kilometers
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
import kotlin.time.Duration.Companion.hours


val DestinationAlternative.age: Int get() = person.age
val DestinationAlternative.hasBike: Boolean get() = person.hasBike
val DestinationAlternative.hasCommuterTicket: Boolean get() = person.hasCommuterTicket
val DestinationAlternative.hasLicense: Boolean get() = person.hasLicense
val DestinationAlternative.sex: Int get() = person.sex.code
val DestinationAlternative.isFemale: Boolean get() = (person.sex == Sex.FEMALE)
val DestinationAlternative.isMale: Boolean get() = (person.sex == Sex.MALE)
val DestinationAlternative.incomeEuro: Double  get() = person.income.euros

val DestinationAlternative.graduation: Int  get() = person.graduation.code
//TODO more choice.graduation upon request

val DestinationAlternative.employment: Int get() = person.employment.code
val DestinationAlternative.isEmploymentUnknown: Boolean get() = (person.employment == Employment.UNKNOWN)
val DestinationAlternative.isEmploymentFulltime: Boolean get() = (person.employment == Employment.FULLTIME)
val DestinationAlternative.isEmploymentParttime: Boolean get() = (person.employment == Employment.PARTTIME)
val DestinationAlternative.isEmploymentMarginal: Boolean get() = (person.employment == Employment.MARGINAL)
val DestinationAlternative.isEmploymentUnemployed: Boolean get() = (person.employment == Employment.UNEMPLOYED)
val DestinationAlternative.isEmploymentStudent: Boolean get() = (person.employment == Employment.STUDENT)
val DestinationAlternative.isEmploymentStudentPrimary: Boolean get() = (person.employment == Employment.STUDENT_PRIMARY)
val DestinationAlternative.isEmploymentStudentSecondary: Boolean get() = (person.employment == Employment.STUDENT_SECONDARY)
val DestinationAlternative.isEmploymentStudentTertiary: Boolean get() = (person.employment == Employment.STUDENT_TERTIARY)
val DestinationAlternative.isEmploymentEducation: Boolean get() = (person.employment == Employment.EDUCATION)
val DestinationAlternative.isEmploymentHomekeeper: Boolean get() = (person.employment == Employment.HOMEKEEPER)
val DestinationAlternative.isEmploymentRetired: Boolean get() = (person.employment == Employment.RETIRED)
val DestinationAlternative.isEmploymentInfant: Boolean get() = (person.employment == Employment.INFANT)
val DestinationAlternative.isEmploymentNone: Boolean get() = (person.employment == Employment.NONE)

val DestinationAlternative.householdType: Int get() = person.household.type
val DestinationAlternative.householdIncomeEuro: Double get() = person.household.incomePerMonth.inEuros
val DestinationAlternative.householdEconomicStatus: Int get() = person.household.economicStatus.code
val DestinationAlternative.isHouseholdEconomicStatusVeryLow: Boolean get() = (person.household.economicStatus == EconomicStatus.VERY_LOW)
val DestinationAlternative.isHouseholdEconomicStatusLow: Boolean get() = (person.household.economicStatus == EconomicStatus.LOW)
val DestinationAlternative.isHouseholdEconomicStatusMiddle: Boolean get() = (person.household.economicStatus == EconomicStatus.MIDDLE)
val DestinationAlternative.isHouseholdEconomicStatusHigh: Boolean get() = (person.household.economicStatus == EconomicStatus.HIGH)
val DestinationAlternative.isHouseholdEconomicStatusVeryHigh: Boolean get() = (person.household.economicStatus == EconomicStatus.VERY_HIGH)
//income class?
val DestinationAlternative.householdSize: Int get() = person.household.members.size
val DestinationAlternative.householdCars: Int get() = person.household.cars.size
val DestinationAlternative.adultsInHousehold: Int get() = person.household.members.count { it.isAdult }


fun DestinationAlternative.isAgeIn(from: Int, to: Int) = (age in from..to)
fun DestinationAlternative.travelTimeMinutes(mode: Mode): Double = impedance.duration(origin, choice , mode, time).minutes
fun DestinationAlternative.travelCostEuro(mode: Mode): Double = impedance.cost(origin, choice, mode, time).euros
val DestinationAlternative.travelDistanceKm: Double get() = impedance.distance(origin, choice, car).kilometers


val DestinationAlternative.employmentIsSomeEducationType get() = isEmploymentStudent || isEmploymentStudentPrimary || isEmploymentStudentSecondary || isEmploymentStudentTertiary || isEmploymentEducation

private val DestinationAlternative.nextFixedActivity
    get() = person.schedule.activities().find { it.location != StandardLocation.LOCATIONUNKNOWN }

private val DestinationAlternative.nextFixedDestination: StandardLocation
    get() = nextFixedActivity?.location ?: person.household.location

private val DestinationAlternative.nextFixedActivityEnd: AbsoluteTime
    get() = nextFixedActivity?.endTime ?: time.plus(7.hours)

fun DestinationAlternative.travelTimeMinutesToFixed(mode: Mode): Double =
    impedance.duration(choice, nextFixedDestination, mode, nextFixedActivityEnd).minutes

fun DestinationAlternative.travelCostEuroToFixed(mode: Mode): Double =
    impedance.cost(choice, nextFixedDestination, mode, nextFixedActivityEnd).euros

private val ModeChoiceCharacteristics.prevMode: Mode?
    get() = person.schedule.pastLegs().lastOrNull()?.transportType


val DestinationAlternative.purpose: ActivityType get() = person.schedule.activities().first().type
val DestinationAlternative.isPurposeWork: Boolean get() = (purpose == TutorialActivity.WORK)
val DestinationAlternative.isPurposeBusiness: Boolean get() = (purpose == TutorialActivity.BUSINESS)
val DestinationAlternative.isPurposeEducationPrimary: Boolean get() = (purpose == TutorialActivity.EDUCATION_PRIMARY)
val DestinationAlternative.isPurposeEducationSecondary: Boolean get() = (purpose == TutorialActivity.EDUCATION_SECONDARY)
val DestinationAlternative.isPurposeEducationTertiary: Boolean get() = (purpose == TutorialActivity.EDUCATION_TERTIARY)
val DestinationAlternative.isPurposeShoppingDaily: Boolean get() = (purpose == TutorialActivity.SHOPPING_DAILY)
val DestinationAlternative.isPurposeShoppingOther: Boolean get() = (purpose == TutorialActivity.SHOPPING_OTHER)
val DestinationAlternative.isPurposeLeisureIndoor: Boolean get() = (purpose == TutorialActivity.LEISURE_INDOOR)
val DestinationAlternative.isPurposeLeisureOutdoor: Boolean get() = (purpose == TutorialActivity.LEISURE_OUTDOOR)
val DestinationAlternative.isPurposeService: Boolean get() = (purpose == TutorialActivity.SERVICE)
val DestinationAlternative.isPurposeHome: Boolean get() = (purpose == TutorialActivity.HOME)
val DestinationAlternative.isPurposePrivateBusiness: Boolean get() = (purpose == TutorialActivity.PRIVATE_BUSINESS)
val DestinationAlternative.isPurposePrivateVisit: Boolean get() = (purpose == TutorialActivity.PRIVATE_VISIT)
val DestinationAlternative.isPurposeUndefined: Boolean get() = (purpose == TutorialActivity.UNDEFINED)

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

val DestinationAlternative.attractivity: Double get() = attractivityModel.attractivenessFor(
    choice.zoneId,
    purpose
).value

// availability properties
private fun DestinationAlternative.isAvailable(mode: Mode): Boolean =
    modeAvailabilityFilter.filter(mode)

val DestinationAlternative.isPedAvailable get() = isAvailable(pedestrian)
val DestinationAlternative.isBikeAvailable get() = isAvailable(bike)
val DestinationAlternative.isCarAvailable get() = isAvailable(car)
val DestinationAlternative.isPassengerAvailable get() = isAvailable(passenger)
val DestinationAlternative.isPutAvailable get() = isAvailable(publictransport)
val DestinationAlternative.isBikesharingAvailable get() = isAvailable(bikesharing)

private val DestinationAlternative.prevMode: Mode?
    get() = person.schedule.pastLegs().lastOrNull()?.transportType

val DestinationAlternative.previousMode: Int get() = prevMode?.code ?: -1
val DestinationAlternative.isPrevModeBike: Boolean get() = (prevMode == bike)
val DestinationAlternative.isPrevModeCar: Boolean get() = (prevMode == car)
val DestinationAlternative.isPrevModePassenger: Boolean get() = (prevMode == passenger)
val DestinationAlternative.isPrevModePedestrian: Boolean get() = (prevMode == pedestrian)
val DestinationAlternative.isPrevModePublicTransport: Boolean get() = (prevMode == publictransport)
val DestinationAlternative.isPrevModePublicBikesharing: Boolean get() = (prevMode == bikesharing)
