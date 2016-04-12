package generator;

/**
 * Created by Márton Rajnai on 2016-04-04.
 * Contact: marton.rajnai@gmail.com
 */

/**
 * Abstract generator class holds a static method that all subclasses use.
 */
abstract class GeneratorAbstract implements GeneratorInterface {

    static void swap( int[] array, int i, int j ) {
        // XOR swap
        if ( i == j )
            return;
        array[ i ] ^= array[ j ];
        array[ j ] ^= array[ i ];
        array[ i ] ^= array[ j ];
    }
}
