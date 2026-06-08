package longterm

import edu.kit.ifv.domain.shared.enums.person.Employment
import edu.kit.ifv.domain.shared.enums.person.Sex
import edu.kit.ifv.domain.synthesis.attributes.person.MaximumPersonAttributes
import edu.kit.ifv.units.Distance

data class RastattPersonAttributes(
    val personId: Long,
    override val age: Int,
    override val sex: Sex,
    override val distanceWork: Distance,
    override val distanceEducation: Distance,
    override var employment: Employment,
    override val hasBicycle: Boolean,
    override val hasLicence: Boolean,
    override var hasTransitPass: Boolean,
): MaximumPersonAttributes
