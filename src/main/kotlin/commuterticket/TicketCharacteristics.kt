package commuterticket


import edu.kit.ifv.domain.shared.enums.household.EconomicStatus
import edu.kit.ifv.domain.shared.enums.person.Employment
import edu.kit.ifv.domain.shared.enums.person.Sex
import edu.kit.ifv.domain.synthesis.attributes.person.employment
import edu.kit.ifv.domain.synthesis.behavior.ISurveyHousehold
import edu.kit.ifv.domain.synthesis.behavior.SurveyPerson
import longterm.RastattHouseholdAttributes
import longterm.RastattPersonAttributes
import util.D
import util.euros


/**
 * The characteristic class in a choice model is used to pass information to the utility function that are necessary for
 * the correct calculation, but remain invariant for the calculation step. For example the age of a person.
 */
class TicketCharacteristics(
    val household: ISurveyHousehold<RastattHouseholdAttributes, RastattPersonAttributes>,
    val person: SurveyPerson<RastattPersonAttributes>,
) {


    companion object {
        context(household: ISurveyHousehold<RastattHouseholdAttributes, RastattPersonAttributes>)
        fun fromPerson(person: SurveyPerson<RastattPersonAttributes>): TicketCharacteristics {
            return TicketCharacteristics(
                household = household,
                person = person,
            )
        }
    }
}


val TicketCharacteristics.age: Int get() = person.age

val TicketCharacteristics.hasBike: Double get() = person.attributes.hasBicycle.D

val TicketCharacteristics.hasCommuterTicket: Boolean get() = person.attributes.hasTransitPass
val TicketCharacteristics.hasLicense: Boolean get() = person.attributes.hasLicence
val TicketCharacteristics.sex: Int get() = person.sex.code
val TicketCharacteristics.isFemale: Boolean get() = (person.sex == Sex.FEMALE)
val TicketCharacteristics.isMale: Boolean get() = (person.sex == Sex.MALE)
val TicketCharacteristics.incomeEuro: Double  get() = household.attributes.income.euros.div(adultsInHousehold) //heuristic

//val TicketCharacteristics.graduation: Int  get() = person.graduation.code
//TODO more choice.graduation upon request

val TicketCharacteristics.employment: Int get() = person.employment.code
val TicketCharacteristics.isEmploymentUnknown: Boolean get() = (person.employment == Employment.UNKNOWN)
val TicketCharacteristics.isEmploymentFulltime: Boolean get() = (person.employment == Employment.FULLTIME)
val TicketCharacteristics.isEmploymentParttime: Boolean get() = (person.employment == Employment.PARTTIME)
val TicketCharacteristics.isEmploymentMarginal: Boolean get() = (person.employment == Employment.MARGINAL)
val TicketCharacteristics.isEmploymentUnemployed: Boolean get() = (person.employment == Employment.UNEMPLOYED)
val TicketCharacteristics.isEmploymentStudent: Boolean get() = (person.employment == Employment.STUDENT)
val TicketCharacteristics.isEmploymentStudentPrimary: Boolean get() = (person.employment == Employment.STUDENT_PRIMARY)
val TicketCharacteristics.isEmploymentStudentSecondary: Boolean get() = (person.employment == Employment.STUDENT_SECONDARY)
val TicketCharacteristics.isEmploymentStudentTertiary: Boolean get() = (person.employment == Employment.STUDENT_TERTIARY)
val TicketCharacteristics.isEmploymentEducation: Boolean get() = (person.employment == Employment.EDUCATION)
val TicketCharacteristics.isEmploymentHomekeeper: Boolean get() = (person.employment == Employment.HOMEKEEPER)
val TicketCharacteristics.isEmploymentRetired: Boolean get() = (person.employment == Employment.RETIRED)
val TicketCharacteristics.isEmploymentInfant: Boolean get() = (person.employment == Employment.INFANT)
val TicketCharacteristics.isEmploymentNone: Boolean get() = (person.employment == Employment.NONE)

//TODO val TicketCharacteristics.type: Int get() = person.household.type
val TicketCharacteristics.householdIncomeEuro: Double get() = household.income.inEuros
val TicketCharacteristics.householdEconomicStatus: Int get() = household.attributes.economicStatus.code
val TicketCharacteristics.isHouseholdEconomicStatusVeryLow: Boolean get() = (household.attributes.economicStatus == EconomicStatus.VERY_LOW)
val TicketCharacteristics.isHouseholdEconomicStatusLow: Boolean get() = (household.attributes.economicStatus == EconomicStatus.LOW)
val TicketCharacteristics.isHouseholdEconomicStatusMiddle: Boolean get() = (household.attributes.economicStatus == EconomicStatus.MIDDLE)
val TicketCharacteristics.isHouseholdEconomicStatusHigh: Boolean get() = (household.attributes.economicStatus == EconomicStatus.HIGH)
val TicketCharacteristics.isHouseholdEconomicStatusVeryHigh: Boolean get() = (household.attributes.economicStatus == EconomicStatus.VERY_HIGH)
//income class?
val TicketCharacteristics.householdSize: Int get() = household.members.size
val TicketCharacteristics.householdCars: Int get() = household.attributes.amountOfCars
val TicketCharacteristics.adultsInHousehold: Int get() = household.members.count { it.age >= 18 }

fun TicketCharacteristics.isAgeIn(from: Int, to: Int) = (age in from..to)

fun TicketCharacteristics.householdMembersBetween(ageFrom: Int, ageTo: Int) = household.members.count { it.age in ageFrom..ageTo }
