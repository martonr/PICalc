package com.github.martonr.picalc.engine.generators;

import com.github.martonr.picalc.engine.random.PCGRandom;

public final class GeneratorPermutationRandom {

    private final int n;

    private final int[] permutation;

    private final PCGRandom random;

    public GeneratorPermutationRandom(int n) {
        this.n = n;
        this.permutation = new int[n];

        for (int i = 0; i < n; ++i)
            permutation[i] = i;

        this.random = new PCGRandom();
    }

    public final int[] next() {
        int j, swap;

        for (int i = n - 1; i > 0; --i) {
            j = random.nextInt(i + 1);

            swap = permutation[j];
            permutation[j] = permutation[i];
            permutation[i] = swap;
        }

        return permutation;
    }
}
