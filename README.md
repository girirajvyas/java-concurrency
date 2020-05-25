# java concurrency

## Agenda

- Understanding concurrency, Threading and synchronization
- Implement the Producer/Consumer pattern using wait notify
- Ordering read and write operations on a multicore CPUs
- Implementing a Thread safe singleton on a multicore CPUs

## Understanding concurrency, Threading and synchronization

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
`1st case:` CPU with one core only (it can do only one task at a time)  
(write)|(check)|(write)|(check)|(print)|(mails)  
---------------------------------------------------------> core 1  
0ms                                            10ms  

**why do we have feeling that everything is happening at the same time?**  
Because things are happening very fast (All above tasks are completed within a span of 0 to 10 milliseconds which is very less)  

`2nd case:` CPU with multiple cores  

(write)|(write)|(mail)|  
---------------------------------------------------------> core 1  

(check)|(check)|(print)  
---------------------------------------------------------> core 2  

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

**How to Prevent?**  
via Synchronization  

> Synchronization prevents a block of code to be executed by more than one thread at the same time

How does synchronization works under the hood?  
- for any thread to get inside a method marked synchronized, it has to acquire key from a lock object
- Thread t1 comes and want to execute the method, it acquires the key to that lock and processes
- Tread t2 also comes and ask for the key, as key is already with the t1, thread t2 has to wait
- Thread t1 returns the key after the processing
- Now, Thread t2 can process, hence, making sure only one thread accessing the method at a time

**What is Lock Object**   
- So, For synchronization to work,  we need a special, technical object that will hold the key.
- In fact, every java object can play this role.
- This key is also called a monitor
- how can we designate this object? 


1. Synchronized used in a static method (public static synchronized SingletonWithRaceCondition getInstance())
- In this code, key is the Singleton class itself
- A synchronized `static method` uses the `class` as a synchronization object

2. Synchronized used in a non-static method (public synchronized String getName())
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

1. Synchronizing more than one method


**Note:** Synchronized keyword on a method, uses and implicit lock object, which is class object in case of static method and instance of a class in case of a non-static method.    

### Reentrant locks and Deadlocks

**Locks:**  
- Locks are reentrant: When a thread holds a lock, it can enter a block synchronized on the lock it is holding.


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



**Deadlock:**  
A deadlock is a situation where a thread T1 holds a key needed by a Thread T2
                                     and T2 holds a key needed by  T1



>JVM is able to detect deadlock situations and can log information to help debug the application.
But, there is not much we can do id a deadlock situation occurs, beside rebooting the JVM.

### Runnable patterns

- The most basic way to create a thread in java is to use the Runnable Pattern
- First creat an instance of Runnable
- Then pass it to the constructor of thread class
- Then call the start() method of this Thread Object.






# Executor framework

```bash
   
                                       java.util.concurrent.Executor (Interface)
                                                         |  
                                                         |
                                    java.util.concurrent.ExecutorService (Interface)
                                                         |
                        _________________________________|_________________________________ 
                       |                                                                   |
                       |                                                                   |  
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

Factory class to get above implementations:
public class java.util.concurrent.Executors
```








# Spring equivalents

|Java                                             | Spring                                     |
|-------                                          |--------                                    |
|java.util.concurrent.ScheduledExecutorService    |org.springframework.scheduling.TaskScheduler|
|java.util.TimerTask                              | NA (above can be used)                     |


### Reference Documentation
For further reference, please consider the following sections:

**Concurrency**
- https://www.javaworld.com/article/2074217/java-101--understanding-java-threads--part-1--introducing-threads-and-runnables.html


**Srping **
- https://dzone.com/articles/schedulers-in-java-and-spring
- https://www.baeldung.com/spring-task-scheduler
- https://www.baeldung.com/java-threadpooltaskexecutor-core-vs-max-poolsize
- https://stackoverflow.com/questions/17659510/core-pool-size-vs-maximum-pool-size-in-threadpoolexecutor
- http://www.bigsoft.co.uk/blog/2009/11/27/rules-of-a-threadpoolexecutor-pool-size

**Spring Boot:**  
* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.3.0.RELEASE/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.3.0.RELEASE/maven-plugin/reference/html/#build-image)

- https://www.netjstech.com/2017/08/java-lambda-expressions-interview-questions.html
