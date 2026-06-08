package longterm.rules

import edu.kit.ifv.domain.shared.location.zone.ZoneId
import edu.kit.ifv.domain.synthesis.behavior.ISurveyHousehold
import edu.kit.ifv.populationsynthesis.rules.provider.MapRuleProvider

/**
 * Builds a [MapRuleProvider] with all synthesis rules for the Rastatt example data.
 *
 * This class adapts a collection of [RastattSynthesisZoneTargets] (by default parsed
 * from `SynthesisZoneTargets.csv`) into the generic [MapRuleProvider] format used
 * by the synthesis framework. For each traffic analysis zone, it aggregates the
 * household-type, household-size, and age×sex rule sets into a single provider entry.
 *
 * The default constructor argument loads all targets via
 * [RastattSynthesisZoneTargets.fromCsv]. A custom collection can be supplied to use
 * different target data or formats.
 *
 * Example:
 * `val ruleProvider = RastattRuleProvider().createRuleProvider()`
 * yields a provider keyed by [ZoneId], with rules applicable to `ISurveyHousehold`s.
 *
 * @property synthesisZoneTargets
 * The per-zone target collection to be converted into synthesis rules.
 */
class RastattRuleProvider(private val synthesisZoneTargets: Collection<RastattSynthesisZoneTargets> = RastattSynthesisZoneTargets.fromCsv()) {
    /**
     * Creates a [MapRuleProvider] keyed by [ZoneId] containing all rules for
     * the configured [synthesisZoneTargets].
     *
     * For each zone, this method:
     * - adds household-type rules from [RastattHouseholdTypeFactory],
     * - adds household-size rules from [RastattHouseholdSizeFactory] with an
     *   explicit size limit of 4 (size ≥ 5 is handled via the implicit "≥ N" rule),
     * - adds person-level age × sex rules from [RastattAgeSexFactory].
     */
    fun createRuleProvider() = MapRuleProvider<ZoneId, ISurveyHousehold<*, *>>().apply {
        synthesisZoneTargets.forEach {
            addRules(it.zoneId, RastattHouseholdTypeFactory.buildRuleSet(it))
            addRules(it.zoneId, RastattHouseholdSizeFactory.buildRuleSet(lastExplicitSize = 4, it))
            addRules(it.zoneId, RastattAgeSexFactory.buildRuleSet(it))
        }
    }
}