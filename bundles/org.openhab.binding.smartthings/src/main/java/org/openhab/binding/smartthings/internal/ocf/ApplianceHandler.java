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
package org.openhab.binding.smartthings.internal.ocf;

import static org.openhab.binding.smartthings.internal.SmartThingsBindingConstants.BINDING_ID;
import static org.openhab.binding.smartthings.internal.SmartThingsBindingConstants.THING_TYPE_APPLIANCE;

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
import java.util.function.LongSupplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.ApplianceFamily;
import org.openhab.binding.smartthings.internal.ocf.Resources.Point;
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
 * Event-driven state updates and capability-checked control of an authenticated Samsung OCF appliance.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public class ApplianceHandler extends BaseThingHandler {
    private static final String IDENTITY = "/oic/d";
    private static final String COLLECTION = "/device/0";
    private static final String DEVICE_ID_PROPERTY = "deviceId";
    private static final String SERVER_FINGERPRINT_PROPERTY = "serverFingerprint";
    private static final int MAX_PENDING_COMMANDS = 16;
    private static final int MAX_PENDING_NOTIFICATIONS = 128;
    private static final int MAX_OBSERVATION_RETRY_SECONDS = 300;
    private final Logger logger = LoggerFactory.getLogger(ApplianceHandler.class);
    private final Object lifecycle = new Object();
    private final ReentrantLock io = new ReentrantLock();
    private final TransportFactory transportFactory;
    private final ApplianceDescriptionProvider descriptionProvider;
    private final ScheduledExecutorService executor;
    private final LongSupplier nanoTime;
    private final List<Channel> declaredChannels;
    private final Set<String> declaredChannelIds;
    private volatile @Nullable Session session;
    private long generation;

    @FunctionalInterface
    interface TransportFactory {
        Transport create(ApplianceConfiguration configuration) throws IOException;
    }

    private record PendingCommand(String channel, Command command) {
    }

    private record Notification(Observation observation, JsonElement payload) {
    }

    private final class Observation implements Transport.Listener {
        final Session owner;
        final String href;
        Transport.@Nullable Subscription subscription;
        final long started;
        boolean established;
        boolean failed;
        int failures;
        long retryAt;

        Observation(Session owner, String href, int failures) {
            this.owner = owner;
            this.href = href;
            this.failures = failures;
            started = nanoTime.getAsLong();
        }

        @Override
        public void onUpdate(JsonElement representation) {
            synchronized (lifecycle) {
                if (!isActive(owner) || !equals(owner.observations.get(href)) || failed) {
                    return;
                }
                if (owner.notifications.size() >= MAX_PENDING_NOTIFICATIONS) {
                    logger.debug("Appliance {} notification queue is full for {}; switching to fallback polling",
                            getThing().getUID(), href);
                    onFailure();
                    return;
                }
                owner.notifications.addLast(new Notification(this, representation));
                startWorker(owner);
            }
        }

        @Override
        public void onFailure() {
            synchronized (lifecycle) {
                if (isActive(owner) && equals(owner.observations.get(href)) && !failed) {
                    failed = true;
                    failures = Math.min(failures + 1, 6);
                    long delay = Math.min((long) owner.configuration.refreshInterval << (failures - 1),
                            Math.max(owner.configuration.refreshInterval, MAX_OBSERVATION_RETRY_SECONDS));
                    retryAt = nanoTime.getAsLong() + TimeUnit.SECONDS.toNanos(delay);
                    logger.debug("Appliance {} observation failed for {}; retry in {} seconds (failure {})",
                            getThing().getUID(), href, delay, failures);
                    owner.notifications.removeIf(notification -> notification.observation().equals(this));
                    schedulePoll(owner);
                }
            }
        }
    }

    private static final class Session {
        final ApplianceConfiguration configuration;
        final Resources resources = new Resources();
        final ArrayDeque<PendingCommand> commands = new ArrayDeque<>();
        final ArrayDeque<Notification> notifications = new ArrayDeque<>();
        final Map<String, Observation> observations = new HashMap<>();
        @Nullable
        UUID identity;
        @Nullable
        String certificateFingerprint;
        volatile @Nullable Transport transport;
        @Nullable
        ScheduledFuture<?> poll;
        @Nullable
        ScheduledFuture<?> worker;
        boolean running;
        boolean refresh = true;
        boolean fallback;

        Session(ApplianceConfiguration configuration, @Nullable UUID identity) {
            this.configuration = configuration;
            this.identity = identity;
        }
    }

    private static final class IdentityMismatchException extends IOException {
        private static final long serialVersionUID = 1L;
    }

    public ApplianceHandler(Thing thing, ApplianceDescriptionProvider descriptionProvider) {
        super(thing);
        this.transportFactory = CoapTransport::new;
        this.descriptionProvider = descriptionProvider;
        this.executor = scheduler;
        this.nanoTime = System::nanoTime;
        this.declaredChannels = List.copyOf(thing.getChannels());
        this.declaredChannelIds = this.declaredChannels.stream().map(channel -> channel.getUID().getId())
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    ApplianceHandler(Thing thing, TransportFactory transportFactory, ScheduledExecutorService executor,
            ApplianceDescriptionProvider descriptionProvider, LongSupplier nanoTime) {
        super(thing);
        this.transportFactory = transportFactory;
        this.descriptionProvider = descriptionProvider;
        this.executor = executor;
        this.nanoTime = nanoTime;
        this.declaredChannels = List.copyOf(thing.getChannels());
        this.declaredChannelIds = this.declaredChannels.stream().map(channel -> channel.getUID().getId())
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    @Override
    public void initialize() {
        long activation = retireSession();
        ApplianceConfiguration configuration = getConfigAs(ApplianceConfiguration.class);
        try {
            if (configuration.serverFingerprint.isBlank() && configuration.ownerPsk.isBlank()) {
                configuration.serverFingerprint = getThing().getProperties().getOrDefault(SERVER_FINGERPRINT_PROPERTY,
                        "");
            }
            configuration.validate();
            String expected = configuration.deviceId.isBlank() ? getThing().getProperties()
                    .getOrDefault(DEVICE_ID_PROPERTY, getThing().getProperties().getOrDefault("deviceid", ""))
                    : configuration.deviceId;
            UUID identity = expected.isBlank() ? null : ApplianceConfiguration.uuid(expected);
            synchronized (lifecycle) {
                if (generation != activation) {
                    return;
                }
                Session current = new Session(configuration, identity);
                session = current;
                logger.debug(
                        "Initializing appliance {} at {}:{} with timeout {} seconds and fallback interval {} seconds",
                        getThing().getUID(), configuration.host, configuration.port, configuration.timeout,
                        configuration.refreshInterval);
                updateStatus(ThingStatus.UNKNOWN);
                startWorker(current);
            }
        } catch (IllegalArgumentException e) {
            synchronized (lifecycle) {
                if (generation == activation) {
                    logger.debug("Appliance {} initialization rejected: invalid connection or credential configuration",
                            getThing().getUID());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Check connection settings and any imported credentials");
                }
            }
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing appliance handler {}", getThing().getUID());
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
            descriptionProvider.retainDescriptions(getThing().getUID(), Set.of());
            if (previous != null) {
                previous.commands.clear();
                previous.notifications.clear();
                previous.observations.clear();
                ScheduledFuture<?> poll = previous.poll;
                if (poll != null) {
                    poll.cancel(false);
                }
                ScheduledFuture<?> worker = previous.worker;
                if (worker != null) {
                    worker.cancel(true);
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
    public void channelLinked(ChannelUID channelUID) {
        super.channelLinked(channelUID);
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        handleCommand(channelUID, RefreshType.REFRESH);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        synchronized (lifecycle) {
            Session current = session;
            if (current == null || !channelUID.getThingUID().equals(getThing().getUID())) {
                return;
            }
            if (command == RefreshType.REFRESH) {
                logger.debug("Appliance {} full refresh requested through channel {}", getThing().getUID(),
                        channelUID.getId());
                current.refresh = true;
            } else if (current.commands.size() < MAX_PENDING_COMMANDS) {
                current.commands.addLast(new PendingCommand(channelUID.getId(), command));
                logger.debug("Appliance {} command queued for channel {}; {} commands pending", getThing().getUID(),
                        channelUID.getId(), current.commands.size());
            } else {
                logger.debug("Appliance {} command queue is full; command for channel {} discarded",
                        getThing().getUID(), channelUID.getId());
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
            Notification notification;
            boolean refresh;
            boolean fallback;
            synchronized (lifecycle) {
                if (!isActive(current)) {
                    return;
                }
                fallback = current.fallback && !current.refresh;
                refresh = current.refresh || current.fallback;
                current.refresh = false;
                current.fallback = false;
                pending = refresh || current.commands.isEmpty() ? null : current.commands.removeFirst();
                notification = refresh || pending != null || current.notifications.isEmpty() ? null
                        : current.notifications.removeFirst();
                if (!refresh && pending == null && notification == null) {
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
                    Transport transport = current.transport;
                    if (transport == null) {
                        logger.debug("Appliance {} opening transport to {}:{} (port 0 enables secure-port discovery)",
                                getThing().getUID(), current.configuration.host, current.configuration.port);
                        transport = transportFactory.create(current.configuration);
                        current.transport = transport;
                        if (!isActive(current)) {
                            closeTransport(current);
                            return;
                        }
                    }
                    if (refresh) {
                        if (!fallback || !pollFallback(current, transport)) {
                            readResources(current, transport, current.resources);
                        }
                        subscribe(current, transport);
                    } else if (pending != null) {
                        sendCommand(current, transport, pending);
                    } else if (notification != null && !applyNotification(current, notification)) {
                        continue;
                    }
                } finally {
                    io.unlock();
                }
                publish(current);
            } catch (IdentityMismatchException e) {
                failure(current, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Authenticated appliance identity or device type does not match this Thing");
            } catch (IOException e) {
                logger.debug("Appliance {} communication failed ({})", getThing().getUID(),
                        e.getClass().getSimpleName());
                failure(current, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Appliance communication failed; check network and credentials");
            } catch (IllegalArgumentException e) {
                logger.debug("Appliance {} command or resource representation was rejected", getThing().getUID());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                synchronized (lifecycle) {
                    current.running = false;
                    current.worker = null;
                }
                return;
            } catch (RuntimeException e) {
                logger.warn("Unexpected failure processing an appliance operation");
                failure(current, ThingStatusDetail.COMMUNICATION_ERROR, "Unexpected appliance processing failure");
            } finally {
                if (refresh) {
                    schedulePoll(current);
                }
            }
        }
    }

    private void subscribe(Session current, Transport transport) {
        Map<String, JsonObject> resources = current.resources.snapshot();
        Set<String> linkedHrefs = new HashSet<>();
        for (Point point : points(current.resources)) {
            if (isLinked(new ChannelUID(getThing().getUID(), point.id()))) {
                linkedHrefs.add(point.href());
            }
        }
        List<Transport.Subscription> obsoleteSubscriptions = new ArrayList<>();
        synchronized (lifecycle) {
            if (!isActive(current)) {
                return;
            }
            current.observations.entrySet().removeIf(entry -> {
                if (linkedHrefs.contains(entry.getKey())) {
                    return false;
                }
                Transport.Subscription subscription = entry.getValue().subscription;
                if (subscription != null) {
                    obsoleteSubscriptions.add(subscription);
                }
                return true;
            });
        }
        obsoleteSubscriptions.forEach(Transport.Subscription::close);
        for (Map.Entry<String, JsonObject> entry : resources.entrySet()) {
            String href = entry.getKey();
            if (!linkedHrefs.contains(href) || IDENTITY.equals(href) || COLLECTION.equals(href)
                    || isSecurityResource(href) || isStub(entry.getValue())) {
                continue;
            }
            Observation observation;
            Observation previous;
            synchronized (lifecycle) {
                if (!isActive(current)) {
                    return;
                }
                previous = current.observations.get(href);
                if (previous != null && (!previous.failed || nanoTime.getAsLong() - previous.retryAt < 0)) {
                    continue;
                }
                observation = new Observation(current, href, previous == null ? 0 : previous.failures);
                current.observations.put(href, observation);
            }
            Transport.Subscription previousSubscription = previous == null ? null : previous.subscription;
            if (previousSubscription != null) {
                previousSubscription.close();
            }
            try {
                logger.debug("Appliance {} subscribing to resource {}", getThing().getUID(), href);
                Transport.Subscription subscription = transport.observe(href, observation);
                boolean retained;
                synchronized (lifecycle) {
                    retained = isActive(current) && observation.equals(current.observations.get(href));
                    if (retained) {
                        observation.subscription = subscription;
                    }
                }
                if (!retained) {
                    subscription.close();
                    return;
                }
            } catch (IOException e) {
                observation.onFailure();
                logger.debug("Appliance {} observation unavailable for {}; retaining fallback polling ({})",
                        getThing().getUID(), href, e.getClass().getSimpleName());
            }
        }
    }

    private boolean applyNotification(Session current, Notification notification) {
        Observation observation = notification.observation();
        synchronized (lifecycle) {
            if (!isActive(current) || !observation.equals(current.observations.get(observation.href))
                    || observation.failed) {
                return false;
            }
            try {
                current.resources.update(observation.href, notification.payload());
                // A successful initial response alone does not prove that push delivery has recovered.
                if (observation.established) {
                    observation.failures = 0;
                }
                if (!observation.established) {
                    logger.debug("Appliance {} observation established for {}", getThing().getUID(), observation.href);
                }
                if (logger.isTraceEnabled()) {
                    logger.trace("Appliance {} applying notification for {}: {}", getThing().getUID(), observation.href,
                            ApplianceDiagnostics.sanitize(notification.payload()));
                }
                observation.established = true;
                schedulePoll(current);
                return true;
            } catch (IOException | IllegalArgumentException e) {
                observation.onFailure();
                logger.debug("Appliance {} malformed notification for {}; retaining fallback polling",
                        getThing().getUID(), observation.href);
                return false;
            }
        }
    }

    private boolean pollFallback(Session current, Transport transport) throws IOException {
        List<Observation> affected;
        synchronized (lifecycle) {
            if (current.observations.isEmpty()) {
                return false;
            }
            // Drain initial responses before deciding which registrations have timed out.
            while (!current.notifications.isEmpty()) {
                applyNotification(current, current.notifications.removeFirst());
            }
            affected = current.observations.values().stream()
                    .filter(observation -> !observation.established || observation.failed).toList();
            for (Observation observation : affected) {
                if (!observation.failed && nanoTime.getAsLong() - observation.started >= TimeUnit.SECONDS
                        .toNanos(current.configuration.timeout)) {
                    observation.onFailure();
                }
            }
        }
        logger.debug("Appliance {} fallback polling {} resources", getThing().getUID(), affected.size());
        boolean readFailed = false;
        boolean received = false;
        for (Observation observation : affected) {
            if (!isActive(current)) {
                return true;
            }
            Transport.Subscription subscription;
            synchronized (lifecycle) {
                subscription = observation.failed ? observation.subscription : null;
                if (subscription != null) {
                    observation.subscription = null;
                }
            }
            if (subscription != null) {
                subscription.close();
            }
            try {
                mergeReadResponse(current, current.resources, observation.href, transport.get(observation.href));
                received = true;
            } catch (IOException e) {
                readFailed = true;
                logger.debug("Appliance {} fallback read unavailable for {} ({})", getThing().getUID(),
                        observation.href, e.getClass().getSimpleName());
            }
        }
        if (!received && readFailed) {
            // An optional resource may be unavailable while the authenticated connection is still healthy.
            verifyIdentity(current, transport, transport.get(IDENTITY));
        }
        return true;
    }

    private void mergeReadResponse(Session current, Resources resources, String href, JsonElement payload)
            throws IOException {
        synchronized (lifecycle) {
            // Merge queued partial events first so they cannot subsequently overwrite a newer readback.
            while (!current.notifications.isEmpty()) {
                applyNotification(current, current.notifications.removeFirst());
            }
            resources.update(href, payload);
            if (logger.isTraceEnabled()) {
                logger.trace("Appliance {} resource {}: {}", getThing().getUID(), href,
                        ApplianceDiagnostics.sanitize(payload));
            }
        }
    }

    private void readResources(Session current, Transport transport, Resources resources) throws IOException {
        logger.debug("Appliance {} reading authenticated identity and resource collection", getThing().getUID());
        JsonElement identity = transport.get(IDENTITY);
        verifyIdentity(current, transport, identity);
        mergeReadResponse(current, resources, IDENTITY, identity);
        JsonElement payload = transport.get(COLLECTION);
        Resources received = new Resources();
        received.update(COLLECTION, payload);
        mergeReadResponse(current, resources, COLLECTION, payload);
        if (!hasBatchRepresentation(payload)) {
            try {
                JsonElement batch = transport.get(COLLECTION + "?if=oic.if.b");
                received.update(COLLECTION, batch);
                mergeReadResponse(current, resources, COLLECTION, batch);
            } catch (IOException e) {
                // Some appliances expose only collection links, which can still be hydrated individually.
                logger.debug("Appliance {} batch interface unavailable; reading linked resources instead",
                        getThing().getUID());
            }
        }
        Set<String> visited = new HashSet<>();
        while (true) {
            // Links must be hydrated on every poll, even when the retained cache already has a value.
            List<String> stubs = received.snapshot().entrySet().stream()
                    .filter(entry -> isStub(entry.getValue()) && !isSecurityResource(entry.getKey()))
                    .map(Map.Entry::getKey).filter(path -> !visited.contains(path)).toList();
            if (stubs.isEmpty()) {
                logger.debug("Appliance {} resource refresh completed; {} linked resources visited",
                        getThing().getUID(), visited.size());
                return;
            }
            for (String href : stubs) {
                visited.add(href);
                try {
                    JsonElement representation = transport.get(href);
                    received.update(href, representation);
                    mergeReadResponse(current, resources, href, representation);
                } catch (IOException e) {
                    // Optional enrichment must not invalidate a successful baseline batch read.
                    logger.debug("Appliance {} linked resource {} could not be read ({})", getThing().getUID(), href,
                            e.getClass().getSimpleName());
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

    private void verifyIdentity(Session current, Transport transport, JsonElement payload) throws IOException {
        if (!payload.isJsonObject()) {
            throw new IOException("Missing authenticated appliance identity");
        }
        JsonObject identityResource = payload.getAsJsonObject();
        ApplianceFamily expectedFamily = ApplianceFamily.forThingType(getThing().getThingTypeUID());
        if (expectedFamily != null) {
            JsonElement deviceTypes = identityResource.get("rt");
            if (deviceTypes == null || !deviceTypes.isJsonArray()
                    || deviceTypes.getAsJsonArray().asList().stream()
                            .noneMatch(type -> expectedFamily.deviceType()
                                    .equals(type.isJsonPrimitive() && type.getAsJsonPrimitive().isString()
                                            ? type.getAsString()
                                            : null))) {
                throw new IdentityMismatchException();
            }
        }
        JsonElement deviceId = identityResource.get("di");
        if (deviceId == null || !deviceId.isJsonPrimitive() || !deviceId.getAsJsonPrimitive().isString()) {
            throw new IOException("Missing authenticated appliance identity");
        }
        UUID identity;
        try {
            identity = ApplianceConfiguration.uuid(deviceId.getAsString());
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid authenticated appliance identity");
        }
        UUID expectedIdentity = current.identity;
        if (expectedIdentity != null && !expectedIdentity.equals(identity)) {
            throw new IdentityMismatchException();
        }
        current.identity = identity;
        current.certificateFingerprint = transport.serverFingerprint();
    }

    private void sendCommand(Session current, Transport transport, PendingCommand pending) throws IOException {
        Point previous = points(current.resources).stream().filter(point -> point.id().equals(pending.channel()))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Unknown resource channel"));
        if (!previous.writable()) {
            throw new IllegalArgumentException("Resource channel is read-only");
        }
        // A separate live model fails closed if capabilities or Remote Control disappear from a fresh response.
        Resources live = new Resources();
        readResources(current, transport, live);
        for (String href : current.resources.snapshot().keySet()) {
            if (!IDENTITY.equals(href) && !COLLECTION.equals(href) && !isSecurityResource(href)) {
                try {
                    JsonElement payload = transport.get(href);
                    live.update(href, payload);
                    mergeReadResponse(current, current.resources, href, payload);
                } catch (IOException e) {
                    if (href.equals(previous.href()) || href.endsWith("/remotectrl/0")
                            || href.endsWith("/remotectrl/vs/0") || previous.itemType().equals("Number:Temperature")
                                    && href.endsWith("/temperature/control/vs/0")) {
                        throw e;
                    }
                    logger.debug(
                            "Appliance {} unrelated resource {} could not be refreshed before command for channel {}",
                            getThing().getUID(), href, pending.channel());
                }
            }
        }
        Point point = points(live).stream().filter(candidate -> candidate.id().equals(pending.channel())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Resource channel is no longer available"));
        JsonObject fields;
        try {
            fields = live.command(point, pending.command());
        } catch (IllegalArgumentException e) {
            logger.debug(
                    "Appliance {} command for channel {} rejected by current capabilities or Remote Control setting",
                    getThing().getUID(), pending.channel());
            return;
        }
        if (!isActive(current)) {
            return;
        }
        // POST is never retried: a failed acknowledgement may still mean the appliance applied the write.
        logger.debug("Appliance {} sending command for channel {} to resource {}", getThing().getUID(),
                pending.channel(), point.href());
        transport.post(point.href(), fields);
        if (isActive(current)) {
            logger.debug("Appliance {} command acknowledged for channel {}; reading back resource {}",
                    getThing().getUID(), pending.channel(), point.href());
            mergeReadResponse(current, current.resources, point.href(), transport.get(point.href()));
        }
    }

    private static Set<String> defaultTags(Point point) {
        if ("Power".equals(point.label())) {
            return point.writable() ? Set.of("Control", "Power") : Set.of("Measurement", "Power");
        }
        if ("Current Temperature".equals(point.label())) {
            return Set.of("Measurement", "Temperature");
        }
        if ("Target Temperature".equals(point.label())) {
            return Set.of("Control", "Setpoint", "Temperature");
        }
        if ("Power Consumption".equals(point.label())) {
            return Set.of("Measurement", "Power");
        }
        if ("Cumulative Energy".equals(point.label())) {
            return Set.of("Measurement", "Energy");
        }
        if (point.writable()) {
            return Set.of("Control");
        }
        if ("Remote Control".equals(point.label()) || point.label().endsWith("State")) {
            return Set.of("Status");
        }
        return Set.of();
    }

    private List<Point> points(Resources resources) {
        return points(resources, false);
    }

    private List<Point> points(Resources resources, boolean logIgnoredChannels) {
        return THING_TYPE_APPLIANCE.equals(getThing().getThingTypeUID()) ? resources.points(logIgnoredChannels)
                : resources.points(declaredChannelIds);
    }

    private void publish(Session current) {
        synchronized (lifecycle) {
            if (!isActive(current)) {
                return;
            }
            boolean genericAppliance = THING_TYPE_APPLIANCE.equals(getThing().getThingTypeUID());
            List<Point> points = points(current.resources, genericAppliance);
            if (logger.isTraceEnabled()) {
                List<String> capabilities = points.stream().map(point -> point.id() + "=" + point.href() + "#"
                        + point.field() + " (" + point.itemType() + ", writable=" + point.writable() + ")").toList();
                logger.trace("Appliance {} recognized channels: {}", getThing().getUID(), capabilities);
            }
            List<Channel> channels;
            if (genericAppliance) {
                channels = new ArrayList<>();
                for (Point point : points) {
                    String type = switch (point.itemType()) {
                        case "Switch" -> "switch";
                        case "Number" -> "number";
                        case "Number:Temperature" -> "temperature";
                        case "Number:Energy" -> "energy";
                        case "Number:Time" -> "time";
                        case "Number:Dimensionless" -> "percentage";
                        default -> "string";
                    };
                    ChannelTypeUID typeUID = switch (point.itemType()) {
                        case "Switch" ->
                            "Power".equals(point.label()) && point.writable() ? new ChannelTypeUID("system:power")
                                    : new ChannelTypeUID(BINDING_ID, point.advanced() ? type + "-readonly-advanced"
                                            : point.writable() ? type : type + "-readonly");
                        case "Number:Power" -> new ChannelTypeUID("system:electric-power");
                        case "Number:Temperature" -> "Current Temperature".equals(point.label())
                                ? new ChannelTypeUID("system:indoor-temperature")
                                : new ChannelTypeUID(BINDING_ID, point.writable() ? type : type + "-readonly");
                        case "Number:Energy", "Number:Dimensionless" ->
                            new ChannelTypeUID(BINDING_ID, type + "-readonly");
                        case "Number:Time" ->
                            new ChannelTypeUID(BINDING_ID, point.writable() ? type : type + "-readonly");
                        default -> new ChannelTypeUID(BINDING_ID, point.advanced() ? type + "-readonly-advanced"
                                : point.writable() ? type : type + "-readonly");
                    };
                    channels.add(
                            ChannelBuilder.create(new ChannelUID(getThing().getUID(), point.id()), point.itemType())
                                    .withType(typeUID).withLabel(point.label()).withDescription(point.description())
                                    .withDefaultTags(defaultTags(point)).build());
                }
            } else {
                Set<String> supportedIds = points.stream().map(Point::id).collect(java.util.stream.Collectors.toSet());
                channels = declaredChannels.stream().filter(channel -> supportedIds.contains(channel.getUID().getId()))
                        .toList();
                points = points.stream().filter(
                        point -> channels.stream().anyMatch(channel -> channel.getUID().getId().equals(point.id())))
                        .toList();
            }
            Map<String, String> properties = new HashMap<>(getThing().getProperties());
            UUID identity = current.identity;
            if (identity != null) {
                properties.put(DEVICE_ID_PROPERTY, identity.toString());
                properties.remove("deviceid");
            }
            String certificateFingerprint = current.certificateFingerprint;
            if (certificateFingerprint != null) {
                properties.put(SERVER_FINGERPRINT_PROPERTY, certificateFingerprint);
            }
            if (!channels.equals(getThing().getChannels()) || !properties.equals(getThing().getProperties())) {
                logger.debug("Appliance {} updating Thing capabilities: {} channels", getThing().getUID(),
                        channels.size());
                updateThing(editThing().withChannels(channels).withProperties(properties).build());
            }
            Set<ChannelUID> channelUIDs = new HashSet<>();
            for (Point point : points) {
                if (!isActive(current)) {
                    return;
                }
                ChannelUID channelUID = new ChannelUID(getThing().getUID(), point.id());
                channelUIDs.add(channelUID);
                descriptionProvider.setDescription(channelUID, current.resources.stateDescription(point),
                        current.resources.commandOptions(point));
                if (!isActive(current)) {
                    return;
                }
                updateState(point.id(), current.resources.state(point));
            }
            if (isActive(current)) {
                descriptionProvider.retainDescriptions(getThing().getUID(), channelUIDs);
                if (getThing().getStatus() != ThingStatus.ONLINE) {
                    logger.debug("Appliance {} connection established; publishing {} channels", getThing().getUID(),
                            points.size());
                }
                updateStatus(ThingStatus.ONLINE);
            }
        }
    }

    private void failure(Session current, ThingStatusDetail detail, String message) {
        Transport transport;
        synchronized (lifecycle) {
            if (!isActive(current)) {
                return;
            }
            transport = current.transport;
            current.transport = null;
            current.observations.clear();
            current.notifications.clear();
            schedulePoll(current);
            logger.debug("Appliance {} going OFFLINE ({}): {}", getThing().getUID(), detail, message);
            updateStatus(ThingStatus.OFFLINE, detail, message);
        }
        if (transport != null) {
            transport.close();
        }
    }

    private void schedulePoll(Session current) {
        synchronized (lifecycle) {
            if (isActive(current)) {
                boolean observed = !current.observations.isEmpty() && current.observations.values().stream()
                        .allMatch(observation -> observation.established && !observation.failed);
                ScheduledFuture<?> poll = current.poll;
                if (observed) {
                    if (poll != null) {
                        logger.debug("Appliance {} all observations established; stopping fallback polling",
                                getThing().getUID());
                        poll.cancel(false);
                        current.poll = null;
                    }
                    return;
                }
                if (poll != null && !poll.isDone()) {
                    return;
                }
                logger.debug("Appliance {} scheduling fallback poll in {} seconds", getThing().getUID(),
                        current.configuration.refreshInterval);
                current.poll = executor.schedule(() -> {
                    synchronized (lifecycle) {
                        if (isActive(current)) {
                            current.poll = null;
                            current.fallback = true;
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
        Transport transport = current.transport;
        if (transport != null) {
            transport.close();
        }
    }
}
