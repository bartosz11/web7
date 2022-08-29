package me.bartosz1.web7;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class HandlerThreadFactory implements ThreadFactory {
    private final String namingScheme;
    private final AtomicInteger counter = new AtomicInteger();

    public HandlerThreadFactory(String namingScheme) {
        this.namingScheme = namingScheme;
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, String.format(namingScheme, counter.incrementAndGet()));
    }
}
