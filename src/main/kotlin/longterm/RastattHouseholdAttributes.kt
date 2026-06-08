package longterm

import edu.kit.ifv.domain.shared.enums.household.EconomicStatus
import edu.kit.ifv.domain.shared.enums.household.HouseholdType
import edu.kit.ifv.domain.shared.location.StandardLocation
import edu.kit.ifv.domain.synthesis.attributes.household.MaximumHouseholdAttributes
import edu.kit.ifv.units.Currency

data class RastattHouseholdAttributes(
    val householdId: Long,
    override val income: Currency,
    override val type: HouseholdType,
    override var amountOfCars: Int,
): MaximumHouseholdAttributes {

    // Fields that get set during the simulation and are not known beforehand. 
    override var location: StandardLocation = StandardLocation.LOCATIONUNKNOWN

    override var economicStatus: EconomicStatus = EconomicStatus.MIDDLE
}
