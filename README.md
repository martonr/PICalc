# PICalc

### Power index calculator

PICalc is a small application with a simple JavaFX GUI that can be used to
calculate the [Shapley-Shubik](https://en.wikipedia.org/wiki/Shapley%E2%80%93Shubik_power_index)
and the [Banzhaf](https://en.wikipedia.org/wiki/Banzhaf_power_index) power indices for voting games.
You can calculate the indices using the classical definitions or use a
[Monte-Carlo method](https://en.wikipedia.org/wiki/Monte_Carlo_method) to estimate the values.

![Classic power index calculator](https://github.com/martonr/PICalc/blob/main/img/picalc_main.png)

Using the latter is essential for large number of participants as calculating the Shapley-Shubik
index can become computationally expensive as the number of players increase.

### Quota change simulation

In quota simulation mode, you can simulate the effect of a change in the "passing vote count" or quota
on the power index for a given vote count.
The simulation assumes that every vote distribution is equally likely, generates random scenarios and
calculates the difference in the Shapley-Shubik and Banzhaf indices.

![Simulation](https://github.com/martonr/PICalc/blob/main/img/picalc_sim.png)

The created plots show how likely is that the vote change results in positive, negative or zero change
in the power index of a player having a specific vote count.

## Build

The project is structured as separate modules which can be built with Maven.

It takes advantage of Java's modularity to create small-sized all-inclusive distributions.

From the root directory running

> mvn package

should build all sub-modules and collect all artifacts in the picalc-distribution module.
These can be added to the Java module path.

A batch script is available to create a slimmed down machine specific JVM distribution with the modules included.

Pre-built versions are provided in the Releases section.

## Run

Running the application is possible via the batch file provided in the pre-built version,
or running Java and specifying the module name, for example:

> java -m com.github.martonr.picalc.gui
