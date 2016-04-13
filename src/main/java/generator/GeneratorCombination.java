package generator;

import java.util.BitSet;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Márton Rajnai on 2016-04-04.
 * Contact: marton.rajnai@gmail.com
 */

/**
 * Combination generator class, can be used to generate random or
 * all possible combinations for a number of players.
 */
public final class GeneratorCombination extends GeneratorAbstract {

    private final Object COMBINATION_LOCK = new Object();

    private final int playerCount;
    private final boolean isRandom;
    private final int[] playerPool;
    private final BitSet randomCombination;

    private int[] combination;
    private int combinationSize;

    private boolean noneVisited = true;
    private boolean allVisited = false;

    /**
     * Create a new combination generator
     *
     * @param pCount - number of players
     * @param isRand - if true generate random combinations, else all possible combinations
     */
    public GeneratorCombination( int pCount, boolean isRand ) {
        playerCount = pCount;
        isRandom = isRand;
        playerPool = new int[ playerCount ];
        combination = new int[ playerCount ];
        combinationSize = playerCount;
        randomCombination = new BitSet( playerCount );

        if ( isRandom ) {
            for ( int i = 0; i < playerCount; ++i ) {
                playerPool[ i ] = i;
            }
        } else {
            for ( int i = 0; i < playerCount; ++i ) {
                playerPool[ i ] = i;
                combination[ i ] = i;
            }
        }
    }

    /**
     * Returns a combination, if all possible combinations were returned, returns null.
     *
     * @return - integer array representing a combination
     */
    @Override
    public int[] getNext() { return getNext( -1 ); }

    /**
     * Returns a combination, if all possible combinations were returned, returns null.
     *
     * @param p - this player is always part of the combination, if <0 empty combinations are also returned
     * @return - integer array representing a combination
     */
    @Override
    public int[] getNext( int p ) {
        synchronized ( COMBINATION_LOCK ) {
            if ( allVisited )
                return null;

            if ( isRandom )
                nextRandom( p );
            else
                nextNormal();

            if ( allVisited )
                return null;

            return combination.clone();
        }
    }

    private void nextRandom( int p ) {
        // Generates a random combination, p player is always set in the combination
        synchronized ( COMBINATION_LOCK ) {
            randomCombination.clear();
            for ( int i = 0; i < playerCount; ++i ) {
                if ( ThreadLocalRandom.current().nextBoolean() ) {
                    randomCombination.set( i );
                }
            }
            if ( p >= 0 )
                randomCombination.set( p );
            combinationSize = randomCombination.cardinality();
            combination = new int[ combinationSize ];

            int j = 0;
            for ( int i = 0; i < playerCount; ++i ) {
                if ( randomCombination.get( i ) ) {
                    combination[ j++ ] = playerPool[ i ];
                }
            }
        }
    }

    private void nextNormal() {
        // Constructs the next combination, if all were visited, returns null
        synchronized ( COMBINATION_LOCK ) {
            if ( noneVisited ) {
                noneVisited = false;
                return;
            }

            if ( combinationSize < 1 ) {
                allVisited = true;
                return;
            }

            if ( combination[ 0 ] == playerCount - combinationSize ) {
                --combinationSize;

                combination = new int[ combinationSize ];

                for ( int i = 0; i < combinationSize; ++i )
                    combination[ i ] = playerPool[ i ];

                return;
            }

            int idx = combinationSize - 1;

            while ( idx > 0 && combination[ idx ] == playerCount - combinationSize + idx )
                --idx;

            ++combination[ idx ];

            for ( int j = idx; j < combinationSize - 1; ++j )
                combination[ j + 1 ] = combination[ j ] + 1;
        }
    }
}
