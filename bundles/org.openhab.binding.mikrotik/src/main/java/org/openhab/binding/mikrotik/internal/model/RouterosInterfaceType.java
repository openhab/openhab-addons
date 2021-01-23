/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

public enum RouterosInterfaceType {
    ETHERNET("ether"),
    BRIDGE("bridge"),
    CAP("cap"),
    PPPOE_CLIENT("pppoe-out"),
    L2TP_SERVER("l2tp-in");

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

    public static RouterosInterfaceType resolve(String routerosType) {
        for (RouterosInterfaceType current : RouterosInterfaceType.values()) {
            if (current.typeName.equals(routerosType)) {
                return current;
            }
        }
        return null;
    }
}
