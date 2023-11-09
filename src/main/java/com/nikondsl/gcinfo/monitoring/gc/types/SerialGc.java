package com.nikondsl.gcinfo.monitoring.gc.types;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

//-XX:+UseSerialGC - does not work?!
public class SerialGc implements GarbageCollector {
    @Override
    public boolean isConcurrentPhase( String cause, String name ) {
        return false;
    }

    public Set<String> getDetectionNames() {
        return new HashSet<>( Arrays.asList( new String[] { } ) ); //?
    }
}
