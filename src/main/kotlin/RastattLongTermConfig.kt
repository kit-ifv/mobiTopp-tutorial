import java.nio.file.Path
import kotlin.io.path.Path

/**
 * This config class holds all the necessary paths and configuration fields for the long term model.
 */
data class RastattLongTermConfig(
    val attractivenessModelPath: Path = Path("data/zone-repository/attractivities.csv"),
    val outputDirectory: Path = Path("results/long-term/demand-data"),
    val logIpuResults: Boolean = true,
    val ipuRelativePath: Path = Path("IPULog.csv"),
    val zoneCommunityMappingPath: Path = Path("src/main/resources/zone-to-community.csv"),
    val commuterFilePath: Path = Path("src/main/resources/commuters-rastatt.csv"),
    val modernizedOutputDirectory: Path = Path("results/long-term-modernized/"),
)