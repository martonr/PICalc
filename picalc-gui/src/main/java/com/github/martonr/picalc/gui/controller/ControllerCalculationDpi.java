package com.github.martonr.picalc.gui.controller;

import com.github.martonr.picalc.engine.calculators.CalculatorParameters;
import com.github.martonr.picalc.engine.service.ServiceCalculation;
import com.github.martonr.picalc.engine.service.ServiceCalculationTask.Results;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.Effect;

import java.io.BufferedWriter;
import java.math.RoundingMode;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Controller class for the DPI power index calculation view
 */
public final class ControllerCalculationDpi {
    /**
     * Input field for the player name
     */
    @FXML
    private TextField playerNameText;

    /**
     * Input field for the player weight
     */
    @FXML
    private TextField playerWeightText;

    /**
     * Remove a selected player
     */
    @FXML
    private Button removeButton;

    /**
     * Add a new player
     */
    @FXML
    private Button addButton;

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
     * Button that save the calculation results to disk
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
     * Input field for the estimation count
     */
    @FXML
    private TextField countText;

    /**
     * Starts the calculation
     */
    @FXML
    private Button calculateButton;

    /**
     * Stops the calculation
     */
    @FXML
    private Button stopButton;

    /**
     * The table shows the currently added players to the user
     */
    @FXML
    private TableView<Player> indexTable;
    @FXML
    private TableColumn<Player, String> playerColumn;
    @FXML
    private TableColumn<Player, Number> weightColumn;
    @FXML
    private TableColumn<Player, Number> dpiColumn;

    /**
     * Calculation service that executes the calculation
     */
    private final ServiceCalculation service = new ServiceCalculation();

    private Results dpiCalculationResult;

    /**
     * Observable collection that stores the currently added Players
     */
    private final ObservableList<Player> currentPlayers = FXCollections.observableArrayList();

    /**
     * Color adjustment used to signal a bad input in a text field
     */
    private final ColorAdjust error = new ColorAdjust(0.03, 0.04, 0.0, 0.0);

    @FXML
    private void initialize() {
        initalizeListeners();
        initializeTableView();
    }

    /**
     * Add cell value formatters and default column widths
     */
    private void initializeTableView() {
        indexTable.setItems(currentPlayers);

        playerColumn.setCellValueFactory(item -> item.getValue().nameProperty());
        weightColumn.setCellValueFactory(item -> item.getValue().weightProperty());
        dpiColumn.setCellValueFactory(item -> item.getValue().dpiProperty());

        weightColumn.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                if (empty)
                    setText("");
                else
                    setText(String.format("%.2f", item.doubleValue()));
            }
        });

        dpiColumn.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                if (empty)
                    setText("");
                else
                    setText(String.format("%.4f", item.doubleValue()));
            }
        });

        // Divide to 3 equal columns
        playerColumn.prefWidthProperty().bind(indexTable.widthProperty().divide(3));
        weightColumn.prefWidthProperty().bind(indexTable.widthProperty().divide(3));
        dpiColumn.prefWidthProperty().bind(indexTable.widthProperty().divide(3));
    }

    /**
     * Set up some basic interface behaviour logic
     */
    private void initalizeListeners() {
        currentPlayers.addListener((ListChangeListener<Player>) c -> {
            if (c.getList().isEmpty()) {
                removeButton.setDisable(true);
                calculateButton.setDisable(true);
            } else {
                removeButton.setDisable(false);
                calculateButton.setDisable(false);
            }
        });

        indexTable.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue)
                indexTable.getSelectionModel().clearSelection();
        });

        exactSelect.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                changeTextFieldLook(countText, null, "Number of estimations", true);
                countText.clear();
                countText.setDisable(true);
            }
        });

        estimateSelect.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                changeTextFieldLook(countText, null, "Number of estimations", true);
                countText.setDisable(false);
            }
        });
    }

    /**
     * Parses the input then adds a new Player
     *
     * @param event ActionEvent, not used
     */
    @FXML
    private void addPlayer(ActionEvent event) {
        // Default name
        String name = playerNameText.getText();
        if (name.isBlank())
            name = "Player " + (currentPlayers.size() + 1);

        try {
            double weight = parseDoubleFromTextField(playerWeightText);
            if (weight < 0) {
                changeTextFieldLook(playerWeightText, error, "Minimum is 0", true);
                return;
            }
            currentPlayers.add(new Player(name, weight));
            changeTextFieldLook(playerNameText, null, "Player name", true);
            changeTextFieldLook(playerWeightText, null, "Player weight", true);
            saveResultsButton.setDisable(true);
            saveResultsButton.setVisible(false);
        } catch (Exception ignored) {
            // Input was not an integer
        }
    }

    /**
     * Removes a currently selected Player
     *
     * @param event ActionEvent, not used
     */
    @FXML
    private void removePlayer(ActionEvent event) {
        TableView.TableViewSelectionModel<Player> selection = indexTable.getSelectionModel();
        if (!selection.isEmpty()) {
            currentPlayers.remove(selection.getSelectedIndex());
            selection.clearSelection();
            saveResultsButton.setDisable(true);
            saveResultsButton.setVisible(false);
        }
    }

    /**
     * Start the calculation
     *
     * @param event ActionEvent, not used
     */
    @FXML
    private void startCalculation(ActionEvent event) {
        try {
            // Parse the text field values
            int count = 1;

            if (estimateSelect.isSelected()) {
                count = parseIntegerFromTextField(countText);
                if (count < 1) {
                    changeTextFieldLook(countText, error, "Minimum 1", true);
                    return;
                }
            }

            // Store weights in an array
            final int n = currentPlayers.size();
            double[] weights = new double[n];
            for (int i = 0; i < n; ++i) {
                weights[i] = currentPlayers.get(i).getWeight();
            }

            addButton.setDisable(true);
            removeButton.setDisable(true);
            calculateButton.setDisable(true);
            stopButton.setDisable(false);
            saveResultsButton.setDisable(true);
            saveResultsButton.setVisible(false);

            changeTextFieldLook(countText, null, "Number of estimations", false);

            progressCircle.setVisible(true);
            progressText.setText("Calculating...");

            CalculatorParameters parameters = new CalculatorParameters();
            parameters.n = n;
            parameters.weights = weights;
            parameters.monteCarloCount = 0;

            if (estimateSelect.isSelected()) {
                parameters.monteCarloCount = count;
            }

            service.calculateDPI(parameters, this::updateTableWithResults);
        } catch (Exception ignored) {
            // Input was not an integer
        }
    }

    /**
     * Stop the calculation
     *
     * @param event ActionEvent, not used
     */
    @FXML
    private void stopCalculation(ActionEvent event) {
        service.cleanupTasks();
    }

    /**
     * Takes the calculation results and updates the players' power index values
     *
     * @param result Result object is a container for the calculation results
     */
    private void updateTableWithResults(Results result) {
        Platform.runLater(() -> {
            progressCircle.setVisible(false);
            if (result == null) {
                progressText.setText("Calculation stopped.");
            } else {
                progressText.setText("Finished in " + result.time + " seconds.");
                int n = currentPlayers.size();
                double[] dpi = result.dpi;
                for (int i = 0; i < n; ++i) {
                    currentPlayers.get(i).setDpi(dpi[i]);
                }
                this.dpiCalculationResult = result;
                dpiColumn.setVisible(true);
                saveResultsButton.setDisable(false);
                saveResultsButton.setVisible(true);
                indexTable.refresh();
            }

            addButton.setDisable(false);
            removeButton.setDisable(false);
            calculateButton.setDisable(false);
            stopButton.setDisable(true);

            playerNameText.requestFocus();
            System.gc();
        });
    }

    /**
     * Save the simulation results to a file
     */
    @FXML
    private void saveCalculationResultsToDisk() {
        double[] dpi = this.dpiCalculationResult.dpi;
        int players = dpi.length;

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
                                    + "_" + minute + "_calculationdpi_result_" + count + ".csv")));
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
            StringBuilder sb = new StringBuilder("weight,");
            sb.append("dpi\n");

            // Save the values
            for (int i = 0; i < players; ++i) {
                sb.append(String.format("%.3f", currentPlayers.get(i).getWeight())).append(",");
                sb.append(df.format(dpi[i])).append("\n");
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

    public void shutdown() {
        service.shutdown();
    }

    /**
     * Container class for the TableView, stores an instance of a Player A Player has a name, a
     * weight value and a DPI index, if those have been calculated
     */
    public static class Player {
        /**
         * Properties holding a Player's values
         */
        private StringProperty name = new SimpleStringProperty(this, "name");
        private DoubleProperty weight = new SimpleDoubleProperty(this, "weight");
        private DoubleProperty dpi = new SimpleDoubleProperty(this, "dpi");

        /**
         * Returns a new instance of a Player container A Player needs to have a name and a set
         * weight value Power index values can be calculated later
         *
         * @param name The player's name
         * @param weight The player's weight
         */
        public Player(String name, double weight) {
            this.name.setValue(name);
            this.weight.setValue(weight);
            this.dpi.setValue(0);
        }

        public final StringProperty nameProperty() {
            return name;
        }

        public final DoubleProperty weightProperty() {
            return weight;
        }

        public final DoubleProperty dpiProperty() {
            return dpi;
        }

        public final String getName() {
            return name.get();
        }

        public final void setName(String name) {
            this.name.set(name);
        }

        public final double getWeight() {
            return weight.get();
        }

        public final void setWeight(int vote) {
            this.weight.set(vote);
        }

        public final double getDpi() {
            return dpi.get();
        }

        public final void setDpi(double dpi) {
            this.dpi.set(dpi);
        }
    }
}
