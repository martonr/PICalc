package com.github.martonr.picalc.engine.generators;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TestCombinationRandom {

    private final int size = 5;
    private final int count = 1000000;

    @Test
    void generateRCombinations() {
        GeneratorCombinationRandom cGenerator = new GeneratorCombinationRandom(size);

        int m;
        int[] counts = new int[size + 1];
        long start = System.nanoTime();
        // int[] combination;
        for (long j = count; j > 0; --j) {
            cGenerator.next();
            m = cGenerator.m;
            counts[m] += 1;
            /*
             * for (int i = 1; i <= t; ++i) { value = combination[i]; System.out.print(value); }
             * System.out.println();
             */
        }

        int sum = 0;
        for (int i = 0; i < size + 1; ++i) {
            sum += counts[i];
            System.out.println(i + " : " + counts[i]);
        }
        System.out.println();

        long elapsed = System.nanoTime() - start;

        System.out.println(
                "Processed " + count + " random combinations in " + elapsed / 1000L + " us");

        Assertions.assertEquals(count, sum);
    }
}
