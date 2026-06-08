package util

import edu.kit.ifv.domain.shared.behavior.ChoiceModelModes
import edu.kit.ifv.domain.shared.enums.MODEUNKOWN
import edu.kit.ifv.utils.CodePlan

/**
 * Modes used in REstatt model
 *
 * @property code integer code of the mode
 */
@Suppress("MagicNumber")
enum class TutorialMode(
    override val code: Int,
    private val isFixed: Boolean = false
) : Mode {
    BIKE(0, true),
    CAR(1, true),
    PASSENGER(2),
    PEDESTRIAN(3),
    PUBLICTRANSPORT(4),

    BIKESHARING(17);

    override val description = name

    companion object : CodePlan<TutorialMode> {
        override fun decode(i: Int) = entries.first { it.code == i }
        override fun decode(s: String) = valueOf(s)
        override fun values(): Set<TutorialMode> = entries.toSet()
    }

    override val requiresVehicleTakeAlong: Boolean = isFixed

}

val tutorialChoiceModelModes = ChoiceModelModes(
    car = TutorialMode.CAR,
    passenger = TutorialMode.PASSENGER,
    bike = TutorialMode.BIKE,
    pedestrian = TutorialMode.PEDESTRIAN,
    publicTransport = TutorialMode.PUBLICTRANSPORT,
    bikeSharing = TutorialMode.BIKESHARING,

    ridePooling = MODEUNKOWN,
    carSharingFree = MODEUNKOWN,
    carSharingStation = MODEUNKOWN,
    taxi = MODEUNKOWN,
    eScooter = MODEUNKOWN,
)
