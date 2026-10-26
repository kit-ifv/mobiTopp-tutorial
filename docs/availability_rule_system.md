# Mode Availability Rule System in mobiTopp

This document explains how mobiTopp decides which transport modes an agent may choose, and how to define these
decisions with the availability rule DSL (`availabilityRules { }`). It goes from the general concepts (the three
availability stages and mode resources) to the DSL and its scopes, and ends with examples taken from this tutorial
(`src/main/kotlin/mode/ModeAvailabilityRules.kt`) and the framework defaults.

## Table of Contents
- [Overview](#overview)
- [Core Concepts](#core-concepts)
  - [The Three Availability Stages](#the-three-availability-stages)
  - [Providers and ProviderAvailability](#providers-and-provideravailability)
  - [Mode Resources](#mode-resources)
  - [ModeAvailabilityModel and AvailabilityRule](#modeavailabilitymodel-and-availabilityrule)
  - [How the Stages are Used During Simulation](#how-the-stages-are-used-during-simulation)
  - [Built-in Behavior of Every Rule](#built-in-behavior-of-every-rule)
- [The Availability Rule DSL](#the-availability-rule-dsl)
  - [Creating a Rule Set: availabilityRules { }](#creating-a-rule-set-availabilityrules--)
  - [Defining a Rule: availabilityOf(mode)](#defining-a-rule-availabilityofmode)
  - [Static Rule Scope](#static-rule-scope)
  - [Provider Rule Scope](#provider-rule-scope)
  - [Resource Rule Scope](#resource-rule-scope)
  - [Shortcuts: simpleAvailabilityOf, default(), providersIf](#shortcuts-simpleavailabilityof-default-providersif)
  - [Discovering the Scopes: help()](#discovering-the-scopes-help)
  - [Predefined Rules](#predefined-rules)
- [Integrating the Model into a Simulation](#integrating-the-model-into-a-simulation)
  - [Model Steps](#model-steps)
  - [Setting the Context Property Directly](#setting-the-context-property-directly)
  - [Using Availability in Choice Models](#using-availability-in-choice-models)
- [Examples](#examples)
  - [Always Available Modes](#always-available-modes)
  - [Home-based Private Vehicles (Bike, Car)](#home-based-private-vehicles-bike-car)
  - [Sharing Modes (Free-floating and Station-based)](#sharing-modes-free-floating-and-station-based)
  - [Ride Pooling (DRT)](#ride-pooling-drt)
  - [Custom Rules](#custom-rules)
  - [Implementing AvailabilityRule Directly](#implementing-availabilityrule-directly)
- [Reference](#reference)

## Overview

Before an agent chooses a mode for a trip, mobiTopp has to know which modes are actually possible:
a person without a driving licence cannot drive, a bike left at home cannot be used at work,
and bike sharing is only possible if a station with a free bike is within walking distance.

The availability rule system answers these questions per mode. It separates:
- **What a person can generally use** (attributes such as licence, bike ownership, memberships)
- **What is possible in the current situation** (location, time, nearby stations, operating areas)
- **Which concrete resource is used** (which car of the household, which sharing vehicle, which ride offer)

The main entry points are found in `edu.kit.ifv.domain.simulation.behavior.availability`:
- Model interface and resources: `ModeAvailabilityModel`, `ProviderAvailability`, `ModeResource` and its implementations
- Rule based implementation: `rules.AvailabilityRule`, `rules.ModeAvailabilityByRule`
- DSL: `rules.builder.availabilityRules`, `availabilityOf`, `staticRule`, `providerRule`, `resourceRule`
- Defaults: `defaultAvailabilityModel(...)` and `rules.DefaultRules.kt`

## Core Concepts

### The Three Availability Stages

Availability is broken down into three stages that build on top of each other
(resource > uses > provider > uses > static):

| Stage | Question | Input | Result |
|---|---|---|---|
| **Static** | Can the person use the mode at all? | `IPerson` | `Boolean` |
| **Provider** | Is the mode available in the current situation, and which providers are involved? | `PersonAgent`, time, destination | `ProviderAvailability` |
| **Resource** | Which concrete resource is used for the trip? | `ModeChoiceCharacteristics` | `ModeResource?` (`null` = not available) |

Examples:
- *Static*: the person has a driving licence and the household owns a car; the person is member of a sharing provider.
- *Provider*: the agent is at home where the car is parked; a sharing station is within walking distance and open.
- *Resource*: the best car of the household that is not in use; the nearest station that still has a vehicle.

This split allows reusing the model at different stages of decision-making: e.g. the destination choice can
cheaply check provider availability for every potential destination, while the expensive resource check is
only done once for the selected destination. Static availability could also be precomputed per person.

### Providers and ProviderAvailability

Some modes depend on resources that are shared between agents: the cars of a household, the vehicles of a sharing
station, the fleet of a ride pooling (DRT) provider. These are called **providers**.

The provider stage returns a `ProviderAvailability`:

```kotlin
data class ProviderAvailability(val mode: Mode, val providers: Collection<Any>? = null) {
    val isAvailable: Boolean = (providers != null)
    val isNotAvailable: Boolean = (providers == null)
}
```

- `providers == null` means the mode is **not available**.
- An empty collection means the mode is **available without any shared provider** (e.g. pedestrian, own bike).
- A non-empty collection lists the providers that are **affected** by the choice (e.g. the household, sharing stations).

The affected providers are important for parallel simulation: before checking resource availability,
the simulation locks all affected providers (mutex), so that two agents cannot take the same vehicle.
Helpers: `mode.available(...)`, `mode.notAvailable`, `Collection<ProviderAvailability>.flatten()` and
`ModeAvailabilityModel.currentlyAffectedProviders(...)`.

### Mode Resources

The resource stage returns a `ModeResource`, which represents what is needed to use a mode
(or `null` if the mode is not available):

```kotlin
interface ModeResource {
    val mode: Mode
    fun startTrip(person: PersonAgent)
    fun endTrip(person: PersonAgent): ModeResource?
}
```

- `startTrip` is called when the trip starts (e.g. mark the car as in use, take a vehicle from a station, book a ride).
- `endTrip` is called when the trip ends. It returns the resource if the agent must **take it along** to the next trip
  (e.g. the car is parked at work and must be driven home), or `null` if the resource is released.

While an agent holds a resource (`person.modeResource`), its next trip continues with that resource instead of a new
mode choice. This is how *home-based* vehicles work: once you leave home by car, you come back by car.

Available implementations:

| Resource | Used for | Behavior |
|---|---|---|
| `NoResourceMode(mode)` | pedestrian, passenger, public transport | Nothing to do. Taken along only if `mode.requiresVehicleTakeAlong` and the agent is not at home. |
| `BikeResource(mode)` | private bike | Taken along until the agent is back home. |
| `CarResource(mode, car)` | private car | Sets car state/driver/key holder; car is parked at the destination and taken along until back home. |
| `SharingStationResource(mode, station)` | station-based sharing | Takes a vehicle from the station; returned when back at the station's zone (or at home). |
| `SharingFreeResource(mode, start, end)` | free-floating / one-way sharing | Takes a vehicle at `start`, returns it at `end` after the trip. |
| `PoolingResource(mode, drtOffer)` | ride pooling (DRT) | Books the offered ride on `startTrip`. |

### ModeAvailabilityModel and AvailabilityRule

`ModeAvailabilityModel` is the interface the simulation works with. It has one method per stage, each taking the mode
as parameter:

```kotlin
interface ModeAvailabilityModel {
    fun staticAvailability(mode: Mode, person: IPerson): Boolean
    fun providerAvailability(mode: Mode, agent: PersonAgent, time: AbsoluteTime, destination: StandardLocation): ProviderAvailability
    fun resourceAvailability(mode: Mode, characteristics: ModeChoiceCharacteristics): ModeResource?

    fun asStaticAvailabilityFilter(): ChoiceFilter<Mode, IPerson>
    fun asProviderAvailabilityFilter(): ChoiceFilter<Mode, DestinationAlternative>
    fun asResourceAvailabilityFilter(): ChoiceFilter<Mode, ModeChoiceCharacteristics>
}
```

The `as...Filter()` functions convert the model into `ChoiceFilter`s that can be used directly in discrete choice models.

`AvailabilityRule` is the same three-stage interface, but for **one** mode. `ModeAvailabilityByRule` implements
`ModeAvailabilityModel` by looking up the rule of the requested mode (an array lookup by `mode.code`) and delegating to it.
Asking for a mode without a rule throws an `IllegalArgumentException`, so **every mode of the choice set needs a rule**.

### How the Stages are Used During Simulation

In the person state machine (see [state_machine_system.md](state_machine_system.md)), the `StartingTrip` state does:

1. If the agent still holds a mode resource (`person.modeResource`), it continues with it — no mode choice.
2. Otherwise:
   1. `providerAvailability` is computed for all modes; available modes form the choice set,
      and their providers are collected.
   2. All collected providers are locked (`synchronizeAll(providers) { ... }`).
   3. Inside the lock, `resourceAvailability` is computed for each mode with the `ModeChoiceCharacteristics`
      of the trip. Modes returning `null` are dropped.
   4. The mode choice model selects among the remaining modes, the chosen resource is stored in
      `person.modeResource` and `startTrip` is called.
3. When the trip ends, `endTrip` decides whether the resource is kept for the next trip.

If a DRT booking fails, the mode choice is repeated without ride pooling.

### Built-in Behavior of Every Rule

Rules created with the DSL (internally `DefaultRule`) add some checks around your lambdas:

- **Provider stage** first evaluates the static rule; if it fails, the mode is not available.
- **Provider stage** returns "not available" if the agent is currently fixed to a **different** mode,
  i.e. holds a `modeResource` of another mode (e.g. drove to work by car, so bike is not available).
- **Resource stage** returns `null` if the mode is not in `characteristics.currentChoices`.
- **Resource stage** returns `NoResourceMode(mode)` without evaluating your rule if only one mode is left in the
  choice set (forced mode).

So in your own rules you do not need to repeat the static conditions in the provider rule.

## The Availability Rule DSL

The DSL lives in `edu.kit.ifv.domain.simulation.behavior.availability.rules.builder`.

### Creating a Rule Set: availabilityRules { }

```kotlin
val model: ModeAvailabilityByRule = availabilityRules {
    // one rule per mode
}
```

Inside the block (`AvailabilityByRuleBuilder`) you define one rule per mode. Defining a second rule for the same mode
throws an exception.

### Defining a Rule: availabilityOf(mode)

A rule is defined as a chain of the three stages, always in this order:

```kotlin
availabilityOf(mode).staticRule {
    // StaticRuleScope -> Boolean
}.providerRule {
    // ProviderRuleScope -> ProviderAvailability
}.resourceRule {
    // ResourceRuleScope -> ModeResource?
}
```

The builder types (`StaticAvailabilityRuleBuilder` → `ProviderAvailabilityRuleBuilder` → `ResourceAvailabilityRuleBuilder`)
enforce the order at compile time. If a stage is missing, building the model fails with
`Cannot build availability rule for <mode> as no ... rule was defined!`.

Each lambda is executed with a scope as receiver, which provides the inputs of the stage and helper functions.

### Static Rule Scope

`StaticRuleScope` — return `true` if the person can generally use the mode.

| Member | Description |
|---|---|
| `mode` | The mode of this rule |
| `person` | The `IPerson` (e.g. `hasLicense`, `hasBike`, `household.cars`, `sharingMemberships`, `drtMemberships`, `age`, ...) |

```kotlin
staticRule { person.hasLicense && person.household.cars.isNotEmpty() }
```

### Provider Rule Scope

`ProviderRuleScope` — return a `ProviderAvailability`.

Inputs:

| Member | Description |
|---|---|
| `mode` | The mode of this rule |
| `agent` | The `PersonAgent` (location, household, `modeResource`, memberships, ...) |
| `time` | The time of the choice situation |
| `destination` | The potential destination |
| `sharingProviders` | Sharing providers of the agent's memberships for this `mode` |
| `drtProviders` | DRT providers of the agent's memberships for this `mode` |

Results:

| Function | Description |
|---|---|
| `available()` | Available, no shared provider |
| `available(vararg providers)` / `available(providers: Collection)` | Available, the given providers are affected |
| `notAvailable()` | Not available |
| `List<Any>.checkAnyAvailable()` | `available(list)` if the list is not empty, otherwise `notAvailable()` |

Helpers:

| Function | Description |
|---|---|
| `modeAlreadyInUse()` | The agent already holds a resource of this mode |
| `homeBasedVehicleRule(useProvider)` | Available if the agent is at home (with the household as provider if `useProvider = true`) or if the mode is already in use; otherwise not available |
| `List<SharingProviderAgent>.checkOperatingHours()` | Keep providers operating at `time.hour` |
| `List<SharingProviderAgent>.stations()` | All stations (or free-floating zones) of the providers |
| `List<SharingStationAgent>.checkAgentInFootZones()` | Keep stations whose walking zones contain the agent's location |
| `List<SharingStationAgent>.checkAgentInRadius(radius, pedestrian, impedance)` | Keep stations within `radius` walking distance of the agent |
| `List<SharingStationAgent>.checkDestinationInFootZones()` | Keep start stations for which another station of the same provider is within walking zones of the destination |
| `List<SharingStationAgent>.checkDestinationInRadius(radius, pedestrian, impedance)` | Same, using a walking distance `radius` |
| `List<DrtProviderAgent>.checkOperatingHoursAndArea()` | Keep DRT providers operating at `time` between the agent's location and `destination` |

The helpers are designed to be chained into a filter pipeline ending in `checkAnyAvailable()`:

```kotlin
providerRule {
    sharingProviders.checkOperatingHours()
        .stations()
        .checkAgentInFootZones()
        .checkAnyAvailable()   // the remaining stations become the affected providers
}
```

### Resource Rule Scope

`ResourceRuleScope` — return a `ModeResource`, or `null` if no resource is available.
It provides the same inputs and filter helpers as the provider scope (`mode`, `agent`, `time`, `destination`,
`sharingProviders`, `drtProviders`, `checkOperatingHours()`, `stations()`, `checkAgentInFootZones()`, ...),
taken from the `characteristics` of the mode choice situation, plus:

| Member | Description |
|---|---|
| `characteristics` | The `ModeChoiceCharacteristics` (person, time, origin, destination, `currentChoices`, ...) |
| `availableWithoutResource()` | Returns `NoResourceMode(mode)` |
| `resourceUnavailable` | `null`, for readability |
| `List<SharingStationAgent>.checkVehiclesAvailable()` | Keep stations with at least one available vehicle |
| `List<SharingStationAgent>.selectStationByMinDistance(pedestrian, impedance)` | `SharingStationResource` of the nearest station (walking distance) |
| `List<SharingStationAgent>.selectStationByMinTravelTime(pedestrian, impedance)` | `SharingStationResource` of the station with minimal walking time |
| `List<SharingStationAgent>.selectMinDistOneWaySharing(pedestrian, impedance)` | `SharingFreeResource` with the start/end station pair minimizing walk to start + ride + walk from end |
| `List<DrtProviderAgent>.selectRideOfferByMinDuration()` | Requests offers from all providers, keeps the fastest, revokes the others; returns a `PoolingResource` |

The resource rule is evaluated while the providers are locked, so it may inspect and reserve shared state.

### Shortcuts: simpleAvailabilityOf, default(), providersIf

- `simpleAvailabilityOf(mode)`: always available, no provider, no resource. Equivalent to
  ```kotlin
  availabilityOf(mode).staticRule { true }.providerRule { available() }.resourceRule { availableWithoutResource() }
  ```
- `default()` fills the current and all remaining stages with these defaults:
  ```kotlin
  availabilityOf(mode).default()                                // everything default
  availabilityOf(mode).staticRule { person.age >= 14 }.default() // provider + resource default
  ```
- `providersIf(vararg providers) { condition }` replaces `providerRule` when the result only depends on a boolean:
  the mode is available with the given providers if the condition is `true`, otherwise not available.
  ```kotlin
  availabilityOf(mode).staticRule { true }
      .providersIf { agent.isHome() }
      .resourceRule { availableWithoutResource() }
  ```

### Discovering the Scopes: help()

Calling `help()` on a builder prints the properties and functions of the **next** scope to the console:

```kotlin
availabilityOf(car).help()                           // prints StaticRuleScope
    .staticRule { ... }.help()                       // prints ProviderRuleScope
    .providerRule { ... }.help()                     // prints ResourceRuleScope
    .resourceRule { ... }
```

`help()` returns the builder, so it can stay in the chain while writing the rules and be removed afterwards.

### Predefined Rules

The framework provides the default rules as extension functions on `AvailabilityByRuleBuilder` (`rules/DefaultRules.kt`):

| Function | Mode type |
|---|---|
| `defaultCarAvailability(car)` | Private car (licence + household car, home-based, household as provider) |
| `defaultBikeRule(bike)` | Private bike (owns bike, home-based, no provider) |
| `defaultSharingStationRules(sharing, pedestrian, impedance)` | Station-based sharing |
| `defaultSharingFloatingRules(sharing, pedestrian, impedance)` | Free-floating / one-way sharing |
| `defaultDrtRules(drt)` | Ride pooling (DRT) |

They can be mixed with custom rules:

```kotlin
availabilityRules {
    simpleAvailabilityOf(pedestrian)
    defaultBikeRule(bike)
    defaultCarAvailability(car)
    availabilityOf(bikesharing).staticRule { ... }.providerRule { ... }.resourceRule { ... }
}
```

`defaultAvailabilityModel(...)` builds a complete model with default rules for pedestrian, bike, car, passenger,
public transport, both car sharing variants, one-way bike sharing, ride pooling and any number of always available
(`beamedModes`) modes.

## Integrating the Model into a Simulation

The simulation reads the model from the context property `modeAvailability`
(`HasModeAvailabilityModel` / `HasMutableModeAvailabilityModel`).

### Model Steps

`application.steps.model` provides two model steps (see [model_steps.md](model_steps.md)):

```kotlin
// framework defaults for the modes configured in the config (requires the impedance in the context)
loadAvailabilityModel()

// custom rules
loadCustomAvailabilityModel {
    simpleAvailabilityOf(pedestrian)
    defaultCarAvailability(car)
    // ...
}
```

### Setting the Context Property Directly

The tutorial defines its rules in `mode/ModeAvailabilityRules.kt` as a function returning a `ModeAvailabilityByRule`:

```kotlin
fun tutorialModeAvailabilityRules(impedance: () -> Impedance): ModeAvailabilityByRule = availabilityRules { ... }
```

The impedance is passed as a supplier (`() -> Impedance`) because it is loaded by a model step **after** the context
was created; the supplier is only evaluated when a resource rule is executed. In the context it can be used as:

```kotlin
override var modeAvailability: ModeAvailabilityModel = tutorialModeAvailabilityRules { impedance }
```

> **Note:** `MainShortTerm.kt` currently still uses a `PlaceholderProvider` rule (always available) for every mode.
> Also note that models which receive `modeAvailability` at construction time (e.g. the `WrappedDestinationChoice`)
> keep the instance they were given. If you replace `modeAvailability` later (e.g. via a model step), make sure these
> models are created afterwards or read the property lazily.

### Using Availability in Choice Models

Availability can be used as a filter or as a property in utility functions:

```kotlin
// filter in a choice model
modeAvailability.asResourceAvailabilityFilter().filter(mode)   // with ModeChoiceCharacteristics in context
modeAvailability.asProviderAvailabilityFilter().filter(mode)   // with DestinationAlternative in context
```

See `destination/DestinationChoiceProperties.kt` and `destination/LogsumProperties.kt` for availability properties
like `isCarAvailable` used in the logsum of the destination choice.

## Examples

All examples below are taken from `src/main/kotlin/mode/ModeAvailabilityRules.kt` or the framework defaults.

### Always Available Modes

Pedestrian, passenger and public transport need no vehicle and no provider. The public transport supply is described by
the impedance matrices only, the driver of a passenger trip is not modelled.

```kotlin
simpleAvailabilityOf(pedestrian)
simpleAvailabilityOf(passenger)
simpleAvailabilityOf(publictransport)
```

### Home-based Private Vehicles (Bike, Car)

Requires a bike. As a home-based mode, the agent must start the tour at home where the bike is.
Bikes are not shared within the household, so no provider is used. The `BikeResource` is taken along until the agent
returns home.

```kotlin
availabilityOf(bike).staticRule {
    person.hasBike
}.providerRule {
    homeBasedVehicleRule(useProvider = false)
}.resourceRule {
    BikeResource(bike)
}
```

The car requires a driving licence and a car in the household. The household is the provider (locked during the
choice, since household members compete for the cars), and the best available car of the household is used.
If no car is left, the resource rule returns `null` and car is removed from the choice set.

```kotlin
availabilityOf(car).staticRule {
    person.hasLicense && person.household.cars.isNotEmpty()
}.providerRule {
    homeBasedVehicleRule(useProvider = true)
}.resourceRule {
    characteristics.person.getBestCarOrNull()?.let { vehicle ->
        CarResource(car, vehicle)
    }
}
```

### Sharing Modes (Free-floating and Station-based)

One-way bike sharing requires a membership. A station (zone) of the provider must be within walking distance of the
agent **and** of the destination, and the start station needs an available bike. The vehicle is returned at the end
station.

```kotlin
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
```

Station-based sharing (return to the same station) only requires a station near the agent. The vehicle is taken along
until the agent is back at the station (or at home):

```kotlin
availabilityOf(carsharingStation).staticRule {
    person.sharingMemberships.any { it.mode == mode }
}.providerRule {
    sharingProviders.checkOperatingHours()
        .stations()
        .checkAgentInFootZones()
        .checkAnyAvailable()
}.resourceRule {
    sharingProviders.checkOperatingHours()
        .stations()
        .checkVehiclesAvailable()
        .checkAgentInFootZones()
        .selectStationByMinDistance(pedestrian, impedance)
}
```

### Ride Pooling (DRT)

Requires a DRT membership and a provider operating at the current time in the area between origin and destination.
The resource rule requests ride offers and keeps the fastest one.

```kotlin
availabilityOf(ridePooling).staticRule {
    person.drtMemberships.any { it.mode == mode }
}.providerRule {
    drtProviders.checkOperatingHoursAndArea()
        .checkAnyAvailable()
}.resourceRule {
    drtProviders.checkOperatingHoursAndArea()
        .selectRideOfferByMinDuration()
}
```

### Custom Rules

Rules are ordinary Kotlin lambdas, so any condition can be expressed. Some ideas:

```kotlin
// Bike only for persons of at least 10 years, otherwise like the default rule
availabilityOf(bike).staticRule {
    person.hasBike && person.age >= 10
}.providerRule {
    homeBasedVehicleRule(useProvider = false)
}.resourceRule {
    BikeResource(bike)
}

// Bike sharing with a fixed walking radius instead of foot zones
availabilityOf(bikesharing).staticRule {
    person.sharingMemberships.any { it.mode == mode }
}.providerRule {
    sharingProviders.checkOperatingHours()
        .stations()
        .checkAgentInRadius(500.meters, pedestrian, impedance())
        .checkDestinationInRadius(500.meters, pedestrian, impedance())
        .checkAnyAvailable()
}.resourceRule {
    sharingProviders.checkOperatingHours()
        .stations()
        .checkVehiclesAvailable()
        .checkAgentInRadius(500.meters, pedestrian, impedance())
        .selectMinDistOneWaySharing(pedestrian, impedance())
}

// Public transport only during service hours
availabilityOf(publictransport).staticRule {
    true
}.providersIf {
    time.hour in 5..23
}.resourceRule {
    availableWithoutResource()
}
```

### Implementing AvailabilityRule Directly

For full control, `AvailabilityRule` can be implemented as a class and put into a `ModeAvailabilityByRule` map.
This is what `mode/PlaceholderProvider.kt` does to make every mode available:

```kotlin
class PlaceholderProvider(override val mode: Mode) : AvailabilityRule {
    override fun staticAvailability(person: IPerson) = true

    override fun providerAvailability(agent: PersonAgent, time: AbsoluteTime, destination: StandardLocation) =
        ProviderAvailability(mode = mode, providers = listOf("me :)"))

    override fun resourceAvailability(characteristics: ModeChoiceCharacteristics): ModeResource? =
        NoResourceMode(mode)
}

val model = ModeAvailabilityByRule(TutorialMode.values().associateWith { PlaceholderProvider(it) })
```

Note that a directly implemented rule does **not** get the [built-in behavior](#built-in-behavior-of-every-rule)
of DSL rules (static check in the provider stage, fixed mode check, `currentChoices` check).

## Reference

| Type / Function | Package (`edu.kit.ifv.domain.simulation.behavior.availability...`) |
|---|---|
| `ModeAvailabilityModel` | `.ModeAvailabilityModel` |
| `ProviderAvailability`, `available`, `notAvailable`, `currentlyAffectedProviders` | `.ProviderAvailability` |
| `ModeResource`, `NoResourceMode`, `BikeResource`, `CarResource`, `SharingStationResource`, `SharingFreeResource`, `PoolingResource` | `.ModeResource` |
| `defaultAvailabilityModel` | `.DefaultAvailabilityModel` |
| `AvailabilityRule` | `.rules.AvailabilityRule` |
| `ModeAvailabilityByRule` | `.rules.ModeAvailabilityByRule` |
| `defaultCarAvailability`, `defaultBikeRule`, `defaultSharingStationRules`, `defaultSharingFloatingRules`, `defaultDrtRules` | `.rules.DefaultRules` |
| `availabilityRules`, `AvailabilityByRuleBuilder`, `simpleAvailabilityOf` | `.rules.builder.AvailabilityModelBuilder` |
| `staticRule`, `providerRule`, `providersIf`, `resourceRule`, `default` | `.rules.builder.RuleBuilder` |
| `StaticRuleScope`, `ProviderRuleScope`, `ResourceRuleScope` | `.rules.builder.RuleScope` |
| `help` | `.rules.builder.ScopeHelp` |
| `loadAvailabilityModel`, `loadCustomAvailabilityModel` | `edu.kit.ifv.application.steps.model.LoadAvailabilityModel` |

---

Tips:
- Every mode in the choice set needs a rule, otherwise the lookup fails at runtime.
- Keep the provider rule cheap: it is evaluated for every mode (and possibly for many destinations).
  Expensive work like selecting stations or requesting ride offers belongs into the resource rule.
- Return all shared providers your resource rule touches from the provider rule, so they are locked
  during the resource check in parallel simulations.
- Pass impedances and other late-loaded data as suppliers (`() -> Impedance`) into the rule definition.
