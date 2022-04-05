package com.github.martonr.picalc.engine.generators;

import org.junit.jupiter.api.Test;

class TestNTupleRandom {

    private final int size = 3;
    private final int values = 4;

    @Test
    void generateNTuples() {
        GeneratorNTupleRandom tGenerator = new GeneratorNTupleRandom(values);
        tGenerator.initialize(size, values);

        int count = 0, value;
        long start = System.nanoTime();
        int[] tuple;
        int[] counts = new int[values];
        for (int j = 0; j < 100; ++j) {
            tuple = tGenerator.next();
            counts[tuple[0]] += 1;

            for (int i = 0; i < size; ++i) {
                value = tuple[i];
                System.out.print(value);
            }
            System.out.println();

            count++;
        }

        for (int i = 0; i < size + 1; ++i) {
            System.out.println(i + " : " + counts[i]);
        }
        System.out.println();

        long elapsed = System.nanoTime() - start;

        System.out.println("Processed " + count + " n-tuples in " + elapsed / 1000L + " us");
    }

}
