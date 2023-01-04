package io.flic.fliclib.javaclient;

import java.io.IOException;

/**
 * TimerTask.
 *
 * Use this interface instead of {@link Runnable} to avoid having to deal with IOExceptions.
 * Invocations of the run method on this interface from the {@link FlicClient} will propagate IOExceptions to the caller of {@link FlicClient#handleEvents()}.
 *
 */
public interface TimerTask {
    void run() throws IOException;
}
