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
package org.openhab.binding.luxom.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link LuxomSystemInfo} class represents the systeminfo Luxom communication object. It contains all
 * Luxom system data received from the Luxom IP controller when initializing the connection.
 *
 * @author Kris Jespers - Initial contribution
 */
@NonNullByDefault
public final class LuxomSystemInfo {

    private @Nullable String swVersion = "";

    public @Nullable String getSwVersion() {
        return swVersion;
    }

    public void setSwVersion(@Nullable String swVersion) {
        this.swVersion = swVersion;
    }
}
