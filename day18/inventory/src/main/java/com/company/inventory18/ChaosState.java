package com.company.inventory18;

import org.springframework.stereotype.Component;

@Component
public class ChaosState {
    private volatile long delayMs = 0;

    public long getDelayMs() {
        return delayMs;
    }

    public void setDelayMs(long delayMs) {
        this.delayMs = delayMs;
    }
}
