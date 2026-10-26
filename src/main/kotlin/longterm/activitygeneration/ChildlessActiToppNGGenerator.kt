package longterm.activitygeneration

import edu.kit.ifv.domain.shared.behavior.ChoiceModelPurposes
import edu.kit.ifv.domain.shared.datastructure.schedule.action.RawActivity
import edu.kit.ifv.domain.shared.location.StandardLocation
import edu.kit.ifv.domain.synthesis.attributes.household.HasNumberOfCars
import edu.kit.ifv.domain.synthesis.attributes.household.MinimumHouseholdAttributes
import edu.kit.ifv.domain.synthesis.attributes.person.HasCommuteDistance
import edu.kit.ifv.domain.synthesis.attributes.person.HasEducationDistance
import edu.kit.ifv.domain.synthesis.attributes.person.HasEmployment
import edu.kit.ifv.domain.synthesis.attributes.person.MinimumPersonAttributes
import edu.kit.ifv.domain.synthesis.attributes.person.employment
import edu.kit.ifv.domain.synthesis.behavior.ISurveyHousehold
import edu.kit.ifv.domain.synthesis.behavior.MinimalistHousehold
import edu.kit.ifv.domain.synthesis.behavior.SurveyPerson
import edu.kit.ifv.domain.synthesis.behavior.activitygeneration.ACTHousehold
import edu.kit.ifv.domain.synthesis.behavior.activitygeneration.ActiToppAdapter
import edu.kit.ifv.domain.synthesis.behavior.activitygeneration.GenerateHouseholdActivitySchedule
import edu.kit.ifv.domain.synthesis.behavior.activitygeneration.PreliminaryActivitySchedule
import edu.kit.ifv.domain.synthesis.behavior.activitygeneration.StandardActiToppAdapter
import edu.kit.ifv.mobitopp.actitoppNG.ActiToppHousehold
import edu.kit.ifv.mobitopp.actitoppNG.ActitoppPerson
import edu.kit.ifv.mobitopp.actitoppNG.AllChoiceModels
import edu.kit.ifv.mobitopp.actitoppNG.HouseholdPlanGeneration
import edu.kit.ifv.mobitopp.actitoppNG.PersonAttributes
import edu.kit.ifv.mobitopp.actitoppNG.PlanGenerationParameters
import edu.kit.ifv.mobitopp.actitoppNG.StandardHouseholdPlanGeneration
import edu.kit.ifv.mobitopp.actitoppNG.enums.AreaType
import edu.kit.ifv.mobitopp.actitoppNG.modernization.ReusablePlanGeneration
import edu.kit.ifv.mobitopp.actitoppNG.modernization.plan.MobilityPlan
import edu.kit.ifv.units.Distance
import edu.kit.ifv.units.kilometers
import edu.kit.ifv.utils.units.sinceStart
import java.lang.Double
import kotlin.collections.component1
import kotlin.collections.component2

class ChildlessActiToppNGGenerator<in S, in T>(
    val purposes: ChoiceModelPurposes,
    val strategy: HouseholdPlanGeneration = StandardHouseholdPlanGeneration.fromModels(
        models = AllChoiceModels.create(PlanGenerationParameters()),

        ) {
        ReusablePlanGeneration(it)
    },
    val maxCommute: Distance = 150.kilometers,
    private val actiToppAdapter: ActiToppAdapter<S, T>,
    private val personIsAllowedToWork: (SurveyPerson<T>) -> Boolean = { true },
) : GenerateHouseholdActivitySchedule<S, T>
        where S : MinimumHouseholdAttributes,
              S : HasNumberOfCars,
              T : MinimumPersonAttributes,
              T : HasCommuteDistance,
              T : HasEducationDistance,
              T : HasEmployment {
    /**
     * Legacy constructor to allow the construction via a converter lambda expression rather than passing an adapter.
     */
    constructor(purposes: ChoiceModelPurposes, converter: (MinimalistHousehold<S, T>) -> AreaType) : this(
        purposes,
        actiToppAdapter = StandardActiToppAdapter(purposes, converter = converter),
    )

    /** Generates a schedule for *all* members of the supplied [household]. */
    override fun generate(household: ISurveyHousehold<S, T>): List<PreliminaryActivitySchedule> {
        val actHH = convert(household)
        val output = strategy.generateSchedules(actHH)

        val schedules = output.values.iterator()

        return household.members.map { member ->
            if (member.age >= 10) {
                finish(schedules.next())
            } else {
                PreliminaryActivitySchedule.STAY_AT_HOME
            }
        }
    }

    /**
     * Completes a raw [MobilityPlan] by converting its activities to the
     * project‑specific model.
     */
    fun finish(mobilityPlan: MobilityPlan): PreliminaryActivitySchedule {
        val finishedActivities = mobilityPlan.finish()
        return PreliminaryActivitySchedule(
            finishedActivities.map {
                RawActivity(
                    location = StandardLocation.LOCATIONUNKNOWN,
                    startTime = it.startTime!!.sinceStart,
                    endTime = it.endTime!!.sinceStart,

                    type = actiToppAdapter.decodeActivityType(it.activityType),
                )
            }.toMutableList(),
        )
    }

    /**
     * Converts a survey household to an actiTopp [ACTHousehold] and creates
     * a corresponding [ActitoppPerson] for each member.
     *
     * @return the populated actiTopp household.
     */
    fun convert(household: ISurveyHousehold<S, T>): ACTHousehold {
        val actHousehold = ActiToppHousehold(
            numMinorsUpTo10 = household.numberOfChilds,
            numMinorsBelow18 = household.numberOfYouths,
            areaType = actiToppAdapter.encodeRegionType(household),
            numberOfCars = household.attributes.amountOfCars,
        )
        household.members.filter { it.age >= 10 }.forEach {
            ActitoppPerson(actHousehold, it.actitoppAttributes())
        }
        return actHousehold
    }

    @Suppress("MagicNumber") // TODO this may be relevant to fix, age 10 is magic
    val ISurveyHousehold<*, *>.numberOfChilds get() = members.count { it.age <= 10 }

    @Suppress("MagicNumber") // TODO this may be relevant to fix, age 10 is magic
    val ISurveyHousehold<*, *>.numberOfYouths get() = members.count { it.age in 10..<18 }

    /**
     * Builds a [PersonAttributes] instance from a [SurveyPerson].
     *
     * Commute distances are truncated to [maxCommute] to keep actiTopp stable.
     */
    fun SurveyPerson<T>.actitoppAttributes(): PersonAttributes = PersonAttributes(
        gender = actiToppAdapter.encodeSex(sex),
        employment = actiToppAdapter.encodeEmployment(employment),
        age = age,
        commuteDistanceWork = Double.min(attributes.distanceWork.inKilometers, maxCommute.inKilometers),
        commuteDistanceEducation = Double.min(attributes.distanceEducation.inKilometers, maxCommute.inKilometers),
        isAllowedToWork = personIsAllowedToWork(this),
    )
}