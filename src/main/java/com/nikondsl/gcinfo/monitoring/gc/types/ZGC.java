package com.nikondsl.gcinfo.monitoring.gc.types;

public class ZGC implements GarbageCollectors {
    @Override
    public boolean isConcurrentPhase(String cause, String name) {
        return "No GC".equals(cause) ||
                "ZGC Cycles".equals(name) ||
                (name.startsWith("GPGC") && !name.endsWith("Pauses"));
    }
}
