package com.nikondsl.utils.monitoring.gc;


import com.nikondsl.utils.convertions.ComputerBytesConverter;
import com.nikondsl.utils.convertions.ConvertionUtils;
import com.nikondsl.utils.date.DateUtils;
import com.nikondsl.utils.date.SynchronizedDateFormat;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by IntelliJ IDEA.
 * User: Igor
 * Date: 04.03.2006
 * Time: 16:20:15
 * To change this template use File | Settings | File Templates.
 */
public class GCInfoReflector {
  private static final int TABLE_HEIGHT = 400;
  private static final SynchronizedDateFormat df = createDateFormat();
  public final String TRANSPARENT_1x1_IMAGE_SOURCE = "data:image/gif;base64,R0lGODlhAQABAIAAAP///wAAACH5BAEAAAAALAAAAAABAAEAAAICRAEAOw==";

  private static SynchronizedDateFormat createDateFormat() {
    final SynchronizedDateFormat format = new SynchronizedDateFormat("yyyy.MM.dd HH:mm:ss z");
    format.setTimeZone(TimeZone.getTimeZone("GMT"));
    return format;
  }

  private static final long MAX_WIDTH = 20L;

  public String getImage(GCInfoBlock gcInfoBlock, long amount, Percentage percentage, int attempt) {
    if (amount <= 0L) {
      return "";
    }
    if (gcInfoBlock.getDuration() > 0L) {
      final String color;
      if (gcInfoBlock.getGcState() == GCInfoBlock.Payloads.OK) {
        color = percentage.getColor();
      }
      else if (gcInfoBlock.getGcState() == GCInfoBlock.Payloads.HIGHLOAD) {
        color = "grey";
      }
      else {
        color = "black";
      }
      return "<img src='"+TRANSPARENT_1x1_IMAGE_SOURCE+"' width='" + getBlockWidth(gcInfoBlock.getDuration(), GCInfoBlock.getMaxDuration())
             + "' height='" + getHeight(amount) +"' style='background-color:" + color + "'" +
             " alt='"  + getTime(gcInfoBlock.getTime()) + "'" +
             " title='" + getAltText(gcInfoBlock) + getTime(gcInfoBlock.getTime()) + "' />";
    }
    if (attempt==1) {
      String ticks = "\nTicks: " + gcInfoBlock.getCompacted() + " (" +
                     DateUtils.getRemainingTime(Locale.ENGLISH, 0L, gcInfoBlock.getCompacted() * 1000L, 2);
      return "<img src='"+TRANSPARENT_1x1_IMAGE_SOURCE+"' width='" + (2 + 2 * gcInfoBlock.getCompacted()) +
             "' height='2' style='background-color:blue' " +
             "alt='" + getTime(gcInfoBlock.getTime()) + ticks + ")' " +
             "title='" + getTime(gcInfoBlock.getTime()) + ticks + ")' />";
    }
    return "";
  }

  long getBlockWidth(long duration, long maxDuration) {
    return Math.min(MAX_WIDTH, Math.max(2L, (duration * MAX_WIDTH / maxDuration)));
  }

  private String getHeight(long amount) {
    return String.valueOf(amount);
  }

  public int getBlockPixelsCount(Percentage type, double max, double value) {
    double percents = 100.0 * value / max;
    double realPixels = TABLE_HEIGHT * (percents - type.getEliminator()) / 100.0;
    return (int) Math.min(TABLE_HEIGHT * type.getPercents() / 100.0, realPixels);
  }

  private String getAltText(GCInfoBlock gcInfoBlock) {

    StringBuilder res = new StringBuilder(100);
    res.append(gcInfoBlock.getGCName()).append("\n");
    res.append("Memory freed: ")
        .append(formatMemoryNumber(gcInfoBlock.getMaxMemory() - gcInfoBlock.getUsedMemory()))
        .append(" (of ")
        .append(formatMemoryNumber(gcInfoBlock.getMaxMemory()))
        .append(")\n");

    res.append(" Count: ")
        .append(gcInfoBlock.getCallNumber())
        .append("\n");
    res.append(" Time: ")
        .append(DateUtils.getRemainingTime(Locale.ENGLISH, 0L, gcInfoBlock.getDuration(), 3))
        .append("\n");

    res.append(" ")
        .append(gcInfoBlock.getTenuredGenString())
        .append("\n");

    return res.toString();
  }

  private String getTime(long currentTime) {
    StringBuilder res = new StringBuilder();
    res.append(" Time: ");
    res.append(df.format(new Date(currentTime)));
    res.append("\n");
    return res.toString();
  }

  String formatMemoryNumber(long value) {
    return ConvertionUtils.convertToString(ComputerBytesConverter.createConverter(), (double)value, 2);
  }
}
