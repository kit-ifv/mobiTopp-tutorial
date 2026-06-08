package cars


import edu.kit.ifv.domain.shared.enums.household.EconomicStatus
import edu.kit.ifv.domain.synthesis.SynthesisHousehold
import edu.kit.ifv.domain.synthesis.attributes.person.hasLicence
import longterm.RastattHouseholdAttributes
import longterm.RastattPersonAttributes

class NumberOfCarsCharacteristics (
    val household: SynthesisHousehold<RastattHouseholdAttributes, RastattPersonAttributes>,
) {


    companion object {
        fun fromHousehold(household: SynthesisHousehold<RastattHouseholdAttributes, RastattPersonAttributes>): NumberOfCarsCharacteristics {
            return NumberOfCarsCharacteristics(
                household,
            )
        }
    }
}

//val NumberOfCarsCharacteristics.type: Int get() = household.type
val NumberOfCarsCharacteristics.householdIncomeEuro: Double get() = household.income.inEuros
val NumberOfCarsCharacteristics.householdEconomicStatus: Int get() = household.attributes.economicStatus.code
val NumberOfCarsCharacteristics.isHouseholdEconomicStatusVeryLow: Boolean get() = (household.attributes.economicStatus == EconomicStatus.VERY_LOW)
val NumberOfCarsCharacteristics.isHouseholdEconomicStatusLow: Boolean get() = (household.attributes.economicStatus == EconomicStatus.LOW)
val NumberOfCarsCharacteristics.isHouseholdEconomicStatusMiddle: Boolean get() = (household.attributes.economicStatus == EconomicStatus.MIDDLE)
val NumberOfCarsCharacteristics.isHouseholdEconomicStatusHigh: Boolean get() = (household.attributes.economicStatus == EconomicStatus.HIGH)
val NumberOfCarsCharacteristics.isHouseholdEconomicStatusVeryHigh: Boolean get() = (household.attributes.economicStatus == EconomicStatus.VERY_HIGH)
//income class?
val NumberOfCarsCharacteristics.householdSize: Int get() = household.members.size
val NumberOfCarsCharacteristics.householdCars: Int get() = household.cars.size
val NumberOfCarsCharacteristics.adultsInHousehold: Int get() = household.members.count { it.age >= 18 }

val NumberOfCarsCharacteristics.householdType: Int get() = household.type.code
val NumberOfCarsCharacteristics.homeRegionType: Int get() = household.attributes.location.regionType.code

val NumberOfCarsCharacteristics.householdNumberOfLicense: Int get() = household.members.count { it.hasLicence }


val NumberOfCarsCharacteristics.regioStaRGem5Code: Int get() = household.attributes.location.regionType.toRegioStaR17().toRegioStaRGem5().code

