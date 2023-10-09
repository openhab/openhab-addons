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
package org.openhab.binding.tapocontrol.internal.api.protocol;

import java.util.HashMap;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Enumeration for Tapo-Protocols
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public enum TapoProtocolEnum {
    PASSTHROUGH("passthrough"),
    SECUREPASSTROUGH("securePassthrough"),
    KLAP("klap");

    private static final HashMap<String, TapoProtocolEnum> BY_VALUE = new HashMap<>();
    public final String value;

    static {
        for (TapoProtocolEnum e : values()) {
            BY_VALUE.put(e.value, e);
        }
    }

    private TapoProtocolEnum(String value) {
        this.value = value;
    }

    public static TapoProtocolEnum valueOfString(String label) {
        return Objects.requireNonNull(BY_VALUE.get(label));
    }
}
