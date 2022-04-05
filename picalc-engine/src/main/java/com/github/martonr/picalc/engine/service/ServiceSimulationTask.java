package com.github.martonr.picalc.engine.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import com.github.martonr.picalc.engine.calculators.CalculatorBanzhaf;
import com.github.martonr.picalc.engine.calculators.CalculatorDPI;
import com.github.martonr.picalc.engine.calculators.CalculatorParameters;
import com.github.martonr.picalc.engine.calculators.CalculatorShapley;
import com.github.martonr.picalc.engine.generators.GeneratorPartitionRandom;
import com.github.martonr.picalc.engine.service.SimulationCache.EntryChecker;

public final class ServiceSimulationTask {

    private static final double[] ZEROS = {0, 0, 0, 0, 0};

    private final ThreadPoolExecutor executor;
    private final int threads;

    public ServiceSimulationTask(ThreadPoolExecutor executor, int threads) {
        this.executor = executor;
        this.threads = threads;
    }

    public final ResultDelta startSimulation(SimulationParameters params) {
        List<Future<?>> spawnedTasks = new ArrayList<>(threads);

        // The limit is the maximum votes that a player can have
        // (if a player has more, other players would have 0 votes)
        int n = params.n, votes = params.votes, limit = params.simulationLimit;
        limit = limit > 0 ? limit : (votes - n + 1);

        ResultDelta output = new ResultDelta(limit);
        SimulationScenario[] scenarios = createScenarios(params, threads);

        long mc = params.monteCarloCount;

        long start = System.nanoTime();

        for (int v = 0; v < limit; ++v) {
            for (int i = 0; i < threads; ++i) {
                int t = i;

                scenarios[t].fixed = v + 1;

                spawnedTasks.add(executor.submit(() -> simulateScenario(scenarios[t])));
                // simulateScenario(scenarios[t]);
            }

            try {
                for (Future<?> f : spawnedTasks)
                    f.get();
            } catch (Exception ex) {
                // Computation was interrupted
                ex.printStackTrace();
                return null;
            } finally {
                for (Future<?> f : spawnedTasks)
                    f.cancel(true);
                spawnedTasks.clear();
            }

            for (int t = 0; t < threads; ++t) {
                for (int i = 0; i < 5; ++i) {
                    output.shapley[v][i] += scenarios[t].ssResults[i];
                    output.banzhaf[v][i] += scenarios[t].bfResults[i];
                }
            }

            output.shapley[v][0] /= mc;
            output.shapley[v][1] /= mc;
            output.shapley[v][2] /= mc;
            output.shapley[v][3] /= threads;
            output.shapley[v][4] /= threads;

            output.shapley[v][4] = Math.sqrt(output.shapley[v][4]);

            output.banzhaf[v][0] /= mc;
            output.banzhaf[v][1] /= mc;
            output.banzhaf[v][2] /= mc;
            output.banzhaf[v][3] /= threads;
            output.banzhaf[v][4] /= threads;

            output.banzhaf[v][4] = Math.sqrt(output.banzhaf[v][4]);
        }

        System.out.println("Simulation had " + scenarios[0].cache.getHits() + " cache hits and "
                + scenarios[0].cache.getStores() + " cache stores out of " + (limit * mc)
                + " scenarios.");

        output.time = TimeUnit.SECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);

        return output;
    }

    public final ResultDeltaSingle startSingleScenario(SimulationParameters params) {
        List<Future<?>> spawnedTasks = new ArrayList<>(threads);

        // The limit is the maximum votes that a player can have
        // (if a player has more, other players would have 0 votes)
        int n = params.n;
        int sc = (int) params.monteCarloCount;

        ResultDeltaSingle output = new ResultDeltaSingle(n, sc);

        SimulationScenario[] scenarios = createSingleScenarios(params, output);

        long start = System.nanoTime();

        for (int i = 0; i < threads; ++i) {
            int t = i;

            spawnedTasks.add(executor.submit(() -> simulateSingleScenario(scenarios[t])));
            // simulateSingleScenario(scenarios[t]);
        }

        try {
            for (Future<?> f : spawnedTasks)
                f.get();
        } catch (Exception ex) {
            // Computation was interrupted
            ex.printStackTrace();
            return null;
        } finally {
            for (Future<?> f : spawnedTasks)
                f.cancel(true);
            spawnedTasks.clear();
        }

        output.time = TimeUnit.SECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);

        return output;
    }

    private final SimulationScenario[] createScenarios(SimulationParameters params, int threads) {
        SimulationScenario[] output = new SimulationScenario[threads];

        int n = params.n;
        int votes = params.votes;
        SimulationCache cache = new SimulationCache(n, (votes - n + 1));

        long mc = params.monteCarloCount;
        long c = mc / threads;
        long r = mc - (c * threads);

        for (int i = 0; i < threads; ++i) {
            // Distribute the work between threads
            long work = (i < r) ? (c + 1) : c;

            output[i] = new SimulationScenario(params);

            output[i].cache = cache;
            output[i].checker = cache.createNewChecker();
            output[i].mc = work;
        }

        return output;
    }

    private final SimulationScenario[] createSingleScenarios(SimulationParameters params,
            ResultDeltaSingle result) {
        SimulationScenario[] output = new SimulationScenario[threads];

        int mc = (int) params.monteCarloCount;
        int c = mc / threads;
        int r = mc - (c * threads);

        int offset = 0;
        for (int i = 0; i < threads; ++i) {
            int work = (i < r) ? (c + 1) : c;

            output[i] = new SimulationScenario(params);

            output[i].sc = work;

            output[i].ssResults = result.shapley;
            output[i].bfResults = result.banzhaf;
            output[i].votes = result.votes;

            output[i].offset = offset;
            offset += work;
        }

        return output;
    }

    private final void simulateScenario(SimulationScenario scenario) {
        SimulationCache cache = scenario.cache;
        EntryChecker checker = scenario.checker;
        SimulationParameters simulationParams = scenario.simulationParams;
        CalculatorParameters calculatorParams = scenario.calculatorParams;
        GeneratorPartitionRandom generator = scenario.generator;

        int n = simulationParams.n;
        // Divide the remaining votes between the remaining players
        // max is number of votes a player can have, minus the fixed player
        int v = simulationParams.votes;
        int m = simulationParams.maximumVote;
        int f = scenario.fixed;
        generator.initialize(v - f, m, f);

        long mc = scenario.mc;
        double tolerance = simulationParams.tolerance;

        // Zero the scenario arrays
        System.arraycopy(ZEROS, 0, scenario.ssResults, 0, 5);
        System.arraycopy(ZEROS, 0, scenario.bfResults, 0, 5);

        long emc = calculatorParams.monteCarloCount;
        boolean estimated = emc > 0;
        boolean isDpi = simulationParams.isDpi;

        boolean inCache;
        double ssDelta = 0, bfDelta = 0;
        for (long i = 0; i < mc; ++i) {
            calculatorParams.votes = generator.next();
            checker.setVotesAndValue(calculatorParams.votes, calculatorParams.votes[n - 1]);

            inCache = false;
            inCache = cache.get(checker);

            if (!inCache) {
                // Calculate values
                if (isDpi) {
                    calculateDPIDDelta(scenario, estimated);
                } else {
                    calculateQDDelta(scenario, estimated);
                }

                ssDelta = scenario.rawDeltaA[n - 1];
                bfDelta = scenario.rawDeltaB[n - 1];

                // Store in the cache
                cache.store(checker, scenario.rawDeltaA, scenario.rawDeltaB);
            } else {
                ssDelta = checker.found[0];
                bfDelta = checker.found[1];
            }

            updateStatistics(tolerance, ssDelta, scenario.ssResults, i + 1);
            updateStatistics(tolerance, bfDelta, scenario.bfResults, i + 1);
        }

        // Unbiased variance estimates
        scenario.ssResults[4] /= (mc - 1);
        scenario.bfResults[4] /= (mc - 1);
    }


    private final void simulateSingleScenario(SimulationScenario scenario) {
        SimulationParameters simulationParams = scenario.simulationParams;
        CalculatorParameters calculatorParams = scenario.calculatorParams;
        GeneratorPartitionRandom generator = scenario.generator;

        int n = simulationParams.n;
        // Divide the remaining votes between the remaining players
        // max is number of votes a player can have, minus the fixed player
        int v = simulationParams.votes;
        int m = simulationParams.maximumVote;
        int f = simulationParams.singleVote;
        generator.initialize(v - f, m, f);

        int sc = scenario.sc;
        int offset = scenario.offset;

        boolean estimated = calculatorParams.monteCarloCount > 0;
        boolean isDpi = simulationParams.isDpi;

        double ssDelta = 0, bfDelta = 0;
        for (int i = 0; i < sc; ++i) {
            calculatorParams.votes = generator.next();

            System.arraycopy(calculatorParams.votes, 0, scenario.votes[offset + i], 0, n);

            // Calculate values
            if (isDpi) {
                calculateDPIDDelta(scenario, estimated);
            } else {
                calculateQDDelta(scenario, estimated);
            }

            ssDelta = scenario.rawDeltaA[n - 1];
            bfDelta = scenario.rawDeltaB[n - 1];

            scenario.ssResults[offset + i] = ssDelta;
            scenario.bfResults[offset + i] = bfDelta;
        }
    }

    private final void updateStatistics(double tolerance, double value, double[] result, long n) {
        long valueBits = Double.doubleToRawLongBits(value);
        // Math.abs(double) inlined
        double abs = Double.longBitsToDouble(valueBits & ~(0x8000000000000000L));

        if (abs < tolerance) {
            // Considered as zero
            result[2]++;
        } else if (valueBits < 0) {
            // Negative value
            result[1]++;
        } else {
            // Positive value
            result[0]++;
        }

        // Welford's algorithm for rolling mean and variance
        double pAvg = result[3], pVar = result[4];
        double nAvg, nVar;

        nAvg = pAvg + (value - pAvg) / n;
        nVar = pVar + (value - pAvg) * (value - nAvg);

        result[3] = nAvg;
        result[4] = nVar;
    }

    private final void calculateQDDelta(SimulationScenario scenario, boolean estimated) {
        int n = scenario.simulationParams.n;
        int quotaTo = scenario.simulationParams.quotaTo;
        int quotaFrom = scenario.simulationParams.quotaFrom;

        double[] tmpValues = scenario.rawValues;
        double[] tmpDeltaA = scenario.rawDeltaA;
        double[] tmpDeltaB = scenario.rawDeltaB;

        CalculatorParameters params = scenario.calculatorParams;
        CalculatorShapley shapley = scenario.shapley;
        CalculatorBanzhaf banzhaf = scenario.banzhaf;

        long emc = params.monteCarloCount;

        if (estimated) {
            params.quota = quotaTo;
            shapley.calculateMC(params, tmpDeltaA, emc);
            CalculatorShapley.normalizeSS(tmpDeltaA, emc);

            params.quota = quotaFrom;
            shapley.calculateMC(params, tmpValues, emc);
            CalculatorShapley.normalizeSS(tmpValues, emc);

            for (int p = 0; p < n; ++p)
                tmpDeltaA[p] -= tmpValues[p];

            params.quota = quotaTo;
            banzhaf.calculateMC(params, tmpDeltaB, emc);
            CalculatorBanzhaf.normalizeBF(tmpDeltaB);

            params.quota = quotaFrom;
            banzhaf.calculateMC(params, tmpValues, emc);
            CalculatorBanzhaf.normalizeBF(tmpValues);

            for (int p = 0; p < n; ++p)
                tmpDeltaB[p] -= tmpValues[p];
        } else {
            params.quota = quotaTo;
            shapley.calculate(params, tmpDeltaA);

            params.quota = quotaFrom;
            shapley.calculate(params, tmpValues);

            for (int p = 0; p < n; ++p)
                tmpDeltaA[p] -= tmpValues[p];

            params.quota = quotaTo;
            banzhaf.calculate(params, tmpDeltaB);

            params.quota = quotaFrom;
            banzhaf.calculate(params, tmpValues);

            for (int p = 0; p < n; ++p)
                tmpDeltaB[p] -= tmpValues[p];
        }
    }

    private final void calculateDPIDDelta(SimulationScenario scenario, boolean estimated) {
        int n = scenario.simulationParams.n;

        double[] tmpValues = scenario.rawValues;
        double[] tmpDeltaA = scenario.rawDeltaA;
        double[] tmpDeltaB = scenario.rawDeltaB;

        CalculatorParameters params = scenario.calculatorParams;
        CalculatorShapley shapley = scenario.shapley;
        CalculatorBanzhaf banzhaf = scenario.banzhaf;
        CalculatorDPI dpi = scenario.dpi;

        long emc = params.monteCarloCount;

        for (int i = 0; i < n; ++i)
            params.weights[i] = params.votes[i];

        if (estimated) {
            shapley.calculateMC(params, tmpDeltaA, emc);
            CalculatorShapley.normalizeSS(tmpDeltaA, emc);

            banzhaf.calculateMC(params, tmpDeltaB, emc);
            CalculatorBanzhaf.normalizeBF(tmpDeltaB);

            dpi.calculateMC(params, tmpValues, emc);
            CalculatorDPI.normalizeDPI(tmpValues);

            for (int p = 0; p < n; ++p) {
                tmpDeltaA[p] -= tmpValues[p];
                tmpDeltaB[p] -= tmpValues[p];
            }
        } else {
            shapley.calculate(params, tmpDeltaA);
            banzhaf.calculate(params, tmpDeltaB);
            dpi.calculate(params, tmpValues);

            for (int p = 0; p < n; ++p) {
                tmpDeltaA[p] -= tmpValues[p];
                tmpDeltaB[p] -= tmpValues[p];
            }
        }
    }

    public static class ResultDelta {
        public double[][] shapley;
        public double[][] banzhaf;
        public long time;

        ResultDelta(int limit) {
            this.shapley = new double[limit][5];
            this.banzhaf = new double[limit][5];
        }
    }

    public static class ResultDeltaSingle {
        public double[] shapley;
        public double[] banzhaf;
        public int[][] votes;
        public long time;

        ResultDeltaSingle(int n, int sc) {
            this.shapley = new double[sc];
            this.banzhaf = new double[sc];
            this.votes = new int[sc][n];
        }
    }

    private static class SimulationScenario {
        private SimulationCache cache;
        private EntryChecker checker;

        private SimulationParameters simulationParams;
        private CalculatorParameters calculatorParams;

        private GeneratorPartitionRandom generator;

        private CalculatorShapley shapley;
        private CalculatorBanzhaf banzhaf;
        private CalculatorDPI dpi;

        private double[] rawValues;
        private double[] rawDeltaA;
        private double[] rawDeltaB;
        private double[] ssResults;
        private double[] bfResults;

        private int[][] votes;

        private long mc;

        private int fixed;
        private int offset;
        private int sc;

        private SimulationScenario(SimulationParameters params) {
            int n = params.n;
            boolean isDpi = params.isDpi;

            simulationParams = params;
            generator = new GeneratorPartitionRandom(n - 1);
            calculatorParams = new CalculatorParameters();
            calculatorParams.n = n;
            calculatorParams.monteCarloCount = params.indexMonteCarloCount;

            shapley = new CalculatorShapley(n);
            banzhaf = new CalculatorBanzhaf(n);
            rawValues = new double[n];
            rawDeltaA = new double[n];
            rawDeltaB = new double[n];

            if (isDpi) {
                calculatorParams.quota = params.quota;
                calculatorParams.weights = new double[n];
                dpi = new CalculatorDPI(n);
            }

            if (params.singleVote < 1) {
                ssResults = new double[5];
                bfResults = new double[5];
            }
        }
    }
}
