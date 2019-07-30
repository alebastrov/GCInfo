package com.nikondsl.utils.monitoring.gc;

import com.nikondsl.utils.thread.BackgroundThread;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public final class GCInfoCollector {
  private static final MemoryUsage empty = new MemoryUsage(1, 1, 1, 1);
  private static GCInfoCollector instance;
  private static Map<String,Long> collCount = new HashMap<>();
  private static Map<String,Long> collDuration = new HashMap<>();

  private int maxEventsCount = 300;
  private ArrayDeque<GCInfoBlock> storage = null;
  private volatile GCInfoBlock.Payloads lastGcState;
  private BackgroundThread thread = null;

  public static synchronized GCInfoCollector getGCInfoCollector(long millis) {
    if (instance == null) {
      instance = new GCInfoCollector(millis);
    }
    return instance;
  }

  private GCInfoCollector(long millis) {
    this.thread = new BackgroundThread (
            "GC Information Collector",
            millis,
            (Callable) () -> {
        List<GarbageCollectorMXBean> mbeans = ManagementFactory.getGarbageCollectorMXBeans();
        if (mbeans == null || mbeans.isEmpty()) return null;
        GCInfoBlock resInfoBlock = null;
        final long curTime = System.currentTimeMillis();
        for (GarbageCollectorMXBean mbean : mbeans) {
          final GCInfoBlock infoBlock = createInfoBlock(mbean);
          if (infoBlock == null) continue;
          if (resInfoBlock == null) {
            resInfoBlock = infoBlock;
          } else {
            resInfoBlock.setGCName(resInfoBlock.getGCName() + " & " +infoBlock.getGCName());
            resInfoBlock.setCallNumber(resInfoBlock.getCallNumber() + infoBlock.getCallNumber());
            resInfoBlock.setDuration(resInfoBlock.getDuration() | infoBlock.getDuration());
          }
        }
        if (resInfoBlock == null) {
          addEmpty(curTime);
        } else {
          addGc(resInfoBlock);
        }
        return null;
    });
    setMaxEventsCount(maxEventsCount);
  }

  private void addGc(GCInfoBlock resInfoBlock) {
    Runtime runtime = Runtime.getRuntime();
    resInfoBlock.setMemoryUsage(new MemoryUsage(-1, (runtime.totalMemory()-runtime.freeMemory()), runtime.totalMemory(),  runtime.maxMemory()));
    if (!storage.isEmpty()) {
      GCInfoBlock last = storage.getLast();
      long workTime = resInfoBlock.getTime() - last.getTime();
      long duration = resInfoBlock.getDuration();
      int workPercent = (int) ((workTime - duration) * 100L / workTime);
      resInfoBlock.setGcState(GCInfoBlock.Payloads.getByLoad(workPercent));
      lastGcState = resInfoBlock.getGcState();
    } else {
      lastGcState = GCInfoBlock.Payloads.OK;
    }
    addToStorage(resInfoBlock);
  }

  private void addToStorage(GCInfoBlock resInfoBlock) {
    storage.addLast(resInfoBlock);
    if (storage.size() > maxEventsCount) storage.removeFirst();
  }

  private GCInfoBlock createInfoBlock(GarbageCollectorMXBean mbean) {
    final long currentCollectionCount = mbean.getCollectionCount();
    if (currentCollectionCount < 0L) return null;
    final long currentCollectionTime = mbean.getCollectionTime();
    final long curTime = System.currentTimeMillis();

    final String mbeanName = mbean.getName();

    final Long storedCollCount = collCount.get(mbeanName);
    final Long storedCollDuration = collDuration.get(mbeanName);
    if (storedCollCount != null && currentCollectionCount < storedCollCount) return null;
    collCount.put(mbeanName, currentCollectionCount);
    collDuration.put(mbeanName, currentCollectionTime);
    final GCInfoBlock infoBlock = new GCInfoBlock();
    infoBlock.setGCName(mbeanName);
    infoBlock.setTime(curTime);
    infoBlock.setCallNumber(currentCollectionCount - (storedCollCount == null ? 0L : storedCollCount));
    infoBlock.setDuration(currentCollectionTime - (storedCollDuration == null ? 0L : storedCollDuration));

    if (infoBlock.getDuration() <= 0L) return null;
    return infoBlock;
  }

  private void addEmpty(long curTime) {
    GCInfoBlock last = !storage.isEmpty()?storage.getLast():null;
    if (last!=null && last.getCallNumber()==0 && last.getDuration()==0){
      last.setCompacted(last.getCompacted()+1);
      return;
    }
    final GCInfoBlock infoBlock = new GCInfoBlock();
    infoBlock.setGCName("--");
    infoBlock.setTime(curTime);
    infoBlock.setCallNumber(0);
    infoBlock.setDuration(0);
    infoBlock.setMemoryUsage(empty);
    addToStorage(infoBlock);
  }

  public GCInfoBlock.Payloads getLastGcState() {
    return lastGcState;
  }

  public synchronized GCInfoBlock getLastBlock() {
    return storage.getLast();
  }

  public synchronized int getCount() {
    return storage.size();
  }

  public synchronized List<GCInfoBlock> getAll() {
    return new ArrayList<>(storage);
  }

  public synchronized void setMaxEventsCount(int maxEventsCount) {
    if (maxEventsCount<10) throw new IllegalArgumentException("Too small ("+maxEventsCount+") value for history (less than 10)");
    this.maxEventsCount = maxEventsCount;
    ArrayDeque<GCInfoBlock> newStorage = new ArrayDeque<>(maxEventsCount);
    if (storage != null && !storage.isEmpty()) {
      newStorage.addAll(storage);
      while (newStorage.size() > maxEventsCount) newStorage.removeFirst();
    }
    storage = newStorage;
  }
}
