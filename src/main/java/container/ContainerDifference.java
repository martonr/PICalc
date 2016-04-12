package container;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;

/**
 * Created by Márton Rajnai on 2016-04-06.
 * Contact: marton.rajnai@gmail.com
 */

/**
 * Holds and computes the results of a change effect simulation.
 */
public final class ContainerDifference {

    private final Object RECORD_LOCK = new Object();

    private final BigDecimal[] shapleyPositive;
    private final BigDecimal[] shapleyZero;
    private final BigDecimal[] shapleyNegative;

    private final BigDecimal[] banzhafPositive;
    private final BigDecimal[] banzhafZero;
    private final BigDecimal[] banzhafNegative;

    private final int simulationCount;
    private boolean calculated = false;

    /**
     * Create a new container for a simulation.
     *
     * @param voteCount - number of votes available
     * @param pCount    - number of players
     * @param sCount    - number of scenarios per vote
     */
    public ContainerDifference( int voteCount, int pCount, int sCount ) {
        simulationCount = sCount;

        shapleyPositive = new BigDecimal[ voteCount - pCount + 1 ];
        shapleyZero = new BigDecimal[ voteCount - pCount + 1 ];
        shapleyNegative = new BigDecimal[ voteCount - pCount + 1 ];

        banzhafPositive = new BigDecimal[ voteCount - pCount + 1 ];
        banzhafZero = new BigDecimal[ voteCount - pCount + 1 ];
        banzhafNegative = new BigDecimal[ voteCount - pCount + 1 ];

        Arrays.fill( shapleyPositive, new BigDecimal( 0 ) );
        Arrays.fill( shapleyZero, new BigDecimal( 0 ) );
        Arrays.fill( shapleyNegative, new BigDecimal( 0 ) );

        Arrays.fill( banzhafPositive, new BigDecimal( 0 ) );
        Arrays.fill( banzhafZero, new BigDecimal( 0 ) );
        Arrays.fill( banzhafNegative, new BigDecimal( 0 ) );
    }

    /**
     * Record the results of a simulation.
     *
     * @param results - integer array holding the results
     * @param i - scenario vote count for the results
     */
    public void record( int[] results, int i ) {
        synchronized ( RECORD_LOCK ) {
            shapleyPositive[ i ] = shapleyPositive[ i ].add( new BigDecimal( results[ 0 ] ) );
            shapleyNegative[ i ] = shapleyNegative[ i ].add( new BigDecimal( results[ 1 ] ) );
            shapleyZero[ i ] = shapleyZero[ i ].add( new BigDecimal( results[ 2 ] ) );

            banzhafPositive[ i ] = banzhafPositive[ i ].add( new BigDecimal( results[ 3 ] ) );
            banzhafNegative[ i ] = banzhafNegative[ i ].add( new BigDecimal( results[ 4 ] ) );
            banzhafZero[ i ] = banzhafZero[ i ].add( new BigDecimal( results[ 5 ] ) );
        }
    }

    private void calculatePercents() {
        if ( calculated )
            return;

        MathContext m = new MathContext( 5 );
        BigDecimal d = new BigDecimal( simulationCount, m );

        for ( int i = 0; i < shapleyPositive.length; ++i ) {
            shapleyPositive[ i ] = ( shapleyPositive[ i ].divide( d, m ) );
            shapleyZero[ i ] = ( shapleyZero[ i ].divide( d, m ) );
            shapleyNegative[ i ] = ( shapleyNegative[ i ].divide( d, m ) );

            banzhafPositive[ i ] = ( banzhafPositive[ i ].divide( d, m ) );
            banzhafZero[ i ] = ( banzhafZero[ i ].divide( d, m ) );
            banzhafNegative[ i ] = ( banzhafNegative[ i ].divide( d, m ) );
        }

        calculated = true;
    }

    public BigDecimal[] getShapleyPos() {
        calculatePercents();
        return shapleyPositive;
    }

    public BigDecimal[] getShapleyZer() {
        calculatePercents();
        return shapleyZero;
    }

    public BigDecimal[] getShapleyNeg() {
        calculatePercents();
        return shapleyNegative;
    }

    public BigDecimal[] getBanzhafPos() {
        calculatePercents();
        return banzhafPositive;
    }

    public BigDecimal[] getBanzhafZer() {
        calculatePercents();
        return banzhafZero;
    }

    public BigDecimal[] getBanzhafNeg() {
        calculatePercents();
        return banzhafNegative;
    }

    @Override
    public String toString() {
        calculatePercents();
        StringBuilder s = new StringBuilder();

        for ( int i = 0; i < shapleyPositive.length; ++i ) {
            s.append( i + 1 ).append( ": " ).append( shapleyPositive[ i ].toString() ).append( " " );
        }
        s.append( "\n" );
        for ( int i = 0; i < shapleyZero.length; ++i ) {
            s.append( i + 1 ).append( ": " ).append( shapleyZero[ i ].toString() ).append( " " );
        }
        s.append( "\n" );
        for ( int i = 0; i < shapleyNegative.length; ++i ) {
            s.append( i + 1 ).append( ": " ).append( shapleyNegative[ i ].toString() ).append( " " );
        }
        s.append( "\n" );
        for ( int i = 0; i < banzhafPositive.length; ++i ) {
            s.append( i + 1 ).append( ": " ).append( banzhafPositive[ i ].toString() ).append( " " );
        }
        s.append( "\n" );
        for ( int i = 0; i < banzhafZero.length; ++i ) {
            s.append( i + 1 ).append( ": " ).append( banzhafZero[ i ].toString() ).append( " " );
        }
        s.append( "\n" );
        for ( int i = 0; i < banzhafNegative.length; ++i ) {
            s.append( i + 1 ).append( ": " ).append( banzhafNegative[ i ].toString() ).append( " " );
        }
        s.append( "\n" );

        return s.toString();
    }
}
