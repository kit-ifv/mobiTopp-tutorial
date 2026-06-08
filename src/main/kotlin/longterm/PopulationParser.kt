package longterm

import edu.kit.ifv.domain.jackson.standardCSVParse
import edu.kit.ifv.domain.synthesis.GenerateFromFlatInput
import kotlin.io.path.Path
import kotlin.io.path.inputStream


/**
 * Builds a [GenerateFromFlatInput] generator for the Rastatt survey population.
 *
 * This function adapts rows from `SurveyPopulation.csv` (or a compatible
 * collection of [RastattSurveyAttributes]) to the generic [GenerateFromFlatInput] API by:
 *
 * - grouping rows by their household id,
 * - constructing one [RastattHouseholdAttributes] instance per household, and
 * - constructing one [RastattPersonAttributes] instance per row.
 *
 * By default, the input is parsed from `src/main/resources/SurveyPopulation.csv`
 * using `;` as a separator. Callers may supply a custom [input] collection if the
 * survey data is loaded differently or pre-filtered.
 *
 * The original survey data does not contain a dedicated person identifier; this
 * parser therefore generates a technical [personId] via an incrementing counter.
 * The resulting ids are only used as internal stable keys and carry no external
 * meaning, as long as they remain consistent across runs.
 *
 * @param input curated survey rows, usually parsed from `SurveyPopulation.csv`.
 * @return a [GenerateFromFlatInput] instance that can generate survey households
 *   and persons with [RastattHouseholdAttributes] and [RastattPersonAttributes].
 */

fun rastattPopulationParser(
    input: Collection<RastattSurveyAttributes> = standardCSVParse(
        input = Path("src/main/resources/SurveyPopulation.csv").inputStream(),
        separator = ';'
    )
):
        GenerateFromFlatInput<RastattSurveyAttributes, RastattHouseholdAttributes, RastattPersonAttributes> =
    GenerateFromFlatInput(
        input,
        idExtractor = { it.householdId },
        householdDataExtractor = {
            val data = it.first()
            RastattHouseholdAttributes(
                householdId = data.householdId,
                income = data.householdIncomeEuros,
                type = data.householdType,
                amountOfCars = data.amountOfCars,
            )
        },
        personDataExtractor = {
            RastattPersonAttributes(
                personId = personCounter++, // Technical id: stable within a run, not present in the original survey.
                age = it.age,
                sex = it.sex,
                distanceWork = it.distanceWork,
                distanceEducation = it.distanceEducation,
                employment = it.employment,
                hasBicycle = it.hasBicycle,
                hasLicence = it.hasLicence,
                hasTransitPass = false // Not present in original data.
            )
        }
    )
private var personCounter: Long = 0