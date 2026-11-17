package com.fourthon.backend.common.client;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class RequestThrottle {

    private final long intervalNanos;
    private long nextAllowedAt;

    public RequestThrottle(Duration interval) {
        this.intervalNanos = interval.toNanos();
        this.nextAllowedAt = System.nanoTime();
    }

    public synchronized void acquire() {
        long now = System.nanoTime();
        long waitNanos = nextAllowedAt - now;
        if (waitNanos > 0) {
            sleep(waitNanos);
        }
        nextAllowedAt = Math.max(now, nextAllowedAt) + intervalNanos;
    }

    private void sleep(long nanos) {
        try {
            TimeUnit.NANOSECONDS.sleep(nanos);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("요청 대기 중 중단되었습니다", e);
        }
    }
}
