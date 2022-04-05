package com.github.martonr.picalc.engine.calculators;

import com.github.martonr.picalc.engine.generators.GeneratorCombination;
import com.github.martonr.picalc.engine.generators.GeneratorCombinationRandom;

public final class CalculatorBanzhaf {

    // private static final long[] POW_2N = {1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048,
    // 4096, 8192, 16384, 32768, 65536, 131072, 262144, 524288, 1048576, 2097152, 4194304};

    private final double[] zeros;

    private final int n;

    private final GeneratorCombination generator;

    private final GeneratorCombinationRandom generatorRandom;

    public CalculatorBanzhaf(int n) {
        this.n = n;
        this.zeros = new double[n];
        this.generator = new GeneratorCombination(n);
        this.generatorRandom = new GeneratorCombinationRandom(n);
    }

    public static final void normalizeBF(double[] values) {
        double s = 0;

        for (int i = 0; i < values.length; ++i)
            s += values[i];

        if (s < 1)
            s = 1;

        for (int i = 0; i < values.length; ++i)
            values[i] /= s;
    }

    public final void calculate(CalculatorParameters params, double[] results) {
        int sum, m, player, quota = params.quota;
        int[] votes = params.votes, combination;

        System.arraycopy(zeros, 0, results, 0, n);

        while (generator.hasNext) {
            if (Thread.currentThread().isInterrupted())
                return;
            combination = generator.next();
            m = generator.m;
            sum = 0;

            // For each combination add up the votes
            for (int i = 0; i < m; ++i) {
                player = combination[i];
                sum += votes[player];
            }

            // If the combination reaches the quota
            if (sum >= quota) {
                // Check which players are critical for this combination
                // A player is critical if without their vote
                // the combination does not meet the quota
                for (int i = 0; i < m; ++i) {
                    player = combination[i];
                    if (sum - votes[player] < quota) {
                        results[player] += 1;
                    }
                }
            }
        }

        this.generator.reset();

        // Normalize the counts
        // This is the original Banzhaf-index which is normalized with the total of critical counts
        double total = 0;
        for (int i = 0; i < n; ++i)
            total += results[i];

        // Guard against the case when nobody is critical (quota is more than total votes)
        if (total == 0)
            total = 1;

        for (int i = 0; i < n; ++i)
            results[i] /= total;

        // This is the modified version from
        // "Mathematical properties of the Banzhaf power index" by Dubey & Shapley, 1979
        // It normalizes with 2^(n - 1)
        // This however does not add up to 1, and the sum here represents
        // "the expected number of pivot players in a combination"
        // for (int i = 0; i < n; ++i) banzhaf[i] /= POW_2N[n - 1];
    }

    public final void calculateMC(CalculatorParameters params, double[] results, long mc) {
        int sum, m, player, quota = params.quota;
        int[] votes = params.votes, combination;

        System.arraycopy(zeros, 0, results, 0, n);

        for (long j = 0; j < mc; ++j) {
            if (Thread.currentThread().isInterrupted())
                return;
            combination = generatorRandom.next();
            m = generatorRandom.m;
            sum = 0;

            for (int i = 0; i < m; ++i) {
                player = combination[i];
                sum += votes[player];
            }

            if (sum >= quota) {
                for (int i = 0; i < m; ++i) {
                    player = combination[i];
                    if (sum - votes[player] < quota) {
                        results[player] += 1;
                    }
                }
            }
        }

        // Calculate final index values outside
    }
}
