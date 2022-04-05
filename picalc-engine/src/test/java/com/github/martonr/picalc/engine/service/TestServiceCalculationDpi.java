package com.github.martonr.picalc.engine.service;

import com.github.martonr.picalc.engine.calculators.CalculatorParameters;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestServiceCalculationDpi {

    private final int count = 10000000;
    private final double[] weights = new double[] {3, 5, 8, 8, 12};
    private final double[] dpi = new double[] {0.082875, 0.146355, 0.228172, 0.228172, 0.314424};

    @Test
    void calculateNormal() {
        CalculatorParameters params = new CalculatorParameters();
        params.n = weights.length;
        params.weights = weights;

        ServiceCalculation service = new ServiceCalculation();
        long start = System.nanoTime();

        service.calculateDPI(params, result -> {
            double[] dpiResult = result.dpi;
            for (int i = 0; i < weights.length; i++) {
                System.out.println(weights[i] + " -> " + String.format("%.5f", dpiResult[i]) + " | "
                        + String.format("%.5f", dpi[i]));
            }
            System.out.println("---");

            System.out.println("Calculation reported: " + result.time + " s");

            Assertions.assertArrayEquals(dpi, dpiResult, 0.001);
        });

        try {
            while (service.isRunning())
                Thread.sleep(100);
        } catch (Exception ignored) {
            // Interrupted
        }

        long elapsed = System.nanoTime() - start;
        System.out.println("Test took " + elapsed / 1000L + " us");
    }

    @Test
    void calculateMC() {
        CalculatorParameters params = new CalculatorParameters();
        params.n = weights.length;
        params.weights = weights;
        params.monteCarloCount = count;

        ServiceCalculation service = new ServiceCalculation();
        long start = System.nanoTime();

        service.calculateDPI(params, result -> {
            double[] dpiResult = result.dpi;
            for (int i = 0; i < weights.length; i++) {
                System.out.println(weights[i] + " -> " + String.format("%.5f", dpiResult[i]) + " | "
                        + String.format("%.5f", dpi[i]));
            }
            System.out.println("---");

            System.out.println("Calculation reported: " + result.time + " s");

            Assertions.assertArrayEquals(dpi, dpiResult, 0.05);
        });

        try {
            while (service.isRunning())
                Thread.sleep(100);
        } catch (Exception ignored) {
            // Interrupted
        }

        long elapsed = System.nanoTime() - start;
        System.out.println("Test took " + elapsed / 1000L + " us");
    }

    @Test
    void cleanupTasks() {
        CalculatorParameters params = new CalculatorParameters();
        params.n = weights.length;
        params.weights = weights;
        params.monteCarloCount = count;

        ServiceCalculation service = new ServiceCalculation();
        long start = System.nanoTime();

        service.calculateDPI(params, result -> {
            double[] dpiResult = result.dpi;
            for (int i = 0; i < weights.length; i++) {
                System.out.println(weights[i] + " -> " + String.format("%.5f", dpiResult[i]) + " | "
                        + String.format("%.5f", dpi[i]));
                System.out.println("---");
            }
            System.out.println();
        });

        try {
            service.cleanupTasks();
            while (service.isRunning())
                Thread.sleep(100);
        } catch (Exception ignored) {
            // Interrupted
        }

        long elapsed = System.nanoTime() - start;
        System.out.println("Finished in " + elapsed / 1000L + " us");
    }

}
