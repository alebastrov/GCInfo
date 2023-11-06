package com.nikondsl.gcinfo.monitoring.gc.types;

import javax.management.openmbean.CompositeData;
import java.util.Objects;

public class GcDetector {
    public static GarbageCollectors get( CompositeData compositeData ) {
        Objects.requireNonNull( compositeData );
        String name = ( String ) compositeData.get( "gcName" );
        if ( name.startsWith( "Shenandoah" ) ) return new Shenandoah();
        if ( name.startsWith( "G1" ) ) return new G1();
        if ( name.startsWith( "ZGC " ) ) return new ZGC();
        if ( "PS Scavenge".equals( name ) || "PS MarkSweep".equals( name ) ) {
            return new ParallelGc();
        }
        return new CMS();
    }
}
