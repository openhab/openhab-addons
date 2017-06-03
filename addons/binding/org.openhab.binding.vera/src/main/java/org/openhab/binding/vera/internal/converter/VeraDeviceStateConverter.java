/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.vera.internal.converter;

import static org.openhab.binding.vera.VeraBindingConstants.*;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.vera.controller.json.Device;
import org.slf4j.Logger;

/**
 * The {@link VeraDeviceStateConverter} is responsible for converting Vera device level to openHAB states
 *
 * @author Dmitriy Ponomarev
 */
public class VeraDeviceStateConverter {
    public static State toState(Device device, Channel channel, Logger logger) {

        String channelType = channel.getUID().getId().split("-")[0];
        if (BATTERY_CHANNEL.equals(channelType)) {
            return getMultilevelState(device.getBatterylevel());
        }
        if (SENSOR_METER_KWH_CHANNEL.equals(channelType)) {
            return getMultilevelState(device.getKwh());
        }
        if (SENSOR_METER_W_CHANNEL.equals(channelType)) {
            return getMultilevelState(device.getWatts());
        }

        int subcategory = Integer.parseInt(device.getSubcategory());
        switch (device.getCategoryType()) {
            case Controller:
            case Interface:
                break;
            case DimmableLight:
                switch (subcategory) {
                    case 1:
                    case 2:
                    case 3:
                        return getPercentState(device.getLevel());
                    case 4:
                        return getPercentState(device.getLevel());// TODO getColorState(device.light);
                }
                break;
            case Switch:
                return getBinaryState(device.getStatus());
            case SecuritySensor:
                switch (subcategory) {
                    case 1:
                        return getDoorlockState("1".equals(device.getTripped()) ? "0" : "1");
                    default:
                        return getBinaryState(device.getTripped());
                }
            case HVAC: // TODO
                logger.warn("TODO: {}, {}", device, device.getCategoryType());
                break;
            case DoorLock:
                return getBinaryState(device.getLocked());
            case WindowCovering:
                return getPercentState(device.getLevel());
            case GenericSensor:
                return getMultilevelState(device.getLevel());
            case SceneController:
                return getBinaryState(device.getStatus());
            case HumiditySensor:
                return getMultilevelState(device.getHumidity());
            case TemperatureSensor:
                return getMultilevelState(device.getTemperature());
            case LightSensor:
                return getMultilevelState(device.getLight());
            case PowerMeter:
                return getMultilevelState(device.getLevel());
            case UVSensor:
                return getMultilevelState(device.getLevel());
            case Camera:
            case AV:
            case ZWaveInterface:
            case InsteonInterface:
            case RemoteControl:
            case IRTransmitter:
            case GenericIO:
            case SerialPort:
            case AlarmPanel:
            case AlarmPartition:
            case Siren:
            case Weather:
            case PhilipsController:
            case Appliance:
                logger.warn("TODO: {}, {}", device, device.getCategoryType());
                break;
            case Unknown:
                logger.warn("Unknown device type: {}, {}", device, device.getCategory());
                break;
        }
        return UnDefType.UNDEF;
    }

    private static State getMultilevelState(String multilevelValue) {
        if (multilevelValue != null) {
            if (multilevelValue.isEmpty()) {
                return new DecimalType("0");
            }
            return new DecimalType(multilevelValue);
        }
        return UnDefType.UNDEF;
    }

    private static State getPercentState(String multilevelValue) {
        if (multilevelValue != null) {
            if (multilevelValue.isEmpty()) {
                return new PercentType("0");
            }
            return new PercentType(multilevelValue);
        }
        return UnDefType.UNDEF;
    }

    private static State getBinaryState(String status) {
        if (status != null) {
            if ("1".equals(status)) {
                return OnOffType.ON;
            } else if ("0".equals(status)) {
                return OnOffType.OFF;
            }
        }
        return UnDefType.UNDEF;
    }

    private static State getDoorlockState(String status) {
        if (status != null) {
            if ("1".equals(status)) {
                return OpenClosedType.CLOSED;
            } else if ("0".equals(status)) {
                return OpenClosedType.OPEN;
            }
        }
        return UnDefType.UNDEF;
    }
}
