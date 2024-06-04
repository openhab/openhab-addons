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
package org.openhab.binding.linktap.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.linktap.protocol.servers.BindingServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BridgeManager} is a singleton responsible for routing based on the key (IP address) back to
 * the relevant LinkTapBridgeHandler instance.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public final class BridgeManager {

    private final Logger logger = LoggerFactory.getLogger(BridgeManager.class);

    private static final BridgeManager INSTANCE = new BridgeManager();

    final Map<String, LinkTapBridgeHandler> ipAddrLookup = new ConcurrentHashMap<String, LinkTapBridgeHandler>();
    final Map<String, LinkTapBridgeHandler> idLookup = new ConcurrentHashMap<String, LinkTapBridgeHandler>();

    private BridgeManager() {
    }

    public static BridgeManager getInstance() {
        return INSTANCE;
    }

    public boolean isEmpty() {
        return ipAddrLookup.isEmpty();
    }

    public boolean registerBridge(final String ipAddress, final LinkTapBridgeHandler bridge) {
        logger.warn("Adding {} -> {}", ipAddress, bridge);
        if (ipAddrLookup.containsKey(ipAddress)) {
            if (!bridge.equals(ipAddrLookup.get(ipAddress))) {
                return false;
            }
        }
        ipAddrLookup.put(ipAddress, bridge);
        logger.warn("Total mappings is now : {}", ipAddrLookup.size());
        BindingServlet.getInstance().registerServlet();
        return true;
    }

    public void deregisterBridge(final String ipAddress, final LinkTapBridgeHandler bridge,
            final LinkTapBridgeHandler handler) {
        logger.warn("Removing {} -> {}", ipAddress, bridge);
        ipAddrLookup.remove(ipAddress, handler);
        logger.warn("Total mappings is now : {}", ipAddrLookup.size());
        if (ipAddrLookup.isEmpty()) {
            BindingServlet.getInstance().unregisterServlet();
        }
    }

    /*
     * public LinkTapBridgeHandler getBridge(final String ipAddress) {
     * logger.warn("Locating {}", ipAddress);
     * LinkTapBridgeHandler result = ipAddrLookup.get(ipAddress);
     * logger.warn("Locating result {} -> {}", ipAddress, result);
     * return result;
     * }
     */
}
