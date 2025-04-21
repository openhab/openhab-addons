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
package org.openhab.binding.wemo.internal.handler;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.wemo.internal.WemoBindingConstants;
import org.openhab.core.io.transport.upnp.UpnpIOParticipant;
import org.openhab.core.io.transport.upnp.UpnpIOService;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link WemoBaseThingHandler} provides a base implementation for the
 * concrete WeMo handlers.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public abstract class WemoBaseThingHandler extends BaseThingHandler implements UpnpIOParticipant {

    private static final int PORT_RANGE_START = 49151;
    private static final int PORT_RANGE_END = 49157;
    private static final int HTTP_TIMEOUT_MS = 1000;

    protected final HttpClient httpClient;

    private final Logger logger = LoggerFactory.getLogger(WemoBaseThingHandler.class);
    private final UpnpIOService service;
    private final Object upnpLock = new Object();

    private @Nullable String host;
    private @Nullable Integer port;
    private Map<String, Instant> subscriptions = new ConcurrentHashMap<>();

    public WemoBaseThingHandler(final Thing thing, final UpnpIOService upnpIOService, final HttpClient httpClient) {
        super(thing);
        this.service = upnpIOService;
        this.httpClient = httpClient;
    }

    @Override
    public void initialize() {
        logger.debug("Registering UPnP participant for {}", getThing().getUID());
        service.registerParticipant(this);
    }

    @Override
    public void dispose() {
        removeSubscriptions();
        logger.debug("Unregistering UPnP participant for {}", getThing().getUID());
        service.unregisterParticipant(this);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // can be overridden by subclasses
    }

    @Override
    public void onStatusChanged(boolean status) {
        if (status) {
            logger.debug("UPnP device {} for {} is present", getUDN(), getThing().getUID());
        } else {
            logger.info("UPnP device {} for {} is absent", getUDN(), getThing().getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR);
            // Expire subscriptions.
            synchronized (upnpLock) {
                for (Entry<String, Instant> subscription : subscriptions.entrySet()) {
                    subscription.setValue(Instant.MIN);
                }
            }
        }
    }

    @Override
    public void onValueReceived(@Nullable String variable, @Nullable String value, @Nullable String service) {
        // can be overridden by subclasses
    }

    @Override
    public void onServiceSubscribed(@Nullable String service, boolean succeeded) {
        if (service == null) {
            return;
        }
        logger.debug("Subscription to service {} for {} {}", service, getUDN(), succeeded ? "succeeded" : "failed");
        if (succeeded) {
            synchronized (upnpLock) {
                subscriptions.put(service, Instant.now());
            }
        }
    }

    @Override
    public @Nullable String getUDN() {
        return (String) this.getConfig().get(WemoBindingConstants.UDN);
    }

    protected boolean isUpnpDeviceRegistered() {
        return service.isRegistered(this);
    }

    protected void addSubscription(String serviceId) {
        synchronized (upnpLock) {
            if (subscriptions.containsKey(serviceId)) {
                logger.debug("{} already subscribed to {}", getUDN(), serviceId);
                return;
            }
            subscriptions.put(serviceId, Instant.MIN);
            logger.debug("Adding GENA subscription {} for {}, participant is {}", serviceId, getUDN(),
                    service.isRegistered(this) ? "registered" : "not registered");
        }
        service.addSubscription(this, serviceId, WemoBindingConstants.SUBSCRIPTION_DURATION_SECONDS);
    }

    private void removeSubscriptions() {
        logger.debug("Removing GENA subscriptions for {}, participant is {}", getUDN(),
                service.isRegistered(this) ? "registered" : "not registered");
        synchronized (upnpLock) {
            subscriptions.forEach((serviceId, lastRenewed) -> {
                logger.debug("Removing subscription for service {}", serviceId);
                service.removeSubscription(this, serviceId);
            });
            subscriptions.clear();
        }
    }

    private @Nullable String getHost() {
        if (host != null) {
            return host;
        }
        host = getHostFromService();
        return host;
    }

    private @Nullable String getHostFromService() {
        URL descriptorURL = service.getDescriptorURL(this);
        if (descriptorURL != null) {
            return descriptorURL.getHost();
        }
        return null;
    }

    private @Nullable Integer getPort() {
        if (port != null) {
            return port;
        }
        port = getPortFromService();
        return port;
    }

    private @Nullable Integer getPortFromService() {
        URL descriptorURL = service.getDescriptorURL(this);
        if (descriptorURL != null) {
            int port = descriptorURL.getPort();
            if (port != -1) {
                return port;
            }
        }
        return null;
    }

    protected String probeAndExecuteCall(String actionService, String soapHeader, String soapBody) throws IOException {
        String host = getHost();
        if (host == null) {
            throw new IOException("@text/config-status.error.missing-ip");
        }

        Integer lastPort = getPort();
        Integer portFromService = getPortFromService();

        // Build prioritized list of ports to try
        Set<Integer> portsToCheck = new LinkedHashSet<>();

        // Last known working port
        if (lastPort != null) {
            portsToCheck.add(lastPort);
        }

        // Port announced via UPnP service
        if (portFromService != null) {
            portsToCheck.add(portFromService);
        }

        // Add remaining ports from the defined range
        for (int port = PORT_RANGE_START; port <= PORT_RANGE_END; port++) {
            portsToCheck.add(port);
        }

        for (Integer port : portsToCheck) {
            String wemoURL = "http://" + host + ":" + port + "/upnp/control/" + actionService + "1";
            try {
                String response = executeCall(wemoURL, soapHeader, soapBody);
                this.port = port;
                return response;
            } catch (IOException e) {
                logger.debug("Failed to connect to device: {}", e.getMessage());
            }
        }

        String attemptedPorts = portsToCheck.stream().map(String::valueOf).collect(Collectors.joining(", "));

        throw new IOException("Failed to connect to device. All attempts on ports [" + attemptedPorts + "] failed.");
    }

    private String executeCall(String wemoURL, String soapHeader, String soapBody) throws IOException {
        Request request = httpClient.newRequest(wemoURL).timeout(HTTP_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .header(HttpHeader.CONTENT_TYPE, WemoBindingConstants.HTTP_CALL_CONTENT_HEADER)
                .header("SOAPACTION", soapHeader).method(HttpMethod.POST).content(new StringContentProvider(soapBody));

        logger.trace("Performing HTTP request for URL: '{}', header: '{}', request body: '{}'", wemoURL, soapHeader,
                soapBody);

        try {
            ContentResponse response = request.send();

            String responseContent = response.getContentAsString();
            logger.trace("HTTP response body: '{}'", responseContent);

            int status = response.getStatus();
            if (!HttpStatus.isSuccess(status)) {
                throw new IOException("The HTTP request failed with error " + status);
            }

            return responseContent;
        } catch (TimeoutException | ExecutionException | InterruptedException e) {
            throw new IOException("The HTTP request failed", e);
        }
    }
}
