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
package org.openhab.binding.netatmo.internal.presence;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Objects;

/**
 * {@link CameraAddress} handles the data to address a camera (VPN and local address).
 *
 * @author Sven Strohschein
 */
@NonNullByDefault
public class CameraAddress {

    private final String vpnURL;
    private final String localURL;

    CameraAddress(final String vpnURL, final String localURL) {
        this.vpnURL = vpnURL;
        this.localURL = localURL;
    }

    public String getVpnURL() {
        return vpnURL;
    }

    public String getLocalURL() {
        return localURL;
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        CameraAddress that = (CameraAddress) object;
        return vpnURL.equals(that.vpnURL) && localURL.equals(that.localURL);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vpnURL, localURL);
    }
}
