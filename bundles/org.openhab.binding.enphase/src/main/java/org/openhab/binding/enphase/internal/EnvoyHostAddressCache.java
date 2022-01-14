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
package org.openhab.binding.enphase.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Service that keeps track of host names/ip addresses of discovered Envoy devices.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public interface EnvoyHostAddressCache {

    /**
     * Returns the known host name/ip address for the device with the given serial number.
     * If not known an empty string is returned.
     *
     * @param serialNumber serial number of device to get host address for
     * @return the known host address or an empty string if not known
     */
    String getLastKnownHostAddress(String serialNumber);
}
