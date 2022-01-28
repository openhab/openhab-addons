/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.wemo.internal.WemoBindingConstants;
import org.openhab.binding.wemo.internal.http.WemoHttpCall;
import org.openhab.core.io.transport.upnp.UpnpIOParticipant;
import org.openhab.core.io.transport.upnp.UpnpIOService;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link WemoBaseThingHandler} provides a base implementation for the
 * concrete WeMo handlers for each thing type.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public abstract class WemoBaseThingHandler extends BaseThingHandler implements UpnpIOParticipant {

    private final Logger logger = LoggerFactory.getLogger(WemoBaseThingHandler.class);
    private final Object upnpLock = new Object();

    protected @Nullable UpnpIOService service;
    protected WemoHttpCall wemoHttpCaller;
    protected String host = "";

    public WemoBaseThingHandler(Thing thing, UpnpIOService upnpIOService, WemoHttpCall wemoHttpCaller) {
        super(thing);
        this.service = upnpIOService;
        this.wemoHttpCaller = wemoHttpCaller;
    }

    @Override
    public void initialize() {
        // can be overridden by subclasses
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // can be overridden by subclasses
    }

    @Override
    public void onStatusChanged(boolean status) {
        // can be overridden by subclasses
    }

    @Override
    public void onValueReceived(@Nullable String variable, @Nullable String value, @Nullable String service) {
        // can be overridden by subclasses
    }

    @Override
    public void onServiceSubscribed(@Nullable String service, boolean succeeded) {
        if (service != null) {
            logger.debug("WeMo {}: Subscription to service {} {}", getUDN(), service,
                    succeeded ? "succeeded" : "failed");
        }
    }

    @Override
    public @Nullable String getUDN() {
        return (String) this.getThing().getConfiguration().get(WemoBindingConstants.UDN);
    }

    protected boolean isUpnpDeviceRegistered() {
        UpnpIOService service = this.service;
        if (service != null) {
            return service.isRegistered(this);
        }
        return false;
    }

    protected synchronized void addSubscription(String subscription) {
        synchronized (upnpLock) {
            UpnpIOService service = this.service;
            if (service != null) {
                if (service.isRegistered(this)) {
                    logger.debug("Setting up GENA subscription {}: Subscribing to service {}...", getUDN(),
                            subscription);
                    service.addSubscription(this, subscription, WemoBindingConstants.SUBSCRIPTION_DURATION_SECONDS);
                } else {
                    logger.debug(
                            "Setting up WeMo GENA subscription for '{}' FAILED - service.isRegistered(this) is FALSE",
                            getThing().getUID());
                }
            }
        }
    }

    protected synchronized void removeSubscription(String subscription) {
        synchronized (upnpLock) {
            UpnpIOService service = this.service;
            if (service != null) {
                if (service.isRegistered(this)) {
                    logger.debug("WeMo {}: Unsubscribing from service {}...", getUDN(), subscription);
                    service.removeSubscription(this, subscription);
                    service.unregisterParticipant(this);
                }
            }
        }
    }

    protected String getHost() {
        String localHost = host;
        if (!localHost.isEmpty()) {
            return localHost;
        }
        UpnpIOService localService = service;
        if (localService != null) {
            URL descriptorURL = localService.getDescriptorURL(this);
            if (descriptorURL != null) {
                return descriptorURL.getHost();
            }
        }
        return "";
    }
}
