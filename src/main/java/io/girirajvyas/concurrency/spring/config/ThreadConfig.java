package io.girirajvyas.concurrency.spring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@ComponentScan(basePackages = "io.girirajvyas.concurrency")
public class ThreadConfig {

  @Bean
  public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setThreadNamePrefix("SpringTaskScheduler-");
    scheduler.setPoolSize(1);// redundant as default is 1
    //scheduler.setWaitForTasksToCompleteOnShutdown(true);
    System.out.println("scheduler instantiated: " + scheduler.getClass());
    return scheduler;
  }

  @Bean
  public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
    ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
    taskExecutor.setThreadNamePrefix("SpringTaskExecutor-");
    taskExecutor.setCorePoolSize(5);
    taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
    taskExecutor.setAwaitTerminationMillis(5);
    return taskExecutor;
  }


}
