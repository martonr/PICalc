package com.github.martonr.picalc.engine.generators;

public final class GeneratorNTuple {

    private final int[] zeros;

    private final int[] tuple;

    private int m;

    public int n;

    public boolean hasNext;

    public GeneratorNTuple(int max) {
        // Set to the maximum possible size
        this.tuple = new int[max];
        this.zeros = new int[max];
    }

    public final void initialize(int n, int m) {
        this.n = n;
        this.m = m;
        this.hasNext = (n > 0) && (m > 0);

        System.arraycopy(zeros, 0, tuple, 0, n);
    }

    public final int[] next() {
        // Algorithm by Siegfried Koepf
        // inspired by Algorithm M in: Knuth: The Art of Computer Programming, Vol. 4:
        // Fascicle 2. Generating All Tuples and Permutations.
        // http://www.aconnect.de/friends/editions/computer/combinatoricode_e.html
        // "Variations with repetition in lexicographic order"
        int lastIndex = n - 1, maxValue = m - 1;

        // Increase the last element
        if (tuple[lastIndex] < maxValue) {
            tuple[lastIndex]++;
            return tuple;
        }

        // Find the first tuple that is less than lastIndex
        // Zero the others
        int j;
        for (j = lastIndex - 1; j >= 0; j--) {
            tuple[j + 1] = 0;

            if (tuple[j] < maxValue)
                break;
        }

        // All elements are at their max value, so return the initial tuple,
        // since that was not returned at the start
        if (j < 0) {
            tuple[0] = 0;
            hasNext = false;
            return tuple;
        }

        // Increase the element, then return
        tuple[j]++;
        return tuple;
    }
}
