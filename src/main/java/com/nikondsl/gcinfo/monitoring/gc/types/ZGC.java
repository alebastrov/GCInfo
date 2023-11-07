package com.nikondsl.gcinfo.monitoring.gc.types;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularDataSupport;

//-XX:+UseZGC
public class ZGC implements GarbageCollectors {
    @Override
    public boolean isConcurrentPhase( String cause, String name ) {
        return "No GC".equals( cause ) || "ZGC Cycles".equals( name );
    }

    public String[] getNames() {
        return new String[] { "ZHeap" };
    }

    public TabularDataSupport getUsageAfterGc( CompositeData cdata ) {
        if ( cdata.containsKey( "memoryUsageAfterGc" ) ) {
            return ( TabularDataSupport ) cdata.get( "memoryUsageAfterGc" );
        }
        return null;
    }
}
