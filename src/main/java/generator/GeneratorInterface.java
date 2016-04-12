package generator;

/**
 * Created by Márton Rajnai on 2016-04-04.
 * Contact: marton.rajnai@gmail.com
 */

/**
 * Simple interface for a generator
 */
interface GeneratorInterface {

    int[] getNext();

    int[] getNext( int p );

}
