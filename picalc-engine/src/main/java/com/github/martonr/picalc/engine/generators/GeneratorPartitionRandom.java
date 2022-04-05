package com.github.martonr.picalc.engine.generators;

import com.github.martonr.picalc.engine.random.PCGRandom;

public final class GeneratorPartitionRandom {

    private final int[] ones;

    private final int p;

    private final int[] partition;
    private final int[] helper;

    private final PCGRandom random;

    private int q;

    private int max;
    private int possibleMax;

    public GeneratorPartitionRandom(int p) {
        this.p = p;
        this.partition = new int[p + 1];
        this.helper = new int[p + 1];
        this.ones = new int[p];

        for (int i = 0; i < p; ++i)
            ones[i] = 1;

        this.random = new PCGRandom();
    }

    public final void initialize(int q, int max, int set) {
        this.q = q;
        this.partition[p] = set;
        this.helper[p] = q;

        // If max < (q / p), there are no possible partitions
        int ratio = q / p;
        ratio = q > (ratio * p) ? (ratio + 1) : ratio;

        this.possibleMax = q - p + 1;
        if (max < ratio || max > possibleMax)
            // The max provided in the parameters is not valid
            this.max = possibleMax;
        else
            this.max = max;
    }

    public final int[] newNext() {
        if (q == p) {
            System.arraycopy(ones, 0, partition, 0, p);
            return partition;
        }

        int possibleMax = max;
        int remaining = q - p + 1;
        int val = 0, got = 0;
        while (got < p - 1) {
            val = random.nextInt(possibleMax);
            if (remaining - val < 1)
                continue;

            partition[got] = val + 1;
            remaining -= val;
            got++;
        }
        partition[p - 1] = remaining;

        return partition;
    }

    // public final int[] newNext() {
    // if (q == p) {
    // // Easy case
    // System.arraycopy(ones, 0, partition, 0, p);
    // return partition;
    // }

    // int sz = 0;
    // boolean found = false;
    // while (sz < (p - 1)) {
    // int candidate = random.nextInt(q - 1) + 1;
    // found = false;
    // for (int i = 1; i <= sz; ++i) {
    // if (helper[i] == candidate) {
    // found = true;
    // break;
    // }
    // }
    // if (!found) {
    // sz++;
    // helper[sz] = candidate;
    // }
    // }

    // Arrays.sort(helper);

    // for (int i = 0; i < p; ++i) {
    // partition[i] = helper[i + 1] - helper[i];
    // }

    // return partition;
    // }

    public final int[] next() {
        if (q == p) {
            // Easy case
            System.arraycopy(ones, 0, partition, 0, p);
            return partition;
        }

        // Generate partitions until no elements are above max
        // Since the partitions are based on compositions, the generated
        // K sized partitions do not have a uniform distribution

        // This means ex. for a partition of 6 with 2 elements,
        // the partition {5, 1} is twice as likely as {3, 3}

        // A vote scenario {5, 1} and {1, 5} are different scenarios,
        // but they are the same partition even though for the
        // power index of the set player there is no difference

        // Randomly sampling this way is much easier than sampling K sized partitions uniformly
        // which require picking a number between 1 and the number of K sized partitions of N
        // This number can be huge, larger than what a 64-bit number can hold even for
        // moderate parameters ex. N = 600, K = 30
        //
        // For an algorithm for uniform sampling of k sized partitions see:
        // "Efficient algorithms for sampling feasible sets of abundance distributions"
        // by Kenneth J. Locey, 2014
        int m, t, l = 0;
        int n = this.q - this.p;
        int k = this.p - 1;
        int v = this.max + 1;
        int nk = n + k;

        // Reject partitions that have an element above max
        while (v > max || l > max) {
            m = t = l = v = 0;

            // Combination of two algorithms:
            //
            // Algorithm S:
            // Selection sampling for a random combination
            // Knuth - "The Art of Computer Programming" Volume 2, 3.4.2
            //
            // RANCOM:
            // Random composition of N into K parts
            // Nijenhuis & Wilf - "Combinatorial Algorithms for Computers and Calculators"

            // Find random composition of N - K elements, then add 1 to each part
            while (m < k) {
                if (random.nextInt(nk - t) < k - m) {
                    v = t - l + 1;
                    if (v > max)
                        break;

                    // partition[m++] = t - l + 1;
                    partition[m++] = v;
                    l = t + 1;
                }
                t++;
            }

            l = nk - l + 1;
            partition[k] = l;
        }

        return partition;
    }
}
