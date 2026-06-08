package commuterticket


import util.binaryStructure
import util.multinomialLogit
import util.times

data class TicketParameters(
    val asc_Ticket: Double = -0.312173681653899,
    val b_hhgro_2: Double = -0.0256945305058742 -0.3,
    val b_hhgro_3: Double = 0.291424576249088  +0.3,
    val b_hhgro_4: Double = 0.426000821405854 +  0.05,
    val b_hhgro_5: Double = 0.652434997336812  + 0.05,
    val b_hhgro_6: Double = 1.1953115261173 +0.05,
    val b_hhgro_7: Double = 2.24858460071818 +0.05,
    val b_hhgro_8: Double = 3.23480976565317 +0.05,
    val b_weibl: Double = 0.0756731876254076 +0.2,
    val b_alter1a: Double = -3.25644379813947,
    val b_alter1b: Double = -1.28889752460068,
    val b_alter3: Double = -0.506306055009702 -0.2,
    val b_alter4: Double = -0.43068644042899 - 0.3,
    val b_alter5: Double = -0.440916518808974 -0.3,
    val b_alter6: Double = -0.412490615376118 -0.3,
    val b_alter7: Double = -0.322763855797514 +0.2,
    val b_alter8: Double = -0.170997032958524 +0.15,
    val b_no_fspkw: Double = 0.995492805381457,
    val b_pkwhh1: Double = -0.691970185373246,
    val b_pkwhh2: Double = -1.27273744779308,
    val b_pkwhh3: Double = -2.06584837619823,
    val b_pkwhh4: Double = -1.96838830652269,
    val b_schueler: Double = 1.59413587911005 - 0.15,
    val b_student: Double = 2.19941421528667  - 0.3,
    val b_home: Double = -1.78872181887129,
    val b_parttime: Double = -0.210495104059506,
    val b_EINKO2: Double = 0.13386078964763,
    val b_EINKO3: Double = 0.184411681953943,
    val b_EINKO4: Double = 0.355024064024888 + 0.2,
    val b_EINKO5: Double = 0.197233443164943 + 0.2,
    val b_EINKO6: Double = 0.257279239343281 + 0.2,
    val b_p05: Double = -0.291721944623581,
    val b_p617: Double = -0.305570335406822,
)

val commuterTicketChoiceModel = binaryStructure<TicketCharacteristics, TicketParameters> {
    option(false) { choice, sit ->
        0.0 //reference category
    }

    option(true) { choice, sit ->
        (
                asc_Ticket +
                        b_hhgro_2 * (sit.householdSize == 2) +
                        b_hhgro_3 * (sit.householdSize == 3) +
                        b_hhgro_4 * (sit.householdSize == 4) +
                        b_hhgro_5 * (sit.householdSize == 5) +
                        b_hhgro_6 * (sit.householdSize == 6) +
                        b_hhgro_7 * (sit.householdSize == 7) +
                        b_hhgro_8 * (sit.householdSize in 8..20) +
                        b_weibl * sit.isFemale +
                        b_alter1a * (sit.age in 0..9) +
                        b_alter1b * (sit.age in 10..17) +
                        b_alter3 * (sit.age in 30..39) +
                        b_alter4 * (sit.age in 40..49) +
                        b_alter5 * (sit.age in 50..59) +
                        b_alter6 * (sit.age in 60..69) +
                        b_alter7 * (sit.age in 70..79) +
                        b_alter8 * (sit.age >= 80) +
                        b_no_fspkw * (!sit.hasLicense) +
                        b_pkwhh1 * (sit.householdCars == 1) +
                        b_pkwhh2 * (sit.householdCars == 2) +
                        b_pkwhh3 * (sit.householdCars == 3) +
                        b_pkwhh4 * (sit.householdCars >= 4) +
                        b_schueler * sit.isEmploymentStudentPrimary +
                        b_schueler * sit.isEmploymentStudentSecondary +
                        b_student * sit.isEmploymentStudentTertiary +
                        b_home * sit.isEmploymentHomekeeper +
                        b_parttime * sit.isEmploymentParttime +
                        b_EINKO2 * (sit.householdIncomeEuro.toInt() in 750 until 1500) +
                        b_EINKO3 * (sit.householdIncomeEuro.toInt() in 1500 until 2250) +
                        b_EINKO4 * (sit.householdIncomeEuro.toInt() in 2250 until 3000) +
                        b_EINKO5 * (sit.householdIncomeEuro.toInt() in 3000 until 4000) +
                        b_EINKO6 * (sit.householdIncomeEuro.toInt() >= 4000) +
                        //      + b_p05* HOUSEHOLD_NUMBER_OF_NOT_SIMULATED_CHILDREN
                        b_p617 * (sit.householdMembersBetween(6,17))
                )
    }

}.multinomialLogit("Example Ticket Assignment Choice Model Structure").build(TicketParameters())
