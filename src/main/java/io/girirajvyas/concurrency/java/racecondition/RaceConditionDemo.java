package io.girirajvyas.concurrency.java.racecondition;

public class RaceConditionDemo {

  public static void main(String[] args) throws InterruptedException {
    // singleThreadExecution();// Works fine as single thread
    multiThreadExecution();// random output
    multiThreadExecutionWithSynchronization();// correct output
  }

  public static void singleThreadExecution() throws InterruptedException {
    LongWrapper longWrapper = new LongWrapper(0L);

    Runnable runnable = () -> {
      for (int i = 0; i < 1_000; i++) {
        longWrapper.incrementValue();
      }
    };

    Thread thread = new Thread(runnable);
    thread.start();
    thread.join(); // makes sure all threads are executed
    System.out.println("Value is: " + longWrapper.getValue());
  }

  public static void multiThreadExecution() throws InterruptedException {
    LongWrapper longWrapper = new LongWrapper(0L);

    Runnable runnable = () -> {
      for (int i = 0; i < 1_000; i++) {
        longWrapper.incrementValue();
      }
    };

    Thread[] threads = new Thread[1000];
    for (int i = 0; i < threads.length; i++) {
      threads[i] = new Thread(runnable);
      threads[i].start();
    }

    for (int i = 0; i < threads.length; i++) {
      threads[i].join();
    }

    System.out.println("Value is: " + longWrapper.getValue());
  }

  public static void multiThreadExecutionWithSynchronization() throws InterruptedException {
    LongWrapper longWrapper = new LongWrapper(0L);

    Runnable runnable = () -> {
      for (int i = 0; i < 1_000; i++) {
        longWrapper.synchronizedIncrementValue();
      }
    };

    Thread[] threads = new Thread[1000];
    for (int i = 0; i < threads.length; i++) {
      threads[i] = new Thread(runnable);
      threads[i].start();
    }

    for (int i = 0; i < threads.length; i++) {
      threads[i].join();
    }

    System.out.println("Value is: " + longWrapper.getValue());
  }
}
