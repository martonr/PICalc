package com.github.martonr.picalc.engine.service;

import com.github.martonr.picalc.engine.service.ServiceSimulationTask.ResultDelta;
import com.github.martonr.picalc.engine.service.ServiceSimulationTask.ResultDeltaSingle;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class ServiceSimulation {
    /**
     * Thread count used by the executor
     */
    private final int THREAD_COUNT;

    /**
     * Thread timeout in milliseconds
     */
    private final long THREAD_TIMEOUT = 1000;

    /**
     * Error term used in double value comparison for exact calculation
     */
    // private final double EPSILON = 0.001;

    /**
     * Error term used in double value comparison for MC estimation
     */
    // private final double EPSILON_MC = 0.001;

    /**
     * ThreadFactory for the executor, based on the default ThreadFactory, but Threads are daemon by
     * default and have minimum priority
     */
    private final ThreadFactory THREAD_FACTORY = r -> {
        Thread t = Executors.defaultThreadFactory().newThread(r);
        t.setDaemon(true);
        t.setPriority(Thread.MIN_PRIORITY);
        return t;
    };

    /**
     * The executor used to manage computation Threads, has a fixed thread count and an unbounded
     * queue
     */
    private final ThreadPoolExecutor EXECUTOR;

    /**
     * A list containing Futures of the currently running tasks
     */
    private final List<Future<?>> runningTasks;

    /**
     * The simulation task
     */
    private final ServiceSimulationTask task;

    public ServiceSimulation() {
        THREAD_COUNT = Runtime.getRuntime().availableProcessors();
        EXECUTOR = new ThreadPoolExecutor(THREAD_COUNT + 1, THREAD_COUNT + 1, THREAD_TIMEOUT,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), THREAD_FACTORY,
                new ThreadPoolExecutor.AbortPolicy());
        EXECUTOR.allowCoreThreadTimeOut(true);
        runningTasks = new ArrayList<>(THREAD_COUNT + 1);
        task = new ServiceSimulationTask(EXECUTOR, THREAD_COUNT);
    }

    public ServiceSimulation(int threads) {
        THREAD_COUNT = threads;
        EXECUTOR = new ThreadPoolExecutor(THREAD_COUNT + 1, THREAD_COUNT + 1, THREAD_TIMEOUT,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), THREAD_FACTORY,
                new ThreadPoolExecutor.AbortPolicy());
        EXECUTOR.allowCoreThreadTimeOut(true);
        runningTasks = new ArrayList<>(THREAD_COUNT + 1);
        task = new ServiceSimulationTask(EXECUTOR, THREAD_COUNT);
    }

    /**
     * Cancels all currently running tasks, empties the executor queue
     */
    public final void cleanupTasks() {
        for (Future<?> f : runningTasks)
            f.cancel(true);
        EXECUTOR.purge();
        runningTasks.clear();
    }

    /**
     * Starts a simulation of different vote distribution scenarios to estimate the power index
     * changes from one quota value to the other. The simulation goes through all vote values for a
     * player and generates random scenarios for the other player votes, computes the power index
     * then records the change. The power index values are computed directly.
     *
     * @param parameters a SimulationParameters object with the parameters
     * @param callback Callback function receiving a Result object with the results
     */
    public final void simulate(SimulationParameters params, Consumer<ResultDelta> callback) {
        // Clean up queue
        cleanupTasks();

        runningTasks.add(EXECUTOR.submit(() -> {
            callback.accept(task.startSimulation(params));
            cleanupTasks();
        }));
    }

    /**
     * Starts a simulation of different vote distribution scenarios to estimate the power index
     * changes from one quota value to the other.
     * <p>
     * This function only generates random scenarios with a given set vote value, computes the power
     * index then records the change. The power index values are calculated directly.
     *
     * @param parameters a SimulationParameters object qith the parameters
     * @param callback Callback function receiving a Result object with the results
     */
    public void generate(SimulationParameters params, Consumer<ResultDeltaSingle> callback) {
        // Clean up queue
        cleanupTasks();

        runningTasks.add(EXECUTOR.submit(() -> {
            callback.accept(task.startSingleScenario(params));
            cleanupTasks();
        }));
    }

    public final boolean waitForRunningTasks() {
        try {
            for (Future<?> f : runningTasks)
                f.get();
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        } finally {
            cleanupTasks();
        }
        return true;
    }

    /**
     * Cane be used to check if any calculations are running currently
     *
     * @return True if there are calculations running currently, false otherwise
     */
    public final boolean isRunning() {
        return !runningTasks.isEmpty();
    }

    /**
     * Can be used to shut down the executor
     */
    public void shutdown() {
        EXECUTOR.shutdownNow();
        try {
            EXECUTOR.awaitTermination(500, TimeUnit.MILLISECONDS);
        } catch (Exception ignored) {
            // Interrupted
        }
    }
}
