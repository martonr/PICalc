package com.github.martonr.picalc.engine.generators;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TestNTuple {

    private final int size = 3;
    private final int values = 4;

    @Test
    void generateNTuples() {
        GeneratorNTuple tGenerator = new GeneratorNTuple(values);
        tGenerator.initialize(size, values);

        int count = 0, value;
        long start = System.nanoTime();
        int[] tuple;
        while (tGenerator.hasNext) {
            tuple = tGenerator.next();
            for (int i = 0; i < size; ++i) {
                value = tuple[i];
                System.out.print(value);
            }
            System.out.println();
            count++;
        }
        long elapsed = System.nanoTime() - start;

        System.out.println("Processed " + count + " n-tuples in " + elapsed / 1000L + " us");

        // The amount equals values ^ size
        Assertions.assertEquals((int) Math.pow(values, size), count);
    }
}
