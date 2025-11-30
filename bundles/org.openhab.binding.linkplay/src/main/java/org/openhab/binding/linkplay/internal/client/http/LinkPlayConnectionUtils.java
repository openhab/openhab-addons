/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.linkplay.internal.client.http;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;

/**
 * Utility class to test the connection to a LinkPlay device.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class LinkPlayConnectionUtils {

    /**
     * Test the connection to a LinkPlay device and return the port number if the connection is successful.
     * 
     * @param httpClient the HTTP client to use
     * @param host the host to test
     * @return the port number if the connection is successful, null otherwise
     */
    public static @Nullable Integer testConnection(HttpClient httpClient, String host) {
        LinkPlayHTTPClient apiClient = new LinkPlayHTTPClient(httpClient);
        apiClient.setHost(host);
        int[] ports = { 443, 4443, 80 };
        for (int port : ports) {
            try {
                apiClient.setPort(port);
                // test that the device is reachable on the given port
                apiClient.getStatusEx().get(5000, TimeUnit.MILLISECONDS);
                return port;
            } catch (TimeoutException | ExecutionException | InterruptedException ignored) {
                // Continue to next port
            }
        }
        return null;
    }
}
