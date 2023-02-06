package com.nikondsl.gcinfo.monitoring.gc.types;

//-XX:+UseParallelGC
public class ParallelGc implements GarbageCollectors {
    @Override
    public boolean isConcurrentPhase(String cause, String name) {
        return false;
    }
}
