package org.openhab.binding.supla.internal.threads;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
public interface ThreadPool {
    void submit(Runnable runnable);
}
