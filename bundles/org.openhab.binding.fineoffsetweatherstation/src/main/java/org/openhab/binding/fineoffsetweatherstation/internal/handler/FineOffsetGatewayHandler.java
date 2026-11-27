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
package org.openhab.binding.fineoffsetweatherstation.internal.handler;

import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.THING_TYPE_GATEWAY;
import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.THING_TYPE_SENSOR;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetGatewayConfiguration;
import org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetSensorConfiguration;
import org.openhab.binding.fineoffsetweatherstation.internal.discovery.FineOffsetGatewayDiscoveryService;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.Sensor;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.SensorGatewayBinding;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.MeasuredValue;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.SensorDevice;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.SystemInfo;
import org.openhab.binding.fineoffsetweatherstation.internal.service.GatewayQueryService;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FineOffsetGatewayHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 * <p>
 * All gateway communication and all changes to the bridge and sensor Things happen on a single refresh job, so
 * the Things are never edited concurrently and requests to the gateway are not interleaved.
 *
 * @author Andreas Berger - Initial contribution
 * @author Christopher Smith - Non-blocking initialization, single refresh job, failure handling
 */
@NonNullByDefault
public class FineOffsetGatewayHandler extends BaseBridgeHandler {

    /**
     * Number of consecutive polls a value may be missing before its gateway channel is removed. Gateway channels
     * aggregate the values of all sensors, so a sensor that is unreachable for a while (e.g. an empty battery) must
     * not lose its channels as quickly as a sensor Thing does.
     */
    static final int GATEWAY_MISSING_VALUE_REMOVAL_THRESHOLD = 60;

    /** How long to wait before reading the gateway information or the paired sensors again after a failure. */
    private static final long RETRY_INTERVAL_NANOS = TimeUnit.MINUTES.toNanos(1);
    /**
     * Gateway information still missing after this many reads is retried only once per discover interval: a request
     * that failed on a flaky link cannot be told apart from one the gateway does not support.
     */
    private static final int FAST_BRIDGE_INFO_ATTEMPTS = 3;
    /** Gateway information still missing after this many reads is taken as not supported by the gateway. */
    private static final int MAX_BRIDGE_INFO_ATTEMPTS = 12;
    private static final String NO_VALID_RESPONSE = "@text/offline.comm-error.no-valid-response";

    private static final String PROPERTY_FREQUENCY = "frequency";
    // {0} = the full ChannelUID of the equivalent channel on the sensor Thing
    private static final String DEFAULT_DEPRECATION_NOTE_WITH_TARGET = "(deprecated — this value is now also available on the sensor Thing as channel: {0})";
    private static final String DEFAULT_DEPRECATION_NOTE = "(deprecated — this value is now also available on the sensor Thing)";

    private final Logger logger = LoggerFactory.getLogger(FineOffsetGatewayHandler.class);
    private final Bundle bundle;

    private volatile @Nullable GatewayQueryService gatewayQueryService;

    private final FineOffsetGatewayDiscoveryService gatewayDiscoveryService;
    private final ChannelTypeRegistry channelTypeRegistry;
    private final TranslationProvider translationProvider;
    private final LocaleProvider localeProvider;

    private final ThingUID bridgeUID;

    private volatile @Nullable Map<SensorGatewayBinding, SensorDevice> sensorDeviceMap;
    private final DynamicChannelReconciler reconciler = new DynamicChannelReconciler(Set.of(),
            GATEWAY_MISSING_VALUE_REMOVAL_THRESHOLD);
    private @Nullable ScheduledFuture<?> refreshJob;
    private volatile boolean disposed;
    /**
     * Counts the calls of initialize() and dispose(). openHAB applies a configuration change by calling both on the
     * same handler, so a refresh run still blocked in a request of the previous session must recognize that it is
     * outdated instead of continuing with the new session's connection and state.
     */
    private final AtomicLong session = new AtomicLong();
    /** Serializes refresh runs, so that an outdated run has ended before a run of the new session starts. */
    private final ReentrantLock refreshLock = new ReentrantLock();
    /** The session of the refresh run in progress; guarded by {@link #refreshLock}. */
    private long runSession;
    /** Set when a sensor Thing initialized, so the refresh job applies the last known sensor state to it. */
    private volatile boolean sensorStatePending;
    private volatile @Nullable CommunicationError lastError;

    // only accessed by the refresh job (and initialize() before it is scheduled)
    private boolean firmwarePending = true;
    /** Whether the gateway ever reported its firmware version, so it is worth reading again. */
    private boolean firmwareRead;
    private boolean systemInfoPending = true;
    private long nextBridgeInfoAttempt;
    private int bridgeInfoAttempts;
    private int failedPolls;
    private int failedPollsUntilOffline = new FineOffsetGatewayConfiguration().failedPollsUntilOffline;
    private long discoverIntervalNanos;
    private @Nullable Long lastSensorFetch;
    private long nextSensorFetchAttempt;

    public FineOffsetGatewayHandler(Bridge bridge, FineOffsetGatewayDiscoveryService gatewayDiscoveryService,
            ChannelTypeRegistry channelTypeRegistry, TranslationProvider translationProvider,
            LocaleProvider localeProvider) {
        super(bridge);
        this.bridgeUID = bridge.getUID();
        this.gatewayDiscoveryService = gatewayDiscoveryService;
        this.channelTypeRegistry = channelTypeRegistry;
        this.translationProvider = translationProvider;
        this.localeProvider = localeProvider;
        this.bundle = FrameworkUtil.getBundle(FineOffsetGatewayDiscoveryService.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        FineOffsetGatewayConfiguration config = getConfigAs(FineOffsetGatewayConfiguration.class);
        String ip = config.ip;
        if (ip == null || ip.isBlank()) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "@text/offline.conf-error.no-ip");
            return;
        }
        if (config.pollingInterval < 1 || config.discoverInterval < 1 || config.timeout < 1
                || config.failedPollsUntilOffline < 1) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "@text/offline.conf-error.invalid-interval");
            return;
        }

        disposed = false;
        gatewayQueryService = config.protocol.getGatewayQueryService(config, this::onQueryStatus);
        updateStatus(ThingStatus.UNKNOWN);
        reconciler.reset();
        sensorDeviceMap = null;
        sensorStatePending = false;
        firmwarePending = true;
        firmwareRead = false;
        systemInfoPending = true;
        nextBridgeInfoAttempt = System.nanoTime();
        bridgeInfoAttempts = 0;
        lastSensorFetch = null;
        nextSensorFetchAttempt = System.nanoTime();
        lastError = null;
        failedPolls = 0;
        failedPollsUntilOffline = config.failedPollsUntilOffline;
        discoverIntervalNanos = TimeUnit.SECONDS.toNanos(config.discoverInterval);
        long currentSession = session.incrementAndGet();
        refreshJob = scheduler.scheduleWithFixedDelay(() -> refresh(currentSession), 0, config.pollingInterval,
                TimeUnit.SECONDS);
    }

    /**
     * Remembers the last communication error of the query service. The Thing status is not changed here but by the
     * outcome of each poll, so that a single slow or lost response does not take the gateway and its sensors OFFLINE.
     */
    private void onQueryStatus(ThingStatus status, ThingStatusDetail detail, @Nullable String description) {
        if (status != ThingStatus.ONLINE) {
            lastError = new CommunicationError(detail, description);
        }
    }

    private record CommunicationError(ThingStatusDetail detail, @Nullable String description) {
    }

    /**
     * Polls the live data first, so that only its outcome decides the status and an unreachable gateway costs a single
     * request per poll. The gateway information and the paired sensors are only read while the gateway answers, and
     * a failed read is retried at most every {@link #RETRY_INTERVAL_NANOS}.
     */
    private void refresh(long refreshSession) {
        refreshLock.lock();
        try {
            if (refreshSession != session.get()) {
                // scheduled by an earlier session that was disposed while this run waited for the lock
                return;
            }
            runSession = refreshSession;
            if (!updateLiveData() || isOutdated()) {
                return;
            }
            long now = System.nanoTime();
            if (bridgeInfoPending() && now - nextBridgeInfoAttempt >= 0) {
                if (!updateBridgeInfo()) {
                    nextBridgeInfoAttempt = now + (bridgeInfoAttempts < FAST_BRIDGE_INFO_ATTEMPTS ? RETRY_INTERVAL_NANOS
                            : discoverIntervalNanos);
                }
            }
            Long lastFetch = lastSensorFetch;
            if (!isOutdated() && now - nextSensorFetchAttempt >= 0
                    && (lastFetch == null || now - lastFetch >= discoverIntervalNanos)) {
                if (fetchAndUpdateSensors()) {
                    lastSensorFetch = now;
                    if (lastFetch != null && firmwareRead && !isOutdated()) {
                        // a firmware update restarts the gateway, often without a poll failing in between
                        refreshFirmwareVersion();
                    }
                } else {
                    nextSensorFetchAttempt = now + RETRY_INTERVAL_NANOS;
                }
            }
            if (sensorStatePending && !isOutdated()) {
                sensorStatePending = false;
                updateSensors();
            }
        } catch (RuntimeException e) {
            // keep the job alive: an exception escaping a fixed-delay task would silently cancel all further runs
            logger.warn("Unexpected error while refreshing gateway {}", bridgeUID, e);
        } finally {
            refreshLock.unlock();
        }
    }

    /**
     * @return {@code true} if the handler was disposed or re-initialized since the refresh run in progress started, so
     *         the run must not query the gateway or change any Thing any more
     */
    private boolean isOutdated() {
        return disposed || runSession != session.get();
    }

    /**
     * @return {@code true} if the paired sensors could be read
     */
    private boolean fetchAndUpdateSensors() {
        @Nullable
        Map<SensorGatewayBinding, SensorDevice> deviceMap = query(GatewayQueryService::getRegisteredSensors);
        if (deviceMap == null || isOutdated()) {
            // keep the last known pairing: a failed read is no evidence that sensors were unpaired
            return false;
        }
        sensorDeviceMap = deviceMap;
        sensorStatePending = false;
        updateSensors();
        gatewayDiscoveryService.addSensors(bridgeUID, deviceMap.values());
        return true;
    }

    private void updateSensors() {
        ((Bridge) thing).getThings().forEach(this::updateSensorThing);
    }

    private void updateSensorThing(Thing thing) {
        Map<SensorGatewayBinding, SensorDevice> sensorMap = sensorDeviceMap;
        if (sensorMap == null) {
            return;
        }
        SensorGatewayBinding sensor = sensorBinding(thing);
        if (sensor != null && thing.getHandler() instanceof FineOffsetSensorHandler sensorHandler) {
            sensorHandler.updateSensorState(sensorMap.get(sensor));
        }
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        // applied by the refresh job, so the sensor Thing is only ever edited from that one thread
        sensorStatePending = true;
    }

    /**
     * @return {@code true} if the gateway answered with live data
     */
    private boolean updateLiveData() {
        if (isOutdated()) {
            return false;
        }
        // only an error of this poll may describe its failure
        lastError = null;
        Collection<MeasuredValue> data = query(GatewayQueryService::getMeasuredValues);
        if (isOutdated()) {
            return false;
        }
        if (data == null) {
            failedPolls++;
            if (failedPolls < failedPollsUntilOffline) {
                // a slow or flaky link loses single responses; keep the status and the last values until several
                // polls in a row have failed
                logger.debug("poll of gateway {} failed ({} of {} until OFFLINE)", bridgeUID, failedPolls,
                        failedPollsUntilOffline);
                return false;
            }
            CommunicationError error = lastError;
            updateStatus(error == null ? ThingStatusDetail.COMMUNICATION_ERROR : error.detail(),
                    error == null ? NO_VALID_RESPONSE : error.description());
            getThing().getChannels().forEach(c -> updateState(c.getUID(), UnDefType.UNDEF));
            // Mirror the failure onto the sensor Things so their measurement channels do not keep stale state. Do not
            // run the reconciler here: a poll failure must not advance the missing-value removal debounce.
            for (Thing child : ((Bridge) thing).getThings()) {
                if (child.getHandler() instanceof FineOffsetSensorHandler sensorHandler) {
                    sensorHandler.markMeasuredValuesUndefined();
                }
            }
            return false;
        }
        failedPolls = 0;
        updateStatus(ThingStatus.ONLINE);

        // Capture the bridge reference before reconcile may replace thing via updateBridgeThing.
        Bridge bridge = (Bridge) thing;
        Map<SensorGatewayBinding, ThingUID> sensorThings = sensorThingUids(bridge);
        DynamicChannelReconciler.Plan plan = reconciler.reconcile(data, thing.getChannels(),
                MeasuredValue::getChannelId, value -> createChannel(value, sensorThings));
        Map<String, Channel> refreshed = refreshDeprecationNotes(data, thing.getChannels(), sensorThings);
        if (plan.hasChannelChanges() || !refreshed.isEmpty()) {
            List<Channel> channels = new ArrayList<>();
            for (Channel channel : thing.getChannels()) {
                channels.add(refreshed.getOrDefault(channel.getUID().getId(), channel));
            }
            channels.addAll(plan.channelsToAdd);
            channels.removeAll(plan.channelsToRemove);
            updateBridgeThing(bridgeBuilder -> bridgeBuilder.withChannels(channels));
        }
        plan.statesToPost.forEach(this::updateState);
        dispatchToSensors(bridge, data);
        return true;
    }

    /**
     * Routes each value carrying a {@link Sensor} tag to the child sensor Thing that owns its
     * {@code (sensor, channel)}. Every child sensor handler is called once per poll - with an empty collection when it
     * produced nothing - so its missing-measurand debounce advances. Gateway channels are unaffected: they already
     * received all values above.
     *
     * @param bridge the bridge captured before any channel reconcile may replace the {@code thing} reference
     */
    private void dispatchToSensors(Bridge bridge, Collection<MeasuredValue> data) {
        Map<SensorGatewayBinding, List<MeasuredValue>> bySensor = new HashMap<>();
        for (MeasuredValue value : data) {
            SensorGatewayBinding binding = sensorBinding(value);
            if (binding != null) {
                bySensor.computeIfAbsent(binding, b -> new ArrayList<>()).add(value);
            }
        }

        for (Thing child : bridge.getThings()) {
            SensorGatewayBinding binding = sensorBinding(child);
            if (binding != null && child.getHandler() instanceof FineOffsetSensorHandler sensorHandler) {
                sensorHandler.updateMeasuredValues(bySensor.getOrDefault(binding, List.of()));
            }
        }
    }

    /**
     * Rebuilds existing tagged channels whose description is outdated: channels persisted before the deprecation note
     * existed, or created before their sensor Thing was added and thus still carrying the no-target note.
     *
     * @return the rebuilt channels by channel ID
     */
    private Map<String, Channel> refreshDeprecationNotes(Collection<MeasuredValue> data,
            Collection<Channel> currentChannels, Map<SensorGatewayBinding, ThingUID> sensorThings) {
        Map<String, Channel> currentById = new HashMap<>();
        for (Channel channel : currentChannels) {
            currentById.put(channel.getUID().getId(), channel);
        }
        Map<String, Channel> refreshed = new HashMap<>();
        for (MeasuredValue value : data) {
            Channel existing = currentById.get(value.getChannelId());
            if (existing == null || value.getSensor() == null) {
                continue;
            }
            String description = channelDescription(value, sensorThings);
            if (description != null && !description.equals(existing.getDescription())) {
                refreshed.put(value.getChannelId(),
                        ChannelBuilder.create(existing).withDescription(description).build());
            }
        }
        return refreshed;
    }

    private @Nullable Channel createChannel(MeasuredValue measuredValue,
            Map<SensorGatewayBinding, ThingUID> sensorThings) {
        ChannelTypeUID channelTypeId = measuredValue.getChannelTypeUID();
        if (channelTypeId == null) {
            logger.debug("cannot create channel for {}", measuredValue.getDebugName());
            return null;
        }
        ChannelBuilder builder = ChannelBuilder.create(new ChannelUID(thing.getUID(), measuredValue.getChannelId()))
                .withKind(ChannelKind.STATE).withType(channelTypeId);
        String channelKey = THING_TYPE_GATEWAY.getId() + ".dynamic-channel." + measuredValue.getChannelPrefix();
        String label = translationProvider.getText(bundle, channelKey + ".label", measuredValue.getDebugName(),
                localeProvider.getLocale(), measuredValue.getChannelNumber());
        if (label != null) {
            builder.withLabel(label);
        }
        String description = channelDescription(measuredValue, sensorThings);
        if (description != null) {
            builder.withDescription(description);
        }
        @Nullable
        ChannelType type = channelTypeRegistry.getChannelType(channelTypeId);
        if (type != null) {
            builder.withAcceptedItemType(type.getItemType());
        }
        return builder.build();
    }

    /** The translated channel description, followed by the deprecation note for values of a discrete sensor. */
    private @Nullable String channelDescription(MeasuredValue measuredValue,
            Map<SensorGatewayBinding, ThingUID> sensorThings) {
        String channelKey = THING_TYPE_GATEWAY.getId() + ".dynamic-channel." + measuredValue.getChannelPrefix();
        String description = translationProvider.getText(bundle, channelKey + ".description", null,
                localeProvider.getLocale(), measuredValue.getChannelNumber());
        if (measuredValue.getSensor() != null) {
            SensorGatewayBinding binding = sensorBinding(measuredValue);
            ThingUID sensorThingUid = binding == null ? null : sensorThings.get(binding);
            String note;
            if (sensorThingUid != null) {
                String sensorChannelUid = new ChannelUID(sensorThingUid, measuredValue.getChannelPrefix())
                        .getAsString();
                note = translationProvider.getText(bundle, "gateway.dynamic-channel.deprecation-note",
                        DEFAULT_DEPRECATION_NOTE_WITH_TARGET, localeProvider.getLocale(), sensorChannelUid);
            } else {
                note = translationProvider.getText(bundle, "gateway.dynamic-channel.deprecation-note-no-target",
                        DEFAULT_DEPRECATION_NOTE, localeProvider.getLocale());
            }
            if (note != null) {
                description = description == null ? note : description + " " + note;
            }
        }
        return description;
    }

    /** The UIDs of the child sensor Things by the sensor they represent, looked up once per poll. */
    private Map<SensorGatewayBinding, ThingUID> sensorThingUids(Bridge bridge) {
        Map<SensorGatewayBinding, ThingUID> result = new HashMap<>();
        for (Thing child : bridge.getThings()) {
            SensorGatewayBinding binding = sensorBinding(child);
            if (binding != null) {
                result.putIfAbsent(binding, child.getUID());
            }
        }
        return result;
    }

    private static @Nullable SensorGatewayBinding sensorBinding(Thing thing) {
        if (!THING_TYPE_SENSOR.equals(thing.getThingTypeUID())) {
            return null;
        }
        return thing.getConfiguration().as(FineOffsetSensorConfiguration.class).sensor;
    }

    private static @Nullable SensorGatewayBinding sensorBinding(MeasuredValue value) {
        Sensor sensor = value.getSensor();
        return sensor == null ? null : SensorGatewayBinding.forSensorAndChannel(sensor, value.getChannelNumber());
    }

    private boolean bridgeInfoPending() {
        return firmwarePending || systemInfoPending;
    }

    /**
     * Reads the gateway information that is still missing.
     *
     * @return {@code true} if nothing is missing any more
     */
    private boolean updateBridgeInfo() {
        Map<String, String> properties = new HashMap<>(thing.getProperties());
        if (firmwarePending) {
            @Nullable
            String firmware = query(GatewayQueryService::getFirmwareVersion);
            if (firmware != null) {
                firmwarePending = false;
                firmwareRead = true;
                putFirmwareVersion(properties, firmware);
            }
        }

        if (systemInfoPending) {
            SystemInfo systemInfo = query(GatewayQueryService::fetchSystemInfo);
            if (systemInfo != null) {
                systemInfoPending = false;
                if (systemInfo.getFrequency() != null) {
                    properties.put(PROPERTY_FREQUENCY, systemInfo.getFrequency() + " MHz");
                }
            }
        }
        if (!isOutdated() && !thing.getProperties().equals(properties)) {
            updateBridgeThing(bridgeBuilder -> bridgeBuilder.withProperties(properties));
        }
        if (bridgeInfoPending() && ++bridgeInfoAttempts >= MAX_BRIDGE_INFO_ATTEMPTS) {
            // e.g. ELV gateways and older firmware do not answer every request; stop asking
            logger.debug("gateway {} did not provide all information after {} attempts (firmware: {}, system info: {})",
                    bridgeUID, bridgeInfoAttempts, !firmwarePending, !systemInfoPending);
            firmwarePending = false;
            systemInfoPending = false;
        }
        return !bridgeInfoPending();
    }

    /** Updates the model and firmware properties if the gateway now reports a different firmware version. */
    private void refreshFirmwareVersion() {
        @Nullable
        String firmware = query(GatewayQueryService::getFirmwareVersion);
        if (firmware == null || isOutdated()) {
            return;
        }
        Map<String, String> properties = new HashMap<>(thing.getProperties());
        putFirmwareVersion(properties, firmware);
        if (!thing.getProperties().equals(properties)) {
            logger.debug("gateway {} reports a new firmware version: {}", bridgeUID, firmware);
            updateBridgeThing(bridgeBuilder -> bridgeBuilder.withProperties(properties));
        }
    }

    /** @param firmware the firmware as reported by the gateway, e.g. {@code GW1100B_V2.4.6} */
    private static void putFirmwareVersion(Map<String, String> properties, String firmware) {
        var fwString = firmware.split("_?V");
        if (fwString.length > 1) {
            properties.put(Thing.PROPERTY_MODEL_ID, fwString[0]);
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, fwString[1]);
        }
    }

    private void updateBridgeThing(Consumer<BridgeBuilder> customizer) {
        BridgeBuilder bridge = editThing();
        customizer.accept(bridge);
        updateThing(bridge.build());
    }

    private <T> @Nullable T query(Function<GatewayQueryService, @Nullable T> delegate) {
        @Nullable
        GatewayQueryService queryService = this.gatewayQueryService;
        if (queryService == null || isOutdated()) {
            return null;
        }
        return delegate.apply(queryService);
    }

    @Override
    public void dispose() {
        disposed = true;
        session.incrementAndGet();
        // stop the job before closing the connection it uses
        ScheduledFuture<?> job = refreshJob;
        if (job != null) {
            job.cancel(true);
        }
        refreshJob = null;
        @Nullable
        GatewayQueryService queryService = this.gatewayQueryService;
        this.gatewayQueryService = null;
        if (queryService != null) {
            try {
                queryService.close();
            } catch (IOException e) {
                logger.debug("failed to close queryService", e);
            }
        }
        this.sensorDeviceMap = null;
    }
}
