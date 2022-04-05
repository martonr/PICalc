package com.github.martonr.picalc.engine.calculators;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TestDPI {

    private final double[] weights = new double[] {0, 3, 5, 8, 8, 12, 13, 17};
    private final double[] expected =
            new double[] {0, 0.05585, 0.08838, 0.13158, 0.13158, 0.17886, 0.18907, 0.22468};
    private final double[] dpiIndex = new double[weights.length];

    @Test
    void testDPICalculation() {
        CalculatorParameters params = new CalculatorParameters();
        int n = weights.length;
        params.n = n;
        params.weights = weights;

        CalculatorDPI calculator = new CalculatorDPI(n);
        long start = System.nanoTime();

        calculator.calculate(params, dpiIndex);
        for (int i = 0; i < weights.length; i++) {
            System.out.println(weights[i] + " -> " + dpiIndex[i] + " | " + expected[i]);
        }
        System.out.println();

        long elapsed = System.nanoTime() - start;
        System.out.println("Calculated in " + elapsed / 1000L + " us");

        Assertions.assertArrayEquals(expected, dpiIndex, 0.001);
    }

    @Test
    void testComplementAlgorithm() {
        int n = 7;
        int[] ones = new int[] {1, 1, 1, 1, 1, 1, 1};
        int[] helper = new int[n];
        int[] combination = new int[] {1, 4, 6, 0, 0, 0, 0};
        int[] expected = new int[] {0, 2, 3, 5, 0, 0, 0};
        int[] complement = new int[] {0, 0, 0, 0, 0, 0, 0};

        int combinationSize = 3;
        int complementSize = n - combinationSize;

        System.arraycopy(ones, 0, helper, 0, n);
        for (int i = 0; i < combinationSize; ++i)
            helper[combination[i]] = 0;

        int t = 0, s;
        for (int i = 0; i < n; ++i) {
            s = helper[i];
            complement[t] = i * s;
            t += s;
        }

        // long cmp = 0;
        // for (int i = 0; i < combinationSize; ++i)
        // cmp |= (1 << combination[i + 1]);

        // cmp = ~cmp;

        // int j = 1, s;
        // for (int i = 0; i < n; ++i) {
        // s = (int) (cmp & 1);
        // complement[j] = i * s;
        // j += s;
        // cmp >>>= 1;
        // }

        System.out.println("The combination was: ");
        for (int i = 0; i < combinationSize; ++i) {
            System.out.print(combination[i]);
        }

        System.out.println();

        System.out.println("The complement was: ");
        for (int i = 0; i < complementSize; ++i) {
            System.out.print(complement[i]);
        }

        Assertions.assertArrayEquals(expected, complement);
    }

}
