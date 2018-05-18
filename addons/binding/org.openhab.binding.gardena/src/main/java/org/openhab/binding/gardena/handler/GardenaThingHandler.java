/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gardena.handler;

import static org.openhab.binding.gardena.GardenaBindingConstants.*;
import static org.openhab.binding.gardena.internal.GardenaSmartCommandName.*;

import java.util.Calendar;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.core.validation.ConfigValidationException;
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
import org.openhab.binding.gardena.internal.GardenaSmartCommandName;
import org.openhab.binding.gardena.internal.GardenaSmartImpl;
import org.openhab.binding.gardena.internal.exception.GardenaDeviceNotFoundException;
import org.openhab.binding.gardena.internal.exception.GardenaException;
import org.openhab.binding.gardena.internal.model.Ability;
import org.openhab.binding.gardena.internal.model.Device;
import org.openhab.binding.gardena.internal.model.Setting;
import org.openhab.binding.gardena.internal.util.DateUtils;
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
    private final Calendar VALID_DATE_START = DateUtils.parseToCalendar("1970-01-02T00:00Z");

    public GardenaThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        try {
            Device device = getDevice();
            updateProperties(device);
            updateSettings(device);
            updateStatus(device);
        } catch (GardenaException ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, ex.getMessage());
        } catch (AccountHandlerNotAvailableException ex) {
            // ignore
        }
    }

    /**
     * Updates the thing configuration from the Gardena device.
     */
    protected void updateSettings(Device device) throws GardenaException {
        if (GardenaSmartImpl.DEVICE_CATEGORY_PUMP.equals(device.getCategory())) {
            Configuration config = editConfiguration();
            config.put(SETTING_LEAKAGE_DETECTION, device.getSetting(SETTING_LEAKAGE_DETECTION).getValue());
            config.put(SETTING_OPERATION_MODE, device.getSetting(SETTING_OPERATION_MODE).getValue());
            config.put(SETTING_TURN_ON_PRESSURE,
                    ObjectUtils.toString(device.getSetting(SETTING_TURN_ON_PRESSURE).getValue()));
            updateConfiguration(config);
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
        Device device = getDevice();
        State state = convertToState(device, channelUID);
        if (state != null) {
            updateState(channelUID, state);
        }
    }

    /**
     * Converts a Gardena property value to a openHAB state.
     */
    private State convertToState(Device device, ChannelUID channelUID) throws GardenaException {
        String abilityName = channelUID.getGroupId();
        String propertyName = channelUID.getIdWithoutGroup();

        try {
            String value = device.getAbility(abilityName).getProperty(propertyName).getValue();

            if (StringUtils.trimToNull(value) == null || StringUtils.equals(value, "N/A")) {
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
                    if (cal != null && !cal.before(VALID_DATE_START)) {
                        return new DateTimeType(cal);
                    } else {
                        return UnDefType.NULL;
                    }
            }
        } catch (GardenaException e) {
            logger.warn("Channel '{}' cannot be updated as device does not contain property '{}:{}'", channelUID,
                    abilityName, propertyName);
        }
        return null;
    }

    /**
     * Converts an openHAB type to a Gardena command property.
     */
    private Object convertFromType(Type type) {
        if (type instanceof OnOffType) {
            return type == OnOffType.ON ? Boolean.TRUE : Boolean.FALSE;
        } else if (type instanceof DecimalType) {
            return ((DecimalType) type).intValue();
        } else if (type instanceof StringType) {
            return ((StringType) type).toFullString();
        }
        return null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            GardenaSmartCommandName commandName = getCommandName(channelUID);
            logger.debug("Received Gardena command: {}", commandName);

            if (RefreshType.REFRESH == command) {
                logger.debug("Refreshing channel '{}'", channelUID);
                if (commandName != null && commandName.toString().startsWith("MEASURE_")) {
                    getGardenaSmart().sendCommand(getDevice(), commandName, null);
                } else {
                    updateChannel(channelUID);
                }
            } else if (commandName != null) {
                getGardenaSmart().sendCommand(getDevice(), commandName, convertFromType(command));
            }
        } catch (AccountHandlerNotAvailableException | GardenaDeviceNotFoundException ex) {
            // ignore
        } catch (Exception ex) {
            logger.warn("{}", ex.getMessage(), ex);
        }
    }

    /**
     * Returns the Gardena command from the channel.
     */
    private GardenaSmartCommandName getCommandName(ChannelUID channelUID) {
        switch (channelUID.getId()) {
            case "mower#park_until_further_notice":
                return PARK_UNTIL_FURTHER_NOTICE;
            case "mower#park_until_next_timer":
                return PARK_UNTIL_NEXT_TIMER;
            case "mower#start_override_timer":
                return START_OVERRIDE_TIMER;
            case "mower#start_resume_schedule":
                return START_RESUME_SCHEDULE;
            case "mower#duration_property":
                return DURATION_PROPERTY;

            case "ambient_temperature#temperature":
                return MEASURE_AMBIENT_TEMPERATURE;
            case "soil_temperature#temperature":
                return MEASURE_SOIL_TEMPERATURE;
            case "humidity#humidity":
                return MEASURE_SOIL_HUMIDITY;
            case "light#light":
                return MEASURE_LIGHT;

            case "outlet#button_manual_override_time":
                return OUTLET_MANUAL_OVERRIDE_TIME;
            case "outlet#valve_open":
                return OUTLET_VALVE;

            case "power#power_timer":
                return POWER_TIMER;
            default:
                return null;
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

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters)
            throws ConfigValidationException {
        validateConfigurationParameters(configurationParameters);

        try {
            GardenaSmart gardena = getGardenaSmart();
            Device device = gardena.getDevice(UidUtils.getGardenaDeviceId(getThing()));

            for (Entry<String, Object> configurationParmeter : configurationParameters.entrySet()) {
                String key = configurationParmeter.getKey();
                Object newValue = configurationParmeter.getValue();
                if (newValue != null && SETTING_TURN_ON_PRESSURE.equals(key)) {
                    newValue = new Double((String) newValue);
                }

                Setting setting = device.getSetting(key);
                if (ObjectUtils.notEqual(setting.getValue(), newValue)) {
                    gardena.sendSetting(setting, newValue);
                    setting.setValue(newValue);
                }
            }
            updateSettings(device);
        } catch (GardenaException | AccountHandlerNotAvailableException ex) {
            logger.warn("Error setting thing properties: {}", ex.getMessage(), ex);
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
