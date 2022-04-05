package com.github.martonr.picalc.engine.calculators;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TestBanzhaf {

    private final int quota = 19;
    private final int[] votes = new int[] {3, 5, 8, 8, 12};
    private final double[] expected =
            new double[] {0.076923, 0.076923, 0.230769, 0.230769, 0.384615};
    private final double[] banzhaf = new double[votes.length];

    @Test
    void calculateBanzhaf() {
        CalculatorParameters params = new CalculatorParameters();
        params.votes = votes;
        params.quota = quota;

        int n = votes.length;
        params.n = n;
        CalculatorBanzhaf calculator = new CalculatorBanzhaf(n);
        long start = System.nanoTime();

        calculator.calculate(params, banzhaf);
        for (int i = 0; i < votes.length; i++) {
            System.out.println(votes[i] + " -> " + banzhaf[i] + " | " + expected[i]);
        }
        System.out.println();

        long elapsed = System.nanoTime() - start;
        System.out.println("Calculated in " + elapsed / 1000L + " us");

        Assertions.assertArrayEquals(expected, banzhaf, 0.00001);
    }

    // This test is for manual testing of specific scenarios
    @Test
    void testCustomBanzhaf() {
        final int quota = 10;
        final int[] votes = new int[] {3, 4, 5, 6};
        final double[] banzhaf = new double[votes.length];

        CalculatorParameters params = new CalculatorParameters();
        params.votes = votes;
        params.quota = quota;

        int n = votes.length;
        params.n = n;
        CalculatorBanzhaf calculator = new CalculatorBanzhaf(n);
        long start = System.nanoTime();

        calculator.calculate(params, banzhaf);
        for (int i = 0; i < votes.length; i++) {
            System.out.println(votes[i] + " -> " + banzhaf[i]);
        }
        System.out.println();

        long elapsed = System.nanoTime() - start;
        System.out.println("Calculated in " + elapsed / 1000L + " us");
    }
}
