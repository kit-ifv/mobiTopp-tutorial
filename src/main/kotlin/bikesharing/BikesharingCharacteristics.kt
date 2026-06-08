package bikesharing

import edu.kit.ifv.domain.shared.enums.household.EconomicStatus
import edu.kit.ifv.domain.shared.enums.person.Employment
import edu.kit.ifv.domain.shared.enums.person.Sex
import edu.kit.ifv.domain.synthesis.SynthesisPerson
import edu.kit.ifv.domain.synthesis.attributes.person.employment
import longterm.RastattHouseholdAttributes
import longterm.RastattPersonAttributes
import util.euros


/**
 * The characteristic class in a choice model is used to pass information to the utility function that are necessary for
 * the correct calculation, but remain invariant for the calculation step. For example the age of a person.
 */
class BikesharingCharacteristics(
    val person: SynthesisPerson<RastattHouseholdAttributes, RastattPersonAttributes>,
) {


    companion object {
        fun fromPerson(person: SynthesisPerson<RastattHouseholdAttributes, RastattPersonAttributes>): BikesharingCharacteristics {
            return BikesharingCharacteristics(
                person = person,
            )
        }
    }
}



val BikesharingCharacteristics.age: Int get() = person.age

//val BikesharingCharacteristics.hasBike: Double get() = person.hasBike //TODO

val BikesharingCharacteristics.hasCommuterTicket: Boolean get() = person.attributes.hasTransitPass
val BikesharingCharacteristics.hasLicense: Boolean get() = person.attributes.hasLicence
val BikesharingCharacteristics.sex: Int get() = person.sex.code
val BikesharingCharacteristics.isFemale: Boolean get() = (person.sex == Sex.FEMALE)
val BikesharingCharacteristics.isMale: Boolean get() = (person.sex == Sex.MALE)
val BikesharingCharacteristics.incomeEuro: Double  get() = person.household.attributes.income.euros.div(adultsInHousehold) //heuristic

//val BikesharingCharacteristics.graduation: Int  get() = person.graduation.code
//TODO more choice.graduation upon request

val BikesharingCharacteristics.employment: Int get() = person.employment.code
val BikesharingCharacteristics.isEmploymentUnknown: Boolean get() = (person.employment == Employment.UNKNOWN)
val BikesharingCharacteristics.isEmploymentFulltime: Boolean get() = (person.employment == Employment.FULLTIME)
val BikesharingCharacteristics.isEmploymentParttime: Boolean get() = (person.employment == Employment.PARTTIME)
val BikesharingCharacteristics.isEmploymentMarginal: Boolean get() = (person.employment == Employment.MARGINAL)
val BikesharingCharacteristics.isEmploymentUnemployed: Boolean get() = (person.employment == Employment.UNEMPLOYED)
val BikesharingCharacteristics.isEmploymentStudent: Boolean get() = (person.employment == Employment.STUDENT)
val BikesharingCharacteristics.isEmploymentStudentPrimary: Boolean get() = (person.employment == Employment.STUDENT_PRIMARY)
val BikesharingCharacteristics.isEmploymentStudentSecondary: Boolean get() = (person.employment == Employment.STUDENT_SECONDARY)
val BikesharingCharacteristics.isEmploymentStudentTertiary: Boolean get() = (person.employment == Employment.STUDENT_TERTIARY)
val BikesharingCharacteristics.isEmploymentEducation: Boolean get() = (person.employment == Employment.EDUCATION)
val BikesharingCharacteristics.isEmploymentHomekeeper: Boolean get() = (person.employment == Employment.HOMEKEEPER)
val BikesharingCharacteristics.isEmploymentRetired: Boolean get() = (person.employment == Employment.RETIRED)
val BikesharingCharacteristics.isEmploymentInfant: Boolean get() = (person.employment == Employment.INFANT)
val BikesharingCharacteristics.isEmploymentNone: Boolean get() = (person.employment == Employment.NONE)

//TODO val BikesharingCharacteristics.type: Int get() = person.household.type
val BikesharingCharacteristics.householdIncomeEuro: Double get() = person.household.income.inEuros
val BikesharingCharacteristics.householdEconomicStatus: Int get() = person.household.attributes.economicStatus.code
val BikesharingCharacteristics.isHouseholdEconomicStatusVeryLow: Boolean get() = (person.household.attributes.economicStatus == EconomicStatus.VERY_LOW)
val BikesharingCharacteristics.isHouseholdEconomicStatusLow: Boolean get() = (person.household.attributes.economicStatus == EconomicStatus.LOW)
val BikesharingCharacteristics.isHouseholdEconomicStatusMiddle: Boolean get() = (person.household.attributes.economicStatus == EconomicStatus.MIDDLE)
val BikesharingCharacteristics.isHouseholdEconomicStatusHigh: Boolean get() = (person.household.attributes.economicStatus == EconomicStatus.HIGH)
val BikesharingCharacteristics.isHouseholdEconomicStatusVeryHigh: Boolean get() = (person.household.attributes.economicStatus == EconomicStatus.VERY_HIGH)
//income class?
val BikesharingCharacteristics.householdSize: Int get() = person.household.members.size
val BikesharingCharacteristics.householdCars: Int get() = person.household.cars.size
val BikesharingCharacteristics.adultsInHousehold: Int get() = person.household.members.count { it.age >= 18 }

fun BikesharingCharacteristics.isAgeIn(from: Int, to: Int) = (age in from..to)


private val JOBS = listOf<Employment?>(
    Employment.FULLTIME,
    Employment.STUDENT_SECONDARY,
    Employment.STUDENT_TERTIARY,
    Employment.HOMEKEEPER,
    Employment.RETIRED,
    Employment.UNEMPLOYED
)

val BikesharingCharacteristics.isEmploymentOther: Boolean get() = !JOBS.contains(person.employment)

val BikesharingCharacteristics.homeRegionType: Int get() = person.household.attributes.location.regionType.code

val BikesharingCharacteristics.hasBike: Boolean get() = person.attributes.hasBicycle

