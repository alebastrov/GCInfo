package com.nikondsl.gcinfo.monitoring.gc.types;


import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularDataSupport;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

// -XX:+UseConcMarkSweepGC
// -XX:CMSInitiatingOccupancyFraction=75
// -XX:+UseCMSInitiatingOccupancyOnly
// <= java 10, dropped in java 11+
public class CMS implements GarbageCollector {

    public static final String GPGC = "GPGC";
    public static final String PAUSES = "Pauses";

    public Set<String> getDetectionNames() {
        return new HashSet<>( Arrays.asList( new String[] { "ConcurrentMarkSweep" } ) );
    }

    @Override
    public boolean isConcurrentPhase( String cause, String name ) {
        return NO_GC.equals( cause )
            || name.startsWith( GPGC ) && !name.endsWith( PAUSES );
    }

    public TabularDataSupport getUsageAfterGc( CompositeData cdata ) {
        if ( cdata.containsKey( MEMORY_USAGE_AFTER_GC ) ) {
            return ( TabularDataSupport ) cdata.get( MEMORY_USAGE_AFTER_GC );
        }
        return null;
    }
}
