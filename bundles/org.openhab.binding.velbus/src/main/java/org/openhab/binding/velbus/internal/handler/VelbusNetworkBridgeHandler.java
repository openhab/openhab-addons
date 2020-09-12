/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.velbus.internal.handler;

import static org.openhab.binding.velbus.internal.VelbusBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.Socket;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link VelbusNetworkBridgeHandler} is the handler for a Velbus network interface and connects it to
 * the framework.
 *
 * @author Cedric Boon - Initial contribution
 */
@NonNullByDefault
public class VelbusNetworkBridgeHandler extends VelbusBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(VelbusNetworkBridgeHandler.class);

    private @Nullable Socket socket;

    public VelbusNetworkBridgeHandler(Bridge velbusBridge) {
        super(velbusBridge);
    }

    /**
     * Runnable that handles inbound communication from Velbus network interface.
     * <p>
     * The thread listens to the TCP socket opened at initialization of the {@link VelbusNetworkBridgeHandler} class
     * and interprets all inbound velbus packets.
     */
    private Runnable networkEvents = () -> {
        readPackets();
    };

    @Override
    protected void connect() {
        String address = (String) getConfig().get(ADDRESS);
        BigDecimal port = (BigDecimal) getConfig().get(PORT);

        if (address != null && port != null) {
            int portInt = port.intValue();
            try {
                Socket socket = new Socket(address, portInt);
                this.socket = socket;

                initializeStreams(socket.getOutputStream(), socket.getInputStream());

                updateStatus(ThingStatus.ONLINE);
                logger.debug("Bridge online on network address {}:{}", address, portInt);
            } catch (IOException ex) {
                onConnectionLost();
                logger.debug("Failed to connect to network address {}:{}", address, port);
            }

            // Start Velbus packet listener. This listener will act on all packets coming from
            // IP-interface.
            (new Thread(networkEvents)).start();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Network address or port not configured");
            logger.debug("Network address or port not configured");
        }
    }

    @Override
    protected void disconnect() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                logger.error("Error while closing socket", e);
            }
            socket = null;
        }

        super.disconnect();
    }
}
