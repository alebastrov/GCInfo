package com.nikondsl.gcinfo.monitoring.gc.types;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularDataSupport;

public interface GarbageCollectors {

    boolean isConcurrentPhase( String cause, String name );

    default Long getDuration( CompositeData cdata ) {
        if ( cdata.containsKey( "duration" ) ) {
            return ( Long ) cdata.get( "duration" );
        }
        return null;
    }

    default Long getId( CompositeData cdata ) {
        if ( cdata.containsKey( "id" ) ) {
            return ( Long ) cdata.get( "id" );
        }
        return null;
    }

    default TabularDataSupport getUsageAfterGc( CompositeData cdata ) {
        if ( cdata.containsKey( "usageAfterGc" ) ) {
            return ( TabularDataSupport ) cdata.get( "usageAfterGc" );
        }
        return null;
    }

    default String getName( CompositeData cdata ) {
        return ( String ) cdata.get( "gcName" );
    }
}
