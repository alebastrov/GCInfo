package com.nikondsl.gcinfo.monitoring.gc.types;

public enum HeapGeneration {
    YOUNG, OLD;

    public static HeapGeneration detect( String mbeanName ) {
        return mbeanName.toLowerCase().contains( "young" ) ? HeapGeneration.YOUNG : HeapGeneration.OLD;
    }
}
