package calculator;

/**
 * Created by Márton Rajnai on 2016-04-04.
 * Contact: marton.rajnai@gmail.com
 */

/**
 * This abstract class declares the common methods
 * all Calculators must implement.
 */
abstract class CalculatorAbstract implements Runnable {

    abstract void calculate();

    abstract void record();

}
