package com.nikondsl.gcinfo.monitoring.gc.types;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

//-XX:+UseParallelGC
public class ParallelGc implements GarbageCollector {

    public Set<String> getDetectionNames() {
        return new HashSet<>( Arrays.asList( new String[] { "PS " } ) );
    }

    @Override
    public boolean isConcurrentPhase( String cause, String name ) {
        return false;
    }
}
