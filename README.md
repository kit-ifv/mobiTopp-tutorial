# mobitopp Tutorial Project

This project is a tutorial for **mobiTopp**, a travel demand modeling framework. It demonstrates how to set up and run both long-term population synthesis and short-term travel demand simulations.

## Project Structure

- `src/main/kotlin/MainLongTerm.kt`: Entry point for population synthesis and long-term choices (e.g., car ownership, transit card ownership).
- `src/main/kotlin/MainShortTerm.kt`: Entry point for the short-term demand simulation (mode and destination choice).
- `data/`: Contains input data such as network matrices, zone information, and configurations.
- `results/`: Directory where simulation results and plots are stored.

## Prerequisites

- **Java JDK 25**: This project is configured to use Java 25.
- **Gradle**: The project uses the Gradle build system (wrapper included).

## Build and Compile

To compile the project without running it:

```bash
./gradlew build
```

## Running the Simulation

You can run the simulation components using the following Gradle commands.

### 1. Long-Term Simulation (Population Synthesis)
This step generates the synthetic population and assigns long-term characteristics.

```bash
./gradlew runLongTerm
```

### 2. Short-Term Simulation (Demand Simulation)
This step simulates the daily travel behavior (trips, mode choice, destination choice) for the generated population.

```bash
./gradlew runShortTerm
```

## Results

After running the simulations, you can find the results in the `results/` directory:
- **Long-term results**: Located in `results/long-term/` and `results/long-term-modernized/`.
- **Short-term results**: Located in `results/short-term/`. The simulated trips can be found in: `results/short-term/demandsimulation.csv`.


## Docs
The following documentation covers some topics in more detail:
- [Model Steps](docs/model_steps.md)
- [State Machines](docs/state_machine_system.md)
- [CSV Parsing](docs/csv_parsing.md)
- [Plots](docs/plots_system.md)

