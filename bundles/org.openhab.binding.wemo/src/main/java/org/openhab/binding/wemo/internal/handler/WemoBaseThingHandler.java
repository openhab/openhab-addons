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

import static org.openhab.binding.wemo.internal.WemoBindingConstants.*;

import java.net.URL;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.wemo.internal.http.WemoHttpCall;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.upnp.UpnpIOParticipant;
import org.openhab.core.io.transport.upnp.UpnpIOService;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link WemoBaseThingHandler} provides a base implementation for the
 * concrete WeMo handlers for each thing type.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class WemoBaseThingHandler extends BaseThingHandler implements UpnpIOParticipant {

    public final Logger logger = LoggerFactory.getLogger(WemoBaseThingHandler.class);

    protected @Nullable UpnpIOService service;
    protected WemoHttpCall wemoHttpCaller;
    protected String host = "";

    public final Object upnpLock = new Object();
    public final Object jobLock = new Object();

    public @Nullable ScheduledFuture<?> pollingJob;

    public Map<String, Boolean> subscriptionState = new HashMap<>();
    public final Collection<String> SERVICE_SUBSCRIPTIONS = Arrays.asList(BASICEVENT);

    public WemoBaseThingHandler(Thing thing, UpnpIOService upnpIOService, WemoHttpCall wemoHttpCaller) {
        super(thing);
        this.service = upnpIOService;
        this.wemoHttpCaller = wemoHttpCaller;
    }

    @Override
    public void initialize() {
        // can be overridden by subclasses
        Configuration configuration = getConfig();

        if (configuration.get(UDN) != null) {
            logger.debug("Initializing handler for UDN '{}'", configuration.get(UDN));
            UpnpIOService localService = service;
            if (localService != null) {
                localService.registerParticipant(this);
            }
            host = getHost();
            pollingJob = scheduler.scheduleWithFixedDelay(this::poll, 0, DEFAULT_REFRESH_INTERVAL_SECONDS,
                    TimeUnit.SECONDS);
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/config-status.error.missing-udn");
            logger.debug("Cannot initalize handler. UDN not set.");
        }
    }

    @Override
    public void dispose() {
        // can be overridden by subclasses
        logger.debug("Handler disposed for thing {}", getThing().getUID());

        ScheduledFuture<?> job = this.pollingJob;
        if (job != null) {
            job.cancel(true);
        }
        this.pollingJob = null;
        removeSubscription();
    }

    public void poll() {
        // can be overridden by subclasses
        synchronized (jobLock) {
            if (pollingJob == null) {
                return;
            }
            try {
                logger.debug("Polling job");
                host = getHost();
                // Check if the Wemo device is set in the UPnP service registry
                // If not, set the thing state to ONLINE/CONFIG-PENDING and wait for the next poll
                if (!isUpnpDeviceRegistered()) {
                    logger.debug("UPnP device {} not yet registered", getUDN());
                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                            "@text/config-status.pending.device-not-registered [\"" + getUDN() + "\"]");
                    synchronized (upnpLock) {
                        subscriptionState = new HashMap<>();
                    }
                    return;
                }
                updateStatus(ThingStatus.ONLINE);
                updateWemoState();
                addSubscription();
            } catch (Exception e) {
                logger.debug("Exception during poll: {}", e.getMessage(), e);
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // can be overridden by subclasses
    }

    protected void updateWemoState() {
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
        // can be overridden by subclasses
        if (service != null) {
            logger.debug("WeMo {}: Subscription to service {} {}", getUDN(), service,
                    succeeded ? "succeeded" : "failed");
            subscriptionState.put(service, succeeded);
        }
    }

    @Override
    public @Nullable String getUDN() {
        return (String) this.getThing().getConfiguration().get(UDN);
    }

    protected boolean isUpnpDeviceRegistered() {
        UpnpIOService service = this.service;
        if (service != null) {
            return service.isRegistered(this);
        }
        return false;
    }

    public synchronized void addSubscription() {
        // can be overridden by subclasses
        synchronized (upnpLock) {
            UpnpIOService localService = service;
            if (localService != null) {
                if (localService.isRegistered(this)) {
                    logger.debug("Checking WeMo GENA subscription for '{}'", getThing().getUID());

                    for (String subscription : SERVICE_SUBSCRIPTIONS) {
                        if (subscriptionState.get(subscription) == null) {
                            logger.debug("Setting up GENA subscription {}: Subscribing to service {}...", getUDN(),
                                    subscription);
                            localService.addSubscription(this, subscription, SUBSCRIPTION_DURATION_SECONDS);
                            subscriptionState.put(subscription, true);
                        }
                    }
                } else {
                    logger.debug(
                            "Setting up WeMo GENA subscription for '{}' FAILED - service.isRegistered(this) is FALSE",
                            getThing().getUID());
                }
            }
        }
    }

    public synchronized void removeSubscription() {
        // can be overridden by subclasses
        synchronized (upnpLock) {
            UpnpIOService localService = service;
            if (localService != null) {
                if (localService.isRegistered(this)) {
                    logger.debug("Removing WeMo GENA subscription for '{}'", getThing().getUID());
                    for (String subscription : SERVICE_SUBSCRIPTIONS) {
                        if (subscriptionState.get(subscription) != null) {
                            logger.debug("WeMo {}: Unsubscribing from service {}...", getUDN(), subscription);
                            localService.removeSubscription(this, subscription);
                        }
                    }
                    subscriptionState = new HashMap<>();
                    localService.unregisterParticipant(this);
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

    public @Nullable State getDateTimeState(String attributeValue) {
        long value = 0;
        try {
            value = Long.parseLong(attributeValue);
        } catch (NumberFormatException e) {
            logger.error("Unable to parse attributeValue '{}' for device '{}'; expected long", attributeValue,
                    getThing().getUID());
            return null;
        }
        ZonedDateTime zoned = ZonedDateTime.ofInstant(Instant.ofEpochSecond(value), TimeZone.getDefault().toZoneId());
        State dateTimeState = new DateTimeType(zoned);
        logger.trace("New attribute '{}' received", dateTimeState);
        return dateTimeState;
    }
}
