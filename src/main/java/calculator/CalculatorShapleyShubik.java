package calculator;

import container.ContainerResults;
import generator.GeneratorPermutation;

/**
 * Created by Márton Rajnai on 2016-04-04.
 * Contact: marton.rajnai@gmail.com
 */

/**
 * The calculator class that computes the Shapley-Shubik power index
 * Depending on the parameters it can calculate the exact
 * or a Monte Carlo value.
 */
final class CalculatorShapleyShubik extends CalculatorAbstract {

    /**
     * permGen - a permutation generator
     * resultHolder - a container that records the results
     * pivotCount - array tha holds the player pivot counts during the calculation
     * playerVotes - holds the initial player votes
     * quotaWin - the vote count required for a coalition to win
     * simulationCount - if 0, an exact values is calculated, if >0 a Monte Carlo method is used
     */
    private final GeneratorPermutation permGen;
    private final ContainerResults resultHolder;

    private final int[] pivotCount;
    private final int[] playerVotes;

    private final int quotaWin;
    private final int simulationCount;

    /**
     * Create a new Banzhaf power index calculator
     *
     * @param pVotes   - integer array holding the player vote counts
     * @param quota    - the votes required to pass
     * @param simCount - if <=0, an exact values is calculated, if >0 a Monte Carlo method is used
     * @param pG       - a permutation generator
     * @param r        - a result container
     */
    CalculatorShapleyShubik( int[] pVotes, int quota, int simCount, GeneratorPermutation pG, ContainerResults r ) {
        playerVotes = pVotes.clone();
        int playerCount = playerVotes.length;
        quotaWin = quota;
        simulationCount = ( simCount <= 0 ) ? 0 : simCount;
        resultHolder = r;

        pivotCount = new int[ playerCount ];

        if ( simulationCount <= 0 )
            permGen = pG;
        else
            permGen = new GeneratorPermutation( playerCount, true );
    }

    @Override
    void calculate() {
        int voteCounter, simCounter = 0;

        // Get a new permutation
        int[] permutation = permGen.getNext();

        while ( permutation != null
                && ( simCounter < simulationCount || simulationCount == 0 )
                && !( Thread.currentThread().isInterrupted() ) ) {
            voteCounter = 0;

            // Check if player is pivot
            for ( int i : permutation ) {
                voteCounter += playerVotes[ i ];
                if ( voteCounter >= quotaWin ) {
                    pivotCount[ i ]++;
                    break;
                }
            }
            simCounter++;

            // Get a new permutation
            permutation = permGen.getNext();
        }
    }

    @Override
    void record() {
        resultHolder.updateShapleyShubik( pivotCount );
    }

    @Override
    public void run() {
        calculate();
        record();
    }
}
