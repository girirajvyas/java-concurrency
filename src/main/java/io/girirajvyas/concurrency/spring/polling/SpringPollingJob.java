package io.girirajvyas.concurrency.spring.polling;

import java.time.Duration;
import java.time.Instant;
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
    System.out.println("I am called by:" + Thread.currentThread().getName());
    for (int i = 0; i < 10; i++) {
      threadPoolTaskExecutor.execute(new RunnableTask(i));
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
      Thread.sleep(10_000);
      if(i == 8) {
        // throw new InterruptedException();
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
      Thread.interrupted();
    }
  }
}
