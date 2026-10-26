import destination.WrappedDestinationChoice
import edu.kit.ifv.application.steps.HasAttractivenessModel
import edu.kit.ifv.application.steps.HasCarRepo
import edu.kit.ifv.application.steps.HasChoiceModelModes
import edu.kit.ifv.application.steps.HasDrtProviderAgentRepo
import edu.kit.ifv.application.steps.HasDrtProviderRepo
import edu.kit.ifv.application.steps.HasHouseholdRepo
import edu.kit.ifv.application.steps.HasModeChoiceModel
import edu.kit.ifv.application.steps.HasModes
import edu.kit.ifv.application.steps.HasMutableArrayBackedImpedance
import edu.kit.ifv.application.steps.HasMutableAttractivenessModel
import edu.kit.ifv.application.steps.HasMutableDestinationChoiceModel
import edu.kit.ifv.application.steps.HasMutableModeAvailabilityModel
import edu.kit.ifv.application.steps.HasPersonAgentRepo
import edu.kit.ifv.application.steps.HasPersonRepo
import edu.kit.ifv.application.steps.HasReplanningStrategy
import edu.kit.ifv.application.steps.HasSharingProviderAgentRepo
import edu.kit.ifv.application.steps.HasSharingProviderRepo
import edu.kit.ifv.application.steps.HasSpawnDestinationCharacteristics
import edu.kit.ifv.application.steps.HasSpawnModeCharacteristics
import edu.kit.ifv.application.steps.HasZoneRepo
import edu.kit.ifv.core.modelsteps.Cloneable
import edu.kit.ifv.core.modelsteps.ExecutionMode
import edu.kit.ifv.core.modelsteps.initReport
import edu.kit.ifv.core.modelsteps.resources.MapRepository
import edu.kit.ifv.core.modelsteps.resources.MutableRepository
import edu.kit.ifv.domain.shared.behavior.AttractivenessModel
import edu.kit.ifv.domain.shared.behavior.ChoiceModelModes
import edu.kit.ifv.domain.shared.car.CarId
import edu.kit.ifv.domain.shared.data.household.HouseholdId
import edu.kit.ifv.domain.shared.data.person.PersonId
import edu.kit.ifv.domain.shared.datastructure.schedule.LinkTrip
import edu.kit.ifv.domain.shared.datastructure.schedule.replanning.HomeActivityEndTimeAnchorStrategy
import edu.kit.ifv.domain.shared.datastructure.schedule.replanning.ReplanningStrategy
import edu.kit.ifv.domain.shared.enums.Mode
import edu.kit.ifv.domain.shared.location.ArrayBackedImpedance
import edu.kit.ifv.domain.shared.location.StandardLocation
import edu.kit.ifv.domain.shared.location.zone.MaximalZone
import edu.kit.ifv.domain.shared.location.zone.ZoneId
import edu.kit.ifv.domain.simulation.agent.DrtProviderAgent
import edu.kit.ifv.domain.simulation.agent.PersonAgent
import edu.kit.ifv.domain.simulation.agent.SharingProviderAgent
import edu.kit.ifv.domain.simulation.behavior.availability.ModeAvailabilityModel
import edu.kit.ifv.domain.simulation.behavior.availability.rules.ModeAvailabilityByRule
import edu.kit.ifv.domain.simulation.behavior.destinationchoice.DestinationChoiceCharacteristics
import edu.kit.ifv.domain.simulation.behavior.destinationchoice.DestinationChoiceCharacteristicsImpl
import edu.kit.ifv.domain.simulation.behavior.modechoice.ModeChoiceCharacteristics
import edu.kit.ifv.domain.simulation.behavior.modechoice.ModeChoiceCharacteristicsImpl
import edu.kit.ifv.domain.simulation.data.car.MutablePrivateCar
import edu.kit.ifv.domain.simulation.data.car.PrivateCar
import edu.kit.ifv.domain.simulation.data.drt.DrtProvider
import edu.kit.ifv.domain.simulation.data.drt.DrtProviderId
import edu.kit.ifv.domain.simulation.data.drt.MutableDrtProviderData
import edu.kit.ifv.domain.simulation.data.household.Household
import edu.kit.ifv.domain.simulation.data.household.MutableHousehold
import edu.kit.ifv.domain.simulation.data.person.MutablePerson
import edu.kit.ifv.domain.simulation.data.person.Person
import edu.kit.ifv.domain.simulation.data.sharing.MutableSharingProvider
import edu.kit.ifv.domain.simulation.data.sharing.SharingProvider
import edu.kit.ifv.domain.simulation.data.sharing.SharingProviderId
import edu.kit.ifv.domain.simulation.events.GenerateDestinationCharacteristics
import edu.kit.ifv.domain.simulation.events.GenerateModeCharacteristics
import edu.kit.ifv.mobitopp.discretechoice.models.FixedChoiceModel
import edu.kit.ifv.utils.CodePlan
import edu.kit.ifv.utils.report.ReportBuilder
import edu.kit.ifv.utils.units.Time
import mode.PlaceholderProvider
import mode.tutorialModeChoiceModel
import util.TutorialMode
import util.tutorialChoiceModelModes
import util.tutorialChoiceModelPurposes

class FastRastattContext:
    HasZoneRepo<MaximalZone, MaximalZone>,
    HasHouseholdRepo<MutableHousehold, Household>,
    HasCarRepo<MutablePrivateCar, PrivateCar>,
    HasPersonRepo<MutablePerson, Person>,
    HasSharingProviderRepo<MutableSharingProvider, SharingProvider>,
    HasDrtProviderRepo<MutableDrtProviderData, DrtProvider>,
    Cloneable<RastattContext>,
    HasMutableAttractivenessModel,
    HasPersonAgentRepo<PersonAgent, PersonAgent>,
    HasSharingProviderAgentRepo<SharingProviderAgent, SharingProviderAgent>,
    HasDrtProviderAgentRepo<DrtProviderAgent, DrtProviderAgent>,
    HasMutableArrayBackedImpedance,
    HasModes, // TODO discuss whether modes are context or config
    HasMutableDestinationChoiceModel,
    HasAttractivenessModel,
    HasMutableModeAvailabilityModel,
    HasSpawnModeCharacteristics,
    HasModeChoiceModel,
    HasChoiceModelModes,
    HasSpawnDestinationCharacteristics,
    HasReplanningStrategy {
    override val scenarioName: String = "regression test short term scenario"
    override val modes: CodePlan<Mode> = TutorialMode
    override lateinit var impedance: ArrayBackedImpedance
    override var modeAvailability: ModeAvailabilityModel = ModeAvailabilityByRule(
        modes.values().associateWith {
            PlaceholderProvider(it)
        }
    )
    override var destinationChoiceModel: FixedChoiceModel<StandardLocation, DestinationChoiceCharacteristics> =
        WrappedDestinationChoice(
            { impedance },
            { attractiveness },
            choicesSpawner = { zoneRepository.elements.map { it.centroidLocation }.toSet() },
            identifiableMap = { it.zoneId },
            name = "FastDestinationChoiceModel",
            modeAvailability = modeAvailability
        )

    override val spawnModeCharacteristics: GenerateModeCharacteristics<ModeChoiceCharacteristics> =  {
            person, time, origin, destination, currentChoices,  custom ->
        ModeChoiceCharacteristicsImpl(person, time, origin, destination, currentChoices, custom)
    }
    override val modeChoiceModel: FixedChoiceModel<Mode, ModeChoiceCharacteristics> by lazy{
        context(impedance) {
            tutorialModeChoiceModel(zoneRepository::getValue)
        }
    }
    override val choiceModelModes: ChoiceModelModes = tutorialChoiceModelModes
    override val spawnDestinationCharacteristics: GenerateDestinationCharacteristics<DestinationChoiceCharacteristics> = {
            person: PersonAgent, time: Time, legs: LinkTrip ->
        DestinationChoiceCharacteristicsImpl(person, time, legs.origin)
    }
    override val replanningStrategy: ReplanningStrategy = HomeActivityEndTimeAnchorStrategy(tutorialChoiceModelPurposes)
    override lateinit var attractiveness: AttractivenessModel
    override val execMode: ExecutionMode = ExecutionMode()
    override val report: ReportBuilder = initReport()
    override val mutableZoneRepository: MutableRepository<MaximalZone, ZoneId> = MapRepository("zone")
    override val mutableHouseholdRepository: MutableRepository<MutableHousehold, HouseholdId> = MapRepository("household")
    override val mutableCarRepository: MutableRepository<MutablePrivateCar, CarId> = MapRepository("car")
    override val mutablePersonRepository: MutableRepository<MutablePerson, PersonId> = MapRepository("person")
    override val mutableSharingProviderRepository: MutableRepository<MutableSharingProvider, SharingProviderId> = MapRepository("sharingProvider")
    override val mutableDrtProviderRepository: MutableRepository<MutableDrtProviderData, DrtProviderId> = MapRepository("drtProvider")
    override val mutablePersonAgentRepository: MutableRepository<PersonAgent, PersonId> = MapRepository("PersonAgents")
    override val mutableSharingProviderAgentRepository: MutableRepository<SharingProviderAgent, SharingProviderId> = MapRepository("SharingProviderAgents")
    override val mutableDrtProviderAgentRepository: MutableRepository<DrtProviderAgent, DrtProviderId> = MapRepository("DrtProviderAgents")

    override fun clone(): RastattContext = RastattContext()

    override var currentStep: String = ""
}
