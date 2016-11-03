/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.amazondashbutton.internal.arp;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.openhab.binding.amazondashbutton.internal.pcap.PcapNetworkInterfaceWrapper;
import org.pcap4j.core.BpfProgram.BpfCompileMode;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PacketListener;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode;
import org.pcap4j.packet.ArpPacket;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.namednumber.ArpOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ArpRequestTracker} is responsible for tracking/capturing ARP requests.
 *
 * @author Oliver Libutzki - Initial contribution
 *
 */
public class ArpRequestTracker {

    private static final Logger logger = LoggerFactory.getLogger(ArpRequestTracker.class);

    private static final int READ_TIMEOUT = 10; // [ms]
    private static final int SNAPLEN = 65536; // [bytes]

    private final PcapNetworkInterfaceWrapper pcapNetworkInterface;

    private PcapHandle pcapHandle;

    public ArpRequestTracker(PcapNetworkInterfaceWrapper pcapNetworkInterface) {
        this.pcapNetworkInterface = pcapNetworkInterface;
    }

    /**
     * Calls {@link #startCapturing(ArpRequestHandler, String)} with a null MAC address.
     *
     * @param arpRequestHandler The handler to be called every time an ARP Packet is detected
     * @return Returns true, if the capturing has been started successfully, otherwise returns false
     */
    public boolean startCapturing(final ArpRequestHandler arpRequestHandler) {
        return startCapturing(arpRequestHandler, null);
    }

    /**
     * Starts the capturing in a dedicated thread, so this method returns immediately. Every time an ARP request is
     * recognized, the {@link ArpRequestHandler#handleArpRequest(ArpPacket)} of the given ArpRequestHandler is called.
     *
     * It's possible to capture ARP requests sent by a specific MAC address by providing the given parameter. If the
     * macAddress is null, all MAC addresses are considered.
     *
     * @param arpRequestHandler The handler to be called every time an ARP Packet is detected
     * @param macAddress The source MAC address of the ARP Request, might be null in order to deactivate this filter
     *            criteria
     * @return Returns true, if the capturing has been started successfully, otherwise returns false
     * @throws IllegalStateException Thrown if {@link PcapHandle#isOpen()} of {@link #pcapHandle} returns true
     */

    public boolean startCapturing(final ArpRequestHandler arpRequestHandler, final String macAddress) {
        if (pcapHandle != null) {
            if (pcapHandle.isOpen()) {
                throw new IllegalStateException("There is an open pcap handle.");
            } else {
                pcapHandle.close();
            }
        }
        try {
            pcapHandle = pcapNetworkInterface.openLive(SNAPLEN, PromiscuousMode.PROMISCUOUS, READ_TIMEOUT);
            StringBuilder filterBuilder = new StringBuilder("arp");
            if (macAddress != null) {
                filterBuilder.append(" and ether src " + macAddress);
            }
            pcapHandle.setFilter(filterBuilder.toString(), BpfCompileMode.OPTIMIZE);
        } catch (Exception e) {
            logger.error("Capturing packets on device " + pcapNetworkInterface.getName() + " failed.", e);
            return false;
        }
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                try {
                    pcapHandle.loop(-1, new PacketListener() {

                        @Override
                        public void gotPacket(Packet packet) {
                            if (packet.contains(ArpPacket.class)) {
                                ArpPacket arpPacket = packet.get(ArpPacket.class);
                                if (arpPacket.getHeader().getOperation().equals(ArpOperation.REQUEST)) {
                                    arpRequestHandler.handleArpRequest(arpPacket);
                                }
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
            }
        });
        if (macAddress == null) {
            logger.debug("Started capturing ARP requests for network device {}.", pcapNetworkInterface.getName());
        } else {
            logger.debug("Started capturing ARP requests for network device {} and MAC address {}.",
                    pcapNetworkInterface.getName(), macAddress);
        }
        return true;
    }

    /**
     * Stops the capturing. This can be called without calling {@link #startCapturing(ArpRequestHandler)} or
     * {@link #startCapturing(ArpRequestHandler, String)} before.
     */
    public void stopCapturing() {
        if (pcapHandle != null) {
            if (pcapHandle.isOpen()) {
                try {
                    pcapHandle.breakLoop();
                    logger.debug("Stopped capturing ARP requests for network device {}.",
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
