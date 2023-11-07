package com.nikondsl.gcinfo.monitoring.gc.types;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularDataSupport;

//-XX:+UseG1GC
public class G1 implements GarbageCollectors {
    @Override
    public boolean isConcurrentPhase( String cause, String name ) {
        return "No GC".equals( cause )
            || name.endsWith( "Cycles" )
            || name.startsWith( "GPGC" ) && !name.endsWith( "Pauses" );
    }

    public String[] getNames() {
        return new String[] { "G1 Old Generation", "G1 Old Gen", "G1 Survivor Space", "G1 Eden Space" };
    }

    @Override
    public Long getDuration( CompositeData cdata ) {
        if ( cdata.containsKey( "startTime" ) &&  cdata.containsKey( "endTime" ) ) {
            return   ( Long ) cdata.get( "endTime" ) - ( Long ) cdata.get( "startTime" );
        }
        return null;
    }

    @Override
    public TabularDataSupport getUsageAfterGc( CompositeData cdata ) {
        if ( cdata.containsKey( "memoryUsageAfterGc" ) ) {
            return ( TabularDataSupport ) cdata.get( "memoryUsageAfterGc" );
        }
        return null;
    }
}
