package calculator;

import container.ContainerDifference;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Created by Márton Rajnai on 2016-04-06.
 * Contact: marton.rajnai@gmail.com
 */

/**
 * Holds a quota change effect simulation.
 */
public final class CalculatorComplex {

    /**
     * differenceResults - records the simulation results
     * diffThreads - collects the threads for the calculation
     * startTime - time when the threads were started
     * runningTime - time when the threads ended
     */
    private final ContainerDifference differenceResults;

    private final Thread[] diffThreads;

    private long startTime;
    private long runningTime;

    /**
     * Create a new simulation.
     *
     * @param vTotal        - the total number of votes
     * @param pCount        - the number of voters
     * @param quota1        - first quota, simulation will measure change FROM this value
     * @param quota2        - second quots, simulation will measure change TO this value
     * @param simCount      - number of scenarios to simulate for each vote possible
     * @param valueSimCount - number of Monte Carlo simulations to use for individual indices
     */
    public CalculatorComplex( int vTotal, int pCount, int quota1, int quota2, int simCount, int valueSimCount ) {
        differenceResults = new ContainerDifference( vTotal, pCount, simCount );
        diffThreads = CalculatorFactory.generateDifference( vTotal,
                                                            pCount,
                                                            quota1,
                                                            quota2,
                                                            simCount,
                                                            valueSimCount,
                                                            differenceResults );
    }

    void startAllThreads() {
        startTime = System.nanoTime();
        for ( Thread t : diffThreads ) {
            t.setDaemon( true );
            t.start();
        }
    }

    void stopAllThreads() {
        for ( Thread t : diffThreads ) {
            t.interrupt();
        }

        runningTime = System.nanoTime() - startTime;
        Arrays.fill( diffThreads, null );
    }

    void waitForThreads() throws InterruptedException {
        for ( Thread t : diffThreads ) {
            t.join();
        }
        runningTime = System.nanoTime() - startTime;
        Arrays.fill( diffThreads, null );
    }

    public BigDecimal[] getShapleyPos() { return differenceResults.getShapleyPos(); }

    public BigDecimal[] getShapleyZer() { return differenceResults.getShapleyZer(); }

    public BigDecimal[] getShapleyNeg() { return differenceResults.getShapleyNeg(); }

    public BigDecimal[] getBanzhafPos() { return differenceResults.getBanzhafPos(); }

    public BigDecimal[] getBanzhafZer() { return differenceResults.getBanzhafZer(); }

    public BigDecimal[] getBanzhafNeg() { return differenceResults.getBanzhafNeg(); }

    public ContainerDifference getContainer() { return differenceResults; }

    public double getRunningTime() {
        return TimeUnit.SECONDS.convert( runningTime, TimeUnit.NANOSECONDS );
    }
}
