package calculator;

import javafx.concurrent.Task;

/**
 * Created by Márton Rajnai on 2016-04-06.
 * Contact: marton.rajnai@gmail.com
 */

/**
 * A task that executes a change effect simulation, and waits for the results, then it updates its value
 * that is used to signal the application that the task is finished.
 */
public class CalculatorComplexTask extends Task< Boolean > {

    private final CalculatorComplex complexCalculator;

    /**
     * Create a new simulation task.
     *
     * @param c - a simulation calculator
     */
    public CalculatorComplexTask( CalculatorComplex c ) {
        complexCalculator = c;
    }

    @Override
    protected Boolean call() throws Exception {
        complexCalculator.startAllThreads();

        try {
            complexCalculator.waitForThreads();
        } catch ( InterruptedException e ) {
            complexCalculator.stopAllThreads();
            return false;
        }

        this.updateValue( true );

        return true;
    }
}
