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
package org.openhab.binding.homematic.internal.model;

/**
 * Definition of the Homematic interfaces.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public enum HmInterface {
    RF,
    WIRED,
    HMIP,
    CUXD,
    GROUP;

    /**
     * Returns the full name of the interface.
     */
    public String getName() {
        switch (this) {
            case RF:
                return "BidCos-RF";
            case WIRED:
                return "BidCos-Wired";
            case HMIP:
                return "HmIP-RF";
            case CUXD:
                return "CUxD";
            case GROUP:
                return "Group";
        }
        return null;
    }
}
