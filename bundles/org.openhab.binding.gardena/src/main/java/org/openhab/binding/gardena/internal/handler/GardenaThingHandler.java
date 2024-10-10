/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.gardena.internal.GardenaSmart;
import org.openhab.binding.gardena.internal.GardenaSmartEventListener;
import org.openhab.binding.gardena.internal.exception.GardenaDeviceNotFoundException;
import org.openhab.binding.gardena.internal.exception.GardenaException;
import org.openhab.binding.gardena.internal.model.dto.Device;
import org.openhab.binding.gardena.internal.model.dto.api.CommonService;
import org.openhab.binding.gardena.internal.model.dto.api.DataItem;
import org.openhab.binding.gardena.internal.model.dto.command.GardenaCommand;
import org.openhab.binding.gardena.internal.model.dto.command.MowerCommand;
import org.openhab.binding.gardena.internal.model.dto.command.MowerCommand.MowerControl;
import org.openhab.binding.gardena.internal.model.dto.command.PowerSocketCommand;
import org.openhab.binding.gardena.internal.model.dto.command.PowerSocketCommand.PowerSocketControl;
import org.openhab.binding.gardena.internal.model.dto.command.ValveCommand;
import org.openhab.binding.gardena.internal.model.dto.command.ValveCommand.ValveControl;
import org.openhab.binding.gardena.internal.model.dto.command.ValveSetCommand;
import org.openhab.binding.gardena.internal.model.dto.command.ValveSetCommand.ValveSetControl;
import org.openhab.binding.gardena.internal.util.PropertyUtils;
import org.openhab.binding.gardena.internal.util.StringUtils;
import org.openhab.binding.gardena.internal.util.UidUtils;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GardenaThingHandler} is responsible for handling commands, which are sent to one of the channels.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
public class GardenaThingHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(GardenaThingHandler.class);
    private TimeZoneProvider timeZoneProvider;
    private @Nullable ScheduledFuture<?> commandResetFuture;
    private Map<String, Integer> commandDurations = new HashMap<>();

    public GardenaThingHandler(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing);
        this.timeZoneProvider = timeZoneProvider;
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

    @Override
    public void dispose() {
        final ScheduledFuture<?> commandResetFuture = this.commandResetFuture;
        if (commandResetFuture != null) {
            commandResetFuture.cancel(true);
        }
        super.dispose();
    }

    /**
     * Updates the thing properties from the Gardena device.
     */
    protected void updateProperties(Device device) throws GardenaException {
        Map<String, String> properties = editProperties();
        String serial = PropertyUtils.getPropertyValue(device, "common.attributes.serial.value", String.class);
        if (serial != null) {
            properties.put(PROPERTY_SERIALNUMBER, serial);
        }
        String modelType = PropertyUtils.getPropertyValue(device, "common.attributes.modelType.value", String.class);
        if (modelType != null) {
            properties.put(PROPERTY_MODELTYPE, modelType);
        }
        updateProperties(properties);
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        try {
            updateChannel(channelUID);
        } catch (GardenaDeviceNotFoundException | AccountHandlerNotAvailableException ex) {
            logger.debug("{}", ex.getMessage(), ex);
        } catch (GardenaException ex) {
            logger.warn("{}", ex.getMessage(), ex);
        }
    }

    /**
     * Updates the channel from the Gardena device.
     */
    protected void updateChannel(ChannelUID channelUID) throws GardenaException, AccountHandlerNotAvailableException {
        String groupId = channelUID.getGroupId();
        if (groupId == null) {
            return;
        }
        if (isLocalDurationCommand(channelUID)) {
            int commandDuration = getCommandDurationSeconds(getDeviceDataItemProperty(channelUID));
            updateState(channelUID, new QuantityType<>(commandDuration, Units.SECOND));
        } else if (!groupId.endsWith("_commands")) {
            Device device = getDevice();
            State state = convertToState(device, channelUID);
            if (state != null) {
                updateState(channelUID, state);
            }
        }
    }

    /**
     * Converts a Gardena property value to an openHAB state.
     */
    private @Nullable State convertToState(Device device, ChannelUID channelUID) throws GardenaException {
        String propertyPath = channelUID.getGroupId() + ".attributes.";
        String propertyName = channelUID.getIdWithoutGroup();
        String unitPropertyPath = propertyPath;

        if (propertyName.endsWith("_timestamp")) {
            propertyPath += propertyName.replace("_", ".");
        } else {
            propertyPath += propertyName + ".value";
            unitPropertyPath += propertyName + "Unit";
        }

        Channel channel = getThing().getChannel(channelUID.getId());
        String acceptedItemType = channel != null ? channel.getAcceptedItemType() : null;
        String baseItemType = StringUtils.substringBefore(acceptedItemType, ":");

        boolean isNullPropertyValue = PropertyUtils.isNull(device, propertyPath);

        if (isNullPropertyValue) {
            return UnDefType.NULL;
        }
        if (baseItemType == null || acceptedItemType == null) {
            return null;
        }

        try {
            switch (baseItemType) {
                case "String":
                    return new StringType(PropertyUtils.getPropertyValue(device, propertyPath, String.class));
                case "Number":
                    if (isNullPropertyValue) {
                        return new DecimalType(0);
                    } else {
                        Number value = PropertyUtils.getPropertyValue(device, propertyPath, Number.class);
                        Unit<?> unit = PropertyUtils.getPropertyValue(device, unitPropertyPath, Unit.class);
                        if (value == null) {
                            return UnDefType.NULL;
                        } else {
                            if ("rfLinkLevel".equals(propertyName)) {
                                // Gardena gives us link level as 0..100%, while the system.signal-strength
                                // channel type wants a 0..4 enum
                                int percent = value.intValue();
                                value = percent == 100 ? 4 : percent / 20;
                                unit = null;
                            }
                            if (acceptedItemType.equals(baseItemType) || unit == null) {
                                // No UoM or no unit found
                                return new DecimalType(value);
                            } else {
                                return new QuantityType<>(value, unit);
                            }
                        }
                    }
                case "DateTime":
                    Date date = PropertyUtils.getPropertyValue(device, propertyPath, Date.class);
                    if (date == null) {
                        return UnDefType.NULL;
                    } else {
                        ZonedDateTime zdt = ZonedDateTime.ofInstant(date.toInstant(), timeZoneProvider.getTimeZone());
                        return new DateTimeType(zdt);
                    }
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
            boolean isOnCommand = command instanceof OnOffType onOffCommand && onOffCommand == OnOffType.ON;
            String dataItemProperty = getDeviceDataItemProperty(channelUID);
            if (RefreshType.REFRESH == command) {
                logger.debug("Refreshing Gardena connection");
                getGardenaSmart().restartWebsockets();
            } else if (isLocalDurationCommand(channelUID)) {
                QuantityType<?> commandInSeconds = null;
                if (command instanceof QuantityType<?> timeCommand) {
                    commandInSeconds = timeCommand.toUnit(Units.SECOND);
                }
                if (commandInSeconds != null) {
                    commandDurations.put(dataItemProperty, commandInSeconds.intValue());
                } else {
                    logger.info("Invalid command '{}' for command duration channel, ignoring.", command);
                }
            } else if (isOnCommand) {
                GardenaCommand gardenaCommand = getGardenaCommand(dataItemProperty, channelUID);
                logger.debug("Received Gardena command: {}, {}", gardenaCommand.getClass().getSimpleName(),
                        gardenaCommand.attributes.command);

                DataItem<?> dataItem = PropertyUtils.getPropertyValue(getDevice(), dataItemProperty, DataItem.class);
                if (dataItem == null) {
                    logger.warn("DataItem {} is empty, ignoring command.", dataItemProperty);
                } else {
                    getGardenaSmart().sendCommand(dataItem, gardenaCommand);

                    commandResetFuture = scheduler.schedule(() -> {
                        updateState(channelUID, OnOffType.OFF);
                    }, 3, TimeUnit.SECONDS);
                }
            }
        } catch (AccountHandlerNotAvailableException | GardenaDeviceNotFoundException ex) {
            // ignore
        } catch (Exception ex) {
            logger.warn("{}", ex.getMessage());
            final Bridge bridge;
            final ThingHandler handler;
            if ((bridge = getBridge()) != null && (handler = bridge.getHandler()) != null) {
                ((GardenaSmartEventListener) handler).onError();
            }
        }
    }

    /**
     * Returns the Gardena command from the channel.
     */
    private GardenaCommand getGardenaCommand(String dataItemProperty, ChannelUID channelUID)
            throws GardenaException, AccountHandlerNotAvailableException {
        String commandName = channelUID.getIdWithoutGroup().toUpperCase();
        String groupId = channelUID.getGroupId();
        if (groupId != null) {
            int commandDuration = getCommandDurationSeconds(dataItemProperty);
            if ("valveSet_commands".equals(groupId)) {
                return new ValveSetCommand(ValveSetControl.valueOf(commandName));
            } else if (groupId.startsWith("valve") && groupId.endsWith("_commands")) {
                return new ValveCommand(ValveControl.valueOf(commandName), commandDuration);
            } else if ("mower_commands".equals(groupId)) {
                return new MowerCommand(MowerControl.valueOf(commandName), commandDuration);
            } else if ("powerSocket_commands".equals(groupId)) {
                return new PowerSocketCommand(PowerSocketControl.valueOf(commandName), commandDuration);
            }
        }
        throw new GardenaException("Command " + channelUID.getId() + " not found or groupId null");
    }

    /**
     * Updates the thing status based on the Gardena device status.
     */
    protected void updateStatus(Device device) {
        ThingStatus oldStatus = thing.getStatus();
        ThingStatus newStatus = ThingStatus.ONLINE;
        ThingStatusDetail newDetail = ThingStatusDetail.NONE;

        CommonService commonServiceAttributes = device.common.attributes;
        if (commonServiceAttributes == null
                || !CONNECTION_STATUS_ONLINE.equals(commonServiceAttributes.rfLinkState.value)) {
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
    private String getDeviceDataItemProperty(ChannelUID channelUID) throws GardenaException {
        String dataItemProperty = StringUtils.substringBeforeLast(channelUID.getGroupId(), "_");
        if (dataItemProperty != null) {
            return dataItemProperty;
        }
        throw new GardenaException("Can't extract dataItemProperty from channel group " + channelUID.getGroupId());
    }

    private int getCommandDurationSeconds(String dataItemProperty) {
        Integer duration = commandDurations.get(dataItemProperty);
        return duration != null ? duration : 3600;
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
     * Returns the Gardena smart system implementation if the bridge is available.
     */
    private GardenaSmart getGardenaSmart() throws AccountHandlerNotAvailableException {
        final Bridge bridge;
        final ThingHandler handler;
        if ((bridge = getBridge()) != null && (handler = bridge.getHandler()) != null) {
            final GardenaSmart gardenaSmart = ((GardenaAccountHandler) handler).getGardenaSmart();
            if (gardenaSmart != null) {
                return gardenaSmart;
            }
        }
        if (thing.getStatus() != ThingStatus.INITIALIZING) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_MISSING_ERROR);
        }
        throw new AccountHandlerNotAvailableException("Gardena AccountHandler not yet available!");
    }
}
