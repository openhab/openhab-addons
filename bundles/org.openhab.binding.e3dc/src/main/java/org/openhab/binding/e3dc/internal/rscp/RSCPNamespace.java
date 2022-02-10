/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.e3dc.internal.rscp;

import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.e3dc.internal.rscp.util.ByteUtils;

/**
 * The {@link RSCPNamespace} is responsible for the definition of all
 * namespaces.
 *
 * @author Marco Loose - Initial Contribution
 */
public enum RSCPNamespace {

    RSCP("00"),
    EMS("01"),
    PVI("02"),
    BAT("03"),
    DCDC("04"),
    PM("05"),
    DB("06"),
    FMS("07"), // old Farming?
    SRV("08"),
    HA("09"),
    INFO("0A"),
    EP("0B"),
    SYS("0C"),
    UM("0D"),
    WB("0E"),
    PTDB("0F"),

    LED("10"),
    DIAG("11"),
    SGR("12"),
    MBS("13"), // verified
    EH("14"),
    UPNPC("15"),
    KNX("16"),
    EMSHB("17"),
    MYPV("18"),
    GPIO("19"),
    FARM("1A"),
    SE("1B"),
    QPI("1C"), // verified
    GAPP("1D"),
    EMSPR("1E"),
    SERVER("F8"),
    GROUP("FC"),

    WBD("20"),
    REFU("21"),
    OVP("22"),

    UNKNOWN("FF"),;

    private static final Map<String, RSCPNamespace> HEX_STRING_TO_TAG = new HashMap<>();

    static {
        for (RSCPNamespace tag : values()) {
            HEX_STRING_TO_TAG.put(ByteUtils.byteArrayToHexString(tag.getValueAsBytes()), tag);
        }
    }

    private final String hexString;

    RSCPNamespace(String hexString) {
        this.hexString = hexString;
    }

    public static RSCPNamespace getTagForHexString(String hexString) {
        return HEX_STRING_TO_TAG.get(hexString);
    }

    public static RSCPNamespace getForBytes(byte[] bytes) {
        return HEX_STRING_TO_TAG.get(ByteUtils.byteArrayToHexString(bytes));
    }

    public byte[] getValueAsBytes() {
        return ByteUtils.hexStringToByteArray(this.hexString);
    }

    public String getValue() {
        return this.hexString;
    }
}
