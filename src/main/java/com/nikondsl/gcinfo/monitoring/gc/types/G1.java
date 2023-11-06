package com.nikondsl.gcinfo.monitoring.gc.types;

//-XX:-UseG1GC
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
}
