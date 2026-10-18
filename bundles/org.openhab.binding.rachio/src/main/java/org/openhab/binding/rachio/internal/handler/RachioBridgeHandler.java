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
package org.openhab.binding.rachio.internal.handler;

import static org.openhab.binding.rachio.internal.RachioBindingConstants.*;
import static org.openhab.binding.rachio.internal.RachioUtils.getString;
import static org.openhab.binding.rachio.internal.RachioUtils.i18nText;
import static org.openhab.binding.rachio.internal.RachioUtils.isSameInstance;

import java.net.UnknownHostException;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.rachio.internal.RachioConfiguration;
import org.openhab.binding.rachio.internal.api.RachioApi;
import org.openhab.binding.rachio.internal.api.RachioApiException;
import org.openhab.binding.rachio.internal.api.RachioApiThrottledException;
import org.openhab.binding.rachio.internal.api.RachioDevice;
import org.openhab.binding.rachio.internal.api.RachioDiscoverySnapshot;
import org.openhab.binding.rachio.internal.api.RachioSmartHoseSnapshot;
import org.openhab.binding.rachio.internal.api.RachioZone;
import org.openhab.binding.rachio.internal.api.json.RachioBaseStation;
import org.openhab.binding.rachio.internal.api.json.RachioEventGsonDTO;
import org.openhab.binding.rachio.internal.api.json.RachioPropertyGsonDTO.RachioProperty;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioCurrentScheduleResponse;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioDeviceEventListResponse;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioFlexScheduleRuleResponse;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioForecastResponse;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioScheduleRuleResponse;
import org.openhab.binding.rachio.internal.api.json.RachioValve;
import org.openhab.binding.rachio.internal.api.json.RachioValveDayViewsResponse;
import org.openhab.binding.rachio.internal.api.json.RachioValveProgram;
import org.openhab.binding.rachio.internal.api.webhook.RachioWebhookMode;
import org.openhab.binding.rachio.internal.api.webhook.RachioWebhookResourceType;
import org.openhab.binding.rachio.internal.discovery.RachioDiscoveryService;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager.Priority;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager.RequestPurpose;
import org.openhab.core.config.core.status.ConfigStatusMessage;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.rest.WebhookService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RachioBridgeHandler} is responsible for implementing the cloud api access.
 * The concept of a Bridge is used. In general multiple bridges are supported using different API keys.
 * Devices are linked to the bridge. All devices and zones go offline if the cloud api access fails.
 *
 * @author Markus Michels - initial contribution
 */
@NonNullByDefault
public class RachioBridgeHandler extends AbstractRachioBridgeHandler {
    private static final Duration SMART_HOSE_REFRESH_INTERVAL = Duration.ofMinutes(15);

    private final Logger logger = LoggerFactory.getLogger(RachioBridgeHandler.class);
    private final Object lifecycleLock = new Object();
    private final HttpClient httpClient;
    private final TimeZoneProvider timeZoneProvider;
    private volatile RachioApi rachioApi;
    private final RachioWebhookCoordinator webhookCoordinator;
    private final Map<String, ZoneId> smartHoseTimeZones = new ConcurrentHashMap<>();
    private volatile RachioConfiguration thingConfig = new RachioConfiguration();
    private final Set<RachioDiscoveryService> discoveryServices = new CopyOnWriteArraySet<>();
    private final AtomicReference<@Nullable Future<?>> initializationJob = new AtomicReference<>();
    private final AtomicBoolean smartHoseRefreshPending = new AtomicBoolean();
    private volatile boolean disposed;
    private volatile RachioSmartHoseSnapshot smartHoseSnapshot = RachioSmartHoseSnapshot.EMPTY;
    private long lifecycleGeneration;

    public enum RefreshReason {
        SCHEDULED_POLL,
        WEBHOOK_RECONCILIATION,
        INITIALIZATION,
        MANUAL;
    }

    /**
     * Thing Handler for the Bridge thing. Handles the cloud connection and links devices+zones to a bridge.
     * Creates an instance of the RachioApi (holding all RachioDevices + RachioZones for the given API key)
     *
     * @param bridge Bridge class object
     * @param httpClient shared HTTP client managed by openHAB core
     */
    public RachioBridgeHandler(Bridge bridge, HttpClient httpClient) {
        this(bridge, httpClient, () -> null, () -> ZoneOffset.UTC);
    }

    public RachioBridgeHandler(Bridge bridge, HttpClient httpClient,
            Supplier<@Nullable WebhookService> webhookServiceSupplier) {
        this(bridge, httpClient, webhookServiceSupplier, () -> ZoneOffset.UTC);
    }

    public RachioBridgeHandler(Bridge bridge, HttpClient httpClient,
            Supplier<@Nullable WebhookService> webhookServiceSupplier, TimeZoneProvider timeZoneProvider) {
        this(bridge, httpClient, new RachioCloudWebhookRegistry(webhookServiceSupplier), timeZoneProvider);
    }

    public RachioBridgeHandler(Bridge bridge, HttpClient httpClient, RachioCloudWebhookRegistry cloudWebhookRegistry) {
        this(bridge, httpClient, cloudWebhookRegistry, () -> ZoneOffset.UTC);
    }

    public RachioBridgeHandler(Bridge bridge, HttpClient httpClient, RachioCloudWebhookRegistry cloudWebhookRegistry,
            TimeZoneProvider timeZoneProvider) {
        super(bridge);
        this.httpClient = httpClient;
        this.timeZoneProvider = timeZoneProvider;
        webhookCoordinator = new RachioWebhookCoordinator(cloudWebhookRegistry, scheduler);
        rachioApi = new RachioApi("", httpClient);
    }

    /**
     * Creates an API client for initialization or refresh work.
     *
     * @param personId Rachio person identifier, or an empty string before initialization
     * @return a new API client
     */
    RachioApi createRachioApi(String personId) {
        return new RachioApi(personId, httpClient);
    }

    /**
     * Initialize the bridge/cloud handler. Creates a connection to the Rachio Cloud, reads devices + zones and
     * initializes the Thing mapping.
     */
    @Override
    public void initialize() {
        RachioConfiguration.ResolvedConfiguration resolvedConfiguration = resolveEffectiveConfiguration();
        RachioConfiguration configuration = resolvedConfiguration.configuration();
        long generation;
        synchronized (lifecycleLock) {
            disposed = false;
            generation = ++lifecycleGeneration;
            thingConfig = configuration;
            smartHoseSnapshot = RachioSmartHoseSnapshot.EMPTY;
            smartHoseTimeZones.clear();
        }
        cancelInitializationJob();
        releaseCloudWebhookUrl("bridge reinitialization");
        logResolvedConfiguration(resolvedConfiguration);

        String configurationError = validateConfiguration(configuration);
        if (configurationError != null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, configurationError);
            return;
        }

        Future<?> job;
        try {
            job = scheduler.submit(() -> initializeBridge(generation, configuration));
        } catch (RuntimeException e) {
            if (isLifecycleCurrent(generation)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        i18nText("thing-status.rachio.bridge.schedule-initialization-failed"));
            }
            logger.debug("RachioCloud: Unable to schedule initialization", e);
            return;
        }
        boolean accepted;
        synchronized (lifecycleLock) {
            accepted = !disposed && lifecycleGeneration == generation;
            if (accepted) {
                initializationJob.set(job);
            }
        }
        if (!accepted) {
            job.cancel(true);
        }
    }

    private void initializeBridge(long generation, RachioConfiguration configuration) {
        String errorMessage = "";
        ThingStatusDetail errorStatusDetail = ThingStatusDetail.COMMUNICATION_ERROR;

        try {
            logger.debug("RachioCloud: Connecting to Rachio Cloud");
            RachioApi initializedApi = createRachioApi("");
            createCloudConnection(initializedApi, configuration, RefreshReason.INITIALIZATION);
            if (!isLifecycleCurrent(generation)) {
                return;
            }
            Map<String, RachioDevice> deviceList = initializedApi.getDevices();
            assignThingUIDs(deviceList);
            synchronized (lifecycleLock) {
                if (disposed || lifecycleGeneration != generation) {
                    return;
                }
                rachioApi = initializedApi;
            }
            if (!isLifecycleCurrent(generation)) {
                return;
            }
            updateProperties();

            logger.debug("RachioCloud: Connector initialized");
            if (!isLifecycleCurrent(generation)) {
                return;
            }
            updateStatus(ThingStatus.ONLINE);
            if (!isLifecycleCurrent(generation)) {
                return;
            }
            updateListenerManagement();
            triggerPostInitializationDiscovery();
            if (isLifecycleCurrent(generation)) {
                reconcileConfiguredWebhooks(RequestPurpose.INITIALIZATION);
            }
        } catch (RachioApiException e) {
            errorMessage = e.toString();
            errorStatusDetail = initializationErrorStatusDetail(e);
            if (e.getApiResult().isResponseRateLimit()) {
                logger.warn("RachioCloud: Account is blocked due to rate limit, wait 24h and retry");
            }
        } catch (UnknownHostException e) {
            errorMessage = "Unknown Host or Internet connection down";
        } catch (RuntimeException e) {
            errorMessage = getString(e.getMessage());
        } finally {
            if (!errorMessage.isEmpty() && isLifecycleCurrent(generation)) {
                logger.debug("RachioCloud: {}", errorMessage);
                updateStatus(ThingStatus.OFFLINE, errorStatusDetail,
                        i18nText("thing-status.rachio.bridge.initialization-failed", errorMessage));
            }
        }
    }

    private @Nullable String validateConfiguration(RachioConfiguration configuration) {
        if (configuration.apikey.isBlank()) {
            return i18nText("thing-status.rachio.bridge.missing-api-key");
        }
        return null;
    }

    private ThingStatusDetail initializationErrorStatusDetail(RachioApiException e) {
        int responseCode = e.getApiResult().responseCode;
        if (responseCode == HttpStatus.UNAUTHORIZED_401 || responseCode == HttpStatus.FORBIDDEN_403) {
            return ThingStatusDetail.CONFIGURATION_ERROR;
        }
        return ThingStatusDetail.COMMUNICATION_ERROR;
    }

    /**
     * Get the services registered for this bridge. Provides the discovery service.
     */
    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(RachioDiscoveryService.class);
    }

    public void registerDiscoveryService(RachioDiscoveryService discoveryService) {
        discoveryServices.add(discoveryService);
        if (getThing().getStatus() == ThingStatus.ONLINE) {
            logger.debug(
                    "RachioCloud: Discovery service registered after cloud initialization; triggering automatic discovery");
            discoveryService.discoverFromCurrentCloudState("service registration");
        }
    }

    public void unregisterDiscoveryService(RachioDiscoveryService discoveryService) {
        discoveryServices.remove(discoveryService);
    }

    private void triggerPostInitializationDiscovery() {
        if (discoveryServices.isEmpty()) {
            logger.debug(
                    "RachioCloud: Post-initialization discovery trigger skipped; discovery service is not registered yet");
            return;
        }

        logger.debug("RachioCloud: Triggering automatic post-initialization discovery using current cloud state");
        for (RachioDiscoveryService discoveryService : discoveryServices) {
            discoveryService.discoverFromCurrentCloudState("post-initialization");
        }
    }

    /**
     * Handle Thing commands - the bridge doesn't implement any commands
     */
    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        // cloud handler has no channels
        logger.debug("RachioCloud: Command {} for {} ignored", command, channelUID.getAsString());
    }

    /**
     * Update device status (poll Rachio Cloud)
     * in addition webhooks are used to get events (if callbackUrl is configured)
     */
    public void refreshDeviceStatus() {
        refreshDeviceStatus(RefreshReason.MANUAL);
    }

    /**
     * Update device status (poll Rachio Cloud)
     * in addition webhooks are used to get events (if callbackUrl is configured)
     */
    public void refreshDeviceStatus(RefreshReason refreshReason) {
        String errorMessage = "";
        @Nullable
        LifecycleSnapshot lifecycle = null;
        logger.trace("RachioCloud: refreshDeviceStatus ({})", refreshReason);

        if (!beginRefresh()) {
            logger.debug("RachioCloud: Already checking");
            return;
        }

        try {
            lifecycle = currentLifecycleSnapshot();
            if (lifecycle == null) {
                return;
            }
            RachioApi activeApi = lifecycle.api();
            Map<String, RachioDevice> deviceList = activeApi.getDevices();

            RachioApi checkApi = createRachioApi(activeApi.getPersonId());
            createCloudConnection(checkApi, lifecycle.configuration(), refreshReason);
            if (!isLifecycleCurrent(lifecycle.generation(), activeApi)) {
                return;
            }
            if (checkApi.getLastApiResult().isRateLimitBlocked()) {
                String errorCritical = "RachioCloud: API access blocked on update ("
                        + checkApi.getLastApiResult().rateRemaining + " / " + checkApi.getLastApiResult().rateLimit
                        + "), reset at " + checkApi.getLastApiResult().rateReset;
                logger.debug("{}", errorCritical);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        i18nText("thing-status.rachio.bridge.api-rate-limit-blocked",
                                checkApi.getLastApiResult().rateRemaining, checkApi.getLastApiResult().rateLimit,
                                checkApi.getLastApiResult().rateReset)); // shutdown bridge+devices+zones
                return;
            }
            Map<String, RachioDevice> checkDevList = checkApi.getDevices();
            Map<String, RachioDevice> reconciledDevices = new HashMap<>();
            for (Map.Entry<String, RachioDevice> de : checkDevList.entrySet()) {
                if (!isLifecycleCurrent(lifecycle.generation(), activeApi)) {
                    return;
                }
                RachioDevice checkDev = de.getValue();
                RachioDevice dev = deviceList.get(checkDev.id);
                if (dev == null) {
                    logger.debug("RachioCloud: New device detected: {} - {}", checkDev.id, checkDev.name);
                    assignThingUID(checkDev);
                    reconciledDevices.put(checkDev.id, checkDev);
                } else {
                    reconcileDeviceAndZones(dev, checkDev, refreshReason);
                    reconciledDevices.put(dev.id, dev);
                }
            }
            synchronized (lifecycleLock) {
                if (disposed || lifecycleGeneration != lifecycle.generation()
                        || !isSameInstance(rachioApi, activeApi)) {
                    return;
                }
                activeApi.replaceDevices(reconciledDevices);
            }
            if (this.getThing().getStatus() != ThingStatus.ONLINE) {
                logger.debug("RachioCloud: Bridge is ONLINE");
                updateStatus(ThingStatus.ONLINE);
            }
            for (RachioStatusListener listener : rachioStatusListeners) {
                if (!isLifecycleCurrent(lifecycle.generation(), activeApi)) {
                    return;
                }
                try {
                    listener.onDeviceCatalogChanged();
                } catch (RuntimeException e) {
                    logger.debug("RachioCloud: Catalogue listener update failed (listener={})",
                            listener.getClass().getSimpleName(), e);
                }
            }
        } catch (RachioApiThrottledException e) {
            logger.debug("RachioCloud: {} refresh deferred by the local API budget guard at priority {}: {}",
                    refreshReason, e.getPriority(), e.getMessage());
        } catch (RachioApiException e) {
            errorMessage = e.toString();
        } catch (RuntimeException | UnknownHostException e) {
            errorMessage = getString(e.getMessage());
        } finally {
            if (!errorMessage.isEmpty()) {
                logger.debug("RachioBridge: {}", errorMessage);
                publishCommunicationErrorIfCurrent(lifecycle, errorMessage);
            }
            endRefresh();
        }
    }

    private void publishCommunicationErrorIfCurrent(@Nullable LifecycleSnapshot lifecycle, String errorMessage) {
        if (lifecycle != null && isLifecycleCurrent(lifecycle.generation(), lifecycle.api())) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    i18nText("thing-status.rachio.bridge.refresh-failed", errorMessage));
        }
    }

    void reconcileDeviceAndZones(RachioDevice dev, RachioDevice checkDev, RefreshReason refreshReason) {
        RachioDeviceHandler deviceHandler = dev.getThingHandler();
        boolean deviceChanged = !dev.compare(checkDev);
        dev.update(checkDev);
        if (deviceChanged) {
            logger.trace("RachioCloud: Update data for device {}", dev.name);
            if (deviceHandler != null) {
                deviceHandler.onThingStateChanged(checkDev, null);
            } else {
                notifyThingStateChanged(checkDev, null);
            }
        } else {
            logger.trace("RachioCloud: Device {} was not updated", checkDev.id);
            if (deviceHandler != null) {
                deviceHandler.refreshThingStatusAfterSuccessfulCommunication();
            }
        }

        Map<String, RachioZone> zoneList = dev.getZones();
        Map<String, RachioZone> checkZoneList = checkDev.getZones();
        Map<String, RachioZone> reconciledZones = new HashMap<>();
        Map<String, Boolean> changedZones = new HashMap<>();
        for (Map.Entry<String, RachioZone> ze : checkZoneList.entrySet()) {
            RachioZone checkZone = ze.getValue();
            RachioZone zone = zoneList.get(checkZone.id);
            if (zone == null) {
                logger.debug("RachioCloud: New zone detected: {} - {}", checkDev.id, checkZone.name);
                ThingUID zoneThingUID = new ThingUID(THING_TYPE_ZONE, getThing().getUID(), checkZone.getThingID());
                checkZone.setUID(dev.getUID(), zoneThingUID);
                zone = checkZone;
            } else {
                boolean zoneChanged = !zone.compare(checkZone);
                zone.update(checkZone);
                changedZones.put(zone.id, zoneChanged);
            }
            reconciledZones.put(zone.id, zone);
        }

        dev.replaceZones(reconciledZones);
        for (RachioZone reconciledZone : reconciledZones.values()) {
            RachioZoneHandler zoneHandler = reconciledZone.getThingHandler();
            boolean zoneChanged = changedZones.getOrDefault(reconciledZone.id, true);
            if (zoneChanged) {
                logger.trace("RachioCloud: Update status for zone {}", reconciledZone.name);
                if (zoneHandler != null) {
                    zoneHandler.rebindToCurrentModel(dev, reconciledZone, "cloud state refresh");
                    zoneHandler.onThingStateChanged(null, reconciledZone);
                } else {
                    notifyThingStateChanged(null, reconciledZone);
                }
            } else {
                logger.trace("RachioCloud: Zone {} was not updated.", reconciledZone.id);
                if (zoneHandler != null) {
                    zoneHandler.rebindToCurrentModel(dev, reconciledZone, "cloud state refresh");
                    zoneHandler.refreshThingStatusAfterSuccessfulCommunication();
                }
            }
        }
        if (deviceHandler != null) {
            deviceHandler.retryDeferredWebhookRegistrationIfDue();
            if (refreshReason == RefreshReason.SCHEDULED_POLL) {
                logger.debug(
                        "RachioCloud: Core scheduled status poll completed for controller '{}'; refreshing essential running state before optional enrichments",
                        checkDev.id);
            }
            deviceHandler.refreshSmartIrrigationReadExtensions(false, getRequestPurpose(refreshReason), refreshReason);
        }
    }

    private void assignThingUIDs(Map<String, RachioDevice> devices) {
        devices.values().forEach(this::assignThingUID);
    }

    private void assignThingUID(RachioDevice device) {
        Bridge bridgeThing = getThing();
        ThingUID deviceThingUID = new ThingUID(THING_TYPE_DEVICE, bridgeThing.getUID(), device.getThingID());
        device.setUID(bridgeThing.getUID(), deviceThingUID);
        for (RachioZone zone : device.getZones().values()) {
            ThingUID zoneThingUID = new ThingUID(THING_TYPE_ZONE, bridgeThing.getUID(), zone.getThingID());
            zone.setUID(deviceThingUID, zoneThingUID);
        }
    }

    @Override
    public void shutdown() {
        logger.debug("RachioCloud: Shutting down");
        invalidateLifecycle();
        cancelInitializationJob();
        releaseCloudWebhookUrl("bridge shutdown");
        super.shutdown();
    }

    private void cancelInitializationJob() {
        Future<?> job = initializationJob.getAndSet(null);
        if (job != null) {
            job.cancel(true);
        }
    }

    /**
     * Create a new Rachio cloud service connection. If a connection already exists, it will be replaced.
     *
     * @throws RachioApiException if there is an error while authenticating to the service
     */
    private void createCloudConnection(RachioApi api, RachioConfiguration configuration, RefreshReason refreshReason)
            throws RachioApiException, UnknownHostException {
        if (configuration.apikey.isEmpty()) {
            throw new RachioApiException(
                    "RachioCloud: Unable to connect to Rachio Cloud: API key is not set; configure the Rachio Cloud Connector Thing.");
        }

        // initialize API access, may throw an exception
        api.initialize(configuration.apikey, this.getThing().getUID(), getRefreshPriority(refreshReason),
                getRequestPurpose(refreshReason));
    }

    /** Returns an immutable snapshot used to guard asynchronous coordinator work. */
    @Nullable
    LifecycleSnapshot currentLifecycleSnapshot() {
        synchronized (lifecycleLock) {
            if (disposed) {
                return null;
            }
            return new LifecycleSnapshot(lifecycleGeneration, thingConfig, rachioApi);
        }
    }

    private boolean isLifecycleCurrent(long generation) {
        synchronized (lifecycleLock) {
            return !disposed && lifecycleGeneration == generation;
        }
    }

    /** Returns whether the supplied generation and API still belong to this live handler. */
    boolean isLifecycleCurrent(long generation, RachioApi api) {
        synchronized (lifecycleLock) {
            return !disposed && lifecycleGeneration == generation && isSameInstance(rachioApi, api);
        }
    }

    /** Publishes coordinator state only while its captured bridge lifecycle remains current. */
    boolean publishWebhookStateIfCurrent(LifecycleSnapshot lifecycle, Runnable publisher) {
        synchronized (lifecycleLock) {
            if (disposed || lifecycleGeneration != lifecycle.generation()
                    || !isSameInstance(rachioApi, lifecycle.api())) {
                return false;
            }
            publisher.run();
            return true;
        }
    }

    /** Returns the effective bridge configuration used by the webhook coordinator. */
    RachioConfiguration getWebhookConfiguration() {
        return thingConfig;
    }

    /** Returns the active API client used by the webhook coordinator. */
    RachioApi getWebhookApi() {
        return rachioApi;
    }

    private void invalidateLifecycle() {
        synchronized (lifecycleLock) {
            disposed = true;
            lifecycleGeneration++;
        }
    }

    /** Captures the state required to reject stale asynchronous work. */
    record LifecycleSnapshot(long generation, RachioConfiguration configuration, RachioApi api) {
    }

    Priority getRefreshPriority(RefreshReason refreshReason) {
        switch (refreshReason) {
            case SCHEDULED_POLL:
            case WEBHOOK_RECONCILIATION:
            case INITIALIZATION:
            case MANUAL:
            default:
                return Priority.MEDIUM;
        }
    }

    RequestPurpose getRequestPurpose(RefreshReason refreshReason) {
        switch (refreshReason) {
            case INITIALIZATION:
                return RequestPurpose.INITIALIZATION;
            case MANUAL:
                return RequestPurpose.USER_COMMAND;
            case SCHEDULED_POLL:
                return RequestPurpose.CORE_STATUS_POLL;
            case WEBHOOK_RECONCILIATION:
            default:
                return RequestPurpose.BACKGROUND_REFRESH;
        }
    }

    /**
     * puts the device into standby mode = disable watering, schedules etc.
     *
     * @param deviceId: Device (ID retrieved from initialization)
     */
    public void disableDevice(String deviceId) throws RachioApiException {
        rachioApi.disableDevice(deviceId);
    }

    /**
     * puts the device into run mode = watering, schedules etc.
     *
     * @param deviceId: Device (ID retrieved from initialization)
     */
    public void enableDevice(String deviceId) throws RachioApiException {
        rachioApi.enableDevice(deviceId);
    }

    /**
     * Stop watering for all zones, disable schedule etc. - puts the device into standby mode
     *
     * @param deviceId: Device (ID retrieved from initialization)
     */
    public void stopWatering(String deviceId) throws RachioApiException {
        rachioApi.stopWatering(deviceId);
    }

    /**
     * Start rain delay cycle.
     *
     * @param deviceId: Device (ID retrieved from initialization)
     * @param delayTime: Number of seconds for the rain delay cycle
     */
    public void startRainDelay(String deviceId, int delayTime) throws RachioApiException {
        rachioApi.rainDelay(deviceId, delayTime);
    }

    /**
     * Pause the active zone run for a device.
     *
     * @param deviceId Device (ID retrieved from initialization)
     * @param duration Number of seconds to pause the active run
     * @throws RachioApiException if the API call fails
     */
    public void pauseZoneRun(String deviceId, int duration) throws RachioApiException {
        rachioApi.pauseZoneRun(deviceId, duration);
    }

    /**
     * Resume the active zone run for a device.
     *
     * @param deviceId Device (ID retrieved from initialization)
     * @throws RachioApiException if the API call fails
     */
    public void resumeZoneRun(String deviceId) throws RachioApiException {
        rachioApi.resumeZoneRun(deviceId);
    }

    /**
     * Start watering for multiple zones.
     *
     * @param zoneListJson: Contains a list of { "id": n} with the zone ids to start
     */
    public void runMultipleZones(String zoneListJson) throws RachioApiException {
        rachioApi.runMultipleZones(zoneListJson);
    }

    /**
     * Start a single zone for given number of seconds.
     *
     * @param zoneId: Rachio Cloud Zone ID
     * @param runTime: Number of seconds to run
     */
    public void startZone(String zoneId, int runTime) throws RachioApiException {
        rachioApi.runZone(zoneId, runTime);
    }

    /**
     * Enable or disable a zone.
     *
     * @param zoneId Rachio Cloud Zone ID
     * @param enabled true to enable, false to disable
     * @throws RachioApiException if the API call fails
     */
    public void setZoneEnabled(String zoneId, boolean enabled) throws RachioApiException {
        if (enabled) {
            rachioApi.enableZone(zoneId);
        } else {
            rachioApi.disableZone(zoneId);
        }
    }

    public RachioCurrentScheduleResponse getCurrentSchedule(String deviceId) throws RachioApiException {
        return rachioApi.getCurrentSchedule(deviceId);
    }

    public RachioCurrentScheduleResponse getCurrentSchedule(String deviceId, RequestPurpose requestPurpose)
            throws RachioApiException {
        return rachioApi.getCurrentSchedule(deviceId, requestPurpose);
    }

    public RachioDeviceEventListResponse getDeviceEvents(String deviceId, long startTime, long endTime)
            throws RachioApiException {
        return rachioApi.getDeviceEvents(deviceId, startTime, endTime);
    }

    public RachioForecastResponse getDeviceForecast(String deviceId, String units) throws RachioApiException {
        return rachioApi.getDeviceForecast(deviceId, units);
    }

    public List<RachioProperty> listProperties(String userId) throws RachioApiException {
        return rachioApi.listProperties(userId);
    }

    public RachioProperty getProperty(String propertyId) throws RachioApiException {
        return rachioApi.getProperty(propertyId);
    }

    public @Nullable RachioProperty findPropertyByEntity(String entityId, String entityType) throws RachioApiException {
        return rachioApi.findPropertyByEntity(entityId, entityType);
    }

    public @Nullable RachioProperty findPropertyForLocation(String locationId) throws RachioApiException {
        return rachioApi.findPropertyForLocation(locationId);
    }

    public @Nullable RachioProperty findPropertyForBaseStation(String baseStationId) throws RachioApiException {
        return rachioApi.findPropertyForBaseStation(baseStationId);
    }

    public @Nullable RachioProperty findPropertyForLightingArea(String lightingAreaId) throws RachioApiException {
        return rachioApi.findPropertyForLightingArea(lightingAreaId);
    }

    public List<RachioBaseStation> listBaseStations() throws RachioApiException {
        RachioApi api = rachioApi;
        return api.listBaseStations(api.getPersonId());
    }

    public RachioSmartHoseSnapshot getSmartHoseSnapshot() throws RachioApiException {
        try {
            refreshSmartHoseSnapshot(false);
        } catch (RachioApiException e) {
            if (smartHoseSnapshot.retrievedAt().equals(Instant.EPOCH)) {
                throw e;
            }
            logger.debug("RachioCloud: Reusing the last Smart Hose snapshot after refresh failure: {}", e.getMessage());
        }
        return smartHoseSnapshot;
    }

    void refreshSmartHoseSnapshot(boolean scheduledRefresh) throws RachioApiException {
        Instant now = Instant.now();
        RachioSmartHoseSnapshot currentSnapshot = smartHoseSnapshot;
        if (!currentSnapshot.retrievedAt().plus(SMART_HOSE_REFRESH_INTERVAL).isBefore(now)) {
            return;
        }
        if (!smartHoseRefreshPending.compareAndSet(false, true)) {
            logger.trace("RachioCloud: Smart Hose snapshot refresh already in progress");
            return;
        }

        try {
            LifecycleSnapshot lifecycle = currentLifecycleSnapshot();
            if (lifecycle == null) {
                return;
            }
            RachioSmartHoseSnapshot updatedSnapshot = loadSmartHoseSnapshot(lifecycle.api(), now);
            if (!isLifecycleCurrent(lifecycle.generation(), lifecycle.api())) {
                return;
            }
            smartHoseSnapshot = updatedSnapshot;
            smartHoseTimeZones.clear();
            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }
            if (currentSnapshot.retrievedAt().equals(Instant.EPOCH)
                    || !updatedSnapshot.hasSameContent(currentSnapshot)) {
                logger.debug("RachioCloud: Smart Hose snapshot updated (baseStations={}, valves={}, programs={})",
                        updatedSnapshot.baseStations().size(), updatedSnapshot.valves().size(),
                        updatedSnapshot.programs().size());
                notifySmartHoseStateChanged(updatedSnapshot);
            } else {
                logger.trace("RachioCloud: Smart Hose snapshot unchanged");
            }
        } catch (RachioApiThrottledException e) {
            if (!scheduledRefresh) {
                throw e;
            }
            logger.debug("RachioCloud: Smart Hose snapshot refresh deferred by the local API budget guard: {}",
                    e.getMessage());
        } finally {
            smartHoseRefreshPending.set(false);
        }
    }

    private RachioSmartHoseSnapshot loadSmartHoseSnapshot(RachioApi api, Instant retrievedAt)
            throws RachioApiException {
        Map<String, RachioBaseStation> baseStations = new HashMap<>();
        Map<String, RachioValve> valves = new HashMap<>();
        Map<String, RachioValveProgram> programs = new HashMap<>();

        for (RachioBaseStation baseStation : api.listBaseStations(api.getPersonId())) {
            if (baseStation.id.isBlank()) {
                continue;
            }
            baseStations.put(baseStation.id, baseStation);
            List<RachioValve> baseStationValves = api.listValves(baseStation.id);
            for (RachioValve valve : baseStationValves) {
                if (valve.id.isBlank()) {
                    continue;
                }
                if (valve.baseStationId.isBlank()) {
                    valve.baseStationId = baseStation.id;
                }
                valves.put(valve.id, valve);
            }

            try {
                addValvePrograms(programs, api.listValveProgramsV2ByBaseStation(baseStation.id), baseStation.id);
            } catch (RachioApiThrottledException e) {
                throw e;
            } catch (RachioApiException e) {
                logger.debug(
                        "RachioCloud: Unable to load Smart Hose programs for base station '{}'; trying valve endpoints: {}",
                        baseStation.id, e.getMessage());
                for (RachioValve valve : baseStationValves) {
                    if (!valve.id.isBlank()) {
                        addValvePrograms(programs, listValveProgramsForValve(api, valve.id), baseStation.id);
                    }
                }
            }
        }
        return new RachioSmartHoseSnapshot(baseStations, valves, programs, retrievedAt);
    }

    private void addValvePrograms(Map<String, RachioValveProgram> programs, List<RachioValveProgram> loadedPrograms,
            String baseStationId) {
        for (RachioValveProgram program : loadedPrograms) {
            if (program.id.isBlank()) {
                continue;
            }
            if (program.baseStationId.isBlank()) {
                program.baseStationId = baseStationId;
            }
            programs.put(program.id, program);
        }
    }

    public RachioBaseStation getBaseStation(String baseStationId) throws RachioApiException {
        return rachioApi.getBaseStation(baseStationId);
    }

    public RachioBaseStation getBaseStationForInitialization(String baseStationId) throws RachioApiException {
        return rachioApi.getBaseStation(baseStationId, RequestPurpose.INITIALIZATION);
    }

    public List<RachioValve> listValves(String baseStationId) throws RachioApiException {
        return rachioApi.listValves(baseStationId);
    }

    public RachioValve getValve(String valveId) throws RachioApiException {
        return rachioApi.getValve(valveId);
    }

    public RachioValve getValveForInitialization(String valveId) throws RachioApiException {
        return rachioApi.getValve(valveId, RequestPurpose.INITIALIZATION);
    }

    public void setValveDefaultRuntime(String valveId, int defaultRuntimeSeconds) throws RachioApiException {
        rachioApi.setValveDefaultRuntime(valveId, defaultRuntimeSeconds);
    }

    public void startValveWatering(String valveId, int durationSeconds) throws RachioApiException {
        rachioApi.startValveWatering(valveId, durationSeconds);
    }

    public void stopValveWatering(String valveId) throws RachioApiException {
        rachioApi.stopValveWatering(valveId);
    }

    public List<RachioValveProgram> listValveProgramsForValve(String valveId) throws RachioApiException {
        return listValveProgramsForValve(rachioApi, valveId);
    }

    private List<RachioValveProgram> listValveProgramsForValve(RachioApi api, String valveId)
            throws RachioApiException {
        try {
            return api.listValveProgramsV2ByValve(valveId);
        } catch (RachioApiThrottledException e) {
            throw e;
        } catch (RachioApiException e) {
            logger.debug(
                    "Unable to load Smart Hose Timer Program V2 list for valve '{}'; trying legacy program list: {}",
                    valveId, e.getMessage());
            return api.listValvePrograms(valveId);
        }
    }

    public RachioValveProgram getValveProgram(String programId) throws RachioApiException {
        try {
            return rachioApi.getValveProgramV2(programId);
        } catch (RachioApiException e) {
            logger.debug("Unable to load Smart Hose Timer Program V2 '{}'; trying legacy program endpoint: {}",
                    programId, e.getMessage());
            return rachioApi.getValveProgram(programId);
        }
    }

    public RachioValveProgram getValveProgramForInitialization(String programId) throws RachioApiException {
        try {
            return rachioApi.getValveProgramV2(programId, RequestPurpose.INITIALIZATION);
        } catch (RachioApiThrottledException e) {
            throw e;
        } catch (RachioApiException e) {
            logger.debug("Unable to load Smart Hose Timer Program V2 '{}'; trying legacy program endpoint: {}",
                    programId, e.getMessage());
            return rachioApi.getValveProgram(programId, RequestPurpose.INITIALIZATION);
        }
    }

    public RachioValveDayViewsResponse getValveDayViews(String valveId) throws RachioApiException {
        ZoneId zoneId = getSmartHoseTimeZoneForValve(valveId, "");
        LocalDate today = LocalDate.now(zoneId);
        LocalDate end = today.plusDays(getHoseSummaryLookaheadDays());
        LocalDate start = today.minusDays(getHoseSummaryLookbackDays());
        return rachioApi.getValveDayViews(valveId, start, end);
    }

    public ZoneId getTimeZone() {
        return timeZoneProvider.getTimeZone();
    }

    public ZoneId getSmartHoseTimeZone(String baseStationId) {
        if (baseStationId.isBlank()) {
            return getTimeZone();
        }
        return Objects
                .requireNonNull(smartHoseTimeZones.computeIfAbsent(baseStationId, this::resolveSmartHoseTimeZone));
    }

    public ZoneId getSmartHoseTimeZoneForValve(String valveId, String baseStationId) {
        String resolvedBaseStationId = baseStationId;
        if (resolvedBaseStationId.isBlank()) {
            RachioValve valve = smartHoseSnapshot.valves().get(valveId);
            if (valve != null) {
                resolvedBaseStationId = valve.baseStationId;
            }
        }
        return getSmartHoseTimeZone(resolvedBaseStationId);
    }

    private ZoneId resolveSmartHoseTimeZone(String baseStationId) {
        try {
            RachioProperty property = findPropertyForBaseStation(baseStationId);
            if (property != null && !property.timeZone.isBlank()) {
                try {
                    return ZoneId.of(property.timeZone);
                } catch (DateTimeException e) {
                    logger.debug(
                            "RachioCloud: Property for Smart Hose base station '{}' returned invalid time zone '{}'",
                            baseStationId, property.timeZone);
                }
            }
        } catch (RachioApiException e) {
            logger.debug("RachioCloud: Unable to resolve time zone for Smart Hose base station '{}': {}", baseStationId,
                    e.getMessage());
        }
        return getTimeZone();
    }

    public void createSkipOverride(String programId, String timestamp) throws RachioApiException {
        rachioApi.createSkipOverride(programId, timestamp);
    }

    public void deleteSkipOverride(String programId, String timestamp) throws RachioApiException {
        rachioApi.deleteSkipOverride(programId, timestamp);
    }

    public void createPlannedRunSkipOverride(String plannedRunId, String date) throws RachioApiException {
        rachioApi.createPlannedRunSkipOverride(plannedRunId, date);
    }

    public void deletePlannedRunSkipOverride(String plannedRunId, String date) throws RachioApiException {
        rachioApi.deletePlannedRunSkipOverride(plannedRunId, date);
    }

    public void setZoneMoistureLevel(String zoneId, double level) throws RachioApiException {
        rachioApi.setZoneMoistureLevel(zoneId, level);
    }

    public void setZoneMoisturePercent(String zoneId, double percent) throws RachioApiException {
        rachioApi.setZoneMoisturePercent(zoneId, percent);
    }

    public RachioScheduleRuleResponse getScheduleRule(String scheduleRuleId) throws RachioApiException {
        return rachioApi.getScheduleRule(scheduleRuleId);
    }

    public RachioScheduleRuleResponse getScheduleRuleForInitialization(String scheduleRuleId)
            throws RachioApiException {
        return rachioApi.getScheduleRule(scheduleRuleId, RequestPurpose.INITIALIZATION);
    }

    public RachioFlexScheduleRuleResponse getFlexScheduleRule(String flexScheduleRuleId) throws RachioApiException {
        return rachioApi.getFlexScheduleRule(flexScheduleRuleId);
    }

    public RachioFlexScheduleRuleResponse getFlexScheduleRuleForInitialization(String flexScheduleRuleId)
            throws RachioApiException {
        return rachioApi.getFlexScheduleRule(flexScheduleRuleId, RequestPurpose.INITIALIZATION);
    }

    public void startScheduleRule(String scheduleRuleId) throws RachioApiException {
        rachioApi.startScheduleRule(scheduleRuleId);
    }

    public void skipScheduleRule(String scheduleRuleId) throws RachioApiException {
        rachioApi.skipScheduleRule(scheduleRuleId);
    }

    public void setScheduleRuleSeasonalAdjustment(String scheduleRuleId, double adjustment) throws RachioApiException {
        rachioApi.setScheduleRuleSeasonalAdjustment(scheduleRuleId, adjustment);
    }

    public void skipForwardZoneRun(String id) throws RachioApiException {
        rachioApi.skipForwardZoneRun(id);
    }

    //
    // ------ Read Thing config
    //

    /**
     * Retrieve the API key for connecting to Rachio cloud
     *
     * @return the Rachio API key
     */
    public String getApiKey() {
        String apikey = thingConfig.apikey;
        if (!apikey.isEmpty()) {
            return apikey;
        }
        return resolveEffectiveConfiguration().configuration().apikey;
    }

    /**
     * Retrieve the polling interval from Thing config
     *
     * @return the polling interval in seconds
     */
    public int getPollingInterval() {
        return thingConfig.pollingInterval;
    }

    /**
     * Retrieve the callback URL for Rachio Cloud Events
     *
     * @return callbackUrl
     */
    public String getCallbackUrl() {
        return thingConfig.callbackUrl;
    }

    public String getCallbackUsername() {
        return thingConfig.callbackUsername;
    }

    public String getCallbackPassword() {
        return thingConfig.callbackPassword;
    }

    /**
     * Retrieve the clearAllCallbacks flag from thing config
     *
     * @return true=clear all callbacks, false=clear only the current one (avoid multiple instances)
     */
    public boolean getClearAllCallbacks() {
        return thingConfig.clearAllCallbacks;
    }

    /**
     * Retrieve the default runtime from Thing config
     *
     * @return the default runtime in seconds
     */
    public int getDefaultRuntime() {
        return thingConfig.defaultRuntime;
    }

    public int getEventHistoryLookbackHours() {
        return thingConfig.eventHistoryLookbackHours;
    }

    public String getForecastUnits() {
        return thingConfig.forecastUnits;
    }

    public int getHoseSummaryLookbackDays() {
        return thingConfig.hoseSummaryLookbackDays;
    }

    public int getHoseSummaryLookaheadDays() {
        return thingConfig.hoseSummaryLookaheadDays;
    }

    boolean isDisposed() {
        return disposed;
    }

    //
    // ------ Stuff used by other classes
    //

    /**
     * Get the list of discovered devices (those retrieved from the Rachio Cloud)
     *
     * @return map of RachioDevice
     */
    public Map<String, RachioDevice> getDevices() {
        return rachioApi.getDevices();
    }

    public RachioDiscoverySnapshot getDiscoverySnapshot() {
        return rachioApi.getDiscoverySnapshot();
    }

    @Nullable
    RachioDevice findIrrigationController(String controllerId) {
        if (controllerId.isBlank()) {
            return null;
        }
        Map<String, RachioDevice> devices = getDevices();
        RachioDevice controller = devices.get(controllerId);
        if (controller != null) {
            return controller;
        }
        for (RachioDevice candidate : devices.values()) {
            if (candidate.id.equalsIgnoreCase(controllerId)) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * return RachioDevice by device Thing UID
     *
     * @param thingUID
     * @return RachioDevice for that device Thing UID
     */
    public @Nullable RachioDevice getDevByUID(@Nullable ThingUID thingUID) {
        return rachioApi.getDevByUID(getThing().getUID(), thingUID);
    }

    public @Nullable RachioDevice getDevByThing(Thing thing) {
        return rachioApi.getDevByUID(getThing().getUID(), thing.getUID(), thing.getConfiguration().getProperties(),
                thing.getProperties());
    }

    public @Nullable RachioDevice getDevByConfiguredDeviceId(Thing thing, String deviceId) {
        return rachioApi.bindDeviceByRachioId(getThing().getUID(), thing.getUID(), deviceId);
    }

    public @Nullable RachioDevice getDevForZone(RachioZone zone) {
        return rachioApi.getDeviceByZoneRachioId(zone.id);
    }

    /**
     * return RachioZone for given Zone Thing UID
     *
     * @param thingUID Zone Thing UID
     * @return matching RachioZone, or null when no zone matches
     */
    public @Nullable RachioZone getZoneByUID(@Nullable ThingUID thingUID) {
        return rachioApi.getZoneByUID(getThing().getUID(), thingUID);
    }

    public @Nullable RachioZone getZoneByThing(Thing thing) {
        return rachioApi.getZoneByUID(getThing().getUID(), thing.getUID(), thing.getConfiguration().getProperties(),
                thing.getProperties());
    }

    /**
     * Register a webhook at Rachio Cloud for the given device ID. The webhook triggers our servlet to process device
     * and zone events.
     *
     * @param deviceId: Matching device ID (as retrieved from device initialization)
     */
    public void registerWebHook(String deviceId) throws RachioApiException {
        registerWebHook(deviceId, RequestPurpose.BACKGROUND_REFRESH);
    }

    public void registerWebHook(String deviceId, RequestPurpose requestPurpose) throws RachioApiException {
        webhookCoordinator.registerWebHook(this, deviceId, requestPurpose);
    }

    public void registerValveWebHook(String valveId) throws RachioApiException {
        registerValveWebHook(valveId, RequestPurpose.BACKGROUND_REFRESH);
    }

    public void registerValveWebHook(String valveId, RequestPurpose requestPurpose) throws RachioApiException {
        webhookCoordinator.registerValveWebHook(this, valveId, requestPurpose);
    }

    public void registerValveProgramWebHook(String programId) throws RachioApiException {
        registerValveProgramWebHook(programId, RequestPurpose.BACKGROUND_REFRESH);
    }

    public void registerValveProgramWebHook(String programId, RequestPurpose requestPurpose) throws RachioApiException {
        webhookCoordinator.registerValveProgramWebHook(this, programId, requestPurpose);
    }

    /** Returns the effective webhook mode for the requested resource type. */
    RachioWebhookMode getWebhookMode(RachioWebhookResourceType resourceType) {
        return webhookCoordinator.getWebhookMode(this, resourceType);
    }

    /** Returns the controller webhook mode currently allowed to process events. */
    RachioWebhookMode getActiveIrrigationWebhookProcessingMode() {
        return webhookCoordinator.getActiveIrrigationWebhookProcessingMode(this);
    }

    public void onCloudWebhookProviderChanged() {
        webhookCoordinator.onCloudWebhookProviderChanged(this);
    }

    /**
     * Reconciles configured webhook registrations using the requested API budget category.
     *
     * @param requestPurpose purpose used by the client-side rate limiter
     */
    void reconcileConfiguredWebhooks(RequestPurpose requestPurpose) {
        webhookCoordinator.reconcileConfiguredWebhooks(this, requestPurpose);
    }

    /** Returns the validated modern webhook registration URL, or an empty string. */
    String getModernWebhookUrlForRegistration() {
        return webhookCoordinator.getModernWebhookUrlForRegistration(this);
    }

    private void releaseCloudWebhookUrl(String reason) {
        webhookCoordinator.releaseCloudWebhookUrl(this, reason);
    }

    /**
     * Handle inbound WebHook event (dispatch to device handler)
     *
     * @param event inbound Rachio webhook event
     * @return true if the event was dispatched to a matching handler
     */
    public boolean webHookEvent(RachioEventGsonDTO event) {
        return webhookCoordinator.webHookEvent(this, event);
    }

    public boolean legacyWebHookEvent(RachioEventGsonDTO event) {
        return webhookCoordinator.legacyWebHookEvent(this, event);
    }

    public @Nullable String getExternalId() {
        return rachioApi.getExternalId();
    }

    @Override
    public Collection<ConfigStatusMessage> getConfigStatus() {
        Collection<ConfigStatusMessage> configStatusMessages = new ArrayList<>();

        RachioConfiguration config = resolveEffectiveConfiguration().configuration();

        if (config.apikey.isEmpty()) {
            configStatusMessages.add(
                    ConfigStatusMessage.Builder.error(PARAM_APIKEY).withMessageKeySuffix("missing-api-key").build());
        }

        return configStatusMessages;
    }

    private RachioConfiguration.ResolvedConfiguration resolveEffectiveConfiguration() {
        return RachioConfiguration.resolveEffectiveConfig(getConfig().getProperties());
    }

    private void logResolvedConfiguration(RachioConfiguration.ResolvedConfiguration resolvedConfiguration) {
        RachioConfiguration config = resolvedConfiguration.configuration();
        logger.debug(
                "Rachio Cloud configuration resolved: apikeyConfigured={} ({}), pollingInterval={} ({}), defaultRuntime={} ({}), eventHistoryLookbackHours={} ({}), forecastUnits={} ({}), hoseSummaryLookbackDays={} ({}), hoseSummaryLookaheadDays={} ({}), callbackUrlConfigured={} ({}), callbackUsernameConfigured={} ({}), callbackPasswordConfigured={} ({}), clearAllCallbacks={} ({}), autoConfigureWebhooks={} ({}), autoConfigureHoseTimerWebhooks={} ({}), useCloudWebhook={} ({}), publicWebhookUrlConfigured={} ({})",
                isConfigured(config.apikey), sourceLabel(resolvedConfiguration, PARAM_APIKEY), config.pollingInterval,
                sourceLabel(resolvedConfiguration, PARAM_POLLING_INTERVAL), config.defaultRuntime,
                sourceLabel(resolvedConfiguration, PARAM_DEFAULT_RUNTIME), config.eventHistoryLookbackHours,
                sourceLabel(resolvedConfiguration, PARAM_EVENT_HISTORY_LOOKBACK_HOURS), config.forecastUnits,
                sourceLabel(resolvedConfiguration, PARAM_FORECAST_UNITS), config.hoseSummaryLookbackDays,
                sourceLabel(resolvedConfiguration, PARAM_HOSE_SUMMARY_LOOKBACK_DAYS), config.hoseSummaryLookaheadDays,
                sourceLabel(resolvedConfiguration, PARAM_HOSE_SUMMARY_LOOKAHEAD_DAYS), isConfigured(config.callbackUrl),
                sourceLabel(resolvedConfiguration, PARAM_CALLBACK_URL), isConfigured(config.callbackUsername),
                sourceLabel(resolvedConfiguration, PARAM_CALLBACK_USERNAME), isConfigured(config.callbackPassword),
                sourceLabel(resolvedConfiguration, PARAM_CALLBACK_PASSWORD), config.clearAllCallbacks,
                sourceLabel(resolvedConfiguration, PARAM_CLEAR_CALLBACK), config.autoConfigureWebhooks,
                sourceLabel(resolvedConfiguration, PARAM_AUTO_CONFIGURE_WEBHOOKS),
                config.autoConfigureHoseTimerWebhooks,
                sourceLabel(resolvedConfiguration, PARAM_AUTO_CONFIGURE_HOSE_TIMER_WEBHOOKS), config.useCloudWebhook,
                sourceLabel(resolvedConfiguration, PARAM_USE_CLOUD_WEBHOOK), isConfigured(config.publicWebhookUrl),
                sourceLabel(resolvedConfiguration, PARAM_PUBLIC_WEBHOOK_URL));
    }

    private String sourceLabel(RachioConfiguration.ResolvedConfiguration resolvedConfiguration, String parameterName) {
        return resolvedConfiguration.source(parameterName).label();
    }

    private boolean isConfigured(String value) {
        return !value.isBlank();
    }

    /** Refreshes bridge Thing properties from the API and webhook coordinator. */
    void updateProperties() {
        Map<String, String> properties = new HashMap<>(rachioApi.fillProperties());
        webhookCoordinator.addProperties(properties);
        updateProperties(properties);
    }

    @Override
    protected int getPollingIntervalSeconds() {
        return getPollingInterval();
    }

    @Override
    protected void runScheduledRefresh() {
        if (getStatusListenerCount() > 0) {
            refreshDeviceStatus(RefreshReason.SCHEDULED_POLL);
        }
        if (getSmartHoseStatusListenerCount() > 0) {
            try {
                refreshSmartHoseSnapshot(true);
            } catch (RachioApiException e) {
                logger.debug("RachioCloud: Unable to refresh Smart Hose snapshot: {}", e.getMessage());
                publishCommunicationErrorIfCurrent(currentLifecycleSnapshot(), e.toString());
            }
        }
    }

    @Override
    public synchronized void dispose() {
        logger.debug("RachioCloud: Disposing handler");
        invalidateLifecycle();
        cancelInitializationJob();
        releaseCloudWebhookUrl("bridge disposal");
        smartHoseSnapshot = RachioSmartHoseSnapshot.EMPTY;
        smartHoseTimeZones.clear();
        super.dispose();
    }
}
