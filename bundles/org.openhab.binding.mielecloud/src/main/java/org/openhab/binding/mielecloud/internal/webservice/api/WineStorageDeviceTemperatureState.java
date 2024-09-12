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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mielecloud.internal.webservice.api.json.DeviceType;

/**
 * Provides easy access to temperature values mapped for wine storage devices.
 *
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class WineStorageDeviceTemperatureState {
    private static final Set<DeviceType> ALL_WINE_STORAGES = Set.of(DeviceType.WINE_CABINET,
            DeviceType.WINE_CABINET_FREEZER_COMBINATION, DeviceType.WINE_CONDITIONING_UNIT,
            DeviceType.WINE_STORAGE_CONDITIONING_UNIT);

    private final DeviceState deviceState;
    private final List<Integer> effectiveTemperatures;
    private final List<Integer> effectiveTargetTemperatures;

    /**
     * Creates a new {@link WineStorageDeviceTemperatureState}.
     *
     * @param deviceState Device state to query extended state information from.
     */
    public WineStorageDeviceTemperatureState(DeviceState deviceState) {
        this.deviceState = deviceState;
        effectiveTemperatures = getEffectiveTemperatures();
        effectiveTargetTemperatures = getEffectiveTargetTemperatures();
    }

    private List<Integer> getEffectiveTemperatures() {
        return Arrays
                .asList(deviceState.getTemperature(0), deviceState.getTemperature(1), deviceState.getTemperature(2))
                .stream().filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
    }

    private List<Integer> getEffectiveTargetTemperatures() {
        return Arrays
                .asList(deviceState.getTargetTemperature(0), deviceState.getTargetTemperature(1),
                        deviceState.getTargetTemperature(2))
                .stream().filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
    }

    /**
     * Gets the current main temperature of the wine storage.
     *
     * @return The current main temperature of the wine storage.
     */
    public Optional<Integer> getTemperature() {
        if (!ALL_WINE_STORAGES.contains(deviceState.getRawType())) {
            return Optional.empty();
        }

        return getTemperatureFromList(effectiveTemperatures);
    }

    /**
     * Gets the target main temperature of the wine storage.
     *
     * @return The target main temperature of the wine storage.
     */
    public Optional<Integer> getTargetTemperature() {
        if (!ALL_WINE_STORAGES.contains(deviceState.getRawType())) {
            return Optional.empty();
        }

        return getTemperatureFromList(effectiveTargetTemperatures);
    }

    private Optional<Integer> getTemperatureFromList(List<Integer> temperatures) {
        if (temperatures.isEmpty()) {
            return Optional.empty();
        }

        if (temperatures.size() > 1) {
            return Optional.empty();
        }

        return Optional.of(temperatures.get(0));
    }

    /**
     * Gets the current top temperature of the wine storage.
     *
     * @return The current top temperature of the wine storage.
     */
    public Optional<Integer> getTopTemperature() {
        if (!ALL_WINE_STORAGES.contains(deviceState.getRawType())) {
            return Optional.empty();
        }

        return getTopTemperatureFromList(effectiveTemperatures);
    }

    /**
     * Gets the target top temperature of the wine storage.
     *
     * @return The target top temperature of the wine storage.
     */
    public Optional<Integer> getTopTargetTemperature() {
        if (!ALL_WINE_STORAGES.contains(deviceState.getRawType())) {
            return Optional.empty();
        }

        return getTopTemperatureFromList(effectiveTargetTemperatures);
    }

    private Optional<Integer> getTopTemperatureFromList(List<Integer> temperatures) {
        if (temperatures.size() <= 1) {
            return Optional.empty();
        }

        return Optional.of(temperatures.get(0));
    }

    /**
     * Gets the current middle temperature of the wine storage.
     *
     * @return The current middle temperature of the wine storage.
     */
    public Optional<Integer> getMiddleTemperature() {
        if (!ALL_WINE_STORAGES.contains(deviceState.getRawType())) {
            return Optional.empty();
        }

        return getMiddleTemperatureFromList(effectiveTemperatures);
    }

    /**
     * Gets the target middle temperature of the wine storage.
     *
     * @return The target middle temperature of the wine storage.
     */
    public Optional<Integer> getMiddleTargetTemperature() {
        if (!ALL_WINE_STORAGES.contains(deviceState.getRawType())) {
            return Optional.empty();
        }

        return getMiddleTemperatureFromList(effectiveTargetTemperatures);
    }

    private Optional<Integer> getMiddleTemperatureFromList(List<Integer> temperatures) {
        if (temperatures.size() != 3) {
            return Optional.empty();
        }

        return Optional.of(temperatures.get(1));
    }

    /**
     * Gets the current bottom temperature of the wine storage.
     *
     * @return The current bottom temperature of the wine storage.
     */
    public Optional<Integer> getBottomTemperature() {
        if (!ALL_WINE_STORAGES.contains(deviceState.getRawType())) {
            return Optional.empty();
        }

        return getBottomTemperatureFromList(effectiveTemperatures);
    }

    /**
     * Gets the target bottom temperature of the wine storage.
     *
     * @return The target bottom temperature of the wine storage.
     */
    public Optional<Integer> getBottomTargetTemperature() {
        if (!ALL_WINE_STORAGES.contains(deviceState.getRawType())) {
            return Optional.empty();
        }

        return getBottomTemperatureFromList(effectiveTargetTemperatures);
    }

    private Optional<Integer> getBottomTemperatureFromList(List<Integer> temperatures) {
        if (temperatures.size() == 3) {
            return Optional.of(temperatures.get(2));
        }

        if (temperatures.size() == 2) {
            return Optional.of(temperatures.get(1));
        }

        return Optional.empty();
    }
}
