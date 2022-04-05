package com.github.martonr.picalc.cli.service;

import java.io.BufferedWriter;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Properties;
import com.github.martonr.picalc.engine.service.SimulationParameters;
import com.github.martonr.picalc.engine.service.ServiceSimulationTask.ResultDelta;

public final class ServiceSimulationFile {

    private final Properties properties = new Properties();

    public int threadsRequested = 1;

    public SimulationParameters readSimulationParameters() {
        try {
            properties.load(
                    Files.newBufferedReader(Paths.get("./sim.properties"), StandardCharsets.UTF_8));
        } catch (Exception ex) {
            System.out.println("Failed to read properties file!");
            return null;
        }

        StringBuilder builder = new StringBuilder("Simulation properties loaded: \n\n");
        for (String name : properties.stringPropertyNames()) {
            builder.append(name).append("=").append(properties.getProperty(name)).append("\n");
        }
        System.out.println(builder.toString());

        int players, votes, quota, quotaFrom, quotaTo, threads = 1, max, limit;
        long simulations = 0, count = 0;
        double epsilon = 0.001;
        boolean isDpi;

        try {
            players = Integer.parseInt(properties.getProperty("players"));
            votes = Integer.parseInt(properties.getProperty("votes"));

            quota = Integer.parseInt(properties.getProperty("quota"));
            quotaFrom = Integer.parseInt(properties.getProperty("quotaFrom"));
            quotaTo = Integer.parseInt(properties.getProperty("quotaTo"));

            max = Integer.parseInt(properties.getProperty("maxRandomVote"));
            limit = Integer.parseInt(properties.getProperty("simulationLimit"));
            simulations = Long.parseLong(properties.getProperty("simulationMC"));

            count = Long.parseLong(properties.getProperty("indexEstimationMC"));
            epsilon = Double.parseDouble(properties.getProperty("epsilon"));

            isDpi = Boolean.parseBoolean(properties.getProperty("dpi"));
            threads = Integer.parseInt(properties.getProperty("threads"));
        } catch (Exception ex) {
            System.out.println("Failed to parse properties file!");
            return null;
        }

        if (threads < 1) {
            System.out.println("Number of threads must be at least 1");
            return null;
        }

        if ((players < 2) || (players > Short.MAX_VALUE)) {
            System.out.println("Too few or too many players! (Can be 2 to 32767)");
            return null;
        }

        // If player count is too large need to do Monte-Carlo estimation
        if (players > 15 && count < 1) {
            System.out.println(
                    "Must do MC estimation with this many players! Specify an indexEstimationMC value.");
            return null;
        }

        // Every player needs at least 1 vote
        if (votes < players) {
            System.out.println("Not enough votes for players!");
            return null;
        }

        if (votes > Short.MAX_VALUE) {
            System.out.println("Too many total votes! (Can be 1 to 32767)");
            return null;
        }

        if (quotaFrom < 1) {
            System.out.println("QuotaFrom value needs to be at least one!");
            return null;
        }

        if (quotaTo < 1) {
            System.out.println("QuotaTo value needs to be at least one!");
            return null;
        }

        if (simulations < 1) {
            System.out.println("SimulationMC value needs to be at least one!");
            return null;
        }

        if (isDpi && quota < 1) {
            System.out.println("Quota value needs to be at least one!");
            return null;
        }

        if (epsilon <= 0 || epsilon >= 1) {
            System.out.println("Epsilon needs the be greater than 0 but less than 1.");
            return null;
        }

        // Set the restricted vote if it was requested
        if (max < 1)
            max = -1;

        max -= 1;

        if (limit < 1) {
            if (isDpi) {
                limit = quota;
            } else {
                limit = (quotaFrom <= quotaTo) ? quotaFrom : quotaTo;
            }
        }

        SimulationParameters parameters = new SimulationParameters();
        parameters.n = players;
        parameters.votes = votes;
        parameters.quota = quota;
        parameters.quotaFrom = quotaFrom;
        parameters.quotaTo = quotaTo;
        parameters.maximumVote = max;
        parameters.simulationLimit = limit;
        parameters.indexMonteCarloCount = count;
        parameters.tolerance = epsilon;
        parameters.monteCarloCount = simulations;
        parameters.isDpi = isDpi;

        this.threadsRequested = threads;

        return parameters;
    }

    public void saveSimulationResults(ResultDelta result) {
        double[][] shapley = result.shapley;
        double[][] banzhaf = result.banzhaf;
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
        df.applyPattern("#0.0000000");
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
            System.out.println("Results saved to file.");
        } catch (Exception ex) {
            // Failed to write the file
            ex.printStackTrace();
            System.out.println("Failed to write simulation results!");
        }
    }
}
