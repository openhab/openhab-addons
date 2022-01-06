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
package org.openhab.binding.amazondashbutton.internal.capturing;

import org.pcap4j.util.MacAddress;

/**
 * The {@link PacketCapturingHandler} is notified if a packet is captured by {@link PacketCapturingService}.
 *
 * @author Oliver Libutzki - Initial contribution
 *
 */
public interface PacketCapturingHandler {
    /**
     * Callback method to handle a captured packet.
     *
     * @param macAddress The mac address which sent the packet
     */
    public void packetCaptured(MacAddress sourceMacAddress);
}
