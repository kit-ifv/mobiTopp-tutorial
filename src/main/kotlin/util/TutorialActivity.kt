package util

import edu.kit.ifv.domain.shared.behavior.ChoiceModelPurposes
import edu.kit.ifv.domain.shared.enums.ActivityType
import edu.kit.ifv.utils.codes.Decodable


@Suppress("MagicNumber")
enum class TutorialActivity(override val code: Int): ActivityType { //TODO welche activity types können wir streichen?
    HOME(7),
    BUSINESS(2),
    LEISURE_INDOOR(51),
    LEISURE_OUTDOOR(52),
    PRIVATE_BUSINESS(11),
    PRIVATE_VISIT(12),
    SERVICE(6),
    SHOPPING_DAILY(41),
    SHOPPING_OTHER(42),
    WORK(1),
    EDUCATION_PRIMARY(31),
    EDUCATION_SECONDARY(32),
    EDUCATION_TERTIARY(33),

    UNDEFINED(-99);

    override val description: String
        get() = this.name

    companion object : Decodable<ActivityType> {
        override fun decode(i: Int) = entries.first { it.code == i }
        override fun decode(s: String) = valueOf(s)
        override fun values(): Set<TutorialActivity> = entries.toSet()
    }

}

val tutorialChoiceModelPurposes = ChoiceModelPurposes(
    home = TutorialActivity.HOME,
    work = TutorialActivity.WORK,
    business = TutorialActivity.BUSINESS,
    shopping = TutorialActivity.SHOPPING_DAILY, //TODO
    privateBusiness = TutorialActivity.PRIVATE_BUSINESS,
    service = TutorialActivity.SERVICE,
    privateVisit = TutorialActivity.PRIVATE_VISIT,
    leisureTravel = TutorialActivity.UNDEFINED,
    businessTravel = TutorialActivity.UNDEFINED,
    education = TutorialActivity.EDUCATION_PRIMARY, //TODO
    leisure = TutorialActivity.LEISURE_INDOOR, //TODO
    undefined = TutorialActivity.UNDEFINED,

    allActivityTypes = TutorialActivity.entries.toSet(),
    leisureTypes = setOf(
        TutorialActivity.LEISURE_INDOOR,
        TutorialActivity.LEISURE_OUTDOOR,
        TutorialActivity.PRIVATE_VISIT,
    ),
    educationTypes = setOf(
        TutorialActivity.EDUCATION_PRIMARY,
        TutorialActivity.EDUCATION_SECONDARY,
        TutorialActivity.EDUCATION_TERTIARY
    ),
    shoppingTypes = setOf(
        TutorialActivity.SHOPPING_DAILY,
        TutorialActivity.SHOPPING_OTHER,
        TutorialActivity.PRIVATE_BUSINESS,
    ),
    businessTypes = setOf(
        TutorialActivity.BUSINESS,
    )
)