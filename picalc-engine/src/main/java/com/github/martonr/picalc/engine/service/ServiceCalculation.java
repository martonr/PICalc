package com.github.martonr.picalc.engine.service;

import com.github.martonr.picalc.engine.calculators.CalculatorParameters;
import com.github.martonr.picalc.engine.service.ServiceCalculationTask.Results;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class ServiceCalculation {
    /**
     * Thread count used by the executor
     */
    private final int THREAD_COUNT;

    /**
     * Thread timeout in milliseconds
     */
    private final long THREAD_TIMEOUT = 1000;

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
    private final ServiceCalculationTask task;

    public ServiceCalculation() {
        THREAD_COUNT = Runtime.getRuntime().availableProcessors();
        EXECUTOR = new ThreadPoolExecutor(THREAD_COUNT + 1, THREAD_COUNT + 1, THREAD_TIMEOUT,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), THREAD_FACTORY,
                new ThreadPoolExecutor.AbortPolicy());
        EXECUTOR.allowCoreThreadTimeOut(true);
        runningTasks = new ArrayList<>(THREAD_COUNT);
        task = new ServiceCalculationTask(EXECUTOR, THREAD_COUNT);
    }

    public ServiceCalculation(int threads) {
        THREAD_COUNT = threads;
        EXECUTOR = new ThreadPoolExecutor(THREAD_COUNT + 1, THREAD_COUNT + 1, THREAD_TIMEOUT,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), THREAD_FACTORY,
                new ThreadPoolExecutor.AbortPolicy());
        EXECUTOR.allowCoreThreadTimeOut(true);
        runningTasks = new ArrayList<>(THREAD_COUNT);
        task = new ServiceCalculationTask(EXECUTOR, THREAD_COUNT);
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
     * This method starts a calculation of the classic index values Requires an array containing the
     * player votes, required quota, and a callback that will be executed when the computation is
     * finished
     *
     * @param params a CalculatorParameters object containing the parameters
     * @param callback Callback function that will be called after the results are available
     */
    public final void calculateSSBF(CalculatorParameters params, Consumer<Results> callback) {
        // Clean up queue
        cleanupTasks();

        // callback.accept(task.startCalculation(params));

        runningTasks.add(EXECUTOR.submit(() -> {
            callback.accept(task.startCalculation(params));
            cleanupTasks();
        }));
    }

    /**
     * This method starts a Monte-Carlo estimation of the classic index values Requires an array
     * containing the player votes, required quota, a simulation count and a callback that will be
     * executed when the computation is finished
     *
     * @param params a CalculatorParameters object containing the parameters
     * @param callback Callback function that will be called after the results are available
     */
    public final void calculateDPI(CalculatorParameters params, Consumer<Results> callback) {
        // Clean up queue
        cleanupTasks();

        // callback.accept(task.startCalculation(params));

        runningTasks.add(EXECUTOR.submit(() -> {
            callback.accept(task.startCalculation(params));
            cleanupTasks();
        }));
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
