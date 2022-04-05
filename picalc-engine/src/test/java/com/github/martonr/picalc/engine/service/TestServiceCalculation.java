package com.github.martonr.picalc.engine.service;

import com.github.martonr.picalc.engine.calculators.CalculatorParameters;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TestServiceCalculation {

    private final int quota = 14;
    private final int count = 100000;
    private final int[] votes = new int[] {3, 5, 8, 8, 12};
    private final double[] shapley = new double[] {0.11667, 0.11667, 0.20, 0.20, 0.36667};
    private final double[] banzhaf = new double[] {0.12, 0.12, 0.20, 0.20, 0.36};

    @Test
    void calculateNormal() {
        CalculatorParameters params = new CalculatorParameters();
        params.n = votes.length;
        params.votes = votes;
        params.quota = quota;

        ServiceCalculation service = new ServiceCalculation();
        long start = System.nanoTime();

        service.calculateSSBF(params, result -> {
            double[] shapleyResult = result.shapley;
            double[] banzhafResult = result.banzhaf;
            for (int i = 0; i < votes.length; i++) {
                System.out.println(votes[i] + " -> " + String.format("%.5f", shapleyResult[i])
                        + " | " + String.format("%.5f", shapley[i]));
                System.out.println(votes[i] + " -> " + String.format("%.5f", banzhafResult[i])
                        + " | " + String.format("%.5f", banzhaf[i]));
                System.out.println("---");
            }
            System.out.println();

            System.out.println("Calculation reported: " + result.time + " ms");

            Assertions.assertArrayEquals(shapley, shapleyResult, 0.00001);
            Assertions.assertArrayEquals(banzhaf, banzhafResult, 0.00001);
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
        params.n = votes.length;
        params.votes = votes;
        params.quota = quota;
        params.monteCarloCount = count;

        ServiceCalculation service = new ServiceCalculation();
        long start = System.nanoTime();

        service.calculateSSBF(params, result -> {
            double[] shapleyResult = result.shapley;
            double[] banzhafResult = result.banzhaf;
            for (int i = 0; i < votes.length; i++) {
                System.out.println(votes[i] + " -> " + String.format("%.5f", shapleyResult[i])
                        + " | " + String.format("%.5f", shapley[i]));
                System.out.println(votes[i] + " -> " + String.format("%.5f", banzhafResult[i])
                        + " | " + String.format("%.5f", banzhaf[i]));
                System.out.println("---");
            }
            System.out.println();

            System.out.println("Calculation reported: " + result.time + " ms");

            Assertions.assertArrayEquals(shapley, shapleyResult, 0.01);
            Assertions.assertArrayEquals(banzhaf, banzhafResult, 0.01);
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
        params.n = votes.length;
        params.votes = votes;
        params.quota = quota;
        params.monteCarloCount = count;

        ServiceCalculation service = new ServiceCalculation();
        long start = System.nanoTime();

        service.calculateSSBF(params, result -> {
            double[] shapleyResult = result.shapley;
            double[] banzhafResult = result.banzhaf;
            for (int i = 0; i < votes.length; i++) {
                System.out.println(votes[i] + " -> " + String.format("%.5f", shapleyResult[i])
                        + " | " + String.format("%.5f", shapley[i]));
                System.out.println(votes[i] + " -> " + String.format("%.5f", banzhafResult[i])
                        + " | " + String.format("%.5f", banzhaf[i]));
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
