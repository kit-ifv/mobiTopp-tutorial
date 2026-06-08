package longterm.rules

import edu.kit.ifv.domain.synthesis.rules.HouseholdSizeFactory
import edu.kit.ifv.domain.synthesis.rules.HouseholdTypeFactory
import edu.kit.ifv.domain.synthesis.rules.PersonAgeSexFactory


/**
 * Factory for household-type rules based on [RastattSynthesisZoneTargets].
 *
 * This adapter maps the household type codes used by the synthesis framework
 * to the corresponding `household_type:<code>` columns in `SynthesisZoneTargets.csv`.
 * The household type taxonomy reflects the German census 2022 household-type
 *
 * represented by [edu.kit.ifv.domain.shared.enums.household.HouseholdType]
 *
 */
object RastattHouseholdTypeFactory : HouseholdTypeFactory<RastattSynthesisZoneTargets>(
    targetExtractor = { input, householdType ->
        input.ruleTargets["household_type:${householdType.code}"]

    }
)

/**
 * Factory for household-size rules based on [RastattSynthesisZoneTargets].
 *
 * Household sizes are provided as equality targets in the CSV (`household_size:1`
 * through `household_size:5`). This adapter wires those columns into the generic
 * [HouseholdSizeFactory] API by:
 *
 * - providing an equality extractor for a specific size N (`household_size:N`), and
 * - providing a "greater or equal" extractor that aggregates all available sizes
 *   from N up to the maximum size present in the Rastatt data (5).
 *
 * The framework's factory implicitly creates an open-ended "≥ N" rule from these
 * extractors, ensuring that households larger than the explicitly listed sizes
 * are still covered by some household-size category and do not lead to surprises
 * in the synthesis results.
 */
object RastattHouseholdSizeFactory : HouseholdSizeFactory<RastattSynthesisZoneTargets>(
    equalTargetExtractor = { input, sizeNumber ->
        input.ruleTargets["household_size:${sizeNumber}"]

    },
    greaterEqualTargetExtractor = { input, sizeNumber ->
        (sizeNumber..5).sumOf { input.ruleTargets["household_size:$it"] ?: 0 }
    }

)

/**
 * Factory for age × sex person rules based on [RastattSynthesisZoneTargets].
 *
 * This adapter uses [RastattSynthesisZoneTargets.getAgeSexRuleDefinitions] to expose
 * the age × sex marginals from `SynthesisZoneTargets.csv` to the framework's
 * [PersonAgeSexFactory] abstraction.
 */
object RastattAgeSexFactory : PersonAgeSexFactory<RastattSynthesisZoneTargets>(
    definitionDecoder = {
        it.getAgeSexRuleDefinitions()
    }
)