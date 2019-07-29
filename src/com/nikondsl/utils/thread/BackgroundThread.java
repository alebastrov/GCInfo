package com.nikondsl.utils.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class BackgroundThread {
    private final Thread thread;
    private AtomicBoolean stop = new AtomicBoolean(false);
    private long nextSleepTime = TimeUnit.SECONDS.toMillis(10);

    public BackgroundThread(String name, long sleepTime, Callable worker) {
        thread = new Thread(() -> {
            while (!stop.get()) {
                try {
                    worker.call();
                    Thread.currentThread().sleep(getNextSleepTime());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception ex) {
                    System.err.println(ex);
                }
            }
        });
        nextSleepTime = sleepTime;
        thread.setName(name);
        thread.start();
    }

    public void shutdown() {
        stop.set(true);
        thread.interrupt();
    }

    public long getNextSleepTime() {
        return nextSleepTime;
    }

    public void setNextSleepTime(long nextSleepTime) {
        this.nextSleepTime = nextSleepTime;
    }
}
