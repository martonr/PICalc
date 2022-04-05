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

public final class ServiceCalculationTask {

    private final ThreadPoolExecutor executor;
    private final int threads;

    public ServiceCalculationTask(ThreadPoolExecutor executor, int threads) {
        this.executor = executor;
        this.threads = threads;
    }

    public final Results startCalculation(CalculatorParameters params) {
        List<Future<?>> spawnedTasks = new ArrayList<>(threads);

        CalculationScenario[] scenarios = createScenarios(params);

        int n = params.n;
        Results output = new Results(n, scenarios[0].isDpi);

        long start = System.nanoTime();

        for (int i = 0; i < scenarios.length; ++i) {
            int t = i;

            spawnedTasks.add(executor.submit(() -> executeScenario(scenarios[t])));
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

        processScenarioResults(scenarios);

        if (scenarios[0].isDpi) {
            System.arraycopy(scenarios[0].rawResultsA, 0, output.dpi, 0, n);
        } else {
            System.arraycopy(scenarios[0].rawResultsA, 0, output.shapley, 0, n);
            System.arraycopy(scenarios[0].rawResultsB, 0, output.banzhaf, 0, n);
        }

        output.time = TimeUnit.SECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);

        return output;
    }

    private final CalculationScenario[] createScenarios(CalculatorParameters params) {
        CalculationScenario[] output;

        long mc = params.monteCarloCount;
        boolean estimated = mc > 0;
        int t = estimated ? threads : 1;

        long c = mc / threads;
        long r = mc - (c * threads);

        output = new CalculationScenario[t];

        for (int i = 0; i < t; ++i) {
            long work = (i < r) ? (c + 1) : c;

            output[i] = new CalculationScenario(params);
            output[i].mc = work;
        }

        return output;
    }

    private final void executeScenario(CalculationScenario scenario) {
        boolean estimated = scenario.mc > 0;

        if (scenario.isDpi) {
            calculateDPI(scenario, estimated);
        } else {
            calculateSSBF(scenario, estimated);
        }
    }

    private final void calculateSSBF(CalculationScenario scenario, boolean estimated) {
        CalculatorParameters params = scenario.calculatorParams;
        CalculatorShapley shapley = scenario.shapley;
        CalculatorBanzhaf banzhaf = scenario.banzhaf;

        long emc = scenario.mc;

        if (estimated) {
            shapley.calculateMC(params, scenario.rawResultsA, emc);
            banzhaf.calculateMC(params, scenario.rawResultsB, emc);
        } else {
            shapley.calculate(params, scenario.rawResultsA);
            banzhaf.calculate(params, scenario.rawResultsB);
        }
    }

    private final void calculateDPI(CalculationScenario scenario, boolean estimated) {
        CalculatorParameters params = scenario.calculatorParams;
        CalculatorDPI dpi = scenario.dpi;

        long emc = scenario.mc;

        if (estimated) {
            dpi.calculateMC(params, scenario.rawResultsA, emc);
        } else {
            dpi.calculate(params, scenario.rawResultsA);
        }
    }

    private final void processScenarioResults(CalculationScenario[] scenarios) {
        int n = scenarios[0].calculatorParams.n;
        long mc = scenarios[0].calculatorParams.monteCarloCount;

        boolean estimated = mc > 0;
        boolean isDpi = scenarios[0].isDpi;

        if (isDpi) {
            for (int j = 1; j < scenarios.length; ++j) {
                for (int i = 0; i < n; ++i) {
                    scenarios[0].rawResultsA[i] += scenarios[j].rawResultsA[i];
                }
            }

            if (estimated) {
                CalculatorDPI.normalizeDPI(scenarios[0].rawResultsA);
            }
        } else {
            for (int j = 1; j < scenarios.length; ++j) {
                for (int i = 0; i < n; ++i) {
                    scenarios[0].rawResultsA[i] += scenarios[j].rawResultsA[i];
                    scenarios[0].rawResultsB[i] += scenarios[j].rawResultsB[i];
                }
            }

            if (estimated) {
                CalculatorShapley.normalizeSS(scenarios[0].rawResultsA, mc);
                CalculatorBanzhaf.normalizeBF(scenarios[0].rawResultsB);
            }
        }
    }

    public static class Results {
        public double[] shapley;
        public double[] banzhaf;
        public double[] dpi;
        public long time;

        Results(int n, boolean isDpi) {
            if (isDpi) {
                dpi = new double[n];
            } else {
                shapley = new double[n];
                banzhaf = new double[n];
            }
        }
    }

    private static class CalculationScenario {
        private CalculatorParameters calculatorParams;

        private CalculatorShapley shapley;
        private CalculatorBanzhaf banzhaf;
        private CalculatorDPI dpi;

        private double[] rawResultsA;
        private double[] rawResultsB;

        private long mc;

        private boolean isDpi;

        private CalculationScenario(CalculatorParameters params) {
            int n = params.n;
            isDpi = params.weights != null;

            calculatorParams = params;
            rawResultsA = new double[n];

            if (isDpi) {
                dpi = new CalculatorDPI(n);
            } else {
                shapley = new CalculatorShapley(n);
                banzhaf = new CalculatorBanzhaf(n);
                rawResultsB = new double[n];
            }
        }
    }
}
