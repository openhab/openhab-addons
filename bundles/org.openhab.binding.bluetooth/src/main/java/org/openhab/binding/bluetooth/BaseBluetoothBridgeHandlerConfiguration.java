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
package org.openhab.binding.bluetooth;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This is the base configuration that all bluetooth bridge implementations will use.
 * Bridges may choose to use a subclass of this class as their configuration in order to
 * support more options.
 *
 * @author Connor Petty - Initial contribution from refactored code
 */
@NonNullByDefault
public class BaseBluetoothBridgeHandlerConfiguration {
    public boolean backgroundDiscovery = false;
    public int inactiveDeviceCleanupInterval = 60;
    public int inactiveDeviceCleanupThreshold = 300;
}
