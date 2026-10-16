package com.company.v2inventory17.Indicator;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class Indicator implements HealthIndicator {
    private volatile boolean up = true;
    public void setUp(boolean up) { this.up = up; }

    public boolean isUp() { return up; }

    @Override
    public @Nullable Health health() {
        return  up ? Health.up().build(): Health.down().build();
    }
}
