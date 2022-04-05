package com.github.martonr.picalc.gui.controller;

import com.github.martonr.picalc.engine.calculators.CalculatorParameters;
import com.github.martonr.picalc.engine.service.ServiceCalculation;
import com.github.martonr.picalc.engine.service.ServiceCalculationTask.Results;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
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
 * Controller class for the power index calculation view
 */
public final class ControllerCalculation {
    /**
     * Input field for the player name
     */
    @FXML
    private TextField playerNameText;

    /**
     * Input field for the player vote count
     */
    @FXML
    private TextField playerVoteText;

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
     * Input field for the quota value
     */
    @FXML
    private TextField quotaText;

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
    private TableColumn<Player, Number> voteColumn;
    @FXML
    private TableColumn<Player, Number> shapleyColumn;
    @FXML
    private TableColumn<Player, Number> banzhafColumn;

    /**
     * Calculation service that executes the calculation
     */
    private final ServiceCalculation service = new ServiceCalculation();

    private Results ssbfCalculationResult;

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
        voteColumn.setCellValueFactory(item -> item.getValue().voteProperty());
        shapleyColumn.setCellValueFactory(item -> item.getValue().shapleyProperty());
        banzhafColumn.setCellValueFactory(item -> item.getValue().banzhafProperty());

        voteColumn.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                if (empty)
                    setText("");
                else
                    setText(String.format("%d", item.intValue()));
            }
        });
        shapleyColumn.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                if (empty)
                    setText("");
                else
                    setText(String.format("%.5f", item.doubleValue()));
            }
        });
        banzhafColumn.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                if (empty)
                    setText("");
                else
                    setText(String.format("%.5f", item.doubleValue()));
            }
        });

        // Divide to 4 equal columns
        playerColumn.prefWidthProperty().bind(indexTable.widthProperty().divide(4));
        voteColumn.prefWidthProperty().bind(indexTable.widthProperty().divide(4));
        shapleyColumn.prefWidthProperty().bind(indexTable.widthProperty().divide(4));
        banzhafColumn.prefWidthProperty().bind(indexTable.widthProperty().divide(4));
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
                if (c.getList().size() > 15) {
                    estimateSelect.setSelected(true);
                    exactSelect.setDisable(true);
                } else {
                    exactSelect.setDisable(false);
                }
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
            int vote = parseIntegerFromTextField(playerVoteText);
            if (vote < 1) {
                changeTextFieldLook(playerVoteText, error, "Minimum 1", true);
                return;
            }
            currentPlayers.add(new Player(name, vote));
            changeTextFieldLook(playerNameText, null, "Player name", true);
            changeTextFieldLook(playerVoteText, null, "Player vote", true);
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
            int quota, count = 1;

            quota = parseIntegerFromTextField(quotaText);

            if (quota < 1) {
                changeTextFieldLook(quotaText, error, "Minimum 1", true);
                return;
            }

            if (estimateSelect.isSelected()) {
                count = parseIntegerFromTextField(countText);
                if (count < 1) {
                    changeTextFieldLook(countText, error, "Minimum 1", true);
                    return;
                }
            }

            // Store votes in an integer array
            final int n = currentPlayers.size();
            int[] votes = new int[n];
            for (int i = 0; i < n; ++i) {
                votes[i] = currentPlayers.get(i).getVote();
            }

            addButton.setDisable(true);
            removeButton.setDisable(true);
            calculateButton.setDisable(true);
            stopButton.setDisable(false);
            saveResultsButton.setDisable(true);
            saveResultsButton.setVisible(false);

            changeTextFieldLook(quotaText, null, "Quota value", false);
            changeTextFieldLook(countText, null, "Number of estimations", false);

            progressCircle.setVisible(true);
            progressText.setText("Calculating...");

            CalculatorParameters parameters = new CalculatorParameters();
            parameters.n = n;
            parameters.votes = votes;
            parameters.quota = quota;
            parameters.monteCarloCount = 0;

            if (estimateSelect.isSelected()) {
                parameters.monteCarloCount = count;
            }

            service.calculateSSBF(parameters, this::updateTableWithResults);
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
                double[] shapley = result.shapley;
                double[] banzhaf = result.banzhaf;
                for (int i = 0; i < n; ++i) {
                    currentPlayers.get(i).setShapley(shapley[i]);
                    currentPlayers.get(i).setBanzhaf(banzhaf[i]);
                }
                this.ssbfCalculationResult = result;
                shapleyColumn.setVisible(true);
                banzhafColumn.setVisible(true);
                saveResultsButton.setDisable(false);
                saveResultsButton.setVisible(true);
                indexTable.refresh();
            }

            addButton.setDisable(false);
            removeButton.setDisable(false);
            calculateButton.setDisable(false);
            stopButton.setDisable(true);

            quotaText.requestFocus();
            System.gc();
        });
    }

    /**
     * Save the simulation results to a file
     */
    @FXML
    private void saveCalculationResultsToDisk() {
        double[] shapley = this.ssbfCalculationResult.shapley;
        double[] banzhaf = this.ssbfCalculationResult.banzhaf;
        int players = shapley.length;

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
                                    + "_" + minute + "_calculation_result_" + count + ".csv")));
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
            StringBuilder sb = new StringBuilder("vote,");
            sb.append("ss,");
            sb.append("bf\n");

            // Save the values
            for (int i = 0; i < players; ++i) {
                sb.append(currentPlayers.get(i).getVote()).append(",");
                sb.append(df.format(shapley[i])).append(",");
                sb.append(df.format(banzhaf[i])).append("\n");
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
     * Container class for the TableView, stores an instance of a Player A Player has a name, a vote
     * value and a Shapley-Shubik, and Banzhaf index, if those have been calculated
     */
    public static class Player {
        /**
         * Properties holding a Player's values
         */
        private StringProperty name = new SimpleStringProperty(this, "name");
        private IntegerProperty vote = new SimpleIntegerProperty(this, "vote");
        private DoubleProperty shapley = new SimpleDoubleProperty(this, "shapley");
        private DoubleProperty banzhaf = new SimpleDoubleProperty(this, "banzhaf");

        /**
         * Returns a new instance of a Player container A Player needs to have a name and a set vote
         * value Power index values can be calculated later
         *
         * @param name The player's name
         * @param vote The player's vote count
         */
        public Player(String name, int vote) {
            this.name.setValue(name);
            this.vote.setValue(vote);
            this.shapley.setValue(0);
            this.banzhaf.setValue(0);
        }

        public final StringProperty nameProperty() {
            return name;
        }

        public final IntegerProperty voteProperty() {
            return vote;
        }

        public final DoubleProperty shapleyProperty() {
            return shapley;
        }

        public final DoubleProperty banzhafProperty() {
            return banzhaf;
        }

        public final String getName() {
            return name.get();
        }

        public final void setName(String name) {
            this.name.set(name);
        }

        public final int getVote() {
            return vote.get();
        }

        public final void setVote(int vote) {
            this.vote.set(vote);
        }

        public final double getShapley() {
            return shapley.get();
        }

        public final void setShapley(double shapley) {
            this.shapley.set(shapley);
        }

        public final double getBanzhaf() {
            return banzhaf.get();
        }

        public final void setBanzhaf(double banzhaf) {
            this.banzhaf.set(banzhaf);
        }
    }
}
