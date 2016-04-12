package container;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Created by Márton Rajnai on 2016-04-04.
 * Contact: marton.rajnai@gmail.com
 */

/**
 * Holds and calculates the results of power index calculations.
 */
public class ContainerResults {

    private final static long[] FACTORIAL_TABLE = { 1L, 1L, 2L, 6L, 24L, 120L, 720L, 5040L, 40320L,
                                                    362880L, 3628800L, 39916800L, 479001600L, 6227020800L, 87178291200L,
                                                    1307674368000L, 20922789888000L, 355687428096000L, 6402373705728000L,
                                                    121645100408832000L, 2432902008176640000L };

    private final Object PIVOT_LOCK = new Object();
    private final Object CRITICAL_LOCK = new Object();

    private final BigDecimal[] shapleyIndex;
    private final BigDecimal[] banzhafIndex;
    private final int[] pivotCount;
    private final int[] criticalCount;

    private final long allPermutation;
    private final int playerCount;

    private boolean sComputed = false;
    private boolean bComputed = false;

    /**
     * Create new container for a power index calculation.
     *
     * @param pCount   - player count
     * @param simCount - simulation count for the power indices
     */
    public ContainerResults( int pCount, int simCount ) {
        playerCount = pCount;
        allPermutation = ( simCount > 0 ) ? ( long ) simCount : FACTORIAL_TABLE[ playerCount ];

        pivotCount = new int[ playerCount ];
        criticalCount = new int[ playerCount ];
        shapleyIndex = new BigDecimal[ playerCount ];
        banzhafIndex = new BigDecimal[ playerCount ];
    }

    public void updateShapleyShubik( int[] pivotCount ) {
        if ( pivotCount.length != playerCount )
            return;
        synchronized ( PIVOT_LOCK ) {
            for ( int i = 0; i < playerCount; ++i ) {
                this.pivotCount[ i ] += pivotCount[ i ];
            }
        }
    }

    public void updateBanzhaf( int[] criticalCount ) {
        if ( criticalCount.length != playerCount )
            return;
        synchronized ( CRITICAL_LOCK ) {
            for ( int i = 0; i < playerCount; ++i ) {
                this.criticalCount[ i ] += criticalCount[ i ];
            }
        }
    }

    public BigDecimal[] getShapleyIndex() {
        if ( sComputed )
            return shapleyIndex;

        MathContext m = new MathContext( 4 );
        BigDecimal d = new BigDecimal( allPermutation );

        for ( int i = 0; i < playerCount; ++i ) {
            shapleyIndex[ i ] = ( new BigDecimal( pivotCount[ i ] ) ).divide( d, m );
        }

        sComputed = true;
        return shapleyIndex;
    }

    public BigDecimal[] getBanzhafIndex() {
        if ( bComputed )
            return banzhafIndex;

        int allCritical = 0;

        for ( int i : criticalCount )
            allCritical += i;

        if ( allCritical == 0 )
            allCritical = 1;

        MathContext m = new MathContext( 4 );
        BigDecimal d = new BigDecimal( allCritical );

        for ( int i = 0; i < playerCount; ++i ) {
            banzhafIndex[ i ] = ( new BigDecimal( criticalCount[ i ] ) ).divide( d, m );
        }

        bComputed = true;
        return banzhafIndex;
    }

    @Override
    public String toString() {
        if ( !sComputed )
            getShapleyIndex();
        if ( !bComputed )
            getBanzhafIndex();

        StringBuilder s = new StringBuilder();

        for ( int i = 0; i < shapleyIndex.length; ++i ) {
            s.append( i + 1 ).append( ": " ).append( shapleyIndex[ i ].toString() ).append( " " );
        }
        s.append( "\n" );
        for ( int i = 0; i < banzhafIndex.length; ++i ) {
            s.append( i + 1 ).append( ": " ).append( banzhafIndex[ i ].toString() ).append( " " );
        }
        s.append( "\n" );

        return s.toString();
    }
}
