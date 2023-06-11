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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.openhab.binding.amazondashbutton.internal.pcap.PcapNetworkInterfaceWrapper;
import org.pcap4j.core.BpfProgram.BpfCompileMode;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PacketListener;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode;
import org.pcap4j.packet.ArpPacket;
import org.pcap4j.packet.EthernetPacket;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.UdpPacket;
import org.pcap4j.packet.namednumber.ArpOperation;
import org.pcap4j.packet.namednumber.UdpPort;
import org.pcap4j.util.MacAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PacketCapturingService} is responsible for capturing packets.
 *
 * @author Oliver Libutzki - Initial contribution
 *
 */
public class PacketCapturingService {

    private final Logger logger = LoggerFactory.getLogger(PacketCapturingService.class);

    private static final int READ_TIMEOUT = 10; // [ms]
    private static final int SNAPLEN = 65536; // [bytes]

    private final PcapNetworkInterfaceWrapper pcapNetworkInterface;

    private PcapHandle pcapHandle;

    public PacketCapturingService(PcapNetworkInterfaceWrapper pcapNetworkInterface) {
        this.pcapNetworkInterface = pcapNetworkInterface;
    }

    /**
     * Calls {@link #startCapturing(PacketCapturingHandler, String)} with a null MAC address.
     *
     * @param packetCapturingHandler The handler to be called every time packet is captured
     * @return Returns true, if the capturing has been started successfully, otherwise returns false
     */
    public boolean startCapturing(final PacketCapturingHandler packetCapturingHandler) {
        return startCapturing(packetCapturingHandler, null);
    }

    /**
     * Starts the capturing in a dedicated thread, so this method returns immediately. Every time a packet is captured,
     * the {@link PacketCapturingHandler#packetCaptured(MacAddress)} of the given
     * {@link PacketCapturingHandler} is called.
     *
     * It's possible to capture packets sent by a specific MAC address by providing the given parameter. If the
     * macAddress is null, all MAC addresses are considered.
     *
     * @param packetCapturingHandler The handler to be called every time a packet is captured
     * @param macAddress The source MAC address of the captured packet, might be null in order to deactivate this filter
     *            criteria
     * @return Returns true, if the capturing has been started successfully, otherwise returns false
     * @throws IllegalStateException Thrown if {@link PcapHandle#isOpen()} of {@link #pcapHandle} returns true
     */

    public boolean startCapturing(final PacketCapturingHandler packetCapturingHandler, final String macAddress) {
        if (pcapHandle != null) {
            if (pcapHandle.isOpen()) {
                throw new IllegalStateException("There is an open pcap handle.");
            } else {
                pcapHandle.close();
            }
        }
        try {
            pcapHandle = pcapNetworkInterface.openLive(SNAPLEN, PromiscuousMode.PROMISCUOUS, READ_TIMEOUT);
            StringBuilder filterBuilder = new StringBuilder("(arp or port bootps)");
            if (macAddress != null) {
                filterBuilder.append(" and ether src " + macAddress);
            }
            pcapHandle.setFilter(filterBuilder.toString(), BpfCompileMode.OPTIMIZE);
        } catch (Exception e) {
            logger.error("Capturing packets on device {} failed.", pcapNetworkInterface.getName(), e);
            return false;
        }
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            try {
                pcapHandle.loop(-1, new PacketListener() {

                    @Override
                    public void gotPacket(Packet packet) {
                        if (!packet.contains(EthernetPacket.class)) {
                            return;
                        }
                        final EthernetPacket ethernetPacket = packet.get(EthernetPacket.class);
                        final MacAddress sourceMacAddress = ethernetPacket.getHeader().getSrcAddr();
                        if (shouldCapture(packet)) {
                            packetCapturingHandler.packetCaptured(sourceMacAddress);
                        }
                    }
                });
            } finally {
                if (pcapHandle != null && pcapHandle.isOpen()) {
                    pcapHandle.close();
                    pcapHandle = null;
                }
            }
            return null;
        });
        if (macAddress == null) {
            logger.debug("Started capturing ARP and BOOTP requests for network device {}.",
                    pcapNetworkInterface.getName());
        } else {
            logger.debug("Started capturing ARP  and BOOTP requests for network device {} and MAC address {}.",
                    pcapNetworkInterface.getName(), macAddress);
        }
        return true;
    }

    /**
     * Checks if the given {@link Packet} should be captured.
     *
     * @param packet The packet to be checked
     * @return Returns true, if the packet should be captured, otherwise false
     */
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    private boolean shouldCapture(final Packet packet) {
        if (packet.contains(ArpPacket.class)) {
            ArpPacket arpPacket = packet.get(ArpPacket.class);
            if (arpPacket.getHeader().getOperation().equals(ArpOperation.REQUEST)) {
                return true;
            }
        }
        if (packet.contains(UdpPacket.class)) {
            final UdpPacket udpPacket = packet.get(UdpPacket.class);
            if (UdpPort.BOOTPS == udpPacket.getHeader().getDstPort()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Stops the capturing. This can be called without calling {@link #startCapturing(PacketCapturingHandler)} or
     * {@link #startCapturing(PacketCapturingHandler, String)} before.
     */
    public void stopCapturing() {
        if (pcapHandle != null) {
            if (pcapHandle.isOpen()) {
                try {
                    pcapHandle.breakLoop();
                    logger.debug("Stopped capturing ARP and BOOTP requests for network device {}.",
                            pcapNetworkInterface.getName());
                } catch (NotOpenException e) {
                    // Just ignore
                }
            } else {
                pcapHandle = null;
            }
        }
    }

    /**
     * Returns the tracked {@link PcapNetworkInterfaceWrapper}.
     *
     * @return the tracked {@link PcapNetworkInterfaceWrapper}
     */
    public PcapNetworkInterfaceWrapper getPcapNetworkInterface() {
        return pcapNetworkInterface;
    }
}
