package org.openhab.binding.supla.internal.threads;

public interface ThreadPool {
    void submit(Runnable runnable);
}
