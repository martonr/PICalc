# PICalc - a power-index calculator

PICalc is a small application with a simple GUI that can be used to
calculate the [Shapley-Shubik](https://en.wikipedia.org/wiki/Shapley%E2%80%93Shubik_power_index)
and the [Banzhaf](https://en.wikipedia.org/wiki/Banzhaf_power_index) power indices.
You can calculate the indices using the exact classical definitions or use a
[Monte-Carlo method](https://en.wikipedia.org/wiki/Monte_Carlo_method) to determine the values.

Using the latter is necessary for large number of participants as calculating the Shapley-Shubik
index takes a number of steps proportional to the factorial of the number of participants.

In addition the application has a simulation mode, where you can simulate the effect of
changing the passing vote or quota. The simulation assumes that every vote distribution is equally
likely, generates scenarios and calculates the difference in the Shapley-Shubik and Banzhaf indices.

The created plots show how likely is that the vote change results in positive, negative or no change
in the power index of a player having a specific vote count.

## Build

To build the application, you need Gradle 2.12 and JDK 8. Then go into the directory where the
build.gradle file is and execute:
> gradle jar

## Run

To run the application you need Java 8 installed, then if you have java.exe in your PATH variable,
double-clicking on the .jar file will start the application.