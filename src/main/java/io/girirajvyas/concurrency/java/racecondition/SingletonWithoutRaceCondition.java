package io.girirajvyas.concurrency.java.racecondition;

public class SingletonWithoutRaceCondition {

  private static SingletonWithoutRaceCondition instance;

  private SingletonWithoutRaceCondition() {}

  public static synchronized SingletonWithoutRaceCondition getInstance() {
    if (instance == null) {
      instance = new SingletonWithoutRaceCondition();
    }
    return instance;
  }
}
