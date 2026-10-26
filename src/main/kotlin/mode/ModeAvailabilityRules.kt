package mode

import edu.kit.ifv.domain.shared.location.Impedance
import edu.kit.ifv.domain.simulation.agent.getBestCarOrNull
import edu.kit.ifv.domain.simulation.behavior.availability.BikeResource
import edu.kit.ifv.domain.simulation.behavior.availability.CarResource
import edu.kit.ifv.domain.simulation.behavior.availability.rules.ModeAvailabilityByRule
import edu.kit.ifv.domain.simulation.behavior.availability.rules.builder.availabilityRules
import util.bike
import util.bikesharing
import util.car
import util.passenger
import util.pedestrian
import util.publictransport

/*
Mode availability rules for all modes of the tutorial. The rules are copies of the framework defaults
(see defaultAvailabilityModel in mobitopp-reengineering), written out so they can be adapted for this model.

Every mode is defined in three stages:
1. staticRule:   Can the person use the mode at all? (e.g. driving licence, owns a bike, sharing membership)
2. providerRule: Is the mode available in the current situation? (e.g. at home with the vehicle, station nearby)
3. resourceRule: Which concrete resource is used? (e.g. which car of the household, which sharing vehicle)

Tip: call help() after availabilityOf(mode), staticRule { } or providerRule { } to print what is available in the
next scope, e.g. availabilityOf(car).help().

The impedance is passed as a supplier, because it is loaded by a model step after the context was created.
 */
fun tutorialModeAvailabilityRules(impedance: () -> Impedance): ModeAvailabilityByRule = availabilityRules {

    // Always available, does not need a vehicle.
    simpleAvailabilityOf(pedestrian)

    // Requires a bike. As a home based mode, the agent must start the tour at home, where the bike is.
    // Bikes are not shared within the household, so no provider is used.
    availabilityOf(bike).staticRule {
        person.hasBike
    }.providerRule {
        homeBasedVehicleRule(useProvider = false)
    }.resourceRule {
        BikeResource(bike)
    }

    // Requires a driving licence and a car in the household. As a home based mode, the agent must start the tour at
    // home. The household provides the car, and the best available car of the household is used.
    availabilityOf(car).staticRule {
        person.hasLicense && person.household.cars.isNotEmpty()
    }.providerRule {
        homeBasedVehicleRule(useProvider = true)
    }.resourceRule {
        characteristics.person.getBestCarOrNull()?.let { vehicle ->
            CarResource(car, vehicle)
        }
    }

    // Always available, the driver is not modelled.
    simpleAvailabilityOf(passenger)

    // Always available, the public transport supply is described by the impedance matrices only.
    simpleAvailabilityOf(publictransport)

    // One way bike sharing: requires a membership. A station (zone) of the provider must be in walking distance of
    // the agent and of the destination, and the station needs an available bike.
    // For station based sharing (return to the same station) see defaultSharingStationRules in the framework.
    availabilityOf(bikesharing).staticRule {
        person.sharingMemberships.any { it.mode == mode }
    }.providerRule {
        sharingProviders.checkOperatingHours()
            .stations()
            .checkAgentInFootZones()
            .checkDestinationInFootZones()
            .checkAnyAvailable()
    }.resourceRule {
        sharingProviders.checkOperatingHours()
            .stations()
            .checkVehiclesAvailable()
            .checkAgentInFootZones()
            .selectMinDistOneWaySharing(pedestrian, impedance())
    }
}
