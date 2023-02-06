package com.nikondsl.gcinfo.monitoring.gc.types;

import com.sun.management.GarbageCollectionNotificationInfo;

import java.util.Objects;

public class GcDetector {
    public static GarbageCollectors get(GarbageCollectionNotificationInfo gcni) {
        Objects.requireNonNull(gcni);
        if (gcni.getGcCause().contains("Shenandoah")) return new Shenandoah();
        if (gcni.getGcCause().contains("G1")) return new G1();
        if (gcni.getGcName().startsWith("ZGC ")) return new ZGC();
        if (gcni.getGcName().equals("PS Scavenge") ||
            gcni.getGcName().equals("PS MarkSweep")) {
            return new ParallelGc();
        }
        return new CMS();
    }
}
