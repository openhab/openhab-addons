/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.bluegiga.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bluetooth.BaseBluetoothBridgeHandlerConfiguration;

/**
 * Configuration class for {@link BlueGigaConfiguration} device.
 *
 * @author Pauli Anttila - Initial contribution
 */
@NonNullByDefault
public class BlueGigaConfiguration extends BaseBluetoothBridgeHandlerConfiguration {
    public String port = "";
    public int passiveScanIdleTime;
    public int passiveScanInterval;
    public int passiveScanWindow;
    public int activeScanInterval;
    public int activeScanWindow;
    public int connIntervalMin;
    public int connIntervalMax;
    public int connLatency;
    public int connTimeout;

    @Override
    public String toString() {
        return String.format(
                "[discovery=%b, port=%s, passiveScanIdleTime=%d, passiveScanInterval=%d, passiveScanWindow=%d"
                        + ", activeScanInterval=%d, activeScanWindow=%d, connIntervalMin=%d, connIntervalMax=%d"
                        + ", connLatency=%d, connTimeout=%d]",
                backgroundDiscovery, port, passiveScanIdleTime, passiveScanInterval, passiveScanWindow,
                activeScanInterval, activeScanWindow, connIntervalMin, connIntervalMax, connLatency, connTimeout);
    }
}
