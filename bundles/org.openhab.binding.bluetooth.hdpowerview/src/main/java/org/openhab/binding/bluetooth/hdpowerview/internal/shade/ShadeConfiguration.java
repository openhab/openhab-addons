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
package org.openhab.binding.bluetooth.hdpowerview.internal.shade;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ShadeConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class ShadeConfiguration {
    public String address = "";
    public int bleTimeout = 6; // seconds
    public int heartbeatDelay = 15; // seconds
    public int pollingDelay = 300; // seconds
    public String encryptionKey = "";

    @Override
    public String toString() {
        return String.format("[address:%s, bleTimeout:%d, heartbeatDelay:%d, pollingDelay:%d]", address, bleTimeout,
                heartbeatDelay, pollingDelay);
    }
}
