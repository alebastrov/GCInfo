package com.nikondsl.utils.monitoring.gc;

import java.lang.management.MemoryUsage;

/**
 * Created by IntelliJ IDEA.
 * User: Igor
 * Date: 04.03.2006
 * Time: 13:22:57
 * To change this template use File | Settings | File Templates.
 */
public class GCInfoBlock {
  public int getCompacted() {
    return (int)(2.0+2.0*Math.log(compacted));
  }

  public void setCompacted(int compacted) {
    this.compacted = compacted;
  }

  public enum Payloads {
    OK(85), MEDIUM_LOAD(75), HIGHLOAD(45), SLOWDOWN(0);

    private int userLoadInPercents;

    Payloads(int userLoadInPercents) {
      this.userLoadInPercents = userLoadInPercents;
    }

    static Payloads getByLoad(int currentUsagePercent) {
      if (currentUsagePercent <= 0) return SLOWDOWN;
      if (currentUsagePercent >= 100) return OK;
      for (Payloads payload : values()) {
        if (payload.userLoadInPercents > currentUsagePercent) continue;
        return payload;
      }
      return OK;
    }
  }
  private static long maxDuration = 200L;

  private String gcName = "";
  private long time;
  private MemoryUsage memoryUsage;
  private long callNumber;
  private long duration;
  private Payloads gcState = Payloads.OK;
  private int compacted=0;

  public GCInfoBlock() {

  }

  public void setGCName(String GCName) {
    this.gcName = GCName;
  }

  public void setTime(long time) {
    this.time = time;
  }

  public void setMemoryUsage(MemoryUsage memoryUsage) {
    this.memoryUsage = memoryUsage;
  }

  public String getGCName() {
    return gcName;
  }

  public long getTime() {
    return time;
  }

  public long getUsedMemory() {
    return memoryUsage.getUsed();
  }

  public long getFreeMemory() {
    return memoryUsage.getCommitted() - memoryUsage.getUsed();
  }

  public MemoryUsage getMemoryUsage() {
    return memoryUsage;
  }

  public String getTenuredGenString() {
    return memoryUsage.toString();
  }

  public long getMaxMemory() {
    return memoryUsage.getMax();
  }

  public void setCallNumber(long callNumber) {
    this.callNumber = callNumber;
  }

  public long getCallNumber() {
    return callNumber;
  }

  public void setDuration(long duration) {
    this.duration = duration;
  }

  public long getDuration() {
    return duration;
  }

  public static long getMaxDuration() {
    return maxDuration;
  }

  public Payloads getGcState() {
    return gcState;
  }

  public void setGcState(Payloads gcState) {
    this.gcState = gcState;
  }
}
