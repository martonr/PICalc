package com.github.martonr.picalc.engine.calculators;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TestShapleyMC {

    private final int quota = 14;
    private final int count = 1000000;
    private final int[] votes = new int[] {3, 5, 8, 8, 12};
    private final double[] expected = new double[] {0.11667, 0.11667, 0.20, 0.20, 0.36667};
    private final double[] shapley = new double[votes.length];

    @Test
    void estimateShapley() {
        CalculatorParameters params = new CalculatorParameters();
        int n = votes.length;
        params.n = n;
        params.votes = votes;
        params.quota = quota;
        params.monteCarloCount = count;


        CalculatorShapley calculator = new CalculatorShapley(n);
        long start = System.nanoTime();

        calculator.calculateMC(params, shapley, params.monteCarloCount);
        normalizeSS(shapley, params.monteCarloCount);

        for (int i = 0; i < votes.length; i++) {
            System.out.println(votes[i] + " -> " + shapley[i] + " | " + expected[i]);
        }
        System.out.println();

        long elapsed = System.nanoTime() - start;
        System.out.println("Calculated in " + elapsed / 1000L + " us");

        Assertions.assertArrayEquals(expected, shapley, 0.01);
    }

    void normalizeSS(double[] values, long emc) {
        for (int i = 0; i < values.length; ++i)
            values[i] /= emc;
    }
}
