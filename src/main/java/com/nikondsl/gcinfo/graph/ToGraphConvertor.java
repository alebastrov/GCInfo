package com.nikondsl.gcinfo.graph;

import com.nikondsl.gcinfo.monitoring.gc.ColoredUsagePercentage;
import com.nikondsl.gcinfo.monitoring.gc.GCInfoBlock;

import static com.nikondsl.gcinfo.monitoring.gc.ColoredUsagePercentage.RED;

public class ToGraphConvertor {
    private static final int TABLE_HEIGHT = 400;
    private static final int MAX_WIDTH = 20;

    public BoxItem toGraph(GCInfoBlock info ) {

        BoxItem result = new BoxItem();
        result.setWidth(getBlockWidth(info.getDuration()));
        ColoredUsagePercentage type = RED;
        result.setHeight(getBlockPixelsCount(type, info.getMaxMemory(), info.getUsedMemory()));
        return result;
    }
    int getBlockWidth(long duration) {
        return (int) Math.min(MAX_WIDTH, Math.max(2L, (duration * MAX_WIDTH / GCInfoBlock.getMaxDuration())));
    }
    int getBlockPixelsCount(ColoredUsagePercentage type, double max, double value) {
        double percents = 100.0 * value / max;
        double realPixels = TABLE_HEIGHT * (percents - type.getEliminator()) / 100.0;
        return (int) Math.min(TABLE_HEIGHT * type.getPercents() / 100.0, realPixels);
    }
}
