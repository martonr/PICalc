package control;

import calculator.CalculatorSimple;
import calculator.CalculatorSimpleTask;
import container.ContainerPlayer;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.ColorAdjust;

import java.math.BigDecimal;

/**
 * Created by Márton Rajnai on 2016-04-08.
 * Contact: marton.rajnai@gmail.com
 */

/**
 * Power index view controller used for the power index calculation functionality.
 */
public final class ControlPowerIndexView {

    @FXML
    private TableView< ContainerPlayer > playerTable;
    @FXML
    private ToggleGroup computationType;
    @FXML
    private ProgressBar computationProgress;
    @FXML
    private TextField playerName;
    @FXML
    private TextField playerVote;
    @FXML
    private TextField quotaValue;
    @FXML
    private TextField simValue;
    @FXML
    private Button addPlayer;
    @FXML
    private Button removePlayer;
    @FXML
    private Button startCalculation;
    @FXML
    private Button stopCalculation;
    @FXML
    private Label timeMeasure;

    private final ColorAdjust errorColor = new ColorAdjust( 0.03, 0.05, 0.0, 0.0 );
    private final SimpleBooleanProperty finishedFlag = new SimpleBooleanProperty( true );

    private CalculatorSimple simpleCalc;
    private Thread calcThread;
    private boolean interrupted = false;

    public void initialize() {
        initPlayerTable();
        initButtons();
        initTextFields();
        initToggle();
        initProgress();
        initFlags();
        initLabels();
    }

    private void initPlayerTable() {
        playerTable.setPlaceholder( new Label( "No players added." ) );
        playerTable.getColumns().get( 0 ).setCellValueFactory( new PropertyValueFactory<>( "playerName" ) );
        playerTable.getColumns().get( 1 ).setCellValueFactory( new PropertyValueFactory<>( "playerVote" ) );
        playerTable.getColumns().get( 2 ).setCellValueFactory( new PropertyValueFactory<>( "playerShapley" ) );
        playerTable.getColumns().get( 3 ).setCellValueFactory( new PropertyValueFactory<>( "playerBanzhaf" ) );

        playerTable.getColumns().get( 2 ).setVisible( false );
        playerTable.getColumns().get( 3 ).setVisible( false );

        playerTable.getItems().addListener( ( ListChangeListener< ContainerPlayer > ) c -> {
            if ( playerTable.getItems().isEmpty() ) {
                removePlayer.setDisable( true );
                startCalculation.setDisable( true );
            } else {
                removePlayer.setDisable( false );
                startCalculation.setDisable( false );
            }

            if ( playerTable.getItems().size() > 16 ) {
                computationType.getToggles().get( 1 ).setSelected( true );
                ( ( RadioButton ) computationType.getToggles().get( 0 ) ).setDisable( true );
            } else {
                ( ( RadioButton ) computationType.getToggles().get( 0 ) ).setDisable( false );
            }
        } );
    }

    private void initButtons() {
        addPlayer.setOnAction( event -> addNewPlayer() );
        removePlayer.setOnAction( event -> remSelectedPlayer() );
        startCalculation.setOnAction( event -> calculate() );
        stopCalculation.setOnAction( event -> interruptCalc() );

        removePlayer.setDisable( true );
        startCalculation.setDisable( true );
        stopCalculation.setDisable( true );
    }

    private void initTextFields() {
        playerName.focusedProperty().addListener( ( observable, oldValue, newValue ) -> {
            if ( newValue ) {
                clearTableSelect();
                resetTextField( playerName, "Name" );
            }
        } );

        playerVote.focusedProperty().addListener( ( observable, oldValue, newValue ) -> {
            if ( newValue ) {
                clearTableSelect();
                resetTextField( playerVote, "Vote" );
            }

            if ( oldValue ) {
                parseNumber( playerVote );
            }
        } );

        quotaValue.focusedProperty().addListener( ( observable, oldValue, newValue ) -> {
            if ( newValue ) {
                clearTableSelect();
                resetTextField( quotaValue, "Quota" );
            }

            if ( oldValue ) {
                parseNumber( quotaValue );
            }
        } );

        simValue.focusedProperty().addListener( ( observable, oldValue, newValue ) -> {
            if ( newValue ) {
                clearTableSelect();
                resetTextField( simValue, "Simulation Count" );
            }

            if ( oldValue ) {
                parseNumber( simValue );
            }
        } );
    }

    private void initToggle() {
        computationType.getToggles().get( 0 ).selectedProperty().addListener( ( observable, oldValue, newValue ) -> {
            if ( newValue ) {
                clearTableSelect();
                resetTextField( simValue, "Simulation Count" );
                simValue.clear();
                simValue.setDisable( true );
            }
        } );

        computationType.getToggles().get( 1 ).selectedProperty().addListener( ( observable, oldValue, newValue ) -> {
            if ( newValue ) {
                clearTableSelect();
                resetTextField( simValue, "Simulation Count" );
                simValue.setDisable( false );
            }
        } );
    }

    private void initProgress() {
        computationProgress.indeterminateProperty().addListener( ( observable, oldValue, newValue ) -> {
            if ( newValue ) {
                startCalculation.setDisable( true );
                stopCalculation.setDisable( false );
                timeMeasure.setVisible( false );
                interrupted = false;
            }

            if ( oldValue ) {
                startCalculation.setDisable( false );
                stopCalculation.setDisable( true );
            }
        } );

        computationProgress.setProgress( 0.0 );
        computationProgress.setVisible( false );
    }

    private void initFlags() {
        finishedFlag.addListener( ( observable, oldValue, newValue ) -> {
            if ( newValue ) {
                computationProgress.setProgress( 1.0 );
                if ( !interrupted ) {
                    finishedFlag.unbind();
                    int pCount = playerTable.getItems().size();
                    BigDecimal[] sV = simpleCalc.getShapleyValues();
                    BigDecimal[] bV = simpleCalc.getBanzhafValues();

                    for ( int i = 0; i < pCount; ++i ) {
                        playerTable.getItems().get( i ).setPlayerShapley( sV[ i ].toString() );
                        playerTable.getItems().get( i ).setPlayerBanzhaf( bV[ i ].toString() );
                    }

                    playerTable.getColumns().get( 2 ).setVisible( true );
                    playerTable.getColumns().get( 3 ).setVisible( true );
                    playerTable.refresh();
                }
                timeMeasure.setText( "Finished in: " + simpleCalc.getRunningTime() + " seconds" );
                timeMeasure.setVisible( true );
                simpleCalc = null;
                calcThread = null;
                System.gc();
            }

            if ( !newValue ) {
                playerTable.getColumns().get( 2 ).setVisible( false );
                playerTable.getColumns().get( 3 ).setVisible( false );

                computationProgress.setVisible( true );
                computationProgress.setProgress( ProgressIndicator.INDETERMINATE_PROGRESS );
            }
        } );
    }

    private void initLabels() {
        timeMeasure.setText( "" );
        timeMeasure.setVisible( false );
    }

    private void clearTableSelect() {
        playerTable.getSelectionModel().clearSelection();
    }

    private void interruptCalc() {
        calcThread.interrupt();
        interrupted = true;
        finishedFlag.unbind();
        finishedFlag.set( true );
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

    private String parseName() {
        return playerName.getText();
    }

    private int parseNumber( TextField field ) {
        try {
            String s = field.getText();
            if ( s.isEmpty() )
                return -1;
            int v = Integer.parseInt( s );
            if ( v < 1 ) {
                errorTextField( field, "Minimum is 1" );
                return -1;
            }
            return v;
        } catch ( NumberFormatException e ) {
            errorTextField( field, "Must be an integer!" );
            return -1;
        }
    }

    private void addNewPlayer() {
        String name = parseName().isEmpty() ? "P" : parseName();
        int vote = parseNumber( playerVote );
        if ( vote < 0 )
            return;
        playerTable.getItems().add( new ContainerPlayer( name, vote ) );
        resetTextField( playerName, "Name" );
        resetTextField( playerVote, "Vote" );
    }

    private void remSelectedPlayer() {
        if ( playerTable.getSelectionModel().isEmpty() )
            return;
        playerTable.getItems().remove( playerTable.getSelectionModel().getSelectedIndex() );
        clearTableSelect();
    }

    private void calculate() {
        boolean error = false;
        int quota = parseNumber( quotaValue );
        int simV = parseNumber( simValue );

        if ( quota < 0 )
            error = errorTextField( quotaValue, "Minimum is 1" );
        if ( !simValue.isDisabled() && simV < 0 )
            error = errorTextField( simValue, "Minimum is 1" );
        if ( error )
            return;

        playerTable.refresh();

        int playerC = playerTable.getItems().size();
        int[] pVotes = new int[ playerC ];

        for ( int i = 0; i < playerC; ++i ) {
            pVotes[ i ] = playerTable.getItems().get( i ).getPlayerVote();
        }

        simpleCalc = new CalculatorSimple( pVotes, quota, simV );
        CalculatorSimpleTask t = new CalculatorSimpleTask( simpleCalc );

        finishedFlag.set( false );

        finishedFlag.bind( t.valueProperty() );

        calcThread = new Thread( t );
        calcThread.setDaemon( true );
        calcThread.start();
    }
}
