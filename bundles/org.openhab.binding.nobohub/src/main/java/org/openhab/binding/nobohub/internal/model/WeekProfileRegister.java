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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Stores a mapping between week profile ids and week profiles that exists.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public final class WeekProfileRegister {

    private @NotNull Map<Integer, WeekProfile> register = new HashMap<Integer, WeekProfile>();

    /**
     * Stores a new week profile in the register. If a week profile exists with the same id, that value is overwritten.
     *
     * @param profile The week profile to store.
     */
    public void put(WeekProfile profile) {
        register.put(profile.getId(), profile);
    }

    /**
     * Removes a WeekProfile from the registry.
     *
     * @param weekProfileId The week profile to remove
     * @return The week profile that is removed. Null if the week profile is not found.
     */
    public @Nullable WeekProfile remove(int weekProfileId) {
        return register.remove(weekProfileId);
    }

    /**
     * Returns a WeekProfile from the registry.
     *
     * @param weekProfileId The id of the week profile to return.
     * @return Returns the week profile, or null if it doesnt exist in the registry.
     */
    public @Nullable WeekProfile get(int weekProfileId) {
        return register.get(weekProfileId);
    }

    /**
     * Returns all WeekProfiles from the registry.
     *
     * @return Returns the week profile, or empty list if no profiles.
     */
    public Collection<WeekProfile> values() {
        return register.values();
    }

    public boolean isEmpty() {
        return register.isEmpty();
    }
}
