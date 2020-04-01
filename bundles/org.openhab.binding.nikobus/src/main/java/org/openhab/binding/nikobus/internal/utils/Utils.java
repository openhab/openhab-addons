/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.nikobus.internal.utils;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link Utils} class defines commonly used utility functions.
 *
 * @author Boris Krivonog - Initial contribution
 * @author Wouter Denayer - support for module addresses as seen in the Niko PC tool
 */
@NonNullByDefault
public class Utils {
    public static void cancel(@Nullable Future<?> future) {
        if (future != null) {
            future.cancel(true);
        }
    }

    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

}
