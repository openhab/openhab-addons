/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.bmwcardata.internal.handler;

import java.math.BigDecimal;
import java.util.Locale;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bmwcardata.internal.api.dto.TelematicValue;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.core.types.util.UnitUtils;

import com.google.gson.JsonPrimitive;

/**
 * The {@link DynamicChannel} represents descriptors without a predefined channel. Their channels are created when
 * the first value is received, with an item type derived from that value.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
final class DynamicChannel {

    static final String TYPE_NUMBER = "generic-number";
    static final String TYPE_STRING = "generic-string";
    static final String TYPE_SWITCH = "generic-switch";

    private static final String DESCRIPTOR_PREFIX = "vehicle.";

    /**
     * The state of a new channel together with the channel definition derived from it.
     */
    record Definition(State state, String itemType, String channelTypeId) {
    }

    private DynamicChannel() {
    }

    /**
     * @return the channel id for a descriptor in lower-case-hyphen notation, e.g. {@code cabin-sunroof-tilt-status}
     *         for {@code vehicle.cabin.sunroof.tiltStatus}
     */
    static String channelId(String descriptor) {
        String id = descriptor.startsWith(DESCRIPTOR_PREFIX) ? descriptor.substring(DESCRIPTOR_PREFIX.length())
                : descriptor;
        return id.replaceAll("([a-z0-9])([A-Z])", "$1-$2").replaceAll("[^A-Za-z0-9]+", "-").toLowerCase(Locale.ROOT);
    }

    /**
     * Derives the channel definition from the first value of a descriptor.
     *
     * @return the definition, or {@code null} if the value does not allow to derive the item type
     */
    static @Nullable Definition define(TelematicValue telematicValue) {
        JsonPrimitive primitive = VehicleChannel.toPrimitive(telematicValue);
        if (primitive == null || VehicleChannel.isUndefined(primitive)) {
            return null;
        }
        Boolean bool = VehicleChannel.toStrictBoolean(primitive);
        if (bool != null) {
            return new Definition(OnOffType.from(bool), CoreItemFactory.SWITCH, TYPE_SWITCH);
        }
        BigDecimal number = VehicleChannel.toNumber(primitive);
        if (number != null) {
            Unit<?> unit = VehicleChannel.parseUnit(telematicValue.unit);
            String dimension = unit != null ? UnitUtils.getDimensionName(unit) : null;
            if (unit != null && dimension != null) {
                return new Definition(new QuantityType<>(number, unit), CoreItemFactory.NUMBER + ":" + dimension,
                        TYPE_NUMBER);
            }
            return new Definition(new DecimalType(number), CoreItemFactory.NUMBER, TYPE_NUMBER);
        }
        return new Definition(new StringType(primitive.getAsString()), CoreItemFactory.STRING, TYPE_STRING);
    }

    /**
     * Converts a value of a descriptor into the state for its existing channel.
     *
     * @return the state, or {@code null} if the value does not fit the item type of the channel
     */
    static @Nullable State toState(TelematicValue telematicValue, @Nullable String itemType) {
        JsonPrimitive primitive = VehicleChannel.toPrimitive(telematicValue);
        if (primitive == null || itemType == null) {
            return null;
        }
        if (VehicleChannel.isUndefined(primitive)) {
            return UnDefType.UNDEF;
        }
        if (CoreItemFactory.SWITCH.equals(itemType)) {
            Boolean bool = VehicleChannel.toBoolean(primitive);
            return bool == null ? null : OnOffType.from(bool);
        }
        if (itemType.startsWith(CoreItemFactory.NUMBER)) {
            BigDecimal number = VehicleChannel.toNumber(primitive);
            if (number == null) {
                return null;
            }
            int separator = itemType.indexOf(':');
            if (separator < 0) {
                return new DecimalType(number);
            }
            Unit<?> unit = VehicleChannel.parseUnit(telematicValue.unit);
            if (unit == null) {
                return new DecimalType(number);
            }
            // a unit of another dimension would be rejected by the item
            return itemType.substring(separator + 1).equals(UnitUtils.getDimensionName(unit))
                    ? new QuantityType<>(number, unit)
                    : null;
        }
        return new StringType(primitive.getAsString());
    }
}
