package com.github.martonr.picalc.gui.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.Effect;
import javafx.util.converter.NumberStringConverter;

import java.io.BufferedWriter;
import java.math.RoundingMode;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.Locale;
import com.github.martonr.picalc.engine.service.ServiceSimulation;
import com.github.martonr.picalc.engine.service.SimulationParameters;
import com.github.martonr.picalc.engine.service.ServiceSimulationTask.ResultDelta;
import com.github.martonr.picalc.engine.service.ServiceSimulationTask.ResultDeltaSingle;

/**
 * Controller class for the quota difference simulation view
 */
public final class ControllerSimulationQuota {
    /**
     * Text field for the number of players input
     */
    @FXML
    private TextField playerCountText;

    /**
     * Text field for the number of votes input
     */
    @FXML
    private TextField votesText;

    /**
     * Text field for the quota value before the change
     */
    @FXML
    private TextField quotaBeforeText;

    /**
     * Text field for the quota value after the change
     */
    @FXML
    private TextField quotaAfterText;

    /**
     * If checked only scenarios where players don't reach the before quota vote value will be
     * simulated
     */
    @FXML
    private CheckBox quotaBeforeCheck;

    /**
     * If checked only scenarios where players don't reach the after quota vote value will be
     * simulated
     */
    @FXML
    private CheckBox quotaAfterCheck;

    /**
     * Graphical indicator for a running computation
     */
    @FXML
    private ProgressIndicator progressCircle;

    /**
     * Text feedback on the computation
     */
    @FXML
    private Label progressText;

    /**
     * Button that save the simulation results to disk
     */
    @FXML
    private Button saveResultsButton;

    /**
     * When selected power index values will be computed exactly
     */
    @FXML
    private RadioButton exactSelect;

    /**
     * When selected power index values will be estimated with Monte-Carlo method
     */
    @FXML
    private RadioButton estimateSelect;
    @FXML
    private ToggleGroup computationType;

    /**
     * Text field for the scenario simulation count input
     */
    @FXML
    private TextField simulationText;

    /**
     * Text field for the scenario power index estimation count
     */
    @FXML
    private TextField countText;

    /**
     * Text field for the scenario error tolerance
     */
    @FXML
    private TextField epsilonText;

    /**
     * Text field for the set vote data generation
     */
    @FXML
    private TextField setVoteText;

    /**
     * If checked limits the amount of simulations to the smaller of quota values
     */
    @FXML
    private CheckBox limitCheck;

    /**
     * Button that starts input validation and then the simulation
     */
    @FXML
    private Button simulateButton;

    /**
     * Button that starts a scenario generation task
     */
    @FXML
    private Button generateButton;

    /**
     * Button that stops a running simulation
     */
    @FXML
    private Button stopButton;

    /**
     * Chart displaying the results of the simulation This shows the probability a player with a
     * specific vote count will have a positive, negative or no change to their Shapley-Shubik power
     * index value if the quotas change
     */
    @FXML
    private SimulationChart<Number, Number> shapleyProbabilityChart;

    /**
     * Chart displaying the results of the simulation This shows the probability a player with a
     * specific vote count will have a positive, negative or no change to their Banzhaf power index
     * value if the quotas change
     */
    @FXML
    private SimulationChart<Number, Number> banzhafProbabilityChart;

    /**
     * Chart displaying the results of the simulation This shows the mean values of the power index
     * changes due to quota changes
     */
    @FXML
    private SimulationChart<Number, Number> meanChart;

    /**
     * Chart displaying the results of the simulation This shows the standard deviation values of
     * the power index changes due to quota changes
     */
    @FXML
    private SimulationChart<Number, Number> stdevChart;

    /**
     * Simulation service that executes the simulation
     */
    private final ServiceSimulation service = new ServiceSimulation();

    private ResultDelta simulationResult;

    /**
     * Formatters used to format axis labels
     */
    private final NumberStringConverter integerToString = new NumberStringConverter("#0;-#0");
    private final NumberStringConverter doubleToString = new NumberStringConverter("#0.00;-#0.00");

    /**
     * Color adjustment used to signal a bad input in a text field
     */
    private final ColorAdjust error = new ColorAdjust(0.03, 0.04, 0.0, 0.0);

    @FXML
    private void initialize() {
        initializeListeners();
        initializeCharts();
    }

    /**
     * Set up some basic interface behaviour logic
     */
    private void initializeListeners() {
        // Disable the scenario MC estimation count text field when Exact computation is
        // selected
        exactSelect.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                changeTextFieldLook(countText, null, "Estimations per scenario", true);
                countText.clear();
                countText.setDisable(true);
            }
        });

        // Enable the scenario MC estimation count text field when Exact computation is
        // selected
        estimateSelect.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                changeTextFieldLook(countText, null, "Estimations per scenario", true);
                countText.setDisable(false);
            }
        });

        // Set up checkboxes, so only one can be selected simultaneously
        // Almost a toggle, however it is possible none are selected
        quotaBeforeCheck.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                quotaAfterCheck.setSelected(false);
            }
        });

        quotaAfterCheck.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                quotaBeforeCheck.setSelected(false);
            }
        });
    }

    /**
     * Add axis label formatters to charts and add default series
     */
    private void initializeCharts() {
        // Add axis label formatters to charts for readable integer and double values
        ((NumberAxis) shapleyProbabilityChart.getXAxis()).setTickLabelFormatter(integerToString);
        ((NumberAxis) banzhafProbabilityChart.getXAxis()).setTickLabelFormatter(integerToString);
        ((NumberAxis) meanChart.getXAxis()).setTickLabelFormatter(integerToString);
        ((NumberAxis) stdevChart.getXAxis()).setTickLabelFormatter(integerToString);

        ((NumberAxis) shapleyProbabilityChart.getYAxis()).setTickLabelFormatter(doubleToString);
        ((NumberAxis) banzhafProbabilityChart.getYAxis()).setTickLabelFormatter(doubleToString);
        ((NumberAxis) meanChart.getYAxis()).setTickLabelFormatter(doubleToString);
        ((NumberAxis) stdevChart.getYAxis()).setTickLabelFormatter(doubleToString);

        addSeriesToCharts();
        addGuidesToCharts();
    }

    /**
     * Add default data series to the charts
     */
    private void addSeriesToCharts() {
        // Create and add Shapley-Shubik chart series
        LineChart.Series<Number, Number> positive = new LineChart.Series<>();
        LineChart.Series<Number, Number> negative = new LineChart.Series<>();
        LineChart.Series<Number, Number> zero = new LineChart.Series<>();

        positive.setName("positive change");
        negative.setName("negative change");
        zero.setName("zero change");

        shapleyProbabilityChart.getData().clear();
        shapleyProbabilityChart.getData().add(positive);
        shapleyProbabilityChart.getData().add(negative);
        shapleyProbabilityChart.getData().add(zero);

        // Create and add Banzhaf chart series
        positive = new LineChart.Series<>();
        negative = new LineChart.Series<>();
        zero = new LineChart.Series<>();

        positive.setName("positive change");
        negative.setName("negative change");
        zero.setName("zero change");

        banzhafProbabilityChart.getData().clear();
        banzhafProbabilityChart.getData().add(positive);
        banzhafProbabilityChart.getData().add(negative);
        banzhafProbabilityChart.getData().add(zero);

        // Create and add difference mean series
        LineChart.Series<Number, Number> shapley = new LineChart.Series<>();
        LineChart.Series<Number, Number> banzhaf = new LineChart.Series<>();

        shapley.setName("shapley");
        banzhaf.setName("banzhaf");

        meanChart.getData().clear();
        meanChart.getData().add(shapley);
        meanChart.getData().add(banzhaf);

        // Create and add difference stdev series
        shapley = new LineChart.Series<>();
        banzhaf = new LineChart.Series<>();

        shapley.setName("shapley");
        banzhaf.setName("banzhaf");

        stdevChart.getData().clear();
        stdevChart.getData().add(shapley);
        stdevChart.getData().add(banzhaf);
    }

    /**
     * Add non data visual aid series to the charts
     */
    private void addGuidesToCharts() {
        // Add guides at 0.25, 0.5, 0.75 to charts
        LineChart.Series<Number, Number> guideLower = new LineChart.Series<>();
        LineChart.Series<Number, Number> guideMid = new LineChart.Series<>();
        LineChart.Series<Number, Number> guideUpper = new LineChart.Series<>();

        guideLower.getData().clear();
        guideMid.getData().clear();
        guideUpper.getData().clear();

        shapleyProbabilityChart.getData().add(guideLower);
        shapleyProbabilityChart.getData().add(guideMid);
        shapleyProbabilityChart.getData().add(guideUpper);

        // Add guide at 0.25, 0.5, 0.75 to charts
        guideLower = new LineChart.Series<>();
        guideMid = new LineChart.Series<>();
        guideUpper = new LineChart.Series<>();

        banzhafProbabilityChart.getData().add(guideLower);
        banzhafProbabilityChart.getData().add(guideMid);
        banzhafProbabilityChart.getData().add(guideUpper);

        guideLower.getData().clear();
        guideMid.getData().clear();
        guideUpper.getData().clear();
    }

    /**
     * Start the simulation
     *
     * @param event ActionEvent, not used
     */
    @FXML
    private void startSimulation(ActionEvent event) {
        try {
            // Parse the text field values
            int quotaBefore, quotaAfter, players, votes, simulations, count = 1;
            double epsilon = 0.001;

            players = parseIntegerFromTextField(playerCountText);
            votes = parseIntegerFromTextField(votesText);
            quotaBefore = parseIntegerFromTextField(quotaBeforeText);
            quotaAfter = parseIntegerFromTextField(quotaAfterText);
            simulations = parseIntegerFromTextField(simulationText);
            epsilon = parseDoubleFromTextField(epsilonText);

            if (players < 2) {
                changeTextFieldLook(playerCountText, error, "Minimum 2", true);
                return;
            }

            if (players > Short.MAX_VALUE) {
                changeTextFieldLook(playerCountText, error, "Too many players", true);
                return;
            }

            // If player count is too large switch to Monte-Carlo estimation
            if (players > 15 && exactSelect.isSelected()) {
                estimateSelect.setSelected(true);
                exactSelect.setDisable(true);
                return;
            } else {
                exactSelect.setDisable(false);
            }

            // Every player needs at least 1 vote
            if (votes < players) {
                changeTextFieldLook(votesText, error, "Minimum is player count", true);
                return;
            }

            if (votes > Short.MAX_VALUE) {
                changeTextFieldLook(votesText, error, "Too many votes", true);
                return;
            }

            if (quotaBefore < 1) {
                changeTextFieldLook(quotaBeforeText, error, "Minimum 1", true);
                return;
            }

            if (quotaAfter < 1) {
                changeTextFieldLook(quotaAfterText, error, "Minimum 1", true);
                return;
            }

            if (simulations < 1) {
                changeTextFieldLook(simulationText, error, "Minimum 1", true);
                return;
            }

            if (epsilon <= 0 || epsilon >= 1) {
                changeTextFieldLook(epsilonText, error, "Between 0 and 1", true);
                return;
            }

            if (estimateSelect.isSelected()) {
                count = parseIntegerFromTextField(countText);
                if (count < 1) {
                    changeTextFieldLook(countText, error, "Minimum 1", true);
                    return;
                }
            }

            // Set the restricted vote if it was requested
            int max = -1;
            if (quotaBeforeCheck.isSelected()) {
                max = quotaBefore;
            }
            if (quotaAfterCheck.isSelected()) {
                max = quotaAfter;
            }

            max -= 1;

            int limit = -1;
            if (limitCheck.isSelected()) {
                limit = (quotaBefore <= quotaAfter ? quotaBefore : quotaAfter);
            }

            simulateButton.setDisable(true);
            generateButton.setDisable(true);
            stopButton.setDisable(false);
            saveResultsButton.setDisable(true);
            saveResultsButton.setVisible(false);

            changeTextFieldLook(playerCountText, null, "Number of players", false);
            changeTextFieldLook(votesText, null, "Total votes", false);
            changeTextFieldLook(quotaBeforeText, null, "Quota before", false);
            changeTextFieldLook(quotaAfterText, null, "Quota after", false);
            changeTextFieldLook(simulationText, null, "Simulations per scenario", false);
            changeTextFieldLook(countText, null, "Estimations per scenario", false);
            changeTextFieldLook(epsilonText, null, "Error tolerance", false);
            changeTextFieldLook(setVoteText, null, "Set vote", false);

            progressCircle.setVisible(true);
            progressText.setText("Calculating...");

            SimulationParameters parameters = new SimulationParameters();
            parameters.n = players;
            parameters.votes = votes;
            parameters.quotaFrom = quotaBefore;
            parameters.quotaTo = quotaAfter;
            parameters.maximumVote = max;
            parameters.simulationLimit = limit;
            parameters.indexMonteCarloCount = 0;
            parameters.tolerance = epsilon;
            parameters.monteCarloCount = simulations;
            parameters.isDpi = false;

            if (estimateSelect.isSelected()) {
                parameters.indexMonteCarloCount = count;
            }

            service.simulate(parameters, this::updateChartsWithResults);
        } catch (Exception ignored) {
            // Input was not an integer
        }
    }

    /**
     * Start a scenario generation task
     *
     * @param event ActionEvent, not used
     */
    @FXML
    private void generateScenarioData(ActionEvent event) {
        try {
            // Parse the text field values
            int quotaBefore, quotaAfter, players, votes, simulations, count = 1, setVote = 1;
            double epsilon = 0.001;

            players = parseIntegerFromTextField(playerCountText);
            votes = parseIntegerFromTextField(votesText);
            quotaBefore = parseIntegerFromTextField(quotaBeforeText);
            quotaAfter = parseIntegerFromTextField(quotaAfterText);
            simulations = parseIntegerFromTextField(simulationText);
            setVote = parseIntegerFromTextField(setVoteText);

            if (players < 2) {
                changeTextFieldLook(playerCountText, error, "Minimum 2", true);
                return;
            }

            if (players > Short.MAX_VALUE) {
                changeTextFieldLook(playerCountText, error, "Too many players", true);
                return;
            }

            // If player count is too large switch to Monte-Carlo estimation
            if (players > 15 && exactSelect.isSelected()) {
                estimateSelect.setSelected(true);
                exactSelect.setDisable(true);
                return;
            } else {
                exactSelect.setDisable(false);
            }

            // Every player needs at least 1 vote
            if (votes < players) {
                changeTextFieldLook(votesText, error, "Minimum is player count", true);
                return;
            }

            if (votes > Short.MAX_VALUE) {
                changeTextFieldLook(votesText, error, "Too many votes", true);
                return;
            }

            if (quotaBefore < 1) {
                changeTextFieldLook(quotaBeforeText, error, "Minimum 1", true);
                return;
            }

            if (quotaAfter < 1) {
                changeTextFieldLook(quotaAfterText, error, "Minimum 1", true);
                return;
            }

            if (simulations < 1) {
                changeTextFieldLook(simulationText, error, "Minimum 1", true);
                return;
            }

            if (setVote < 1 || setVote > (votes - players + 1)) {
                changeTextFieldLook(setVoteText, error, "Error", true);
                return;
            }

            if (estimateSelect.isSelected()) {
                count = parseIntegerFromTextField(countText);
                if (count < 1) {
                    changeTextFieldLook(countText, error, "Minimum 1", true);
                    return;
                }
            }

            // Set the restricted vote if it was requested
            int max = -1;
            if (quotaBeforeCheck.isSelected()) {
                max = quotaBefore;
            }
            if (quotaAfterCheck.isSelected()) {
                max = quotaAfter;
            }

            max -= 1;

            simulateButton.setDisable(true);
            generateButton.setDisable(true);
            stopButton.setDisable(false);
            saveResultsButton.setDisable(true);
            saveResultsButton.setVisible(false);

            changeTextFieldLook(playerCountText, null, "Number of players", false);
            changeTextFieldLook(votesText, null, "Total votes", false);
            changeTextFieldLook(quotaBeforeText, null, "Quota before", false);
            changeTextFieldLook(quotaAfterText, null, "Quota after", false);
            changeTextFieldLook(simulationText, null, "Simulations per scenario", false);
            changeTextFieldLook(countText, null, "Estimations per scenario", false);
            changeTextFieldLook(epsilonText, null, "Error tolerance", false);
            changeTextFieldLook(setVoteText, null, "Set vote", false);

            progressCircle.setVisible(true);
            progressText.setText("Calculating...");

            SimulationParameters parameters = new SimulationParameters();
            parameters.n = players;
            parameters.votes = votes;
            parameters.quotaFrom = quotaBefore;
            parameters.quotaTo = quotaAfter;
            parameters.maximumVote = max;
            parameters.singleVote = setVote;
            parameters.indexMonteCarloCount = 0;
            parameters.monteCarloCount = simulations;
            parameters.tolerance = epsilon;
            parameters.isDpi = false;

            if (estimateSelect.isSelected()) {
                parameters.indexMonteCarloCount = count;
            }

            service.generate(parameters, this::processGeneratedData);
        } catch (Exception ignored) {
            // Input was not an integer
        }
    }

    /**
     * Stop the simulation
     *
     * @param event ActionEvent, not used
     */
    @FXML
    private void stopSimulation(ActionEvent event) {
        service.cleanupTasks();
    }

    /**
     * Takes the simulation results and updates the charts
     *
     * @param result Result object is a container for the simulation results
     */
    private void updateChartsWithResults(ResultDelta result) {
        Platform.runLater(() -> {
            progressCircle.setVisible(false);
            if (result == null) {
                progressText.setText("Calculation stopped.");
            } else {
                progressText.setText("Finished in " + result.time + " seconds.");
                populateChartsWithData(result.shapley, result.banzhaf);
                this.simulationResult = result;
                saveResultsButton.setDisable(false);
                saveResultsButton.setVisible(true);
            }

            simulateButton.setDisable(false);
            generateButton.setDisable(false);
            stopButton.setDisable(true);

            simulationText.requestFocus();
            System.gc();
        });
    }

    /**
     * Handles the chart updates
     *
     * @param shapley 2D array for the simulation Shapley-Shubik results
     * @param banzhaf 2D array for the simulation Banzhaf results
     */
    private void populateChartsWithData(double[][] shapley, double[][] banzhaf) {
        final int previousSize = shapleyProbabilityChart.getData().get(0).getData().size();
        final int newSize = shapley.length;

        adjustSeriesForNewResults(previousSize, newSize);

        // Used to determine new Y axis range
        double maxMean = Double.MIN_VALUE, minMean = Double.MAX_VALUE, maxStdev = Double.MIN_VALUE,
                minStdev = Double.MAX_VALUE;

        for (int j = 0; j < newSize; ++j) {
            for (int i = 0; i < 3; ++i) {
                shapleyProbabilityChart.getData().get(i).getData().get(j).setYValue(shapley[j][i]);
                banzhafProbabilityChart.getData().get(i).getData().get(j).setYValue(banzhaf[j][i]);
            }

            // Find maximum and minimum value for proper Y axis range
            maxMean = (maxMean >= shapley[j][3]) ? maxMean : shapley[j][3];
            maxMean = (maxMean >= banzhaf[j][3]) ? maxMean : banzhaf[j][3];
            minMean = (minMean <= shapley[j][3]) ? minMean : shapley[j][3];
            minMean = (minMean <= banzhaf[j][3]) ? minMean : banzhaf[j][3];

            maxStdev = (maxStdev >= shapley[j][4]) ? maxStdev : shapley[j][4];
            maxStdev = (maxStdev >= banzhaf[j][4]) ? maxStdev : banzhaf[j][4];
            minStdev = (minStdev <= shapley[j][4]) ? minStdev : shapley[j][4];
            minStdev = (minStdev <= banzhaf[j][4]) ? minStdev : banzhaf[j][4];

            meanChart.getData().get(0).getData().get(j).setYValue(shapley[j][3]);
            meanChart.getData().get(1).getData().get(j).setYValue(banzhaf[j][3]);

            stdevChart.getData().get(0).getData().get(j).setYValue(shapley[j][4]);
            stdevChart.getData().get(1).getData().get(j).setYValue(banzhaf[j][4]);
        }

        // Update Y axis ranges
        ((NumberAxis) meanChart.getYAxis()).setUpperBound((int) (maxMean + 1));
        ((NumberAxis) meanChart.getYAxis()).setLowerBound((int) (minMean - 1));
        ((NumberAxis) stdevChart.getYAxis()).setUpperBound((int) (maxStdev + 1));
        ((NumberAxis) stdevChart.getYAxis()).setLowerBound((int) (minStdev - 1));
    }

    private void adjustSeriesForNewResults(int previousSize, int newSize) {
        int delta = newSize - previousSize;

        if (delta < 0) {
            // Remove data points from the end to match the simulation results
            for (int j = previousSize; j > previousSize + delta; --j) {
                shapleyProbabilityChart.getData().get(3).getData().remove(j - 1);
                shapleyProbabilityChart.getData().get(4).getData().remove(j - 1);
                shapleyProbabilityChart.getData().get(5).getData().remove(j - 1);

                banzhafProbabilityChart.getData().get(3).getData().remove(j - 1);
                banzhafProbabilityChart.getData().get(4).getData().remove(j - 1);
                banzhafProbabilityChart.getData().get(5).getData().remove(j - 1);

                for (int i = 0; i < 3; ++i) {
                    shapleyProbabilityChart.getData().get(i).getData().remove(j - 1);
                    banzhafProbabilityChart.getData().get(i).getData().remove(j - 1);
                }
                for (int i = 0; i < 2; ++i) {
                    meanChart.getData().get(i).getData().remove(j - 1);
                    stdevChart.getData().get(i).getData().remove(j - 1);
                }
            }

            // Set the new X Axis bounds
            ((NumberAxis) shapleyProbabilityChart.getXAxis()).setUpperBound(newSize);
            ((NumberAxis) banzhafProbabilityChart.getXAxis()).setUpperBound(newSize);
            ((NumberAxis) meanChart.getXAxis()).setUpperBound(newSize);
            ((NumberAxis) stdevChart.getXAxis()).setUpperBound(newSize);
        } else if (delta > 0) {
            // Add data points to the end to match the simulation results
            // There is no Data point at X = 0, since a player can't have 0 votes
            for (int j = previousSize; j < previousSize + delta; ++j) {
                shapleyProbabilityChart.getData().get(3).getData()
                        .add(new LineChart.Data<>(j + 1, 0.25));
                shapleyProbabilityChart.getData().get(4).getData()
                        .add(new LineChart.Data<>(j + 1, 0.5));
                shapleyProbabilityChart.getData().get(5).getData()
                        .add(new LineChart.Data<>(j + 1, 0.75));

                banzhafProbabilityChart.getData().get(3).getData()
                        .add(new LineChart.Data<>(j + 1, 0.25));
                banzhafProbabilityChart.getData().get(4).getData()
                        .add(new LineChart.Data<>(j + 1, 0.5));
                banzhafProbabilityChart.getData().get(5).getData()
                        .add(new LineChart.Data<>(j + 1, 0.75));

                for (int i = 0; i < 3; ++i) {
                    shapleyProbabilityChart.getData().get(i).getData()
                            .add(new LineChart.Data<>(j + 1, 0.0));
                    banzhafProbabilityChart.getData().get(i).getData()
                            .add(new LineChart.Data<>(j + 1, 0.0));
                }

                for (int i = 0; i < 2; ++i) {
                    meanChart.getData().get(i).getData().add(new LineChart.Data<>(j + 1, 0.0));
                    stdevChart.getData().get(i).getData().add(new LineChart.Data<>(j + 1, 0.0));
                }
            }

            // Set the new X Axis bounds
            ((NumberAxis) shapleyProbabilityChart.getXAxis()).setUpperBound(newSize);
            ((NumberAxis) banzhafProbabilityChart.getXAxis()).setUpperBound(newSize);
            ((NumberAxis) meanChart.getXAxis()).setUpperBound(newSize);
            ((NumberAxis) stdevChart.getXAxis()).setUpperBound(newSize);
        }
    }

    /**
     * Takes the scenario generation results and saves it to disk
     *
     * @param result Result object is a container for the simulation generation results
     */
    private void processGeneratedData(ResultDeltaSingle result) {
        Platform.runLater(() -> {
            progressCircle.setVisible(false);
            if (result == null) {
                progressText.setText("Calculation stopped.");
            } else {
                progressText.setText("Finished in " + result.time + " seconds.");
                saveGeneratedData(result);
            }

            simulateButton.setDisable(false);
            generateButton.setDisable(false);
            stopButton.setDisable(true);

            simulationText.requestFocus();
            System.gc();
        });
    }

    /**
     * Takes the scenario generation results and saves it to disk
     *
     * @param result Result object is a container for the simulation generation results
     */
    private void saveGeneratedData(ResultDeltaSingle result) {
        double[] shapley = result.shapley;
        double[] banzhaf = result.banzhaf;
        int[][] votes = result.votes;

        int values = shapley.length;
        int players = votes[0].length;

        try {
            Files.createDirectories(Paths.get("./generated/"));
        } catch (FileAlreadyExistsException ex) {
            // Directory exists
        } catch (Exception ignored) {
            // Failed to create a directory
            return;
        }

        BufferedWriter bw = null;
        LocalDateTime date = LocalDateTime.now();

        DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance(Locale.ENGLISH);
        df.applyPattern("#0.00000");
        df.setRoundingMode(RoundingMode.HALF_UP);

        int year = date.getYear();
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();
        int hour = date.getHour();
        int minute = date.getMinute();
        int count = 0;

        try {
            while (bw == null) {
                try {
                    bw = Files.newBufferedWriter(Files
                            .createFile(Paths.get("./generated/" + year + "_" + month + "_" + day
                                    + "_" + hour + "_" + minute + "_quotasim_" + count + ".csv")));
                } catch (FileAlreadyExistsException ex) {
                    // File with this name exists
                    count++;
                    // Too many files with the same name...
                    if (count > 1000)
                        return;
                } catch (Exception ignored) {
                    // Failed to create the file
                    return;
                }
            }
            // Create the header
            StringBuilder sb = new StringBuilder("p1,");

            for (int i = 2; i <= players; ++i) {
                sb.append("p").append(i).append(",");
            }
            sb.append("p1_ss_change,");
            sb.append("p1_bf_change\n");

            // Save the values
            for (int i = 0; i < values; ++i) {
                for (int j = players - 1; j >= 0; --j) {
                    sb.append(votes[i][j]).append(",");
                }
                sb.append(df.format(shapley[i])).append(",");
                sb.append(df.format(banzhaf[i])).append("\n");

                bw.write(sb.toString());
                sb.setLength(0);
            }

            bw.flush();
            bw.close();
            progressText.setText("Generated results saved to file.");
        } catch (Exception ignored) {
            // Failed to write the file
        }
    }

    /**
     * Save the simulation results to a file
     */
    @FXML
    private void saveSimulationResultsToDisk() {
        double[][] shapley = this.simulationResult.shapley;
        double[][] banzhaf = this.simulationResult.banzhaf;
        int vote = shapley.length;

        try {
            Files.createDirectories(Paths.get("./results/"));
        } catch (FileAlreadyExistsException ex) {
            // Directory exists
        } catch (Exception ignored) {
            // Failed to create a directory
            return;
        }

        BufferedWriter bw = null;
        LocalDateTime date = LocalDateTime.now();

        DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance(Locale.ENGLISH);
        df.applyPattern("#0.00000");
        df.setRoundingMode(RoundingMode.HALF_UP);

        int year = date.getYear();
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();
        int hour = date.getHour();
        int minute = date.getMinute();
        int count = 0;

        try {
            while (bw == null) {
                try {
                    bw = Files.newBufferedWriter(Files.createFile(
                            Paths.get("./results/" + year + "_" + month + "_" + day + "_" + hour
                                    + "_" + minute + "_simulation_result_" + count + ".csv")));
                } catch (FileAlreadyExistsException ex) {
                    // File with this name exists
                    count++;
                    // Too many files with the same name...
                    if (count > 1000)
                        return;
                } catch (Exception ignored) {
                    // Failed to create the file
                    return;
                }
            }

            // Create the header
            StringBuilder sb = new StringBuilder("set_vote,");
            sb.append("ss_change_pos,");
            sb.append("ss_change_neg,");
            sb.append("ss_change_zero,");
            sb.append("ss_change_mean,");
            sb.append("ss_change_stdev,");
            sb.append("bf_change_pos,");
            sb.append("bf_change_neg,");
            sb.append("bf_change_zero,");
            sb.append("bf_change_mean,");
            sb.append("bf_change_stdev\n");

            // Save the values
            for (int i = 0; i < vote; ++i) {
                sb.append((i + 1)).append(",");
                sb.append(df.format(shapley[i][0])).append(",");
                sb.append(df.format(shapley[i][1])).append(",");
                sb.append(df.format(shapley[i][2])).append(",");
                sb.append(df.format(shapley[i][3])).append(",");
                sb.append(df.format(shapley[i][4])).append(",");

                sb.append(df.format(banzhaf[i][0])).append(",");
                sb.append(df.format(banzhaf[i][1])).append(",");
                sb.append(df.format(banzhaf[i][2])).append(",");
                sb.append(df.format(banzhaf[i][3])).append(",");
                sb.append(df.format(banzhaf[i][4])).append("\n");
                bw.write(sb.toString());
                // Reuse StringBuilder buffer
                sb.setLength(0);
            }

            bw.flush();
            bw.close();
            progressText.setText("Results saved to file.");
        } catch (Exception ignored) {
            // Failed to write the file
        }
    }

    /**
     * Parses an integer from a text field input
     *
     * @param field TextField to parse from
     * @return The parsed integer
     * @throws NumberFormatException If the input can't be parsed as an integer
     */
    private int parseIntegerFromTextField(TextField field) throws NumberFormatException {
        try {
            return Integer.parseInt(field.getText());
        } catch (Exception ignored) {
            changeTextFieldLook(field, error, "Must be an integer", true);
            throw new NumberFormatException();
        }
    }

    /**
     * Parses a double from a text field input
     *
     * @param field TextField to parse from
     * @return The parsed double
     * @throws NumberFormatException If the input can't be parsed as a double
     */
    private double parseDoubleFromTextField(TextField field) throws NumberFormatException {
        try {
            return Double.parseDouble(field.getText());
        } catch (Exception ignored) {
            changeTextFieldLook(field, error, "Must be a number", true);
            throw new NumberFormatException();
        }
    }

    public void shutdown() {
        service.shutdown();
    }

    /**
     * Changes a TextField, applying an effect to it, changing the prompt message and potentially
     * clearing the contents
     *
     * @param field Target TextField
     * @param effect Chosen Effect to apply to the TextField
     * @param message String message to display in the prompt
     * @param clear If true, the TextField contents will be cleared (to see the prompt message)
     */
    private void changeTextFieldLook(TextField field, Effect effect, String message,
            boolean clear) {
        field.setEffect(effect);
        field.setPromptText(message);
        if (clear)
            field.clear();
    }
}
