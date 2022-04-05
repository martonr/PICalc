package com.github.martonr.picalc.engine.calculators;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TestDPIMC {

    private final int count = 10000000;
    private final double[] weights = new double[] {0, 3, 5, 8, 8, 12, 13, 17};
    private final double[] expected =
            new double[] {0, 0.05585, 0.08838, 0.13158, 0.13158, 0.17886, 0.18907, 0.22468};
    private final double[] dpi = new double[weights.length];

    @Test
    void estimateDPI() {
        CalculatorParameters params = new CalculatorParameters();
        int n = weights.length;
        params.n = n;
        params.weights = weights;
        params.monteCarloCount = count;

        CalculatorDPI calculator = new CalculatorDPI(n);
        long start = System.nanoTime();

        calculator.calculateMC(params, dpi, params.monteCarloCount);
        normalizeBF(dpi);

        double total = 0;
        for (int i = 0; i < weights.length; ++i) {
            total += dpi[i];
        }

        if (total < 1)
            total = 1;

        for (int i = 0; i < dpi.length; ++i) {
            dpi[i] /= total;
        }

        for (int i = 0; i < dpi.length; i++) {
            System.out.println(weights[i] + " -> " + dpi[i] + " | " + expected[i]);
        }
        System.out.println();

        long elapsed = System.nanoTime() - start;
        System.out.println("Calculated in " + elapsed / 1000L + " us");

        Assertions.assertArrayEquals(expected, dpi, 0.05);
    }

    @Test
    void testRatioComputation() {
        int n = 4;
        int combinationSize = 3;

        double ratio = 1;
        int cnt = n - combinationSize;
        cnt = combinationSize <= cnt ? combinationSize : cnt;

        for (int i = cnt; i > 0; --i) {
            ratio *= ((n + 1.0 - i) / i);
        }
        ratio /= Math.pow(2.0, n);

        System.out.println("The ratio is: " + ratio);
        System.out.println("\n\n");
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
