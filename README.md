# java concurrency

## Table of contents

- Basics
  - A. Understanding concurrency, Threading and synchronization
    - Introduction
    - Threads
    - At the same time meaning
  - B. Implementing the Producer/Consumer pattern using wait/notify
  - C. Ordering read and write operations on a multicore CPUs
  - D. Implementing a Thread safe singleton on a multicore CPUs

## A. Understanding Concurrency, Threading and Synchronization

### Introduction

- Concurrency: the art of doing several things at the same time
- What does correct code mean in the concurrent world
- How to improve your code by leveraging multi-core CPUs
- Writing code, implementing patterns
- Race condition, synchronization, volatility
- Visibility, false sharing, happens-before

### Threads

- A Thread is defined at the Operating System level
- A Thread is a set of instructions
- An application can be composed of several threads
- Different threads can be executed "at the same time"
- The java virtual machine works with Several threads (example: Garbage collector, Just in time compiler, etc)
- https://docs.oracle.com/javase/8/docs/api/java/lang/Thread.html

### At the same time meaning

- lets have a look for 4 tasks
- Writing a text document (write) | Running the spell check (check) | Print document(print) | Receiving mails (mails)
- On a CPU timeline a slice will be devoted to do a task.

**What is happening at CPU level?**  

```
1st case: CPU with one core only (it can do only one task at a time)  
(write)|(check)|(write)|(check)|(print)|(mails)  
---------------------------------------------------------> core 1  
0ms                                            10ms  
```

**why do we have feeling that everything is happening at the same time?**  
Because things are happening very fast (All above tasks are completed within a span of 0 to 10 milliseconds which is very less)  

```
2nd case: CPU with multiple cores  

(write)|(write)|(mail)|  
---------------------------------------------------------> core 1  

(check)|(check)|(print)  
---------------------------------------------------------> core 2  
```

Only on a multicore CPU, things are happening at a same time  

### CPU time sharing using a ThreadScheduler

Who is responsible for CPU sharing  
A special element called Scheduler, that is going to share the cpu time evenly divided into time slices  

There are 3 reasons for scheduler to pause a thread:  
1. The CPU should be shared equally among threads  
2. The thread is waiting for some more data  
3. The thread is waiting for another thread to do something  

### Race condition

- Accessing data concurrently may lead to issues
- Race condition means that 2 `different` threads are trying to `read and write` the `same` variable at the `same` time.
- This is called a race condition
- `same time` does not mean the same thing on single core and multicore CPU

Analysis (Example is The Singleton Pattern):  
```java
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
```

**What is happening if two threads are calling getInstance()?**
Now, What happens if 2 threads call this method at the same time, below is the timeline  
When the process starts, lets assume t1 starts processing and t2 is waiting for thread scheduler to give a time slice  

|Thread t1                         |Thread t2                                 |
|------------------------------    |---------                                 |
| Check if instance is null?       | Waiting                                  |
| The answer is yes                |                                          |
| Enters the if block              |                                          |
|`Thread scheduler pauses t1`      |                                          |
|                                  | `Thread scheduler gives time slice to t2`|
|                                  | Check if instance is null?               |
|                                  | The answer is yes                        |
|                                  | Enters the if block                      |
|                                  | Creates an instance of Singleton         |
|                                  | `Thread scheduler pauses t2`             |
| Creates an instance of Singleton |                                          |


![alt text](https://github.com/girirajvyas/java-concurrency/raw/master/src/test/resources/images/before-synchronization.png "Before Synchronization")


**How to Prevent?**  
via Synchronization  

> Synchronization prevents a block of code to be executed by more than one thread at the same time

![alt text](https://github.com/girirajvyas/java-concurrency/raw/master/src/test/resources/images/after-synchronization.png "After Synchronization")

How does synchronization works under the hood?  
- For any thread to get inside a method marked synchronized, it has to acquire key from a lock object
- Thread Red comes and want to execute the method, it acquires the key to that lock and processes
- Thread Blue also comes and ask for the key, as key is already with the t1, thread t2 has to wait
- Thread Red returns the key after the processing
- Now, Thread Blue can process, hence, making sure only one thread accessing the method at a time


> java uses a special object as lock object, that has a key. In Fact, Every object in java language has this key that is used for synchronization



**Understanding the Lock Object or Identifying key object**   
- So, For synchronization to work,  we need a special, technical object that will hold the key.
- In fact, every java object can play this role.
- This key is also called a monitor
- how can we designate this object? So there are ways as defined below


1. Synchronized used in a static method  
```java
public static SingletonWithRaceCondition getInstance() {
    if (instance == null) {
      instance = new SingletonWithRaceCondition();
    }
    return instance;
}
```
- In this code, key is the Singleton class itself
- **A synchronized `static method` uses the `class` as a synchronization object**

2. Synchronized used in a non-static method (public synchronized String getName())
```
    public synchronized String getName() {
       return this.name;
    }
```
- In this code, key is the instance of the class
- A synchronized `non-static method` uses the `instance` as a synchronization object. 
 
3. Use a dedicated object to conduct synchronization
```java
public class Person {
   private final Object key = new Object();
   
   public String init() {
      synchronized(key) {
        doStuff();
      }
   }
}
```
- Use a dedicated object to synchronized
- It is always good idea to hiode an object used for synchronization

### Synchronization use cases

**Synchronizing more than one method**  

![alt text](https://github.com/girirajvyas/java-concurrency/raw/master/src/test/resources/images/synchronization-instance-level-lock-eg1.png "Single Instance scenario")

![alt text](https://github.com/girirajvyas/java-concurrency/raw/master/src/test/resources/images/synchronization-instance-level-lock-eg2.png "Two instance objects scenario")

![alt text](https://github.com/girirajvyas/java-concurrency/raw/master/src/test/resources/images/synchronization-class-level-lock-eg.png "Class level scenario")

**Note:** Using `synchronized` keyword on a method, uses and implicit lock object, which is:  
- class object in case of static method and  
- instance of a class in case of a non-static method.  

# Lock Hierarchy

```bash
                                        java.util.concurrent.locks.Lock(Interface) 
                                                         |
                  _______________________________________|_________________________________ 
                 |                                       |                                 |
                 |                                       |                                 |
       Read Lock(static class)                 WriteLock (static class)          ReentrantLock(class)
```
```java
public class ReentrantLock implements Lock, java.io.Serializable {}

public class ReentrantReadWriteLock implements ReadWriteLock, java.io.Serializable {
  * public static class ReadLock implements Lock, java.io.Serializable {}
  * public static class WriteLock implements Lock, java.io.Serializable {}
}
public class StampedLock implements java.io.Serializable {(Java 8)
  * final class WriteLockView implements Lock {}
  * final class ReadLockView implements Lock {}
}
```

### Reentrant locks and Deadlocks

**Locks:**  
> Locks are reentrant: When a thread holds a lock, it can enter a block synchronized on the lock it is holding.

**Deadlock:**  
A deadlock is a situation where a thread T1 holds a key needed by a Thread T2  
                                     and T2 holds a key needed by  T1

>JVM is able to detect deadlock situations and can log information to help debug the application.
But, there is not much we can do id a deadlock situation occurs, beside rebooting the JVM.

### Runnable pattern

- The most basic way to create a thread in java is to use the Runnable Pattern
- First creat an instance of Runnable
- Then pass it to the constructor of thread class
- Then call the `start()` method of this Thread Object.

### Code examples
- Basics
```java
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
```
- Solving Race condition with Synchronization
```java
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
```

- Deadlock
```
public class Deadlock {

  private Object key1 = new Object();
  private Object key2 = new Object();

  public void a() {
    synchronized (key1) {
      System.out.println("[ " + Thread.currentThread().getName() + " ] I am in a()");
      b();
    }
  }

  public void b() {
    synchronized (key2) {
      System.out.println("[ " + Thread.currentThread().getName() + " ] I am in b()");
      c();
    }
  }

  public void c() {
    synchronized (key1) {
      System.out.println("[ " + Thread.currentThread().getName() + " ] I am in c()");
    }
  }
}

public class DeadlockDemo {

  public static void main(String[] args) throws InterruptedException {
    Deadlock deadlock = new Deadlock();

    Runnable r1 = () -> deadlock.a();
    Runnable r2 = () -> deadlock.b();

    Thread t1 = new Thread(r1);
    Thread t2 = new Thread(r2);

    t1.start();
    t2.start();

    t1.join();
    t2.join();

  }
}
```

### Summary

- A thread executes task in a special context. In java thread is modelled by an object, instance of thread class and task is modelled by an instance of a runnable interface.
- Race condition and how to use synchronization to avoid the Race condition
- Reentrantlocks and deadlocks

## Implementing the Producer/Consumer pattern using wait/notify

### Agenda

- The Runnable pattern
- What is the Producer/Consumer pattern
- How to implement it using synchronization and the wait/notify pattern

### The Runnable Pattern

**Introduction**  

- This is the first pattern used to launch threads in java
- Introduced in Java 1.0
- Other patterns have been introduced in Java 5 (java.util.concurrent API)

**How to launch a task in new Thread**  

- A thread executes a Task, In Java 1, the model for a task is the Runtime Interface.
- Runnable has only one method run(). Hence it is a FunctionalInterface from java 8

```java
@FunctionalInterface
public interface Runnable {
    void run();
}

```
Steps:  
- Create an instance of Runnable
- Create an instance of Thread with task as parameter
- Launch the thread
- COMMON MISTAKE: Do not call the run() method instead of start() method.
- Knowing in which thread a task is executed, you can use `Thread.currentthread()` static method returns the current thread
    ```java
    Runnable task = () -> System.out.println("Hello World..!");
    Thread thread = new Thread(task);
    thread.start();
    thread.run(); // NEVER do this, if you do this, the task will be executed, but in the current thread
    ```

**How to Stop a Thread**  

- It is more tricky then it seems
- There is a method in the Thread class called `stop()`
- This method should not be used
- It is there for legacy, backward compatibility reasons
- The right pattern is to use the `interrupt()` method
- The call to `interrupt()` causes the `isInterrupted()` method to return true
- If the thread is blocked, or waiting then the corresponding method will throw an InterruptedException
- The methods `wait()/notify()`, `join()` throw InterruptedException

```
Thread t1 = ...
t1.interrupt(); // causes the `isInterrupted()` method to return true


Runnable task = () -> {
   while(!Thread.currentThread().isInterrupted()) { 
     // Always check isInterrupted before executing the task
     // task 
   }
}

```


**What is a Producer/Consumer**

- A producer produces values in buffer
- A consumer consumes values from this buffer
- Be careful: the buffer can be empty or full
- Producers and Consumers are run in their own thread

Simple Producer

```java
int count = 0;
int[] buffer = new int[BUFFER_SIZE];

class Producer {
    public void produce() {
        while(isFull(buffer)) {
            buffer[count++] = 1;
        }
    } 
}
```

Simple Consumer

```java
int count = 0;
int[] buffer = new int[BUFFER_SIZE];

class Consumer {
    public void consume() {
        while(isEmpty(buffer)) {
            buffer[--count] = 0;
        }
    } 
}
```


# Executor framework

```bash
   
                                       java.util.concurrent.Executor (Interface)
                                            void execute(Runnable command);
                                                         |  
                                                         |
                                    java.util.concurrent.ExecutorService (Interface)---->Executors(factory)
                                                    void shutdown();                             ^
                                         List<Runnable> shutdownNow();                           |
                                         <T> Future<T> submit(Callable<T> task);                 |
                                         <T> Future<T> submit(Runnable task, T result);          |
                                         Future<?> submit(Runnable task);                        |
                          <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) |
                                                         |                                       |
                        _________________________________|_________________________________      |
                       |                                                                   |     | 
                       |                                                                   |     |
        AbstractExecutorService(abstract class)                         ScheduledExecutorService(Interface)
                       |
         ______________|_______________
        |                              |
        |                              |
ThreadPoolExecutor                ForkJoinPool
   (Class)                     (Class, since 1.7) 
        |
        |
ScheduledThreadPoolExecutor
(also implements ScheduledExecutorService)
```

```java
public interface ExecutorService extends Executor {}
public abstract class AbstractExecutorService implements ExecutorService {} 
public interface ScheduledExecutorService extends ExecutorService {}
public class ThreadPoolExecutor extends AbstractExecutorService {}
public class ForkJoinPool extends AbstractExecutorService {}
public class ScheduledThreadPoolExecutor extends ThreadPoolExecutor implements ScheduledExecutorService{} 
```

**Factory class to get above implementations:**  
```java
public class java.util.concurrent.Executors
```


# Spring version of Executor framework

```bash
                                       java.util.concurrent.Executor (Interface)
                                            void execute(Runnable command);
                                                         |
                                                         |
                                 org.springframework.core.task.TaskExecutor (Interface)
                                            @Override
                                            void execute(Runnable command);
                                                         |
                                                         |
                                 org.springframework.core.task.AsyncTaskExecutor (Interface)
                                   void execute(Runnable task, long startTimeout);
                                         Future<?> submit(Runnable task);
                                         <T> Future<T> submit(Callable<T> task);
                                                         |
                        _________________________________|_________________________________
                       |                                                                   |
                       |                                                                   |
           AsyncListenableTaskExecutor (Interface)                         SchedulingTaskExecutor(Interface)
        ListenableFuture<?> submitListenable(Runnable task);         default boolean prefersShortLivedTasks() {
        <T> ListenableFuture<T> submitListenable(Callable<T> task);           return true; }
```

```java
public interface TaskExecutor extends Executor
public interface AsyncTaskExecutor extends TaskExecutor
public interface AsyncListenableTaskExecutor extends AsyncTaskExecutor
public interface SchedulingTaskExecutor extends AsyncTaskExecutor
```

|Java                                             | Spring                                     |
|-------                                          |--------                                    |
|java.util.concurrent.ScheduledExecutorService    |org.springframework.scheduling.TaskScheduler|
|java.util.TimerTask                              | NA (above can be used)                     |

### Topics to explore
 - countdownlatch, cyclic barrier, threadlocal
 - bounded vs unbounded queue
 - advantages of Lock
 - odd/even numbers from different treads (learn inter thread communication)
   - https://www.baeldung.com/java-even-odd-numbers-with-2-threads
   - https://www.geeksforgeeks.org/print-even-and-odd-numbers-in-increasing-order-using-two-threads-in-java/


### Reference Documentation
For further reference, please consider the following sections:


**Concurrency**
- https://app.pluralsight.com/library/courses/java-patterns-concurrency-multi-threading
- https://www.javaworld.com/article/2074217/java-101--understanding-java-threads--part-1--introducing-threads-and-runnables.html
- https://dzone.com/articles/java-concurrency-evolution


**Spring**
- https://dzone.com/articles/schedulers-in-java-and-spring
- https://www.baeldung.com/spring-task-scheduler
- https://www.baeldung.com/java-threadpooltaskexecutor-core-vs-max-poolsize
- https://stackoverflow.com/questions/17659510/core-pool-size-vs-maximum-pool-size-in-threadpoolexecutor
- http://www.bigsoft.co.uk/blog/2009/11/27/rules-of-a-threadpoolexecutor-pool-size

**Spring Boot:**  
- [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.3.0.RELEASE/maven-plugin/reference/html/)
- [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.3.0.RELEASE/maven-plugin/reference/html/#build-image)