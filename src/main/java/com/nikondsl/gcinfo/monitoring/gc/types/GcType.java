package com.nikondsl.gcinfo.monitoring.gc.types;

import javax.management.openmbean.CompositeData;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public enum GcType implements Supplier<GarbageCollector> {
    SerialGc( new SerialGc() ),
    ParallelGc( new ParallelGc() ),
    CMS( new CMS() ),
    G1( new G1() ),
    ShennandoahGC( new Shenandoah()  ),
    ZGC( new ZGC() );

    private GarbageCollector delegate;

    GcType( GarbageCollector collector ) {
        this.delegate = collector;
    }

    @Override
    public GarbageCollector get() {
        return delegate;
    }

    public static GcType get( CompositeData cdata ) {
        Objects.requireNonNull( cdata );
        String name = ( String ) cdata.get( GarbageCollector.GC_NAME );
        for ( GcType type : GcType.values() ) {
            Optional<String> possibleType = type.get().getDetectionNames()
                .stream()
                .filter( n -> name.startsWith( n ) )
                .findFirst();
            if( possibleType.isPresent() ) return type;
        }
        return GcType.CMS;
    }
}
