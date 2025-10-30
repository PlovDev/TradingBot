package com.plovdev.bot.modules.beerjes.bitunix;

import java.util.concurrent.atomic.AtomicInteger;

public class ExecutionTask {
    private final String name;
    private final Runnable task;
    private final AtomicInteger retryCount = new AtomicInteger(0);
    private final int maxRetries;

    public ExecutionTask(String name, Runnable task, int maxRetries) {
        this.name = name;
        this.task = task;
        this.maxRetries = maxRetries;
    }

    public boolean shouldRetry() {
        return retryCount.get() < maxRetries;
    }

    public void incrementRetry() {
        retryCount.incrementAndGet();
    }

    public String getName() { return name; }
    public Runnable getTask() { return task; }
    public int getRetryCount() { return retryCount.get(); }
    public int getMaxRetries() { return maxRetries; }
}