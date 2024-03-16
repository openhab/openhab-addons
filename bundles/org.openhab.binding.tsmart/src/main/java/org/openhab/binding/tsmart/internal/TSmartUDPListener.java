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
package org.openhab.binding.tsmart.internal;

import static org.openhab.binding.tsmart.internal.TSmartBindingConstants.T_SMART_PORT;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TSmartUDPListener} is responsible for listening for UDP packets and
 * routing the information to the correct ThingHandler or Discovery Service
 *
 * @author James Melville - Initial contribution
 */
@NonNullByDefault
public class TSmartUDPListener implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(TSmartUDPListener.class);

    private static @Nullable TSmartUDPListener listener;
    private static @Nullable TSmartDiscoveryService discoService;
    private volatile static Map<InetAddress, TSmartHandler> handlerAddressMap = new ConcurrentHashMap<InetAddress, TSmartHandler>();

    private static void enableListener() {
        if (listener == null) {
            listener = new TSmartUDPListener();
            Thread t = new Thread(listener);
            t.start();
        }
    }

    /**
     * Maintain a mapping between a device address and the openHAB handler
     * in order to route any received UDP packets to the correct handler.
     * This method adds the address and ThingHandler to the map.
     *
     * @param addr Network address of device
     * @param handler Instance of ThingHander
     */
    public static void addHandler(InetAddress addr, TSmartHandler handler) {
        if (handlerAddressMap.containsKey(addr)) {
            handler.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error.duplicate-hostnames");
        } else {
            handlerAddressMap.put(addr, handler);
            enableListener();
        }
    }

    /**
     * Maintain a mapping between a device address and the openHAB handler
     * in order to route any received UDP packets to the correct handler.
     * This method removes the mapping.
     *
     * @param addr Network address of device
     */
    public static void removeHandler(InetAddress addr) {
        handlerAddressMap.remove(addr);
    }

    /**
     * Add the DiscoveryService to handle any UDP packets as responses
     * to discovery broadcast packets.
     *
     * @param discoService Instance of the TSmartDiscoveryService
     */
    public static void startDiscovery(TSmartDiscoveryService discoService) {
        TSmartUDPListener.discoService = discoService;
        enableListener();
    }

    /**
     * Stop listening for responses to discovery broadcast packets
     */
    public static void stopDiscovery() {
        discoService = null;
    }

    /**
     * Method to receive UDP packets and route them to the appropriate
     * ThingHandler or DiscoveryService.
     */
    @Override
    public void run() {

        try {
            DatagramSocket socket = new DatagramSocket(T_SMART_PORT);

            while (isEnabled()) {

                byte[] buffer = new byte[512];
                DatagramPacket response = new DatagramPacket(buffer, buffer.length);
                socket.receive(response);
                if (handlerAddressMap.containsKey(response.getAddress())) {

                    TSmartHandler handler = handlerAddressMap.get(response.getAddress());

                    if (handler != null && buffer[0] == (byte) 0xF1) {
                        handler.updateStatusHandler(buffer);
                    }
                } else {
                    TSmartDiscoveryService discoService = TSmartUDPListener.discoService;
                    if (discoService != null && buffer[0] == (byte) 0x01 && buffer[3] != (byte) 0x54) {
                        discoService.handleDiscoveryResponse(response.getAddress(), buffer);
                    }
                }
            }
            socket.close();
            listener = null;
            discoService = null;
        } catch (IOException ex) {
            for (TSmartHandler handler : handlerAddressMap.values()) {
                handler.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
            }
        }
    }

    private boolean isEnabled() {
        return !handlerAddressMap.isEmpty() || discoService != null;
    }
}
