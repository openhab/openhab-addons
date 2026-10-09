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
package org.openhab.binding.smartthings.internal.local;

import static org.openhab.binding.smartthings.internal.SmartthingsBindingConstants.BINDING_ID;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.local.LocalResources.Point;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Polling and capability-checked local control of an authenticated Samsung OCF appliance.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public class LocalApplianceHandler extends BaseThingHandler {
    private static final String IDENTITY = "/oic/d";
    private static final String COLLECTION = "/device/0";
    private static final String DEVICE_ID_PROPERTY = "deviceid";
    private static final int MAX_PENDING_COMMANDS = 16;
    private final Logger logger = LoggerFactory.getLogger(LocalApplianceHandler.class);
    private final Object lifecycle = new Object();
    private final ReentrantLock io = new ReentrantLock();
    private final TransportFactory transportFactory;
    private final ScheduledExecutorService executor;
    private volatile @Nullable Session session;
    private long generation;

    @FunctionalInterface
    interface TransportFactory {
        LocalTransport create(LocalApplianceConfiguration configuration) throws IOException;
    }

    private record PendingCommand(String channel, Command command) {
    }

    private static final class Session {
        final LocalApplianceConfiguration configuration;
        final LocalResources resources = new LocalResources();
        final ArrayDeque<PendingCommand> commands = new ArrayDeque<>();
        @Nullable
        UUID identity;
        volatile @Nullable LocalTransport transport;
        @Nullable
        ScheduledFuture<?> poll;
        @Nullable
        ScheduledFuture<?> worker;
        boolean running;
        boolean refresh = true;

        Session(LocalApplianceConfiguration configuration, @Nullable UUID identity) {
            this.configuration = configuration;
            this.identity = identity;
        }
    }

    private static final class IdentityMismatchException extends IOException {
        private static final long serialVersionUID = 1L;
    }

    public LocalApplianceHandler(Thing thing) {
        super(thing);
        this.transportFactory = LocalCoapTransport::new;
        this.executor = scheduler;
    }

    LocalApplianceHandler(Thing thing, TransportFactory transportFactory, ScheduledExecutorService executor) {
        super(thing);
        this.transportFactory = transportFactory;
        this.executor = executor;
    }

    @Override
    public void initialize() {
        long activation = retireSession();
        LocalApplianceConfiguration configuration = getConfigAs(LocalApplianceConfiguration.class);
        try {
            configuration.validate();
            String expected = configuration.deviceId.isBlank() ? getThing().getProperties().get(DEVICE_ID_PROPERTY)
                    : configuration.deviceId;
            UUID identity = expected == null || expected.isBlank() ? null : LocalApplianceConfiguration.uuid(expected);
            synchronized (lifecycle) {
                if (generation != activation) {
                    return;
                }
                Session current = new Session(configuration, identity);
                session = current;
                updateStatus(ThingStatus.UNKNOWN);
                startWorker(current);
            }
        } catch (IllegalArgumentException e) {
            synchronized (lifecycle) {
                if (generation == activation) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Check local connection settings and explicitly imported credentials");
                }
            }
        }
    }

    @Override
    public void dispose() {
        retireSession();
        super.dispose();
    }

    private long retireSession() {
        Session previous;
        long activation;
        synchronized (lifecycle) {
            activation = ++generation;
            previous = session;
            session = null;
            if (previous != null) {
                previous.commands.clear();
                if (previous.poll != null) {
                    previous.poll.cancel(false);
                }
                if (previous.worker != null) {
                    previous.worker.cancel(true);
                }
            }
        }
        if (previous != null) {
            // Closing aborts an exchange, but must not make framework lifecycle methods wait for network I/O.
            executor.execute(() -> closeTransport(previous));
        }
        return activation;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        synchronized (lifecycle) {
            Session current = session;
            if (current == null || !channelUID.getThingUID().equals(getThing().getUID())) {
                return;
            }
            if (command == RefreshType.REFRESH) {
                current.refresh = true;
            } else if (current.commands.size() < MAX_PENDING_COMMANDS) {
                current.commands.addLast(new PendingCommand(channelUID.getId(), command));
            } else {
                logger.debug("Local appliance command queue is full; command discarded");
                return;
            }
            startWorker(current);
        }
    }

    /** Called only under the lifecycle monitor; refreshes coalesce and each session has at most one I/O worker. */
    private void startWorker(Session current) {
        if (isActive(current) && !current.running) {
            current.running = true;
            current.worker = executor.schedule(() -> runWorker(current), 0, TimeUnit.MILLISECONDS);
        }
    }

    private void runWorker(Session current) {
        while (true) {
            PendingCommand pending;
            boolean refresh;
            synchronized (lifecycle) {
                if (!isActive(current)) {
                    return;
                }
                refresh = current.refresh;
                current.refresh = false;
                pending = refresh ? null : current.commands.pollFirst();
                if (!refresh && pending == null) {
                    current.running = false;
                    current.worker = null;
                    return;
                }
            }
            try {
                io.lockInterruptibly();
                try {
                    if (!isActive(current)) {
                        return;
                    }
                    LocalTransport transport = current.transport;
                    if (transport == null) {
                        transport = transportFactory.create(current.configuration);
                        current.transport = transport;
                        if (!isActive(current)) {
                            closeTransport(current);
                            return;
                        }
                    }
                    if (refresh) {
                        readResources(current, transport, current.resources);
                    } else if (pending != null) {
                        sendCommand(current, transport, pending);
                    }
                } finally {
                    io.unlock();
                }
                publish(current);
            } catch (IdentityMismatchException e) {
                failure(current, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Authenticated appliance identity does not match this Thing");
            } catch (IOException e) {
                failure(current, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Local appliance communication failed; check network and credentials");
            } catch (IllegalArgumentException e) {
                logger.debug("Local appliance command or resource representation was rejected");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                synchronized (lifecycle) {
                    current.running = false;
                    current.worker = null;
                }
                return;
            } catch (RuntimeException e) {
                logger.warn("Unexpected failure processing a local appliance operation");
                failure(current, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Unexpected local appliance processing failure");
            } finally {
                if (refresh) {
                    schedulePoll(current);
                }
            }
        }
    }

    private void readResources(Session current, LocalTransport transport, LocalResources resources) throws IOException {
        JsonElement identity = transport.get(IDENTITY);
        verifyIdentity(current, identity);
        resources.update(IDENTITY, identity);
        JsonElement payload = transport.get(COLLECTION);
        LocalResources received = new LocalResources();
        received.update(COLLECTION, payload);
        resources.update(COLLECTION, payload);
        if (!hasBatchRepresentation(payload)) {
            try {
                JsonElement batch = transport.get(COLLECTION + "?if=oic.if.b");
                received.update(COLLECTION, batch);
                resources.update(COLLECTION, batch);
            } catch (IOException e) {
                // Some appliances expose only collection links, which can still be hydrated individually.
                logger.debug("Local appliance batch interface unavailable; reading linked resources instead");
            }
        }
        Set<String> visited = new HashSet<>();
        while (true) {
            // Links must be hydrated on every poll, even when the retained cache already has a value.
            List<String> stubs = received.snapshot().entrySet().stream()
                    .filter(entry -> isStub(entry.getValue()) && !isSecurityResource(entry.getKey()))
                    .map(Map.Entry::getKey).filter(path -> !visited.contains(path)).toList();
            if (stubs.isEmpty()) {
                return;
            }
            for (String href : stubs) {
                visited.add(href);
                try {
                    JsonElement representation = transport.get(href);
                    received.update(href, representation);
                    resources.update(href, representation);
                } catch (IOException e) {
                    // Optional enrichment must not invalidate a successful baseline batch read.
                    logger.debug("A linked local appliance resource could not be read");
                }
            }
        }
    }

    private static boolean hasBatchRepresentation(JsonElement payload) {
        if (payload.isJsonArray()) {
            for (JsonElement entry : payload.getAsJsonArray()) {
                if (hasBatchRepresentation(entry)) {
                    return true;
                }
            }
        } else if (payload.isJsonObject()) {
            JsonObject object = payload.getAsJsonObject();
            if (object.has("href") && object.has("rep") && object.get("rep").isJsonObject()) {
                return true;
            }
            JsonElement links = object.get("links");
            return links != null && hasBatchRepresentation(links);
        }
        return false;
    }

    private static boolean isStub(JsonObject representation) {
        return representation.size() == 1 && representation.has("href");
    }

    private static boolean isSecurityResource(String href) {
        return href.contains("/sec/");
    }

    private static void verifyIdentity(Session current, JsonElement payload) throws IOException {
        if (!payload.isJsonObject()) {
            throw new IOException("Missing authenticated appliance identity");
        }
        JsonElement deviceId = payload.getAsJsonObject().get("di");
        if (deviceId == null || !deviceId.isJsonPrimitive() || !deviceId.getAsJsonPrimitive().isString()) {
            throw new IOException("Missing authenticated appliance identity");
        }
        UUID identity;
        try {
            identity = LocalApplianceConfiguration.uuid(deviceId.getAsString());
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid authenticated appliance identity");
        }
        if (current.identity != null && !current.identity.equals(identity)) {
            throw new IdentityMismatchException();
        }
        current.identity = identity;
    }

    private void sendCommand(Session current, LocalTransport transport, PendingCommand pending) throws IOException {
        Point previous = current.resources.points().stream().filter(point -> point.id().equals(pending.channel()))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Unknown resource channel"));
        if (!previous.writable()) {
            throw new IllegalArgumentException("Resource channel is read-only");
        }
        // A separate live model fails closed if capabilities or Remote Control disappear from a fresh response.
        LocalResources live = new LocalResources();
        readResources(current, transport, live);
        for (String href : current.resources.snapshot().keySet()) {
            if (!IDENTITY.equals(href) && !COLLECTION.equals(href) && !isSecurityResource(href)) {
                try {
                    JsonElement payload = transport.get(href);
                    live.update(href, payload);
                    current.resources.update(href, payload);
                } catch (IOException e) {
                    if (href.equals(previous.href()) || href.endsWith("/remotectrl/0")
                            || href.endsWith("/remotectrl/vs/0") || previous.itemType().equals("Number:Temperature")
                                    && href.endsWith("/temperature/control/vs/0")) {
                        throw e;
                    }
                    logger.debug("An unrelated local appliance resource could not be refreshed before a command");
                }
            }
        }
        Point point = live.points().stream().filter(candidate -> candidate.id().equals(pending.channel())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Resource channel is no longer available"));
        JsonObject fields;
        try {
            fields = live.command(point, pending.command());
        } catch (IllegalArgumentException e) {
            logger.debug("Local appliance command rejected by the current capabilities or Remote Control setting");
            return;
        }
        if (!isActive(current)) {
            return;
        }
        // POST is never retried: a failed acknowledgement may still mean the appliance applied the write.
        transport.post(point.href(), fields);
        if (isActive(current)) {
            current.resources.update(point.href(), transport.get(point.href()));
        }
    }

    private void publish(Session current) {
        synchronized (lifecycle) {
            if (!isActive(current)) {
                return;
            }
            List<Point> points = current.resources.points();
            List<Channel> channels = new ArrayList<>();
            for (Point point : points) {
                String type = switch (point.itemType()) {
                    case "Switch" -> "local-switch";
                    case "Number" -> "local-number";
                    case "Number:Temperature" -> "local-temperature";
                    case "Number:Power" -> "local-power";
                    default -> "local-string";
                };
                if (!point.writable()) {
                    type += "-readonly";
                }
                channels.add(ChannelBuilder.create(new ChannelUID(getThing().getUID(), point.id()), point.itemType())
                        .withType(new ChannelTypeUID(BINDING_ID, type)).withLabel(point.label()).build());
            }
            Map<String, String> properties = new HashMap<>(getThing().getProperties());
            UUID identity = current.identity;
            if (identity != null) {
                properties.put(DEVICE_ID_PROPERTY, identity.toString());
            }
            if (!channels.equals(getThing().getChannels()) || !properties.equals(getThing().getProperties())) {
                updateThing(editThing().withChannels(channels).withProperties(properties).build());
            }
            for (Point point : points) {
                if (!isActive(current)) {
                    return;
                }
                updateState(point.id(), current.resources.state(point));
            }
            if (isActive(current)) {
                updateStatus(ThingStatus.ONLINE);
            }
        }
    }

    private void failure(Session current, ThingStatusDetail detail, String message) {
        synchronized (lifecycle) {
            if (isActive(current)) {
                updateStatus(ThingStatus.OFFLINE, detail, message);
            }
        }
    }

    private void schedulePoll(Session current) {
        synchronized (lifecycle) {
            if (isActive(current)) {
                if (current.poll != null) {
                    current.poll.cancel(false);
                }
                current.poll = executor.schedule(() -> {
                    synchronized (lifecycle) {
                        if (isActive(current)) {
                            current.refresh = true;
                            startWorker(current);
                        }
                    }
                }, current.configuration.refreshInterval, TimeUnit.SECONDS);
            }
        }
    }

    private boolean isActive(Session current) {
        return current.equals(session);
    }

    private static void closeTransport(Session current) {
        LocalTransport transport = current.transport;
        if (transport != null) {
            transport.close();
        }
    }
}
