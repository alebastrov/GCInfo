package com.nikondsl.gcinfo.monitoring.gc.types;

//-XX:-UseShenandoahGC
public class Shenandoah implements GarbageCollectors {
    @Override
    public boolean isConcurrentPhase(String cause, String name) {
        return "No GC".equals(cause) || "Shenandoah Cycles".equals(name);
    }
}
