package com.github.martonr.picalc.engine.generators;

import com.github.martonr.picalc.engine.random.PCGRandom;

public final class GeneratorNTupleRandom {

    private final int[] tuple;

    private final PCGRandom random;

    private int m;

    public int n;

    public GeneratorNTupleRandom(int max) {
        this.tuple = new int[max];
        this.random = new PCGRandom();
    }

    public final void initialize(int n, int m) {
        this.n = n;
        this.m = m;
    }

    public final int[] next() {
        for (int i = 0; i < n; ++i)
            tuple[i] = random.nextInt(m);

        return tuple;
    }
}
