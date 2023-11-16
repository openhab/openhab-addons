/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.zway.internal.converter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Channel;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import de.fh_zwickau.informatik.sensor.model.devices.Color;
import de.fh_zwickau.informatik.sensor.model.devices.Device;
import de.fh_zwickau.informatik.sensor.model.devices.types.Battery;
import de.fh_zwickau.informatik.sensor.model.devices.types.Doorlock;
import de.fh_zwickau.informatik.sensor.model.devices.types.SensorBinary;
import de.fh_zwickau.informatik.sensor.model.devices.types.SensorDiscrete;
import de.fh_zwickau.informatik.sensor.model.devices.types.SensorMultilevel;
import de.fh_zwickau.informatik.sensor.model.devices.types.SwitchBinary;
import de.fh_zwickau.informatik.sensor.model.devices.types.SwitchControl;
import de.fh_zwickau.informatik.sensor.model.devices.types.SwitchMultilevel;
import de.fh_zwickau.informatik.sensor.model.devices.types.SwitchRGBW;
import de.fh_zwickau.informatik.sensor.model.devices.types.SwitchToggle;
import de.fh_zwickau.informatik.sensor.model.devices.types.Thermostat;
import de.fh_zwickau.informatik.sensor.model.devices.types.ToggleButton;

/**
 * The {@link ZWayDeviceStateConverter} is responsible for converting Z-Way device level to openHAB states
 *
 * @author Patrick Hecker - Initial contribution
 */
@NonNullByDefault
public class ZWayDeviceStateConverter {
    public static State toState(Device device, Channel channel) {
        // Store level locally
        String level = device.getMetrics().getLevel();

        // Set item state to level depending on device type
        if (device instanceof Battery) {
            return getMultilevelState(level);
        } else if (device instanceof Doorlock) {
            return getBinaryState(level.toLowerCase());
        } else if (device instanceof SensorBinary) {
            if ("Contact".equals(channel.getAcceptedItemType())) {
                return getDoorlockState(level.toLowerCase());
            } else {
                return getBinaryState(level.toLowerCase());
            }
        } else if (device instanceof SensorMultilevel) {
            return getMultilevelState(level);
        } else if (device instanceof SwitchBinary) {
            return getBinaryState(level.toLowerCase());
        } else if (device instanceof SwitchMultilevel) {
            if ("Rollershutter".equals(channel.getAcceptedItemType())
                    || "Dimmer".equals(channel.getAcceptedItemType())) {
                return getPercentState(level);
            } else {
                return getMultilevelState(level);
            }
        } else if (device instanceof SwitchRGBW) {
            return getColorState(device.getMetrics().getColor());
        } else if (device instanceof Thermostat) {
            return getMultilevelState(level);
        } else if (device instanceof SwitchControl) {
            return getBinaryState(level.toLowerCase());
        } else if (device instanceof ToggleButton || device instanceof SwitchToggle) {
            return getBinaryState(level.toLowerCase());
        } else if (device instanceof SensorDiscrete) {
            return getMultilevelState(level);
        }

        return UnDefType.UNDEF;
    }

    /**
     * Transforms a value in an openHAB type.
     *
     * @param multilevel sensor value
     * @return transformed openHAB state
     */
    private static State getMultilevelState(@Nullable String multilevelValue) {
        if (multilevelValue != null) {
            return new DecimalType(multilevelValue);
        }
        return UnDefType.UNDEF;
    }

    private static State getPercentState(@Nullable String multilevelValue) {
        if (multilevelValue != null) {
            return new PercentType(multilevelValue);
        }
        return UnDefType.UNDEF;
    }

    /**
     * Transforms a value in an openHAB type.
     *
     * @param binary switch value
     * @return transformed openHAB state
     */
    private static State getBinaryState(@Nullable String binarySwitchState) {
        if (binarySwitchState != null) {
            if ("on".equals(binarySwitchState)) {
                return OnOffType.ON;
            } else if ("off".equals(binarySwitchState)) {
                return OnOffType.OFF;
            }
        }
        return UnDefType.UNDEF;
    }

    /**
     * Transforms a value in an openHAB type.
     * - ON to OPEN
     * - OFF to CLOSED
     *
     * @param binary sensor state
     * @return
     */
    private static State getDoorlockState(@Nullable String binarySensorState) {
        if (binarySensorState != null) {
            if ("on".equals(binarySensorState)) {
                return OpenClosedType.OPEN;
            } else if ("off".equals(binarySensorState)) {
                return OpenClosedType.CLOSED;
            }
        }
        return UnDefType.UNDEF;
    }

    /**
     * Transforms a value in an openHAB type.
     *
     * @param Z-Way color value
     * @return transformed openHAB state
     */
    private static State getColorState(@Nullable Color colorSwitchState) {
        if (colorSwitchState != null && colorSwitchState.getRed() != null && colorSwitchState.getGreen() != null
                && colorSwitchState.getBlue() != null) {
            return HSBType.fromRGB(colorSwitchState.getRed(), colorSwitchState.getGreen(), colorSwitchState.getBlue());
        }

        return UnDefType.UNDEF;
    }
}
