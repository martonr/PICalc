package calculator;

import container.ContainerDifference;
import container.ContainerResults;
import generator.GeneratorCombination;
import generator.GeneratorPermutation;

/**
 * Created by Márton Rajnai on 2016-04-04.
 * Contact: marton.rajnai@gmail.com
 */

/**
 * A simple factory that generates threads for calculations.
 * The methods are static and the constructor is private.
 */
final class CalculatorFactory {

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    private CalculatorFactory() {}

    static Thread[] generateShapleyShubik( int[] pV, int qW, int sC, GeneratorPermutation pG, ContainerResults c ) {
        int threadCount = ( sC / CPU_COUNT );
        int lastThreadCount = threadCount + ( sC % CPU_COUNT );
        Thread[] threadArray = new Thread[ CPU_COUNT ];

        for ( int i = 0; i < ( CPU_COUNT - 1 ); ++i ) {
            threadArray[ i ] = new Thread( new CalculatorShapleyShubik( pV,
                                                                        qW,
                                                                        threadCount,
                                                                        pG,
                                                                        c ) );
        }

        threadArray[ CPU_COUNT - 1 ] = new Thread( new CalculatorShapleyShubik( pV,
                                                                                qW,
                                                                                lastThreadCount,
                                                                                pG,
                                                                                c ) );

        return threadArray;
    }

    static Thread[] generateBanzhaf( int[] pV, int qW, int sC, GeneratorCombination cG, ContainerResults c ) {
        int threadCount = ( sC / CPU_COUNT );
        int lastThreadCount = threadCount + ( sC % CPU_COUNT );
        int extra = threadCount % pV.length;
        threadCount = threadCount - extra;
        lastThreadCount = lastThreadCount + ( ( CPU_COUNT - 1 ) * extra );
        Thread[] threadArray = new Thread[ CPU_COUNT ];

        for ( int i = 0; i < ( CPU_COUNT - 1 ); ++i ) {
            threadArray[ i ] = new Thread( new CalculatorBanzhaf( pV,
                                                                  qW,
                                                                  threadCount,
                                                                  cG,
                                                                  c ) );
        }

        threadArray[ CPU_COUNT - 1 ] = new Thread( new CalculatorBanzhaf( pV,
                                                                          qW,
                                                                          lastThreadCount,
                                                                          cG,
                                                                          c ) );

        return threadArray;
    }

    static Thread[] generateDifference( int tV, int pC, int fQ, int sQ, int sSC, int vSC, ContainerDifference c ) {
        int threadCount = ( sSC / CPU_COUNT );
        int lastThreadCount = threadCount + ( sSC % CPU_COUNT );
        Thread[] threadArray = new Thread[ CPU_COUNT ];

        for ( int i = 0; i < ( CPU_COUNT - 1 ); ++i ) {
            threadArray[ i ] = new Thread( new CalculatorDifference( tV,
                                                                     pC,
                                                                     fQ,
                                                                     sQ,
                                                                     threadCount,
                                                                     vSC,
                                                                     c ) );
        }

        threadArray[ CPU_COUNT - 1 ] = new Thread( new CalculatorDifference( tV,
                                                                             pC,
                                                                             fQ,
                                                                             sQ,
                                                                             lastThreadCount,
                                                                             vSC,
                                                                             c ) );
        return threadArray;
    }
}
