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

import java.net.URL;
import java.time.Instant;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.wemo.internal.ApiController;
import org.openhab.binding.wemo.internal.WemoBindingConstants;
import org.openhab.binding.wemo.internal.exception.MissingHostException;
import org.openhab.binding.wemo.internal.exception.WemoException;
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

    private final Logger logger = LoggerFactory.getLogger(WemoBaseThingHandler.class);
    private final UpnpIOService service;
    private final ApiController apiController;
    private final Object upnpLock = new Object();

    private @Nullable String host;
    private Map<String, Instant> subscriptions = new ConcurrentHashMap<>();

    public WemoBaseThingHandler(final Thing thing, final UpnpIOService upnpIOService, final HttpClient httpClient) {
        super(thing);
        this.apiController = new ApiController(httpClient);
        this.service = upnpIOService;
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

    private @Nullable String getHostFromService() {
        URL descriptorURL = service.getDescriptorURL(this);
        if (descriptorURL != null) {
            return descriptorURL.getHost();
        }
        return null;
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

    protected String probeAndExecuteCall(String actionService, String soapHeader, String soapBody)
            throws WemoException {
        String host = this.host;

        if (host == null) {
            host = this.host = getHostFromService();
        }

        if (host == null) {
            throw new MissingHostException("Host/IP address is missing");
        }

        try {
            return apiController.probeAndExecuteCall(host, getPortFromService(), actionService, soapHeader, soapBody);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WemoException("Interrupted", e);
        }
    }
}
