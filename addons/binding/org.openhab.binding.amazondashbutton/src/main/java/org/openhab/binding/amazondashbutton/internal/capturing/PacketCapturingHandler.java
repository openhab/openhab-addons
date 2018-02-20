/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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