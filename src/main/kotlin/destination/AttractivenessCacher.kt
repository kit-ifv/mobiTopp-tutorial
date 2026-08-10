package destination

import edu.kit.ifv.domain.shared.behavior.AttractivenessModel
import edu.kit.ifv.domain.shared.enums.ActivityType
import edu.kit.ifv.domain.shared.location.zone.ZoneId

/*
I need the attractiveness as array, which the attractiveness model does not provide per default.
Also i don't want to recalculate the static attractiveness arrays for each call.
 */
class AttractivenessCacher(
    private val attractivenessModel: AttractivenessModel,
    private val zoneIds: List<ZoneId>,
) {
    private val map: MutableMap<ActivityType, DoubleArray> = mutableMapOf()


    operator fun get(activity: ActivityType): DoubleArray {
        return map.getOrPut(activity) {
            DoubleArray(zoneIds.size) {
                attractivenessModel.attractivenessFor(zoneIds[it], activity).value
            }
        }
    }
}