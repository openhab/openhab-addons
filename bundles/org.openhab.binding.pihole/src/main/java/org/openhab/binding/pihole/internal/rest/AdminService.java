/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.pihole.internal.rest;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.pihole.internal.rest.model.DnsStatistics;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
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
