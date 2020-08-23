package io.girirajvyas.concurrency.java.basic;

public class FirstRunnableExample {

  public static void main(String[] args) {
    Runnable runnable = () -> {
      System.out.println("I am running in: " + Thread.currentThread().getName());
    };

    Thread t = new Thread(runnable);
    t.setName("My custom name");
    t.start(); // Output: I am running in: My custom name
    // t.run(); //AVOID!!! Output: I am running in: main
  }
}
