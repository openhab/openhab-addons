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
package org.openhab.binding.myvaillant.internal.api;

import java.util.Locale;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.util.StringUtils;

/**
 * One kind of energy value a device reports, e.g. the consumed electrical energy for heating.
 *
 * @param operationMode e.g. {@code HEATING}, {@code DOMESTIC_HOT_WATER} or {@code COOLING}
 * @param valueType e.g. {@code CONSUMED_ELECTRICAL_ENERGY}, {@code HEAT_GENERATED} or {@code EARNED_ENVIRONMENT_ENERGY}
 *
 * @author Marius Korte - Initial contribution
 */
@NonNullByDefault
public record EnergyDataType(String operationMode, String valueType) {

    /** Channel id, e.g. {@code consumed-electrical-energy-heating} */
    public String getChannelId() {
        return toChannelIdPart(valueType) + "-" + toChannelIdPart(operationMode);
    }

    private static String toChannelIdPart(String value) {
        return value.toLowerCase(Locale.ROOT).replace('_', '-');
    }

    /** Channel label, e.g. {@code Consumed Electrical Energy Heating Today} */
    public String getLabel() {
        return words(valueType) + " " + words(operationMode) + " Today";
    }

    /** e.g. {@code CONSUMED_ELECTRICAL_ENERGY} to {@code Consumed Electrical Energy} */
    private static String words(String value) {
        return Objects.requireNonNull(StringUtils.capitalizeByUnderscore(value)).replace('_', ' ');
    }
}
