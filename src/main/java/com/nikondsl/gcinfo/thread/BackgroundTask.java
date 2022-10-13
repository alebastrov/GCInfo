package com.nikondsl.gcinfo.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class BackgroundTask {
    static class CompletableFutureWithException<R> extends CompletableFuture<R> {
        private Exception exception = null;
        private CompletableFuture<R> future;

        public boolean isDone() {
            return exception != null || future.isDone();
        }

        public R get() throws InterruptedException, ExecutionException {
            R result = future.get();
            if ( exception != null ) {
                throw new ExecutionException( exception );
            }
            return result;
        }
    }

    public static <R>CompletableFuture<R> runInBackground(Callable<R> task) throws Exception {
        CompletableFutureWithException result = new CompletableFutureWithException();
        result.future = CompletableFuture.supplyAsync( () -> {
            try {
                return task.call();
            } catch (Exception e) {
                result.exception = e;
                return null;
            }
        } );
        return result;
    }

    public static void main(String[] args) throws Exception {
        System.err.println("Main");
        CompletableFuture<Integer> future1 = runInBackground( ()-> {
            System.out.println("task 1...");
            Thread.currentThread().sleep(1000);
            System.out.println("task 1 done");
            return 1;
        });
        System.err.println("Task 1 started");
        CompletableFuture<Integer> future2 = runInBackground( ()-> {
            System.out.println("task 2...");
            Thread.currentThread().sleep(1500);
            System.out.println("task 2 done");
            return 2;
        });
        System.err.println("Task 2 started");
        CompletableFuture<Integer> future3 = runInBackground( ()-> {
            System.out.println("task 3...");
            Thread.currentThread().sleep(2000);
            throw new IllegalArgumentException("Task 3 thew an exception");
        });
        System.err.println("Task 3 started");
        System.err.println("task 1 is done: " + future1.isDone());
        System.err.println("task 2 is done: " + future2.isDone());
        System.err.println("task 3 is done: " + future3.isDone());

        System.err.println("result 1: " + future1.get());
        System.err.println("result 2: " + future2.get());
        System.err.println("result 3: " + future3.get());
    }
}
