package longterm.rules

import edu.kit.ifv.domain.jackson.standardCSVParse
import edu.kit.ifv.domain.shared.enums.person.Sex
import edu.kit.ifv.domain.shared.location.zone.ZoneId
import edu.kit.ifv.domain.synthesis.rules.measurements.PersonAgeSexDefinition
import edu.kit.ifv.processor.builder.splitOnce
import java.nio.file.Path
import kotlin.io.path.inputStream

/**
 * Per-zone synthesis targets parsed from `SynthesisZoneTargets.csv`.
 *
 * Each instance corresponds to one traffic analysis zone (TAZ) and exposes
 * marginal targets (age×sex, household size, household type) as a typed view
 * over a raw CSV row.
 *
 * This class is part of an adapter that translates one specific CSV layout
 * into the generic synthesis framework; the CSV schema is considered
 * **example input**, not a stable external API.
 *
 * @property zoneId
 * Identifier of the traffic analysis zone, parsed from the `zoneId` column.
 *
 * @property ruleTargets
 * Raw targets keyed by CSV column name (for example
 * `household_size:1`, `household_type:3`, `age_f:18-24`, `age_m:75-`).
 */
data class RastattSynthesisZoneTargets(
    val zoneId: ZoneId,
    val ruleTargets: Map<String, Int>,
) {

    /**
     * Returns all person age × sex targets encoded in this zone.
     *
     * Age × sex targets are taken from every column whose name starts with `"age_"`,
     * and decoded from CSV column names such as `age_f:0-5` or `age_m:75-`
     * into [PersonAgeSexDefinition] instances.
     *
     * @return a list of pairs where the first element is the decoded
     * [PersonAgeSexDefinition] and the second element is the corresponding target count.
     */
    fun getAgeSexRuleDefinitions(): List<Pair<PersonAgeSexDefinition, Number>> {
        return ruleTargets.filterKeys { it.contains("age_") }.map { (columnName, target) ->
            decodeStringToRuleDefinition(columnName) to target
        }
    }

    /**
     * Decodes an age×sex column name from the CSV header into a [PersonAgeSexDefinition].
     *
     * The supported format is `age_<sex>:<from>-<to>`, where `<sex>` is `f` (female) or `m` (male),
     * `<from>` and `<to>` are integer age bounds in years, and an empty `<to>` (for example
     * `age_f:75-`) denotes an open-ended upper bound.
     *
     * @param input the column name to decode (e.g. `"age_m:18-24"` or `"age_f:75-"`).
     * @throws IllegalArgumentException if the sex prefix cannot be parsed.
     */
    fun decodeStringToRuleDefinition(input: String): PersonAgeSexDefinition {
        val (genderPrefix, rangeSuffix) = input.splitOnce(":")
        val sex = when (genderPrefix.removePrefix("age_")) {
            "f" -> Sex.FEMALE
            "m" -> Sex.MALE
            else -> throw IllegalArgumentException("Cannot parse $input to an age sex definition rule")
        }
        val (rangeStartText, rangeEndText) = rangeSuffix.splitOnce("-")
        val rangeStart = rangeStartText.toInt() // Deliberately forceful, the first number is always present.
        val rangeEnd = rangeEndText.toIntOrNull()
            ?: Integer.MAX_VALUE // Open-ended upper bound such as `75-`.
        return PersonAgeSexDefinition(rangeStart..rangeEnd, sex)

    }

    companion object {

        /**
         * Parses all zone targets from a `SynthesisZoneTargets.csv` file.
         *
         * The CSV is expected to use `;` as a separator and contain a `zoneId` column,
         * followed by any number of household size, household type, and age×sex columns,
         * such as:
         *
         * - `household_size:1` … `household_size:5`
         * - `household_type:1`, `household_type:3`, `household_type:4`, `household_type:5`
         * - `age_f:0-5`, `age_f:6-9`, …, `age_f:75-`
         * - `age_m:0-5`, …, `age_m:75-`
         *
         * This is an **internal example format**; changing the CSV schema only requires
         * adapting this method and the factories that consume [RastattSynthesisZoneTargets].
         *
         * @param path path to the `SynthesisZoneTargets.csv` file. Defaults to
         * `src/main/resources/SynthesisZoneTargets.csv`.
         * @return a collection of [RastattSynthesisZoneTargets], one per CSV row.
         */
        fun fromCsv(path: Path = Path.of("src/main/resources/SynthesisZoneTargets.csv")):
                Collection<RastattSynthesisZoneTargets> {
            val parsedMap = standardCSVParse<Map<String, String>>(path.inputStream())
            return parsedMap.map {
                val filterKeys: Map<String, String> = it.filterKeys { it != "zoneId" }
                RastattSynthesisZoneTargets(
                    zoneId = ZoneId(it.getValue("zoneId").toLong()),
                    ruleTargets = filterKeys.mapValues { (_, value) -> value
                        .toInt() }
                )
            }

        }
    }
}