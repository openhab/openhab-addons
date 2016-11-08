/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.amazondashbutton.internal.arp;

import org.pcap4j.packet.ArpPacket;

/**
 * The {@link ArpRequestHandler} is responsible for handling captured ARP requests.
 *
 * @author Oliver Libutzki - Initial contribution
 *
 */
public interface ArpRequestHandler {
    /**
     * Callback method to handle a captured ARP request.
     *
     * @param arpPacket The ARP Request packet which has been captured
     */
    public void handleArpRequest(ArpPacket arpPacket);
}