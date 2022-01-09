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
package org.openhab.binding.tplinksmarthome.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Service to get the actual ip of a TP-Link Smart Home device as registered on the network.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public interface TPLinkIpAddressService {

    /**
     * Returns the last known ip address of the given device id. If no ip address known null is returned.
     *
     * @param deviceId id of the device to get the ip address of
     * @return ip address or null
     */
    @Nullable
    String getLastKnownIpAddress(String deviceId);
}
