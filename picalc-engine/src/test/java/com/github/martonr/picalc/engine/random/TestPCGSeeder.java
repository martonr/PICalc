package com.github.martonr.picalc.engine.random;

import org.junit.jupiter.api.Test;

class TestPCGSeeder {

    @Test
    void generateAutoSeeds() {
        int seedCount = 32;
        int[] seeds = new int[seedCount];
        long duration, start = System.nanoTime();

        PCGSeeder seeder = new PCGSeeder();

        seeder.generate(seeds, seedCount);

        for (int i = 0; i < seedCount; ++i) {
            if (i % 4 == 0)
                System.out.println(" 0x" + Integer.toUnsignedString(seeds[i], 16));
        }
        System.out.println();

        duration = System.nanoTime() - start;

        System.out.println("Generated " + seedCount + " auto seeds in " + duration / 1000L + " us");
        System.out.println("\n");
    }

    @Test
    void generateSetSeeds() {
        int seedCount = 32;
        int[] seeds = new int[seedCount];
        long duration, start = System.nanoTime();

        PCGSeeder seeder = new PCGSeeder(1, 2, 3, 4);

        seeder.generate(seeds, seedCount);

        for (int i = 0; i < seedCount; ++i) {
            if (i % 4 == 0)
                System.out.println(" 0x" + Integer.toUnsignedString(seeds[i], 16));
        }
        System.out.println();

        duration = System.nanoTime() - start;

        System.out.println("Generated " + seedCount + " set seeds in " + duration / 1000L + " us");
        System.out.println("\n");
    }
}
