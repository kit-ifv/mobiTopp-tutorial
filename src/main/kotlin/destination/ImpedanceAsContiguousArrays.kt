package destination

import edu.kit.ifv.domain.shared.location.Impedance
import util.TutorialMode

class ImpedanceAsContiguousArrays(
    private val travelTimePed: DoubleArray,
    private val travelTimeBike: DoubleArray,
    private val travelTimePt: DoubleArray,
    private val travelCostPt: DoubleArray,
    private val travelTimeBikesharing: DoubleArray,
    private val travelCostBikesharing: DoubleArray,
    private val travelTimeCar: DoubleArray,
    private val travelCostCar: DoubleArray,
    private val travelTimePedFix: DoubleArray,
    private val travelTimeBikeFix: DoubleArray,
    private val travelTimePtFix: DoubleArray,
    private val travelCostPtFix: DoubleArray,
    private val travelTimeBikesharingFix: DoubleArray,
    private val travelCostBikesharingFix: DoubleArray,
    private val travelTimeCarFix: DoubleArray,
    private val travelCostCarFix: DoubleArray,
    private val travelDistanceCar: DoubleArray,

    private val destinationCount: Int,
) {
    fun forOrigin(origin: Int, attractiveness: DoubleArray): OriginArrays {
        require(origin >= 0)

        return OriginArrays(
            source = this,
            attractiveness = attractiveness,
            offset = origin * destinationCount,
        )
    }

    class OriginArrays internal constructor(
        private val source: ImpedanceAsContiguousArrays,
        private val attractiveness: DoubleArray,
        private val offset: Int,
    ) {
        fun attractiveness(index: Int): Double =
            attractiveness[index]

        fun travelTimePed(index: Int): Double =
            source.travelTimePed[offset + index]

        fun travelTimeBike(index: Int): Double =
            source.travelTimeBike[offset + index]

        fun travelTimePt(index: Int): Double =
            source.travelTimePt[offset + index]

        fun travelCostPt(index: Int): Double =
            source.travelCostPt[offset + index]

        fun travelTimeBikesharing(index: Int): Double =
            source.travelTimeBikesharing[offset + index]

        fun travelCostBikesharing(index: Int): Double =
            source.travelCostBikesharing[offset + index]

        fun travelTimeCar(index: Int): Double =
            source.travelTimeCar[offset + index]

        fun travelCostCar(index: Int): Double =
            source.travelCostCar[offset + index]

        fun travelTimePedFix(index: Int): Double =
            source.travelTimePedFix[offset + index]

        fun travelTimeBikeFix(index: Int): Double =
            source.travelTimeBikeFix[offset + index]

        fun travelTimePtFix(index: Int): Double =
            source.travelTimePtFix[offset + index]

        fun travelCostPtFix(index: Int): Double =
            source.travelCostPtFix[offset + index]

        fun travelTimeBikesharingFix(index: Int): Double =
            source.travelTimeBikesharingFix[offset + index]

        fun travelCostBikesharingFix(index: Int): Double =
            source.travelCostBikesharingFix[offset + index]

        fun travelTimeCarFix(index: Int): Double =
            source.travelTimeCarFix[offset + index]

        fun travelDistanceCar(index: Int): Double =
            source.travelDistanceCar[offset + index]


        fun travelCostCarFix(index: Int): Double =
            source.travelCostCarFix[offset + index]
    }

    companion object {
        fun fromImpedance(impedance: Impedance, size: Int): ImpedanceAsContiguousArrays {
            return ImpedanceAsContiguousArrays(
                travelTimePed = impedance.durationArray(TutorialMode.PEDESTRIAN).values,
                travelTimeBike = impedance.durationArray(TutorialMode.BIKE).values,
                travelTimePt = impedance.durationArray(TutorialMode.PUBLICTRANSPORT).values,
                travelCostPt = impedance.costArray(TutorialMode.PUBLICTRANSPORT).values,
                travelTimeBikesharing = impedance.durationArray(TutorialMode.BIKESHARING).values,
                travelCostBikesharing = impedance.costArray(TutorialMode.BIKESHARING).values,
                travelTimeCar = impedance.durationArray(TutorialMode.CAR).values,
                travelCostCar = impedance.costArray(TutorialMode.CAR).values,
                travelTimePedFix = impedance.durationArray(TutorialMode.PEDESTRIAN).transpose().values,
                travelTimeBikeFix = impedance.durationArray(TutorialMode.BIKE).transpose().values,
                travelTimePtFix = impedance.durationArray(TutorialMode.PUBLICTRANSPORT).transpose().values,
                travelCostPtFix = impedance.costArray(TutorialMode.PUBLICTRANSPORT).transpose().values,
                travelTimeBikesharingFix = impedance.durationArray(TutorialMode.BIKESHARING).transpose().values,
                travelCostBikesharingFix = impedance.costArray(TutorialMode.BIKESHARING).transpose().values,
                travelTimeCarFix = impedance.durationArray(TutorialMode.CAR).transpose().values,
                travelCostCarFix = impedance.costArray(TutorialMode.CAR).transpose().values,
                destinationCount = size,
                travelDistanceCar = impedance.distanceArray(TutorialMode.CAR).values
            )
        }
    }
}