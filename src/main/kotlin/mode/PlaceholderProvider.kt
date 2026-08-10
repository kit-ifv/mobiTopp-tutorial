package mode

import edu.kit.ifv.domain.shared.enums.Mode
import edu.kit.ifv.domain.shared.location.StandardLocation
import edu.kit.ifv.domain.simulation.agent.PersonAgent
import edu.kit.ifv.domain.simulation.behavior.availability.ModeResource
import edu.kit.ifv.domain.simulation.behavior.availability.NoResourceMode
import edu.kit.ifv.domain.simulation.behavior.availability.ProviderAvailability
import edu.kit.ifv.domain.simulation.behavior.availability.rules.AvailabilityRule
import edu.kit.ifv.domain.simulation.behavior.modechoice.ModeChoiceCharacteristics
import edu.kit.ifv.domain.simulation.data.person.IPerson
import edu.kit.ifv.utils.units.AbsoluteTime

class PlaceholderProvider(override val mode: Mode) : AvailabilityRule {
    override fun staticAvailability(person: IPerson): Boolean {
        return true
    }

    override fun providerAvailability(
        agent: PersonAgent,
        time: AbsoluteTime,
        destination: StandardLocation
    ): ProviderAvailability {
        return ProviderAvailability(
            mode = mode,
            providers = listOf("me :)")
        )
    }

    override fun resourceAvailability(characteristics: ModeChoiceCharacteristics): ModeResource? {
        return NoResourceMode(mode)
    }
}