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
package org.openhab.binding.freeboxos.internal.api.lan;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link L2Ident} is the Java class used to map the "LanHostL2Ident"
 * structure used by the Lan Hosts Browser API
 * https://dev.freebox.fr/sdk/os/lan/#lan-browser
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
class L2Ident {
    private static final String L2_TYPE_MAC_ADDRESS = "mac_address";

    private @NonNullByDefault({}) String id;
    private @Nullable String type;

    public @Nullable String getIfMac() {
        return L2_TYPE_MAC_ADDRESS.equalsIgnoreCase(type) ? id.toLowerCase() : null;
    }
}
