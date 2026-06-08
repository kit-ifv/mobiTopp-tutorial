import bikesharing.BikesharingCharacteristics
import bikesharing.bikesharingChoiceModel
import cars.NumberOfCarsCharacteristics
import cars.carChoiceModel
import commuterticket.TicketCharacteristics
import commuterticket.commuterTicketChoiceModel
import edu.kit.ifv.domain.jackson.writeCsv
import edu.kit.ifv.domain.shared.enums.areatype.ZoneRegionType
import edu.kit.ifv.domain.shared.location.StandardLocation
import edu.kit.ifv.domain.synthesis.AssignmentStrategy
import edu.kit.ifv.domain.synthesis.AttractivenessModelParser
import edu.kit.ifv.domain.synthesis.HouseholdAssignmentStep
import edu.kit.ifv.domain.synthesis.PopulationSynthesis
import edu.kit.ifv.domain.synthesis.SeededProvider
import edu.kit.ifv.domain.synthesis.assignAmountOfCars
import edu.kit.ifv.domain.synthesis.assignEconomicStatus
import edu.kit.ifv.domain.synthesis.assignTransitCardOwnership
import edu.kit.ifv.domain.synthesis.behavior.HouseholdFactory
import edu.kit.ifv.domain.synthesis.behavior.activitygeneration.ActiToppNGGenerator
import edu.kit.ifv.domain.synthesis.behavior.cars.generation.SamplingCarGeneration
import edu.kit.ifv.domain.synthesis.behavior.cars.ownership.UnfilteredSeniority
import edu.kit.ifv.domain.synthesis.behavior.economicstatus.OECDAssigner
import edu.kit.ifv.domain.synthesis.behavior.fixeddestinations.UseClosestLocation
import edu.kit.ifv.domain.synthesis.behavior.fixeddestinations.bandwidth.BandwidthLocator
import edu.kit.ifv.domain.synthesis.behavior.fixeddestinations.communitybased.CommunityBasedGroupLocator
import edu.kit.ifv.domain.synthesis.behavior.fixeddestinations.communitybased.CommuterDemandsMatrix
import edu.kit.ifv.domain.synthesis.behavior.fixeddestinations.communitybased.CommuterDistance
import edu.kit.ifv.domain.synthesis.behavior.fixeddestinations.primarySchool
import edu.kit.ifv.domain.synthesis.behavior.fixeddestinations.secondarySchool
import edu.kit.ifv.domain.synthesis.behavior.fixeddestinations.work
import edu.kit.ifv.domain.synthesis.behavior.householdlocation.AssignAroundPoint
import edu.kit.ifv.domain.synthesis.results.legacy.writeLegacyOutput
import edu.kit.ifv.domain.synthesis.rules.flatHierarchy
import edu.kit.ifv.domain.synthesis.withConverter
import edu.kit.ifv.populationsynthesis.algorithms.hierarchic.distribution.HierarchicDistribution
import edu.kit.ifv.populationsynthesis.algorithms.hierarchic.distribution.HierarchicDistributionConfig
import edu.kit.ifv.populationsynthesis.evaluation.Verification
import edu.kit.ifv.units.meters
import longterm.RastattHouseholdAttributes
import longterm.RastattPersonAttributes
import longterm.RastattZone
import longterm.rastattPopulationParser
import longterm.rules.RastattRuleProvider
import longterm.rules.RastattSynthesisZoneTargets
import util.TutorialActivity
import util.tutorialChoiceModelPurposes
import kotlin.io.path.createParentDirectories
import kotlin.random.Random


fun main() {
    /*
    We need several objects before we can start with the population synthesis. In particular we require:
    1) A config with all the relevant paths and options
    2) A rule provider for the synthesis with the marginal sum targets for the synthesis step.
    3) a list of avialable zones and a mapping to zone ids.
    4) an attractiveness model to generate the fixed destinations.
    5) Some locations for work and school to be the designated targets during fixed destination assignment.

    Note that these objects are only needed for this particular layout. If you were to change, say, the fixed
    destination generation for secondary school activities from a Bandwidth locator to another implementation,
    then the attractiveness model can be omitted as it is no longer part of the required parameters.


     */
    val config = RastattLongTermConfig()
    val ruleProvider = RastattRuleProvider(
        synthesisZoneTargets = RastattSynthesisZoneTargets.fromCsv()
    ).createRuleProvider().flatHierarchy()

    val zones: List<RastattZone> = RastattZone.fromCsv()
    val zoneMapping = zones.associateBy { it.zoneId }
    val attractivenessModel = AttractivenessModelParser.parse(config.attractivenessModelPath, tutorialChoiceModelPurposes)

    val primarySchools: List<StandardLocation> = zones.map { it.centroidLocation }
    val works: List<StandardLocation> = zones.map { it.centroidLocation }


    val populationSynthesis = PopulationSynthesis.configure(
        surveyPopulation = rastattPopulationParser(),
        zones = zones
    ) {
        outputDirectory = config.outputDirectory


    }




    populationSynthesis.execute(
        randomProvider = SeededProvider(
            seed = 42,
            personRandomSpawner = { seed, person -> Random(seed + person.attributes.personId) }
        )
    ) {

        synthesize(zoneMapping::getValue) {
            val hierarchicDistribution = HierarchicDistribution(
                ruleProvider = ruleProvider,
                seedHouseholds = surveyHouseholds,
                config = HierarchicDistributionConfig()
            )


            hierarchicDistribution.withConverter {
                HouseholdFactory(RastattHouseholdAttributes::copy, RastattPersonAttributes::copy).createFrom(it)
            }
        }
        if(config.logIpuResults) {
            val verificationOutput = Verification.verify(ruleProvider, householdsByZone.mapKeys { it.key.zoneId })
            writeCsv(outputDirectory.resolve(config.ipuRelativePath).createParentDirectories(), verificationOutput)
        }

        assignLocations {
            AssignAroundPoint(100.meters)
        }

        assignEconomicStatus {
            OECDAssigner.default()
        }

        assignAmountOfCars {
            AssignmentStrategy.viaChoiceModel(
                model = carChoiceModel,
                situation = NumberOfCarsCharacteristics::fromHousehold
            )
        }

        assignTransitCardOwnership {

            HouseholdAssignmentStep {
                context(TicketCharacteristics.fromPerson(it), randomProvider.provideFor(it)) {
                    commuterTicketChoiceModel.select()
                }

            }
        }

        assignSharingMemberships {
            provider("BIKESHARING") {
                AssignmentStrategy.viaChoiceModel(
                    model = bikesharingChoiceModel,
                    situation = BikesharingCharacteristics::fromPerson
                )
            }
        }

        assignFixedDestinations {
            work {
                activityType = TutorialActivity.WORK
                assignmentStrategy = CommunityBasedGroupLocator(
                    demands = CommuterDemandsMatrix.parse(
                        mappingFile = config.zoneCommunityMappingPath,
                        commuterFile = config.commuterFilePath
                    ),
                    strategy = CommuterDistance(),
                    potentialLocations = works
                )
            }
            primarySchool {
                activityType = TutorialActivity.EDUCATION_PRIMARY
                assignmentStrategy = UseClosestLocation(primarySchools)
            }
            secondarySchool {
                activityType = TutorialActivity.EDUCATION_SECONDARY
                assignmentStrategy =
                    BandwidthLocator(primarySchools, attractivenessModel, TutorialActivity.EDUCATION_SECONDARY)
            }

        }
        spawnCars(
            generationStrategy = SamplingCarGeneration { randomProvider.provideFor(it) },
            assignStrategy = UnfilteredSeniority()
        )
        assignActivities {
            ActiToppNGGenerator(tutorialChoiceModelPurposes) {
                ZoneRegionType.DEFAULT
            }
        }

        writeStandardOutputCSV(config.modernizedOutputDirectory)
        writeLegacyOutput()
    }
}