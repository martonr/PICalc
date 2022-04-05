package com.github.martonr.picalc.engine.random;

import java.util.Random;

public final class PCGSeeder {

    private static final int INIT_A = 0x43B0D7E5;
    private static final int MULT_A = 0x931E8875;

    private static final int INIT_B = 0x8B51F9DD;
    private static final int MULT_B = 0x58F38DED;

    private static final int MIX_MULT_L = 0xCA01F9DD;
    private static final int MIX_MULT_R = 0x4973F715;

    private static final int SIZE = 4;

    private static int GLOBAL_COUNTER = new Random().nextInt();

    private final int[] mixer = new int[SIZE];

    private int hashConst = INIT_A;
    private int eIdx = 0;

    public PCGSeeder() {
        this.mixer[0] = crushToInt(System.nanoTime());
        this.mixer[1] = System.identityHashCode(new Object());
        this.mixer[2] = new Random().nextInt();
        this.mixer[3] = GLOBAL_COUNTER;

        // This is an intentional race condition possibility.
        // If a race condition happens it just provides more
        // entropy to the next invocation.
        GLOBAL_COUNTER = GLOBAL_COUNTER + 0xEDF19156;

        // Lots of mixing to compensate for low entropy source values
        for (int i = 0; i < SIZE; ++i) {
            mix_entropy();
        }

        hashConst = INIT_B;
    }

    public PCGSeeder(int val1, int val2, int val3, int val4) {
        this.mixer[0] = val1;
        this.mixer[1] = val2;
        this.mixer[2] = val3;
        this.mixer[3] = val4;

        for (int i = 0; i < SIZE; ++i) {
            mix_entropy();
        }

        hashConst = INIT_B;
    }

    private final int crushToInt(long value) {
        long x = value;
        x = x * 0xBC2AD017D719504DL;

        return (int) (x ^ (x >>> 32));
    }

    private final int hash(int value) {
        int x = value ^ hashConst;
        hashConst = hashConst * MULT_A;
        x = x * hashConst;
        x = x ^ (x >>> 16);

        return x;
    }

    private final int mix(int valA, int valB) {
        int x = (MIX_MULT_L * valA) - (MIX_MULT_R * valB);
        x = x ^ (x >>> 16);

        return x;
    }

    private final void mix_entropy() {
        for (int i = 0; i < SIZE; ++i) {
            mixer[i] = hash(mixer[i]);
        }

        for (int j = 0; j < SIZE; ++j) {
            for (int i = 0; i < SIZE; ++i) {
                if (j != i) {
                    mixer[i] = mix(mixer[i], hash(mixer[j]));
                }
            }
        }
    }

    public final void generate(int[] seeds, int count) {
        int x = 0;

        for (int i = 0; i < count; ++i) {
            x = mixer[eIdx];

            if (++eIdx == SIZE)
                eIdx = 0;

            x = x ^ hashConst;
            hashConst = hashConst * MULT_B;
            x = x * hashConst;
            x = x ^ (x >>> 16);

            seeds[i] = x;
        }
    }
}
