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

import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rachio.internal.RachioConfiguration;
import org.openhab.binding.rachio.internal.api.RachioApi;
import org.openhab.binding.rachio.internal.api.RachioApiException;
import org.openhab.binding.rachio.internal.api.RachioApiThrottledException;
import org.openhab.binding.rachio.internal.api.RachioDevice;
import org.openhab.binding.rachio.internal.api.RachioZone;
import org.openhab.binding.rachio.internal.api.json.RachioEventGsonDTO;
import org.openhab.binding.rachio.internal.api.json.RachioPropertyGsonDTO.RachioProperty;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioBaseStation;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioValve;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioValveDayViewsResponse;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioValveProgram;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioCurrentScheduleResponse;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioDeviceEventListResponse;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioFlexScheduleRuleResponse;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioForecastResponse;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioScheduleRuleResponse;
import org.openhab.binding.rachio.internal.api.webhook.RachioWebhookMode;
import org.openhab.binding.rachio.internal.api.webhook.RachioWebhookResourceType;
import org.openhab.binding.rachio.internal.api.webhook.RachioWebhookTarget;
import org.openhab.binding.rachio.internal.discovery.RachioDiscoveryService;
import org.openhab.binding.rachio.internal.handler.RachioCloudWebhookRegistry.CloudWebhookException;
import org.openhab.binding.rachio.internal.handler.RachioCloudWebhookRegistry.CloudWebhookLease;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager.PRIORITY;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager.RequestPurpose;
import org.openhab.core.config.core.status.ConfigStatusMessage;
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
    private static final long MIN_CLOUD_WEBHOOK_REFRESH_DELAY_SECONDS = 60;
    private static final long NO_CLOUD_WEBHOOK_GENERATION = -1;
    private static final Duration CLOUD_WEBHOOK_REFRESH_SAFETY_WINDOW = Duration.ofHours(1);

    private final Logger logger = LoggerFactory.getLogger(RachioBridgeHandler.class);
    private final RachioApi rachioApi;
    private final RachioCloudWebhookRegistry cloudWebhookRegistry;
    private RachioConfiguration thingConfig = new RachioConfiguration();
    private String personId = "";
    private final Set<RachioDiscoveryService> discoveryServices = new CopyOnWriteArraySet<>();
    private @Nullable ScheduledFuture<?> cloudWebhookRefreshJob;
    private @Nullable String resolvedModernWebhookUrl;
    private long resolvedCloudWebhookGeneration = NO_CLOUD_WEBHOOK_GENERATION;
    private String webhookMode = "disabled";
    private String webhookRegistrationState = "disabled";
    private String lastWebhookRegistrationAttempt = "";
    private String lastWebhookEventTimestamp = "";
    private String lastWebhookEventType = "";
    private volatile boolean disposed;

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
     * @param bridge: Bridge class object
     */
    public RachioBridgeHandler(final Bridge bridge) {
        this(bridge, () -> null);
    }

    public RachioBridgeHandler(final Bridge bridge, Supplier<@Nullable WebhookService> webhookServiceSupplier) {
        this(bridge, new RachioCloudWebhookRegistry(webhookServiceSupplier));
    }

    public RachioBridgeHandler(final Bridge bridge, RachioCloudWebhookRegistry cloudWebhookRegistry) {
        super(bridge);
        this.cloudWebhookRegistry = cloudWebhookRegistry;
        rachioApi = new RachioApi(personId);
    }

    /**
     * Initialize the bridge/cloud handler. Creates a connection to the Rachio Cloud, reads devices + zones and
     * initializes the Thing mapping.
     */
    @Override
    public void initialize() {
        String errorMessage = "";

        try {
            RachioConfiguration.ResolvedConfiguration resolvedConfiguration = resolveEffectiveConfiguration();
            thingConfig = resolvedConfiguration.configuration();
            logResolvedConfiguration(resolvedConfiguration);

            logger.debug("RachioCloud: Connecting to Rachio Cloud");
            createCloudConnection(rachioApi, RefreshReason.INITIALIZATION);
            updateProperties();

            // Pass BridgeUID to device, RachioDeviceHandler will fill DeviceUID
            Bridge bridgeThing = this.getThing();
            HashMap<String, RachioDevice> deviceList = getDevices();
            if (deviceList == null) {
                throw new RachioApiException("RachioCloud: Device list is unavailable after cloud initialization.");
            }
            for (HashMap.Entry<String, RachioDevice> de : deviceList.entrySet()) {
                RachioDevice dev = de.getValue();
                ThingUID devThingUID = new ThingUID(THING_TYPE_DEVICE, bridgeThing.getUID(), dev.getThingID());
                dev.setUID(this.getThing().getUID(), devThingUID);
                // Set DeviceUID for all zones
                HashMap<String, RachioZone> zoneList = dev.getZones();
                for (HashMap.Entry<String, RachioZone> ze : zoneList.entrySet()) {
                    RachioZone zone = ze.getValue();
                    ThingUID zoneThingUID = new ThingUID(THING_TYPE_ZONE, bridgeThing.getUID(), zone.getThingID());
                    zone.setUID(dev.getUID(), zoneThingUID);
                }
            }

            logger.info("RachioCloud: Connector initialized");
            updateStatus(ThingStatus.ONLINE);
            updateListenerManagement();
            triggerPostInitializationDiscovery();
            reconcileConfiguredWebhooks(RequestPurpose.INITIALIZATION);
        } catch (RachioApiException e) {
            errorMessage = e.toString();
            if (e.getApiResult().isResponseRateLimit()) {
                logger.warn("RachioCloud: Account is blocked due to rate limit, wait 24h and retry");
            }
        } catch (UnknownHostException e) {
            errorMessage = "Unknown Host or Internet connection down";
        } catch (RuntimeException e) {
            errorMessage = getString(e.getMessage());
        } finally {
            if (!errorMessage.isEmpty()) {
                logger.debug("RachioCloud: {}", errorMessage);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorMessage);
            }
        }
    }

    /**
     * This routine is called every time the Thing configuration has been changed
     */
    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        boolean configurationChanged = isModifyingCurrentConfig(configurationParameters);
        boolean webhookConfigurationTouched = configurationParameters.containsKey(PARAM_AUTO_CONFIGURE_WEBHOOKS)
                || configurationParameters.containsKey(PARAM_AUTO_CONFIGURE_HOSE_TIMER_WEBHOOKS)
                || configurationParameters.containsKey(PARAM_USE_CLOUD_WEBHOOK)
                || configurationParameters.containsKey(PARAM_PUBLIC_WEBHOOK_URL);
        boolean wasUsingCloudWebhook = isCloudWebhookRegistrationEnabled();
        super.handleConfigurationUpdate(configurationParameters);
        if (configurationChanged || webhookConfigurationTouched) {
            RachioConfiguration.ResolvedConfiguration resolvedConfiguration = resolveEffectiveConfiguration();
            thingConfig = resolvedConfiguration.configuration();
            logResolvedConfiguration(resolvedConfiguration);
            if (wasUsingCloudWebhook && !isCloudWebhookRegistrationEnabled()) {
                releaseCloudWebhookUrl("configuration update");
            }
            clearResolvedModernWebhookUrl();
            reconcileConfiguredWebhooks(RequestPurpose.USER_COMMAND);
            rachioStatusListeners.stream().forEach(l -> l.onConfigurationUpdated());
        }
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
        if (getThing().getStatus() == ThingStatus.ONLINE && getDevices() != null) {
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
        logger.trace("RachioCloud: refreshDeviceStatus ({})", refreshReason);

        try {
            if (!beginRefresh()) {
                logger.debug("RachioCloud: Already checking");
                return;
            }

            HashMap<String, RachioDevice> deviceList = getDevices();
            if (deviceList == null) {
                logger.debug("RachioCloud: Cloud access not initialized yet!");
                return;
            }

            RachioApi checkApi = new RachioApi(personId);
            createCloudConnection(checkApi, refreshReason);
            if (checkApi.getLastApiResult().isRateLimitBlocked()) {
                String errorCritical = "";
                errorCritical = MessageFormat.format(
                        "RachioCloud: API access blocked on update ({0} / {1}), reset at {2}",
                        checkApi.getLastApiResult().rateRemaining, checkApi.getLastApiResult().rateLimit,
                        checkApi.getLastApiResult().rateReset);
                logger.debug("{}", errorCritical);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorCritical); // shutdown
                                                                                                         // bridge+devices+zones
                return;
            }
            if (this.getThing().getStatus() != ThingStatus.ONLINE) {
                logger.debug("RachioCloud: Bridge is ONLINE");
                updateStatus(ThingStatus.ONLINE);
            }

            HashMap<String, RachioDevice> checkDevList = checkApi.getDevices();
            for (HashMap.Entry<String, RachioDevice> de : checkDevList.entrySet()) {
                RachioDevice checkDev = de.getValue();
                RachioDevice dev = deviceList.get(checkDev.id);
                if (dev == null) {
                    logger.info("RachioCloud: New device detected: {} - {}", checkDev.id, checkDev.name);
                } else {
                    reconcileDeviceAndZones(dev, checkDev, refreshReason);
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
            }
            endRefresh();
        }
    }

    void reconcileDeviceAndZones(RachioDevice dev, RachioDevice checkDev, RefreshReason refreshReason) {
        RachioDeviceHandler deviceHandler = dev.getThingHandler();
        if (!dev.compare(checkDev)) {
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

        HashMap<String, RachioZone> zoneList = dev.getZones();
        HashMap<String, RachioZone> checkZoneList = checkDev.getZones();
        for (HashMap.Entry<String, RachioZone> ze : checkZoneList.entrySet()) {
            RachioZone checkZone = ze.getValue();
            RachioZone zone = zoneList.get(checkZone.id);
            if (zone == null) {
                logger.debug("RachioCloud: New zone detected: {} - {}", checkDev.id, checkZone.name);
            } else if (!zone.compare(checkZone)) {
                logger.trace("RachioCloud: Update status for zone {}", zone.name);
                RachioZoneHandler zoneHandler = zone.getThingHandler();
                if (zoneHandler != null) {
                    zoneHandler.onThingStateChanged(null, checkZone);
                } else {
                    notifyThingStateChanged(null, checkZone);
                }
            } else {
                logger.trace("RachioCloud: Zone {} was not updated.", checkZone.id);
                RachioZoneHandler zoneHandler = zone.getThingHandler();
                if (zoneHandler != null) {
                    zoneHandler.refreshThingStatusAfterSuccessfulCommunication();
                }
            }
        }

        // Sync the zoneList with the new state
        zoneList.keySet().retainAll(checkZoneList.keySet());
        for (HashMap.Entry<String, RachioZone> entry : checkZoneList.entrySet()) {
            if (!zoneList.containsKey(entry.getKey())) {
                zoneList.put(entry.getKey(), entry.getValue());
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

    @Override
    public void shutdown() {
        logger.info("RachioCloud: Shutting down");
        disposed = true;
        releaseCloudWebhookUrl("bridge shutdown");
        super.shutdown();
    }

    /**
     * Create a new Rachio cloud service connection. If a connection already exists, it will be replaced.
     *
     * @throws RachioApiException if there is an error while authenticating to the service
     */
    private void createCloudConnection(RachioApi api, RefreshReason refreshReason)
            throws RachioApiException, UnknownHostException {
        if (thingConfig.apikey.isEmpty()) {
            throw new RachioApiException(
                    "RachioCloud: Unable to connect to Rachio Cloud: API key is not set; configure the Rachio Cloud Connector Thing.");
        }

        // initialize API access, may throw an exception
        api.initialize(thingConfig.apikey, this.getThing().getUID(), getRefreshPriority(refreshReason),
                getRequestPurpose(refreshReason));
        personId = api.getPersonId();
    }

    PRIORITY getRefreshPriority(RefreshReason refreshReason) {
        switch (refreshReason) {
            case SCHEDULED_POLL:
            case WEBHOOK_RECONCILIATION:
            case INITIALIZATION:
            case MANUAL:
            default:
                return PRIORITY.MED;
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

    public Optional<RachioProperty> findPropertyByEntity(String entityId, String entityType) throws RachioApiException {
        return rachioApi.findPropertyByEntity(entityId, entityType);
    }

    public Optional<RachioProperty> findPropertyForLocation(String locationId) throws RachioApiException {
        return rachioApi.findPropertyForLocation(locationId);
    }

    public Optional<RachioProperty> findPropertyForBaseStation(String baseStationId) throws RachioApiException {
        return rachioApi.findPropertyForBaseStation(baseStationId);
    }

    public Optional<RachioProperty> findPropertyForLightingArea(String lightingAreaId) throws RachioApiException {
        return rachioApi.findPropertyForLightingArea(lightingAreaId);
    }

    public List<RachioBaseStation> listBaseStations() throws RachioApiException {
        return rachioApi.listBaseStations(personId);
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

    public List<RachioValveProgram> listValveProgramsForBaseStation(String baseStationId) throws RachioApiException {
        return rachioApi.listValveProgramsV2ByBaseStation(baseStationId);
    }

    public List<RachioValveProgram> listValveProgramsForValve(String valveId) throws RachioApiException {
        try {
            return rachioApi.listValveProgramsV2ByValve(valveId);
        } catch (RachioApiException e) {
            logger.debug(
                    "Unable to load Smart Hose Timer Program V2 list for valve '{}'; trying legacy program list: {}",
                    valveId, e.getMessage());
            return rachioApi.listValvePrograms(valveId);
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
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        LocalDate end = today.plusDays(getHoseSummaryLookaheadDays());
        LocalDate start = today.minusDays(getHoseSummaryLookbackDays());
        return rachioApi.getValveDayViews(valveId, start, end);
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
    public Boolean getClearAllCallbacks() {
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

    public boolean isWebhookAutoConfigureEnabled() {
        return thingConfig.autoConfigureWebhooks;
    }

    public boolean isCloudWebhookEnabled() {
        return thingConfig.useCloudWebhook;
    }

    boolean isDisposed() {
        return disposed;
    }

    public String getPublicWebhookUrl() {
        return thingConfig.publicWebhookUrl;
    }

    //
    // ------ Stuff used by other classes
    //

    /**
     * Get the list of discovered devices (those retrieved from the Rachio Cloud)
     *
     * @return HashMap of RachioDevice
     */
    public @Nullable HashMap<String, RachioDevice> getDevices() {
        try {
            return rachioApi.getDevices();
        } catch (RuntimeException e) {
            logger.debug("Unable to retrieve device list", e);
        }
        return null;
    }

    @Nullable
    RachioDevice findIrrigationController(String controllerId) {
        if (controllerId.isBlank()) {
            return null;
        }
        HashMap<String, RachioDevice> devices = getDevices();
        if (devices == null) {
            return null;
        }
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
        RachioWebhookMode mode = getWebhookMode(RachioWebhookResourceType.IRRIGATION_CONTROLLER);
        switch (mode) {
            case LEGACY_NOTIFICATION_SERVICE:
                updateWebhookMode("legacy");
                updateWebhookRegistrationAttempt();
                rachioApi.registerLegacyNotificationWebHook(deviceId, getCallbackUrl(), getCallbackUsername(),
                        getCallbackPassword(), getExternalId(), getClearAllCallbacks(), requestPurpose);
                updateWebhookRegistrationState("registered");
                break;
            case WEBHOOK_SERVICE:
                String webhookUrl = getModernWebhookUrlForRegistration();
                if (webhookUrl.isBlank()) {
                    logger.debug(
                            "RachioCloud: Modern controller webhook registration is enabled but no public webhook URL is available; polling remains active.");
                    return;
                }
                updateWebhookRegistrationAttempt();
                rachioApi.registerWebHook(deviceId, webhookUrl, "", "", getExternalId(), getClearAllCallbacks(),
                        requestPurpose);
                updateWebhookRegistrationState("registered");
                break;
            case DISABLED:
                logger.debug("RachioCloud: Controller webhook registration disabled; polling remains active.");
                updateWebhookRegistrationState("disabled");
                break;
        }
    }

    public void registerValveWebHook(String valveId) throws RachioApiException {
        registerValveWebHook(valveId, RequestPurpose.BACKGROUND_REFRESH);
    }

    public void registerValveWebHook(String valveId, RequestPurpose requestPurpose) throws RachioApiException {
        RachioWebhookMode mode = getWebhookMode(RachioWebhookResourceType.VALVE);
        if (mode == RachioWebhookMode.WEBHOOK_SERVICE) {
            String webhookUrl = getModernWebhookUrlForRegistration();
            if (webhookUrl.isBlank()) {
                logger.debug(
                        "RachioCloud: Smart Hose Timer valve webhook registration is enabled but no public webhook URL is available; polling remains active.");
                return;
            }
            RachioWebhookTarget target = new RachioWebhookTarget(valveId, RachioWebhookResourceType.VALVE,
                    List.of(EVENT_VALVE_RUN_START, EVENT_VALVE_RUN_END));
            logger.debug("RachioCloud: Smart Hose Timer valve webhook registration enabled for target '{}'",
                    target.describe());
            rachioApi.registerWebHookTarget(target, webhookUrl, "", "", getExternalId(), getClearAllCallbacks(),
                    requestPurpose);
        } else {
            logger.debug("RachioCloud: Valve webhook registration disabled; Smart Hose Timer polling remains active.");
        }
    }

    public void registerValveProgramWebHook(String programId) throws RachioApiException {
        registerValveProgramWebHook(programId, RequestPurpose.BACKGROUND_REFRESH);
    }

    public void registerValveProgramWebHook(String programId, RequestPurpose requestPurpose) throws RachioApiException {
        RachioWebhookMode mode = getWebhookMode(RachioWebhookResourceType.PROGRAM);
        if (mode == RachioWebhookMode.WEBHOOK_SERVICE) {
            String webhookUrl = getModernWebhookUrlForRegistration();
            if (webhookUrl.isBlank()) {
                logger.debug(
                        "RachioCloud: Smart Hose Timer program webhook registration is enabled but no public webhook URL is available; polling remains active.");
                return;
            }
            RachioWebhookTarget target = new RachioWebhookTarget(programId, RachioWebhookResourceType.PROGRAM,
                    List.of(EVENT_PROGRAM_RAIN_SKIP_CREATED, EVENT_PROGRAM_RAIN_SKIP_CANCELED));
            logger.debug("RachioCloud: Smart Hose Timer program webhook registration enabled for target '{}'",
                    target.describe());
            rachioApi.registerWebHookTarget(target, webhookUrl, "", "", getExternalId(), getClearAllCallbacks(),
                    requestPurpose);
        } else {
            logger.debug(
                    "RachioCloud: Valve program webhook registration disabled; Smart Hose Timer polling remains active.");
        }
    }

    RachioWebhookMode getWebhookMode(RachioWebhookResourceType resourceType) {
        return selectWebhookMode(resourceType, !getCallbackUrl().isBlank(), thingConfig.autoConfigureWebhooks,
                thingConfig.autoConfigureHoseTimerWebhooks,
                thingConfig.useCloudWebhook || !thingConfig.publicWebhookUrl.isBlank());
    }

    RachioWebhookMode getActiveIrrigationWebhookProcessingMode() {
        return selectWebhookMode(RachioWebhookResourceType.IRRIGATION_CONTROLLER, !getCallbackUrl().isBlank(),
                thingConfig.autoConfigureWebhooks, isModernIrrigationWebhookUrlAvailableForProcessing());
    }

    private boolean isModernIrrigationWebhookUrlAvailableForProcessing() {
        if (!thingConfig.autoConfigureWebhooks) {
            return false;
        }
        if (!thingConfig.publicWebhookUrl.isBlank()) {
            try {
                normalizeModernWebhookUrl(thingConfig.publicWebhookUrl);
                return true;
            } catch (RachioApiException e) {
                return false;
            }
        }
        String cachedUrl = resolvedModernWebhookUrl;
        return thingConfig.useCloudWebhook && cachedUrl != null && !cachedUrl.isBlank();
    }

    static RachioWebhookMode selectWebhookMode(RachioWebhookResourceType resourceType, boolean callbackConfigured) {
        return selectWebhookMode(resourceType, callbackConfigured, false, false, false);
    }

    static RachioWebhookMode selectWebhookMode(RachioWebhookResourceType resourceType, boolean callbackConfigured,
            boolean autoConfigureWebhooks, boolean modernWebhookUrlConfigured) {
        return selectWebhookMode(resourceType, callbackConfigured, autoConfigureWebhooks, false,
                modernWebhookUrlConfigured);
    }

    static RachioWebhookMode selectWebhookMode(RachioWebhookResourceType resourceType, boolean callbackConfigured,
            boolean autoConfigureWebhooks, boolean autoConfigureHoseTimerWebhooks, boolean modernWebhookUrlConfigured) {
        switch (resourceType) {
            case IRRIGATION_CONTROLLER:
                if (autoConfigureWebhooks && modernWebhookUrlConfigured) {
                    return RachioWebhookMode.WEBHOOK_SERVICE;
                }
                return callbackConfigured ? RachioWebhookMode.LEGACY_NOTIFICATION_SERVICE : RachioWebhookMode.DISABLED;
            case VALVE:
            case PROGRAM:
                return autoConfigureWebhooks && autoConfigureHoseTimerWebhooks && modernWebhookUrlConfigured
                        ? RachioWebhookMode.WEBHOOK_SERVICE
                        : RachioWebhookMode.DISABLED;
            case LIGHTING_CONTROLLER:
            case LIGHTING_ZONE:
            case LIGHTING_SCENE:
            case LIGHTING_PROGRAM:
            case UNKNOWN:
            default:
                return RachioWebhookMode.DISABLED;
        }
    }

    public void onWebhookServiceChanged() {
        if (disposed) {
            return;
        }
        if (!thingConfig.autoConfigureWebhooks || !thingConfig.useCloudWebhook) {
            return;
        }
        logger.debug("RachioCloud: openHAB core WebhookService availability changed; retrying webhook reconciliation.");
        cloudWebhookRegistry.invalidateCachedWebhook(resolvedCloudWebhookGeneration);
        clearResolvedModernWebhookUrl();
        reconcileConfiguredWebhooks(RequestPurpose.BACKGROUND_REFRESH);
        reconcileSmartHoseTimerWebhooks(RequestPurpose.BACKGROUND_REFRESH);
    }

    private void reconcileSmartHoseTimerWebhooks(RequestPurpose requestPurpose) {
        if (getWebhookMode(RachioWebhookResourceType.VALVE) != RachioWebhookMode.WEBHOOK_SERVICE
                && getWebhookMode(RachioWebhookResourceType.PROGRAM) != RachioWebhookMode.WEBHOOK_SERVICE) {
            return;
        }

        for (RachioStatusListener listener : rachioStatusListeners) {
            if (listener instanceof RachioValveHandler valveHandler) {
                valveHandler.renewWebhookRegistration(requestPurpose);
            } else if (listener instanceof RachioValveProgramHandler programHandler) {
                programHandler.renewWebhookRegistration(requestPurpose);
            }
        }
    }

    private void reconcileConfiguredWebhooks(RequestPurpose requestPurpose) {
        if (disposed) {
            return;
        }
        if (!thingConfig.autoConfigureWebhooks) {
            releaseCloudWebhookUrl("modern webhook registration disabled");
            updateWebhookMode("disabled");
            updateWebhookRegistrationState("disabled");
            return;
        }
        if (getWebhookMode(RachioWebhookResourceType.IRRIGATION_CONTROLLER) != RachioWebhookMode.WEBHOOK_SERVICE) {
            releaseCloudWebhookUrl("modern webhook mode inactive");
            updateWebhookRegistrationState("disabled");
            return;
        }

        HashMap<String, RachioDevice> devices = getDevices();
        if (devices == null || devices.isEmpty()) {
            updateWebhookRegistrationState("waiting for controller resources");
            return;
        }

        for (RachioDevice device : devices.values()) {
            try {
                registerWebHook(device.id, requestPurpose);
            } catch (RachioApiThrottledException e) {
                updateWebhookRegistrationState("deferred by local API budget guard");
                logger.info(
                        "Modern webhook registration for controller '{}' deferred because the local Rachio API budget guard is active; polling remains active.",
                        device.id);
                return;
            } catch (RachioApiException e) {
                updateWebhookRegistrationState("registration failed: " + e.getClass().getSimpleName());
                logger.warn("Modern webhook registration for controller '{}' failed; polling remains active, cause={}",
                        device.id, e.getClass().getSimpleName());
            } catch (RuntimeException e) {
                updateWebhookRegistrationState("registration failed: " + e.getClass().getSimpleName());
                logger.warn("Modern webhook registration for controller '{}' failed; polling remains active, cause={}",
                        device.id, e.getClass().getSimpleName());
            }
        }
    }

    String getModernWebhookUrlForRegistration() {
        if (!thingConfig.autoConfigureWebhooks) {
            releaseCloudWebhookUrl("modern webhook registration disabled");
            updateWebhookMode("disabled");
            updateWebhookRegistrationState("disabled");
            return "";
        }
        if (thingConfig.useCloudWebhook) {
            updateWebhookMode("cloud");
            return getCloudWebhookUrlForRegistration();
        }
        if (!thingConfig.publicWebhookUrl.isBlank()) {
            releaseCloudWebhookUrl("manual webhook URL configured");
            updateWebhookMode("manual");
            return getManualWebhookUrlForRegistration();
        }

        releaseCloudWebhookUrl("no public webhook URL configured");
        updateWebhookMode("disabled");
        updateWebhookRegistrationState("no public webhook URL configured");
        return "";
    }

    private String getManualWebhookUrlForRegistration() {
        String cachedUrl = resolvedModernWebhookUrl;
        if (cachedUrl != null && !cachedUrl.isBlank()) {
            return cachedUrl;
        }

        try {
            String webhookUrl = normalizeModernWebhookUrl(thingConfig.publicWebhookUrl);
            resolvedModernWebhookUrl = webhookUrl;
            updateWebhookRegistrationState("manual URL ready");
            updateProperties();
            return webhookUrl;
        } catch (RachioApiException e) {
            updateWebhookRegistrationState("invalid publicWebhookUrl");
            logger.warn("Modern Rachio webhook registration disabled because publicWebhookUrl is invalid, cause={}",
                    e.getClass().getSimpleName());
            return "";
        }
    }

    private String getCloudWebhookUrlForRegistration() {
        String cachedUrl = resolvedModernWebhookUrl;
        if (cachedUrl != null && !cachedUrl.isBlank()) {
            return cachedUrl;
        }

        updateWebhookRegistrationState("requesting cloud webhook URL");
        try {
            CloudWebhookLease webhook = cloudWebhookRegistry.acquire(getCloudWebhookConsumerId());
            String webhookUrl = normalizeModernWebhookUrl(webhook.url());
            resolvedModernWebhookUrl = webhookUrl;
            resolvedCloudWebhookGeneration = webhook.generation();
            updateWebhookRegistrationState("cloud URL ready");
            scheduleCloudWebhookRefresh(webhook.expiresAt());
            updateProperties();
            return webhookUrl;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            updateWebhookRegistrationState("cloud webhook URL request interrupted");
            logger.warn("Unable to request openHAB Cloud webhook URL for Rachio; polling remains active, cause={}",
                    e.getClass().getSimpleName());
        } catch (CloudWebhookException e) {
            updateWebhookRegistrationState("cloud webhook URL unavailable: " + e.diagnosticCause());
            logger.warn("Unable to request openHAB Cloud webhook URL for Rachio; polling remains active, cause={}",
                    e.diagnosticCause());
        } catch (RachioApiException e) {
            updateWebhookRegistrationState("cloud webhook URL unavailable: " + e.getClass().getSimpleName());
            logger.warn("Unable to request openHAB Cloud webhook URL for Rachio; polling remains active, cause={}",
                    e.getClass().getSimpleName());
        }
        return "";
    }

    private String normalizeModernWebhookUrl(String webhookUrl) throws RachioApiException {
        try {
            URI uri = new URI(webhookUrl.trim());
            if (!uri.isAbsolute() || uri.getRawAuthority() == null || uri.getHost() == null) {
                throw new RachioApiException("Invalid webhook URL format: expected an absolute URL with a host.");
            }
            if (!"https".equalsIgnoreCase(uri.getScheme())) {
                throw new RachioApiException("Modern webhook URL must use HTTPS.");
            }
            if (uri.getRawUserInfo() != null) {
                throw new RachioApiException("Modern webhook URL must not contain URL userinfo credentials.");
            }
            return uri.toASCIIString();
        } catch (URISyntaxException e) {
            throw new RachioApiException("Invalid webhook URL format: " + e.getReason());
        }
    }

    private synchronized void scheduleCloudWebhookRefresh(Instant expiresAt) {
        cancelCloudWebhookRefresh("new cloud webhook URL");
        long delaySeconds = Duration.between(Instant.now(), expiresAt.minus(CLOUD_WEBHOOK_REFRESH_SAFETY_WINDOW))
                .getSeconds();
        delaySeconds = Math.max(MIN_CLOUD_WEBHOOK_REFRESH_DELAY_SECONDS, delaySeconds);
        cloudWebhookRefreshJob = scheduler.schedule(this::onWebhookServiceChanged, delaySeconds, TimeUnit.SECONDS);
        logger.debug("RachioCloud: Scheduled openHAB Cloud webhook URL refresh in {} seconds", delaySeconds);
    }

    private synchronized void cancelCloudWebhookRefresh(String reason) {
        ScheduledFuture<?> refreshJob = cloudWebhookRefreshJob;
        if (refreshJob != null) {
            logger.debug("RachioCloud: Cancelling openHAB Cloud webhook URL refresh ({})", reason);
            refreshJob.cancel(true);
            cloudWebhookRefreshJob = null;
        }
    }

    private void clearResolvedModernWebhookUrl() {
        resolvedCloudWebhookGeneration = NO_CLOUD_WEBHOOK_GENERATION;
        if (resolvedModernWebhookUrl == null) {
            return;
        }
        resolvedModernWebhookUrl = null;
        updateProperties();
    }

    private void releaseCloudWebhookUrl(String reason) {
        cancelCloudWebhookRefresh(reason);
        cloudWebhookRegistry.release(getCloudWebhookConsumerId());
        clearResolvedModernWebhookUrl();
    }

    private boolean isCloudWebhookRegistrationEnabled() {
        return thingConfig.autoConfigureWebhooks && thingConfig.useCloudWebhook;
    }

    private String getCloudWebhookConsumerId() {
        return getThing().getUID().getAsString();
    }

    private void updateWebhookMode(String mode) {
        if (webhookMode.equals(mode)) {
            return;
        }
        webhookMode = mode;
        updateProperties();
    }

    private void updateWebhookRegistrationAttempt() {
        lastWebhookRegistrationAttempt = Instant.now().toString();
        updateProperties();
    }

    private void updateWebhookRegistrationState(String state) {
        if (webhookRegistrationState.equals(state)) {
            return;
        }
        webhookRegistrationState = state;
        updateProperties();
    }

    private void recordWebhookEvent(RachioEventGsonDTO event) {
        lastWebhookEventTimestamp = Instant.now().toString();
        lastWebhookEventType = getEventTypeForProperties(event);
        updateProperties();
    }

    private String getEventTypeForProperties(RachioEventGsonDTO event) {
        if (!event.eventType.isBlank()) {
            return event.eventType;
        }
        if (!event.type.isBlank() && !event.subType.isBlank()) {
            return event.type + "." + event.subType;
        }
        if (!event.type.isBlank()) {
            return event.type;
        }
        return "unknown";
    }

    /**
     * Handle inbound WebHook event (dispatch to device handler)
     *
     * @param event inbound Rachio webhook event
     * @return true if the event was dispatched to a matching handler
     */
    public boolean webHookEvent(RachioEventGsonDTO event) {
        if (isModernWebhookEvent(event)) {
            RachioWebhookResourceType resourceType = RachioWebhookResourceType.fromApiValue(event.resourceType);
            if (resourceType == RachioWebhookResourceType.IRRIGATION_CONTROLLER) {
                RachioWebhookMode mode = getActiveIrrigationWebhookProcessingMode();
                if (mode != RachioWebhookMode.WEBHOOK_SERVICE) {
                    logger.debug(
                            "RachioCloud: Ignoring modern WebhookService irrigation event eventType='{}', resourceType='{}' because active irrigation webhook processing mode is {}; polling remains active",
                            event.eventType, event.resourceType, mode);
                    return true;
                }
                logger.debug(
                        "RachioCloud: Processing modern WebhookService irrigation event eventType='{}', resourceType='{}' because active irrigation webhook processing mode is {}",
                        event.eventType, event.resourceType, mode);
            } else if (resourceType == RachioWebhookResourceType.VALVE
                    || resourceType == RachioWebhookResourceType.PROGRAM) {
                RachioWebhookMode mode = getWebhookMode(resourceType);
                if (mode != RachioWebhookMode.WEBHOOK_SERVICE) {
                    logger.debug(
                            "RachioCloud: Ignoring Smart Hose Timer WebhookService event eventType='{}', resourceType='{}' because active webhook processing mode is {}; polling remains active",
                            event.eventType, event.resourceType, mode);
                    return true;
                }
                logger.debug(
                        "RachioCloud: Processing Smart Hose Timer WebhookService event eventType='{}', resourceType='{}' because active webhook processing mode is {}",
                        event.eventType, event.resourceType, mode);
            }
            boolean dispatched = dispatchWebHookEvent(event);
            if (!dispatched) {
                logger.debug(
                        "RachioCloud: Modern webhook event eventType='{}', resourceType='{}', resourceIdPresent={} was not directly handled; reconciliation refresh remains active",
                        event.eventType, event.resourceType, !event.resourceId.isBlank());
            }
            reconcileAfterModernWebhookEvent(event);
            return true;
        }
        boolean dispatched = dispatchWebHookEvent(event);
        return dispatched;
    }

    private void reconcileAfterModernWebhookEvent(RachioEventGsonDTO event) {
        RachioWebhookResourceType resourceType = RachioWebhookResourceType.fromApiValue(event.resourceType);
        if (resourceType == RachioWebhookResourceType.IRRIGATION_CONTROLLER) {
            logger.debug(
                    "RachioCloud: Refreshing essential controller state after modern webhook event eventType='{}', resourceType='{}'",
                    event.eventType, event.resourceType);
            refreshDeviceStatus(RefreshReason.WEBHOOK_RECONCILIATION);
            return;
        }
        if (resourceType == RachioWebhookResourceType.VALVE || resourceType == RachioWebhookResourceType.PROGRAM) {
            logger.debug(
                    "RachioCloud: Smart Hose Timer webhook event eventType='{}', resourceType='{}' handled or acknowledged; Thing polling remains available as reconciliation fallback",
                    event.eventType, event.resourceType);
            return;
        }
        logger.debug(
                "RachioCloud: Modern webhook event eventType='{}', resourceType='{}' acknowledged; polling remains available as fallback",
                event.eventType, event.resourceType);
    }

    private boolean dispatchWebHookEvent(RachioEventGsonDTO event) {
        try {
            recordWebhookEvent(event);
            return RachioWebhookDispatcher.createDefault(this).dispatch(event);
        } catch (RuntimeException e) {
            logger.debug("RachioCloud: Unable to process event {}.{} for device {}", event.category, event.type,
                    event.deviceId, e);
        }
        return false;
    }

    private boolean isModernWebhookEvent(RachioEventGsonDTO event) {
        return !event.eventType.isBlank() && !event.resourceType.isBlank();
    }

    public boolean legacyWebHookEvent(RachioEventGsonDTO event) {
        if (findIrrigationController(event.deviceId) == null) {
            logger.warn("RachioCloud: Rejecting legacy NotificationService event for unknown controller '{}'",
                    event.deviceId);
            return false;
        }
        RachioWebhookMode mode = getActiveIrrigationWebhookProcessingMode();
        if (mode == RachioWebhookMode.WEBHOOK_SERVICE) {
            logger.debug(
                    "RachioCloud: Ignoring legacy NotificationService event {}.{} because modern WebhookService mode is active for irrigation events",
                    event.type, event.subType);
            return true;
        }
        if (mode == RachioWebhookMode.DISABLED) {
            logger.debug(
                    "RachioCloud: Ignoring legacy NotificationService event {}.{} because irrigation webhook processing is disabled; polling remains active",
                    event.type, event.subType);
            return true;
        }
        logger.debug(
                "RachioCloud: Dispatching validated legacy NotificationService event {}.{} before reconciliation because active irrigation webhook processing mode is {}",
                event.type, event.subType, mode);
        boolean dispatched = dispatchWebHookEvent(event);
        if (!dispatched) {
            logger.debug(
                    "RachioCloud: Legacy NotificationService event {}.{} was not directly handled; reconciliation refresh remains active",
                    event.type, event.subType);
        }
        logger.debug("RachioCloud: Refreshing essential controller state after legacy NotificationService event {}.{}",
                event.type, event.subType);
        refreshDeviceStatus(RefreshReason.WEBHOOK_RECONCILIATION);
        return true;
    }

    public @Nullable String getExternalId() {
        return rachioApi.getExternalId();
    }

    @Override
    public Collection<ConfigStatusMessage> getConfigStatus() {
        Collection<ConfigStatusMessage> configStatusMessages = new ArrayList<>();

        RachioConfiguration config = resolveEffectiveConfiguration().configuration();

        if (config.apikey.isEmpty()) {
            configStatusMessages.add(ConfigStatusMessage.Builder.error(PARAM_APIKEY)
                    .withMessageKeySuffix("ERROR: No/invalid APIKEY in Cloud Connector configuration!")
                    .withArguments(PARAM_APIKEY).build());
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

    private void updateProperties() {
        Map<String, String> properties = new HashMap<>(rachioApi.fillProperties());
        properties.put(PROPERTY_WEBHOOK_MODE, webhookMode);
        properties.put(PROPERTY_WEBHOOK_REGISTRATION_STATE, webhookRegistrationState);
        properties.put(PROPERTY_LAST_WEBHOOK_REGISTRATION_ATTEMPT, lastWebhookRegistrationAttempt);
        properties.put(PROPERTY_LAST_WEBHOOK_EVENT_TIMESTAMP, lastWebhookEventTimestamp);
        properties.put(PROPERTY_LAST_WEBHOOK_EVENT_TYPE, lastWebhookEventType);
        updateProperties(properties);
    }

    @Override
    protected int getPollingIntervalSeconds() {
        return getPollingInterval();
    }

    @Override
    protected void runScheduledRefresh() {
        refreshDeviceStatus(RefreshReason.SCHEDULED_POLL);
    }

    @Override
    public synchronized void dispose() {
        logger.debug("RachioCloud: Disposing handler");
        disposed = true;
        releaseCloudWebhookUrl("bridge disposal");
        super.dispose();
    }
}
