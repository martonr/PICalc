package com.github.martonr.picalc.engine.service;

import com.github.martonr.picalc.engine.calculators.CalculatorBanzhaf;
import com.github.martonr.picalc.engine.calculators.CalculatorParameters;
import com.github.martonr.picalc.engine.calculators.CalculatorShapley;
import com.github.martonr.picalc.engine.generators.GeneratorPartitionRandom;
import com.github.martonr.picalc.engine.service.SimulationCache.EntryChecker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TestSimulationCache {

    private int n = 5;
    private int v = 300;
    private int q1 = 150;
    private int q2 = 200;
    private int htSize = 3;

    @Test
    void createCache() {
        SimulationCache cache = new SimulationCache(n, v, htSize);
        EntryChecker checker = cache.createNewChecker();
        GeneratorPartitionRandom generator = new GeneratorPartitionRandom(n - 1);
        CalculatorShapley shapley = new CalculatorShapley(n);
        CalculatorBanzhaf banzhaf = new CalculatorBanzhaf(n);
        CalculatorParameters params = new CalculatorParameters();

        generator.initialize(v - 45, q1, 45);

        int[] original = new int[n];
        int[] votes;
        double[] tmp = new double[n];
        double[] resultsA = new double[n];
        double[] resultsB = new double[n];

        double[] originalValues = new double[2];
        double[] fromCacheValues = new double[2];

        int val = 0;
        for (int i = 0; i < 10; ++i) {
            votes = generator.next();
            checker.setVotesAndValue(votes, votes[n - 1]);

            if (i == 0) {
                // Store this and swap two elements
                System.arraycopy(votes, 0, original, 0, n);
                val = original[2];
                original[2] = original[0];
                original[0] = val;
            }
            if (i == 5) {
                // Check if this is found in the cache
                checker.setVotesAndValue(original, val);
                Assertions.assertTrue(cache.get(checker));
                fromCacheValues[0] = checker.found[0];
                fromCacheValues[1] = checker.found[1];
            }

            params.votes = votes;

            params.quota = q1;
            shapley.calculate(params, resultsA);
            params.quota = q2;
            shapley.calculate(params, tmp);

            for (int p = 0; p < n; ++p)
                resultsA[p] -= tmp[p];

            params.quota = q1;
            banzhaf.calculate(params, resultsB);
            params.quota = q2;
            banzhaf.calculate(params, tmp);

            for (int p = 0; p < n; ++p)
                resultsB[p] -= tmp[p];

            cache.store(checker, resultsA, resultsB);

            if (i == 0) {
                originalValues[0] = resultsA[2];
                originalValues[1] = resultsB[2];
            }
        }

        Assertions.assertEquals(8, cache.getSize());
        Assertions.assertArrayEquals(originalValues, fromCacheValues);
    }
}
