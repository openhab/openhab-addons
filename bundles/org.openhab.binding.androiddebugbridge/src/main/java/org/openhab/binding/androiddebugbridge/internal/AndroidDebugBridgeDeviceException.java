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
package org.openhab.binding.androiddebugbridge.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.androiddebugbridge.internal.discovery.AndroidDebugBridgeDiscoveryService;

/**
 * The {@link AndroidDebugBridgeDiscoveryService} discover Android ADB Instances in the network.
 *
 * @author Miguel Alvarez - Initial contribution
 */
@NonNullByDefault
public class AndroidDebugBridgeDeviceException extends Exception {
    private static final long serialVersionUID = 6608406239134276286L;

    public AndroidDebugBridgeDeviceException(String message) {
        super(message);
    }
}
