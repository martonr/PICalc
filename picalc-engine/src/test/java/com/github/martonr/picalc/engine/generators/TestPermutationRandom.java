package com.github.martonr.picalc.engine.generators;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TestPermutationRandom {

    private final int size = 6;
    private final int count = 100000;

    @Test
    void generateRPermutations() {
        GeneratorPermutationRandom pGenerator = new GeneratorPermutationRandom(size);

        int value;
        int[] counts = new int[size];
        long start = System.nanoTime();
        int[] permutation;
        for (long j = count; j > 0; --j) {
            permutation = pGenerator.next();
            for (int i = 0; i < size; ++i) {
                value = permutation[i];
                if (value == 0)
                    counts[i] += 1;
            }
        }

        int sum = 0;
        for (int i = 0; i < size; ++i) {
            sum += counts[i];
            System.out.println((i + 1) + " : " + counts[i]);
        }
        System.out.println();

        long elapsed = System.nanoTime() - start;

        System.out.println(
                "Processed " + count + " random permutations in " + elapsed / 1000L + " us");

        Assertions.assertEquals(count, sum);
    }
}
