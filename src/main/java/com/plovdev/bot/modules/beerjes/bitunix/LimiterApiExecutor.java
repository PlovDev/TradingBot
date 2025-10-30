package com.plovdev.bot.modules.beerjes.bitunix;

import com.google.common.util.concurrent.RateLimiter;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class LimiterApiExecutor {
    private final BlockingQueue<ExecutionTask> taskQueue = new LinkedBlockingQueue<>();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final RateLimiter rateLimiter = RateLimiter.create(0.9); // 1 запрос в секунду
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    public void addTask(ExecutionTask task) {
        taskQueue.add(task);
    }
    private void processQueue() {
        if (taskQueue.isEmpty()) {
            return;
        }

        ExecutionTask task = taskQueue.peek(); // Берем первую задачу
        if (task == null) return;

        try {
            rateLimiter.acquire(); // Ждем, чтобы не превышать лимит
            task.getTask().run(); // Выполняем задачу
            // Если успешно, удаляем из очереди
            taskQueue.poll();

        } catch (Exception e) {
            // Если ошибка
            System.err.println("Ошибка выполнения задачи '" + task.getName() + "': " + e.getMessage());
            task.incrementRetry();

            if (task.shouldRetry()) {
                // Можно добавить логику для повторной попытки через время
                // Например, через некоторое время снова добавить в очередь
                System.out.println("Повторная попытка задачи '" + task.getName() + "' (" + task.getRetryCount() + "/" + task.getMaxRetries() + ")");
                processQueue();
            } else {
                System.err.println("Задача '" + task.getName() + "' не удалась после " + task.getMaxRetries() + " попыток");
                taskQueue.poll(); // Удаляем из очереди, если исчерпаны попытки
            }
        }
    }
    public void shutdown() {
        isRunning.set(false);
        executor.shutdown();
        try {
            if (!executor.awaitTermination(20, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    public int getQueueSize() {
        return taskQueue.size();
    }
}