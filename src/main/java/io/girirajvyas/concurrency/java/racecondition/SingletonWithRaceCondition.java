package io.girirajvyas.concurrency.java.racecondition;

public class SingletonWithRaceCondition {

  private static SingletonWithRaceCondition instance;

  private SingletonWithRaceCondition() {}

  public static SingletonWithRaceCondition getInstance() {
    if (instance == null) {
      instance = new SingletonWithRaceCondition();
    }
    return instance;
  }
}
