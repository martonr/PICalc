package com.github.martonr.picalc.engine.service;

import org.junit.jupiter.api.Test;

class TestServiceSimulationQuota {

    private final int players = 5;
    private final int vote = 300;
    private final int qFrom = 150;
    private final int qTo = 200;
    private final int max = -1;
    private final int limit = qFrom;
    private final int count = 100;
    private final int countMC = 100;

    @Test
    void simulateNormal() {
        SimulationParameters params = new SimulationParameters();
        params.n = players;
        params.votes = vote;
        params.quotaFrom = qFrom;
        params.quotaTo = qTo;
        params.maximumVote = max;
        params.simulationLimit = limit;
        params.indexMonteCarloCount = 0;
        params.monteCarloCount = count;
        params.isDpi = false;

        ServiceSimulation service = new ServiceSimulation();
        long start = System.nanoTime();

        service.simulate(params, result -> {
            double[][] resultShapley = result.shapley;
            double[][] resultBanzhaf = result.banzhaf;

            for (int i = 0; i < limit; ++i) {
                System.out.println("vote " + (i + 1) + " : ");

                System.out.print("S - p " + String.format("%.5f", resultShapley[i][0]) + " n "
                        + String.format("%.5f", resultShapley[i][1]) + " z "
                        + String.format("%.5f", resultShapley[i][2]));
                System.out.println(" | m " + String.format("%.5f", resultShapley[i][3]) + " sd "
                        + String.format("%.5f", resultShapley[i][4]));

                System.out.print("B - p " + String.format("%.5f", resultBanzhaf[i][0]) + " n "
                        + String.format("%.5f", resultBanzhaf[i][1]) + " z "
                        + String.format("%.5f", resultBanzhaf[i][2]));
                System.out.println(" | m " + String.format("%.5f", resultBanzhaf[i][3]) + " sd "
                        + String.format("%.5f", resultBanzhaf[i][4]));
            }
            System.out.println();

            System.out.println("Simulation reported: " + result.time + " s");

        });

        try {
            while (service.isRunning())
                Thread.sleep(100);
        } catch (Exception ignored) {
            // Interrupted
        }

        long elapsed = System.nanoTime() - start;
        System.out.println("Simulate exact test took " + elapsed / 1000L + " us");
    }

    // Expensive test
    @Test
    void simulateMC() {
        SimulationParameters params = new SimulationParameters();
        params.n = players;
        params.votes = vote;
        params.quotaFrom = qFrom;
        params.quotaTo = qTo;
        params.maximumVote = max;
        params.simulationLimit = limit;
        params.indexMonteCarloCount = countMC;
        params.monteCarloCount = count;
        params.isDpi = false;

        ServiceSimulation service = new ServiceSimulation();
        long start = System.nanoTime();

        service.simulate(params, result -> {
            double[][] resultShapley = result.shapley;
            double[][] resultBanzhaf = result.banzhaf;

            for (int i = 0; i < limit; ++i) {
                System.out.println("vote " + (i + 1) + " : ");

                System.out.print("S - p " + String.format("%.5f", resultShapley[i][0]) + " n "
                        + String.format("%.5f", resultShapley[i][1]) + " z "
                        + String.format("%.5f", resultShapley[i][2]));
                System.out.println(" | m " + String.format("%.5f", resultShapley[i][3]) + " sd "
                        + String.format("%.5f", resultShapley[i][4]));

                System.out.print("B - p " + String.format("%.5f", resultBanzhaf[i][0]) + " n "
                        + String.format("%.5f", resultBanzhaf[i][1]) + " z "
                        + String.format("%.5f", resultBanzhaf[i][2]));
                System.out.println(" | m " + String.format("%.5f", resultBanzhaf[i][3]) + " sd "
                        + String.format("%.5f", resultBanzhaf[i][4]));
            }
            System.out.println();

            System.out.println("Simulation reported: " + result.time + " s");

        });

        try {
            while (service.isRunning())
                Thread.sleep(100);
        } catch (Exception ignored) {
            // Interrupted
        }

        long elapsed = System.nanoTime() - start;
        System.out.println("SimulateMC test took " + elapsed / 1000L + " us");
    }

    @Test
    void cleanupTasks() {
        SimulationParameters params = new SimulationParameters();
        params.n = players;
        params.votes = vote;
        params.quotaFrom = qFrom;
        params.quotaTo = qTo;
        params.maximumVote = max;
        params.simulationLimit = limit;
        params.indexMonteCarloCount = countMC;
        params.monteCarloCount = count;
        params.isDpi = false;

        ServiceSimulation service = new ServiceSimulation();
        long start = System.nanoTime();

        service.simulate(params, result -> {
            double[][] resultShapley = result.shapley;
            double[][] resultBanzhaf = result.banzhaf;
            // int maxVote = vote - players + 1;

            for (int i = 0; i < limit; ++i) {
                System.out.println("vote " + (i + 1) + " : ");

                System.out.print("S - p " + String.format("%.5f", resultShapley[i][0]) + " n "
                        + String.format("%.5f", resultShapley[i][1]) + " z "
                        + String.format("%.5f", resultShapley[i][2]));
                System.out.println(" | m " + String.format("%.5f", resultShapley[i][3]) + " sd "
                        + String.format("%.5f", resultShapley[i][4]));

                System.out.print("B - p " + String.format("%.5f", resultBanzhaf[i][0]) + " n "
                        + String.format("%.5f", resultBanzhaf[i][1]) + " z "
                        + String.format("%.5f", resultBanzhaf[i][2]));
                System.out.println(" | m " + String.format("%.5f", resultBanzhaf[i][3]) + " sd "
                        + String.format("%.5f", resultBanzhaf[i][4]));
            }
            System.out.println();

            System.out.println("Simulation reported: " + result.time + " s");

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
