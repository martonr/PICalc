package calculator;

import container.ContainerResults;
import generator.GeneratorCombination;
import generator.GeneratorPermutation;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Created by Márton Rajnai on 2016-04-05.
 * Contact: marton.rajnai@gmail.com
 */

/**
 * Holds a power index calculation.
 */
public final class CalculatorSimple {

    /**
     * calculationResult - records the results
     * shapleyThreads - holds the threads for the Shapley-Shubik calculation
     * banzhafThreads - holds the threads for the Banzhaf calculation
     * startTime - time when the threads were started
     * runningTime - time when the threads ended
     */
    private final ContainerResults calculationResult;

    private final Thread[] shapleyThreads;
    private final Thread[] banzhafThreads;

    private long startTime;
    private long runningTime;

    /**
     * Create a new power index calculation.
     *
     * @param pVotes   - integer array holding the player votes
     * @param quota    - number of votes required to pass
     * @param simCount - if <=0, an exact values is calculated, if >0 a Monte Carlo method is used
     */
    public CalculatorSimple( int[] pVotes, int quota, int simCount ) {
        calculationResult = new ContainerResults( pVotes.length, simCount );

        GeneratorPermutation pGen = new GeneratorPermutation( pVotes.length, false );
        GeneratorCombination cGen = new GeneratorCombination( pVotes.length, false );

        shapleyThreads = CalculatorFactory.generateShapleyShubik( pVotes, quota, simCount, pGen, calculationResult );
        banzhafThreads = CalculatorFactory.generateBanzhaf( pVotes, quota, simCount, cGen, calculationResult );
    }

    void startAllThreads() {
        startTime = System.nanoTime();
        for ( Thread t : shapleyThreads ) {
            t.setDaemon( true );
            t.start();
        }

        for ( Thread t : banzhafThreads ) {
            t.setDaemon( true );
            t.start();
        }
    }

    void stopAllThreads() {
        for ( Thread t : shapleyThreads ) {
            t.interrupt();
        }

        for ( Thread t : banzhafThreads ) {
            t.interrupt();
        }

        runningTime = System.nanoTime() - startTime;

        Arrays.fill( shapleyThreads, null );
        Arrays.fill( banzhafThreads, null );
    }

    void waitForThreads() throws InterruptedException {
        for ( Thread t : shapleyThreads ) {
            t.join();
        }

        for ( Thread t : banzhafThreads ) {
            t.join();
        }

        runningTime = System.nanoTime() - startTime;

        Arrays.fill( shapleyThreads, null );
        Arrays.fill( banzhafThreads, null );
    }

    public BigDecimal[] getShapleyValues() { return calculationResult.getShapleyIndex(); }

    public BigDecimal[] getBanzhafValues() { return calculationResult.getBanzhafIndex(); }

    public ContainerResults getContainer() { return calculationResult; }

    public double getRunningTime() {
        return TimeUnit.SECONDS.convert( runningTime, TimeUnit.NANOSECONDS );
    }
}
