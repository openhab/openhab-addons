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
package org.openhab.binding.ipobserver.internal;

import static org.openhab.binding.ipobserver.internal.IpObserverBindingConstants.LIVE_DATA_URL;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;

/**
 * The {@link IpObserverDiscoveryJob} class allows auto discovery of
 * devices for a single IP address. This is used
 * for threading to make discovery faster.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class IpObserverDiscoveryJob implements Runnable {
    private IpObserverDiscoveryService discoveryClass;
    private String ipAddress;

    public IpObserverDiscoveryJob(IpObserverDiscoveryService service, String ip) {
        this.discoveryClass = service;
        this.ipAddress = ip;
    }

    @Override
    public void run() {
        if (isIpObserverDevice(this.ipAddress)) {
            discoveryClass.submitDiscoveryResults(this.ipAddress);
        }
    }

    private boolean isIpObserverDevice(String ip) {
        Request request = discoveryClass.getHttpClient().newRequest("http://" + ip + LIVE_DATA_URL);
        request.method(HttpMethod.GET).timeout(5, TimeUnit.SECONDS).header(HttpHeader.ACCEPT_ENCODING, "gzip");
        ContentResponse contentResponse;
        try {
            contentResponse = request.send();
            if (contentResponse.getStatus() == 200 && contentResponse.getContentAsString().contains("livedata.htm")) {
                return true;
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
        }
        return false;
    }
}
