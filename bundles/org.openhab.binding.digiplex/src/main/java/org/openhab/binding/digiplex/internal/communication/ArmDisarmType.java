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
package org.openhab.binding.digiplex.internal.communication;

import java.util.Arrays;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 * Indicates type of arm/disarm message returned for PRT3 module
 *
 * @author Robert Michalak - Initial contribution
 *
 */
@NonNullByDefault
public enum ArmDisarmType {
    ARM("AA"),
    QUICK_ARM("AQ"),
    DISARM("AD"),
    UNKNOWN("");

    private String indicator;

    ArmDisarmType(String indicator) {
        this.indicator = indicator;
    }

    public static ArmDisarmType fromMessage(String indicator) {
        return Objects.requireNonNull(Arrays.stream(ArmDisarmType.values())
                .filter(type -> type.indicator.equals(indicator)).findFirst().orElse(UNKNOWN));
    }
}
