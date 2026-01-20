package com.ab.tasktracker.actuator.metric;

import com.ab.tasktracker.repository.TaskRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * actuator/metric/total.tasks.db -> Current value of tasks in DB
 * <p>
 * * Rest metrics for processing & memory:
 * * <p>
 * * /actuator/metrics/jvm.memory.used
 * * /actuator/metrics/system.cpu.usage
 * * /actuator/metrics/process.uptime
 * * /actuator/metrics/jvm.threads.live
 *
 *
 * actuator/metric/task.insert.count -> Total inserts in DB
 * actuator/metric/task.update.count -> Total updates in DB
 *
 */
@Component
public class TaskTrackerMetrics {

    private final TaskRepository taskRepository;

    private final Counter taskInsertCounter;

    private final Counter taskUpdateCounter;

    @Autowired
    public TaskTrackerMetrics(TaskRepository taskRepository, MeterRegistry meterRegistry) {
        this.taskRepository = taskRepository;
        this.taskInsertCounter = Counter.builder("task.insert.count").register(meterRegistry);
        this.taskUpdateCounter = Counter.builder("task.update.count").register(meterRegistry);
        Gauge.builder("total.tasks.db", taskRepository, TaskRepository::countTasks).description("Gives current value of total tasks in DB").register(meterRegistry);
    }

    public void taskInsertCounterIncrement() {
        taskInsertCounter.increment();
    }

    public void taskUpdateCounterIncrement() {
        taskUpdateCounter.increment();
    }
}
