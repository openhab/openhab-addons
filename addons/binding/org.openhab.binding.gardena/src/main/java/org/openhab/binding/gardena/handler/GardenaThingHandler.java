/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gardena.handler;

import static org.openhab.binding.gardena.GardenaBindingConstants.*;

import java.util.Calendar;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.gardena.internal.GardenaSmart;
import org.openhab.binding.gardena.internal.exception.GardenaDeviceNotFoundException;
import org.openhab.binding.gardena.internal.exception.GardenaException;
import org.openhab.binding.gardena.internal.model.Ability;
import org.openhab.binding.gardena.internal.model.Device;
import org.openhab.binding.gardena.internal.model.Property;
import org.openhab.binding.gardena.internal.util.DateUtils;
import org.openhab.binding.gardena.util.UidUtils;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize() {
        try {
            GardenaSmart gardena = getGardenaSmart();

            String deviceId = getConfig().as(GardenaDeviceConfig.class).deviceId;
            if (deviceId == null) {
                deviceId = UidUtils.getGardenaDeviceId(getThing());
            }
            Device device = gardena.getDevice(deviceId);

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
        Ability deviceInfo = device.getAbility(ABILITY_DEVICE_INFO);
        properties.put(PROPERTY_MANUFACTURER, deviceInfo.getProperty(PROPERTY_MANUFACTURER).getValue());
        properties.put(PROPERTY_PRODUCT, deviceInfo.getProperty(PROPERTY_PRODUCT).getValue());
        properties.put(PROPERTY_SERIALNUMBER, deviceInfo.getProperty(PROPERTY_SERIALNUMBER).getValue());
        properties.put(PROPERTY_SGTIN, deviceInfo.getProperty(PROPERTY_SGTIN).getValue());
        properties.put(PROPERTY_VERSION, deviceInfo.getProperty(PROPERTY_VERSION).getValue());
        properties.put(PROPERTY_CATEGORY, deviceInfo.getProperty(PROPERTY_CATEGORY).getValue());
        updateProperties(properties);
    }

    /**
     * Updates the channel from the Gardena device.
     */
    protected void updateChannel(ChannelUID channelUID) throws GardenaException, AccountHandlerNotAvailableException {
        Device device = getDevice();
        updateState(channelUID, convertToState(device, channelUID));
    }

    /**
     * Converts a Gardena property value to a openHab state.
     */
    private State convertToState(Device device, ChannelUID channelUID) throws GardenaException {
        String abilityName = channelUID.getGroupId();
        String propertyName = channelUID.getIdWithoutGroup();
        String value = device.getAbility(abilityName).getProperty(propertyName).getValue();

        if (StringUtils.trimToNull(value) == null || StringUtils.equals(value, "N/A")
                || StringUtils.startsWith(value, "1970-01-01")) {
            return UnDefType.NULL;
        }

        switch (getThing().getChannel(channelUID.getId()).getAcceptedItemType()) {
            case "String":
                return new StringType(value);
            case "Number":
                if (ABILITY_RADIO.equals(abilityName) && PROPERTY_STATE.equals(propertyName)) {
                    switch (value) {
                        case "poor":
                            return new DecimalType(1);
                        case "good":
                            return new DecimalType(2);
                        case "excellent":
                            return new DecimalType(4);
                        default:
                            return UnDefType.NULL;
                    }
                }
                return new DecimalType(value);
            case "Switch":
                return Boolean.TRUE.toString().equalsIgnoreCase(value) ? OnOffType.ON : OnOffType.OFF;
            case "DateTime":
                Calendar cal = DateUtils.parseToCalendar(value);
                if (cal != null) {
                    return new DateTimeType(cal);
                }
        }
        return UnDefType.NULL;
    }

    /**
     * Converts an openHAB type to a Gardena command property.
     */
    private Object convertFromType(Type type) {
        if (type instanceof OnOffType) {
            return type == OnOffType.ON ? Boolean.TRUE : Boolean.FALSE;
        } else if (type instanceof DecimalType) {
            return ((DecimalType) type).intValue();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            String abilityName = channelUID.getGroupId();
            String propertyName = channelUID.getIdWithoutGroup();

            if (RefreshType.REFRESH == command) {
                logger.debug("Refreshing channel '{}'", channelUID);
                updateChannel(channelUID);
            } else {
                Property commandProperty = getDevice().getAbility(abilityName).getProperty(propertyName);
                getGardenaSmart().sendCommand(commandProperty, convertFromType(command));
            }
        } catch (AccountHandlerNotAvailableException | GardenaDeviceNotFoundException ex) {
            // ignore
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    /**
     * Updates the thing status based on the Gardena device status.
     */
    protected void updateStatus(Device device) {
        String connectionStatus = "";
        try {
            connectionStatus = device.getAbility(ABILITY_RADIO).getProperty(PROPERTY_CONNECTION_STATUS).getValue();
        } catch (GardenaException ex) {
            // ignore, device has no connection status property
        }

        boolean isUnreach = PROPERTY_CONNECTION_STATUS_UNREACH_VALUE.equals(connectionStatus);

        ThingStatus oldStatus = thing.getStatus();
        ThingStatus newStatus = ThingStatus.ONLINE;
        ThingStatusDetail newDetail = ThingStatusDetail.NONE;

        if (isUnreach) {
            newStatus = ThingStatus.OFFLINE;
            newDetail = ThingStatusDetail.COMMUNICATION_ERROR;
        } else if (!device.isConfigurationSynchronized()) {
            newStatus = thing.getStatus();
            newDetail = ThingStatusDetail.CONFIGURATION_PENDING;
        }

        if (oldStatus != newStatus || thing.getStatusInfo().getStatusDetail() != newDetail) {
            updateStatus(newStatus, newDetail);
        }
    }

    /**
     * Returns the Gardena device for this ThingHandler.
     */
    private Device getDevice() throws GardenaException, AccountHandlerNotAvailableException {
        return getGardenaSmart().getDevice(UidUtils.getGardenaDeviceId(getThing()));
    }

    /**
     * Returns the Gardena Smart Home implementation if the bridge is available.
     */
    private GardenaSmart getGardenaSmart() throws AccountHandlerNotAvailableException {
        if (getBridge() == null || getBridge().getHandler() == null
                || ((GardenaAccountHandler) getBridge().getHandler()).getGardenaSmart() == null) {
            if (thing.getStatus() != ThingStatus.INITIALIZING) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_MISSING_ERROR);
            }
            throw new AccountHandlerNotAvailableException("Gardena AccountHandler not yet available!");
        }

        return ((GardenaAccountHandler) getBridge().getHandler()).getGardenaSmart();
    }

}
