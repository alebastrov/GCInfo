package com.nikondsl.gcinfo.monitoring.gc.types;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularDataSupport;
import java.util.Set;

public interface GarbageCollector {
    String NO_GC = "No GC";
    String MEMORY_USAGE_AFTER_GC = "memoryUsageAfterGc";
    String START_TIME = "startTime";
    String END_TIME = "endTime";
    String DURATION = "duration";
    String ID = "id";
    String USAGE_AFTER_GC = "usageAfterGc";
    String GC_NAME = "gcName";

    boolean isConcurrentPhase( String cause, String name );

    default Long getDuration( CompositeData cdata ) {
        if ( cdata.containsKey( DURATION ) ) {
            return ( Long ) cdata.get( DURATION );
        }
        return null;
    }

    default Long getId( CompositeData cdata ) {
        if ( cdata.containsKey( ID ) ) {
            return ( Long ) cdata.get( ID );
        }
        return null;
    }

    default TabularDataSupport getUsageAfterGc( CompositeData cdata ) {
        if ( cdata.containsKey( USAGE_AFTER_GC ) ) {
            return ( TabularDataSupport ) cdata.get( USAGE_AFTER_GC );
        }
        return null;
    }

    public static String getName( CompositeData cdata ) {
        return ( String ) cdata.get( GC_NAME );
    }

    Set<String> getDetectionNames();
}
