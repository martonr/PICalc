package com.github.martonr.picalc.engine.calculators;

import com.github.martonr.picalc.engine.generators.GeneratorPermutation;
import com.github.martonr.picalc.engine.generators.GeneratorPermutationRandom;

public final class CalculatorShapley {

    private static final long[] FACTORIAL =
            {1L, 1L, 2L, 6L, 24L, 120L, 720L, 5040L, 40320L, 362880L, 3628800L, 39916800L,
                    479001600L, 6227020800L, 87178291200L, 1307674368000L, 20922789888000L,
                    355687428096000L, 6402373705728000L, 121645100408832000L, 2432902008176640000L};

    private final double[] zeros;

    private final int n;

    private final GeneratorPermutation generator;

    private final GeneratorPermutationRandom generatorRandom;

    public CalculatorShapley(int n) {
        this.n = n;
        this.zeros = new double[n];
        this.generator = new GeneratorPermutation(n);
        this.generatorRandom = new GeneratorPermutationRandom(n);
    }

    public static final void normalizeSS(double[] values, long emc) {
        for (int i = 0; i < values.length; ++i)
            values[i] /= emc;
    }

    public final void calculate(CalculatorParameters params, double[] results) {
        int sum, player, quota = params.quota;
        int[] votes = params.votes, permutation;

        System.arraycopy(zeros, 0, results, 0, n);

        while (generator.hasNext) {
            if (Thread.currentThread().isInterrupted())
                return;
            permutation = generator.next();
            sum = 0;

            // For each permutation of the players add up the votes
            // If a player's votes increases the total above the quota, player is critical
            for (int i = 0; i < n; ++i) {
                player = permutation[i];
                sum += votes[player];

                if (sum >= quota) {
                    results[player] += 1;
                    break;
                }
            }
        }

        this.generator.reset();

        // Divide the critical count by the number of permutations
        for (int i = 0; i < n; ++i)
            results[i] /= FACTORIAL[n];
    }

    public final void calculateMC(CalculatorParameters params, double[] results, long mc) {
        int sum, player, quota = params.quota;
        int[] votes = params.votes, permutation;

        System.arraycopy(zeros, 0, results, 0, n);

        for (long j = 0; j < mc; ++j) {
            if (Thread.currentThread().isInterrupted())
                return;
            permutation = generatorRandom.next();
            sum = 0;

            // For each permutation of the players add up the votes
            // If a player's votes increases the total above the quota, player is critical
            for (int i = 0; i < n; ++i) {
                player = permutation[i];
                sum += votes[player];

                if (sum >= quota) {
                    results[player] += 1;
                    break;
                }
            }
        }

        // Calculate final index values outside
    }
}
