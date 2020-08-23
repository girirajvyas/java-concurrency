package io.girirajvyas.concurrency.java.racecondition;

public class LongWrapper {
  private long value;

  public LongWrapper(long value) {
    this.value = value;
  }

  public long getValue() {
    return value;
  }

  public void incrementValue() {
    value = value + 1;
  }

  public synchronized void synchronizedIncrementValue() {
    value = value + 1;
  }

}
