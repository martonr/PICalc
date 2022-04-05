package com.github.martonr.picalc.engine.calculators;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TestShapley {

    private final int quota = 14;
    private final int[] votes = new int[] {3, 5, 8, 8, 12};
    private final double[] expected = new double[] {0.11667, 0.11667, 0.20, 0.20, 0.36667};
    private final double[] shapley = new double[votes.length];

    @Test
    void calculateShapley() {
        CalculatorParameters params = new CalculatorParameters();
        int n = votes.length;
        params.n = n;
        params.votes = votes;
        params.quota = quota;

        CalculatorShapley calculator = new CalculatorShapley(n);
        long start = System.nanoTime();

        calculator.calculate(params, shapley);
        for (int i = 0; i < votes.length; i++) {
            System.out.println(votes[i] + " -> " + shapley[i] + " | " + expected[i]);
        }
        System.out.println();

        long elapsed = System.nanoTime() - start;
        System.out.println("Calculated in " + elapsed / 1000L + " us");

        Assertions.assertArrayEquals(expected, shapley, 0.00001);
    }

    // This test is for manual testing of specific scenarios
    // @Test
    void testCustomShapley() {
        final int quota = 67;
        final int[] votes = new int[] {67, 2, 1, 2, 28};
        final double[] shapley = new double[votes.length];

        CalculatorParameters params = new CalculatorParameters();
        int n = votes.length;
        params.n = n;
        params.votes = votes;
        params.quota = quota;

        CalculatorShapley calculator = new CalculatorShapley(n);
        long start = System.nanoTime();

        calculator.calculate(params, shapley);
        for (int i = 0; i < votes.length; i++) {
            System.out.println(votes[i] + " -> " + shapley[i] + " | " + expected[i]);
        }
        System.out.println();

        long elapsed = System.nanoTime() - start;
        System.out.println("Calculated in " + elapsed / 1000L + " us");
    }
}
