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
import org.openhab.binding.vera2.controller.Vera.json.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VeraDeviceStateConverter} is responsible for converting Vera device level to openHAB states
 *
 * @author Dmitriy Ponomarev
 */
public class VeraDeviceStateConverter {
    private static Logger logger = LoggerFactory.getLogger(VeraDeviceStateConverter.class);

    public static State toState(Device device, Channel channel) {
        if (channel.getUID().getId().split("-")[0].equals(BATTERY_CHANNEL)) {
            return getMultilevelState(device.batterylevel);
        }

        int category = Integer.parseInt(device.category);
        int subcategory = Integer.parseInt(device.subcategory);
        switch (category) {
            case 0:
            case 1: // Interface
                break;
            case 2: // Dimmable Light
                switch (subcategory) {
                    case 1:
                    case 2:
                    case 3:
                        return getPercentState(device.level);
                    case 4:
                        return getPercentState(device.level);// TODO getColorState(device.light);
                }
                break;
            case 3: // Switch
                return getBinaryState(device.status);
            case 4: // Security Sensor
                switch (subcategory) {
                    case 1:
                        return getDoorlockState(device.tripped.equals("1") ? "0" : "1");
                    default:
                        return getBinaryState(device.tripped);
                }
            case 5: // TODO HVAC
                logger.warn("TODO: HVAC: {}, {}, {}, {}", device.id, device.name, device.category, device.categoryName);
                break;
            case 6: // TODO Camera
                logger.warn("TODO: Camera: {}, {}, {}, {}", device.id, device.name, device.category,
                        device.categoryName);
                break;
            case 7: // Door Lock
                return getBinaryState(device.locked);
            case 8: // Window Covering
                return getPercentState(device.level);
            case 9: // TODO Remote Control
                logger.warn("TODO: Remote Control: {}, {}, {}, {}", device.id, device.name, device.category,
                        device.categoryName);
                break;
            case 10: // TODO IR Transmitter
                logger.warn("TODO: IR Transmitter: {}, {}, {}, {}", device.id, device.name, device.category,
                        device.categoryName);
                break;
            case 11: // TODO Generic I/O
                logger.warn("TODO: Generic I/O: {}, {}, {}, {}", device.id, device.name, device.category,
                        device.categoryName);
                break;
            case 12: // Generic Sensor
                return getMultilevelState(device.level);
            case 13: // TODO Serial Port
                logger.warn("TODO: Serial Port I/O: {}, {}, {}, {}", device.id, device.name, device.category,
                        device.categoryName);
                break;
            case 14: // Scene Controller
                return getBinaryState(device.status);
            case 15: // TODO A/V
                logger.warn("TODO: A/V: {}, {}, {}, {}", device.id, device.name, device.category, device.categoryName);
                break;
            case 16: // Humidity Sensor
                return getMultilevelState(device.humidity);
            case 17: // Temperature Sensor
                return getMultilevelState(device.temperature);
            case 18: // Light Sensor
                return getMultilevelState(device.light);
            case 19: // TODO Z-Wave Interface
                logger.warn("TODO: Z-Wave Interface: {}, {}, {}, {}", device.id, device.name, device.category,
                        device.categoryName);
                break;
            case 20: // TODO Insteon Interface
                logger.warn("TODO: Insteon Interface: {}, {}, {}, {}", device.id, device.name, device.category,
                        device.categoryName);
                break;
            case 21: // Power Meter
                return getMultilevelState(device.level);
            case 22: // TODO Alarm Panel
                logger.warn("TODO: Alarm Panel: {}, {}, {}, {}", device.id, device.name, device.category,
                        device.categoryName);
                break;
            case 23: // TODO Alarm Partition
                logger.warn("TODO: Alarm Partition: {}, {}, {}, {}", device.id, device.name, device.category,
                        device.categoryName);
                break;
            case 24: // TODO Siren
                logger.warn("TODO: Siren: {}, {}, {}, {}", device.id, device.name, device.category,
                        device.categoryName);
                break;
            case 25: // TODO Weather
                logger.warn("TODO: Weather: {}, {}, {}, {}", device.id, device.name, device.category,
                        device.categoryName);
                break;
            case 26: // TODO Philips Controller
                logger.warn("TODO: Philips Controller: {}, {}, {}, {}", device.id, device.name, device.category,
                        device.categoryName);
                break;
            case 27: // TODO Appliance
                logger.warn("TODO: Appliance: {}, {}, {}, {}", device.id, device.name, device.category,
                        device.categoryName);
                break;
            case 28: // UV Sensor
                return getMultilevelState(device.level);
            default:
                logger.warn("Unknown device type: {}, {}, {}, {}", device.id, device.name, device.category,
                        device.categoryName);
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
            if (status.equals("1")) {
                return OnOffType.ON;
            } else if (status.equals("0")) {
                return OnOffType.OFF;
            }
        }
        return UnDefType.UNDEF;
    }

    private static State getDoorlockState(String status) {
        if (status != null) {
            if (status.equals("1")) {
                return OpenClosedType.CLOSED;
            } else if (status.equals("0")) {
                return OpenClosedType.OPEN;
            }
        }
        return UnDefType.UNDEF;
    }
}
