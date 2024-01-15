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
package org.openhab.binding.network.internal.dhcp;

import java.net.SocketException;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A singleton. IPRequestReceivedCallback objects can register and unregister.
 * If the first one is registered and there is no singleton instance, an instance will be created and the
 * receiver thread will be started. If the last IPRequestReceivedCallback is removed, the thread will be stopped
 * after the receive socket is closed.
 * IPRequestReceivedCallback will be called for the address that is registered and matches the
 * DHO_DHCP_REQUESTED_ADDRESS address field.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class DHCPListenService {
    static @Nullable DHCPPacketListenerServer instance;
    private static final Map<String, IPRequestReceivedCallback> REGISTERED_LISTENERS = new TreeMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(DHCPListenService.class);

    public static synchronized DHCPPacketListenerServer register(String hostAddress,
            IPRequestReceivedCallback dhcpListener) throws SocketException {
        DHCPPacketListenerServer instance = DHCPListenService.instance;
        if (instance == null) {
            instance = new DHCPPacketListenerServer(ipAddress -> {
                IPRequestReceivedCallback listener = REGISTERED_LISTENERS.get(ipAddress);
                if (listener != null) {
                    listener.dhcpRequestReceived(ipAddress);
                } else {
                    LOGGER.trace("DHCP request for unknown address: {}", ipAddress);
                }
            });
            DHCPListenService.instance = instance;
            instance.start();
        }
        synchronized (REGISTERED_LISTENERS) {
            REGISTERED_LISTENERS.put(hostAddress, dhcpListener);
        }
        return instance;
    }

    public static void unregister(String hostAddress) {
        synchronized (REGISTERED_LISTENERS) {
            REGISTERED_LISTENERS.remove(hostAddress);
            if (!REGISTERED_LISTENERS.isEmpty()) {
                return;
            }
        }

        final DHCPPacketListenerServer instance = DHCPListenService.instance;
        if (instance != null) {
            instance.close();
        }
        DHCPListenService.instance = null;
    }
}
