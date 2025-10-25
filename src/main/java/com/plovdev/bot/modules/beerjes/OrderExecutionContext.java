package com.plovdev.bot.modules.beerjes;

import com.plovdev.bot.modules.models.StopInProfitTrigger;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Этот класс инкапсулирует состояние выполнения одного ордера.
 * Он используется для предотвращения состояний гонки при одновременной обработке нескольких пользователей.
 */
public class OrderExecutionContext {

    private String stopLossId;
    private final AtomicBoolean isPositioned = new AtomicBoolean(false);
    private final StopInProfitTrigger trigger;

    public OrderExecutionContext(StopInProfitTrigger trigger) {
        this.trigger = trigger;
    }

    public String getStopLossId() {
        return stopLossId;
    }

    public void setStopLossId(String stopLossId) {
        this.stopLossId = stopLossId;
    }

    public boolean isPositioned() {
        return isPositioned.get();
    }

    public void setPositioned(boolean positioned) {
        this.isPositioned.set(positioned);
    }
    
    public boolean getAndSetPositioned(boolean positioned) {
        return this.isPositioned.getAndSet(positioned);
    }

    public StopInProfitTrigger getTrigger() {
        return trigger;
    }
}
