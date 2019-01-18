/**
 * Copyright (c) 2014,2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.bluetooth;

/**
 * This is a listener interface that is e.g. used by {@link BluetoothAdapter}s after discovering new devices.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public interface BluetoothDiscoveryListener {

    /**
     * Reports the discovery of a new device.
     *
     * @param device the newly discovered {@link BluetoothDevice}
     */
    void deviceDiscovered(BluetoothDevice device);

}
