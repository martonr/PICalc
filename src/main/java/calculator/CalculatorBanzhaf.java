package calculator;

import container.ContainerResults;
import generator.GeneratorCombination;

/**
 * Created by Márton Rajnai on 2016-04-04.
 * Contact: marton.rajnai@gmail.com
 */

/**
 * The calculator class that computes the Banzhaf power index
 * Depending on the parameters it can calculate the exact
 * or a Monte Carlo value.
 */
final class CalculatorBanzhaf extends CalculatorAbstract {

    /**
     * combGen - a combination generator
     * resultHolder - a container that records the results
     * criticalCount - array tha holds the swing vote counts during the calculation
     * playerVotes - holds the initial player votes
     * playerCount - the number of players
     * quotaWin - the vote count required for a coalition to win
     * quotaBlock - the vote count required for the coalition to prevent the opposing from winning
     * simulationCount - if 0, an exact values is calculated, if >0 a Monte Carlo method is used
     */
    private final GeneratorCombination combGen;
    private final ContainerResults resultHolder;

    private final int[] criticalCount;
    private final int[] playerVotes;

    private final int playerCount;
    private final int quotaWin;
    private final int quotaBlock;
    private final int simulationCount;

    /**
     * Create a new Banzhaf power index calculator
     *
     * @param pVotes   - integer array holding the player vote counts
     * @param quota    - the votes required to pass
     * @param simCount - if <=0, an exact values is calculated, if >0 a Monte Carlo method is used
     * @param cG       - a combination generator
     * @param c        - a result container
     */
    CalculatorBanzhaf( int[] pVotes, int quota, int simCount, GeneratorCombination cG, ContainerResults c ) {
        playerVotes = pVotes.clone();
        playerCount = playerVotes.length;
        quotaWin = quota;
        simulationCount = ( simCount <= 0 ) ? 0 : simCount;
        resultHolder = c;

        // Calculate the quota to block
        int total = 0;
        for ( int i : pVotes )
            total += i;
        quotaBlock = total - quotaWin + 1;

        criticalCount = new int[ playerCount ];

        if ( simulationCount <= 0 )
            combGen = cG;
        else
            // Get a random combination generator
            combGen = new GeneratorCombination( playerCount, true );
    }

    @Override
    void calculate() {
        int voteCounter, simCounter = 0;
        // Get the next combination
        int[] combination = combGen.getNext( simCounter % playerCount );

        while ( combination != null &&
                ( simCounter < simulationCount || simulationCount == 0 ) &&
                !( Thread.currentThread().isInterrupted() ) ) {
            voteCounter = 0;

            for ( int i : combination )
                voteCounter += playerVotes[ i ];

            // Check if the player is critical
            for ( int i : combination ) {
                if ( voteCounter >= quotaWin ) {
                    if ( voteCounter - playerVotes[ i ] < quotaWin )
                        criticalCount[ i ]++;
                }

                if ( voteCounter >= quotaBlock ) {
                    if ( voteCounter - playerVotes[ i ] < quotaBlock )
                        criticalCount[ i ]++;
                }
            }
            simCounter++;
            // Get the next combination
            combination = combGen.getNext( simCounter % playerCount );
        }
    }

    @Override
    void record() {
        resultHolder.updateBanzhaf( criticalCount );
    }

    @Override
    public void run() {
        calculate();
        record();
    }
}
