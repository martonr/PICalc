package com.github.martonr.picalc.engine.random;

import org.junit.jupiter.api.Test;

class TestPCGRandom {

    @Test
    void generateRandomBits() {
        int countOnes = 0, countZeroes = 0, r;
        long duration, start = System.nanoTime();

        PCGRandom rnd = new PCGRandom();

        for (int i = 0; i < 1000000; ++i) {
            r = rnd.nextBit();
            if (r == 1) {
                countOnes++;
            } else if (r == 0) {
                countZeroes++;
            }
        }
        duration = System.nanoTime() - start;

        System.out.println("Generated a million bits in " + duration / 1000L + " us");
        System.out.println("Ones: " + countOnes + " zeroes: " + countZeroes);
        System.out.println("\n");
    }

    @Test
    void generateKnownGoodNumbers() {
        // The output of this test needs to match the output of the minimal C
        // implementation on the PCG website
        PCGRandom rnd = new PCGRandom(42L, 54L);

        for (int j = 1; j <= 5; ++j) {
            System.out.println("Round: " + j);
            System.out.print("  32bit:");
            for (int i = 0; i < 6; ++i) {
                System.out.print(" 0x");
                System.out.print(Integer.toUnsignedString(rnd.nextInt(), 16));
            }
            System.out.println();

            System.out.print("  coins: ");
            for (int i = 0; i < 65; ++i) {
                System.out.print(rnd.nextInt(2) == 1 ? "H" : "T");
            }
            System.out.println();

            System.out.print("  rolls:");
            for (int i = 0; i < 33; ++i) {
                System.out.print(" " + (rnd.nextInt(6) + 1));
            }
            System.out.println();

            int[] cards = new int[52];
            for (int i = 0; i < cards.length; ++i) {
                cards[i] = i;
            }

            for (int i = cards.length; i > 1; --i) {
                int chosen = rnd.nextInt(i);
                int card = cards[chosen];
                cards[chosen] = cards[i - 1];
                cards[i - 1] = card;
            }

            System.out.print("  cards:");
            char[] number = {'A', '2', '3', '4', '5', '6', '7', '8', '9', 'T', 'J', 'Q', 'K'};
            char[] suit = {'h', 'c', 'd', 's'};
            for (int i = 0; i < cards.length; ++i) {
                System.out.print(" " + number[cards[i] / 4] + suit[cards[i] % 4]);
                if ((i + 1) % 22 == 0) {
                    System.out.println();
                    System.out.print("        ");
                }
            }
            System.out.println();
        }
        System.out.println("\n");
    }

    @Test
    void generateManyUnbounded() {
        long duration, start = System.nanoTime();

        PCGRandom rnd = new PCGRandom();
        for (int i = 0; i < 1000000; ++i) {
            rnd.nextInt(7);
        }
        duration = System.nanoTime() - start;

        System.out.println("Generated a million integers in " + duration / 1000L + " us");

        start = System.nanoTime();
        for (int i = 0; i < 1000000; ++i) {
            rnd.nextInt(17);
        }
        duration = System.nanoTime() - start;

        System.out.println("Generated a million integers in " + duration / 1000L + " us");

        start = System.nanoTime();
        for (int i = 0; i < 1000000; ++i) {
            rnd.nextInt(23);
        }
        duration = System.nanoTime() - start;

        System.out.println("Generated a million integers in " + duration / 1000L + " us");
        System.out.println("\n");
    }
}
