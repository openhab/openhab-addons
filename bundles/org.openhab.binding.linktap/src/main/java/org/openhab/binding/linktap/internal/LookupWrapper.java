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
package org.openhab.binding.linktap.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.validation.constraints.NotNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link LookupWrapper} is a container providing common functionality for providing
 * key -> T mappings. The backend store is ConcurrentHashMap.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class LookupWrapper<@Nullable itemT> {

    final Map<@NotNull String, @Nullable itemT> storeLookup = new ConcurrentHashMap<>();

    /**
     * Register using key the given T instance, and after addition call the specified Runnable
     * 
     * @param key - The key for the item
     * @param item - The instance to store a reference to
     * @param afterAddition - The runnable to run after the addition has been completed
     * @return - false if another item is already assigned to the key preventing the addition, or true
     *         when added successfully.
     */
    public boolean registerItem(final @NotNull String key, final @NotNull itemT item,
            @NotNull final Runnable afterAddition) {
        if (storeLookup.containsKey(key)) {
            final itemT found = storeLookup.get(key);
            if (found != null && !found.equals(item)) {
                return false;
            }
        }
        storeLookup.put(key, item);
        afterAddition.run();
        return true;
    }

    /**
     * Remove the given key and item combination
     * 
     * @param key - The expected key of the item
     * @param item - The item referenced by the key
     * @param whenEmpty - Runnable executed when no more key -> item mappings exist
     */
    public void deregisterItem(final @NotNull String key, final @NotNull itemT item,
            @NotNull final Runnable whenEmpty) {
        storeLookup.remove(key, item);
        if (storeLookup.isEmpty()) {
            whenEmpty.run();
        }
    }

    /**
     * Returns the item associated to the given key
     * 
     * @param key - the key to find the item for
     * @return - null if no item is found otherwise the found item
     */
    public @Nullable itemT getItem(final @NotNull String key) {
        return storeLookup.get(key);
    }

    /**
     * Clears a entry when only the given key is known
     *
     * @param key - the key remove if it exists
     */
    public void clearItem(final @NotNull String key) {
        storeLookup.remove(key);
    }
}
