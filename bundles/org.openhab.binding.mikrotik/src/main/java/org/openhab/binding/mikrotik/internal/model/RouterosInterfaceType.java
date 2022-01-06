/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.mikrotik.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link RouterosInterfaceType} enum wraps RouterOS network interface type strings.
 *
 * @author Oleg Vivtash - Initial contribution
 */

@NonNullByDefault
public enum RouterosInterfaceType {
    ETHERNET("ether"),
    BRIDGE("bridge"),
    WLAN("wlan"),
    CAP("cap"),
    PPP_CLIENT("ppp-out"),
    PPPOE_CLIENT("pppoe-out"),
    L2TP_SERVER("l2tp-in"),
    L2TP_CLIENT("l2tp-out"),
    LTE("lte");

    private final String typeName;

    RouterosInterfaceType(String routerosType) {
        this.typeName = routerosType;
    }

    public boolean equalsName(String otherType) {
        // (otherName == null) check is not needed because name.equals(null) returns false
        return typeName.equals(otherType);
    }

    public String toString() {
        return this.typeName;
    }

    public @Nullable static RouterosInterfaceType resolve(@Nullable String routerosType) {
        for (RouterosInterfaceType current : RouterosInterfaceType.values()) {
            if (current.typeName.equals(routerosType)) {
                return current;
            }
        }
        return null;
    }
}
