package com.nikondsl.gcinfo.monitoring.gc.types;


import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularDataSupport;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

//-XX:+UseZGC
public class ZGC implements GarbageCollector {

    public static final String ZGC_CYCLES = "ZGC Cycles";

    @Override
    public boolean isConcurrentPhase( String cause, String name ) {
        return NO_GC.equals( cause ) || ZGC_CYCLES.equals( name );
    }

    public Set<String> getDetectionNames() {
        return new HashSet<>( Arrays.asList( new String[] { "ZGC " } ) );
    }

    public TabularDataSupport getUsageAfterGc( CompositeData cdata ) {
        if ( cdata.containsKey( MEMORY_USAGE_AFTER_GC ) ) {
            return ( TabularDataSupport ) cdata.get( MEMORY_USAGE_AFTER_GC );
        }
        return null;
    }
}
