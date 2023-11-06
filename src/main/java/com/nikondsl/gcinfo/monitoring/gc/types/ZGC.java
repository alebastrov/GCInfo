package com.nikondsl.gcinfo.monitoring.gc.types;

//-XX:-UseZGC
public class ZGC implements GarbageCollectors {
    @Override
    public boolean isConcurrentPhase( String cause, String name ) {
        return "No GC".equals( cause ) || "ZGC Cycles".equals( name );
    }

    public String[] getNames() {
        return new String[] { "ZHeap" };
    }
}
