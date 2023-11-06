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
package org.openhab.binding.tr064.internal.soap;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link CallListType} is used for post-processing the retrieved call list
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public enum CallListType {
    MISSED_COUNT("2"),
    INBOUND_COUNT("1"),
    REJECTED_COUNT("10"),
    OUTBOUND_COUNT("3"),
    JSON_LIST("");

    private final String value;

    CallListType(String value) {
        this.value = value;
    }

    public String typeString() {
        return value;
    }
}
