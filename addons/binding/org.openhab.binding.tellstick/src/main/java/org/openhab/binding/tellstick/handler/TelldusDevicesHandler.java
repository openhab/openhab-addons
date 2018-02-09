/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tellstick.handler;

import static org.openhab.binding.tellstick.TellstickBindingConstants.*;

import java.math.BigDecimal;
import java.util.Calendar;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.tellstick.TellstickBindingConstants;
import org.openhab.binding.tellstick.internal.live.xml.DataTypeValue;
import org.openhab.binding.tellstick.internal.live.xml.TellstickNetSensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * @author Jarle Hjortland
 *
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
        timestampChannel = new ChannelUID(getThing().getUID(), CHANNEL_TIMESTAMP);
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
            getBridge().getHandler().handleCommand(channelUID, command);
            refreshDevice(dev);
            return;
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
            } catch (Exception e) {
                logger.error("Failed to send command to tellstick", e);
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
        if (getBridge() != null) {
            bridgeStatusChanged(getBridge().getStatusInfo());
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("device: {} bridgeStatusChanged: {}", deviceId, bridgeStatusInfo);
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            try {
                TelldusBridgeHandler tellHandler = (TelldusBridgeHandler) getBridge().getHandler();
                logger.debug("Init bridge for {}, bridge:{}", deviceId, tellHandler);
                if (tellHandler != null) {
                    this.bridgeHandler = tellHandler;
                    this.bridgeHandler.registerDeviceStatusListener(this);
                    Configuration config = editConfiguration();
                    Device dev = getDevice(tellHandler, deviceId);
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
        if (deviceId != null && isSensor()) {
            dev = tellHandler.getSensor(deviceId);
        } else if (deviceId != null) {
            dev = tellHandler.getDevice(deviceId);
            updateDeviceState(dev);
        }
        return dev;
    }

    private boolean isSensor() {
        return (getThing().getThingTypeUID().equals(TellstickBindingConstants.SENSOR_THING_TYPE)
                || getThing().getThingTypeUID().equals(TellstickBindingConstants.RAINSENSOR_THING_TYPE)
                || getThing().getThingTypeUID().equals(TellstickBindingConstants.WINDSENSOR_THING_TYPE));
    }

    private void updateSensorStates(Device dev) {
        if (dev instanceof TellstickSensor) {
            updateStatus(ThingStatus.ONLINE);
            for (DataType type : ((TellstickSensor) dev).getData().keySet()) {
                updateSensorDateState(type, ((TellstickSensor) dev).getData(type));
            }
        } else if (dev instanceof TellstickNetSensor) {
            if (((TellstickNetSensor) dev).getOnline()) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
            for (DataTypeValue type : ((TellstickNetSensor) dev).getData()) {
                updateSensorDateState(type.getName(), type.getValue());
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

            switch (device.getDeviceType()) {
                case DEVICE:
                    updateDeviceState(device);
                    break;
                case SENSOR:
                    TellstickSensorEvent sensorevent = (TellstickSensorEvent) event;
                    updateSensorDateState(sensorevent.getDataType(), sensorevent.getData());
                    break;

                default:
                    logger.debug("Unhandled Device {}.", device.getDeviceType());
                    break;

            }
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(event.getTimestamp());
            updateState(timestampChannel, new DateTimeType(cal));

        }
    }

    private void updateSensorDateState(DataType dataType, String data) {
        switch (dataType) {
            case HUMIDITY:
                updateState(humidityChannel, new DecimalType(data));
                break;
            case TEMPERATURE:
                updateState(tempChannel, new DecimalType(data));
                break;
            case RAINRATE:
                updateState(rainRateChannel, new DecimalType(data));
                break;
            case RAINTOTAL:
                updateState(raintTotChannel, new DecimalType(data));
                break;
            case WINDAVERAGE:
                updateState(windAverageChannel, new DecimalType(data));
                break;
            case WINDDIRECTION:
                updateState(windDirectionChannel, new StringType(data));
                break;
            case WINDGUST:
                updateState(windGuestChannel, new DecimalType(data));
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
