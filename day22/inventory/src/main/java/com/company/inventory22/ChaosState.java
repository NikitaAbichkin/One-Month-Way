package com.company.inventory22;

import org.springframework.stereotype.Component;

@Component
public class ChaosState {
    private volatile long delayMs = 0;
    private volatile int failRate = 0;

    public long getDelayMs() {
        return delayMs;
    }

    public void setDelayMs(long delayMs) {
        this.delayMs = delayMs;
    }

    public int getFailRate() {
        return failRate;
    }

    public void setFailRate(int failRate) {
        this.failRate = failRate;
    }
}
