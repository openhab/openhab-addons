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
package org.openhab.binding.freeboxos.internal.api.lan.browser;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.L2Type;

/**
 * The {@link LanHostL2Ident} is the Java class used to map the "LanHostL2Ident" structure used by the Lan Hosts Browser API
 *
 * https://dev.freebox.fr/sdk/os/lan/#lan-browser
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
class LanHostL2Ident {
    private @Nullable String id;
    private L2Type type = L2Type.UNKNOWN;

    public String getId() {
        return Objects.requireNonNull(id);
    }

    public boolean isMac() {
        return L2Type.MAC_ADDRESS.equals(type);
    }
}
