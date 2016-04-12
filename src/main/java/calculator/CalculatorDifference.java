package calculator;

import container.ContainerDifference;
import generator.GeneratorPermutation;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Márton Rajnai on 2016-04-06.
 * Contact: marton.rajnai@gmail.com
 */

/**
 * A class that computes power index values for generated
 * scenarios, used for the power index change simulation.
 */
final class CalculatorDifference extends CalculatorAbstract {

    /**
     * permGen - a permutation generator
     * differenceHolder - used to record the results
     * voteTotal - total votes available
     * playerCount - the number of players
     * scenarioSimCount - how many scenarios to generate for each vote
     * valueSimCount - used for the power index calculation
     * firstQuota - the quota to measure the change FROM
     * secondQuota - the quota to measure the change TO
     * <p>
     * currentVotes - holds the votes for the current scenario
     * currentScenario - holds the players for the current scenario
     * currentResults - temporary storage for the current change effects
     */
    private final GeneratorPermutation permGen;
    private final ContainerDifference differenceHolder;

    private final int voteTotal;
    private final int playerCount;
    private final int scenarioSimCount;
    private final int valueSimCount;
    private final int firstQuota;
    private final int secondQuota;

    private int[] currentVotes;
    private int[] currentScenario;
    private int[] currentResults;

    /**
     * Create a new power effect change simulation.
     *
     * @param vTotal - total votes available
     * @param pCount - the number of players
     * @param quota1 - the quota to measure the change FROM
     * @param quota2 - the quota to measure the change TO
     * @param simCount - how many scenarios to generate for each vote
     * @param vSimCount - used for the power index calculation
     * @param c - used to record the results
     */
    CalculatorDifference( int vTotal,
                          int pCount,
                          int quota1,
                          int quota2,
                          int simCount,
                          int vSimCount,
                          ContainerDifference c ) {
        voteTotal = vTotal;
        playerCount = pCount;
        firstQuota = quota1;
        secondQuota = quota2;
        scenarioSimCount = simCount;
        valueSimCount = vSimCount;
        currentVotes = new int[ playerCount ];
        currentScenario = new int[ playerCount ];
        currentResults = new int[ 6 ];

        // Get a random permutation generator
        permGen = new GeneratorPermutation( playerCount - 1, true );
        differenceHolder = c;
    }

    private void generateScenario( int x ) {
        int distributedVotes = x + playerCount - 1;
        int v;
        currentVotes[ 0 ] = x;
        currentScenario = permGen.getNext();

        // Distribute the votes to the players in a random order
        for ( int i = 0; i < ( playerCount - 2 ); ++i ) {
            v = ThreadLocalRandom.current().nextInt( voteTotal - distributedVotes + 1 );
            currentVotes[ currentScenario[ i ] + 1 ] = 1 + v;
            distributedVotes += v;
        }

        currentVotes[ currentScenario[ playerCount - 2 ] + 1 ] = 1 + voteTotal - distributedVotes;
    }

    private void recordScenarioResults( BigDecimal sV1, BigDecimal sV2, BigDecimal bV1, BigDecimal bV2 ) {
        MathContext m = new MathContext( 5 );

        BigDecimal shapleyDiff = ( sV1.subtract( sV2, m ) );
        BigDecimal banzhafDiff = ( bV1.subtract( bV2, m ) );

        if ( shapleyDiff.compareTo( BigDecimal.ZERO ) > 0 ) {
            currentResults[ 0 ]++;
        } else if ( shapleyDiff.compareTo( BigDecimal.ZERO ) < 0 ) {
            currentResults[ 1 ]++;
        } else {
            currentResults[ 2 ]++;
        }

        if ( banzhafDiff.compareTo( BigDecimal.ZERO ) > 0 ) {
            currentResults[ 3 ]++;
        } else if ( banzhafDiff.compareTo( BigDecimal.ZERO ) < 0 ) {
            currentResults[ 4 ]++;
        } else {
            currentResults[ 5 ]++;
        }
    }

    private boolean calculateAndWait( CalculatorSimple c ) {
        c.startAllThreads();
        try {
            c.waitForThreads();
        } catch ( InterruptedException e ) {
            c.stopAllThreads();
            return true;
        }
        return false;
    }

    @Override
    void calculate() {
        BigDecimal s1, s2, b1, b2;
        CalculatorSimple c;

        for ( int i = 0; i < voteTotal - playerCount + 1; ++i ) {
            for ( int j = 0; j < scenarioSimCount; ++j ) {

                generateScenario( i + 1 );

                // First quota
                c = new CalculatorSimple( currentVotes, firstQuota, valueSimCount );
                if ( calculateAndWait( c ) )
                    return;
                s1 = c.getShapleyValues()[ 0 ];
                b1 = c.getBanzhafValues()[ 0 ];

                // Second quota
                c = new CalculatorSimple( currentVotes, secondQuota, valueSimCount );
                if ( calculateAndWait( c ) )
                    return;
                s2 = c.getShapleyValues()[ 0 ];
                b2 = c.getBanzhafValues()[ 0 ];

                recordScenarioResults( s1, s2, b1, b2 );
            }
            differenceHolder.record( currentResults, i );
            Arrays.fill( currentResults, 0 );
        }
    }

    @Override
    void record() { }

    @Override
    public void run() {
        calculate();
    }
}