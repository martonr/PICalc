package com.github.martonr.picalc.engine.calculators;

import com.github.martonr.picalc.engine.generators.GeneratorCombination;
import com.github.martonr.picalc.engine.generators.GeneratorCombinationRandom;
import com.github.martonr.picalc.engine.generators.GeneratorNTuple;
import com.github.martonr.picalc.engine.generators.GeneratorNTupleRandom;

public final class CalculatorDPI {

    private final double[] zeros;

    private final int[] ones;

    private final int n;

    private final int[] complement;
    private final int[] helper;

    private final GeneratorCombination generator;

    private final GeneratorNTuple generatorScenario;

    private final GeneratorCombinationRandom generatorRandom;

    private final GeneratorNTupleRandom generatorScenarioRandom;

    public CalculatorDPI(int n) {
        // The N-tuple generation goes through (n - k)^k from k=(0 to n) number of
        // possible tuples
        // This can get huge pretty fast, with 20 players, a 7 sized coalition with 13
        // opponents
        // has 7^13 possible tuples, 96,889,010,407, more than a 32-bit can hold...

        // The complement array is determined via bit counting with shifts,
        // so >64 players have to be handled differently if needed
        this.n = n;
        this.zeros = new double[n];
        this.ones = new int[n];
        for (int i = 0; i < n; ++i)
            this.ones[i] = 1;
        this.complement = new int[n];
        this.helper = new int[n];
        this.generator = new GeneratorCombination(n);
        this.generatorScenario = new GeneratorNTuple(n);
        this.generatorRandom = new GeneratorCombinationRandom(n);
        this.generatorScenarioRandom = new GeneratorNTupleRandom(n);
    }

    public static final void normalizeDPI(double[] values) {
        double s = 0;

        for (int i = 0; i < values.length; ++i)
            s += values[i];

        // if (s < 1)
        // s = 1;

        for (int i = 0; i < values.length; ++i)
            values[i] /= s;
    }

    public final void calculate(CalculatorParameters params, double[] results) {
        int combinationSize, complementSize;
        int[] combination, scenario;
        double[] weights = params.weights;

        double coalition, denominator, w;

        System.arraycopy(zeros, 0, results, 0, n);

        // For every possible coalition combination of players
        while (generator.hasNext) {
            combination = generator.next();

            // Determine the opposition size
            combinationSize = generator.m;
            complementSize = n - combinationSize;

            System.arraycopy(ones, 0, helper, 0, n);
            for (int i = 0; i < combinationSize; ++i)
                helper[combination[i]] = 0;

            int t = 0, s;
            for (int i = 0; i < n; ++i) {
                s = helper[i];
                complement[t] = i * s;
                t += s;
            }

            // // Find the complement combination
            // long cmp = 0;
            // // Flip bits for each participant
            // for (int i = 0; i < combinationSize; ++i)
            // cmp |= (1 << combination[i]);

            // // Negate all bits
            // cmp = ~cmp;

            // // Test the bits and record players
            // int t = 0, s;
            // for (int i = 0; i < n; ++i) {
            // s = (int) (cmp & 1);
            // complement[t] = i * s;
            // t += s;
            // cmp = cmp >>> 1;
            // }

            // For all possible "convincing" scenarios
            // Ex. for an AB - CDE player situation:
            // AAA, AAB, ABA, BAA, ABB, BBA, BBB, BAB
            // These are scenarios that could happen based on who "convinces" who
            generatorScenario.initialize(complementSize, combinationSize);

            while (generatorScenario.hasNext) {
                if (Thread.currentThread().isInterrupted())
                    return;
                scenario = generatorScenario.next();
                coalition = 1;

                // DPI if two players:
                // A / (A + B) where A is in coalition, B is an opponent
                for (int i = 0; i < complementSize; ++i) {
                    w = weights[combination[scenario[i]]];
                    denominator = w + weights[complement[i]];
                    if (denominator == 0)
                        denominator = 1;
                    coalition *= (w / denominator);
                }

                // For now just add the final coalition "success" chance to each player
                // for (int i = 1; i <= combinationSize; ++i) dpi[combination[i]] += coalition;

                // 2nd version, add the success chance only to the LAST player in the scenario
                // dpi[combination[scenario[complementSize - 1]]] += coalition;

                // 3rd version add it to every scenario member
                // kind of representing the "work" they completed convincing opponents
                for (int i = 0; i < complementSize; ++i) {
                    results[combination[scenario[i]]] += coalition;
                }
            }

            // Problem 1:
            // Do we need to consider every order an AAA - CDE convincing can happen?
            // Do we need to calculate the same value multiple times to get AAA - CDE, AAA -
            // DCE, AAA - CED too?
        }

        this.generator.reset();

        // Problem 2:
        // What should the final DPI value be?
        // Currently it is a ratio of the summed probabilities of all players

        // This is like the Banzhaf normalization
        double s = 0;
        for (int i = 0; i < n; ++i)
            s += results[i];

        // if (s < 1)
        // s = 1;

        for (int i = 0; i < n; ++i)
            results[i] /= s;

        // Normalize with all counts ?
    }

    public final void calculateMC(CalculatorParameters params, double[] results, long mc) {
        int combinationSize, complementSize;
        int[] combination, scenario;
        double[] weights = params.weights;

        // Divide the simulation between combination and complement MC draws
        // long count = ((long) Math.sqrt(mc)) * n;
        // long countScenario = ((long) (count / (n * n)));
        long count = (long) Math.sqrt(mc);
        long countScenario = count;

        double coalition, denominator, w;

        System.arraycopy(zeros, 0, results, 0, n);

        // For every possible coalition combination of players
        for (long k = 0; k < count; ++k) {
            combination = generatorRandom.next();

            // Determine the opposition size
            combinationSize = generatorRandom.m;
            complementSize = n - combinationSize;

            System.arraycopy(ones, 0, helper, 0, n);
            for (int i = 0; i < combinationSize; ++i)
                helper[combination[i]] = 0;

            int t = 0, s;
            for (int i = 0; i < n; ++i) {
                s = helper[i];
                complement[t] = i * s;
                t += s;
            }

            // // Find the complement combination
            // long cmp = 0;
            // // Flip bits for each participant
            // for (int i = 0; i < combinationSize; ++i)
            // cmp |= (1 << combination[i]);

            // // Negate all bits
            // cmp = ~cmp;

            // // Test the bits and record players
            // int t = 0, s;
            // for (int i = 0; i < n; ++i) {
            // s = (int) (cmp & 1);
            // complement[t] = i * s;
            // t += s;
            // cmp >>>= 1;
            // }

            // For all possible "convincing" scenarios
            // Ex. for an AB - CDE player situation:
            // AAA, AAB, ABA, BAA, ABB, BBA, BBB, BAB
            // These are scenarios that could happen based on who "convinces" who
            generatorScenarioRandom.initialize(complementSize, combinationSize);

            for (long j = 0; j < countScenario; ++j) {
                if (Thread.currentThread().isInterrupted())
                    return;
                scenario = generatorScenarioRandom.next();
                coalition = 1;

                // DPI if two players:
                // A / (A + B) where A is in coalition, B is an opponent
                for (int i = 0; i < complementSize; ++i) {
                    w = weights[combination[scenario[i]]];
                    denominator = w + weights[complement[i]];
                    if (denominator == 0)
                        denominator = 1;
                    coalition *= (w / denominator);
                }

                // For now just add the final coalition "success" chance to each player
                // for (int i = 1; i <= combinationSize; ++i) dpi[combination[i]] += coalition;

                // 2nd version, add the success chance only to the LAST player in the scenario
                // dpi[combination[scenario[complementSize - 1] + 1]] += coalition;

                // 3rd version add it to every scenario member
                // kind of representing the "work" they completed convincing opponents
                for (int i = 0; i < complementSize; ++i) {
                    results[combination[scenario[i]]] += coalition;
                }
            }
        }
        // Calculate final index values outside
    }
}
