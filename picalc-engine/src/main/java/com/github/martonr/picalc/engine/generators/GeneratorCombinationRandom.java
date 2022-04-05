package com.github.martonr.picalc.engine.generators;

import com.github.martonr.picalc.engine.random.PCGRandom;

public final class GeneratorCombinationRandom {

    private final int n;

    private final int[] combination;

    private final PCGRandom random;

    public int m;

    public GeneratorCombinationRandom(int n) {
        this.n = n;
        this.combination = new int[n];

        this.random = new PCGRandom();
    }

    public final int[] next() {
        int r, k = 0;

        // Pick a random sized combination including null
        // This gives a combination size distribution similar
        // to the all combinations distribution (binomial distribution)
        // This is required for the power index estimations to be correct
        for (int i = 0; i < n; ++i) {
            r = random.nextBit();
            combination[k] = r * i;
            k += r;
        }

        this.m = k;

        return combination;

        // The below algorithm produces combinations whose size is uniformly distributed
        // So a size 0 combination is equally likely as a size 2
        // However since there are more size 2 combinations as there are size 0
        // from the set of all possible combinations, size 0 is overrepresented
        // This changes the power index estimations which require a representative sampling
        // from all possible combinations
        // int t = 0, m = 0, k = 0;

        /*
         * k = random.nextInt(n + 1); combination[0] = k;
         * 
         * if (k == 0) { count -= 1; hasNext = count > 0;
         * 
         * return combination; }
         * 
         * // k = random.nextInt(n) + 1; // combination[0] = k;
         * 
         * // Algorithm S - Selection sampling for a random combination // Knuth
         * "The Art of Computer Programming" Volume 2, 3.4.2 while (m < k) { if (random.nextInt(n -
         * t) < k - m) combination[++m] = t; ++t; }
         */
    }
}
