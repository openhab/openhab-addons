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
package org.openhab.binding.matter.internal.handler;

import static org.openhab.binding.matter.internal.MatterBindingConstants.THING_STATUS_DETAIL_CONTROLLER_WAITING_FOR_DATA;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.MatterBindingConstants;
import org.openhab.binding.matter.internal.MatterChannelTypeProvider;
import org.openhab.binding.matter.internal.MatterConfigDescriptionProvider;
import org.openhab.binding.matter.internal.MatterStateDescriptionOptionProvider;
import org.openhab.binding.matter.internal.client.AttributeListener;
import org.openhab.binding.matter.internal.client.EventTriggeredListener;
import org.openhab.binding.matter.internal.client.MatterWebsocketClient;
import org.openhab.binding.matter.internal.client.dto.Endpoint;
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.BaseCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.BasicInformationCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.BridgedDeviceBasicInformationCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.GeneralDiagnosticsCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.GeneralDiagnosticsCluster.NetworkInterface;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.client.dto.ws.EventTriggeredMessage;
import org.openhab.binding.matter.internal.controller.MatterControllerClient;
import org.openhab.binding.matter.internal.controller.devices.converter.GenericConverter;
import org.openhab.binding.matter.internal.controller.devices.types.DeviceType;
import org.openhab.binding.matter.internal.controller.devices.types.DeviceTypeRegistry;
import org.openhab.binding.matter.internal.util.MatterLabelUtils;
import org.openhab.binding.matter.internal.util.MatterUIDUtils;
import org.openhab.binding.matter.internal.util.TranslationService;
import org.openhab.core.config.core.ConfigDescription;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelGroupDefinition;
import org.openhab.core.thing.type.ChannelGroupType;
import org.openhab.core.thing.type.ChannelGroupTypeBuilder;
import org.openhab.core.thing.type.ChannelGroupTypeUID;
import org.openhab.core.thing.type.ThingTypeBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

/**
 * The {@link MatterBaseThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 * 
 * This class is the base class for all Matter device types. It is responsible for creating the channels and channel
 * groups for each Node or Bridge Endpoint.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public abstract class MatterBaseThingHandler extends BaseThingHandler
        implements AttributeListener, EventTriggeredListener {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final BaseThingHandlerFactory thingHandlerFactory;
    protected final MatterStateDescriptionOptionProvider stateDescriptionProvider;
    protected final MatterChannelTypeProvider channelTypeProvider;
    protected final MatterConfigDescriptionProvider configDescriptionProvider;
    protected final TranslationService translationService;
    protected Map<Integer, DeviceType> devices = new HashMap<>();
    protected @Nullable MatterControllerClient cachedClient;
    private @Nullable ScheduledFuture<?> pollingTask;

    public MatterBaseThingHandler(Thing thing, BaseThingHandlerFactory thingHandlerFactory,
            MatterStateDescriptionOptionProvider stateDescriptionProvider,
            MatterChannelTypeProvider channelTypeProvider, MatterConfigDescriptionProvider configDescriptionProvider,
            TranslationService translationService) {
        super(thing);
        this.thingHandlerFactory = thingHandlerFactory;
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.channelTypeProvider = channelTypeProvider;
        this.configDescriptionProvider = configDescriptionProvider;
        this.translationService = translationService;
    }

    public abstract BigInteger getNodeId();

    public abstract Integer getPollInterval();

    protected abstract ThingTypeUID getDynamicThingTypeUID();

    protected abstract boolean isBridgeType();

    /**
     * When processing endpoints, give implementers the ability to ignore certain endpoints
     * 
     * @param endpoint the endpoint to check
     */
    protected boolean shouldAddEndpoint(Endpoint endpoint) {
        return true;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        MatterWebsocketClient client = getClient();
        if (client == null) {
            logger.debug("Matter client not present, ignoring command");
            return;
        }

        String endpointIdString = channelUID.getGroupId();
        if (endpointIdString != null) {
            Integer endpointId = Integer.valueOf(endpointIdString);
            DeviceType deviceType = this.devices.get(endpointId);
            if (deviceType != null) {
                deviceType.handleCommand(channelUID, command);
            }
        }
    }

    @Override
    public void initialize() {
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE,
                    translationService.getTranslation(THING_STATUS_DETAIL_CONTROLLER_WAITING_FOR_DATA));
        }
    }

    @Override
    public void dispose() {
        channelTypeProvider.removeChannelGroupTypesForPrefix(getThing().getThingTypeUID().getId());
        stopPolling();
        super.dispose();
    }

    @Override
    public void handleRemoval() {
        channelTypeProvider.removeThingType(getThing().getThingTypeUID());
        channelTypeProvider.removeChannelGroupTypesForPrefix(thing.getThingTypeUID().getId());
        super.handleRemoval();
    }

    @Override
    protected ThingBuilder editThing() {
        return ThingBuilder.create(getDynamicThingTypeUID(), getThing().getUID()).withBridge(getThing().getBridgeUID())
                .withChannels(getThing().getChannels()).withConfiguration(getThing().getConfiguration())
                .withLabel(getThing().getLabel()).withLocation(getThing().getLocation())
                .withProperties(getThing().getProperties());
    }

    @Override
    protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        super.updateStatus(status, statusDetail, description);
        if (status != ThingStatus.ONLINE) {
            stopPolling();
        } else {
            startPolling();
        }
    }

    @Override
    public void onEvent(AttributeChangedMessage message) {
        logger.debug("AttributeChangedMessage for endpoint {}", message.path.endpointId);
        DeviceType deviceType = devices.get(message.path.endpointId);
        if (deviceType != null) {
            deviceType.onEvent(message);
        }
    }

    @Override
    public void onEvent(EventTriggeredMessage message) {
        DeviceType deviceType = devices.get(message.path.endpointId);
        if (deviceType != null) {
            deviceType.onEvent(message);
        }
    }

    /**
     * Update the state of a channel.
     *
     * @param endpointNumber The endpoint number.
     * @param channelId The channel ID.
     * @param state The state to update.
     */
    public void updateState(Integer endpointNumber, String channelId, State state) {
        ChannelGroupUID channelGroupUID = new ChannelGroupUID(getThing().getUID(), endpointNumber.toString());
        ChannelUID channelUID = new ChannelUID(channelGroupUID, channelId);
        super.updateState(channelUID, state);
    }

    /**
     * Trigger a channel event.
     *
     * @param endpointNumber The endpoint number.
     * @param channelId The channel ID.
     * @param event The event to trigger.
     */
    public void triggerChannel(Integer endpointNumber, String channelId, String event) {
        ChannelGroupUID channelGroupUID = new ChannelGroupUID(getThing().getUID(), endpointNumber.toString());
        ChannelUID channelUID = new ChannelUID(channelGroupUID, channelId);
        super.triggerChannel(channelUID, event);
    }

    /**
     * Set the thing status of the endpoint.
     *
     * @param status The status to set.
     * @param detail The detail of the status.
     * @param description The description of the status.
     */
    public void setEndpointStatus(ThingStatus status, ThingStatusDetail detail, String description) {
        logger.debug("setEndpointStatus {} {} {} {}", status, detail, description, getNodeId());
        updateStatus(status, detail, description);
    }

    /**
     * Send a cluster command to the MatterControllerClient.
     *
     * @param endpointId The endpoint ID.
     * @param clusterName The cluster name.
     * @param command The command to send.
     * @return CompletableFuture<JsonElement>
     */
    public CompletableFuture<JsonElement> sendClusterCommand(Integer endpointId, String clusterName,
            ClusterCommand command) {
        MatterControllerClient client = getClient();
        if (client != null) {
            logger.debug("sendClusterCommand {} {} {}", getNodeId(), endpointId, clusterName);
            return client.clusterCommand(getNodeId(), endpointId, clusterName, command);
        }
        throw new IllegalStateException("Client is null");
    }

    /**
     * Write an attribute to the MatterControllerClient.
     *
     * @param endpointId The endpoint ID.
     * @param clusterName The cluster name.
     * @param attributeName The attribute name.
     * @param value The value to write.
     */
    public void writeAttribute(Integer endpointId, String clusterName, String attributeName, String value) {
        MatterControllerClient ws = getClient();
        if (ws != null) {
            ws.clusterWriteAttribute(getNodeId(), endpointId, clusterName, attributeName, value);
        } else {
            throw new IllegalStateException("Client is null");
        }
    }

    /**
     * Read an attribute from the MatterControllerClient.
     *
     * @param endpointId The endpoint ID.
     * @param clusterName The cluster name.
     * @param attributeName The attribute name.
     * @return CompletableFuture<String>
     */
    public CompletableFuture<String> readAttribute(Integer endpointId, String clusterName, String attributeName) {
        MatterControllerClient ws = getClient();
        if (ws != null) {
            return ws.clusterReadAttribute(getNodeId(), endpointId, clusterName, attributeName);
        }
        throw new IllegalStateException("Client is null");
    }

    /**
     * Read a cluster from the MatterControllerClient.
     *
     * @param <T> The type of the cluster to read.
     * @param type The class of the cluster to read.
     * @param endpointId The endpoint ID.
     * @param clusterId The cluster ID.
     * @return CompletableFuture<T>
     * @throws JsonParseException when completing the future if the cluster cannot be deserialized
     */
    public <T extends BaseCluster> CompletableFuture<T> readCluster(Class<T> type, Integer endpointId,
            Integer clusterId) {
        MatterControllerClient ws = getClient();
        if (ws != null) {
            return ws.readCluster(type, getNodeId(), endpointId, clusterId);
        }
        throw new IllegalStateException("Client is null");
    }

    /**
     * Get the MatterControllerClient instance.
     *
     * @return The MatterControllerClient instance, or null if not available.
     */
    public @Nullable MatterControllerClient getClient() {
        if (cachedClient == null) {
            ControllerHandler c = controllerHandler();
            if (c != null) {
                cachedClient = c.getClient();
            }
        }
        return cachedClient;
    }

    /**
     * Update the property of the thing for a given cluster and attribute name. Null values will remove the property.
     * 
     * @param clusterName The name of the cluster.
     * @param attributeName The name of the attribute.
     * @param value The value of the attribute.
     */
    public void updateClusterAttributeProperty(String clusterName, String attributeName, @Nullable Object value) {
        getThing().setProperty(clusterName + "-" + attributeName, value != null ? value.toString() : null);
    }

    public synchronized void updateConfiguration(Map<String, Object> entries) {
        Configuration config = editConfiguration();
        for (Map.Entry<String, Object> entry : entries.entrySet()) {
            config.put(entry.getKey(), entry.getValue());
        }
        updateConfiguration(config);
    }

    /**
     * Registers a service with the thing handler factory.
     *
     * @param klass The class of the service to register.
     */
    public void registerService(Class<? extends ThingHandlerService> klass) {
        thingHandlerFactory.registerService(this, klass);
    }

    /**
     * Finds the first matching converter of the specified type across all managed devices (endpoints).
     *
     * @param <T> The type of the converter to find.
     * @param converterType The class of the converter to find (e.g., OnOffConverter.class).
     * @return The first matching converter of the specified type, or null if none is found.
     */
    public <T extends GenericConverter<? extends BaseCluster>> @Nullable T findConverterByType(Class<T> converterType) {
        for (DeviceType deviceType : devices.values()) {
            for (GenericConverter<? extends BaseCluster> converter : deviceType.getClusterConverters().values()) {
                if (converterType.isInstance(converter)) {
                    return converterType.cast(converter);
                }
            }
        }
        return null;
    }

    public void addConfigDescription(ConfigDescription configDescription) {
        logger.debug("addConfigDescription {}", configDescription.getUID());
        configDescriptionProvider.addConfigDescription(configDescription);
    }

    public @Nullable ConfigDescription getConfigDescription() {
        return configDescriptionProvider.getConfigDescription(getConfigDescriptionURI(), Locale.getDefault());
    }

    public URI getConfigDescriptionURI() {
        logger.debug("getConfigDescriptionURI {}:{}", MatterBindingConstants.CONFIG_DESCRIPTION_URI_THING_PREFIX,
                getThing().getUID().getAsString());
        return URI.create(
                MatterBindingConstants.CONFIG_DESCRIPTION_URI_THING_PREFIX + ":" + getThing().getUID().getAsString());
    }

    public final String getTranslation(String key) {
        return translationService.getTranslation(key);
    }

    protected @Nullable ControllerHandler controllerHandler() {
        Bridge bridge = getBridge();
        while (bridge != null) {
            BridgeHandler handler = bridge.getHandler();
            if (handler instanceof ControllerHandler controllerHandler) {
                return controllerHandler;
            } else if (handler instanceof MatterBaseThingHandler matterBaseThingHandler) {
                bridge = matterBaseThingHandler.getBridge();
            }

        }
        return null;
    }

    protected void updateRootProperties(Endpoint root) {
        BaseCluster cluster = root.clusters.get(BasicInformationCluster.CLUSTER_NAME);
        if (cluster != null && cluster instanceof BasicInformationCluster basicCluster) {
            updateClusterAttributeProperty(BasicInformationCluster.CLUSTER_NAME, Thing.PROPERTY_SERIAL_NUMBER,
                    basicCluster.serialNumber);
            updateClusterAttributeProperty(BasicInformationCluster.CLUSTER_NAME, Thing.PROPERTY_FIRMWARE_VERSION,
                    basicCluster.softwareVersionString);
            updateClusterAttributeProperty(BasicInformationCluster.CLUSTER_NAME, Thing.PROPERTY_VENDOR,
                    basicCluster.vendorName);
            updateClusterAttributeProperty(BasicInformationCluster.CLUSTER_NAME, Thing.PROPERTY_MODEL_ID,
                    basicCluster.productName);
            updateClusterAttributeProperty(BasicInformationCluster.CLUSTER_NAME, Thing.PROPERTY_HARDWARE_VERSION,
                    basicCluster.hardwareVersionString);
        } else {
            cluster = root.clusters.get(BridgedDeviceBasicInformationCluster.CLUSTER_NAME);
            if (cluster != null && cluster instanceof BridgedDeviceBasicInformationCluster basicCluster) {
                updateClusterAttributeProperty(BridgedDeviceBasicInformationCluster.CLUSTER_NAME,
                        Thing.PROPERTY_SERIAL_NUMBER, basicCluster.serialNumber);
                updateClusterAttributeProperty(BridgedDeviceBasicInformationCluster.CLUSTER_NAME,
                        Thing.PROPERTY_FIRMWARE_VERSION, basicCluster.softwareVersionString);
                updateClusterAttributeProperty(BridgedDeviceBasicInformationCluster.CLUSTER_NAME, Thing.PROPERTY_VENDOR,
                        basicCluster.vendorName);
                updateClusterAttributeProperty(BridgedDeviceBasicInformationCluster.CLUSTER_NAME,
                        Thing.PROPERTY_MODEL_ID, basicCluster.productName);
                updateClusterAttributeProperty(BridgedDeviceBasicInformationCluster.CLUSTER_NAME,
                        Thing.PROPERTY_HARDWARE_VERSION, basicCluster.hardwareVersionString);
            }
        }
        cluster = root.clusters.get(GeneralDiagnosticsCluster.CLUSTER_NAME);
        if (cluster instanceof GeneralDiagnosticsCluster generalCluster && generalCluster.networkInterfaces != null) {
            List<String> allIpv6Addresses = new ArrayList<>();
            List<String> allIpv4Addresses = new ArrayList<>();
            for (NetworkInterface ni : generalCluster.networkInterfaces) {
                updateClusterAttributeProperty(GeneralDiagnosticsCluster.CLUSTER_NAME, Thing.PROPERTY_MAC_ADDRESS,
                        MatterLabelUtils.formatMacAddress(ni.hardwareAddress));
                if (!ni.iPv6Addresses.isEmpty()) {
                    allIpv6Addresses.addAll(ni.iPv6Addresses.stream().map(MatterLabelUtils::formatIPv6Address)
                            .collect(Collectors.toList()));
                }
                if (!ni.iPv4Addresses.isEmpty()) {
                    allIpv4Addresses.addAll(ni.iPv4Addresses.stream().map(MatterLabelUtils::formatIPv4Address)
                            .collect(Collectors.toList()));
                }
            }
            if (!allIpv6Addresses.isEmpty()) {
                updateClusterAttributeProperty(GeneralDiagnosticsCluster.CLUSTER_NAME,
                        GeneralDiagnosticsCluster.ATTRIBUTE_NETWORK_INTERFACES + "-ipv6",
                        String.join(",", allIpv6Addresses));
            }
            if (!allIpv4Addresses.isEmpty()) {
                updateClusterAttributeProperty(GeneralDiagnosticsCluster.CLUSTER_NAME,
                        GeneralDiagnosticsCluster.ATTRIBUTE_NETWORK_INTERFACES + "-ipv4",
                        String.join(",", allIpv4Addresses));
            }
        }
    }

    /**
     * Main Processing of Matter endpoints. This will create a Channel Group per endpoint
     * 
     * @param endpoint
     */
    protected synchronized void updateBaseEndpoint(Endpoint endpoint) {
        List<ChannelGroupType> groupTypes = new ArrayList<>();
        List<ChannelGroupDefinition> groupDefs = new ArrayList<>();
        List<Channel> channels = new ArrayList<>();

        updateEndpointInternal(endpoint, groupTypes, groupDefs, channels);

        // update our thing using a custom thing type so we can add channel groups dynamically.
        // editThing() is overridden to use the type specific thing type here.
        // See https://github.com/openhab/openhab-addons/pull/16600 for more information on this technique
        ThingTypeUID baseThingTypeUID = MatterUIDUtils.baseTypeForThingType(getDynamicThingTypeUID());
        ThingTypeUID dynamicThingTypeUID = getDynamicThingTypeUID();
        if (baseThingTypeUID == null) {
            throw new IllegalStateException("Could not resolve base thing type for " + getThing().getThingTypeUID());
        }

        // create our dynamic thing from defaults from the xml defined thing, along with the dynamic type of the bridge
        // thing
        ThingTypeBuilder thingTypeBuilder = channelTypeProvider.derive(dynamicThingTypeUID, baseThingTypeUID, Optional
                .ofNullable(getBridge()).map(bridge -> List.of(bridge.getThingTypeUID().toString())).orElse(null));

        thingTypeBuilder.withChannelGroupDefinitions(groupDefs);
        channelTypeProvider.putThingType(isBridgeType() ? thingTypeBuilder.buildBridge() : thingTypeBuilder.build());
        channelTypeProvider.updateChannelGroupTypesForPrefix(dynamicThingTypeUID.getId(), groupTypes);

        ThingBuilder thingBuilder = editThing().withChannels();
        thingBuilder.withChannels(channels);
        updateThing(thingBuilder.build());

        // have all clusters send their initial states
        devices.values().forEach(deviceType -> deviceType.initState());
    }

    /**
     * Iterate recursively through endpoints, adding channel groups and channels.
     * 
     * @param endpoint
     */
    private void updateEndpointInternal(Endpoint endpoint, List<ChannelGroupType> groupTypes,
            List<ChannelGroupDefinition> groupDefs, List<Channel> channels) {
        logger.debug("updateEndpointInternal {}", endpoint.number);

        // give implementers the ability to act on and prevent endpoints from being added (mainly for bridge endpoints)
        if (!shouldAddEndpoint(endpoint)) {
            return;
        }

        Map<String, BaseCluster> clusters = endpoint.clusters;
        Integer deviceTypeID = MatterLabelUtils.primaryDeviceTypeForEndpoint(endpoint);
        String deviceLabel = MatterLabelUtils.labelForEndpoint(endpoint);

        // Do we already have a device created yet?
        DeviceType deviceType = devices.get(endpoint.number);
        if (deviceType == null) {
            deviceType = DeviceTypeRegistry.createDeviceType(deviceTypeID, this, endpoint.number);
            devices.put(endpoint.number, deviceType);
            logger.debug("updateEndpointInternal added device type {} {}", deviceTypeID, endpoint.number);
        }

        // create custom group for endpoint
        ChannelGroupTypeUID channelGroupTypeUID = new ChannelGroupTypeUID(MatterBindingConstants.BINDING_ID,
                getNodeId() + "-" + endpoint.number);
        ChannelGroupDefinition channelGroupDefinition = new ChannelGroupDefinition(endpoint.number.toString(),
                channelGroupTypeUID);
        ChannelGroupType channelGroupType = ChannelGroupTypeBuilder.instance(channelGroupTypeUID, deviceLabel)
                .withDescription(deviceLabel).build();
        groupDefs.add(channelGroupDefinition);
        groupTypes.add(channelGroupType);

        ChannelGroupUID channelGroupUID = new ChannelGroupUID(getThing().getUID(), endpoint.number.toString());

        // add channels, which will be used by the calling 'updateEndpoint' function
        channels.addAll(deviceType.createChannels(endpoint.number, clusters, channelGroupUID));

        // add the state descriptions of channels to our custom stateDescriptionProvider
        deviceType.getStateDescriptions().forEach((channelUID, stateDescription) -> {
            if (stateDescription != null) {
                Optional.ofNullable(stateDescription.getOptions())
                        .ifPresent(options -> stateDescriptionProvider.setStateOptions(channelUID, options));

                Optional.ofNullable(stateDescription.getPattern())
                        .ifPresent(pattern -> stateDescriptionProvider.setStatePattern(channelUID, pattern));

                BigDecimal min = stateDescription.getMinimum();
                BigDecimal max = stateDescription.getMaximum();
                if (min != null && max != null) {
                    stateDescriptionProvider.setMinMax(channelUID, min, max, stateDescription.getStep(),
                            stateDescription.getPattern());
                }
            }
        });
        // add any children recursively (endpoints can have child endpoints)
        endpoint.children.forEach(e -> updateEndpointInternal(e, groupTypes, groupDefs, channels));
    }

    /**
     * Start polling the device for updates if needed
     */
    private synchronized void startPolling() {
        stopPolling();
        Integer pollInterval = getPollInterval();
        if (pollInterval > 0) {
            pollingTask = scheduler.scheduleWithFixedDelay(() -> {
                if (getThing().getStatus() == ThingStatus.ONLINE) {
                    devices.values().forEach(deviceType -> deviceType.pollClusters());
                }
            }, pollInterval, pollInterval, TimeUnit.SECONDS);
        }
    }

    public void stopPolling() {
        ScheduledFuture<?> pollingTask = this.pollingTask;
        if (pollingTask != null) {
            pollingTask.cancel(true);
            this.pollingTask = null;
        }
    }
}
