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
package org.openhab.binding.viessmann.internal.handler;

import static org.openhab.binding.viessmann.internal.ViessmannBindingConstants.*;

import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.viessmann.internal.ViessmannDiscoveryService;
import org.openhab.binding.viessmann.internal.api.ViessmannApi;
import org.openhab.binding.viessmann.internal.api.ViessmannCommunicationException;
import org.openhab.binding.viessmann.internal.config.BridgeConfiguration;
import org.openhab.binding.viessmann.internal.dto.device.DeviceDTO;
import org.openhab.binding.viessmann.internal.dto.device.DeviceData;
import org.openhab.binding.viessmann.internal.dto.events.EventsDTO;
import org.openhab.binding.viessmann.internal.dto.features.FeatureDataDTO;
import org.openhab.binding.viessmann.internal.dto.features.FeaturesDTO;
import org.openhab.binding.viessmann.internal.interfaces.ApiInterface;
import org.openhab.binding.viessmann.internal.interfaces.BridgeInterface;
import org.openhab.binding.viessmann.internal.util.ViessmannUtil;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
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
 * The {@link ViessmannBridgeHandler} is responsible for handling the api connection.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class ViessmannBridgeHandler extends BaseBridgeHandler implements BridgeInterface, ApiInterface {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Storage<String> stateStorage;

    private static final Set<String> ERROR_CHANNELS = Set.of("last-error-message", "error-is-active");
    private static final String STORED_API_CALLS = "apiCalls";

    private final HttpClient httpClient;
    private final @Nullable String callbackUrl;

    private @NonNullByDefault({}) ViessmannApi api;

    protected @Nullable ViessmannDiscoveryService discoveryService;

    private int apiCalls;
    private int pollingInterval = 90;
    private boolean countReset = true;

    private @Nullable String newInstallationId;
    private @Nullable String newGatewaySerial;

    private @Nullable ScheduledFuture<?> viessmannBridgePollingJob;
    private @Nullable ScheduledFuture<?> viessmannErrorsPollingJob;
    private @Nullable ScheduledFuture<?> viessmannBridgeLimitJob;

    private @Nullable ScheduledFuture<?> initJob;
    private volatile boolean disposed = false;

    public @Nullable List<DeviceData> devicesData;
    protected final List<String> devicesList = new ArrayList<>();

    private final ItemChannelLinkRegistry linkRegistry;

    private BridgeConfiguration config = new BridgeConfiguration();

    public ViessmannBridgeHandler(Bridge bridge, Storage<String> stateStorage, HttpClient httpClient,
            @Nullable String callbackUrl, ItemChannelLinkRegistry linkRegistry) {
        super(bridge);
        this.stateStorage = stateStorage;
        this.httpClient = httpClient;
        this.callbackUrl = callbackUrl;
        this.linkRegistry = linkRegistry;
    }

    @Override
    public void setInstallationGatewayId(@Nullable String newInstallation, @Nullable String newGateway) {
        newInstallationId = newInstallation;
        newGatewaySerial = newGateway;
    }

    /**
     * get the devices list (needed for discovery)
     *
     * @return a list of the all devices
     */
    public List<String> getDevicesList() {
        // return a copy of the list, so we don't run into concurrency problems
        return new ArrayList<>(devicesList);
    }

    public List<Thing> getChildren() {
        return getThing().getThings().stream().filter(Thing::isEnabled).collect(Collectors.toList());
    }

    private void setConfigInstallationGatewayId() {
        Configuration conf = editConfiguration();
        conf.put("installationId", newInstallationId);
        conf.put("gatewaySerial", newGatewaySerial);
        updateConfiguration(conf);
    }

    private boolean errorChannelsLinked() {
        return getThing().getChannels().stream()
                .anyMatch(c -> isLinked(c.getUID()) && ERROR_CHANNELS.contains(c.getUID().getId()));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_RUN_QUERY_ONCE) && OnOffType.ON.equals(command)) {
            logger.debug("Received command: CHANNEL_RUN_QUERY_ONCE");
            pollingFeatures();
            updateState(CHANNEL_RUN_QUERY_ONCE, OnOffType.OFF);
        }
        if (channelUID.getId().equals(CHANNEL_RUN_ERROR_QUERY_ONCE) && OnOffType.ON.equals(command)) {
            logger.debug("Received command: CHANNEL_RUN_ERROR_QUERY_ONCE");
            getDeviceError();
            updateState(CHANNEL_RUN_ERROR_QUERY_ONCE, OnOffType.OFF);
        }
    }

    @Override
    public void dispose() {
        disposed = true;

        ScheduledFuture<?> currentInitJob = initJob;
        if (currentInitJob != null) {
            currentInitJob.cancel(true);
            initJob = null;
        }

        stopViessmannBridgePolling();
        stopViessmannErrorsPolling();
        stopViessmannBridgeLimitReset();

        super.dispose();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(ViessmannDiscoveryService.class);
    }

    @Override
    public void initialize() {
        logger.debug("Initialize Viessmann Accountservice");

        BridgeConfiguration config = getConfigAs(BridgeConfiguration.class);
        this.config = config;
        String storedApiCalls = this.stateStorage.get(STORED_API_CALLS);
        if (storedApiCalls != null) {
            apiCalls = Integer.parseInt(storedApiCalls);
        } else {
            apiCalls = 0;
        }
        disposed = false;
        newInstallationId = "";
        newGatewaySerial = "";

        api = new ViessmannApi(this.config.apiKey, httpClient, this.config.user, this.config.password,
                this.config.installationId, this.config.gatewaySerial, callbackUrl);
        api.createOAuthClientService(this);

        initJob = scheduler.schedule(() -> {
            if (disposed) {
                return;
            }
            try {
                if (api.doAuthorize()) {
                    if (this.config.installationId.isEmpty() || this.config.gatewaySerial.isEmpty()) {
                        api.setInstallationAndGatewayId(this);
                        setConfigInstallationGatewayId();
                    }

                    if (!config.disablePolling && errorChannelsLinked()) {
                        startViessmannErrorsPolling(config.pollingIntervalErrors);
                    }

                    migrateChannelIds();

                    getAllDevices();
                    if (!devicesList.isEmpty() && !disposed) {
                        updateBridgeStatus(ThingStatus.ONLINE);
                        startViessmannBridgePolling(getPollingInterval(), 1);
                    }
                }
            } catch (Exception e) {
                if (!disposed) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                }
            }
        }, 0, TimeUnit.SECONDS);
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
    }

    public void getAllDevices() {
        logger.trace("Loading Device List from Viessmann Bridge");
        try {
            DeviceDTO allDevices = api.getAllDevices(this);
            countApiCalls();
            if (allDevices != null) {
                devicesData = allDevices.data;
                if (devicesData == null) {
                    logger.warn("Device list is empty.");
                } else {
                    for (DeviceData deviceData : allDevices.data) {
                        String deviceId = deviceData.id;
                        String deviceType = deviceData.deviceType;
                        if (!devicesList.contains(deviceId)) {
                            devicesList.add(deviceId);
                        }
                        logger.trace("Device ID: {}, Type: {}", deviceId, deviceType);
                    }
                }
            }
        } catch (ViessmannCommunicationException e) {
            updateBridgeStatusExtended(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Installation not reachable");
        } catch (JsonSyntaxException | IllegalStateException e) {
            logger.warn("Parsing Viessmann response fails: {}", e.getMessage());
        }
    }

    public void getDeviceError() {
        logger.trace("Loading error-list from Viessmann Bridge");
        try {
            EventsDTO errors = api.getSelectedEvents(this, "device-error");
            countApiCalls();
            logger.trace("Errors:{}", errors);
            if (errors != null && !errors.data.isEmpty()) {
                String state = errors.data.get(0).body.errorDescription;
                Boolean active = errors.data.get(0).body.active;
                updateState(CHANNEL_LAST_ERROR_MESSAGE, StringType.valueOf(state));
                updateState(CHANNEL_ERROR_IS_ACTIVE, OnOffType.from(active));
            }
        } catch (ViessmannCommunicationException e) {
            updateBridgeStatusExtended(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Installation not reachable");
        } catch (JsonSyntaxException | IllegalStateException e) {
            logger.warn("Parsing Viessmann response fails: {}", e.getMessage());
        }
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
            if (errorChannelsLinked()) {
                errorApiCalls = 1440 / this.config.pollingIntervalErrors;
            }

            int calculatedInterval = (86400 / (this.config.apiCallLimit - this.config.bufferApiCommands - errorApiCalls)
                    * getAllActiveDevices()) + 1;

            if (calculatedInterval < 60) {
                calculatedInterval = 60;
            }
            return calculatedInterval;
        }
    }

    private void countApiCalls() {
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
        List<Thing> children = getThing().getThings().stream().filter(Thing::isEnabled).collect(Collectors.toList());
        for (Thing child : children) {
            ThingHandler childHandler = child.getHandler();
            if (childHandler instanceof DeviceHandler && ThingHandlerHelper.isHandlerInitialized(childHandler)) {
                updateFeaturesOfDevice((DeviceHandler) childHandler);
            }
        }
    }

    private Integer getAllActiveDevices() {
        Integer activeDevices = 0;
        List<Thing> bridgeChildren = getThing().getThings().stream().filter(Thing::isEnabled).toList();
        for (Thing bridgeChild : bridgeChildren) {
            ThingHandler childHandler = bridgeChild.getHandler();
            if (childHandler instanceof DeviceHandler) {
                activeDevices++;
            }
        }
        return activeDevices;
    }

    @Override
    public void updateFeaturesOfDevice(@Nullable DeviceHandler handler) {
        String deviceId = "";
        if (handler != null) {
            deviceId = handler.getDeviceId();
            logger.debug("Loading features from Device ID: {}", deviceId);
            try {
                FeaturesDTO allFeatures = api.getAllFeatures(this, deviceId);
                countApiCalls();
                if (allFeatures != null) {
                    List<FeatureDataDTO> featuresData = allFeatures.data;
                    if (featuresData != null && !featuresData.isEmpty()) {
                        for (FeatureDataDTO featureDataDTO : featuresData) {
                            handler.handleUpdate(featureDataDTO);
                        }
                    } else {
                        logger.warn("Features of Device ID \"{}\" is empty.", deviceId);
                        String statusMessage = String.format("@text/offline.comm-error.features-empty [%s]", deviceId);
                        handler.updateThingStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, statusMessage);
                    }
                }
            } catch (ViessmannCommunicationException e) {
                String statusMessage = String.format("@text/offline.comm-error.device-not-reachable [%s]",
                        e.getMessage());
                handler.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, statusMessage);
            } catch (JsonSyntaxException | IllegalStateException e) {
                logger.warn("Parsing Viessmann response fails: {}", e.getMessage());
            }
        }
    }

    private void startViessmannBridgePolling(Integer pollingIntervalS, Integer initialDelay) {
        if (viessmannBridgePollingJob != null) {
            return;
        }

        viessmannBridgePollingJob = scheduler.scheduleWithFixedDelay(() -> {
            if (disposed || !isInitialized()) {
                return;
            }

            api.checkExpiringToken();
            checkResetApiCalls();
            if (!config.disablePolling) {
                logger.debug("Refresh job scheduled to run every {} seconds for '{}'", pollingIntervalS,
                        getThing().getUID());
                pollingFeatures();
                int newPollingInterval = getPollingInterval();
                if (newPollingInterval != pollingInterval) {
                    pollingInterval = newPollingInterval;
                    updateBridgePollingInterval();
                }
            }
        }, initialDelay, pollingIntervalS, TimeUnit.SECONDS);
    }

    protected synchronized void manageErrorPolling() {
        ScheduledFuture<?> errorPollingJob = viessmannErrorsPollingJob;
        if (errorChannelsLinked() && errorPollingJob == null) {
            stopViessmannBridgePolling();
            startViessmannBridgePolling(getPollingInterval(), getPollingInterval());
            startViessmannErrorsPolling(config.pollingIntervalErrors);
        } else {
            if (!errorChannelsLinked() && errorPollingJob != null) {
                stopViessmannErrorsPolling();
                stopViessmannBridgePolling();
                startViessmannBridgePolling(getPollingInterval(), getPollingInterval());
            }
        }
    }

    private void startViessmannErrorsPolling(Integer pollingInterval) {
        if (viessmannErrorsPollingJob != null) {
            return;
        }
        viessmannErrorsPollingJob = scheduler.scheduleWithFixedDelay(() -> {
            if (disposed || !isInitialized()) {
                return;
            }
            logger.debug("Refresh job scheduled to run every {} minutes for polling errors", pollingInterval);
            getDeviceError();
        }, 0, pollingInterval, TimeUnit.MINUTES);
    }

    private void startViessmannBridgeLimitReset(Long delay) {
        if (viessmannBridgeLimitJob != null) {
            return;
        }

        viessmannBridgeLimitJob = scheduler.scheduleWithFixedDelay(() -> {
            if (disposed || !isInitialized()) {
                return;
            }
            logger.debug("Resetting limit and reconnect for '{}'", getThing().getUID());
            api.checkExpiringToken();
            checkResetApiCalls();
            getAllDevices();
            if (!devicesList.isEmpty()) {
                updateBridgeStatus(ThingStatus.ONLINE);
                startViessmannBridgePolling(getPollingInterval(), 1);
                stopViessmannBridgeLimitReset();
            }
        }, delay, 120, TimeUnit.SECONDS);
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
        }
    }

    public void stopViessmannErrorsPolling() {
        ScheduledFuture<?> currentPollingJob = viessmannErrorsPollingJob;
        if (currentPollingJob != null) {
            currentPollingJob.cancel(true);
            viessmannErrorsPollingJob = null;
        }
    }

    public void stopViessmannBridgeLimitReset() {
        ScheduledFuture<?> currentPollingJob = viessmannBridgeLimitJob;
        if (currentPollingJob != null) {
            currentPollingJob.cancel(true);
            viessmannBridgeLimitJob = null;
        }
    }

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
    public void channelLinked(ChannelUID channelUID) {
        manageErrorPolling();
        super.channelLinked(channelUID);
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        manageErrorPolling();
        super.channelUnlinked(channelUID);
    }

    @Override
    public String getThingUIDasString() {
        return getThing().getUID().getAsString();
    }

    @Override
    public void setConfigInstallationGatewayIdToDevice(@Nullable DeviceHandler handler) {
        if (handler != null) {
            handler.setConfigInstallationGatewayId(this.config.installationId, this.config.gatewaySerial);
        }
    }
}
