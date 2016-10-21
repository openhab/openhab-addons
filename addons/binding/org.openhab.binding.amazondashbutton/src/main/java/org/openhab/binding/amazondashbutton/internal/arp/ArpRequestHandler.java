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