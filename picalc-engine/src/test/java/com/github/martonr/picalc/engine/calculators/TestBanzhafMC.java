package com.github.martonr.picalc.engine.calculators;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TestBanzhafMC {

    private final int quota = 19;
    private final int count = 1000000;
    private final int[] votes = new int[] {3, 5, 8, 8, 12};
    private final double[] expected =
            new double[] {0.076923, 0.076923, 0.230769, 0.230769, 0.384615};
    private final double[] banzhaf = new double[votes.length];

    @Test
    void estimateBanzhaf() {
        CalculatorParameters params = new CalculatorParameters();
        int n = votes.length;
        params.n = n;
        params.votes = votes;
        params.quota = quota;
        params.monteCarloCount = count;

        CalculatorBanzhaf calculator = new CalculatorBanzhaf(n);
        long start = System.nanoTime();

        calculator.calculateMC(params, banzhaf, params.monteCarloCount);
        normalizeBF(banzhaf);

        for (int i = 0; i < votes.length; i++) {
            System.out.println(votes[i] + " -> " + banzhaf[i] + " | " + expected[i]);
        }
        System.out.println();

        long elapsed = System.nanoTime() - start;
        System.out.println("Calculated in " + elapsed / 1000L + " us");

        Assertions.assertArrayEquals(expected, banzhaf, 0.01);
    }

    @Test
    void estimateCustomBanzhaf() {
        final int quota = 10;
        final int[] votes = new int[] {3, 4, 5, 6};
        final double[] banzhaf = new double[votes.length];
        final double[] expected = new double[] {0.0833, 0.25, 0.25, 0.4166};

        CalculatorParameters params = new CalculatorParameters();
        int n = votes.length;
        params.n = n;
        params.votes = votes;
        params.quota = quota;
        params.monteCarloCount = count;

        CalculatorBanzhaf calculator = new CalculatorBanzhaf(n);
        calculator.calculateMC(params, banzhaf, params.monteCarloCount);
        normalizeBF(banzhaf);

        for (int i = 0; i < votes.length; i++) {
            System.out.println(votes[i] + " -> " + banzhaf[i] + " | " + expected[i]);
        }
        System.out.println();
    }

    void normalizeBF(double[] values) {
        double s = 0;

        for (int i = 0; i < values.length; ++i)
            s += values[i];

        s = s < 1 ? 1 : s;

        for (int i = 0; i < values.length; ++i)
            values[i] /= s;
    }
}
