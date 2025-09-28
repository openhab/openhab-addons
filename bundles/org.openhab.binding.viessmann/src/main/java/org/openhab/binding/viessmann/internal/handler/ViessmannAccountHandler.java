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
package org.openhab.binding.viessmann.internal.handler;

import static org.openhab.binding.viessmann.internal.ViessmannBindingConstants.*;

import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.viessmann.internal.ViessmannAccountDiscoveryService;
import org.openhab.binding.viessmann.internal.api.ViessmannApi;
import org.openhab.binding.viessmann.internal.api.ViessmannCommunicationException;
import org.openhab.binding.viessmann.internal.config.AccountConfiguration;
import org.openhab.binding.viessmann.internal.dto.device.DeviceDTO;
import org.openhab.binding.viessmann.internal.dto.device.DeviceData;
import org.openhab.binding.viessmann.internal.dto.events.EventsDTO;
import org.openhab.binding.viessmann.internal.dto.features.FeatureDataDTO;
import org.openhab.binding.viessmann.internal.dto.features.FeaturesDTO;
import org.openhab.binding.viessmann.internal.dto.installation.Data;
import org.openhab.binding.viessmann.internal.dto.installation.Gateway;
import org.openhab.binding.viessmann.internal.dto.installation.InstallationDTO;
import org.openhab.binding.viessmann.internal.interfaces.ApiInterface;
import org.openhab.binding.viessmann.internal.util.ViessmannUtil;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.link.ItemChannelLink;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.util.ThingHandlerHelper;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * The {@link ViessmannAccountHandler} is responsible for handling the api connection.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class ViessmannAccountHandler extends BaseBridgeHandler implements ApiInterface {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Storage<String> stateStorage;

    private static final String STORED_API_CALLS = "apiCalls";

    private final HttpClient httpClient;
    private final @Nullable String callbackUrl;

    private @NonNullByDefault({}) ViessmannApi api;

    protected @Nullable ViessmannAccountDiscoveryService discoveryService;

    private int apiCalls;
    private int pollingInterval = 90;
    private boolean countReset = true;

    private @Nullable ScheduledFuture<?> viessmannBridgePollingJob;
    private @Nullable ScheduledFuture<?> viessmannBridgeLimitJob;

    public @Nullable List<DeviceData> devicesData;

    protected final List<String> gatewaysList = new ArrayList<>();
    protected final List<String> registeredErrorPollingGateway = new ArrayList<>();
    protected final Map<String, Integer> serialToInstallationId = new HashMap<>();
    protected final Map<String, String> serialToGatewayType = new HashMap<>();
    protected final Map<String, Integer> registeredErrorPollingInterval = new HashMap<>();

    private final ItemChannelLinkRegistry linkRegistry;

    private AccountConfiguration config = new AccountConfiguration();

    public ViessmannAccountHandler(Bridge bridge, Storage<String> stateStorage, HttpClient httpClient,
            @Nullable String callbackUrl, ItemChannelLinkRegistry linkRegistry) {
        super(bridge);
        this.stateStorage = stateStorage;
        this.httpClient = httpClient;
        this.callbackUrl = callbackUrl;
        this.linkRegistry = linkRegistry;
    }

    public void setInstallationGatewayId(@Nullable String newInstallation, @Nullable String newGateway) {
    }

    public void addRegisteredErrorPollingGateway(String serial, Integer pollingIntervalErrors) {
        if (!registeredErrorPollingGateway.contains(serial)) {
            registeredErrorPollingGateway.add(serial);
        }
        registeredErrorPollingInterval.put(serial, pollingIntervalErrors);
    }

    public void removeRegisteredErrorPollingGateway(String serial) {
        registeredErrorPollingGateway.remove(serial);
        registeredErrorPollingInterval.remove(serial);
    }

    /**
     * get the gateways list (needed for discovery)
     *
     * @return a list of the all gateways
     */
    public List<String> getGatewaysList() {
        // return a copy of the list, so we don't run into concurrency problems
        return new ArrayList<>(gatewaysList);
    }

    /**
     * get the gateways to installation map (needed for discovery)
     *
     * @return a map of the all gateways with installationId
     */
    public Map<String, Integer> getSerialToInstallationId() {
        // return a copy of the map, so we don't run into concurrency problems
        return new HashMap<>(serialToInstallationId);
    }

    /**
     * get the gateways to gatewayType map (needed for discovery)
     *
     * @return a map of the all gateways with gatewayType
     */
    public Map<String, String> getSerialToGatewayType() {
        // return a copy of the map, so we don't run into concurrency problems
        return new HashMap<>(serialToGatewayType);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // nothing to handle here
    }

    @Override
    public void dispose() {
        stopViessmannBridgePolling();
        stopViessmannErrorsPolling();
        stopViessmannBridgeLimitReset();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(ViessmannAccountDiscoveryService.class);
    }

    @Override
    public void initialize() {
        logger.debug("Initialize Viessmann Account service");

        this.config = getConfigAs(AccountConfiguration.class);
        String storedApiCalls = this.stateStorage.get(STORED_API_CALLS);
        if (storedApiCalls != null) {
            apiCalls = Integer.parseInt(storedApiCalls);
        } else {
            apiCalls = 0;
        }

        api = new ViessmannApi(this.config.apiKey, httpClient, this.config.user, this.config.password,
                this.config.installationId, this.config.gatewaySerial, callbackUrl);
        api.createOAuthClientService(this);

        if (api.doAuthorize()) {
            migrateChannelIds();

            getAllGateways();
            if (!gatewaysList.isEmpty()) {
                updateBridgeStatus(ThingStatus.ONLINE);
                updateProperty("pollingInterval [s]", String.valueOf(pollingInterval));
                startViessmannBridgePolling(pollingInterval, 1);
            }
        }
    }

    private void migrateChannelIds() {
        List<Channel> oldChannels = thing.getChannels();
        List<Channel> newChannels = new ArrayList<>(oldChannels.size());

        Map<ChannelUID, ChannelUID> renameMap = new LinkedHashMap<>();

        for (Channel channel : oldChannels) {
            String oldId = channel.getUID().getId();
            String newId = ViessmannUtil.camelToHyphen(oldId);
            boolean updateChannelType = false;
            String channelLabel = channel.getLabel();
            String channelType = ViessmannUtil
                    .camelToHyphen(Objects.requireNonNull(channel.getChannelTypeUID()).toString());
            channelType = channelType.replace(BINDING_ID + ":", "");

            if (channelType.contains("type-")) {
                channelType = channelType.replace("type-", "");
                updateChannelType = true;
            }

            if (!newId.equals(oldId) || updateChannelType) {
                logger.info("Migrating channel '{}' -> '{}'", oldId, newId);

                ChannelUID oldUid = channel.getUID();
                ChannelUID newUid = new ChannelUID(thing.getUID(), newId);

                ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID, channelType);
                if (channelLabel != null) {
                    Channel newChannel = ChannelBuilder.create(newUid, channel.getAcceptedItemType())
                            .withLabel(channelLabel).withType(channelTypeUID).withProperties(channel.getProperties())
                            .build();

                    newChannels.add(newChannel);
                    renameMap.put(oldUid, newUid);
                }
            }
        }

        if (renameMap.isEmpty()) {
            return;
        }

        updateThing(editThing().withChannels(newChannels).build());

        if (linkRegistry != null) {
            for (Map.Entry<ChannelUID, ChannelUID> e : renameMap.entrySet()) {
                ChannelUID oldUid = e.getKey();
                ChannelUID newUid = e.getValue();

                Collection<ItemChannelLink> links = new ArrayList<>(linkRegistry.getLinks(oldUid));

                for (ItemChannelLink link : links) {
                    String item = link.getItemName();
                    try {
                        linkRegistry.remove(link.getUID());
                    } catch (Exception ex) {
                        logger.warn("Could not remove old link {} -> {}: {}", item, oldUid, ex.getMessage());
                    }

                    linkRegistry.add(new ItemChannelLink(item, newUid));
                    logger.info("Re-linked item '{}' from '{}' to '{}'", item, oldUid.getId(), newUid.getId());
                }
            }
        } else {
            logger.warn("ItemChannelLinkRegistry not available – cannot migrate item links.");
        }
    }

    private void getAllGateways() {
        logger.trace("Loading Gateway List from Viessmann Account");
        try {
            InstallationDTO installations = api.getInstallationsAndGateways(this);
            countApiCalls();
            if (installations != null) {
                List<Data> installationsData = installations.data;
                for (Data installation : installationsData) {
                    Integer installationId = installation.id;
                    List<Gateway> gateways = installation.gateways;
                    for (Gateway gateway : gateways) {
                        String gatewayType = gateway.gatewayType;
                        String serial = gateway.serial;
                        if (!gatewaysList.contains(serial)) {
                            gatewaysList.add(serial);
                            serialToInstallationId.put(serial, installationId);
                            serialToGatewayType.put(serial, gatewayType);
                        }
                    }
                }
            }
        } catch (JsonSyntaxException | IllegalStateException e) {
            logger.warn("[getAllGateways] Parsing Viessmann response fails: {}", e.getMessage());
        }
    }

    public @Nullable DeviceDTO getAllDevices(String installationId, String gatewaySerial) {
        try {
            countApiCalls();
            return api.getAllDevices(this, installationId, gatewaySerial);
        } catch (ViessmannCommunicationException e) {
            updateBridgeStatusExtended(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Installation not reachable");
        } catch (JsonSyntaxException | IllegalStateException e) {
            logger.warn(
                    "[getAllDevices(String installationId, String gatewaySerial)] Parsing Viessmann response fails: {}",
                    e.getMessage());
        }
        return null;
    }

    public @Nullable EventsDTO getSelectedEvents(String eventType, String installationId, String gatewaySerial)
            throws ViessmannCommunicationException {
        try {
            countApiCalls();
            return api.getSelectedEvents(this, eventType, installationId, gatewaySerial);
        } catch (ViessmannCommunicationException e) {
            updateBridgeStatusExtended(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Installation not reachable");
        } catch (JsonSyntaxException | IllegalStateException e) {
            logger.warn("[getSelectedEvents] Parsing Viessmann response fails: {}", e.getMessage());
        }
        return null;
    }

    public boolean setData(@Nullable String url, @Nullable String json) throws ViessmannCommunicationException {
        if (url != null && json != null) {
            countApiCalls();
            return api.setData(this, url, json);
        }
        return false;
    }

    private Integer getPollingInterval() {
        if (this.config.pollingInterval > 0) {
            return this.config.pollingInterval;
        } else {
            int errorApiCalls = 0;
            if (!registeredErrorPollingGateway.isEmpty()) {
                for (String gatewaySerial : registeredErrorPollingGateway) {
                    Integer errorPollingInterval = registeredErrorPollingInterval.get(gatewaySerial);
                    if (errorPollingInterval != null) {
                        errorApiCalls = errorApiCalls + (1440 / errorPollingInterval);
                    }
                }
            }
            int calculatedInterval = (86400 / (this.config.apiCallLimit - this.config.bufferApiCommands - errorApiCalls)
                    * getAllActiveDevices()) + 1;

            if (calculatedInterval < 60) {
                calculatedInterval = 60;
            }
            return calculatedInterval;
        }
    }

    public void countApiCalls() {
        apiCalls++;
        String apiCallsAsString = String.valueOf(apiCalls);
        stateStorage.put(STORED_API_CALLS, apiCallsAsString);
        updateState(COUNT_API_CALLS, DecimalType.valueOf(apiCallsAsString));
    }

    private void checkResetApiCalls() {
        LocalTime time = LocalTime.now();
        if (time.isAfter(LocalTime.of(0, 0, 1)) && (time.isBefore(LocalTime.of(1, 0, 0)))) {
            if (countReset) {
                logger.debug("Resetting API call counts");
                apiCalls = 0;
                countReset = false;
            }
        } else {
            countReset = true;
        }
    }

    private void pollingFeatures() {
        List<Thing> accountChildren = getThing().getThings().stream().filter(Thing::isEnabled).toList();
        for (Thing accountChild : accountChildren) {
            ViessmannGatewayHandler accountChildHandler = (ViessmannGatewayHandler) accountChild.getHandler();
            if (accountChildHandler != null && ThingHandlerHelper.isHandlerInitialized(accountChildHandler)) {
                List<Thing> children = accountChildHandler.getChildren();
                for (Thing child : children) {
                    ThingHandler childHandler = child.getHandler();
                    if (childHandler instanceof DeviceHandler
                            && ThingHandlerHelper.isHandlerInitialized(childHandler)) {
                        updateFeaturesOfDevice((DeviceHandler) childHandler);
                    }
                }
            }
        }
    }

    private Integer getAllActiveDevices() {
        Integer activeDevices = 0;
        List<Thing> accountChildren = getThing().getThings().stream().filter(Thing::isEnabled).toList();
        for (Thing accountChild : accountChildren) {
            ViessmannGatewayHandler accountChildHandler = (ViessmannGatewayHandler) accountChild.getHandler();
            if (accountChildHandler != null) {
                List<Thing> children = accountChildHandler.getChildren();
                for (Thing child : children) {
                    ThingHandler childHandler = child.getHandler();
                    if (childHandler instanceof DeviceHandler) {
                        activeDevices++;
                    }
                }
            }
        }
        return activeDevices;
    }

    public @Nullable FeaturesDTO getAllFeatures(@Nullable String deviceId, String installationId, String gatewaySerial)
            throws ViessmannCommunicationException {
        if (deviceId != null) {
            countApiCalls();
            return api.getAllFeatures(this, deviceId, installationId, gatewaySerial);
        }
        return null;
    }

    public void updateFeaturesOfDevice(DeviceHandler handler) {
        String deviceId = handler.getDeviceId();
        String installationId = handler.getInstallationId();
        String gatewaySerial = handler.getGatewaySerial();
        logger.debug("[updateFeaturesOfDevice] Loading features from Device ID: {}", deviceId);
        try {
            FeaturesDTO allFeatures = api.getAllFeatures(this, deviceId, installationId, gatewaySerial);
            countApiCalls();
            if (allFeatures != null) {
                List<FeatureDataDTO> featuresData = allFeatures.data;
                if (featuresData != null && !featuresData.isEmpty()) {
                    for (FeatureDataDTO featureDataDTO : featuresData) {
                        handler.handleUpdate(featureDataDTO);
                    }
                } else {
                    logger.warn("[updateFeaturesOfDevice] Features of Device ID {} is empty.", deviceId);
                }
            }
        } catch (ViessmannCommunicationException e) {
            handler.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    String.format("Device not reachable %s", e.getMessage()));
        } catch (JsonSyntaxException | IllegalStateException e) {
            logger.warn("[updateFeaturesOfDevice] Parsing Viessmann response fails: {}", e.getMessage());
        }
    }

    public void startViessmannBridgePolling() {
        startViessmannBridgePolling(getPollingInterval(), getPollingInterval());
    }

    private void startViessmannBridgePolling(Integer pollingIntervalS, Integer initialDelay) {
        ScheduledFuture<?> currentPollingJob = viessmannBridgePollingJob;
        if (currentPollingJob == null) {
            viessmannBridgePollingJob = scheduler.scheduleWithFixedDelay(() -> {
                api.checkExpiringToken();
                checkResetApiCalls();
                if (!config.disablePolling) {
                    logger.debug("[startViessmannBridgePolling] Refresh job scheduled to run every {} seconds for '{}'",
                            pollingIntervalS, getThing().getUID());
                    pollingFeatures();
                }
                int newPollingInterval = getPollingInterval();
                if (newPollingInterval != pollingInterval) {
                    pollingInterval = newPollingInterval;
                    updateBridgePollingInterval();
                }

            }, initialDelay, pollingIntervalS, TimeUnit.SECONDS);
        }
    }

    private void startViessmannBridgeLimitReset(Long delay) {
        ScheduledFuture<?> currentPollingJob = viessmannBridgeLimitJob;
        if (currentPollingJob == null) {
            viessmannBridgeLimitJob = scheduler.scheduleWithFixedDelay(() -> {
                logger.debug("Resetting limit and reconnect for '{}'", getThing().getUID());
                api.checkExpiringToken();
                checkResetApiCalls();
                updateBridgeStatus(ThingStatus.ONLINE);
                startViessmannBridgePolling(pollingInterval, 1);
                stopViessmannBridgeLimitReset();
                startViessmannErrorsPolling();
            }, delay, 120, TimeUnit.SECONDS);
        }
    }

    private void updateBridgePollingInterval() {
        updateProperty("pollingInterval [s]", String.valueOf(pollingInterval));
        stopViessmannBridgePolling();
        startViessmannBridgePolling(pollingInterval, pollingInterval);
    }

    public void stopViessmannBridgePolling() {
        ScheduledFuture<?> currentPollingJob = viessmannBridgePollingJob;
        if (currentPollingJob != null) {
            currentPollingJob.cancel(true);
            viessmannBridgePollingJob = null;
            logger.trace("ViessmannBridgePolling is stopped for {}", getThing().getUID());
        }
    }

    public void stopViessmannErrorsPolling() {
        List<Thing> accountChildren = getThing().getThings().stream().filter(Thing::isEnabled).toList();
        for (Thing accountChild : accountChildren) {
            ViessmannGatewayHandler accountChildHandler = (ViessmannGatewayHandler) accountChild.getHandler();
            if (accountChildHandler != null) {
                accountChildHandler.stopViessmannErrorsPolling();
            }
        }
    }

    public void startViessmannErrorsPolling() {
        List<Thing> accountChildren = getThing().getThings().stream().filter(Thing::isEnabled).toList();
        for (Thing accountChild : accountChildren) {
            ViessmannGatewayHandler accountChildHandler = (ViessmannGatewayHandler) accountChild.getHandler();
            if (accountChildHandler != null) {
                accountChildHandler.manageErrorPolling();
            }
        }
    }

    public void stopViessmannBridgeLimitReset() {
        ScheduledFuture<?> currentPollingJob = viessmannBridgeLimitJob;
        if (currentPollingJob != null) {
            currentPollingJob.cancel(true);
            viessmannBridgeLimitJob = null;
        }
    }

    @Override
    public void waitForApiCallLimitReset(@Nullable Long resetLimitMillis) {
        long delay = 0L;
        if (resetLimitMillis != null) {
            delay = (resetLimitMillis - Instant.now().toEpochMilli()) / 1000;
        }
        stopViessmannBridgePolling();
        stopViessmannErrorsPolling();
        stopViessmannBridgeLimitReset();
        startViessmannBridgeLimitReset(delay);
    }

    @Override
    public void updateBridgeStatus(@Nullable ThingStatus status) {
        if (status != null) {
            updateStatus(status);
        }
    }

    @Override
    public void updateBridgeStatusExtended(@Nullable ThingStatus status, @Nullable ThingStatusDetail statusDetail,
            @Nullable String statusMessage) {
        if (status != null && statusDetail != null && statusMessage != null) {
            updateStatus(status, statusDetail, statusMessage);
        }
    }

    @Override
    public String getThingUIDasString() {
        return getThing().getUID().getAsString();
    }
}
