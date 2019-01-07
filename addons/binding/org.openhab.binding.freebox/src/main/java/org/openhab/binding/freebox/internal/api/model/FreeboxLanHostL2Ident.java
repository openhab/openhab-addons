/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
