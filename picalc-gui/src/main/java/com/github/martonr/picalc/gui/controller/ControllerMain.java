package com.github.martonr.picalc.gui.controller;

import com.github.martonr.picalc.gui.MainApplication;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public final class ControllerMain {
    /**
     * Holds the main tabs of the application
     */
    @FXML
    private TabPane tabPane;

    /**
     * This tab holds the power index calculation interface
     */
    @FXML
    private Tab powerIndexTab;

    /**
     * This tab holds the quota change simulation interface
     */
    @FXML
    private Tab quotaDifferenceTab;

    /**
     * This tab holds the dpi calculation interface
     */
    @FXML
    private Tab dpiTab;

    /**
     * This tab holds the dpi difference simulation interface
     */
    @FXML
    private Tab dpiDifferenceTab;

    /**
     * Controller instances of the tabs
     */
    private ControllerCalculation calculation;
    private ControllerCalculationDpi calculationDpi;
    private ControllerSimulationQuota simulationQuota;
    private ControllerSimulationDpi simulationDpi;

    /**
     * Initialize the tab contents
     */
    @FXML
    public void initialize() {
        try {
            FXMLLoader loader =
                    new FXMLLoader(MainApplication.class.getResource("fxml/calculation.fxml"));
            Parent firstTab = loader.load();
            calculation = loader.getController();
            powerIndexTab.setContent(firstTab);

            loader = new FXMLLoader(MainApplication.class.getResource("fxml/simulationQuota.fxml"));
            Parent secondTab = loader.load();
            simulationQuota = loader.getController();
            quotaDifferenceTab.setContent(secondTab);

            loader = new FXMLLoader(MainApplication.class.getResource("fxml/calculationDpi.fxml"));
            Parent thirdTab = loader.load();
            calculationDpi = loader.getController();
            dpiTab.setContent(thirdTab);

            loader = new FXMLLoader(MainApplication.class.getResource("fxml/simulationDpi.fxml"));
            Parent fourthTab = loader.load();
            simulationDpi = loader.getController();
            dpiDifferenceTab.setContent(fourthTab);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Failed to load FXML file for the UI.");
        }
        tabPane.getSelectionModel().clearSelection();
    }

    public void shutdown() {
        calculation.shutdown();
        calculationDpi.shutdown();
        simulationQuota.shutdown();
        simulationDpi.shutdown();
    }
}
