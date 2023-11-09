package com.nikondsl.gcinfo.monitoring.gc.types;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

// -XX:+UseConcMarkSweepGC
// <= java 10, dropped in java 11+
public class CMS implements GarbageCollector {

    public static final String GPGC = "GPGC";
    public static final String PAUSES = "Pauses";

    public Set<String> getDetectionNames() {
        return new HashSet<>( Arrays.asList( new String[] { } ) ); //?
    }

    @Override
    public boolean isConcurrentPhase( String cause, String name ) {
        return NO_GC.equals( cause )
            || name.startsWith( GPGC ) && !name.endsWith( PAUSES );
    }
}
