package com.github.martonr.picalc.engine.generators;

public final class GeneratorCombination {

    private final int[] zeros;

    private final int n;

    private final int[] combination;

    private final int[] helper;

    public int m;

    public boolean hasNext;

    public GeneratorCombination(int n) {
        this.n = n;
        this.m = n + 1;
        this.hasNext = (n > 0);

        // Initialize arrays
        this.zeros = new int[n + 2];
        this.combination = new int[n];
        this.helper = new int[n + 2];

        helper[0] = n + 1;
        helper[1] = 1;
        helper[n + 1] = -2;
    }

    public void reset() {
        this.m = n + 1;
        this.hasNext = true;

        System.arraycopy(zeros, 0, combination, 0, n);
        System.arraycopy(zeros, 0, helper, 0, n + 2);

        helper[0] = n + 1;
        helper[1] = 1;
        helper[n + 1] = -2;
    }

    public final int[] next() {
        // Algorithm 382 or Chase's Twiddle
        // "Combinations of M out of N objects" by Phillip J. Chase, 1970
        int i, j = 0;

        // L1
        while (helper[++j] < 1);

        if (helper[j - 1] == 0) {
            for (i = j - 1; i > 1; --i)
                helper[i] = -1;
            helper[j] = 0;
            helper[1] = 1;

            combination[0] = 0;
            return combination;
        }

        if (j > 1)
            helper[j - 1] = 0;

        // L2
        while (helper[++j] > 0);

        i = j - 1;

        // L3
        while (helper[++i] == 0)
            helper[i] = -1;

        if (helper[i] == -1) {
            helper[i] = helper[j - 1];
            helper[j - 1] = -1;

            combination[helper[i] - 1] = i - 1;
            return combination;
        }

        // If true all t sized combinations have been returned
        if (i == helper[0]) {
            // Decrement the subset size
            m -= 1;

            hasNext = (m > 0);

            // Initialize for the next m sized combination
            for (int k = 1; k <= n - m; ++k)
                helper[k] = 0;

            for (int k = 1; k <= m; ++k) {
                combination[k - 1] = n - m + k - 1;
                helper[n - m + k] = k;
            }

            return combination;
        }

        helper[j] = helper[i];
        helper[i] = 0;

        combination[helper[j] - 1] = j - 1;
        // L4
        return combination;
    }
}
