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
package org.openhab.binding.netatmo.internal.api.dto;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.ApiResponse;
import org.openhab.binding.netatmo.internal.api.ListBodyResponse;
import org.openhab.binding.netatmo.internal.api.data.ModuleType;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.FeatureArea;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.SetpointMode;
import org.openhab.binding.netatmo.internal.deserialization.NAObjectMap;

/**
 * The {@link NAHomeData} holds home information returned by homesdata endpoint.
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class NAHomeData extends NAThing implements NetatmoModule, NetatmoLocationEx {
    public class NAHomesDataResponse extends ApiResponse<ListBodyResponse<NAHomeData>> {
    }

    private double altitude;
    private double[] coordinates = {};
    private @Nullable String country;
    private @Nullable String timezone;

    private @Nullable String temperatureControlMode;
    private SetpointMode thermMode = SetpointMode.UNKNOWN;
    private int thermSetpointDefaultDuration;
    private List<NAThermProgram> schedules = List.of();

    private NAObjectMap<NAHomeDataPerson> persons = new NAObjectMap<>();
    private NAObjectMap<NAHomeDataRoom> rooms = new NAObjectMap<>();
    private NAObjectMap<NAHomeDataModule> modules = new NAObjectMap<>();

    @Override
    public ModuleType getType() {
        return ModuleType.NAHome;
    }

    @Override
    public double getAltitude() {
        return altitude;
    }

    @Override
    public double[] getCoordinates() {
        return coordinates;
    }

    @Override
    public Optional<String> getCountry() {
        return Optional.ofNullable(country);
    }

    @Override
    public Optional<String> getTimezone() {
        return Optional.ofNullable(timezone);
    }

    public int getThermSetpointDefaultDuration() {
        return thermSetpointDefaultDuration;
    }

    public SetpointMode getThermMode() {
        return thermMode;
    }

    public NAObjectMap<NAHomeDataPerson> getPersons() {
        return persons;
    }

    public List<NAHomeDataPerson> getKnownPersons() {
        return persons.values().stream().filter(NAHomeDataPerson::isKnown).collect(Collectors.toList());
    }

    public Optional<String> getTemperatureControlMode() {
        return Optional.ofNullable(temperatureControlMode);
    }

    public NAObjectMap<NAHomeDataRoom> getRooms() {
        return rooms;
    }

    public NAObjectMap<NAHomeDataModule> getModules() {
        return modules;
    }

    public Set<FeatureArea> getFeatures() {
        return getModules().values().stream().map(m -> m.getType().feature).collect(Collectors.toSet());
    }

    public List<NAThermProgram> getThermSchedules() {
        return schedules;
    }

    public @Nullable NAThermProgram getActiveProgram() {
        return schedules.stream().filter(NAThermProgram::isSelected).findFirst().orElse(null);
    }
}
