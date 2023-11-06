package com.nikondsl.gcinfo.monitoring.gc;




import com.nikondsl.gcinfo.monitoring.gc.GCInfoBlock;
import com.nikondsl.gcinfo.monitoring.gc.types.GarbageCollectors;
import com.nikondsl.gcinfo.monitoring.gc.types.GcDetector;

import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.TabularDataSupport;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


public final class GCInfoCollector {
  private static final MemoryUsage empty = new MemoryUsage( 1, 1, 1, 1 );
  private static GCInfoCollector instance;

  private int maxEventsCount = 300;
  private ArrayDeque<GCInfoBlock> storage = null;
  private volatile GCInfoBlock.Payloads lastGcState = GCInfoBlock.Payloads.OK;
  private GcNotificationListener gcEventListener = new GcNotificationListener();

  public static synchronized GCInfoCollector getGCInfoCollector() {
    if ( instance == null ) {
      instance = new GCInfoCollector();
    }
    return instance;
  }

  class GcNotificationListener implements NotificationListener {
    @Override
    public void handleNotification( Notification notification, Object handback ) {
      CompositeData cdata = ( CompositeData ) notification.getUserData();
      String gcAction = ( String ) cdata.get( "gcAction" );
      CompositeDataSupport gcni = ( CompositeDataSupport ) cdata.get( "gcInfo" );
      if ( "end of minor GC".equals( gcAction ) ) {
        GCInfoBlock infoBlock = createInfoBlock2( cdata, gcni );
        if ( infoBlock == null ) return;
        infoBlock.setType( guessGcType( cdata ) );
        addGc( infoBlock );
        return;
      }
      if ( "end of major GC".equals( gcAction ) ) {
        GCInfoBlock infoBlock = createInfoBlock2( cdata, gcni );
        if ( infoBlock == null ) addEmpty( infoBlock.getDuration() );
        else {
          infoBlock.setType( guessGcType( cdata ) );
          addGc( infoBlock );
        }
        return;
      }
      if ( "end of GC pause".equals( gcAction ) || "end of GC cycle".equals( gcAction ) ) {
        //shenandoah minor gc | concurrent
        GCInfoBlock infoBlock = createInfoBlock2( cdata, gcni );
        if ( infoBlock == null ) addEmpty( ( Long ) gcni.get( "duration" ) );
        else {
          infoBlock.setType( guessGcType( cdata ) );
          addGc( infoBlock );
        }
      }

//      System.err.println( "!!! phase: [" + gcAction + "]" );
    }

    private GCInfoBlock createInfoBlock2( CompositeData cdata, CompositeDataSupport compositeData ) {
      Long duration = ( Long ) compositeData.get( "duration" );
      if ( duration <= 0L ) {
        return null;
      }
      long curTime = System.currentTimeMillis();
      String mbeanName = ( String ) cdata.get( "gcName" );

      final GCInfoBlock infoBlock = new GCInfoBlock();
      infoBlock.setGCName( mbeanName );
      infoBlock.setTime( curTime );
      infoBlock.setCallNumber( ( Long ) compositeData.get( "id" ) );
      infoBlock.setDuration( duration );
      TabularDataSupport tds = ( TabularDataSupport ) compositeData.get( "memoryUsageAfterGc" );
      infoBlock.setMemoryUsage( getSumOfMemoryUsages( tds ) );
      return infoBlock;
    }
  }

  private static final Set<String> nonGcStuff = new HashSet<>();
  static {
    nonGcStuff.add( "Metaspace" );
    nonGcStuff.add( "Compressed Class Space" );
    nonGcStuff.add( "CodeHeap 'non-profiled nmethods'" );
    nonGcStuff.add( "CodeHeap 'profiled nmethods'" );
    nonGcStuff.add( "CodeHeap 'non-nmethods'" );
  }

  private MemoryUsage getSumOfMemoryUsages( TabularDataSupport tds ) {
    AtomicLong init = new AtomicLong();
    AtomicLong used = new AtomicLong();
    AtomicLong committed = new AtomicLong();
    AtomicLong max = new AtomicLong();

    tds.forEach( ( k, v ) -> {
      String gcInfoEventName = ( String ) ( ( List ) k ).get( 0 );
      if ( nonGcStuff.contains( gcInfoEventName ) ) return; //skip all non GC stuff
      MemoryUsage memoryUsage = MemoryUsage.from( ( CompositeData ) ( ( CompositeDataSupport ) v ).get( "value" ) );
      init.addAndGet( memoryUsage.getInit() );
      used.addAndGet( memoryUsage.getUsed() );
      committed.addAndGet( memoryUsage.getCommitted() );
      max.addAndGet( memoryUsage.getMax() );
    } );

    long cm = committed.get();
    long mx = max.get();
    if ( mx >= 0 && cm > mx ) {
      cm = mx;
    }
    return new MemoryUsage( init.get(), used.get(), cm, mx );
  }

  public void attachListenerToGarbageCollector( List<GarbageCollectorMXBean> mbeans ) {
    // Attach a listener to the GarbageCollectorMXBeans

    for ( GarbageCollectorMXBean garbageCollectorMXBean : mbeans ) {
      if ( NotificationEmitter.class.isInstance( garbageCollectorMXBean ) ) {
        NotificationEmitter emitter = ( NotificationEmitter ) garbageCollectorMXBean;
        emitter.addNotificationListener( gcEventListener, null, null );
      }
    }
  }

  private GCInfoCollector() {
    setMaxEventsCount( maxEventsCount );
    List<GarbageCollectorMXBean> mbeans = ManagementFactory.getGarbageCollectorMXBeans();
    if ( mbeans != null && !mbeans.isEmpty() ) {
      attachListenerToGarbageCollector( mbeans );
    }
  }

  private void addGc( GCInfoBlock infoBlock ) {

    if ( infoBlock == null ) {
      return;
    }

    if ( !storage.isEmpty() ) {
      GCInfoBlock last = storage.getLast();
      long workTime = infoBlock.getTime() - last.getTime();
      long duration = infoBlock.getDuration();
      int workPercent = workTime == 0 ? 0 : ( int ) ( ( workTime - duration ) * 100L / workTime );
      infoBlock.setGcState( GCInfoBlock.Payloads.getByLoad( workPercent ) );
      lastGcState = infoBlock.getGcState();
    } else {
      lastGcState = GCInfoBlock.Payloads.OK;
    }
    addToStorage( infoBlock );
  }

  private void addToStorage( GCInfoBlock resInfoBlock ) {
    if ( resInfoBlock.getDuration() == 0 ) return;
    storage.addLast( resInfoBlock );
    if ( storage.size() > maxEventsCount ) storage.removeFirst();
  }

  @Deprecated
  private void addEmpty( long curTime ) {
    GCInfoBlock last = !storage.isEmpty() ? storage.getLast() : null;
    if ( last != null && last.getCallNumber() == 0 && last.getDuration() == 0 ) {
      last.setCompacted( last.getCompacted() + 1 );
      return;
    }
    final GCInfoBlock infoBlock = new GCInfoBlock();
    infoBlock.setGCName( "--" );
    infoBlock.setTime( curTime );
    infoBlock.setCallNumber( 0 );
    infoBlock.setDuration( 0 );
    infoBlock.setMemoryUsage( empty );
    infoBlock.setEmpty( true );
    addToStorage( infoBlock );
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
    return storage.stream().filter( gcInfoBlock -> !gcInfoBlock.isEmpty() ).toList();
  }

  public synchronized void setMaxEventsCount( int maxEventsCount ) {
    if ( maxEventsCount < 10 ) throw new IllegalArgumentException( "Too small (" + maxEventsCount + ") value for history (less than 10)" );
    this.maxEventsCount = maxEventsCount;
    ArrayDeque<GCInfoBlock> newStorage = new ArrayDeque<>( maxEventsCount );
    if ( storage != null && !storage.isEmpty() ) {
      newStorage.addAll( storage );
      while ( newStorage.size() > maxEventsCount ) newStorage.removeFirst();
    }
    storage = newStorage;
  }

  private interface ReferenceValue<T> {
    T getValue();

    static <T> ReferenceValue<T> getInstance( ReferenceType type, T value ) {
      ReferenceValue<T> resul;
      switch ( type ) {
        case WEAK: resul = new WeakReferenceHolder<>( value );
          break;
        case SOFT: resul = new SoftReferenceHolder<>( value );
          break;
        case STRONG: resul = new StrongReferenceHolder<>( value );
          break;
        default:  throw new IllegalArgumentException();
      }
      return resul;
    }
  }
  public enum ReferenceType {
    WEAK, SOFT, STRONG
  }
  private static class StrongReferenceHolder<T> implements ReferenceValue<T> {
    private final T value;
    StrongReferenceHolder( T value ) {
      this.value = value;
    }
    public T getValue() {
      return value;
    }
  }
  private static class SoftReferenceHolder<T> implements ReferenceValue<T> {
    private final SoftReference<T> value;
    SoftReferenceHolder( T val ) {
      value = new SoftReference<>( val );
    }
    public T getValue() {
      return value.get();
    }
  }
  private static class WeakReferenceHolder<T> implements ReferenceValue<T> {
    private final WeakReference<T> value;
    WeakReferenceHolder( T val ) {
      value = new WeakReference<>( val );
    }
    public T getValue() {
      return value.get();
    }
  }

  GCInfoBlock.GcType guessGcType( CompositeData cdata ) {
    GarbageCollectors collector = GcDetector.get( cdata );
    if ( collector.isConcurrentPhase( ( String ) cdata.get( "gcCause" ), ( String ) cdata.get( "gcName" ) ) ) return GCInfoBlock.GcType.CONCURRENT;
    return GCInfoBlock.GcType.STW;
  }


  public static void main( String[] args ) throws Exception {
    final AtomicInteger cicleNumber = new AtomicInteger();
    // to attach listener
    GCInfoCollector.getGCInfoCollector();
    Thread nagibatel = new Thread( () -> {
      Map<Integer, ReferenceValue<byte[]>> map = new HashMap<>();
      for ( int i = 0; i < 100_000_000; i++ ) {
        cicleNumber.set( i );
        map.put( i % 2049, i % 3 == 0
                ? ReferenceValue.getInstance( ReferenceType.STRONG, new byte[1024_800] )
                : ReferenceValue.getInstance( ReferenceType.SOFT, new byte[1024_800] ) );

        if ( i % 1401 == 0 ) System.gc();
        if ( i % 100 == 0 ) {
          try {
            Thread.currentThread().sleep( 10 );
          } catch ( InterruptedException e ) {
            throw new RuntimeException( e );
          }
        }
      }
    } );
    nagibatel.start();
    while ( true ) {
      long free = Runtime.getRuntime().freeMemory() / 1024 / 1024;
      System.out.println( free + " MiB on i: " + cicleNumber.get() );
      Thread.currentThread().sleep( 5000 );

    }
  }

}
