/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.gardena.internal.handler;

import static org.openhab.binding.gardena.internal.GardenaBindingConstants.*;
import static org.openhab.binding.gardena.internal.model.command.MowerCommand.MowerControl;
import static org.openhab.binding.gardena.internal.model.command.PowerSocketCommand.PowerSocketControl;
import static org.openhab.binding.gardena.internal.model.command.ValveCommand.ValveControl;
import static org.openhab.binding.gardena.internal.model.command.ValveSetCommand.ValveSetControl;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.library.types.*;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.gardena.internal.GardenaSmart;
import org.openhab.binding.gardena.internal.GardenaSmartEventListener;
import org.openhab.binding.gardena.internal.exception.GardenaDeviceNotFoundException;
import org.openhab.binding.gardena.internal.exception.GardenaException;
import org.openhab.binding.gardena.internal.model.Device;
import org.openhab.binding.gardena.internal.model.api.DataItem;
import org.openhab.binding.gardena.internal.model.command.*;
import org.openhab.binding.gardena.internal.util.PropertyUtils;
import org.openhab.binding.gardena.internal.util.UidUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GardenaThingHandler} is responsible for handling commands, which are sent to one of the channels.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class GardenaThingHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(GardenaThingHandler.class);

    public GardenaThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        try {
            Device device = getDevice();
            updateProperties(device);
            updateStatus(device);
        } catch (GardenaException ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, ex.getMessage());
        } catch (AccountHandlerNotAvailableException ex) {
            // ignore
        }
    }

    /**
     * Updates the thing properties from the Gardena device.
     */
    protected void updateProperties(Device device) throws GardenaException {
        Map<String, String> properties = editProperties();
        properties.put(PROPERTY_SERIALNUMBER,
                PropertyUtils.getPropertyValue(device, "common.attributes.serial.value", String.class));
        properties.put(PROPERTY_MODELTYPE,
                PropertyUtils.getPropertyValue(device, "common.attributes.modelType.value", String.class));
        updateProperties(properties);
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        try {
            updateChannel(channelUID);
        } catch (GardenaDeviceNotFoundException | AccountHandlerNotAvailableException ex) {
            logger.debug("{}", ex.getMessage(), ex);
        } catch (GardenaException ex) {
            logger.error("{}", ex.getMessage(), ex);
        }
    }

    /**
     * Updates the channel from the Gardena device.
     */
    protected void updateChannel(ChannelUID channelUID) throws GardenaException, AccountHandlerNotAvailableException {
        boolean isCommand = channelUID.getGroupId().endsWith("_commands");
        if (!isCommand || (isCommand && isLocalDurationCommand(channelUID))) {
            Device device = getDevice();
            State state = convertToState(device, channelUID);
            if (state != null) {
                updateState(channelUID, state);
            }
        }
    }

    /**
     * Converts a Gardena property value to a openHAB state.
     */
    private State convertToState(Device device, ChannelUID channelUID) throws GardenaException {
        if (isLocalDurationCommand(channelUID)) {
            String dataItemProperty = getDeviceDataItemProperty(channelUID);
            return new DecimalType(Math.round(device.getLocalService(dataItemProperty).commandDuration / 60.0));
        }

        String propertyPath = channelUID.getGroupId() + ".attributes.";
        String propertyName = channelUID.getIdWithoutGroup();

        if (propertyName.endsWith("_timestamp")) {
            propertyPath += propertyName.replace("_", ".");
        } else {
            propertyPath += propertyName + ".value";
        }

        String acceptedItemType = StringUtils
                .substringBefore(getThing().getChannel(channelUID.getId()).getAcceptedItemType(), ":");

        try {
            boolean isNullPropertyValue = PropertyUtils.isNull(device, propertyPath);
            boolean isDurationProperty = "duration".equals(propertyName);

            if (isNullPropertyValue && !isDurationProperty) {
                return UnDefType.NULL;
            }
            switch (acceptedItemType) {
                case "String":
                    return new StringType(PropertyUtils.getPropertyValue(device, propertyPath, String.class));
                case "Number":
                    if (isNullPropertyValue) {
                        return new DecimalType(0);
                    } else {
                        long value = PropertyUtils.getPropertyValue(device, propertyPath, Number.class).longValue();
                        // convert duration from seconds to minutes
                        if (isDurationProperty) {
                            value = Math.round(value / 60.0);
                        }
                        return new DecimalType(value);
                    }
                case "DateTime":
                    Date date = PropertyUtils.getPropertyValue(device, propertyPath, Date.class);
                    ZonedDateTime zdt = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
                    return new DateTimeType(zdt);
            }
        } catch (GardenaException e) {
            logger.warn("Channel '{}' cannot be updated as device does not contain propertyPath '{}'", channelUID,
                    propertyPath);
        } catch (ClassCastException ex) {
            logger.warn("Value of propertyPath '{}' can not be casted to {}: {}", propertyPath, acceptedItemType,
                    ex.getMessage());
        }
        return null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Command received: {}", command);
        try {
            boolean isOnCommand = command instanceof OnOffType && ((OnOffType) command) == OnOffType.ON;
            String dataItemProperty = getDeviceDataItemProperty(channelUID);
            if (RefreshType.REFRESH == command) {
                logger.debug("Refreshing Gardena connection");
                getGardenaSmart().restartWebsockets();
            } else if (isLocalDurationCommand(channelUID)) {
                QuantityType<?> quantityType = (QuantityType<?>) command;
                getDevice().getLocalService(dataItemProperty).commandDuration = quantityType.intValue() * 60;
            } else if (isOnCommand) {
                GardenaCommand gardenaCommand = getGardenaCommand(dataItemProperty, channelUID);
                if (gardenaCommand != null) {
                    logger.debug("Received Gardena command: {}, {}", gardenaCommand.getClass().getSimpleName(),
                            gardenaCommand.attributes.command);

                    DataItem<?> dataItem = PropertyUtils.getPropertyValue(getDevice(), dataItemProperty, DataItem.class);
                    if (dataItem == null) {
                        logger.warn("DataItem {} is empty, ignoring command.", dataItemProperty);
                    } else {
                        getGardenaSmart().sendCommand(dataItem, gardenaCommand);

                        scheduler.schedule(() -> {
                            updateState(channelUID, OnOffType.OFF);
                        }, 3, TimeUnit.SECONDS);
                    }
                }
            }
        } catch (AccountHandlerNotAvailableException | GardenaDeviceNotFoundException ex) {
            // ignore
        } catch (Exception ex) {
            logger.warn("{}", ex.getMessage(), ex);
            ((GardenaSmartEventListener) getBridge().getHandler()).onError();
        }
    }

    /**
     * Returns the Gardena command from the channel.
     */
    private GardenaCommand getGardenaCommand(String dataItemProperty, ChannelUID channelUID)
            throws GardenaException, AccountHandlerNotAvailableException {
        String commandName = channelUID.getIdWithoutGroup().toUpperCase();
        if (StringUtils.startsWith(channelUID.getGroupId(), "valve_")
                && StringUtils.endsWith(channelUID.getGroupId(), "_commands")) {
            return new ValveCommand(ValveControl.valueOf(commandName),
                    getDevice().getLocalService(dataItemProperty).commandDuration);
        } else if ("mower_commands".equals(channelUID.getGroupId())) {
            return new MowerCommand(MowerControl.valueOf(commandName),
                    getDevice().getLocalService(dataItemProperty).commandDuration);
        } else if ("valveSet_commands".equals(channelUID.getGroupId())) {
            return new ValveSetCommand(ValveSetControl.valueOf(commandName));
        } else if ("powerSocket_commands".equals(channelUID.getGroupId())) {
            return new PowerSocketCommand(PowerSocketControl.valueOf(commandName),
                    getDevice().getLocalService(dataItemProperty).commandDuration);
        }
        throw new GardenaException("Command " + channelUID.getId() + " not found");
    }

    /**
     * Updates the thing status based on the Gardena device status.
     */
    protected void updateStatus(Device device) {
        ThingStatus oldStatus = thing.getStatus();
        ThingStatus newStatus = ThingStatus.ONLINE;
        ThingStatusDetail newDetail = ThingStatusDetail.NONE;

        if (!CONNECTION_STATUS_ONLINE.equals(device.common.attributes.rfLinkState.value)) {
            newStatus = ThingStatus.OFFLINE;
            newDetail = ThingStatusDetail.COMMUNICATION_ERROR;
        }

        if (oldStatus != newStatus || thing.getStatusInfo().getStatusDetail() != newDetail) {
            updateStatus(newStatus, newDetail);
        }
    }

    /**
     * Returns the device property for the dataItem from the channel.
     */
    private String getDeviceDataItemProperty(ChannelUID channelUID) {
        return StringUtils.substringBeforeLast(channelUID.getGroupId(), "_");
    }

    /**
     * Returns true, if the channel is the duration command.
     */
    private boolean isLocalDurationCommand(ChannelUID channelUID) {
        return "commandDuration".equals(channelUID.getIdWithoutGroup());
    }

    /**
     * Returns the Gardena device for this ThingHandler.
     */
    private Device getDevice() throws GardenaException, AccountHandlerNotAvailableException {
        return getGardenaSmart().getDevice(UidUtils.getGardenaDeviceId(getThing()));
    }

    /**
     * Returns the Gardena account handler if the bridge is available.
     */
    private GardenaAccountHandler getGardenaAccountHandler() throws AccountHandlerNotAvailableException {
        if (getBridge() == null || getBridge().getHandler() == null
                || ((GardenaAccountHandler) getBridge().getHandler()).getGardenaSmart() == null) {
            if (thing.getStatus() != ThingStatus.INITIALIZING) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_MISSING_ERROR);
            }
            throw new AccountHandlerNotAvailableException("Gardena AccountHandler not yet available!");
        }

        return ((GardenaAccountHandler) getBridge().getHandler());
    }

    /**
     * Returns the Gardena smart system implementation if the bridge is available.
     */
    private GardenaSmart getGardenaSmart() throws AccountHandlerNotAvailableException {
        return getGardenaAccountHandler().getGardenaSmart();
    }
}
