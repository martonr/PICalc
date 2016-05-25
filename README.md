# PICalc - a power-index calculator

PICalc is a small application with a simple JavaFX GUI that can be used to
calculate the [Shapley-Shubik](https://en.wikipedia.org/wiki/Shapley%E2%80%93Shubik_power_index)
and the [Banzhaf](https://en.wikipedia.org/wiki/Banzhaf_power_index) power indices.
You can calculate the indices using the classical definitions or use a
[Monte-Carlo method](https://en.wikipedia.org/wiki/Monte_Carlo_method) to estimate the values.

![Classic power index calculator](https://github.com/martonr/PICalc/blob/master/img/pI_calc.png)

Using the latter is essential for large number of participants as calculating the Shapley-Shubik
index takes a number of steps proportional to the factorial of the number of participants.

The application also has a simulation mode, where you can simulate the effect of
change in the "passing vote count" or quota. The simulation assumes that every vote distribution is equally
likely, generates scenarios and calculates the difference in the Shapley-Shubik and Banzhaf indices.

![Simulation](https://github.com/martonr/PICalc/blob/master/img/pI_sim.png)

The created plots show how likely is that the vote change results in positive, negative or no change
in the power index of a player having a specific vote count.

## Build

Use Gradle and JDK 8 for building (JDK 8 required for lambda support). Go into the directory where
the build.gradle file is and execute:
> gradle jar

Already built versions are provided in the Releases section.

## Run

To run the application you need Java 8 installed for the full use of JavaFX, then if you
have java.exe in your PATH variable, double-clicking on the .jar file will start the application.