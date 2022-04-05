package com.github.martonr.picalc.engine.service;

import java.util.concurrent.atomic.AtomicLong;
import com.github.martonr.picalc.engine.random.PCGRandom;

public final class SimulationCache {

    private static final int HT_SIZE = 24;
    private static final int MAX_PROMOTIONS = (1 << 23);

    private final Entry[] hashTable;
    private final int[] lookupTable;

    private final Entry head;
    private final Entry free;

    private final int allVotes;
    private final int mask;
    private final int max;
    private final int n;

    private final AtomicLong hits = new AtomicLong(0);
    private final AtomicLong stores = new AtomicLong(0);
    private volatile long promotions = 0;
    private volatile int size = 0;

    public SimulationCache(int n, int v) {
        this(n, v, HT_SIZE);
    }

    public SimulationCache(int n, int v, int htSize) {
        this.n = n;
        this.allVotes = v;
        // Keep the table at 50% occupancy
        this.max = (1 << htSize);

        int tableSize = (max << 1);
        this.mask = tableSize - 1;
        this.hashTable = new Entry[tableSize];

        for (int i = 0; i < tableSize; ++i)
            this.hashTable[i] = new Entry();

        this.head = new Entry();
        this.head.newer = this.head;
        this.head.older = this.head;
        this.free = new Entry();
        this.free.newer = this.free;
        this.free.older = this.free;

        Entry e;
        for (int i = 0; i < 16; ++i) {
            e = new Entry(n);
            e.older = free;
            e.newer = free.newer;
            e.newer.older = e;
            e.older.newer = e;
        }

        PCGRandom random = new PCGRandom();
        this.lookupTable = new int[v];

        // Make sure every value is unique
        int c, f = 0;
        boolean found;
        for (;;) {
            c = random.nextInt();

            found = false;
            for (int i = 0; i < f; ++i) {
                if (lookupTable[i] == c) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                lookupTable[f] = c;
                f++;
            }

            if (f == v)
                break;
        }
    }

    public final EntryChecker createNewChecker() {
        return new EntryChecker(this.allVotes);
    }

    public final long getHits() {
        return hits.get();
    }

    public final long getStores() {
        return stores.get();
    }

    public final int getSize() {
        return size;
    }

    private final void computeHash(EntryChecker checker) {
        // Zobrist hashing
        int result = 0;
        for (int i = 0; i < n; ++i)
            // This way two vote distributions in different order
            // will hash to the same value
            result += lookupTable[checker.votes[i] - 1];

        checker.hashCode = result;
    }

    public final boolean get(EntryChecker checker) {
        final int hashCode = checker.hashCode;

        Entry entry;
        synchronized (hashTable[hashCode & mask]) {
            entry = hashTable[hashCode & mask].next;
            while (entry != null) {
                if (entry.hashCode == hashCode && checker.checkEqualAndFind(entry)) {
                    hits.incrementAndGet();
                    long p = promotions;
                    long diff = p - entry.seenPromotions;
                    if (diff > MAX_PROMOTIONS || diff < 0) {
                        synchronized (head) {
                            if (entry != head.older && entry.newer != null)
                                promote(entry, p);
                        }
                    }
                    return true;
                }
                entry = entry.next;
            }
        }
        return false;
    }

    public final void store(EntryChecker checker, double[] ssDelta, double[] bfDelta) {
        final int hashCode = checker.hashCode;

        Entry entry;
        Entry previous;
        Entry toBeFreed = null;
        synchronized (hashTable[hashCode & mask]) {
            entry = hashTable[hashCode & mask].next;
            previous = hashTable[hashCode & mask];
            while (entry != null) {
                if (entry.hashCode == hashCode && checker.checkEqualAndFind(entry)) {
                    // Entry was inserted in the meantime
                    return;
                }
                previous = entry;
                entry = entry.next;
            }

            Entry e = getFreeNode();

            synchronized (head) {
                if (size >= max) {
                    // Remove tail from list
                    toBeFreed = head.newer;
                    toBeFreed.older.newer = toBeFreed.newer;
                    toBeFreed.newer.older = toBeFreed.older;
                    toBeFreed.newer = null;
                    toBeFreed.older = null;
                    toBeFreed.seenPromotions = 0;
                } else {
                    size++;
                }

                e.newer = head;
                e.older = head.older;
                e.older.newer = e;
                e.newer.older = e;

                e.seenPromotions = promotions;
                if (promotions == Long.MAX_VALUE)
                    promotions = 0;
                else
                    promotions++;
            }

            e.hashCode = hashCode;
            System.arraycopy(checker.votes, 0, e.votes, 0, n);
            System.arraycopy(ssDelta, 0, e.ssDelta, 0, n);
            System.arraycopy(bfDelta, 0, e.bfDelta, 0, n);

            // Insert into the hashTable
            previous.next = e;
            e.prev = previous;
        }
        stores.incrementAndGet();

        // Remove LRU entry from the list and table
        if (toBeFreed != null) {
            free(toBeFreed);
            addFreeNode(toBeFreed);
        } else {
            addFreeNode(new Entry(n));
        }
    }

    private final void promote(Entry e, long p) {
        // The head is always newer than the first and older than the last
        e.seenPromotions = p;
        if (promotions == Long.MAX_VALUE)
            promotions = 0;
        else
            promotions++;
        // entry was somewhere in the chain
        // First remove it from the chain
        e.older.newer = e.newer;
        e.newer.older = e.older;

        // Insert it as first
        e.newer = head;
        e.older = head.older;
        e.older.newer = e;
        e.newer.older = e;
    }

    private final void free(Entry e) {
        synchronized (hashTable[e.hashCode & mask]) {
            // Remove us from the hashtable
            e.prev.next = e.next;
            if (e.next != null)
                e.next.prev = e.prev;

            // else we were the last in the chain, nothing more to do
            e.prev = null;
            e.next = null;
            e.hashCode = 0;
        }
    }

    private final Entry getFreeNode() {
        Entry result = null;
        for (;;) {
            synchronized (free) {
                if (free.older != free) {
                    // There are available free nodes
                    result = free.older;
                    result.older.newer = result.newer;
                    result.newer.older = result.older;
                    result.older = null;
                    result.newer = null;

                    return result;
                }
            }
            Thread.onSpinWait();
        }
    }

    private final void addFreeNode(Entry e) {
        synchronized (free) {
            // Insert to tail
            e.older = free;
            e.newer = free.newer;
            e.newer.older = e;
            e.older.newer = e;
        }
    }

    private static final class Entry {
        private volatile int hashCode;
        private volatile long seenPromotions;
        private volatile Entry next;
        private volatile Entry prev;

        private volatile Entry newer;
        private volatile Entry older;

        private final double[] ssDelta;
        private final double[] bfDelta;
        private final int[] votes;

        private Entry() {
            this.votes = null;
            this.ssDelta = null;
            this.bfDelta = null;
        }

        private Entry(int n) {
            this.votes = new int[n];
            this.ssDelta = new double[n];
            this.bfDelta = new double[n];
        }
    }

    public final class EntryChecker {
        private final int[] table;
        private int[] votes;
        private int value;
        private int hashCode;

        public final double[] found;

        private EntryChecker(int v) {
            this.table = new int[v];
            this.found = new double[2];
        }

        public final void setVotesAndValue(int[] array, int value) {
            this.votes = array;
            this.value = value;

            computeHash(this);
        }

        private final boolean checkEqualAndFind(Entry e) {
            int[] A = this.votes;
            int[] B = e.votes;
            int val = this.value;
            int idx = -1;

            // Set up table
            for (int i = 0; i < n; ++i)
                table[A[i] - 1]++;

            // Check the votes in entry for equality
            // Record the index for the vote value
            // that we are interested in
            for (int i = 0; i < n; ++i) {
                if (table[B[i] - 1] <= 0) {
                    idx = -1;
                    break;
                }

                if (B[i] == val)
                    idx = i;

                table[B[i] - 1]--;
            }

            // Reset table
            for (int i = 0; i < n; ++i)
                table[A[i] - 1] = 0;

            if (idx >= 0) {
                // Found equal
                this.found[0] = e.ssDelta[idx];
                this.found[1] = e.bfDelta[idx];
                return true;
            }

            return false;
        }
    }
}
