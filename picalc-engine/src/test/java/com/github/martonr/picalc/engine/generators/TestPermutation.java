package com.github.martonr.picalc.engine.generators;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TestPermutation {

    private final int size = 5;

    @Test
    void generatePermutations() {
        GeneratorPermutation pGenerator = new GeneratorPermutation(size);

        int count = 0, value;
        long start = System.nanoTime();
        int[] permutation;
        while (pGenerator.hasNext) {
            permutation = pGenerator.next();
            for (int i = 0; i < size; ++i) {
                value = permutation[i];
                System.out.print(value);
            }
            System.out.println();
            count++;
        }
        long elapsed = System.nanoTime() - start;

        System.out.println("Processed " + count + " permutations in " + elapsed / 1000L + " us");

        Assertions.assertEquals(120, count);
    }
}
