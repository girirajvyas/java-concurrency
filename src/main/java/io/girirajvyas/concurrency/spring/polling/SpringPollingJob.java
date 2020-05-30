package io.girirajvyas.concurrency.spring.polling;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

@Component
public class SpringPollingJob {

  @Autowired
  private ThreadPoolTaskScheduler threadPoolTaskScheduler;

  @Autowired
  private ThreadPoolTaskExecutor threadPoolTaskExecutor;

  private boolean firstBatch = true;
  
  public SpringPollingJob() {}

  @PostConstruct
  public void init() {
    // 1. schedule() demo with method reference instead of Runnable impl
    // threadPoolTaskScheduler.schedule(this::process, Instant.now());
    // 2. schedule() demo with lambda instead of Runnable impl
    // threadPoolTaskScheduler.schedule(() -> process(), Instant.now());
    // 3. scheduleAtFixedRate()
    // threadPoolTaskScheduler.scheduleAtFixedRate(this::process, Instant.now(),
    //    Duration.ofMillis(10_000));
    threadPoolTaskScheduler.scheduleWithFixedDelay(this::process, Instant.now(), Duration.ofSeconds(30));
  }

  public void process() {
    System.out.println("Current time:" + Instant.now());
    
    if(firstBatch) {
      System.out.println("firstBatch status for first iteration: " + firstBatch);
      firstBatch = false;
    }
    
    System.out.println("firstBatch status is: " + firstBatch);
    System.out.println("I am called by:" + Thread.currentThread().getName());
    List<Future<?>> futureArr = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      Future<?> future = threadPoolTaskExecutor.submit(new RunnableTask(i));
      futureArr.add(future);
    }
    
    for (Future<?> future : futureArr) {
      try {
        future.get();
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (ExecutionException e) {
        e.printStackTrace();
      }
    }
    
  }
}

class RunnableTask implements Runnable {
  
  private Integer i;
  RunnableTask(Integer i) {
    this.i = i;
  }
  
  public void run() {
    System.out.println("I am called by Executorservice:" + i + " "+ Thread.currentThread().getName());
    try {
      Thread.sleep(30_000);
    } catch (InterruptedException e) {
      e.printStackTrace();
      Thread.interrupted();
    }
  }
}
