package com.github.martonr.picalc.engine.random;

public final class PCGRandom {

    private static final int INT_MIN = 0x80000000;
    private static final long UINT_MAX = 0xFFFFFFFFL;
    private static final long PCG_MULT = 6364136223846793005L;


    private long state;
    private long increment;

    public PCGRandom() {
        // Non-deterministic seed, used to get a different generator for every instance

        long initialState;

        PCGSeeder seeder = new PCGSeeder();
        int[] seeds = new int[4];
        seeder.generate(seeds, 4);

        initialState = ((((long) seeds[1]) & UINT_MAX) << 32) | (((long) seeds[0]) & UINT_MAX);
        this.increment = ((((long) seeds[3]) & UINT_MAX) << 32) | (((long) seeds[2]) & UINT_MAX);
        this.increment = (this.increment << 1) | 1;

        this.state = 0L;
        this.state = this.state * PCG_MULT + increment; // Advance one step
        this.state = this.state + initialState;
        this.state = this.state * PCG_MULT + increment; // Advance one step
    }

    public PCGRandom(long initialState, long increment) {
        this.state = 0L;
        this.increment = (increment << 1) | 1;
        this.state = this.state * PCG_MULT + this.increment;
        this.state = this.state + initialState;
        this.state = this.state * PCG_MULT + this.increment;
    }

    public final int nextInt() {
        // PCG Random generation algorithm by Melissa O'Neill
        // Reimplemented from the minimal C version
        // http://www.pcg-random.org
        long oldState;

        oldState = this.state;
        this.state = oldState * PCG_MULT + this.increment;

        int xorShift = (int) (((oldState >>> 18) ^ oldState) >>> 27);
        int rotation = (int) (oldState >>> 59);

        return (xorShift >>> rotation) | (xorShift << (-rotation));
    }

    public final int nextInt(int bound) {
        // PCG Random bounded value generation by Melissa O'Neill
        // Reimplemented from the blog post:
        //
        // "Efficiently Generating a Number in a Range"
        // https://www.pcg-random.org/posts/bounded-rands.html

        int x = nextInt();
        // This explicit conversion to unsigned long before
        // multiplication is needed to bypass Java's
        // "sign extension" during a widening conversion
        long m = (((long) x) & UINT_MAX) * (((long) bound) & UINT_MAX);
        int l = (int) m;

        int uBound = bound + INT_MIN;
        // This is an inlined form of Integer.compareUnsigned()
        if ((l + INT_MIN) < uBound) {
            int t = -bound;

            // This optimizes modulo
            if ((t + INT_MIN) >= uBound) {
                t -= bound;

                if ((t + INT_MIN) >= uBound) {
                    // This is unsigned modulo with signed integers
                    // Bound is always positive here
                    // From "Hacker's Delight", section 9.3
                    final int q = ((t >>> 1) / bound) << 1;
                    final int r = t - q * bound;

                    // Inlined from Long.remainderUnsigned()
                    // Adapted to integers
                    t = r - ((~(r - bound) >> 31) & bound);
                }
            }

            // Threshold based discarding of values
            t += INT_MIN;
            while ((l + INT_MIN) < t) {
                x = nextInt();
                m = (((long) x) & UINT_MAX) * (((long) bound) & UINT_MAX);
                l = (int) m;
            }
        }
        return (int) (m >>> 32);
    }

    public final int nextBit() {
        return (nextInt() & 1);
    }
}
