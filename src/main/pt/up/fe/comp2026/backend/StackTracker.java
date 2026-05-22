package pt.up.fe.comp2026.backend;

/** Tracks operand stack depth for a single JVM method's .limit stack calculation. */
class StackTracker {
    private int current;
    private int max;

    void reset() {
        current = 0;
        max = 0;
    }

    void update(int delta) {
        current += delta;
        if (current > max) max = current;
    }

    int getMax() {
        return max;
    }
}