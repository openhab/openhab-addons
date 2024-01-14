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
package org.openhab.binding.ecovacs.internal.util;

import java.util.HashMap;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public class StateOptionMapping<T extends Enum<T>> extends HashMap<T, StateOptionEntry<T>> {
    private static final long serialVersionUID = -6828690091106259902L;

    public String getMappedValue(T key) {
        StateOptionEntry<T> entry = get(key);
        if (entry != null) {
            return entry.value;
        }
        throw new IllegalArgumentException("No mapping for key " + key);
    }

    public Optional<T> findMappedEnumValue(String value) {
        return entrySet().stream().filter(entry -> entry.getValue().value.equals(value)).map(entry -> entry.getKey())
                .findFirst();
    }

    @SafeVarargs
    public static <T extends Enum<T>> StateOptionMapping<T> of(StateOptionEntry<T>... entries) {
        StateOptionMapping<T> map = new StateOptionMapping<>();
        for (StateOptionEntry<T> entry : entries) {
            map.put(entry.enumValue, entry);
        }
        return map;
    }
}
