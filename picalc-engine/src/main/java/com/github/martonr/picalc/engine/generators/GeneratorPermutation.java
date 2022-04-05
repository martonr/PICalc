package com.github.martonr.picalc.engine.generators;

public final class GeneratorPermutation {

    private final int[] zeros;
    private final int[] start;

    private final int n;

    private final int[] permutation;

    private final int[] helper;

    public boolean hasNext;

    public GeneratorPermutation(int n) {
        this.n = n;
        this.hasNext = (n > 0);

        this.permutation = new int[n];
        this.helper = new int[n];
        this.zeros = new int[n];
        this.start = new int[n];

        for (int i = 0; i < n; ++i) {
            this.permutation[i] = i;
            this.start[i] = i;
        }
    }

    public final void reset() {
        this.hasNext = true;

        System.arraycopy(zeros, 0, helper, 0, n);
        System.arraycopy(start, 0, permutation, 0, n);
    }

    public final int[] next() {
        // Iterative version of Heap's algorithm
        // Algorithm 2 in "Permutation Generation Methods" by Robert Sedgewick, 1977
        int k, swap;
        for (int i = 1; i < n; ++i) {
            if (helper[i] < i) {
                // i is never negative, use i & 1 for checking if even instead of i % 2
                // Branchless version of ((i & 1) == 0) ? 0 : helper[i]
                k = (i & 1) * helper[i];

                // Swap values
                swap = permutation[i];
                permutation[i] = permutation[k];
                permutation[k] = swap;

                helper[i] += 1;
                return permutation;
            }
            helper[i] = 0;
        }

        hasNext = false;
        // Last permutation is the starting one
        for (int i = 0; i < n; ++i)
            permutation[i] = i;

        return permutation;
    }
}
