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
package org.openhab.binding.tellstick.internal.handler;

import static org.openhab.binding.tellstick.internal.TellstickBindingConstants.*;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;

import org.openhab.binding.tellstick.internal.TellstickBindingConstants;
import org.openhab.binding.tellstick.internal.live.xml.DataTypeValue;
import org.openhab.binding.tellstick.internal.live.xml.TellstickNetSensor;
import org.openhab.binding.tellstick.internal.live.xml.TellstickNetSensorEvent;
import org.openhab.binding.tellstick.internal.local.dto.LocalDataTypeValueDTO;
import org.openhab.binding.tellstick.internal.local.dto.TellstickLocalSensorDTO;
import org.openhab.binding.tellstick.internal.local.dto.TellstickLocalSensorEventDTO;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tellstick.device.TellstickDeviceEvent;
import org.tellstick.device.TellstickException;
import org.tellstick.device.TellstickSensor;
import org.tellstick.device.TellstickSensorEvent;
import org.tellstick.device.iface.Device;
import org.tellstick.device.iface.DimmableDevice;
import org.tellstick.device.iface.TellstickEvent;
import org.tellstick.enums.DataType;
import org.tellstick.enums.DeviceType;

/**
 * Handler for telldus and tellstick devices. This sends the commands to the correct bridge.
 *
 * @author Jarle Hjortland - Initial contribution
 */
public class TelldusDevicesHandler extends BaseThingHandler implements DeviceStatusListener {

    private Logger logger = LoggerFactory.getLogger(TelldusDevicesHandler.class);
    private String deviceId;
    private Boolean isDimmer = Boolean.FALSE;
    private int resend = 1;
    private TelldusBridgeHandler bridgeHandler = null;
    private final ChannelUID stateChannel;
    private final ChannelUID dimChannel;
    private final ChannelUID humidityChannel;
    private final ChannelUID tempChannel;
    private final ChannelUID raintTotChannel;
    private final ChannelUID rainRateChannel;
    private final ChannelUID windAverageChannel;
    private final ChannelUID windDirectionChannel;
    private final ChannelUID windGuestChannel;
    private final ChannelUID wattChannel;
    private final ChannelUID ampereChannel;
    private final ChannelUID luxChannel;
    private final ChannelUID timestampChannel;

    public TelldusDevicesHandler(Thing thing) {
        super(thing);
        stateChannel = new ChannelUID(getThing().getUID(), CHANNEL_STATE);
        dimChannel = new ChannelUID(getThing().getUID(), CHANNEL_DIMMER);
        humidityChannel = new ChannelUID(getThing().getUID(), CHANNEL_HUMIDITY);
        tempChannel = new ChannelUID(getThing().getUID(), CHANNEL_TEMPERATURE);
        raintTotChannel = new ChannelUID(getThing().getUID(), CHANNEL_RAINTOTAL);
        rainRateChannel = new ChannelUID(getThing().getUID(), CHANNEL_RAINRATE);
        windAverageChannel = new ChannelUID(getThing().getUID(), CHANNEL_WINDAVERAGE);
        windDirectionChannel = new ChannelUID(getThing().getUID(), CHANNEL_WINDDIRECTION);
        windGuestChannel = new ChannelUID(getThing().getUID(), CHANNEL_WINDGUST);
        wattChannel = new ChannelUID(getThing().getUID(), CHANNEL_WATT);
        ampereChannel = new ChannelUID(getThing().getUID(), CHANNEL_AMPERE);
        timestampChannel = new ChannelUID(getThing().getUID(), CHANNEL_TIMESTAMP);
        luxChannel = new ChannelUID(getThing().getUID(), CHANNEL_LUX);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handle event {} for {}", command, channelUID);
        TelldusBridgeHandler bridgeHandler = getTellstickBridgeHandler();
        if (bridgeHandler == null) {
            logger.warn("Tellstick bridge handler not found. Cannot handle command without bridge.");
            return;
        }
        Device dev = getDevice(bridgeHandler, deviceId);

        if (dev == null) {
            logger.warn("Device not found. Can't send command to device '{}'", deviceId);
            return;
        }
        if (command instanceof RefreshType) {
            Bridge bridge = getBridge();
            if (bridge != null) {
                TelldusBridgeHandler localBridgeHandler = (TelldusBridgeHandler) bridge.getHandler();
                if (localBridgeHandler != null) {
                    localBridgeHandler.handleCommand(channelUID, command);
                    refreshDevice(dev);
                    return;
                }
            }
        }
        if (channelUID.getId().equals(CHANNEL_DIMMER) || channelUID.getId().equals(CHANNEL_STATE)) {
            try {
                if (dev.getDeviceType() == DeviceType.DEVICE) {
                    getTellstickBridgeHandler().getController().handleSendEvent(dev, resend, isDimmer, command);
                } else {
                    logger.warn("{} is not an updateable device. Read-only", dev);
                }
            } catch (TellstickException e) {
                logger.debug("Failed to send command to tellstick", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        } else {
            logger.warn("Setting of channel {} not possible. Read-only", channelUID);
        }
    }

    private void refreshDevice(Device dev) {
        if (deviceId != null && isSensor()) {
            updateSensorStates(dev);
        } else if (deviceId != null) {
            updateDeviceState(dev);
        }
    }

    @Override
    public void initialize() {
        Configuration config = getConfig();
        logger.debug("Initialize TelldusDeviceHandler {}. class {}", config, config.getClass());
        final Object configDeviceId = config.get(TellstickBindingConstants.DEVICE_ID);
        if (configDeviceId != null) {
            deviceId = configDeviceId.toString();
        } else {
            logger.debug("Initialized TellStick device missing serialNumber configuration... troubles ahead");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        }
        final Boolean isADimmer = (Boolean) config.get(TellstickBindingConstants.DEVICE_ISDIMMER);
        if (isADimmer != null) {
            this.isDimmer = isADimmer;
        }
        final BigDecimal repeatCount = (BigDecimal) config.get(TellstickBindingConstants.DEVICE_RESEND_COUNT);
        if (repeatCount != null) {
            resend = repeatCount.intValue();
        }
        Bridge bridge = getBridge();
        if (bridge != null) {
            bridgeStatusChanged(bridge.getStatusInfo());
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("device: {} bridgeStatusChanged: {}", deviceId, bridgeStatusInfo);
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            try {
                Bridge localBridge = getBridge();
                if (localBridge != null) {
                    TelldusBridgeHandler telldusBridgeHandler = (TelldusBridgeHandler) localBridge.getHandler();
                    logger.debug("Init bridge for {}, bridge:{}", deviceId, telldusBridgeHandler);
                    if (telldusBridgeHandler != null) {
                        this.bridgeHandler = telldusBridgeHandler;
                        this.bridgeHandler.registerDeviceStatusListener(this);
                        Configuration config = editConfiguration();
                        Device dev = getDevice(telldusBridgeHandler, deviceId);
                        if (dev != null) {
                            if (dev.getName() != null) {
                                config.put(TellstickBindingConstants.DEVICE_NAME, dev.getName());
                            }
                            if (dev.getProtocol() != null) {
                                config.put(TellstickBindingConstants.DEVICE_PROTOCOL, dev.getProtocol());
                            }
                            if (dev.getModel() != null) {
                                config.put(TellstickBindingConstants.DEVICE_MODEL, dev.getModel());
                            }
                            updateConfiguration(config);

                            updateStatus(ThingStatus.ONLINE);
                        } else {
                            logger.warn(
                                    "Could not find {}, please make sure it is defined and that telldus service is running",
                                    deviceId);
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to init bridge for {}", deviceId, e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, bridgeStatusInfo.getStatusDetail());
        }
    }

    private Device getDevice(TelldusBridgeHandler tellHandler, String deviceId) {
        Device dev = null;
        if (deviceId != null) {
            if (isSensor()) {
                dev = tellHandler.getSensor(deviceId);
            } else {
                dev = tellHandler.getDevice(deviceId);
                updateDeviceState(dev);
            }
        }
        return dev;
    }

    private boolean isSensor() {
        return (getThing().getThingTypeUID().equals(TellstickBindingConstants.SENSOR_THING_TYPE)
                || getThing().getThingTypeUID().equals(TellstickBindingConstants.RAINSENSOR_THING_TYPE)
                || getThing().getThingTypeUID().equals(TellstickBindingConstants.WINDSENSOR_THING_TYPE)
                || getThing().getThingTypeUID().equals(TellstickBindingConstants.POWERSENSOR_THING_TYPE));
    }

    private void updateSensorStates(Device dev) {
        if (dev instanceof TellstickSensor) {
            updateStatus(ThingStatus.ONLINE);
            for (DataType type : ((TellstickSensor) dev).getData().keySet()) {
                updateSensorDataState(type, ((TellstickSensor) dev).getData(type));
            }
        } else if (dev instanceof TellstickNetSensor) {
            if (((TellstickNetSensor) dev).getOnline()) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
            for (DataTypeValue type : ((TellstickNetSensor) dev).getData()) {
                updateSensorDataState(type);
            }
        } else if (dev instanceof TellstickLocalSensorDTO) {
            for (LocalDataTypeValueDTO type : ((TellstickLocalSensorDTO) dev).getData()) {
                updateSensorDataState(type);
            }
        }
    }

    private synchronized TelldusBridgeHandler getTellstickBridgeHandler() {
        if (this.bridgeHandler == null) {
            logger.debug("No available bridge handler found for {} bridge {} .", deviceId, getBridge());
        }
        return this.bridgeHandler;
    }

    @Override
    public void onDeviceStateChanged(Bridge bridge, Device device, TellstickEvent event) {
        logger.debug("Updating states of ({} {} ({}) id: {} or {}", device.getDeviceType(), device.getName(),
                device.getUUId(), getThing().getUID(), deviceId);
        if (device.getUUId().equals(deviceId)) {
            if (event instanceof TellstickDeviceEvent) {
                updateDeviceState(device);
            } else if (event instanceof TellstickNetSensorEvent) {
                TellstickNetSensorEvent sensorevent = (TellstickNetSensorEvent) event;
                updateSensorDataState(sensorevent.getDataTypeValue());
            } else if (event instanceof TellstickLocalSensorEventDTO) {
                TellstickLocalSensorEventDTO sensorevent = (TellstickLocalSensorEventDTO) event;
                updateSensorDataState(sensorevent.getDataTypeValue());
            } else if (event instanceof TellstickSensorEvent) {
                TellstickSensorEvent sensorevent = (TellstickSensorEvent) event;
                updateSensorDataState(sensorevent.getDataType(), sensorevent.getData());
            } else {
                logger.debug("Unhandled Device {}.", device.getDeviceType());
            }
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(event.getTimestamp());
            updateState(timestampChannel,
                    new DateTimeType(ZonedDateTime.ofInstant(cal.toInstant(), ZoneId.systemDefault())));
        }
    }

    private void updateSensorDataState(DataType dataType, String data) {
        switch (dataType) {
            case HUMIDITY:
                updateState(humidityChannel, new QuantityType<>(new BigDecimal(data), HUMIDITY_UNIT));
                break;
            case TEMPERATURE:
                updateState(tempChannel, new QuantityType<>(new BigDecimal(data), SIUnits.CELSIUS));
                break;
            case RAINRATE:
                updateState(rainRateChannel, new QuantityType<>(new BigDecimal(data), RAIN_UNIT));
                break;
            case RAINTOTAL:
                updateState(raintTotChannel, new QuantityType<>(new BigDecimal(data), RAIN_UNIT));
                break;
            case WINDAVERAGE:
                updateState(windAverageChannel, new QuantityType<>(new BigDecimal(data), WIND_SPEED_UNIT_MS));
                break;
            case WINDDIRECTION:
                updateState(windDirectionChannel, new QuantityType<>(new BigDecimal(data), WIND_DIRECTION_UNIT));
                break;
            case WINDGUST:
                updateState(windGuestChannel, new QuantityType<>(new BigDecimal(data), WIND_SPEED_UNIT_MS));
                break;
            default:
        }
    }

    private void updateSensorDataState(DataTypeValue dataType) {
        switch (dataType.getName()) {
            case HUMIDITY:
                updateState(humidityChannel, new QuantityType<>(new BigDecimal(dataType.getValue()), HUMIDITY_UNIT));
                break;
            case TEMPERATURE:
                updateState(tempChannel, new QuantityType<>(new BigDecimal(dataType.getValue()), SIUnits.CELSIUS));
                break;
            case RAINRATE:
                updateState(rainRateChannel, new QuantityType<>(new BigDecimal(dataType.getValue()), RAIN_UNIT));
                break;
            case RAINTOTAL:
                updateState(raintTotChannel, new QuantityType<>(new BigDecimal(dataType.getValue()), RAIN_UNIT));
                break;
            case WINDAVERAGE:
                updateState(windAverageChannel,
                        new QuantityType<>(new BigDecimal(dataType.getValue()), WIND_SPEED_UNIT_MS));
                break;
            case WINDDIRECTION:
                updateState(windDirectionChannel,
                        new QuantityType<>(new BigDecimal(dataType.getValue()), WIND_DIRECTION_UNIT));
                break;
            case WINDGUST:
                updateState(windGuestChannel,
                        new QuantityType<>(new BigDecimal(dataType.getValue()), WIND_SPEED_UNIT_MS));
                break;
            case WATT:
                if (dataType.getUnit() != null && dataType.getUnit().equals("A")) {
                    updateState(ampereChannel, new QuantityType<>(new BigDecimal(dataType.getValue()), ELECTRIC_UNIT));
                } else {
                    updateState(wattChannel, new QuantityType<>(new BigDecimal(dataType.getValue()), POWER_UNIT));
                }
                break;
            case LUMINATION:
                updateState(luxChannel, new QuantityType<>(new DecimalType(dataType.getValue()), LUX_UNIT));
                break;
            default:
        }
    }

    private void updateSensorDataState(LocalDataTypeValueDTO dataType) {
        switch (dataType.getName()) {
            case HUMIDITY:
                updateState(humidityChannel, new QuantityType<>(new BigDecimal(dataType.getValue()), HUMIDITY_UNIT));
                break;
            case TEMPERATURE:
                updateState(tempChannel, new QuantityType<>(new BigDecimal(dataType.getValue()), SIUnits.CELSIUS));
                break;
            case RAINRATE:
                updateState(rainRateChannel, new QuantityType<>(new BigDecimal(dataType.getValue()), RAIN_UNIT));
                break;
            case RAINTOTAL:
                updateState(raintTotChannel, new QuantityType<>(new BigDecimal(dataType.getValue()), RAIN_UNIT));
                break;
            case WINDAVERAGE:
                updateState(windAverageChannel,
                        new QuantityType<>(new BigDecimal(dataType.getValue()), WIND_SPEED_UNIT_MS));
                break;
            case WINDDIRECTION:
                updateState(windDirectionChannel,
                        new QuantityType<>(new BigDecimal(dataType.getValue()), WIND_DIRECTION_UNIT));
                break;
            case WINDGUST:
                updateState(windGuestChannel,
                        new QuantityType<>(new BigDecimal(dataType.getValue()), WIND_SPEED_UNIT_MS));
                break;
            case WATT:
                if (dataType.getScale() == 5) {
                    updateState(ampereChannel, new QuantityType<>(new BigDecimal(dataType.getValue()), ELECTRIC_UNIT));
                } else if (dataType.getScale() == 2) {
                    updateState(wattChannel, new QuantityType<>(new BigDecimal(dataType.getValue()), Units.WATT));
                }
                break;
            case LUMINATION:
                updateState(luxChannel, new QuantityType<>(new DecimalType(dataType.getValue()), LUX_UNIT));
                break;
            default:
        }
    }

    private void updateDeviceState(Device device) {
        if (device != null) {
            logger.debug("Updating state of {} {} ({}) id: {}", device.getDeviceType(), device.getName(),
                    device.getUUId(), getThing().getUID());
            TelldusBridgeHandler bridgeHandler = getTellstickBridgeHandler();
            State st = null;
            if (bridgeHandler != null && bridgeHandler.getController() != null) {
                st = bridgeHandler.getController().calcState(device);
            }
            if (st != null && bridgeHandler != null) {
                BigDecimal dimValue = bridgeHandler.getController().calcDimValue(device);
                updateState(stateChannel, st);
                if (device instanceof DimmableDevice) {
                    updateState(dimChannel, new PercentType(dimValue));
                }
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        } else {
            updateStatus(ThingStatus.REMOVED);
        }
    }

    @Override
    public void onDeviceRemoved(Bridge bridge, Device device) {
        if (device.getUUId().equals(deviceId)) {
            updateStatus(ThingStatus.REMOVED);
        }
    }

    @Override
    public void onDeviceAdded(Bridge bridge, Device device) {
    }
}
