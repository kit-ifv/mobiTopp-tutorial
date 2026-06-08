package longterm

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import edu.kit.ifv.domain.shared.enums.household.HouseholdType
import edu.kit.ifv.domain.shared.enums.person.Employment
import edu.kit.ifv.domain.shared.enums.person.Sex
import edu.kit.ifv.units.euros
import edu.kit.ifv.units.kilometers

/**
 * Raw survey row for the Rastatt population, as read from `SurveyPopulation.csv`.
 *
 * Each instance represents one person in the flat input format, combining both
 * person-level and household-level information. The `*Code` properties mirror
 * the integer-coded values in the CSV, while the computed properties decode
 * these into framework-specific types and units.
 *
 * The provided `SurveyPopulation.csv` resource is curated and validated: all
 * code values can be decoded without producing errors or `UNKNOWN` categories.
 * Unknown additional columns in the input are ignored.
 *
 * @property householdId household identifier from the `ID` column; used to group
 *   persons into households.
 * @property sexCode numeric sex code from `sex`; use [sex] for the decoded [Sex].
 * @property year survey reference year from `year`.
 * @property birthYear birth year of the person from `birthYear`.
 * @property distanceWorkKm distance to work in kilometers from `distance_work`.
 * @property distanceEducationKm distance to education in kilometers from `distance_education`.
 * @property employmentCode employment status code from `employmenttype`; use [employment]
 *   for the decoded [Employment].
 * @property hasBicycleCode indicator from `bicycle`; use [hasBicycle] for the decoded boolean.
 * @property hasLicenceCode indicator from `licence`; use [hasLicence] for the decoded boolean.
 * @property amountOfCars number of cars in the household from `cars`.
 * @property householdIncome household income in whole euros from `hhincome`;
 *   use [householdIncomeEuros] for the domain-specific money type.
 * @property householdTypeCode household type classification from `type`; use [householdType]
 *   for the decoded [HouseholdType].
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class RastattSurveyAttributes(
    @field:JsonProperty("ID")
    val householdId: Long,
    @get:JsonProperty("sex")
    val sexCode: Int,
    val year: Int,
    @field:JsonProperty("birthyear")
    val birthYear: Int,
    @field:JsonProperty("distance_work")
    val distanceWorkKm: Double,
    @field:JsonProperty("distance_education")
    val distanceEducationKm: Double,
    @field:JsonProperty("employmenttype")
    val employmentCode: Int,
    @field:JsonProperty("bicycle")
    val hasBicycleCode: Int,
    @field:JsonProperty("licence")
    val hasLicenceCode: Int,

    @field:JsonProperty("cars")
    val amountOfCars: Int,
    @field:JsonProperty("hhincome")
    val householdIncome: Int,
    @field:JsonProperty("type")
    val householdTypeCode: Int,

) {
    // Helper functions to decode survey population data.
    val age get()= year - birthYear
    val sex get()= Sex.decode(sexCode)
    val hasBicycle get() = hasBicycleCode > 0
    val hasLicence get() = hasLicenceCode > 0
    val distanceWork get() = distanceWorkKm.kilometers
    val distanceEducation get() = distanceEducationKm.kilometers
    val employment get() = Employment.decode(employmentCode)
    val householdType get() = HouseholdType.decode(householdTypeCode)
    val householdIncomeEuros get() = householdIncome.euros
}
