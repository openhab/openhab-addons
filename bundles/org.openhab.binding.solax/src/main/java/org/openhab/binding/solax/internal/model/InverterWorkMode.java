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
package org.openhab.binding.solax.internal.model;

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link InverterType} class is enum representing the different inverter types with a simple logic to convert from
 * int(coming from the JSON) to a more meaningful enum value.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public enum InverterWorkMode {

    WAITING(0, "Waiting"),
    CHECKING(1, "Checking"),
    NORMAL(2, "Normal"),
    FAULT(3, "Fault"),
    PERMANENT_FAULT(4, "Permanent Fault"),
    UPDATING(5, "Updating"),
    EPS_CHECK(6, "EPS Check"),
    SELF_TEST(7, "Self Test"),
    IDLE(9, "Idle"),
    STANDBY(10, "Standby"),
    PV_WAKE_UP_BATTERY(11, "PV Wake-up Battery"),
    GEN_CHECK(12, "GEN Check"),
    GEN_RUN(13, "GEN Run"),
    UNKNOWN(Integer.MIN_VALUE, "Unknown");

    private int code;
    // TODO Temporary field to provide English translated state until the PR [solax] Cloud connection support #16124 is
    // integrated because it adds locale,
    // timezone and other stuff from the framework. Refactoring here will be required after it
    private String text;

    InverterWorkMode(int code, String englishTranslation) {
        this.code = code;
        this.text = englishTranslation;
    }

    InverterWorkMode() {
        this.code = Integer.MIN_VALUE;
        this.text = "Unknown";
    }

    public static InverterWorkMode fromCode(int code) {
        InverterWorkMode[] values = InverterWorkMode.values();
        return Stream.of(values).filter(value -> value.code == code).findFirst().orElse(UNKNOWN);
    }

    public int getCode() {
        return code;
    }

    public String getText() {
        return text;
    }
}
