/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.evcc.internal.handler;

import static org.openhab.binding.evcc.internal.EvccBindingConstants.NUMBER_DIMENSIONLESS;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.NUMBER_ELECTRIC_CURRENT;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.NUMBER_EMISSION_INTENSITY;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.NUMBER_ENERGY;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.NUMBER_LENGTH;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.NUMBER_POWER;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.NUMBER_TIME;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;

/**
 * The {@link Utils} provides utility methods
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class Utils {

    private static final Map<String, Unit<?>> UNIT_MAP = Map.of(NUMBER_LENGTH, SIUnits.METRE, NUMBER_POWER, Units.WATT,
            NUMBER_ENERGY, Units.WATT_HOUR, NUMBER_TIME, Units.SECOND, NUMBER_ELECTRIC_CURRENT, Units.AMPERE,
            NUMBER_DIMENSIONLESS, Units.ONE, NUMBER_EMISSION_INTENSITY, SIUnits.GRAM.divide(Units.KILOWATT_HOUR));

    /**
     * This method retrieves the unit for the corresponding item type
     * 
     * @param itemType the unit will be received for
     * @return Unit<?> that is mapped to the item type
     */
    public static Unit<?> getUnitFromChannelType(String itemType) {
        Unit<?> unit = UNIT_MAP.get(itemType);
        return Objects.requireNonNullElse(unit, Units.ONE);
    }

    /**
     * This method will capitalize the words of a given string
     * 
     * @param input string containing hyphenized words
     * @return A string with spaces instead of hyphens and the first letter of each word is capitalized
     */
    public static String capitalizeWords(String input) {
        String[] allParts = input.split("-");
        String[] parts = Arrays.stream(allParts, 1, allParts.length).toArray(String[]::new);
        StringJoiner joiner = new StringJoiner(" ");

        for (String part : parts) {
            if (!part.isEmpty()) {
                joiner.add(Character.toUpperCase(part.charAt(0)) + part.substring(1));
            }
        }

        return joiner.toString();
    }

    /**
     * This method will sanitize a given string for a channel ID
     * 
     * @param input camel case string
     * @return A string that contains hyphens
     */
    public static String sanitizeChannelID(String input) {
        return input.replaceAll("(?<!^)(?=[A-Z])", "-").toLowerCase();
    }

    /**
     * This method will get the key from a ChannelUID
     * 
     * @param channelUID which the key shall be retrieved from
     * @return the key in camel case
     */
    public static String getKeyFromChannelUID(ChannelUID channelUID) {
        String[] split = channelUID.getIdWithoutGroup().split("-");
        String[] parts = Arrays.stream(split, 1, split.length).toArray(String[]::new);

        StringBuilder camelCase = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (i == 0) {
                camelCase.append(part.toLowerCase());
            } else {
                camelCase.append(part.substring(0, 1).toUpperCase());
                camelCase.append(part.substring(1).toLowerCase());
            }
        }

        return camelCase.toString();
    }

    /**
     * This method will convert the version string into an int array
     * If the input is blank, the method will return 0, 0, 0
     * 
     * @param input version string
     * @return int array of size 3
     */
    public static int[] convertVersionStringToIntArray(String input) {
        if (input.isEmpty()) {
            return new int[] { 0, 0, 0 };
        }
        return Arrays.stream(input.split("\\.")).mapToInt(Integer::parseInt).toArray();
    }
}
