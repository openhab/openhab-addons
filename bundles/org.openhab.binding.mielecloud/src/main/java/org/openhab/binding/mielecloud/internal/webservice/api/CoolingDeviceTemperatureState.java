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
package org.openhab.binding.mielecloud.internal.webservice.api;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Provides easy access to temperature values mapped for cooling devices.
 *
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class CoolingDeviceTemperatureState {
    private final DeviceState deviceState;

    public CoolingDeviceTemperatureState(DeviceState deviceState) {
        this.deviceState = deviceState;
    }

    /**
     * Gets the current temperature of the fridge part of the device.
     *
     * @return The current temperature of the fridge part of the device.
     */
    public Optional<Integer> getFridgeTemperature() {
        switch (deviceState.getRawType()) {
            case FRIDGE:
                return deviceState.getTemperature(0);

            case FRIDGE_FREEZER_COMBINATION:
                return deviceState.getTemperature(0);

            default:
                return Optional.empty();
        }
    }

    /**
     * Gets the target temperature of the fridge part of the device.
     *
     * @return The target temperature of the fridge part of the device.
     */
    public Optional<Integer> getFridgeTargetTemperature() {
        switch (deviceState.getRawType()) {
            case FRIDGE:
                return deviceState.getTargetTemperature(0);

            case FRIDGE_FREEZER_COMBINATION:
                return deviceState.getTargetTemperature(0);

            default:
                return Optional.empty();
        }
    }

    /**
     * Gets the current temperature of the freezer part of the device.
     *
     * @return The current temperature of the freezer part of the device.
     */
    public Optional<Integer> getFreezerTemperature() {
        switch (deviceState.getRawType()) {
            case FREEZER:
                return deviceState.getTemperature(0);

            case FRIDGE_FREEZER_COMBINATION:
                return deviceState.getTemperature(1);

            default:
                return Optional.empty();
        }
    }

    /**
     * Gets the target temperature of the freezer part of the device.
     *
     * @return The target temperature of the freezer part of the device.
     */
    public Optional<Integer> getFreezerTargetTemperature() {
        switch (deviceState.getRawType()) {
            case FREEZER:
                return deviceState.getTargetTemperature(0);

            case FRIDGE_FREEZER_COMBINATION:
                return deviceState.getTargetTemperature(1);

            default:
                return Optional.empty();
        }
    }
}
