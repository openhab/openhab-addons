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
package org.openhab.binding.milight.internal.discovery;

import java.net.InetAddress;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Result callback interface.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public interface DiscoveryResultListener {
    /**
     * A Milight bridge got detected
     *
     * @param addr The IP address
     * @param id The bridge ID
     * @param version The bridge version (either 3 or 6)
     */
    void bridgeDetected(InetAddress addr, String id, int version);
}
