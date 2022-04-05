package com.github.martonr.picalc.engine.generators;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.io.BufferedWriter;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

class TestPartitionRandom {

    @Test
    void generateDistributionData() {
        int n = 300;
        int k = 5;
        int max = -1;
        int number = 10000;

        GeneratorPartitionRandom generator = new GeneratorPartitionRandom(k);

        generator.initialize(n, max, -1);

        int count = 0;
        BufferedWriter bw = null;
        while (bw == null) {
            try {
                String fileName = "./" + number + "_" + n + "_" + k;
                if (max > 0) {
                    fileName += "_";
                    fileName += max;
                }

                fileName += "-";
                fileName += count;

                bw = Files.newBufferedWriter(Files.createFile(Paths.get(fileName + ".csv")));
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

        try {
            int[] votes;
            // Create the header
            StringBuilder sb = new StringBuilder("");

            for (int i = 1; i < k; ++i) {
                sb.append("p").append(i).append(",");
            }
            sb.append("p").append(k).append("\n");

            // Save the values
            for (int i = 0; i < number; ++i) {
                votes = generator.newNext();
                for (int j = 0; j < k - 1; ++j) {
                    sb.append(votes[j]).append(",");
                }
                sb.append(votes[k - 1]).append("\n");

                bw.write(sb.toString());
                sb.setLength(0);
            }

            bw.flush();
            bw.close();
            System.out.println("Generated votes written to file.");
        } catch (Exception ignored) {
            // Failed to write the file
        }
    }

    @Test
    void generatePartitionsWithMax() {
        final int n = 16;
        final int k = 6;
        final int max = 7;

        GeneratorPartitionRandom pGenerator = new GeneratorPartitionRandom(k);

        pGenerator.initialize(n, max, 0);

        int counter = 0, value, s;
        long start = System.nanoTime();
        int[] partition;
        for (int j = 0; j < 50; ++j) {
            partition = pGenerator.next();
            s = 0;
            for (int i = 0; i < k; ++i) {
                value = partition[i];
                Assertions.assertTrue(value <= n);
                s += value;
                System.out.print(value + " ");
            }
            System.out.println();

            Assertions.assertEquals(n, s);
            counter++;
        }
        long elapsed = System.nanoTime() - start;

        System.out.println();
        System.out.println(
                "Processed " + counter + " random partitions in " + elapsed / 1000L + " us");
        System.out.println();
    }

    @Test
    void generatePartitionDistribution() {
        final int n = 6;
        final int k = 6;
        final int max = -1;
        final int count = 10000000;

        GeneratorPartitionRandom cGenerator;

        int counter = 0, value, s;
        long start = System.nanoTime();
        int[] partition;
        int[] partitions = new int[11];

        for (int j = 1; j <= k; ++j) {

            cGenerator = new GeneratorPartitionRandom(j);
            cGenerator.initialize(n, max, 100);

            for (int h = 0; h < count; ++h) {
                partition = cGenerator.next();
                s = 0;
                for (int i = 0; i < j; ++i) {
                    value = partition[i];
                    Assertions.assertTrue(value <= n);
                    s += value;
                }

                Assertions.assertEquals(n, s);
                Arrays.sort(partition);

                if (j == 1) {
                    // Partition 6 into 1 partitions
                    // This is the partition of { 6 }
                    partitions[0] += 1;
                } else if (j == 2) {
                    // Partition 6 into 2 partitions
                    if (partition[j - 1] == 3)
                        // This is the partition of:
                        // { 3, 3 }
                        partitions[1] += 1;
                    if (partition[j - 1] == 4)
                        // This is the partition of:
                        // { 2, 4 }
                        // { 4, 2 }
                        partitions[2] += 1;
                    if (partition[j - 1] == 5)
                        // This is the partition of:
                        // { 1, 5 }
                        // { 5, 1 }
                        partitions[3] += 1;
                } else if (j == 3) {
                    // Partition 6 into 3 partitions
                    if (partition[j - 1] == 2)
                        // This is the partition of:
                        // { 2, 2, 2 }
                        partitions[4] += 1;
                    if (partition[j - 1] == 3)
                        // This is the partition of:
                        // { 1, 2, 3 }
                        // { 2, 1, 3 }
                        // { 1, 3, 2 }
                        // { 2, 3, 1 }
                        // { 3, 1, 2 }
                        // { 3, 2, 1 }
                        partitions[5] += 1;
                    if (partition[j - 1] == 4)
                        // This is the partition of:
                        // { 1, 1, 4 }
                        // { 1, 4, 1 }
                        // { 4, 1, 1 }
                        partitions[6] += 1;
                } else if (j == 4) {
                    // Partition 6 into 4 partitions
                    if (partition[j - 1] == 2)
                        // This is the partition of:
                        // { 1, 1, 2, 2 }
                        // { 1, 2, 1, 2 }
                        // { 2, 1, 1, 2 }
                        // { 1, 2, 2, 1 }
                        // { 2, 1, 2, 1 }
                        // { 2, 2, 1, 1 }
                        partitions[7] += 1;
                    if (partition[j - 1] == 3)
                        // This is the partition of:
                        // { 1, 1, 1, 3 }
                        // { 1, 1, 3, 1 }
                        // { 1, 3, 1, 1 }
                        // { 3, 1, 1, 1 }
                        partitions[8] += 1;
                } else if (j == 5) {
                    // Partition 6 into 5 partitions
                    // This is the partition of:
                    // { 1, 1, 1, 1, 2 }
                    // { 1, 1, 1, 2, 1 }
                    // { 1, 1, 2, 1, 1 }
                    // { 1, 2, 1, 1, 1 }
                    // { 2, 1, 1, 1, 1 }
                    partitions[9] += 1;
                } else {
                    // Partition 6 into 6 partitions
                    // This is the partition of:
                    // { 1, 1, 1, 1, 1, 1 }
                    partitions[10] += 1;
                }

                counter++;
            }
        }
        long elapsed = System.nanoTime() - start;

        System.out.println();

        int sum = 0;
        for (int i = 0; i < 11; ++i) {
            sum += partitions[i];
            System.out.println((i + 1) + " : " + partitions[i]);
        }

        System.out.println();
        System.out.println(
                "Processed " + counter + " random partitions in " + elapsed / 1000L + " us");
        System.out.println();

        Assertions.assertEquals(k * count, sum);
    }
}
