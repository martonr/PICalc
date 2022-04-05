package com.github.martonr.picalc.cli;

import com.github.martonr.picalc.cli.service.ServiceSimulationFile;
import com.github.martonr.picalc.engine.service.ServiceSimulation;
import com.github.martonr.picalc.engine.service.SimulationParameters;
import com.github.martonr.picalc.engine.service.ServiceSimulationTask.ResultDelta;

public final class MainApplication {

    private final ServiceSimulationFile fileIO = new ServiceSimulationFile();

    private ServiceSimulation simulation;
    private SimulationParameters params;

    private volatile boolean safeToExit = false;

    private void startup() {
        params = fileIO.readSimulationParameters();
        simulation = new ServiceSimulation(fileIO.threadsRequested);
    }

    private void startSimulation() {
        safeToExit = false;

        simulation.simulate(params, this::processResults);

        boolean result = simulation.waitForRunningTasks();

        if (!result)
            System.out.println("The simulation was not completed.");
    }

    private void processResults(ResultDelta result) {
        if (result == null) {
            System.out.println("Simulation aborted!");
        } else {
            System.out.println("Finished in " + result.time + " seconds.");
            fileIO.saveSimulationResults(result);
        }

        safeToExit = true;
    }

    private void cleanup() {
        while (!safeToExit)
            Thread.onSpinWait();

        simulation.shutdown();
    }

    public static void main(String[] args) {
        MainApplication application = new MainApplication();
        application.startup();
        application.startSimulation();
        application.cleanup();
    }
}
