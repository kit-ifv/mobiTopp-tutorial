@file:Suppress("UnusedPrivateProperty", "MagicNumber")

import destination.tutorialDestinationChoiceModel
import edu.kit.ifv.application.config.subconfigs.BaseCSVFiles
import edu.kit.ifv.application.config.subconfigs.CoreCSVConfig
import edu.kit.ifv.application.steps.ActivityTypesConfig
import edu.kit.ifv.application.steps.AttractivenessFileConfig
import edu.kit.ifv.application.steps.CarCodesConfig
import edu.kit.ifv.application.steps.DrtSourceFilesConfig
import edu.kit.ifv.application.steps.HasCarRepo
import edu.kit.ifv.application.steps.HasDrtProviderAgentRepo
import edu.kit.ifv.application.steps.HasDrtProviderRepo
import edu.kit.ifv.application.steps.HasHouseholdRepo
import edu.kit.ifv.application.steps.HasModes
import edu.kit.ifv.application.steps.HasMutableAttractivenessModel
import edu.kit.ifv.application.steps.HasMutableImpedance
import edu.kit.ifv.application.steps.HasMutablePersonBehavior
import edu.kit.ifv.application.steps.HasPersonAgentRepo
import edu.kit.ifv.application.steps.HasPersonRepo
import edu.kit.ifv.application.steps.HasSharingProviderAgentRepo
import edu.kit.ifv.application.steps.HasSharingProviderRepo
import edu.kit.ifv.application.steps.HasZoneRepo
import edu.kit.ifv.application.steps.HouseholdCodesConfig
import edu.kit.ifv.application.steps.MatrixConfig
import edu.kit.ifv.application.steps.PurposesConfig
import edu.kit.ifv.application.steps.RegionCodesConfig
import edu.kit.ifv.application.steps.ResultsConfig
import edu.kit.ifv.application.steps.SharingModesConfig
import edu.kit.ifv.application.steps.SharingSourceFilesConfig
import edu.kit.ifv.application.steps.SimulationConfig
import edu.kit.ifv.application.steps.SourceFilesConfig
import edu.kit.ifv.application.steps.UnitConfig
import edu.kit.ifv.application.steps.model.assignHouseholdLocation
import edu.kit.ifv.application.steps.model.assignMainCarUsers
import edu.kit.ifv.application.steps.model.buildSimulationAgents
import edu.kit.ifv.application.steps.model.gaussianDurationRandomizer
import edu.kit.ifv.application.steps.model.loadBehaviorModels
import edu.kit.ifv.application.steps.model.simpleDrtAlgorithm
import edu.kit.ifv.application.steps.model.simulate
import edu.kit.ifv.application.steps.parser.csv.bikeSharingProviderCsv
import edu.kit.ifv.application.steps.parser.csv.bikeSharingProviderStationParser
import edu.kit.ifv.application.steps.parser.csv.carCsv
import edu.kit.ifv.application.steps.parser.csv.cars
import edu.kit.ifv.application.steps.parser.csv.filterFractionOfPopulation
import edu.kit.ifv.application.steps.parser.csv.fixedDestinationCsv
import edu.kit.ifv.application.steps.parser.csv.fixedDestinations
import edu.kit.ifv.application.steps.parser.csv.householdCsv
import edu.kit.ifv.application.steps.parser.csv.households
import edu.kit.ifv.application.steps.parser.csv.loadActivities
import edu.kit.ifv.application.steps.parser.csv.loadAttractivenessModelFromCsv
import edu.kit.ifv.application.steps.parser.csv.loadCars
import edu.kit.ifv.application.steps.parser.csv.loadHouseholds
import edu.kit.ifv.application.steps.parser.csv.loadPersons
import edu.kit.ifv.application.steps.parser.csv.loadSharingProviders
import edu.kit.ifv.application.steps.parser.csv.loadZones
import edu.kit.ifv.application.steps.parser.csv.personCsv
import edu.kit.ifv.application.steps.parser.csv.persons
import edu.kit.ifv.application.steps.parser.csv.plannedActivities
import edu.kit.ifv.application.steps.parser.csv.plannedActivityCsv
import edu.kit.ifv.application.steps.parser.csv.sharingProviders
import edu.kit.ifv.application.steps.parser.csv.zoneCsv
import edu.kit.ifv.application.steps.parser.csv.zones
import edu.kit.ifv.application.steps.parser.csv.zonesByFootInRadius
import edu.kit.ifv.application.steps.parser.loadImpedance
import edu.kit.ifv.application.steps.results.addPlot
import edu.kit.ifv.application.steps.results.createHtmlReport
import edu.kit.ifv.application.steps.results.distance
import edu.kit.ifv.application.steps.results.households
import edu.kit.ifv.application.steps.results.personLegs
import edu.kit.ifv.application.steps.results.writeTrips
import edu.kit.ifv.core.modelsteps.Cloneable
import edu.kit.ifv.core.modelsteps.Config
import edu.kit.ifv.core.modelsteps.ExecutionMode
import edu.kit.ifv.core.modelsteps.Simulation
import edu.kit.ifv.core.modelsteps.initReport
import edu.kit.ifv.core.modelsteps.resources.MapRepository
import edu.kit.ifv.core.modelsteps.resources.MutableRepository
import edu.kit.ifv.core.results.plots.asHistogram
import edu.kit.ifv.core.results.plots.asLinePlot
import edu.kit.ifv.core.results.plots.data.Ordering
import edu.kit.ifv.core.results.plots.forData
import edu.kit.ifv.core.results.plots.modeStringColor
import edu.kit.ifv.core.results.plots.normalizeByGroup
import edu.kit.ifv.domain.shared.behavior.AttractivenessModel
import edu.kit.ifv.domain.shared.car.CarId
import edu.kit.ifv.domain.shared.car.CarSegment
import edu.kit.ifv.domain.shared.data.household.HouseholdId
import edu.kit.ifv.domain.shared.data.person.PersonId
import edu.kit.ifv.domain.shared.datastructure.matrix.KeyBasedMatrixCreation
import edu.kit.ifv.domain.shared.datastructure.matrix.ZoneMatrixCreation
import edu.kit.ifv.domain.shared.enums.ActivityType
import edu.kit.ifv.domain.shared.enums.MODEUNKOWN
import edu.kit.ifv.domain.shared.enums.Mode
import edu.kit.ifv.domain.shared.enums.areatype.RegioStaR17
import edu.kit.ifv.domain.shared.enums.areatype.RegionType
import edu.kit.ifv.domain.shared.enums.household.EconomicStatus
import edu.kit.ifv.domain.shared.location.Impedance
import edu.kit.ifv.domain.shared.location.zone.MaximalZone
import edu.kit.ifv.domain.shared.location.zone.ZoneId
import edu.kit.ifv.domain.simulation.agent.DrtProviderAgent
import edu.kit.ifv.domain.simulation.agent.PersonAgent
import edu.kit.ifv.domain.simulation.agent.SharingProviderAgent
import edu.kit.ifv.domain.simulation.behavior.kilometers
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
import edu.kit.ifv.domain.simulation.events.PersonBehavior
import edu.kit.ifv.domain.simulation.events.drtProviderStateMachine
import edu.kit.ifv.domain.simulation.events.personStateMachine
import edu.kit.ifv.domain.simulation.parser.SharingProviderByStationCsvColumns
import edu.kit.ifv.units.CurrencyUnit
import edu.kit.ifv.units.DistanceUnit
import edu.kit.ifv.units.UnitIntervalValue
import edu.kit.ifv.units.meters
import edu.kit.ifv.units.share
import edu.kit.ifv.utils.CodePlan
import edu.kit.ifv.utils.ErrorHandling
import edu.kit.ifv.utils.collections.asBins
import edu.kit.ifv.utils.collections.mapToBins
import edu.kit.ifv.utils.report.ReportBuilder
import edu.kit.ifv.utils.units.AbsoluteTime
import mode.tutorialModeChoiceModel
import util.TutorialActivity
import util.TutorialMode
import util.tutorialChoiceModelModes
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit


class RastattConfig :
    Config,
    SimulationConfig,
    MatrixConfig,
    UnitConfig,
    RegionCodesConfig,
    HouseholdCodesConfig,
    CarCodesConfig,
    ActivityTypesConfig,
    SourceFilesConfig,
    SharingSourceFilesConfig,
    SharingModesConfig,
    DrtSourceFilesConfig,
//    DrtModesConfig,
    AttractivenessFileConfig,
    PurposesConfig,
    ResultsConfig {
    override val seed: Long = 42L
    override val fractionOfPopulation: UnitIntervalValue = 1.0.share()
    override val simulationStart: AbsoluteTime = AbsoluteTime.START //Start of simulation is set to 0 (Monday 00:00)
    override val simulationEnd: AbsoluteTime = simulationStart + 1.days
    override val timeStep: Duration = 1.minutes

    override val errorHandling: ErrorHandling = ErrorHandling.THROW

    private val matrixBasePath = Path("data/matrix")
    override val costMatrixConfig: Path = matrixBasePath.resolve("cost-matrix-configuration.yaml")
    override val durationMatrixConfig: Path = matrixBasePath.resolve("time-matrix-configuration.yaml")
    override val distanceMatrix: Path = matrixBasePath.resolve("other/CAR_DIS.mtx/CAR_DIS.mtx.bz2")
    override val matrixCreation: ZoneMatrixCreation = KeyBasedMatrixCreation

    override val resultDir: Path = Path("results/short-term")

    override val cachePath: Path = resultDir.resolve("cache")
    override val sourceFiles: BaseCSVFiles = CoreCSVConfig(
        dataRepo = Path("results/long-term/demand-data"),
        zoneRepo = Path("data/zone-repository/"),
        attractivitiesCSV = Path("data/zone-repository/attractivities.csv"),
    )

    override val bikeSharingStations: Path = Path("data/zone-repository/sharing/bikesharing-properties.csv")

    // currently not used in the Rastatt model
    override val carSharingStations: Path = Path("")
    override val carSharingFloatingArea: Path = Path("")
    override val ridePoolingServiceAreas: Path = Path("")

    override val attractivenessFile: Path = sourceFiles.attractivitiesCSV

    override val distanceUnit: DistanceUnit = DistanceUnit.METERS
    override val durationUnit: DurationUnit = DurationUnit.MINUTES
    override val currencyUnit: CurrencyUnit = CurrencyUnit.EUROS

    override val carSegmentCodes: CodePlan<CarSegment> = CarSegment
    override val regionTypeCodes: CodePlan<RegionType> = RegioStaR17
    override val activityTypes: CodePlan<ActivityType> = TutorialActivity
    override val economicStatusCodes: CodePlan<EconomicStatus> = EconomicStatus


    override val work: ActivityType = TutorialActivity.WORK
    override val privateVisit: ActivityType = TutorialActivity.PRIVATE_VISIT

    override val bikeSharingMode: Mode = TutorialMode.BIKESHARING

    //not used in this tutorial
    override val carSharingStationMode: Mode = MODEUNKOWN
    override val carSharingFloatingMode: Mode = MODEUNKOWN
//
//    override val ridePoolingMode: Mode = TutorialMode.RIDE_POOLING
}

class RastattContext :
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
    HasMutableImpedance,
    HasModes, // TODO discuss whether modes are context or config
    HasMutablePersonBehavior {
    override val scenarioName: String = "regression test short term scenario"
    override val modes: CodePlan<Mode> = TutorialMode
    override lateinit var impedance: Impedance
    override lateinit var attractiveness: AttractivenessModel
    override lateinit var personBehavior: PersonBehavior
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

fun main(args: Array<String>) {

    /*
     * Welcome to the mobiTopp tutorial!
     * This main function demonstrates how to set up and run a simulation using 'ModelSteps'.
     *
     * Concept 1: Two-Phase Operation
     * Every simulation runs in two phases:
     * 1. Validation Phase: Checks for errors (e.g., missing files) without running heavy logic.
     * 2. Execution Phase: If validation passes, the context is reset and the actual simulation runs.
     * This ensures that obvious mistakes are caught early before a long-running simulation starts.
     *
     * Concept 2: Config vs. Context
     * - Config (MyConfig): Holds all static configuration parameters such as file paths, seeds, and code plans.
     * - Context (MyContext): Holds intermediate model step results (e.g., loaded persons, zones).
     *   (The context is passed as a constructor (::MyContext) so the simulation can create a fresh context
     *   for each phase: validation and execution).
     */
    Simulation(RastattConfig()) {
        RastattContext()

    }.steps {
        // Load travel time, cost, and distance matrices for all transport modes.
        loadImpedance()

        // Define the spatial zones and their properties for the study area.
        zones(sealed = true) {
            loadZones(zoneCsv())
        }

        // Define how attractive each zone is for different activities (e.g., work, shopping).
        loadAttractivenessModelFromCsv()

        /*
         * Concept 3: Customization of Model Steps
         * Model steps have default settings based on the config object but can be customized.
         * For example, below we customize the CSV parser for bike sharing by explicitly
         * mapping the CSV columns to our input csv columns which differ from the expected default columns.
         */
        // Define available mobility service providers, such as bike sharing stations.
        sharingProviders(sealed=true) {
            loadSharingProviders(
                bikeSharingProviderCsv(
                    parser = bikeSharingProviderStationParser {
                        columns = SharingProviderByStationCsvColumns(
                            provider = "system",
                            numVehicles = "num_vehicles",
                            zone = "zoneId",
                            uid = "zoneId",
                            name = "zoneId"
                        )
                        zonesByFoot = zonesByFootInRadius(500.meters)
                    }
                )
            )
        }

        val zoneByIndex: (ZoneId) -> MaximalZone = { zoneRepository.elements.elementAt(it.value.toInt()) }

        // Load the household population and assign their home locations.
        households {
            // Read household data from CSV.
            loadHouseholds(
                householdCsv()
            )

            // Optionally scale the population to a smaller fraction for faster runs.
            filterFractionOfPopulation()

            // Assign precise geographic coordinates to households within their zones.
            assignHouseholdLocation()
        }

        // Load the individuals within the households and their scheduled activitie
        persons {
            // Read person data from CSV.
            loadPersons(personCsv())

            // Define the daily activity plans for each person.
            plannedActivities {
                // Read scheduled activities from CSV.
                loadActivities(plannedActivityCsv())

                // Link specific activities (like 'Home') to fixed locations.
                fixedDestinations(
                    homeActivity = TutorialActivity.HOME,
                    fixedDestinationCsv(),
                )
            }

            // e.g. Determine eligibility for Demand Responsive Transport (DRT) services.
//                addDrtMembershipsIf { person, provider ->
//                    person.age > 16
//                }
        }

        // Initialize the private car fleet and determine primary users.
        cars {
            // Read car data from CSV.
            loadCars(carCsv())
            // Assign the main driver for each household car.
            assignMainCarUsers()
        }

        // Initialize decision-making models for destination and mode choice.
        loadBehaviorModels(
            tutorialDestinationChoiceModel,
            tutorialModeChoiceModel(zoneRepository::getValue),
            tutorialChoiceModelModes,
        )

        // Convert static data into active agents ready for the agent-based simulation.
        buildSimulationAgents( // TODO maybe create individual model steps to set up the state machines
            personStateMachine,
            drtStateMachine = drtProviderStateMachine,
            drtAlgorithm = { _ ->
                simpleDrtAlgorithm(
                    impedance,
                    zoneRepository.elements.filter { it.isDestination }.toList(),
                )
            },
            durationRandomizer = gaussianDurationRandomizer(),
        )

        // Run the agent-based simulation.
        simulate()

        // Export the simulated trips to a CSV file.
        writeTrips()

        /*
         * Concept 4: Reporting and Results
         * Model steps can also be used to generate results such as plots.
         * The 'addPlot' function adds a visualization to the simulation report.
         */
        addPlot {
            forData {
                personLegs()
            }.groupBy {
                it.leg.transportType
            }.count {
                it.leg.startTime.roundToMultipleOf(5.minutes)
            }.sortX {
                Ordering.Ascending()
            }.asLinePlot {
                name = "timeline by mode"
                xAxisLabel = "time"
                yAxisLabel = "trip count"
                coloring = { modeStringColor(it.description) }
            }
        }

        addPlot {
            forData {
                personLegs()
            }.count {
                it.leg.transportType
            }.normalizeByGroup().asHistogram {
                name = "modal split"
                xAxisLabel = "mode"
            }
        }

        addPlot {
            forData {
                households()
            }.count {
                it.cars.size
            }.asHistogram {
                name = "number of cars per household distribution"
            }
        }


        val distBins = listOf(
            0.0 to 0.5,
            0.5 to 1.0,
            1.0 to 2.0,
            2.0 to 5.0,
            5.0 to 10.0,
            10.0 to 20.0,
            20.0 to 50.0,
            50.0 to 100.0
        ).asBins(appendOpenBin = true)

        addPlot {
            forData {
                personLegs()
            }.count {
                it.distance(impedance).kilometers.mapToBins(distBins)
            }.normalizeByGroup().asHistogram {
                name = "distance distribution"
                xAxisLabel = "mode"
            }
        }


        // Export simulation log report as HTML file.
        createHtmlReport()

    }

}

