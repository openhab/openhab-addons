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
package org.openhab.binding.freebox.internal.api.model;

/**
 * The {@link FreeboxLanHostL2Ident} is the Java class used to map the "LanHostL2Ident"
 * structure used by the Lan Hosts Browser API
 * https://dev.freebox.fr/sdk/os/lan/#lan-browser
 *
 * @author Laurent Garnier - Initial contribution
 */
public class FreeboxLanHostL2Ident {
    private static final String L2_TYPE_MAC_ADDRESS = "mac_address";

    private String id;
    private String type;

    public boolean isMacAddress() {
        return L2_TYPE_MAC_ADDRESS.equalsIgnoreCase(type);
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }
}
