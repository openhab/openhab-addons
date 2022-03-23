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

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.ApiResponse;
import org.openhab.binding.netatmo.internal.deserialization.NAObjectMap;

/**
 * The {@link NAHomeStatus} holds data for a given home.
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class NAHomeStatus {
    public class NAHomeStatusResponse extends ApiResponse<NAHomeStatus> {
    }

    public class HomeStatus extends NAThing {
        private @Nullable NAObjectMap<HomeStatusModule> modules;
        private @Nullable NAObjectMap<HomeStatusPerson> persons;
        private @Nullable NAObjectMap<Room> rooms;

        public NAObjectMap<HomeStatusModule> getModules() {
            NAObjectMap<HomeStatusModule> localModules = modules;
            return localModules != null ? localModules : new NAObjectMap<>();
        }

        public NAObjectMap<HomeStatusPerson> getPersons() {
            NAObjectMap<HomeStatusPerson> localPersons = persons;
            return localPersons != null ? localPersons : new NAObjectMap<>();
        }

        public NAObjectMap<Room> getRooms() {
            NAObjectMap<Room> localRooms = rooms;
            return localRooms != null ? localRooms : new NAObjectMap<>();
        }
    }

    private @Nullable HomeStatus home;

    public Optional<HomeStatus> getHomeStatus() {
        return Optional.ofNullable(home);
    }
}
