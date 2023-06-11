/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.io.IOException;
import java.net.Socket;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.velbus.internal.config.VelbusNetworkBridgeConfig;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
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
    private @NonNullByDefault({}) VelbusNetworkBridgeConfig networkBridgeConfig;

    public VelbusNetworkBridgeHandler(Bridge velbusBridge) {
        super(velbusBridge);
    }

    @Override
    public void initialize() {
        this.networkBridgeConfig = getConfigAs(VelbusNetworkBridgeConfig.class);

        super.initialize();
    }

    /**
     * Makes a connection to the Velbus system.
     *
     * @return True if the connection succeeded, false if the connection did not succeed.
     */
    @Override
    protected boolean connect() {
        try {
            Socket socket = new Socket(networkBridgeConfig.address, networkBridgeConfig.port);
            this.socket = socket;

            initializeStreams(socket.getOutputStream(), socket.getInputStream());

            updateStatus(ThingStatus.ONLINE);
            logger.debug("Bridge online on network address {}:{}", networkBridgeConfig.address,
                    networkBridgeConfig.port);

            // Start Velbus packet listener. This listener will act on all packets coming from
            // IP-interface.
            Thread thread = new Thread(this::readPackets, "OH-binding-" + this.thing.getUID());
            thread.setDaemon(true);
            thread.start();

            return true;
        } catch (IOException ex) {
            onConnectionLost();
            logger.debug("Failed to connect to network address {}:{}", networkBridgeConfig.address,
                    networkBridgeConfig.port);
        }

        return false;
    }

    @Override
    protected void disconnect() {
        final Socket socket = this.socket;
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                logger.debug("Error while closing socket", e);
            }
            this.socket = null;
        }

        super.disconnect();
    }
}
