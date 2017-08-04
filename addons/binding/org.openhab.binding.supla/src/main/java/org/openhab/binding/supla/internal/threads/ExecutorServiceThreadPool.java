package org.openhab.binding.supla.internal.threads;

import java.util.concurrent.ExecutorService;

import static java.util.Objects.requireNonNull;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
public class ExecutorServiceThreadPool implements ThreadPool {
    private final ExecutorService executorService;

    public ExecutorServiceThreadPool(ExecutorService executorService) {
        this.executorService = requireNonNull(executorService);
    }

    @Override
    public void submit(Runnable runnable) {
        executorService.submit(runnable);
    }
}
