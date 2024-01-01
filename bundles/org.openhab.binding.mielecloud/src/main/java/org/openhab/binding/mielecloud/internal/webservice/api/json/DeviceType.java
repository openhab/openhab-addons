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
package org.openhab.binding.mielecloud.internal.webservice.api.json;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Represents the Miele device type.
 *
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public enum DeviceType {
    /**
     * {@link DeviceType} for unknown devices.
     */
    UNKNOWN,

    @SerializedName("1")
    WASHING_MACHINE,

    @SerializedName("2")
    TUMBLE_DRYER,

    @SerializedName("7")
    DISHWASHER,

    @SerializedName("8")
    DISHWASHER_SEMI_PROF,

    @SerializedName("12")
    OVEN,

    @SerializedName("13")
    OVEN_MICROWAVE,

    @SerializedName("14")
    HOB_HIGHLIGHT,

    @SerializedName("15")
    STEAM_OVEN,

    @SerializedName("16")
    MICROWAVE,

    @SerializedName("17")
    COFFEE_SYSTEM,

    @SerializedName("18")
    HOOD,

    @SerializedName("19")
    FRIDGE,

    @SerializedName("20")
    FREEZER,

    @SerializedName("21")
    FRIDGE_FREEZER_COMBINATION,

    /**
     * Might also be AUTOMATIC ROBOTIC VACUUM CLEANER.
     */
    @SerializedName("23")
    VACUUM_CLEANER,

    @SerializedName("24")
    WASHER_DRYER,

    @SerializedName("25")
    DISH_WARMER,

    @SerializedName("27")
    HOB_INDUCTION,

    @SerializedName("28")
    HOB_GAS,

    @SerializedName("31")
    STEAM_OVEN_COMBINATION,

    @SerializedName("32")
    WINE_CABINET,

    @SerializedName("33")
    WINE_CONDITIONING_UNIT,

    @SerializedName("34")
    WINE_STORAGE_CONDITIONING_UNIT,

    @SerializedName("39")
    DOUBLE_OVEN,

    @SerializedName("40")
    DOUBLE_STEAM_OVEN,

    @SerializedName("41")
    DOUBLE_STEAM_OVEN_COMBINATION,

    @SerializedName("42")
    DOUBLE_MICROWAVE,

    @SerializedName("43")
    DOUBLE_MICROWAVE_OVEN,

    @SerializedName("45")
    STEAM_OVEN_MICROWAVE_COMBINATION,

    @SerializedName("48")
    VACUUM_DRAWER,

    @SerializedName("67")
    DIALOGOVEN,

    @SerializedName("68")
    WINE_CABINET_FREEZER_COMBINATION,
}
