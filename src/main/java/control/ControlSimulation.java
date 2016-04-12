package control;

import calculator.CalculatorComplex;
import calculator.CalculatorComplexTask;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.input.MouseEvent;

import java.math.BigDecimal;

/**
 * Created by Márton Rajnai on 2016-04-08.
 * Contact: marton.rajnai@gmail.com
 */

/**
 * Simulation controller used for the simulation functionality.
 */
public final class ControlSimulation {

    @FXML
    private ToggleGroup computationType;
    @FXML
    private ProgressBar simulationProgress;
    @FXML
    private TextField playerCount;
    @FXML
    private TextField totalVote;
    @FXML
    private TextField firstQuota;
    @FXML
    private TextField secondQuota;
    @FXML
    private TextField scenarioSim;
    @FXML
    private TextField valueSim;
    @FXML
    private Button startSimulation;
    @FXML
    private Button stopSimulation;
    @FXML
    private ScatterChart< String, Double > shapleyChart;
    @FXML
    private ScatterChart< String, Double > banzhafChart;
    @FXML
    private NumberAxis sYAxis;
    @FXML
    private NumberAxis bYAxis;
    @FXML
    private Label timeMeasure;

    private final ColorAdjust errorColor = new ColorAdjust( 0.03, 0.05, 0.0, 0.0 );
    private final SimpleBooleanProperty finishedFlag = new SimpleBooleanProperty( true );

    private CalculatorComplex complexCalc;
    private Thread simThread;
    private boolean interrupted = false;

    public void initialize() {
        initButtons();
        initTextFields();
        initToggle();
        initProgress();
        initFlags();
        initCharts();
        initLabels();
    }

    private void initButtons() {
        startSimulation.setOnAction( event -> simulate() );

        stopSimulation.setOnAction( event -> interruptSim() );

        stopSimulation.setDisable( true );
    }

    private void initTextFields() {
        playerCount.focusedProperty().addListener( ( observable, oldValue, newValue ) -> {
            if ( newValue ) {
                resetTextField( playerCount, "Player Count" );
            }

            if ( oldValue ) {
                int v = parseNumber( playerCount );
                if ( v > 8 ) {
                    computationType.getToggles().get( 1 ).setSelected( true );
                    ( ( RadioButton ) computationType.getToggles().get( 0 ) ).setDisable( true );
                } else {
                    ( ( RadioButton ) computationType.getToggles().get( 0 ) ).setDisable( false );
                }
            }
        } );

        totalVote.focusedProperty().addListener( ( observable, oldValue, newValue ) -> {
            if ( newValue ) {
                resetTextField( totalVote, "Total Votes" );
            }

            if ( oldValue ) {
                parseNumber( totalVote );
            }
        } );

        firstQuota.focusedProperty().addListener( ( observable, oldValue, newValue ) -> {
            if ( newValue ) {
                resetTextField( firstQuota, "Upper Quota" );
            }

            if ( oldValue ) {
                parseNumber( firstQuota );
            }
        } );

        secondQuota.focusedProperty().addListener( ( observable, oldValue, newValue ) -> {
            if ( newValue ) {
                resetTextField( secondQuota, "Lower Quota" );
            }

            if ( oldValue ) {
                parseNumber( secondQuota );
            }
        } );

        scenarioSim.focusedProperty().addListener( ( observable, oldValue, newValue ) -> {
            if ( newValue ) {
                resetTextField( scenarioSim, "Scenario Simulation" );
            }

            if ( oldValue ) {
                parseNumber( scenarioSim );
            }
        } );

        valueSim.focusedProperty().addListener( ( observable, oldValue, newValue ) -> {
            if ( newValue ) {
                resetTextField( valueSim, "Simulation Count" );
            }

            if ( oldValue ) {
                parseNumber( valueSim );
            }
        } );

    }

    private void initToggle() {
        computationType.getToggles().get( 0 ).selectedProperty().addListener( ( observable, oldValue, newValue ) -> {
            if ( newValue ) {
                resetTextField( valueSim, "Simulation Count" );
                valueSim.clear();
                valueSim.setDisable( true );
            }
        } );

        computationType.getToggles().get( 1 ).selectedProperty().addListener( ( observable, oldValue, newValue ) -> {
            if ( newValue ) {
                resetTextField( valueSim, "Simulation Count" );
                valueSim.setDisable( false );
            }
        } );
    }

    private void initProgress() {
        simulationProgress.indeterminateProperty().addListener( ( observable, oldValue, newValue ) -> {
            if ( newValue ) {
                startSimulation.setDisable( true );
                stopSimulation.setDisable( false );
                timeMeasure.setVisible( false );
                interrupted = false;
            }

            if ( oldValue ) {
                startSimulation.setDisable( false );
                stopSimulation.setDisable( true );
            }
        } );

        simulationProgress.setProgress( 0.0 );
        simulationProgress.setVisible( false );
    }

    private void initFlags() {
        finishedFlag.addListener( ( observable, oldValue, newValue ) -> {
            if ( newValue ) {
                simulationProgress.setProgress( 1.0 );
                if ( !interrupted ) {
                    finishedFlag.unbind();
                    shapleyChart.getData().clear();
                    shapleyChart.getData().add( constructSeries( "positive", complexCalc.getShapleyPos() ) );
                    shapleyChart.getData().add( constructSeries( "negative", complexCalc.getShapleyNeg() ) );
                    shapleyChart.getData().add( constructSeries( "neutral", complexCalc.getShapleyZer() ) );

                    banzhafChart.getData().clear();
                    banzhafChart.getData().add( constructSeries( "positive", complexCalc.getBanzhafPos() ) );
                    banzhafChart.getData().add( constructSeries( "negative", complexCalc.getBanzhafNeg() ) );
                    banzhafChart.getData().add( constructSeries( "neutral", complexCalc.getBanzhafZer() ) );
                    generateTooltips( shapleyChart );
                    generateTooltips( banzhafChart );
                }
                timeMeasure.setText( "Finished in: " + complexCalc.getRunningTime() + " seconds" );
                timeMeasure.setVisible( true );
                complexCalc = null;
                simThread = null;
                System.gc();
            }

            if ( !newValue ) {
                simulationProgress.setVisible( true );
                simulationProgress.setProgress( ProgressIndicator.INDETERMINATE_PROGRESS );
            }
        } );
    }

    private void initCharts() {
        shapleyChart.getXAxis().setLabel( "# of votes" );
        sYAxis.setLabel( "probability" );
        sYAxis.setAutoRanging( false );
        sYAxis.setUpperBound( 1.0 );
        sYAxis.setLowerBound( 0.0 );
        sYAxis.setTickUnit( 0.1 );
        shapleyChart.setAnimated( true );

        banzhafChart.getXAxis().setLabel( "# of votes" );
        bYAxis.setLabel( "probability" );
        bYAxis.setAutoRanging( false );
        bYAxis.setUpperBound( 1.0 );
        bYAxis.setLowerBound( 0.0 );
        bYAxis.setTickUnit( 0.1 );
        banzhafChart.setAnimated( true );
    }

    private void initLabels() {
        timeMeasure.setText( "" );
        timeMeasure.setVisible( false );
    }

    private void interruptSim() {
        simThread.interrupt();
        interrupted = true;
        finishedFlag.unbind();
        finishedFlag.set( true );
    }

    private XYChart.Series< String, Double > constructSeries( String name, BigDecimal[] values ) {
        XYChart.Series< String, Double > result = new XYChart.Series<>();

        for ( int i = 0; i < values.length; ++i ) {
            result.getData().add( new XYChart.Data<>( String.valueOf( i + 1 ), values[ i ].doubleValue() ) );
        }

        result.setName( name );
        return result;
    }

    private void generateTooltips( ScatterChart< String, Double > chart ) {
        for ( final XYChart.Series< String, Double > series : chart.getData() ) {
            for ( final XYChart.Data< String, Double > data : series.getData() ) {
                data.getNode()
                    .addEventHandler( MouseEvent.MOUSE_ENTERED,
                                      event -> Tooltip.install( data.getNode(),
                                                                new Tooltip( "Vote: " +
                                                                             String.valueOf( data.getXValue() ) +
                                                                             "\nProb: " +
                                                                             String.valueOf( data.getYValue() ) ) ) );
            }
        }
    }

    private boolean errorTextField( TextField field, String prompt ) {
        field.setEffect( errorColor );
        field.setPromptText( prompt );
        field.clear();
        return true;
    }

    private void resetTextField( TextField field, String prompt ) {
        field.setEffect( null );
        field.setPromptText( prompt );
    }

    private int parseNumber( TextField field ) {
        try {
            String s = field.getText();
            if ( s.isEmpty() )
                return 0;
            int v = Integer.parseInt( s );
            if ( v < 1 ) {
                errorTextField( field, "Minimum is 1" );
                return -1;
            }
            return v;
        } catch ( NumberFormatException e ) {
            errorTextField( field, "Must be an integer!" );
            return -2;
        }
    }

    private void simulate() {
        boolean error = false;
        int pCount = parseNumber( playerCount );
        int tVote = parseNumber( totalVote );
        int q1 = parseNumber( firstQuota );
        int q2 = parseNumber( secondQuota );
        int sSC = parseNumber( scenarioSim );
        int vSC = parseNumber( valueSim );

        if ( pCount < 1 )
            error = errorTextField( playerCount, "Minimum is 1" );
        if ( tVote < 1 )
            error = errorTextField( totalVote, "Minimum is 1" );
        if ( q1 < 1 )
            error = errorTextField( firstQuota, "Minimum is 1" );
        if ( q2 < 1 )
            error = errorTextField( secondQuota, "Minimum is 1" );
        if ( sSC < 1 )
            error = errorTextField( scenarioSim, "Minimum is 1" );
        if ( !valueSim.isDisabled() && vSC < 1 )
            error = errorTextField( valueSim, "Minimum is 1" );
        if ( error )
            return;

        complexCalc = new CalculatorComplex( tVote, pCount, q1, q2, sSC, vSC );
        CalculatorComplexTask t = new CalculatorComplexTask( complexCalc );

        finishedFlag.set( false );

        finishedFlag.bind( t.valueProperty() );

        simThread = new Thread( t );
        simThread.setDaemon( true );
        simThread.start();
    }

}
