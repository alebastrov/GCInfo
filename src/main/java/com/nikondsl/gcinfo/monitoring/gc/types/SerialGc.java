package com.nikondsl.gcinfo.monitoring.gc.types;

//-XX:+UseSerialGC - does not work?!
public class SerialGc implements GarbageCollectors {
    @Override
    public boolean isConcurrentPhase(String cause, String name) {
        return false;
    }
}
