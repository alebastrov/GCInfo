package com.nikondsl.gcinfo.monitoring.gc.types;

public class G1 implements GarbageCollectors {
    @Override
    public boolean isConcurrentPhase(String cause, String name) {
        return "No GC".equals(cause) ||
                (name.startsWith("GPGC") && !name.endsWith("Pauses"));
    }
}
