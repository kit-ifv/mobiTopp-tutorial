package util

import edu.kit.ifv.application.steps.HasAttractivenessModel
import edu.kit.ifv.application.steps.HasImpedance
import edu.kit.ifv.application.steps.HasMutableDestinationChoiceModel
import edu.kit.ifv.application.steps.HasMutableModeAvailabilityModel
import edu.kit.ifv.application.steps.HasZoneRepo
import edu.kit.ifv.core.modelsteps.steps.repositoryDependentStep
import edu.kit.ifv.core.results.plots.modeStringColor
import edu.kit.ifv.domain.shared.behavior.AttractivenessModel
import edu.kit.ifv.domain.shared.location.Impedance
import edu.kit.ifv.domain.shared.location.StandardLocation
import edu.kit.ifv.domain.shared.location.zone.MaximalZone
import edu.kit.ifv.domain.simulation.behavior.availability.ModeAvailabilityModel
import edu.kit.ifv.domain.simulation.behavior.destinationchoice.DestinationChoiceCharacteristics
import edu.kit.ifv.domain.simulation.behavior.destinationchoice.legacyDestinationChoice
import edu.kit.ifv.mobitopp.discretechoice.models.FixedChoiceModel
import edu.kit.ifv.mobitopp.discretechoice.models.UtilityBasedChoiceModel

fun <C> C.loadDestinationChoiceModel(
    choiceModelFactory: (Impedance, AttractivenessModel, ModeAvailabilityModel) ->
    UtilityBasedChoiceModel<StandardLocation, DestinationChoiceCharacteristics> = { _,_,_ -> this.legacyDestinationChoice },
) where C : HasZoneRepo<*, MaximalZone>, C : HasMutableDestinationChoiceModel,
        C : HasImpedance, C : HasAttractivenessModel, C : HasMutableModeAvailabilityModel =
    repositoryDependentStep(
        "load destination choice model",
        dependentRepositories = setOf(zoneRepository),
    ) {
        val model = choiceModelFactory(impedance, attractiveness, modeAvailability)

        destinationChoiceModel =
            model as? FixedChoiceModel
                ?: model.fixed(
                    zoneRepository.elements.filter { it.isDestination }.map {
                        it.centroidLocation
                    }.toSet(),
                )
    }