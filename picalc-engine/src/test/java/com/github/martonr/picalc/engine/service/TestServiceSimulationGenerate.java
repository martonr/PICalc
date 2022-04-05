package com.github.martonr.picalc.engine.service;

import org.junit.jupiter.api.Test;

class TestServiceSimulationGenerate {

    private final int players = 5;
    private final int vote = 300;
    private final int qFrom = 150;
    private final int qTo = 200;
    private final int max = -1;
    private final int limit = qFrom;
    private final int count = 100;
    private final int setVote = 47;
    // private final int countMC = 1000;

    @Test
    void generateNormal() {
        SimulationParameters params = new SimulationParameters();
        params.n = players;
        params.votes = vote;
        params.quotaFrom = qFrom;
        params.quotaTo = qTo;
        params.maximumVote = max;
        params.simulationLimit = limit;
        params.singleVote = setVote;
        params.indexMonteCarloCount = 0;
        params.monteCarloCount = count;
        params.isDpi = false;

        ServiceSimulation service = new ServiceSimulation();
        long start = System.nanoTime();

        service.generate(params, result -> {
            double[] resultShapley = result.shapley;
            double[] resultBanzhaf = result.banzhaf;
            int[][] resultVotes = result.votes;

            for (int i = 0; i < resultShapley.length; ++i) {
                for (int j = resultVotes[0].length - 1; j > 0; --j) {
                    System.out.print(resultVotes[i][j] + ",");
                }
                System.out.print(resultVotes[i][0] + " : ");

                System.out.print("S - p1 " + String.format("%.5f", resultShapley[i]));
                System.out.println(" | B - p1 " + String.format("%.5f", resultBanzhaf[i]));
            }
            System.out.println();

            System.out.println("Generation reported: " + result.time + " s");

        });

        try {
            while (service.isRunning())
                Thread.sleep(100);
        } catch (Exception ignored) {
            // Interrupted
        }

        long elapsed = System.nanoTime() - start;
        System.out.println("Generate exact test took " + elapsed / 1000L + " us");
    }
}
