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
 * The {@link HomeData} holds home information returned by homesdata endpoint.
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class HomeData extends NAThing implements NAModule, LocationEx {
    public class HomesDataResponse extends ApiResponse<ListBodyResponse<HomeData>> {
    }

    public class Security extends HomeData {
        private @Nullable NAObjectMap<HomeDataPerson> persons;

        public NAObjectMap<HomeDataPerson> getPersons() {
            NAObjectMap<HomeDataPerson> localPersons = persons;
            return localPersons != null ? localPersons : new NAObjectMap<>();
        }

        public List<HomeDataPerson> getKnownPersons() {
            NAObjectMap<HomeDataPerson> localPersons = persons;
            return localPersons != null ? localPersons.values().stream().filter(HomeDataPerson::isKnown).toList()
                    : List.of();
        }
    }

    public class Energy extends HomeData {
        private String temperatureControlMode = "";
        private SetpointMode thermMode = SetpointMode.UNKNOWN;
        private int thermSetpointDefaultDuration;
        private List<ThermProgram> schedules = List.of();

        public int getThermSetpointDefaultDuration() {
            return thermSetpointDefaultDuration;
        }

        public SetpointMode getThermMode() {
            return thermMode;
        }

        public String getTemperatureControlMode() {
            return temperatureControlMode;
        }

        public List<ThermProgram> getThermSchedules() {
            return schedules;
        }

        public @Nullable ThermProgram getActiveProgram() {
            return schedules.stream().filter(ThermProgram::isSelected).findFirst().orElse(null);
        }
    }

    private double altitude;
    private double[] coordinates = {};
    private @Nullable String country;
    private @Nullable String timezone;

    private NAObjectMap<HomeDataRoom> rooms = new NAObjectMap<>();
    private @Nullable NAObjectMap<HomeDataModule> modules;

    @Override
    public ModuleType getType() {
        return ModuleType.HOME;
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

    public NAObjectMap<HomeDataRoom> getRooms() {
        return rooms;
    }

    public NAObjectMap<HomeDataModule> getModules() {
        NAObjectMap<HomeDataModule> local = modules;
        return local != null ? local : new NAObjectMap<>();
    }

    public Set<FeatureArea> getFeatures() {
        return getModules().values().stream().map(m -> m.getType().feature).collect(Collectors.toSet());
    }
}
