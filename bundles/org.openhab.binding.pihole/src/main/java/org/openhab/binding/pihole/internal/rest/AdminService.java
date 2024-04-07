package org.openhab.binding.pihole.internal.rest;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.pihole.internal.rest.model.DnsStatistics;

@NonNullByDefault
public interface AdminService {
    /**
     * Retrieves a summary of DNS statistics.
     *
     * @return An optional containing the DNS statistics.
     * @throws ExecutionException If an execution exception occurs.
     * @throws InterruptedException If the operation is interrupted.
     * @throws TimeoutException If the operation times out.
     */
    Optional<DnsStatistics> summary() throws ExecutionException, InterruptedException, TimeoutException;

    /**
     * Disables blocking for a specified duration.
     *
     * @param seconds The duration in seconds for which blocking should be disabled.
     * @throws ExecutionException If an execution exception occurs.
     * @throws InterruptedException If the operation is interrupted.
     * @throws TimeoutException If the operation times out.
     */
    void disableBlocking(long seconds) throws ExecutionException, InterruptedException, TimeoutException;

    /**
     * Enables blocking.
     *
     * @throws ExecutionException If an execution exception occurs.
     * @throws InterruptedException If the operation is interrupted.
     * @throws TimeoutException If the operation times out.
     */
    void enableBlocking() throws ExecutionException, InterruptedException, TimeoutException;
}
