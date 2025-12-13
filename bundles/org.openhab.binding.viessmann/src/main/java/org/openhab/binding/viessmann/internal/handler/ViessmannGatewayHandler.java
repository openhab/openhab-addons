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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.viessmann.internal.ViessmannGatewayDiscoveryService;
import org.openhab.binding.viessmann.internal.api.ViessmannCommunicationException;
import org.openhab.binding.viessmann.internal.config.GatewayConfiguration;
import org.openhab.binding.viessmann.internal.dto.device.DeviceDTO;
import org.openhab.binding.viessmann.internal.dto.device.DeviceData;
import org.openhab.binding.viessmann.internal.dto.events.EventsDTO;
import org.openhab.binding.viessmann.internal.dto.features.FeatureDataDTO;
import org.openhab.binding.viessmann.internal.dto.features.FeaturesDTO;
import org.openhab.binding.viessmann.internal.interfaces.BridgeInterface;
import org.openhab.binding.viessmann.internal.util.ViessmannUtil;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.BridgeHandler;
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
 * The {@link ViessmannGatewayHandler} is responsible for handling the api connection.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class ViessmannGatewayHandler extends BaseBridgeHandler implements BridgeInterface {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final Set<String> ERROR_CHANNELS = Set.of("last-error-message", "error-is-active");

    protected @Nullable ViessmannGatewayDiscoveryService discoveryService;

    private String installationId = "";
    private String gatewaySerial = "";

    private @Nullable ScheduledFuture<?> viessmannErrorsPollingJob;

    public @Nullable List<DeviceData> devicesData;
    protected final List<String> devicesList = new ArrayList<>();
    protected final List<DeviceData> discoveredDeviceList = new ArrayList<>();

    private final ItemChannelLinkRegistry linkRegistry;

    private GatewayConfiguration config = new GatewayConfiguration();

    public ViessmannGatewayHandler(Bridge bridge, ItemChannelLinkRegistry linkRegistry) {
        super(bridge);
        this.linkRegistry = linkRegistry;
    }

    @Override
    public void setConfigInstallationGatewayIdToDevice(@Nullable DeviceHandler handler) {
        if (handler != null) {
            handler.setConfigInstallationGatewayId(installationId, gatewaySerial);
        }
    }

    /**
     * get the discovered devices list (needed for discovery)
     *
     * @return a list of the all devices
     */
    public List<DeviceData> getDiscoveredDeviceList() {
        // return a copy of the list, so we don't run into concurrency problems
        return new ArrayList<>(discoveredDeviceList);
    }

    public List<Thing> getChildren() {
        return getThing().getThings().stream().filter(Thing::isEnabled).toList();
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
        BridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler != null) {
            ((ViessmannAccountHandler) bridgeHandler).removeRegisteredErrorPollingGateway(gatewaySerial);
        }
        stopViessmannErrorsPolling();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(ViessmannGatewayDiscoveryService.class);
    }

    @Override
    public void initialize() {
        logger.debug("Initialize Viessmann Gateway");

        GatewayConfiguration config = getConfigAs(GatewayConfiguration.class);
        this.config = config;

        this.gatewaySerial = config.gatewaySerial;
        this.installationId = config.installationId;

        migrateChannelIds();

        if (!config.disablePolling && errorChannelsLinked()) {
            BridgeHandler bridgeHandler = getBridgeHandler();
            if (bridgeHandler != null) {
                ((ViessmannAccountHandler) bridgeHandler).addRegisteredErrorPollingGateway(gatewaySerial,
                        config.pollingIntervalErrors);
            }
            startViessmannErrorsPolling(config.pollingIntervalErrors);
        }

        scheduler.execute(() -> {
            try {
                getAllDevices();
                if (!devicesList.isEmpty()) {
                    updateBridgeStatus(ThingStatus.ONLINE);
                }
            } catch (Exception e) {
                logger.error("Failed to initialize Viessmann Gateway", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        });
    }

    private void migrateChannelIds() {
        List<Channel> oldChannels = thing.getChannels();
        List<Channel> newChannels = new ArrayList<>(oldChannels.size());

        Map<ChannelUID, ChannelUID> renameMap = new LinkedHashMap<>();

        for (Channel channel : oldChannels) {
            String oldId = channel.getUID().getId();
            String newId = ViessmannUtil.camelToHyphen(oldId);

            if (!newId.equals(oldId)) {
                logger.info("Migrating channel '{}' -> '{}'", oldId, newId);

                ChannelUID oldUid = channel.getUID();
                ChannelUID newUid = new ChannelUID(thing.getUID(), newId);

                String channelLabel = channel.getLabel();

                String channelType = ViessmannUtil
                        .camelToHyphen(Objects.requireNonNull(channel.getChannelTypeUID()).toString());
                channelType = channelType.replace(BINDING_ID + ":", "");

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

    private @Nullable BridgeHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            BridgeHandler handler = bridge.getHandler();
            if (handler != null) {
                if (handler instanceof ViessmannAccountHandler) {
                    return handler;
                }
            }
        }
        return null;
    }

    public void getAllDevices() {
        logger.trace("Loading Device List from Viessmann Gateway");
        try {
            DeviceDTO allDevices = new DeviceDTO();

            BridgeHandler handler = getBridgeHandler();
            if (handler != null) {
                allDevices = ((ViessmannAccountHandler) handler).getAllDevices(installationId, gatewaySerial);
            }

            if (allDevices != null) {
                devicesData = allDevices.data;
                if (devicesData == null) {
                    logger.warn("Device list is empty.");
                } else {
                    for (DeviceData deviceData : allDevices.data) {
                        deviceData.setInstallationId(installationId);
                        String deviceId = deviceData.id;
                        String deviceType = deviceData.deviceType;
                        if (!"gateway".equals(deviceId) && !devicesList.contains(deviceId)) {
                            discoveredDeviceList.add(deviceData);
                            devicesList.add(deviceId);
                        }
                        logger.trace("Device ID: {}, Type: {}", deviceId, deviceType);
                    }
                }
            }
        } catch (JsonSyntaxException | IllegalStateException e) {
            logger.warn("Parsing Viessmann response fails: {}", e.getMessage());
        }
    }

    public void getDeviceError() {
        logger.trace("Loading error-list from Viessmann Account Handler");
        try {
            EventsDTO errors = new EventsDTO();
            String eventType = "device-error";
            BridgeHandler handler = getBridgeHandler();
            if (handler != null) {
                errors = ((ViessmannAccountHandler) handler).getSelectedEvents(eventType, installationId,
                        gatewaySerial);
            }
            logger.trace("Errors:{}", errors);
            if (errors != null && !errors.data.isEmpty()) {
                String state = errors.data.get(0).body.errorDescription;
                Boolean active = errors.data.get(0).body.active;
                updateState(CHANNEL_LAST_ERROR_MESSAGE, StringType.valueOf(state));
                updateState(CHANNEL_ERROR_IS_ACTIVE, OnOffType.from(active));
            }
        } catch (ViessmannCommunicationException e) {
            updateBridgeStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Installation not reachable");
        } catch (JsonSyntaxException | IllegalStateException e) {
            logger.warn("Parsing Viessmann response fails: {}", e.getMessage());
        }
    }

    public boolean setData(@Nullable String url, @Nullable String json) throws ViessmannCommunicationException {
        if (url != null && json != null) {
            BridgeHandler handler = getBridgeHandler();
            if (handler != null) {
                return ((ViessmannAccountHandler) handler).setData(url, json);
            }
        }
        return false;
    }

    private void pollingFeatures() {
        List<Thing> children = getChildren();
        for (Thing child : children) {
            ThingHandler childHandler = child.getHandler();
            if (childHandler instanceof DeviceHandler && ThingHandlerHelper.isHandlerInitialized(childHandler)) {
                updateFeaturesOfDevice((DeviceHandler) childHandler);
            }
        }
    }

    @Override
    public void updateFeaturesOfDevice(@Nullable DeviceHandler handler) {
        String deviceId = "";
        if (handler != null) {
            deviceId = handler.getDeviceId();
            logger.debug("Loading features from Device ID: {}", deviceId);
            try {
                FeaturesDTO allFeatures = null;

                BridgeHandler bridgeHandler = getBridgeHandler();
                if (bridgeHandler != null) {
                    allFeatures = ((ViessmannAccountHandler) bridgeHandler).getAllFeatures(deviceId, installationId,
                            gatewaySerial);
                }
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

    protected synchronized void manageErrorPolling() {
        BridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler == null) {
            return;
        }
        ScheduledFuture<?> errorPollingJob = viessmannErrorsPollingJob;
        if (errorChannelsLinked() && errorPollingJob == null) {
            ((ViessmannAccountHandler) bridgeHandler).addRegisteredErrorPollingGateway(gatewaySerial,
                    config.pollingIntervalErrors);
            ((ViessmannAccountHandler) bridgeHandler).stopViessmannBridgePolling();
            ((ViessmannAccountHandler) bridgeHandler).startViessmannBridgePolling();
            startViessmannErrorsPolling(config.pollingIntervalErrors);
        } else {
            if (!errorChannelsLinked() && errorPollingJob != null) {
                stopViessmannErrorsPolling();
                ((ViessmannAccountHandler) bridgeHandler).removeRegisteredErrorPollingGateway(gatewaySerial);
                ((ViessmannAccountHandler) bridgeHandler).stopViessmannBridgePolling();
                ((ViessmannAccountHandler) bridgeHandler).startViessmannBridgePolling();
            }
        }
    }

    private void startViessmannErrorsPolling(Integer pollingInterval) {
        ScheduledFuture<?> currentPollingJob = viessmannErrorsPollingJob;
        if (currentPollingJob == null) {
            viessmannErrorsPollingJob = scheduler.scheduleWithFixedDelay(() -> {
                logger.debug("Refresh job scheduled to run every {} minutes for polling errors", pollingInterval);
                getDeviceError();
            }, 0, pollingInterval, TimeUnit.MINUTES);
        }
    }

    public void stopViessmannErrorsPolling() {
        ScheduledFuture<?> currentPollingJob = viessmannErrorsPollingJob;
        if (currentPollingJob != null) {
            currentPollingJob.cancel(true);
            viessmannErrorsPollingJob = null;
        }
    }

    public void updateBridgeStatus(ThingStatus status) {
        updateStatus(status);
    }

    public void updateBridgeStatus(ThingStatus status, ThingStatusDetail statusDetail, String statusMessage) {
        updateStatus(status, statusDetail, statusMessage);
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
}
