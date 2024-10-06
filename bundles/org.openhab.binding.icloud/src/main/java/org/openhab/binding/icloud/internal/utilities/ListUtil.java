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
package org.openhab.binding.icloud.internal.utilities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class implements util methods for list handling.
 *
 * @author Simon Spielmann - Initial contribution
 *
 */
@NonNullByDefault
public abstract class ListUtil {

    private ListUtil() {
    }

    /**
     * Replace entries in the given originalList with entries from replacements, if the have an equal key.
     *
     * @param <K> Type of first pair element
     * @param <V> Type of second pair element
     * @param originalList List with entries to replace
     * @param replacements Replacement entries
     * @return New list with replaced entries
     */
    public static <K extends @NonNull Object, V extends @NonNull Object> List<Pair<K, V>> replaceEntries(
            List<Pair<K, V>> originalList, @Nullable List<Pair<K, V>> replacements) {
        List<Pair<K, V>> result = new ArrayList<>(originalList);
        if (replacements != null) {
            Iterator<Pair<K, V>> it = result.iterator();
            while (it.hasNext()) {
                Pair<K, V> requestHeader = it.next();
                for (Pair<K, V> replacementHeader : replacements) {
                    if (requestHeader.getKey().equals(replacementHeader.getKey())) {
                        it.remove();
                    }
                }
            }
            result.addAll(replacements);
        }
        return result;
    }
}
