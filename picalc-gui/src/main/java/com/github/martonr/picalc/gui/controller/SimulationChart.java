package com.github.martonr.picalc.gui.controller;

import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import com.sun.javafx.charts.Legend;

public final class SimulationChart<X, Y> extends LineChart<X, Y> {

    @SuppressWarnings("unchecked")
    public SimulationChart() {
        super((Axis<X>) new NumberAxis(), (Axis<Y>) new NumberAxis());
        // Same as LineChart, we just need to modify the Legend update logic
    }

    @Override
    protected void updateLegend() {
        super.updateLegend();

        Legend legend = ((Legend) getLegend());
        int size = legend.getItems().size();

        // Remove the last 3 entries if there are any
        while (size > 3) {
            legend.getItems().remove(size - 1);
            size--;
        }
    }

    public void setXAxis(Axis<X> xAxis) {
        NumberAxis current = (NumberAxis) super.getXAxis();
        current.setTickUnit(((NumberAxis) xAxis).getTickUnit());
        current.setLabel(((NumberAxis) xAxis).getLabel());
        current.setAutoRanging(((NumberAxis) xAxis).isAutoRanging());
        current.setSide(((NumberAxis) xAxis).getSide());
    }

    public void setYAxis(Axis<Y> yAxis) {
        NumberAxis current = (NumberAxis) super.getYAxis();
        current.setUpperBound(((NumberAxis) yAxis).getUpperBound());
        current.setTickUnit(((NumberAxis) yAxis).getTickUnit());
        current.setLabel(((NumberAxis) yAxis).getLabel());
        current.setAutoRanging(((NumberAxis) yAxis).isAutoRanging());
        current.setSide(((NumberAxis) yAxis).getSide());
    }
}
