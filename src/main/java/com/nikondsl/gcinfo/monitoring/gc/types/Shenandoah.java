package com.nikondsl.gcinfo.monitoring.gc.types;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularDataSupport;

//-XX:+UseShenandoahGC
public class Shenandoah implements GarbageCollectors {
    @Override
    public boolean isConcurrentPhase( String cause, String name ) {
        return "No GC".equals( cause )
            || "Shenandoah Cycles".equals( name );
    }

    public String[] getNames() {
        return new String[] { "Shenandoah" };
    }

    public TabularDataSupport getUsageAfterGc( CompositeData cdata ) {
        if ( cdata.containsKey( "memoryUsageAfterGc" ) ) {
            return ( TabularDataSupport ) cdata.get( "memoryUsageAfterGc" );
        }
        return null;
    }
}
