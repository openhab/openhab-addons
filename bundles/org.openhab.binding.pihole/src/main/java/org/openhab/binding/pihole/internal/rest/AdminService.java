/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.openhab.binding.pihole.internal.PiHoleException;
import org.openhab.binding.pihole.internal.rest.model.DnsStatistics;

import com.google.gson.Gson;

/**
 * @author Martin Grzeslowski - Initial contribution
 * @author Gaël L'hopital - Changed from 'interface' to abstract class
 */
@NonNullByDefault
public abstract class AdminService {
    protected static final long TIMEOUT_SECONDS = 10L;

    protected final HttpClient client;
    protected final Gson gson;

    public AdminService(HttpClient client, Gson gson) {
        this.client = client;
        this.gson = gson;
    }

    /**
     * Retrieves a summary of DNS statistics.
     *
     * @return An optional containing the DNS statistics.
     * @throws PiHoleException In case of error
     */
    public abstract Optional<DnsStatistics> summary() throws PiHoleException;

    /**
     * Disables blocking for a specified duration.
     *
     * @param seconds The duration in seconds for which blocking should be disabled.
     * @throws PiHoleException In case of error
     */
    public abstract void disableBlocking(long seconds) throws PiHoleException;

    /**
     * Enables blocking.
     *
     * @throws PiHoleException In case of error
     */
    public abstract void enableBlocking() throws PiHoleException;

    protected ContentResponse send(Request request) throws PiHoleException {
        try {
            return request.send();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new PiHoleException(
                    "Exception while sending request to Pi-hole. %s".formatted(e.getLocalizedMessage()), e);
        }
    }
}
