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
import org.openhab.binding.netatmo.internal.deserialization.NAObjectMap;

/**
 * The {@link Device} holds common data for all Netatmo devices.
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class Device extends NAThing {
    private @Nullable NAObjectMap<Module> modules;
    private long dateSetup;
    private long lastUpgrade;
    private @Nullable Place place;

    public NAObjectMap<Module> getModules() {
        NAObjectMap<Module> localModules = modules;
        return localModules != null ? localModules : new NAObjectMap<>();
    }

    public long getDateSetup() {
        return dateSetup;
    }

    public long getLastUpgrade() {
        return lastUpgrade;
    }

    public Optional<Place> getPlace() {
        return Optional.ofNullable(place);
    }
}
