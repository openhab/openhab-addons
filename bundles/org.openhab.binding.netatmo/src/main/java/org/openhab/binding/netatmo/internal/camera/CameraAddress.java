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
package org.openhab.binding.netatmo.internal.camera;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * {@link CameraAddress} handles the data to address a camera (VPN and local address).
 *
 * @author Sven Strohschein - Initial contribution
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

    /**
     * Checks if the VPN URL was changed / isn't equal to the given VPN-URL.
     * 
     * @param vpnURL old / known VPN URL
     * @return true, when the VPN URL isn't equal given VPN URL, otherwise false
     */
    public boolean isVpnURLChanged(String vpnURL) {
        return !getVpnURL().equals(vpnURL);
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        CameraAddress that = (CameraAddress) object;
        return vpnURL.equals(that.vpnURL) && localURL.equals(that.localURL);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vpnURL, localURL);
    }
}
