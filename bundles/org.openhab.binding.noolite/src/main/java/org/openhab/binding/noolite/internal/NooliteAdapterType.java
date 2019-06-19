/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.noolite.internal;

/**
 *
 * @author Petr Shatsillo - Initial contribution
 *
 */
public enum NooliteAdapterType {
    NOOLIE_TX(0),
    NOOLITE_RX(1),
    NOOLITE_F_TX(2),
    NOOLITE_F_RX(3),
    NOOLITE_F_SERVICE(4),
    NOOLITE_F_UPDATE(5);

    private final int code;

    NooliteAdapterType(int i) {
        this.code = i;
    }

    public int getCode() {
        return code;
    }

    public static NooliteAdapterType getValue(byte value) {
        for (NooliteAdapterType e : NooliteAdapterType.values()) {
            if (e.getCode() == value) {
                return e;
            }
        }
        return null;
    }
}
