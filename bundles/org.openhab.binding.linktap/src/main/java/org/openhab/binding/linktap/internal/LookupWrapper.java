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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LookupWrapper} is a container providing common functionality for providing
 * key -> T mappings. The backend store is ConcurrentHashMap.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class LookupWrapper<@Nullable itemT> {
    private final Logger logger = LoggerFactory.getLogger(LookupWrapper.class);

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
        logger.trace("Adding {} -> {}", key, item);
        if (storeLookup.containsKey(key)) {
            final itemT found = storeLookup.get(key);
            if (found != null && !found.equals(item)) {
                return false;
            }
        }
        storeLookup.put(key, item);
        logger.trace("Total mappings after addition now : {}", storeLookup.size());
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
        logger.trace("Removing {} -> {}", key, item);
        storeLookup.remove(key, item);
        logger.trace("Total mappings after remove now : {}", storeLookup.size());
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
        logger.trace("Locating {}", key);
        itemT result = storeLookup.get(key);
        logger.trace("Located result {} -> {}", key, result);
        return result;
    }
}
