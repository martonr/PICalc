package com.github.martonr.picalc.engine.generators;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TestCombination {

    private final int size = 5;

    @Test
    void generateCombinations() {
        GeneratorCombination cGenerator = new GeneratorCombination(size);

        int count = 0, value;
        long start = System.nanoTime();
        int[] combination;
        while (cGenerator.hasNext) {
            combination = cGenerator.next();
            for (int i = 0; i < cGenerator.m; ++i) {
                value = combination[i];
                System.out.print(value);
            }
            System.out.println();
            count++;
        }
        long elapsed = System.nanoTime() - start;

        System.out.println("Processed " + count + " combinations in " + elapsed / 1000L + " us");

        Assertions.assertEquals(32, count);
    }
}
