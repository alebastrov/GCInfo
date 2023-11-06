package com.nikondsl.gcinfo.monitoring.gc.types;
// -XX:+UseConcMarkSweepGC
// < java 14, dropped in java 14+
public class CMS implements GarbageCollectors {
    @Override
    public boolean isConcurrentPhase(String cause, String name) {
        return "No GC".equals( cause ) || name.startsWith( "GPGC" ) && !name.endsWith( "Pauses" );
    }
}
