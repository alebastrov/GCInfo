package com.nikondsl.gcinfo.monitoring.gc.types;

public interface GarbageCollectors {

    boolean isConcurrentPhase(String cause, String name);
}
