package net.novalab.webstart.file.monitoring.task.entity;

import java.nio.file.Path;
import java.util.Date;
import java.util.Objects;

public class Task implements Runnable, Comparable<Task> {
    private Path domain;
    private Runnable execution;
    private String name;
    private Date lastActionTime;

    public Task(Path domain, String name, Runnable execution) {
        this.domain = Objects.requireNonNull(domain);
        this.name = name;
        this.execution = execution;
        updateLastActionTimeToNow();
    }

    public Path getDomain() {
        return domain;
    }

    @Override
    public void run() {
        execution.run();
    }

    public Date getLastActionTime() {
        return lastActionTime;
    }

    public void updateLastActionTimeToNow() {
        this.lastActionTime = new Date();
    }

    public long timeSinceLastAction() {
        return new Date().getTime() - lastActionTime.getTime();
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Task task = (Task) o;

        return getDomain().equals(task.getDomain());
    }

    @Override
    public int hashCode() {
        return getDomain().hashCode();
    }

    @Override
    public int compareTo(Task o) {
        return getDomain().compareTo(o.getDomain());
    }
}
