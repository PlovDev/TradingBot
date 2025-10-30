package com.plovdev.bot.modules.beerjes;

import com.plovdev.bot.modules.parsers.Signal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class SignalQueue {
    private static final Logger log = LoggerFactory.getLogger(SignalQueue.class);
    private final Queue<Signal> signalQueue = new PriorityBlockingQueue<>();
    private final AtomicBoolean isSignalProcessing = new AtomicBoolean(false);
    private Consumer<Signal> nextSignalHandler;
    private static final long MAX_SIGNAL_LIVE = 120_000;

    public void removeExpiredSignals() {
        long now = System.currentTimeMillis();
        // очередь прекрасно поддерживает этот метод. Это Java Collections Framework:)
        signalQueue.removeIf(signal -> (now - signal.getTimestamp()) > MAX_SIGNAL_LIVE);
    }


    public Consumer<Signal> getNextSignalHandler() {
        return nextSignalHandler;
    }

    public synchronized void setNextSignalHandler(Consumer<Signal> nextSignalHandler) {
        this.nextSignalHandler = nextSignalHandler;
    }

    /**
     * Добавляет сигнал в очередь и пытается запустить обработку, если она не идёт.
     */
    public void add(Signal signal) {
        log.info("Added new signal to queue, symbol: {}, direction: {}", signal.getSymbol(), signal.getDirection());
        signalQueue.offer(signal);
        tryStartNext();
    }

    /**
     * Вызывается ВНЕШНИМ кодом (например, из Bot) после полной обработки сигнала.
     */
    public void onSignalProcessComplete() {
        isSignalProcessing.set(false);
        tryStartNext();
    }

    private synchronized void tryStartNext() {
        if (signalQueue.isEmpty() || nextSignalHandler == null) {
            return;
        }
        removeExpiredSignals();
        // Пытаемся "захватить" обработку
        if (!isSignalProcessing.compareAndSet(false, true)) {
            return; // кто-то уже обрабатывает
        }
        Signal next = signalQueue.poll();
        nextSignalHandler.accept(next);
    }

    public boolean isProcessing() {
        return isSignalProcessing.get();
    }

    public int getQueueSize() {
        return signalQueue.size();
    }
    public boolean isEmpty() {
        return signalQueue.isEmpty();
    }

    public Queue<Signal> getSignalQueue() {
        return signalQueue;
    }
}