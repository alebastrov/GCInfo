package com.nikondsl.gcinfo.monitoring.gc;

import com.nikondsl.gcinfo.thread.BackgroundThread;
import com.sun.management.GarbageCollectionNotificationInfo;
import com.sun.management.GcInfo;

import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.sun.management.GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION;

public final class GCInfoCollector {
  private static final MemoryUsage empty = new MemoryUsage(1, 1, 1, 1);
  private static GCInfoCollector instance;
  private static Map<String,Long> collCount = new HashMap<>();
  private static Map<String,Long> collDuration = new HashMap<>();

  private int maxEventsCount = 300;
  private ArrayDeque<GCInfoBlock> storage = null;
  private volatile GCInfoBlock.Payloads lastGcState = GCInfoBlock.Payloads.OK;
  private BackgroundThread thread = null;

  private GarbageCollectionNotificationInfo gcNotificationInfo;

  public static synchronized GCInfoCollector getGCInfoCollector(long millis) {
    if (instance == null) {
      instance = new GCInfoCollector(millis);
    }
    return instance;
  }

  public void attachListenerToGarbageCollector(List<GarbageCollectorMXBean> mbeans) {
    // Attach a listener to the GarbageCollectorMXBeans
    NotificationListener gcEventListener = new NotificationListener() {
      @Override
      public void handleNotification(Notification notification, Object handback) {
        String notificationType = notification.getType();
        if (!notificationType.equals(GARBAGE_COLLECTION_NOTIFICATION)) {
          System.err.println(notificationType + "received ...");
          return;
        }
        CompositeData compositeData = (CompositeData) notification.getUserData();
        GarbageCollectionNotificationInfo gcNotificationInfo = GarbageCollectionNotificationInfo.from(compositeData);
        GCInfoCollector.this.gcNotificationInfo = gcNotificationInfo;
        if ("end of minor GC".equals(gcNotificationInfo.getGcAction())) {
          addEmpty(gcNotificationInfo.getGcInfo().getDuration());
        } else if ("end of major GC".equals(gcNotificationInfo.getGcAction())) {
          GCInfoBlock infoBlock = createInfoBlock2(gcNotificationInfo);
          if (infoBlock == null) addEmpty(gcNotificationInfo.getGcInfo().getDuration());
          else addGc(infoBlock);
        }
      }

      private GCInfoBlock createInfoBlock2(GarbageCollectionNotificationInfo gcNotificationInfo) {
        final long currentCollectionCount = gcNotificationInfo.getGcInfo().getId();
        if (currentCollectionCount < 0L) {
          return null;
        }

        final long currentCollectionTime = gcNotificationInfo.getGcInfo().getDuration();
        final long curTime = System.currentTimeMillis();

        final String mbeanName = gcNotificationInfo.getGcName();

        final Long storedCollCount = collCount.get(mbeanName);
        final Long storedCollDuration = collDuration.get(mbeanName);
        if (storedCollCount != null && currentCollectionCount < storedCollCount) {
          return null;
        }
        collCount.put(mbeanName, currentCollectionCount);
        collDuration.put(mbeanName, currentCollectionTime);
        final GCInfoBlock infoBlock = new GCInfoBlock();
        infoBlock.setGCName(mbeanName);
        infoBlock.setTime(curTime);
        infoBlock.setCallNumber(currentCollectionCount - (storedCollCount == null ? 0L : storedCollCount));
        infoBlock.setDuration(currentCollectionTime - (storedCollDuration == null ? 0L : storedCollDuration));

        if (infoBlock.getDuration() <= 0L) {
          return null;
        }
        return infoBlock;
      }
    };

    for (final GarbageCollectorMXBean garbageCollectorMXBean : mbeans) {
      if (NotificationEmitter.class.isInstance(garbageCollectorMXBean)) {
        NotificationEmitter emitter = NotificationEmitter.class.cast(garbageCollectorMXBean);
        emitter.addNotificationListener(gcEventListener, null, null);
      }
    }
  }

  private GCInfoCollector(long millis) {
    setMaxEventsCount(maxEventsCount);
    List<GarbageCollectorMXBean> mbeans = ManagementFactory.getGarbageCollectorMXBeans();
    if (true && mbeans != null && !mbeans.isEmpty()) {
      attachListenerToGarbageCollector(mbeans);
      return;
    }

    this.thread = new BackgroundThread (
            "GC Information Collector thread",
            millis,
            () -> {
        attachListenerToGarbageCollector(mbeans);

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
  }

  private void addGc(GCInfoBlock resInfoBlock) {
    Runtime runtime = Runtime.getRuntime();
    if (resInfoBlock == null) {
      return;
    }
    resInfoBlock.setMemoryUsage(new MemoryUsage(-1, (runtime.totalMemory()-runtime.freeMemory()), runtime.totalMemory(),  runtime.maxMemory()));
    if (!storage.isEmpty()) {
      GCInfoBlock last = storage.getLast();
      long workTime = resInfoBlock.getTime() - last.getTime();
      long duration = resInfoBlock.getDuration();
      int workPercent = workTime == 0 ? 0 : (int) ((workTime - duration) * 100L / workTime);
      resInfoBlock.setGcState(GCInfoBlock.Payloads.getByLoad(workPercent));
      lastGcState = resInfoBlock.getGcState();
    } else {
      lastGcState = GCInfoBlock.Payloads.OK;
    }
    System.err.println(resInfoBlock);
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
    GCInfoBlock last = !storage.isEmpty() ? storage.getLast() : null;
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

  public synchronized int getCurentSize() {
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
  public static Map<String, Object> toMap(GarbageCollectorMXBean o) {
    LinkedHashMap<String, Object> map = new LinkedHashMap<>();
    BeanInfo beanInfo = null;
    try {
      beanInfo = Introspector.getBeanInfo(o.getClass());
    } catch (IntrospectionException e) {
      throw new RuntimeException(e);
    }
    for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
      if (propertyDescriptor.getName().equals("class")) continue;
      Object value = null;
      try {
        value = propertyDescriptor.getReadMethod().invoke(o);
      } catch (Exception e) {
        value = e.toString();
      }
      map.put(propertyDescriptor.getName(), value);
    }
    return map;
  }
}
