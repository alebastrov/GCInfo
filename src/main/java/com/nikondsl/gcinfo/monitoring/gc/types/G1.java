package com.nikondsl.gcinfo.monitoring.gc.types;


import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularDataSupport;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

//-XX:+UseG1GC
public class G1 implements GarbageCollector {

    public static final String CYCLES = "Cycles";
    public static final String GPGC = "GPGC";
    public static final String PAUSES = "Pauses";

    public Set<String> getDetectionNames() {
        return new HashSet<>( Arrays.asList( new String[] { "G1 Young Generation", "G1 Old Generation" } ) );
    }

    @Override
    public boolean isConcurrentPhase( String cause, String name ) {
        return NO_GC.equals( cause )
            || name.endsWith( CYCLES )
            || name.startsWith( GPGC ) && !name.endsWith( PAUSES );
    }

    @Override
    public Long getDuration( CompositeData cdata ) {
        if ( cdata.containsKey( START_TIME ) &&  cdata.containsKey( END_TIME ) ) {
            return   ( Long ) cdata.get( END_TIME ) - ( Long ) cdata.get( START_TIME );
        }
        return null;
    }

    @Override
    public TabularDataSupport getUsageAfterGc( CompositeData cdata ) {
        if ( cdata.containsKey( MEMORY_USAGE_AFTER_GC ) ) {
            return ( TabularDataSupport ) cdata.get( MEMORY_USAGE_AFTER_GC );
        }
        return null;
    }
}
