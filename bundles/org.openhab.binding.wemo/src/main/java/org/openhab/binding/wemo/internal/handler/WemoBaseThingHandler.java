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
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
 * concrete WeMo handlers.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public abstract class WemoBaseThingHandler extends BaseThingHandler implements UpnpIOParticipant {

    private static final int SUBSCRIPTION_RENEWAL_INITIAL_DELAY_SECONDS = 15;
    private static final int SUBSCRIPTION_RENEWAL_INTERVAL_SECONDS = 60;

    private final Logger logger = LoggerFactory.getLogger(WemoBaseThingHandler.class);

    protected @Nullable UpnpIOService service;
    protected WemoHttpCall wemoHttpCaller;
    protected String host = "";

    private Map<String, Instant> subscriptions = new ConcurrentHashMap<String, Instant>();
    private @Nullable ScheduledFuture<?> subscriptionRenewalJob;

    public WemoBaseThingHandler(Thing thing, UpnpIOService upnpIOService, WemoHttpCall wemoHttpCaller) {
        super(thing);
        this.service = upnpIOService;
        this.wemoHttpCaller = wemoHttpCaller;
    }

    @Override
    public void initialize() {
        UpnpIOService service = this.service;
        if (service != null) {
            logger.debug("Registering UPnP participant for {}", getThing().getUID());
            service.registerParticipant(this);
        }
    }

    @Override
    public void dispose() {
        removeSubscriptions();
        UpnpIOService service = this.service;
        if (service != null) {
            logger.debug("Unregistering UPnP participant for {}", getThing().getUID());
            service.unregisterParticipant(this);
        }
        cancelSubscriptionRenewalJob();
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
        if (service == null) {
            return;
        }
        logger.debug("Subscription to service {} for {} {}", service, getUDN(), succeeded ? "succeeded" : "failed");
        if (succeeded) {
            subscriptions.put(service, Instant.now());
        }
    }

    @Override
    public @Nullable String getUDN() {
        return (String) this.getThing().getConfiguration().get(WemoBindingConstants.UDN);
    }

    protected boolean isUpnpDeviceRegistered() {
        UpnpIOService service = this.service;
        return service != null && service.isRegistered(this);
    }

    protected void addSubscription(String serviceId) {
        if (subscriptions.containsKey(serviceId)) {
            logger.debug("{} already subscribed to {}", getUDN(), serviceId);
            return;
        }
        if (subscriptions.isEmpty()) {
            logger.debug("Adding first GENA subscription for {}, scheduling renewal job", getUDN());
            scheduleSubscriptionRenewalJob();
        }
        subscriptions.put(serviceId, Instant.ofEpochSecond(0));
        UpnpIOService service = this.service;
        if (service == null) {
            return;
        }
        if (!service.isRegistered(this)) {
            logger.debug("Registering UPnP participant for {}", getUDN());
            service.registerParticipant(this);
        }
        if (!service.isRegistered(this)) {
            logger.debug("Trying to add GENA subscription {} for {}, but service is not registered", serviceId,
                    getUDN());
            return;
        }
        logger.debug("Adding GENA subscription {} for {}", serviceId, getUDN());
        service.addSubscription(this, serviceId, WemoBindingConstants.SUBSCRIPTION_DURATION_SECONDS);
    }

    protected void removeSubscription(String serviceId) {
        UpnpIOService service = this.service;
        if (service == null) {
            return;
        }
        subscriptions.remove(serviceId);
        if (subscriptions.isEmpty()) {
            logger.debug("Removing last GENA subscription for {}, cancelling renewal job", getUDN());
            cancelSubscriptionRenewalJob();
        }
        if (!service.isRegistered(this)) {
            logger.debug("Trying to remove GENA subscription {} for {}, but service is not registered", serviceId,
                    getUDN());
            return;
        }
        logger.debug("Unsubscribing {} from service {}", getUDN(), serviceId);
        service.removeSubscription(this, serviceId);
    }

    private void scheduleSubscriptionRenewalJob() {
        cancelSubscriptionRenewalJob();
        this.subscriptionRenewalJob = scheduler.scheduleWithFixedDelay(this::renewSubscriptions,
                SUBSCRIPTION_RENEWAL_INITIAL_DELAY_SECONDS, SUBSCRIPTION_RENEWAL_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    private void cancelSubscriptionRenewalJob() {
        ScheduledFuture<?> subscriptionRenewalJob = this.subscriptionRenewalJob;
        if (subscriptionRenewalJob != null) {
            subscriptionRenewalJob.cancel(true);
        }
        this.subscriptionRenewalJob = null;
    }

    private void renewSubscriptions() {
        if (subscriptions.isEmpty()) {
            return;
        }
        UpnpIOService service = this.service;
        if (service == null) {
            return;
        }
        if (!service.isRegistered(this)) {
            service.registerParticipant(this);
        }
        if (!service.isRegistered(this)) {
            logger.debug("Trying to renew GENA subscriptions for {}, but service is not registered", getUDN());
            return;
        }
        logger.debug("Renewing GENA subscriptions for {}", getUDN());
        subscriptions.forEach((serviceId, lastRenewed) -> {
            if (lastRenewed.isBefore(Instant.now().minusSeconds(
                    WemoBindingConstants.SUBSCRIPTION_DURATION_SECONDS - SUBSCRIPTION_RENEWAL_INTERVAL_SECONDS))) {
                logger.debug("Subscription for service {} with timestamp {} has expired, renewing", serviceId,
                        lastRenewed);
                service.removeSubscription(this, serviceId);
                service.addSubscription(this, serviceId, WemoBindingConstants.SUBSCRIPTION_DURATION_SECONDS);
            }
        });
    }

    private void removeSubscriptions() {
        if (subscriptions.isEmpty()) {
            return;
        }
        UpnpIOService service = this.service;
        if (service == null) {
            return;
        }
        if (!service.isRegistered(this)) {
            logger.debug("Trying to remove GENA subscriptions for {}, but service is not registered",
                    getThing().getUID());
            return;
        }
        logger.debug("Removing GENA subscriptions for {}", getUDN());
        subscriptions.forEach((serviceId, lastRenewed) -> {
            logger.debug("Removing subscription for service {}", serviceId);
            service.removeSubscription(this, serviceId);
        });
        subscriptions.clear();
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
