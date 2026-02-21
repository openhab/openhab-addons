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
package org.openhab.binding.linkplay.internal.client.upnp;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jupnp.UpnpService;
import org.jupnp.controlpoint.ControlPoint;
import org.jupnp.model.message.header.UDNHeader;
import org.jupnp.model.meta.RemoteDevice;
import org.jupnp.model.types.UDN;
import org.openhab.core.io.transport.upnp.UpnpIOParticipant;
import org.openhab.core.io.transport.upnp.UpnpIOService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles all UPnP communication for LinkPlay devices.
 * Manages subscriptions, events, and UPnP actions.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class LinkPlayUpnpClient implements UpnpIOParticipant, LinkPlayUpnpCommands.UpnpActionExecutor {

    private final Logger logger = LoggerFactory.getLogger(LinkPlayUpnpClient.class);

    private static final String SERVICE_AV_TRANSPORT = "AVTransport";
    private static final String SERVICE_RENDERING_CONTROL = "RenderingControl";
    private static final Collection<String> SERVICE_SUBSCRIPTIONS = Arrays.asList(SERVICE_AV_TRANSPORT,
            SERVICE_RENDERING_CONTROL);
    private static final int SUBSCRIPTION_DURATION = 1800;

    private final LinkPlayUpnpClientHandler handler;
    private final UpnpIOService upnpIOService;
    private final UpnpService upnpService;
    private final ScheduledExecutorService scheduler;
    private final LinkPlayUpnpCommands commands = new LinkPlayUpnpCommands(this);
    private final Object upnpLock = new Object();
    private final Map<String, Boolean> subscriptionState = Collections.synchronizedMap(new HashMap<>());
    private final Set<CompletableFuture<?>> pendingFutures = ConcurrentHashMap.newKeySet();
    private final CopyOnWriteArrayList<UpnpValueListener> upnpValueListeners = new CopyOnWriteArrayList<>();
    private final AtomicBoolean needsUpnpInitialization = new AtomicBoolean(true);

    private @Nullable RemoteDevice remoteDevice;
    private String udn = "";
    private boolean disposed;

    public LinkPlayUpnpClient(LinkPlayUpnpClientHandler handler, UpnpIOService upnpIOService, UpnpService upnpService,
            ScheduledExecutorService scheduler) {
        this.handler = handler;
        this.upnpIOService = upnpIOService;
        this.upnpService = upnpService;
        this.scheduler = scheduler;
    }

    public void initialize(String udn) {
        this.udn = udn;
        this.disposed = false;
        needsUpnpInitialization.set(true);
    }

    public void dispose() {
        logger.debug("{}: UPnP dispose", udn);
        disposed = true;
        removeSubscriptions();
        for (CompletableFuture<?> f : pendingFutures) {
            f.completeExceptionally(new IllegalStateException("UPnP client disposed"));
        }
        pendingFutures.clear();
    }

    public void setRemoteDevice(@Nullable RemoteDevice device) {
        this.remoteDevice = device;
    }

    public @Nullable RemoteDevice getRemoteDevice() {
        return remoteDevice;
    }

    public boolean needsUpnpInitialization() {
        return needsUpnpInitialization.get();
    }

    public void setNeedsUpnpInitialization(boolean needs) {
        needsUpnpInitialization.set(needs);
    }

    public LinkPlayUpnpCommands getCommands() {
        return commands;
    }

    @Override
    public String getUDN() {
        return udn;
    }

    @Override
    public void onValueReceived(@Nullable String variable, @Nullable String value, @Nullable String service) {
        if (!handler.shouldProcessUpnpEvents()) {
            logger.warn("{}: onValueReceived: handler is not ready to process UPnP events", udn);
            return;
        }
        if (logger.isTraceEnabled()) {
            logger.debug("{}: onValueReceived: {} {} {}", udn, service, variable, value);
        } else {
            // ignore logging position related variables
            if (logger.isDebugEnabled() && !("AbsTime".equals(variable) || "RelCount".equals(variable)
                    || "RelTime".equals(variable) || "AbsCount".equals(variable) || "Track".equals(variable)
                    || "TrackDuration".equals(variable))) {
                logger.debug("{}: onValueReceived: {} {} {}", udn, service, variable, value);
            }
        }
        if (value == null || service == null) {
            return;
        }

        switch (service) {
            case SERVICE_AV_TRANSPORT:
                Map<String, String> avt = UpnpXMLParser.getAVTransportFromXML(value);
                handler.onAvTransportEvent(avt);
                break;
            case SERVICE_RENDERING_CONTROL:
                Map<String, @Nullable String> rc = UpnpXMLParser.getRenderingControlFromXML(value);
                handler.onRenderingControlEvent(rc);
                break;
            default:
                logger.debug("{}: onValueReceived unknown service: {} {} {}", udn, service, variable, value);
                break;
        }

        upnpValueListeners.forEach(listener -> {
            try {
                listener.onUpnpValueReceived(variable, value, service);
            } catch (Exception e) {
                logger.debug("{}: Error in UPnP value listener", udn, e);
            }
        });
    }

    @Override
    public void onServiceSubscribed(@Nullable String service, boolean succeeded) {
        logger.debug("{}: onServiceSubscribed: {} {}", udn, service, succeeded);
        if (service != null) {
            subscriptionState.put(service, succeeded);
            handler.onUpnpSubscriptionStateChanged(isFullySubscribed());
        }
    }

    @Override
    public void onStatusChanged(boolean status) {
        logger.debug("{}: onStatusChanged: {}", udn, status);
        handler.onUpnpServiceStatusChanged(status);
    }

    @Override
    public CompletableFuture<Map<String, String>> executeAction(String serviceId, String actionId,
            @Nullable Map<String, String> inputs) {
        return executeAction(null, serviceId, actionId, inputs);
    }

    @Override
    public CompletableFuture<Map<String, String>> executeAction(@Nullable String namespace, String serviceId,
            String actionId, @Nullable Map<String, String> inputs) {
        if (disposed) {
            return CompletableFuture.failedFuture(new IllegalStateException("UPnP client is disposed"));
        }
        if (!"GetPositionInfo".equals(actionId)) {
            logger.debug("{}: Executing action {}:{} with inputs {}", udn, serviceId, actionId, inputs);
        }
        CompletableFuture<Map<String, String>> future = new CompletableFuture<>();
        pendingFutures.add(future);
        scheduler.execute(() -> {
            Map<String, String> result = upnpIOService.invokeAction(this, namespace, serviceId, actionId, inputs);
            if (logger.isTraceEnabled() && !"GetPositionInfo".equals(actionId)) {
                logger.trace("{}: Action result: {}", udn, result);
            }
            future.complete(result != null ? result : Collections.emptyMap());
        });
        future.whenComplete((r, t) -> pendingFutures.remove(future));
        return future;
    }

    @Override
    public CompletableFuture<Void> executeVoidAction(String serviceId, String actionId,
            @Nullable Map<String, String> inputs) {
        return executeVoidAction(null, serviceId, actionId, inputs);
    }

    @Override
    public CompletableFuture<Void> executeVoidAction(@Nullable String namespace, String serviceId, String actionId,
            @Nullable Map<String, String> inputs) {
        if (disposed) {
            return CompletableFuture.failedFuture(new IllegalStateException("UPnP client is disposed"));
        }
        logger.debug("{}: Executing ack action {}:{} with inputs {}", udn, serviceId, actionId, inputs);
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            upnpIOService.invokeAction(this, namespace, serviceId, actionId, inputs);
        }, scheduler);
        pendingFutures.add(future);
        future.whenComplete((r, t) -> pendingFutures.remove(future));
        return future;
    }

    public void addSubscriptions() {
        synchronized (upnpLock) {
            if (disposed) {
                return;
            }
            if (upnpIOService.isRegistered(this)) {
                for (String subscription : SERVICE_SUBSCRIPTIONS) {
                    Boolean state = subscriptionState.get(subscription);
                    if (state == null || !state) {
                        logger.debug("{}: Subscribing to service {}...", udn, subscription);
                        upnpIOService.addSubscription(this, subscription, SUBSCRIPTION_DURATION);
                        subscriptionState.put(subscription, true);
                    }
                }
            }
        }
    }

    public void removeSubscriptions() {
        synchronized (upnpLock) {
            for (String subscription : SERVICE_SUBSCRIPTIONS) {
                logger.debug("{}: Unsubscribing from service {}...", udn, subscription);
                upnpIOService.removeSubscription(this, subscription);
            }
            subscriptionState.clear();
        }
    }

    public void clearSubscriptionState() {
        subscriptionState.clear();
    }

    public boolean isFullySubscribed() {
        for (String s : SERVICE_SUBSCRIPTIONS) {
            if (!subscriptionState.getOrDefault(s, true)) {
                return false;
            }
        }
        return true;
    }

    public boolean isRegistered() {
        return upnpIOService.isRegistered(this);
    }

    public void sendDeviceSearchRequest() {
        ControlPoint controlPoint = upnpService.getControlPoint();
        if (controlPoint != null) {
            controlPoint.search(new UDNHeader(new UDN(getUDN())));
            logger.debug("M-SEARCH query sent for device UDN: {}", getUDN());
        }
    }

    public void registerUpnpValueListener(UpnpValueListener listener) {
        upnpValueListeners.add(listener);
    }

    public void unregisterUpnpValueListener(UpnpValueListener listener) {
        upnpValueListeners.remove(listener);
    }

    public @Nullable PlayQueue getPlayListQueue() {
        try {
            Map<String, String> result = commands.browseQueue("TotalQueue").get();
            if (result.get("QueueContext") instanceof String queueContext && !queueContext.isBlank()) {
                if (UpnpXMLParser.getPlayQueueFromXML(queueContext) instanceof PlayQueue playQueue) {
                    logger.debug("{}: Play list queue current play list name: {}", udn,
                            playQueue.getCurrentPlayListName());
                    return playQueue;
                } else {
                    logger.debug("{}: Could not parse PlayQueue from TotalQueueResponse", udn);
                }
            } else {
                logger.debug("{}: Could not parse QueueContext from TotalQueueResponse", udn);
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.trace("{}: Error while retrieving play list queue: {}", udn, e.getMessage(), e);
        }
        return null;
    }
}
