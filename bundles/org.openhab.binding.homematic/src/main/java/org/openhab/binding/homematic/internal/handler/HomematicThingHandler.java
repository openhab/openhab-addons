/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.homematic.internal.handler;

import static org.openhab.binding.homematic.internal.HomematicBindingConstants.*;
import static org.openhab.binding.homematic.internal.misc.HomematicConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.Future;

import org.openhab.binding.homematic.internal.HomematicBindingConstants;
import org.openhab.binding.homematic.internal.common.HomematicConfig;
import org.openhab.binding.homematic.internal.communicator.HomematicGateway;
import org.openhab.binding.homematic.internal.converter.ConverterException;
import org.openhab.binding.homematic.internal.converter.ConverterFactory;
import org.openhab.binding.homematic.internal.converter.ConverterTypeException;
import org.openhab.binding.homematic.internal.converter.TypeConverter;
import org.openhab.binding.homematic.internal.misc.HomematicClientException;
import org.openhab.binding.homematic.internal.misc.HomematicConstants;
import org.openhab.binding.homematic.internal.model.HmChannel;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmDatapointConfig;
import org.openhab.binding.homematic.internal.model.HmDatapointInfo;
import org.openhab.binding.homematic.internal.model.HmDevice;
import org.openhab.binding.homematic.internal.model.HmParamsetType;
import org.openhab.binding.homematic.internal.type.HomematicChannelTypeProvider;
import org.openhab.binding.homematic.internal.type.HomematicTypeGeneratorImpl;
import org.openhab.binding.homematic.internal.type.MetadataUtils;
import org.openhab.binding.homematic.internal.type.UidUtils;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.core.validation.ConfigValidationException;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HomematicThingHandler} is responsible for handling commands, which are sent to one of the channels.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class HomematicThingHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(HomematicThingHandler.class);
    private final HomematicChannelTypeProvider channelTypeProvider;
    private Future<?> initFuture;
    private final Object initLock = new Object();
    private volatile boolean deviceDeletionPending = false;

    public HomematicThingHandler(Thing thing, HomematicChannelTypeProvider channelTypeProvider) {
        super(thing);
        this.channelTypeProvider = channelTypeProvider;
    }

    @Override
    public void initialize() {
        if (initFuture != null) {
            return;
        }

        initFuture = scheduler.submit(() -> {
            initFuture = null;
            try {
                synchronized (initLock) {
                    doInitializeInBackground();
                }
            } catch (HomematicClientException ex) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, ex.getMessage());
            } catch (IOException ex) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
            } catch (GatewayNotAvailableException ex) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, ex.getMessage());
            } catch (Exception ex) {
                logger.error("{}", ex.getMessage(), ex);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, ex.getMessage());
            }
        });
    }

    private void doInitializeInBackground() throws GatewayNotAvailableException, HomematicClientException, IOException {
        HomematicGateway gateway = getHomematicGateway();
        HmDevice device = gateway.getDevice(UidUtils.getHomematicAddress(getThing()));
        HmChannel channelZero = device.getChannel(0);
        loadHomematicChannelValues(channelZero);
        updateStatus(device);
        logger.debug("Initializing thing '{}' from gateway '{}'", getThing().getUID(), gateway.getId());

        // update properties
        Map<String, String> properties = editProperties();
        setProperty(properties, channelZero, PROPERTY_BATTERY_TYPE, VIRTUAL_DATAPOINT_NAME_BATTERY_TYPE);
        setProperty(properties, channelZero, Thing.PROPERTY_FIRMWARE_VERSION, VIRTUAL_DATAPOINT_NAME_FIRMWARE);
        setProperty(properties, channelZero, Thing.PROPERTY_SERIAL_NUMBER, device.getAddress());
        setProperty(properties, channelZero, PROPERTY_AES_KEY, DATAPOINT_NAME_AES_KEY);
        updateProperties(properties);

        // update data point list for reconfigurable channels
        for (HmChannel channel : device.getChannels()) {
            if (channel.isReconfigurable()) {
                loadHomematicChannelValues(channel);
                if (channel.checkForChannelFunctionChange()) {
                    gateway.updateChannelValueDatapoints(channel);
                }
            }
        }

        // update configurations
        Configuration config = editConfiguration();
        for (HmChannel channel : device.getChannels()) {
            loadHomematicChannelValues(channel);
            for (HmDatapoint dp : channel.getDatapoints()) {
                if (dp.getParamsetType() == HmParamsetType.MASTER) {
                    config.put(MetadataUtils.getParameterName(dp),
                            dp.isEnumType() ? dp.getOptionValue() : dp.getValue());
                }
            }
        }
        updateConfiguration(config);

        boolean channelsChanged = false;

        // update thing channel list for reconfigurable channels (relies on the new value of the
        // CHANNEL_FUNCTION datapoint fetched during configuration update)
        List<Channel> thingChannels = new ArrayList<>(getThing().getChannels());

        if (thingChannels.isEmpty()) {
            for (HmChannel channel : device.getChannels()) {
                for (HmDatapoint dp : channel.getDatapoints()) {
                    if (HomematicTypeGeneratorImpl.isIgnoredDatapoint(dp)
                            || dp.getParamsetType() != HmParamsetType.VALUES) {
                        continue;
                    }
                    ChannelUID channelUID = UidUtils.generateChannelUID(dp, getThing().getUID());
                    if (containsChannel(thingChannels, channelUID)) {
                        // Channel is already present
                        continue;
                    }

                    ChannelTypeUID channelTypeUID = UidUtils.generateChannelTypeUID(dp);
                    ChannelType channelType = channelTypeProvider.getInternalChannelType(channelTypeUID);
                    if (channelType == null) {
                        channelType = HomematicTypeGeneratorImpl.createChannelType(dp, channelTypeUID);
                        channelTypeProvider.addChannelType(channelType);
                    }

                    Channel thingChannel = ChannelBuilder.create(channelUID, MetadataUtils.getItemType(dp))
                            .withLabel(MetadataUtils.getLabel(dp))
                            .withDescription(MetadataUtils.getDatapointDescription(dp)).withType(channelType.getUID())
                            .build();
                    thingChannels.add(thingChannel);

                    logger.debug(
                            "Updated value datapoints for channel {} of device '{}' (function {}), now has {} datapoints",
                            channel, channel.getDevice().getAddress(), channel.getCurrentFunction(),
                            channel.getDatapoints().size());
                }
            }
            channelsChanged = true;
        }

        if (updateDynamicChannelList(device, thingChannels)) {
            channelsChanged = true;
        }

        if (channelsChanged) {
            updateThing(editThing().withChannels(thingChannels).build());
        }

        thingChannels.forEach(channel -> {
            if (isLinked(channel.getUID())) {
                channelLinked(channel.getUID());
            }
        });
    }

    /**
     * Update the given thing channel list to reflect the device's current datapoint set
     *
     * @return true if the list was modified, false if it was not modified
     */
    private boolean updateDynamicChannelList(HmDevice device, List<Channel> thingChannels) {
        boolean changed = false;
        for (HmChannel channel : device.getChannels()) {
            if (!channel.isReconfigurable()) {
                continue;
            }
            final String expectedFunction = channel
                    .getDatapoint(HmParamsetType.MASTER, HomematicConstants.DATAPOINT_NAME_CHANNEL_FUNCTION)
                    .getOptionValue();
            final String propertyName = String.format(PROPERTY_DYNAMIC_FUNCTION_FORMAT, channel.getNumber());

            // remove thing channels that were configured for a different function
            Iterator<Channel> channelIter = thingChannels.iterator();
            while (channelIter.hasNext()) {
                Map<String, String> properties = channelIter.next().getProperties();
                String function = properties.get(propertyName);
                if (function != null && !function.equals(expectedFunction)) {
                    channelIter.remove();
                    changed = true;
                }
            }
            for (HmDatapoint dp : channel.getDatapoints()) {
                if (HomematicTypeGeneratorImpl.isIgnoredDatapoint(dp)
                        || dp.getParamsetType() != HmParamsetType.VALUES) {
                    continue;
                }
                ChannelUID channelUID = UidUtils.generateChannelUID(dp, getThing().getUID());
                if (containsChannel(thingChannels, channelUID)) {
                    // Channel is already present -> channel configuration likely hasn't changed
                    continue;
                }

                Map<String, String> channelProps = new HashMap<>();
                channelProps.put(propertyName, expectedFunction);

                ChannelTypeUID channelTypeUID = UidUtils.generateChannelTypeUID(dp);
                ChannelType channelType = channelTypeProvider.getInternalChannelType(channelTypeUID);
                if (channelType == null) {
                    channelType = HomematicTypeGeneratorImpl.createChannelType(dp, channelTypeUID);
                    channelTypeProvider.addChannelType(channelType);
                }

                Channel thingChannel = ChannelBuilder.create(channelUID, MetadataUtils.getItemType(dp))
                        .withProperties(channelProps).withLabel(MetadataUtils.getLabel(dp))
                        .withDescription(MetadataUtils.getDatapointDescription(dp)).withType(channelType.getUID())
                        .build();
                thingChannels.add(thingChannel);
                changed = true;
            }
        }

        return changed;
    }

    /**
     * Checks whether the given list includes a channel with the given UID
     */
    private static boolean containsChannel(List<Channel> channels, ChannelUID channelUID) {
        for (Channel channel : channels) {
            ChannelUID uid = channel.getUID();
            if (Objects.equals(channelUID.getGroupId(), uid.getGroupId())
                    && Objects.equals(channelUID.getId(), uid.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets a thing property with a datapoint value.
     */
    private void setProperty(Map<String, String> properties, HmChannel channelZero, String propertyName,
            String datapointName) {
        HmDatapoint dp = channelZero
                .getDatapoint(new HmDatapointInfo(HmParamsetType.VALUES, channelZero, datapointName));
        if (dp != null) {
            properties.put(propertyName, Objects.toString(dp.getValue(), ""));
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        handleRefresh(channelUID);
    }

    /**
     * Updates the state of the given channel.
     */
    protected void handleRefresh(ChannelUID channelUID) {
        try {
            if (thing.getStatus() == ThingStatus.ONLINE) {
                logger.debug("Updating channel '{}' from thing id '{}'", channelUID, getThing().getUID().getId());
                updateChannelState(channelUID);
            }
        } catch (Exception ex) {
            logger.warn("{}", ex.getMessage());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command '{}' for channel '{}'", command, channelUID);
        HmDatapoint dp = null;
        try {
            HomematicGateway gateway = getHomematicGateway();
            HmDatapointInfo dpInfo = UidUtils.createHmDatapointInfo(channelUID);
            if (RefreshType.REFRESH == command) {
                logger.debug("Refreshing {}", dpInfo);
                dpInfo = new HmDatapointInfo(dpInfo.getAddress(), HmParamsetType.VALUES, 0,
                        VIRTUAL_DATAPOINT_NAME_RELOAD_FROM_GATEWAY);
                dp = gateway.getDatapoint(dpInfo);
                sendDatapoint(dp, new HmDatapointConfig(), Boolean.TRUE);
            } else {
                Channel channel = getThing().getChannel(channelUID.getId());
                if (channel == null) {
                    logger.warn("Channel '{}' not found in thing '{}' on gateway '{}'", channelUID, getThing().getUID(),
                            gateway.getId());
                } else {
                    if (StopMoveType.STOP == command && DATAPOINT_NAME_LEVEL.equals(dpInfo.getName())) {
                        // special case with stop type (rollershutter)
                        dpInfo.setName(DATAPOINT_NAME_STOP);
                        HmDatapoint stopDp = gateway.getDatapoint(dpInfo);
                        ChannelUID stopChannelUID = UidUtils.generateChannelUID(stopDp, getThing().getUID());
                        handleCommand(stopChannelUID, OnOffType.ON);
                    } else {
                        dp = gateway.getDatapoint(dpInfo);
                        TypeConverter<?> converter = ConverterFactory.createConverter(channel.getAcceptedItemType());
                        Object newValue = converter.convertToBinding(command, dp);
                        HmDatapointConfig config = getChannelConfig(channel, dp);
                        sendDatapoint(dp, config, newValue);
                    }
                }
            }
        } catch (HomematicClientException | GatewayNotAvailableException ex) {
            logger.warn("{}", ex.getMessage());
        } catch (IOException ex) {
            if (dp != null && dp.getChannel().getDevice().isOffline()) {
                logger.warn("Device '{}' is OFFLINE, can't send command '{}' for channel '{}'",
                        dp.getChannel().getDevice().getAddress(), command, channelUID);
                logger.trace("{}", ex.getMessage(), ex);
            } else {
                logger.error("{}", ex.getMessage(), ex);
            }
        } catch (ConverterTypeException ex) {
            logger.warn("{}, please check the item type and the commands in your scripts", ex.getMessage());
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage(), ex);
        }
    }

    private void sendDatapoint(HmDatapoint dp, HmDatapointConfig config, Object newValue)
            throws IOException, HomematicClientException, GatewayNotAvailableException {
        String rxMode = getRxModeForDatapointTransmission(dp.getName(), dp.getValue(), newValue);
        getHomematicGateway().sendDatapoint(dp, config, newValue, rxMode);
    }

    /**
     * Returns the rx mode that shall be used for transmitting a new value of a datapoint to the device. The
     * HomematicThingHandler always uses the default rx mode; custom thing handlers can override this method to
     * adjust the rx mode.
     *
     * @param datapointName The datapoint that will be updated on the device
     * @param currentValue The current value of the datapoint
     * @param newValue The value that will be sent to the device
     * @return The rxMode ({@link HomematicBindingConstants#RX_BURST_MODE "BURST"} for burst mode,
     *         {@link HomematicBindingConstants#RX_WAKEUP_MODE "WAKEUP"} for wakeup mode, or null for the default mode)
     */
    protected String getRxModeForDatapointTransmission(String datapointName, Object currentValue, Object newValue) {
        return null;
    }

    /**
     * Evaluates the channel and datapoint for this channelUID and updates the state of the channel.
     */
    private void updateChannelState(ChannelUID channelUID)
            throws GatewayNotAvailableException, HomematicClientException, IOException, ConverterException {
        HomematicGateway gateway = getHomematicGateway();
        HmDatapointInfo dpInfo = UidUtils.createHmDatapointInfo(channelUID);
        HmDatapoint dp = gateway.getDatapoint(dpInfo);
        Channel channel = getThing().getChannel(channelUID.getId());
        updateChannelState(dp, channel);
    }

    /**
     * Sets the configuration or evaluates the channel for this datapoint and updates the state of the channel.
     */
    protected void updateDatapointState(HmDatapoint dp) {
        try {
            updateStatus(dp.getChannel().getDevice());

            if (dp.getParamsetType() == HmParamsetType.MASTER) {
                // update configuration
                Configuration config = editConfiguration();
                config.put(MetadataUtils.getParameterName(dp), dp.isEnumType() ? dp.getOptionValue() : dp.getValue());
                updateConfiguration(config);
            } else if (!HomematicTypeGeneratorImpl.isIgnoredDatapoint(dp)) {
                // update channel
                ChannelUID channelUID = UidUtils.generateChannelUID(dp, thing.getUID());
                Channel channel = thing.getChannel(channelUID.getId());
                if (channel != null) {
                    updateChannelState(dp, channel);
                } else {
                    logger.warn("Channel not found for datapoint '{}'", new HmDatapointInfo(dp));
                }
            }
        } catch (GatewayNotAvailableException ex) {
            // ignore
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage(), ex);
        }
    }

    /**
     * Converts the value of the datapoint to a State, updates the channel and also sets the thing status if necessary.
     */
    private void updateChannelState(final HmDatapoint dp, Channel channel)
            throws IOException, GatewayNotAvailableException, ConverterException {
        if (dp.isTrigger()) {
            final Object value = dp.getValue();
            if (value != null && !value.equals(dp.getPreviousValue())) {
                triggerChannel(channel.getUID(), value.toString());
            }
        } else if (isLinked(channel)) {
            loadHomematicChannelValues(dp.getChannel());

            TypeConverter<?> converter = ConverterFactory.createConverter(channel.getAcceptedItemType());
            State state = converter.convertFromBinding(dp);
            if (state != null) {
                updateState(channel.getUID(), state);
            } else {
                logger.debug("Failed to get converted state from datapoint '{}'", dp.getName());
            }
        }
    }

    /**
     * Loads all values for the given Homematic channel if it is not initialized.
     */
    private void loadHomematicChannelValues(HmChannel hmChannel) throws GatewayNotAvailableException, IOException {
        if (!hmChannel.isInitialized()) {
            synchronized (this) {
                if (!hmChannel.isInitialized()) {
                    try {
                        getHomematicGateway().loadChannelValues(hmChannel);
                    } catch (IOException ex) {
                        if (hmChannel.getDevice().isOffline()) {
                            logger.warn("Device '{}' is OFFLINE, can't update channel '{}'",
                                    hmChannel.getDevice().getAddress(), hmChannel.getNumber());
                        } else {
                            throw ex;
                        }
                    }
                }
            }
        }
    }

    /**
     * Updates the thing status based on device status.
     */
    private void updateStatus(HmDevice device) throws GatewayNotAvailableException, IOException {
        loadHomematicChannelValues(device.getChannel(0));

        ThingStatus oldStatus = thing.getStatus();
        if (oldStatus == ThingStatus.UNINITIALIZED) {
            return;
        }
        ThingStatus newStatus = ThingStatus.ONLINE;
        ThingStatusDetail newDetail = ThingStatusDetail.NONE;

        if ((getBridge() != null) && (getBridge().getStatus() == ThingStatus.OFFLINE)) {
            newStatus = ThingStatus.OFFLINE;
            newDetail = ThingStatusDetail.BRIDGE_OFFLINE;
        } else if (device.isFirmwareUpdating()) {
            newStatus = ThingStatus.OFFLINE;
            newDetail = ThingStatusDetail.FIRMWARE_UPDATING;
        } else if (device.isUnreach()) {
            newStatus = ThingStatus.OFFLINE;
            newDetail = ThingStatusDetail.COMMUNICATION_ERROR;
        } else if (device.isConfigPending() || device.isUpdatePending()) {
            newDetail = ThingStatusDetail.CONFIGURATION_PENDING;
        }

        if (thing.getStatus() != newStatus || thing.getStatusInfo().getStatusDetail() != newDetail) {
            updateStatus(newStatus, newDetail);
        }
        if (oldStatus == ThingStatus.OFFLINE && newStatus == ThingStatus.ONLINE) {
            initialize();
        }
    }

    /**
     * Returns true, if the channel is linked at least to one item.
     */
    private boolean isLinked(Channel channel) {
        return channel != null && super.isLinked(channel.getUID().getId());
    }

    /**
     * Returns the channel config for the given datapoint.
     */
    protected HmDatapointConfig getChannelConfig(HmDatapoint dp) {
        ChannelUID channelUid = UidUtils.generateChannelUID(dp, getThing().getUID());
        Channel channel = getThing().getChannel(channelUid.getId());
        return channel != null ? getChannelConfig(channel, dp) : new HmDatapointConfig();
    }

    /**
     * Returns the config for a channel.
     */
    private HmDatapointConfig getChannelConfig(Channel channel, HmDatapoint dp) {
        return channel.getConfiguration().as(HmDatapointConfig.class);
    }

    /**
     * Returns the Homematic gateway if the bridge is available.
     */
    private HomematicGateway getHomematicGateway() throws GatewayNotAvailableException {
        final Bridge bridge = getBridge();
        if (bridge != null) {
            HomematicBridgeHandler bridgeHandler = (HomematicBridgeHandler) bridge.getHandler();
            if (bridgeHandler != null && bridgeHandler.getGateway() != null) {
                return bridgeHandler.getGateway();
            }
        }

        throw new GatewayNotAvailableException("HomematicGateway not yet available!");
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters)
            throws ConfigValidationException {
        super.handleConfigurationUpdate(configurationParameters);

        try {
            HomematicGateway gateway = getHomematicGateway();
            HmDevice device = gateway.getDevice(UidUtils.getHomematicAddress(getThing()));

            for (Entry<String, Object> configurationParameter : configurationParameters.entrySet()) {
                String key = configurationParameter.getKey();
                Object newValue = configurationParameter.getValue();

                if (key.startsWith("HMP_")) {
                    key = key.substring(4);
                    int sepPos = key.indexOf("_");
                    Integer channelNumber = Integer.valueOf(key.substring(0, sepPos));
                    String dpName = key.substring(sepPos + 1);

                    HmDatapointInfo dpInfo = new HmDatapointInfo(device.getAddress(), HmParamsetType.MASTER,
                            channelNumber, dpName);
                    HmDatapoint dp = device.getChannel(channelNumber).getDatapoint(dpInfo);

                    if (dp != null) {
                        try {
                            if (newValue != null) {
                                if (newValue instanceof BigDecimal) {
                                    final BigDecimal decimal = (BigDecimal) newValue;
                                    if (dp.isIntegerType()) {
                                        newValue = decimal.intValue();
                                    } else if (dp.isFloatType()) {
                                        newValue = decimal.doubleValue();
                                    }
                                }
                                if (!Objects.equals(dp.isEnumType() ? dp.getOptionValue() : dp.getValue(), newValue)) {
                                    sendDatapoint(dp, new HmDatapointConfig(), newValue);
                                }
                            }
                        } catch (IOException ex) {
                            logger.error("Error setting thing property {}: {}", dpInfo, ex.getMessage());
                        }
                    } else {
                        logger.error("Can't find datapoint for thing property {}", dpInfo);
                    }
                }
            }
            gateway.triggerDeviceValuesReload(device);
        } catch (HomematicClientException | GatewayNotAvailableException ex) {
            logger.error("Error setting thing properties: {}", ex.getMessage(), ex);
        }
    }

    @SuppressWarnings("null")
    @Override
    public synchronized void handleRemoval() {
        final Bridge bridge;
        final ThingHandler handler;

        if ((bridge = getBridge()) == null || (handler = bridge.getHandler()) == null) {
            super.handleRemoval();
            return;
        }

        final HomematicConfig config = bridge.getConfiguration().as(HomematicConfig.class);
        final boolean factoryResetOnDeletion = config.isFactoryResetOnDeletion();
        final boolean unpairOnDeletion = factoryResetOnDeletion || config.isUnpairOnDeletion();

        if (unpairOnDeletion) {
            deviceDeletionPending = true;
            ((HomematicBridgeHandler) handler).deleteFromGateway(UidUtils.getHomematicAddress(thing),
                    factoryResetOnDeletion, false, true);
        } else {
            super.handleRemoval();
        }
    }

    /**
     * Called by the bridgeHandler when this device has been removed from the gateway.
     */
    public synchronized void deviceRemoved() {
        deviceDeletionPending = false;
        if (getThing().getStatus() == ThingStatus.REMOVING) {
            // thing removal was initiated
            updateStatus(ThingStatus.REMOVED);
        } else {
            // device removal was initiated on homematic side, thing is not removed
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE);
        }
    }

    /**
     * Called by the bridgeHandler when the device for this thing has been added to the gateway.
     * This is used to reconnect a device that was previously unpaired.
     *
     * @param device The device that has been added to the gateway
     */
    public void deviceLoaded(HmDevice device) {
        try {
            updateStatus(device);
        } catch (GatewayNotAvailableException ex) {
            // ignore
        } catch (IOException ex) {
            logger.warn("Could not reinitialize the device '{}': {}", device.getAddress(), ex.getMessage(), ex);
        }
    }

    /**
     * Returns whether the device deletion is pending.
     *
     * @return true, if the deletion of this device on its gateway has been triggered but has not yet completed
     */
    public synchronized boolean isDeletionPending() {
        return deviceDeletionPending;
    }
}
