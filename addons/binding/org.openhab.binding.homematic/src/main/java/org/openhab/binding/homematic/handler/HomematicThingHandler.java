/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.handler;

import static org.openhab.binding.homematic.HomematicBindingConstants.*;
import static org.openhab.binding.homematic.internal.misc.HomematicConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.core.validation.ConfigValidationException;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.homematic.converter.ConverterException;
import org.openhab.binding.homematic.converter.ConverterFactory;
import org.openhab.binding.homematic.converter.ConverterTypeException;
import org.openhab.binding.homematic.converter.TypeConverter;
import org.openhab.binding.homematic.internal.communicator.HomematicGateway;
import org.openhab.binding.homematic.internal.misc.HomematicClientException;
import org.openhab.binding.homematic.internal.model.HmChannel;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmDatapointConfig;
import org.openhab.binding.homematic.internal.model.HmDatapointInfo;
import org.openhab.binding.homematic.internal.model.HmDevice;
import org.openhab.binding.homematic.internal.model.HmParamsetType;
import org.openhab.binding.homematic.type.HomematicTypeGeneratorImpl;
import org.openhab.binding.homematic.type.MetadataUtils;
import org.openhab.binding.homematic.type.UidUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HomematicThingHandler} is responsible for handling commands, which are sent to one of the channels.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class HomematicThingHandler extends BaseThingHandler {
    private Logger logger = LoggerFactory.getLogger(HomematicThingHandler.class);

    public HomematicThingHandler(Thing thing) {
        super(thing);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize() {
        scheduler.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    HomematicGateway gateway = getHomematicGateway();
                    HmDevice device = gateway.getDevice(UidUtils.getHomematicAddress(getThing()));
                    updateStatus(device);
                    if (!device.isOffline()) {
                        logger.debug("Initializing {} channels of thing '{}' from gateway '{}'",
                                getThing().getChannels().size(), getThing().getUID(), gateway.getId());

                        // update channel states
                        for (Channel channel : getThing().getChannels()) {
                            updateChannelState(channel.getUID());
                        }

                        // update properties
                        Map<String, String> properties = editProperties();
                        setProperty(properties, device, PROPERTY_BATTERY_TYPE, VIRTUAL_DATAPOINT_NAME_BATTERY_TYPE);
                        setProperty(properties, device, Thing.PROPERTY_FIRMWARE_VERSION,
                                VIRTUAL_DATAPOINT_NAME_FIRMWARE);
                        setProperty(properties, device, Thing.PROPERTY_SERIAL_NUMBER, device.getAddress());
                        setProperty(properties, device, PROPERTY_AES_KEY, DATAPOINT_NAME_AES_KEY);
                        updateProperties(properties);

                        // update configurations
                        Configuration config = editConfiguration();
                        for (HmChannel channel : device.getChannels()) {
                            for (HmDatapoint dp : channel.getDatapoints().values()) {
                                if (dp.getParamsetType() == HmParamsetType.MASTER) {
                                    loadHomematicChannelValues(dp.getChannel());
                                    config.put(MetadataUtils.getParameterName(dp),
                                            dp.isEnumType() ? dp.getOptionValue() : dp.getValue());
                                }
                            }
                        }
                        updateConfiguration(config);
                    }
                } catch (HomematicClientException ex) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, ex.getMessage());
                } catch (IOException ex) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
                } catch (BridgeHandlerNotAvailableException ex) {
                    // ignore
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
        });
    }

    /**
     * Sets a thing property with a datapoint value.
     */
    private void setProperty(Map<String, String> properties, HmDevice device, String propertyName,
            String datapointName) {
        HmChannel channelZero = device.getChannel(0);
        HmDatapoint dp = channelZero
                .getDatapoint(new HmDatapointInfo(HmParamsetType.VALUES, channelZero, datapointName));
        if (dp != null) {
            properties.put(propertyName, ObjectUtils.toString(dp.getValue()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void channelLinked(ChannelUID channelUID) {
        try {
            if (thing.getStatus() == ThingStatus.ONLINE) {
                logger.debug("Channel linked '{}' from thing id '{}'", channelUID, getThing().getUID().getId());
                updateChannelState(channelUID);
            }
        } catch (Exception ex) {
            logger.warn(ex.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
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
                gateway.sendDatapoint(dp, new HmDatapointConfig(), Boolean.TRUE);
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
                        gateway.sendDatapoint(dp, config, newValue);
                    }
                }
            }
        } catch (HomematicClientException | BridgeHandlerNotAvailableException ex) {
            logger.warn(ex.getMessage());
        } catch (IOException ex) {
            if (dp != null && dp.getChannel().getDevice().isOffline()) {
                logger.warn("Device '{}' is OFFLINE, can't send command '{}' for channel '{}'",
                        dp.getChannel().getDevice().getAddress(), command, channelUID);
                logger.trace(ex.getMessage(), ex);
            } else {
                logger.error(ex.getMessage(), ex);
            }
        } catch (ConverterTypeException ex) {
            logger.warn("{}, please check the item type and the commands in your scripts", ex.getMessage());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    /**
     * Evaluates the channel and datapoint for this channelUID and updates the state of the channel.
     */
    private void updateChannelState(ChannelUID channelUID)
            throws BridgeHandlerNotAvailableException, HomematicClientException, IOException, ConverterException {
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
            if (HomematicTypeGeneratorImpl.isStatusDatapoint(dp)) {
                updateStatus(dp.getChannel().getDevice());
            }
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
        } catch (BridgeHandlerNotAvailableException ex) {
            // ignore
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    /**
     * Converts the value of the datapoint to a State, updates the channel and also sets the thing status if necessary.
     */
    private void updateChannelState(final HmDatapoint dp, Channel channel)
            throws IOException, BridgeHandlerNotAvailableException, ConverterException {

        boolean isChannelLinked = isLinked(channel);
        if (isChannelLinked) {
            loadHomematicChannelValues(dp.getChannel());

            TypeConverter<?> converter = ConverterFactory.createConverter(channel.getAcceptedItemType());
            State state = converter.convertFromBinding(dp);
            updateState(channel.getUID(), state);
        }
    }

    /**
     * Loads all values for the given Homematic channel if it is not initialized.
     */
    private void loadHomematicChannelValues(HmChannel hmChannel)
            throws BridgeHandlerNotAvailableException, IOException {
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
    private void updateStatus(HmDevice device) throws BridgeHandlerNotAvailableException, IOException {
        loadHomematicChannelValues(device.getChannel(0));

        ThingStatus oldStatus = thing.getStatus();
        ThingStatus newStatus = ThingStatus.ONLINE;
        ThingStatusDetail newDetail = ThingStatusDetail.NONE;

        if (device.isFirmwareUpdating()) {
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
     * {@inheritDoc}
     */
    @Override
    protected void updateStatus(ThingStatus status) {
        super.updateStatus(status);
    }

    /**
     * Returns true, if the channel is linked at least to one item.
     */
    private boolean isLinked(Channel channel) {
        return channel != null && super.isLinked(channel.getUID().getId());
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
    private HomematicGateway getHomematicGateway() throws BridgeHandlerNotAvailableException {
        if (getBridge() == null || getBridge().getHandler() == null
                || ((HomematicBridgeHandler) getBridge().getHandler()).getGateway() == null) {
            if (thing.getStatus() != ThingStatus.INITIALIZING) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_MISSING_ERROR);
            }
            throw new BridgeHandlerNotAvailableException("BridgeHandler not yet available!");
        }

        return ((HomematicBridgeHandler) getBridge().getHandler()).getGateway();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters)
            throws ConfigValidationException {
        validateConfigurationParameters(configurationParameters);

        try {
            HomematicGateway gateway = getHomematicGateway();
            HmDevice device = gateway.getDevice(UidUtils.getHomematicAddress(getThing()));

            for (Entry<String, Object> configurationParmeter : configurationParameters.entrySet()) {
                String key = configurationParmeter.getKey();
                Object newValue = configurationParmeter.getValue();

                if (key.startsWith("HMP_")) {
                    key = StringUtils.removeStart(key, "HMP_");
                    Integer channelNumber = NumberUtils.toInt(StringUtils.substringBefore(key, "_"));
                    String dpName = StringUtils.substringAfter(key, "_");

                    HmDatapointInfo dpInfo = new HmDatapointInfo(device.getAddress(), HmParamsetType.MASTER,
                            channelNumber, dpName);
                    HmDatapoint dp = device.getChannel(channelNumber).getDatapoint(dpInfo);

                    if (dp != null) {
                        try {
                            if (newValue != null) {
                                if (newValue instanceof BigDecimal) {
                                    if (dp.isIntegerType()) {
                                        newValue = ((BigDecimal) newValue).intValue();
                                    } else if (dp.isFloatType()) {
                                        newValue = ((BigDecimal) newValue).doubleValue();
                                    }
                                }
                                if (ObjectUtils.notEqual(dp.isEnumType() ? dp.getOptionValue() : dp.getValue(),
                                        newValue)) {
                                    gateway.sendDatapoint(dp, new HmDatapointConfig(), newValue);
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
        } catch (HomematicClientException | BridgeHandlerNotAvailableException ex) {
            logger.error("Error setting thing properties: " + ex.getMessage(), ex);
        }
    }
}
