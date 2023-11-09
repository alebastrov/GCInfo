package com.nikondsl.gcinfo.monitoring.gc.types;


import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularDataSupport;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

//-XX:+UseShenandoahGC
public class Shenandoah implements GarbageCollector {

    public static final String SHENANDOAH_CYCLES = "Shenandoah Cycles";

    public Set<String> getDetectionNames() {
        return new HashSet<>( Arrays.asList( new String[] { "Shenandoah" } ) );
    }

    @Override
    public boolean isConcurrentPhase( String cause, String name ) {
        return NO_GC.equals( cause )
            || SHENANDOAH_CYCLES.equals( name );
    }

    public TabularDataSupport getUsageAfterGc( CompositeData cdata ) {
        if ( cdata.containsKey( MEMORY_USAGE_AFTER_GC ) ) {
            return ( TabularDataSupport ) cdata.get( MEMORY_USAGE_AFTER_GC );
        }
        return null;
    }
}
