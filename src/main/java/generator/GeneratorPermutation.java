package generator;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Márton Rajnai on 2016-04-04.
 * Contact: marton.rajnai@gmail.com
 */

/**
 * Permutation generator class, can be used to generate random or
 * all possible permutations for a number of players.
 */
public final class GeneratorPermutation extends GeneratorAbstract {

    private final Object PERMUTATION_LOCK = new Object();

    private final int playerCount;
    private final boolean isRandom;

    private final int[] permutation;
    private final int[] auxArray;

    private boolean noneVisited = true;
    private boolean allVisited = false;

    /**
     * Create a new permuation generator.
     *
     * @param pCount - number of players
     * @param isRand - if true, generates random permutations, else all permutations
     */
    public GeneratorPermutation( int pCount, boolean isRand ) {
        playerCount = pCount;
        isRandom = isRand;
        permutation = new int[ playerCount ];

        if ( isRandom ) {
            auxArray = null;
            for ( int i = 0; i < playerCount; ++i ) {
                permutation[ i ] = i;
            }
        } else {
            auxArray = new int[ playerCount ];
            for ( int i = 0; i < playerCount; ++i ) {
                permutation[ i ] = i;
                auxArray[ i ] = 0;
            }
        }
    }

    /**
     * Get the next permutation.
     *
     * @return - integer array holding the permutation
     */
    @Override
    public int[] getNext() {
        synchronized ( PERMUTATION_LOCK ) {
            if ( allVisited )
                return null;

            if ( isRandom )
                nextRandom();
            else
                nextNormal();

            if ( allVisited )
                return null;

            return permutation.clone();
        }
    }

    /**
     * same as getNext().
     *
     * @param p - not used.
     * @return - integer array holding the permutation.
     */
    @Override
    public int[] getNext( int p ) { return getNext(); }

    private void nextRandom() {
        // Shuffle the permutation randomly

        synchronized ( PERMUTATION_LOCK ) {
            for ( int j = playerCount; j > 1; --j ) {
                swap( permutation, j - 1, ThreadLocalRandom.current().nextInt( j ) );
            }
        }
    }

    private void nextNormal() {
        // Generate the next possible permutation, if all visited return null
        synchronized ( PERMUTATION_LOCK ) {
            if ( noneVisited ) {
                noneVisited = false;
                return;
            }

            // Heap's algorithm
            for ( int i = 1; i < playerCount; ) {
                if ( auxArray[ i ] < i ) {
                    swap( permutation, ( i % 2 ) * auxArray[ i ], i );
                    auxArray[ i ]++;
                    return;
                } else {
                    auxArray[ i++ ] = 0;
                }
            }
            allVisited = true;
        }
    }
}
