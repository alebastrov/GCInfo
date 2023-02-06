package com.nikondsl.gcinfo.monitoring.gc;

import com.nikondsl.gcinfo.GCInfo2HtmlPrinter;
import com.nikondsl.gcinfo.monitoring.gc.types.GarbageCollectors;
import com.nikondsl.gcinfo.monitoring.gc.types.GcDetector;
import com.sun.management.GarbageCollectionNotificationInfo;

import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static com.sun.management.GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION;
import static com.sun.management.GarbageCollectionNotificationInfo.from;

public final class GCInfoCollector {
  private static final MemoryUsage empty = new MemoryUsage(1, 1, 1, 1);
  private static GCInfoCollector instance;

  private int maxEventsCount = 300;
  private ArrayDeque<GCInfoBlock> storage = null;
  private volatile GCInfoBlock.Payloads lastGcState = GCInfoBlock.Payloads.OK;
  private GcNotificationListener gcEventListener = new GcNotificationListener();

  private GarbageCollectionNotificationInfo gcNotificationInfo;

  public static synchronized GCInfoCollector getGCInfoCollector(long millis) {
    if (instance == null) {
      instance = new GCInfoCollector();
    }
    return instance;
  }

  class GcNotificationListener implements NotificationListener {
    @Override
    public void handleNotification(Notification notification, Object handback) {
      String notificationType = notification.getType();
      if (!notificationType.equals(GARBAGE_COLLECTION_NOTIFICATION)) {
        System.err.println(notificationType + " received ...");
        return;
      }
      CompositeData compositeData = (CompositeData) notification.getUserData();
      GarbageCollectionNotificationInfo gcni = from(compositeData);
      GCInfoCollector.this.gcNotificationInfo = gcni;
//      System.out.printf("%d ms,\tname:%s\tcause:%s%n",
//              gcni.getGcInfo().getDuration(),
//              gcni.getGcName(),
//              gcni.getGcCause());
      if ("end of minor GC".equals(gcni.getGcAction())) {
        GCInfoBlock infoBlock = createInfoBlock2(gcni);
        infoBlock.setType(guessGcType(gcni));
        addGc(infoBlock, gcni);
        return;
      }
      if ("end of major GC".equals(gcni.getGcAction())) {
        GCInfoBlock infoBlock = createInfoBlock2(gcni);
        if (infoBlock == null) addEmpty(gcni.getGcInfo().getDuration());
        else {
          infoBlock.setType(guessGcType(gcni));
          addGc(infoBlock, gcni);
        }
        return;
      }
      if ("end of GC pause".equals(gcni.getGcAction()) ||
          "end of GC cycle".equals(gcni.getGcAction())) {
        //shenandoah minor gc | concurrent
        GCInfoBlock infoBlock = createInfoBlock2(gcni);
        if (infoBlock == null) addEmpty(gcni.getGcInfo().getDuration());
        else {
          infoBlock.setType(guessGcType(gcni));
          addGc(infoBlock, gcni);
        }
        return;
      }

      System.err.println("!!! phase: ["+gcni.getGcAction()+"]");
    }

    private GCInfoBlock createInfoBlock2(GarbageCollectionNotificationInfo gcmi) {
      if (gcmi.getGcInfo().getDuration() <= 0L) {
        return null;
      }
      long curTime = System.currentTimeMillis();
      String mbeanName = gcmi.getGcName();

      final GCInfoBlock infoBlock = new GCInfoBlock();
      infoBlock.setGCName(mbeanName);
      infoBlock.setTime(curTime);
      infoBlock.setCallNumber(gcmi.getGcInfo().getId());
      infoBlock.setDuration(gcmi.getGcInfo().getDuration());
//      infoBlock.setMemoryUsage(new MemoryUsage(
//              gcmi.getGcInfo().getMemoryUsageAfterGc()
//      ));
      return infoBlock;
    }
  };

  public void attachListenerToGarbageCollector(List<GarbageCollectorMXBean> mbeans) {
    // Attach a listener to the GarbageCollectorMXBeans

    for (final GarbageCollectorMXBean garbageCollectorMXBean : mbeans) {
      if (NotificationEmitter.class.isInstance(garbageCollectorMXBean)) {
        NotificationEmitter emitter = NotificationEmitter.class.cast(garbageCollectorMXBean);
        emitter.addNotificationListener(gcEventListener, null, null);
      }
    }
  }

  private GCInfoCollector() {
    setMaxEventsCount(maxEventsCount);
    List<GarbageCollectorMXBean> mbeans = ManagementFactory.getGarbageCollectorMXBeans();
    if (mbeans != null && !mbeans.isEmpty()) {
      attachListenerToGarbageCollector(mbeans);
    }
  }

  private static final Set<String> nonGcStuff = new HashSet<>();
  static {
    nonGcStuff.add("Metaspace");
    nonGcStuff.add("Compressed Class Space");
    nonGcStuff.add("CodeHeap 'non-profiled nmethods'");
    nonGcStuff.add("CodeHeap 'profiled nmethods'");
    nonGcStuff.add("CodeHeap 'non-nmethods'");
  }
  private void addGc(GCInfoBlock resInfoBlock, GarbageCollectionNotificationInfo gcni) {
    Runtime runtime = Runtime.getRuntime();
    if (resInfoBlock == null) {
      return;
    }
    Map<String, MemoryUsage> memoryUsageMap = gcni.getGcInfo().getMemoryUsageAfterGc();
    //skip all non GC stuff
    //count sum
    AtomicLong init = new AtomicLong();
    AtomicLong used = new AtomicLong();
    AtomicLong committed = new AtomicLong();
    AtomicLong max = new AtomicLong();
    memoryUsageMap.entrySet()
      .stream()
      .filter( entry -> !nonGcStuff.contains(entry.getKey()))
      .forEach( entry -> {
        MemoryUsage memoryUsage = entry.getValue();
        init.addAndGet(memoryUsage.getInit());
        used.addAndGet(memoryUsage.getUsed());
        committed.addAndGet(memoryUsage.getCommitted());
        max.addAndGet(memoryUsage.getMax());
      });
    resInfoBlock.setMemoryUsage(
            new MemoryUsage(
                    init.get(),
                    used.get(),
                    committed.get(),
                    max.get()
            )
    );
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
    addToStorage(resInfoBlock);
  }

  //@ToDo split to 3 graphs - young, survivor, old
  private void addToStorage(GCInfoBlock resInfoBlock) {
    if (resInfoBlock.getDuration() == 0) return;
    storage.addLast(resInfoBlock);
    if (storage.size() > maxEventsCount) storage.removeFirst();
  }

  @Deprecated
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
    infoBlock.setEmpty(true);
    addToStorage(infoBlock);
  }

  public GCInfoBlock.Payloads getLastGcState() {
    return lastGcState;
  }

  public synchronized GCInfoBlock getLastBlock() {
    return storage.getLast();
  }

  public synchronized int getCurrentSize() {
    return getAll().size();
  }

  public synchronized List<GCInfoBlock> getAll() {
    return storage.stream().filter(gcInfoBlock -> !gcInfoBlock.isEmpty()).collect(Collectors.toList());
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

  private static interface ReferenceValue<T> {
    T getValue();

    static <T> ReferenceValue<T> getInstance(ReferenceType type, T value) {
      ReferenceValue<T> resul;
      switch (type) {
        case WEAK: resul = new GCInfoCollector.WeakReferenceHolder<T>(value);
          break;
        case SOFT: resul = new GCInfoCollector.SoftReferenceHolder<T>(value);
          break;
        case STRONG: resul = new GCInfoCollector.StrongReferenceHolder<T>(value);
          break;
        default:  throw new IllegalArgumentException();
      };
      return resul;
    }
  }
  public enum ReferenceType{
    WEAK, SOFT, STRONG;
  }
  private static class StrongReferenceHolder<T> implements ReferenceValue<T> {
    private final T value;
    public StrongReferenceHolder(T value) {
      this.value = value;
    }
    public T getValue() {
      return value;
    }
  }
  private static class SoftReferenceHolder<T> implements ReferenceValue<T> {
    private final SoftReference<T> value;
    public SoftReferenceHolder(T val) {
      value = new SoftReference<T>(val);
    }
    public T getValue() {
      return value.get();
    }
  }
  private static class WeakReferenceHolder<T> implements ReferenceValue<T> {
    private final WeakReference<T> value;
    public WeakReferenceHolder(T val) {
      value = new WeakReference<>(val);
    }
    public T getValue() {
      return value.get();
    }
  }
  public static void main(String[] args) throws Exception {
    final AtomicInteger cicleNumber = new AtomicInteger();
    // to attach listener
    GCInfoCollector infoCollector = GCInfoCollector.getGCInfoCollector(1000);
    Thread nagibatel = new Thread(()->{
      Map<Integer, ReferenceValue<byte[]>> map = new HashMap<>();
      for (int i = 0; i < 100_000_000; i++) {
        cicleNumber.set(i);
        map.put(i % 2049, i%3==0
                           ? ReferenceValue.getInstance(ReferenceType.STRONG, new byte[1024_800])
                           : ReferenceValue.getInstance(ReferenceType.SOFT, new byte[1024_800]));

        if (i % 1401==0) System.gc();
        if ( i % 100 == 0) {
          try {
            Thread.currentThread().sleep(100);
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
        }
      }
    });
    nagibatel.start();
    while ( true ) {
      long free = (long) Runtime.getRuntime().freeMemory() / 1024 / 1024;
      System.out.println(free + " MiB on i: " + cicleNumber.get() );
      Thread.currentThread().sleep(5000);

      if (free>2000) {
        String html = new GCInfo2HtmlPrinter().getHtml();
        File f = new File("/Users/mac/IdeaProjects/GCInfo/gcinfo_"+System.currentTimeMillis()+".html");
        System.err.println(f.getCanonicalPath());
        FileOutputStream fileOutputStream = new FileOutputStream(f);
        fileOutputStream.write(html.getBytes());
        fileOutputStream.flush();
        fileOutputStream.close();

      }
    }
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
  GCInfoBlock.GcType guessGcType(GarbageCollectionNotificationInfo gcni) {
    GarbageCollectors collector = GcDetector.get(gcni);
    if (collector.isConcurrentPhase(gcni.getGcCause(), gcni.getGcName())) return GCInfoBlock.GcType.CONCURRENT;
    return GCInfoBlock.GcType.STW;
  }
}
