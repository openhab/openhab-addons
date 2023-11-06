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
package org.openhab.binding.nobohub.internal.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Stores a mapping between zone ids and zones that exists.
 *
 * @author Jørgen Austvik - Initial contribution
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
public final class ZoneRegister {

    private final @NotNull Map<Integer, Zone> register = new HashMap<Integer, Zone>();

    /**
     * Stores a new Zone in the register. If a zone exists with the same id, that value is overwritten.
     *
     * @param zone The Zone to store.
     */
    public void put(Zone zone) {
        register.put(zone.getId(), zone);
    }

    /**
     * Removes a zone from the registry.
     *
     * @param zoneId The zone to remove
     * @return The zone that is removed. Null if the zone is not found.
     */
    public @Nullable Zone remove(int zoneId) {
        return register.remove(zoneId);
    }

    /**
     * Returns a Zone from the registry.
     *
     * @param zoneId The id of the zone to return.
     * @return Returns the zone, or null if it doesnt exist in the regestry.
     */
    public @Nullable Zone get(int zoneId) {
        return register.get(zoneId);
    }

    public Collection<Zone> values() {
        return register.values();
    }
}
