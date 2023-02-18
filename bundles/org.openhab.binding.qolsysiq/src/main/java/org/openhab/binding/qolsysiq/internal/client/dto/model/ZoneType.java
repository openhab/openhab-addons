/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.qolsysiq.internal.client.dto.model;

import com.google.gson.annotations.SerializedName;

/**
 * The zone physical type
 *
 * Big thanks to the folks at https://community.home-assistant.io/t/qolsys-iq-panel-2-and-3rd-party-integration/231405
 *
 * @author Dan Cunningham - Initial contribution
 */
public enum ZoneType {
    @SerializedName("0")
    UNKNOWN,
    @SerializedName("1")
    CONTACT,
    @SerializedName("2")
    MOTION,
    @SerializedName("3")
    SOUND,
    @SerializedName("4")
    BREAKAGE,
    @SerializedName("5")
    SMOKE_HEAT,
    @SerializedName("6")
    CARBON_MONOXIDE,
    @SerializedName("7")
    RADON,
    @SerializedName("8")
    TEMPERATURE,
    @SerializedName("9")
    PANIC_BUTTON,
    @SerializedName("10")
    CONTROL,
    @SerializedName("11")
    CAMERA,
    @SerializedName("12")
    LIGHT,
    @SerializedName("13")
    GPS,
    @SerializedName("14")
    SIREN,
    @SerializedName("15")
    WATER,
    @SerializedName("16")
    TILT,
    @SerializedName("17")
    FREEZE,
    @SerializedName("18")
    TAKEOVER_MODULE,
    @SerializedName("19")
    GLASSBREAK,
    @SerializedName("20")
    TRANSLATOR,
    @SerializedName("21")
    MEDICAL_PENDANT,
    @SerializedName("22")
    WATER_IQ_FLOOD,
    @SerializedName("23")
    WATER_OTHER_FLOOD,
    @SerializedName("30")
    IMAGE_SENSOR,
    @SerializedName("100")
    WIRED_SENSOR,
    @SerializedName("101")
    RF_SENSOR,
    @SerializedName("102")
    KEYFOB,
    @SerializedName("103")
    WALLFOB,
    @SerializedName("104")
    RF_KEYPAD,
    @SerializedName("105")
    PANEL,
    @SerializedName("106")
    WTTS_OR_SECONDARY,
    @SerializedName("107")
    SHOCK,
    @SerializedName("108")
    SHOCK_SENSOR_MULTI_FUNCTION,
    @SerializedName("109")
    DOOR_BELL,
    @SerializedName("110")
    CONTACT_MULTI_FUNCTION,
    @SerializedName("111")
    SMOKE_MULTI_FUNCTION,
    @SerializedName("112")
    TEMPERATURE_MULTI_FUNCTION,
    @SerializedName("113")
    SHOCK_OTHERS,
    @SerializedName("114")
    OCCUPANCY_SENSOR,
    @SerializedName("115")
    BLUETOOTH,
    @SerializedName("116")
    PANEL_GLASS_BREAK,
    @SerializedName("117")
    POWERG_SIREN,
    @SerializedName("118")
    BLUETOOTH_SPEAKER,
    @SerializedName("119")
    PANEL_MOTION,
    @SerializedName("120")
    ZWAVE_SIREN,
    @SerializedName("121")
    COUNT;
}
