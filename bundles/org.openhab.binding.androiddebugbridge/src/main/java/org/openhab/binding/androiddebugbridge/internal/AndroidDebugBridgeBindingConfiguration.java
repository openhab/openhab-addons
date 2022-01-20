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
package org.openhab.binding.androiddebugbridge.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link AndroidDebugBridgeConfiguration} class contains fields mapping binding configuration parameters.
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
public class AndroidDebugBridgeBindingConfiguration {
    /**
     * Port used on discovery.
     */
    public int discoveryPort = 5555;
    /**
     * Discovery reachable timeout.
     */
    public int discoveryReachableMs = 3000;
    /**
     * Discovery from ip index.
     */
    public int discoveryIpRangeMin = 0;
    /**
     * Discovery to ip index.
     */
    public int discoveryIpRangeMax = 255;
}
