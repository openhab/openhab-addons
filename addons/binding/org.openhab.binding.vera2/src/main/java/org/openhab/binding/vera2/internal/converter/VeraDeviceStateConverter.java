/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.vera2.internal.converter;

import static org.openhab.binding.vera2.VeraBindingConstants.BATTERY_CHANNEL;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.vera2.controller.json.Device;
import org.slf4j.Logger;

/**
 * The {@link VeraDeviceStateConverter} is responsible for converting Vera device level to openHAB states
 *
 * @author Dmitriy Ponomarev
 */
public class VeraDeviceStateConverter {
    public static State toState(Device device, Channel channel, Logger logger) {
        if (channel.getUID().getId().split("-")[0].equals(BATTERY_CHANNEL)) {
            return getMultilevelState(device.getBatterylevel());
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
            case Camera: // TODO
                logger.warn("TODO: {}, {}", device, device.getCategoryType());
                break;
            case DoorLock:
                return getBinaryState(device.getLocked());
            case WindowCovering:
                return getPercentState(device.getLevel());
            case RemoteControl: // TODO
                logger.warn("TODO: {}, {}", device, device.getCategoryType());
                break;
            case IRTransmitter: // TODO
                logger.warn("TODO: {}, {}", device, device.getCategoryType());
                break;
            case GenericIO: // TODO
                logger.warn("TODO: {}, {}", device, device.getCategoryType());
                break;
            case GenericSensor:
                return getMultilevelState(device.getLevel());
            case SerialPort: // TODO
                logger.warn("TODO: {}, {}", device, device.getCategoryType());
                break;
            case SceneController:
                return getBinaryState(device.getStatus());
            case AV: // TODO
                logger.warn("TODO: {}, {}", device, device.getCategoryType());
                break;
            case HumiditySensor:
                return getMultilevelState(device.getHumidity());
            case TemperatureSensor:
                return getMultilevelState(device.getTemperature());
            case LightSensor:
                return getMultilevelState(device.getLight());
            case ZWaveInterface: // TODO
                logger.warn("TODO: {}, {}", device, device.getCategoryType());
                break;
            case InsteonInterface: // TODO
                logger.warn("TODO: {}, {}", device, device.getCategoryType());
                break;
            case PowerMeter:
                return getMultilevelState(device.getLevel());
            case AlarmPanel: // TODO
                logger.warn("TODO: {}, {}", device, device.getCategoryType());
                break;
            case AlarmPartition: // TODO
                logger.warn("TODO: {}, {}", device, device.getCategoryType());
                break;
            case Siren: // TODO
                logger.warn("TODO: {}, {}", device, device.getCategoryType());
                break;
            case Weather: // TODO
                logger.warn("TODO: {}, {}", device, device.getCategoryType());
                break;
            case PhilipsController: // TODO
                logger.warn("TODO: {}, {}", device, device.getCategoryType());
                break;
            case Appliance: // TODO
                logger.warn("TODO: {}, {}", device, device.getCategoryType());
                break;
            case UVSensor:
                return getMultilevelState(device.getLevel());
            case Unknown:
                logger.warn("Unknown device type: {}, {}", device, device.getCategory());
                break;
        }
        return UnDefType.UNDEF;
    }

    private static State getMultilevelState(String multilevelValue) {
        if (multilevelValue != null) {
            return new DecimalType(multilevelValue);
        }
        return UnDefType.UNDEF;
    }

    private static State getPercentState(String multilevelValue) {
        if (multilevelValue != null) {
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
