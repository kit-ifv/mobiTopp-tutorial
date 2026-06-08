package longterm

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import edu.kit.ifv.domain.jackson.standardCSVParse
import edu.kit.ifv.domain.shared.enums.areatype.RegioStaR17
import edu.kit.ifv.domain.shared.enums.areatype.RegionType
import edu.kit.ifv.domain.shared.location.StandardLocation
import edu.kit.ifv.domain.shared.location.StandardLocationImpl
import edu.kit.ifv.domain.shared.location.zone.Zone
import edu.kit.ifv.domain.shared.location.zone.ZoneId
import edu.kit.ifv.domain.shared.location.zone.attributes.HasCentroid
import edu.kit.ifv.domain.shared.location.zone.attributes.HasRegionType
import org.locationtech.jts.geom.CoordinateXY
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Point
import org.locationtech.jts.geom.PrecisionModel
import java.nio.charset.Charset
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.inputStream

class RastattZone(
    override val zoneId: ZoneId,
    override val attributes: RastattZoneAttributes
) : Zone<RastattZoneAttributes, > {
    override val centroidLocation: StandardLocation = StandardLocationImpl(attributes.centroid, this,)
    companion object {
        fun fromCsv(path: Path = Path("data/zone-repository/zones.csv")): List<RastattZone> {
            val dtos = standardCSVParse<ZoneAttributesRecord>(path.inputStream(), charset = Charset.forName
                ("windows-1252"))
            return fromRecord(dtos)
        }
        fun fromRecord(elements: Collection<ZoneAttributesRecord>): List<RastattZone> {
            return elements.map { RastattZone(
                ZoneId(it.zoneId),
                RastattZoneAttributes(
                    regionType = RegioStaR17.decode(it.regionTypeCode),
                    centroid = GeometryFactory(PrecisionModel(), 4326).createPoint(it.coordinate)

                )
            ) }
        }
    }
}

data class RastattZoneAttributes(

    override val regionType: RegionType,
    override val centroid: Point

): HasRegionType, HasCentroid

@JsonIgnoreProperties(ignoreUnknown = true)
data class ZoneAttributesRecord(
    @field:JsonProperty("id")
    val zoneId: Long,
    @field:JsonProperty("regionType")
    val regionTypeCode: Int,
    @field:JsonProperty("centroidLocation")
    val centroidText: String
) {
    val x = centroidText.split(",")[0].removePrefix("(").toDouble()
    val y = centroidText.split(":")[0].split(",")[1].toDouble()
    val coordinate = CoordinateXY(x, y)
}