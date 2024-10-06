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
package org.openhab.binding.growatt.internal.dto.helper;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.growatt.internal.GrowattChannels;
import org.openhab.binding.growatt.internal.GrowattChannels.UoM;
import org.openhab.binding.growatt.internal.dto.GrottValues;
import org.openhab.core.library.types.QuantityType;

/**
 * Helper routines for the {@link GrottValues} DTO class.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class GrottValuesHelper {

    /**
     * Return the valid values from the given target DTO in a map between channel id and respective QuantityType states.
     *
     * @return a map of channel ids and respective QuantityType state values.
     */
    public static Map<String, QuantityType<?>> getChannelStates(GrottValues target)
            throws NoSuchFieldException, SecurityException, IllegalAccessException, IllegalArgumentException {
        Map<String, QuantityType<?>> map = new HashMap<>();
        GrowattChannels.getMap().entrySet().forEach(entry -> {
            String channelId = entry.getKey();
            try {
                Object field = target.getClass().getField(GrottValues.getFieldName(channelId)).get(target);
                if (field instanceof Integer) {
                    UoM uom = entry.getValue();
                    map.put(channelId, QuantityType.valueOf(((Integer) field).doubleValue() / uom.divisor, uom.units));
                }
            } catch (NoSuchFieldException | SecurityException | IllegalAccessException | IllegalArgumentException e) {
                // Exceptions should never actually occur at run time; nevertheless the caller logs if one would occur..
                // - NoSuchFieldException never occurs since we have explicitly tested this in the JUnit tests.
                // - SecurityException, IllegalAccessException never occur since all fields are public.
                // - IllegalArgumentException never occurs since we are explicitly working within this same class.
            }
        });
        return map;
    }
}
