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

        public NAObjectMap<HomeStatusModule> getModules() {
            NAObjectMap<HomeStatusModule> localModules = modules;
            return localModules != null ? localModules : new NAObjectMap<>();
        }
    }

    public class Energy extends HomeStatus {
        private NAObjectMap<Room> rooms = new NAObjectMap<>();

        public NAObjectMap<Room> getRooms() {
            return rooms;
        }
    }

    public class Security extends HomeStatus {
        private NAObjectMap<HomeStatusPerson> persons = new NAObjectMap<>();

        public NAObjectMap<HomeStatusPerson> getPersons() {
            return persons;
        }
    }

    private @Nullable HomeStatus home;

    public Optional<HomeStatus> getHomeStatus() {
        return Optional.ofNullable(home);
    }
}
