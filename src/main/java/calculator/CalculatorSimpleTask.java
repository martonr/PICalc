package calculator;

import javafx.concurrent.Task;

/**
 * Created by Márton Rajnai on 2016-04-05.
 * Contact: marton.rajnai@gmail.com
 */

/**
 * A task that executes a power index calculation, and waits for the results, then it updates its value
 * that is used to signal the application that the task is finished.
 */
public final class CalculatorSimpleTask extends Task< Boolean > {

    private final CalculatorSimple simpleCalculator;

    /**
     * Create a new power index task.
     *
     * @param c - a power index calculator
     */
    public CalculatorSimpleTask( CalculatorSimple c ) {
        simpleCalculator = c;
    }

    @Override
    protected Boolean call() throws Exception {
        simpleCalculator.startAllThreads();

        try {
            simpleCalculator.waitForThreads();
        } catch ( InterruptedException e ) {
            simpleCalculator.stopAllThreads();
            return false;
        }

        this.updateValue( true );

        return true;
    }
}
