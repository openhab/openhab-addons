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
package org.openhab.binding.nobohub.internal.model;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Stores a mapping between override ids and overrides that are in place.
 *
 * @author Jørgen Austvik - Initial contribution
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
public final class OverrideRegister {

    private final @NotNull Map<Integer, Override> register = new HashMap<Integer, Override>();

    /**
     * Stores a new Override in the register. If an override exists with the same id, that value is overwritten.
     *
     * @param override The Override to store.
     */
    public void put(Override override) {
        register.put(override.getId(), override);
    }

    /**
     * Removes an override from the registry.
     *
     * @param overrideId The override to remove
     * @return The override that is removed. Null if the override is not found.
     */
    public @Nullable Override remove(int overrideId) {
        return register.remove(overrideId);
    }

    /**
     * Returns an Override from the registry.
     *
     * @param overrideId The id of the override to return.
     * @return Returns the override, or null if it doesnt exist in the regestry.
     */
    public @Nullable Override get(int overrideId) {
        return register.get(overrideId);
    }
}
