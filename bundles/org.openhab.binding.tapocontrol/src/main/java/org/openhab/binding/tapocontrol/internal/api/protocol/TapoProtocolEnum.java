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
package org.openhab.binding.tapocontrol.internal.api.protocol;

import java.util.EnumSet;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Enumeration for Tapo-Protocols
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public enum TapoProtocolEnum {
    PASSTHROUGH(""),
    SECUREPASSTROUGH("AES"),
    KLAP("KLAP");

    public final String value;

    private TapoProtocolEnum(String value) {
        this.value = value;
    }

    public static TapoProtocolEnum valueOfString(String label) {
        return EnumSet.allOf(TapoProtocolEnum.class).stream().filter(p -> p.value.equals(label)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException());
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getValue();
    }
}
