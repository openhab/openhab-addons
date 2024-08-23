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
package org.openhab.binding.pentair.internal.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pentair.internal.config.PentairIPBridgeConfig;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PentairIPBridgeHandler } class implements the the IPBridge.
 * Implements the connect and disconnect abstract methods of {@link PentairBaseBridgeHandler}
 *
 * @author Jeff James - Initial contribution
 */

@NonNullByDefault
public class PentairIPBridgeHandler extends PentairBaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(PentairIPBridgeHandler.class);

    public PentairIPBridgeConfig config = new PentairIPBridgeConfig();

    private @Nullable Socket socket;

    public PentairIPBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    protected synchronized boolean connect() {
        config = getConfigAs(PentairIPBridgeConfig.class);

        try {
            this.socket = new Socket(config.address, config.port);
            Socket socket = this.socket;

            if (socket == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/offline.communication-error.ip-stream-error");
                return false;
            }

            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            if (inputStream == null || outputStream == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/offline.communication-error.ip-stream-error");
                return false;
            }

            setInputStream(socket.getInputStream());
            setOutputStream(socket.getOutputStream());

            logger.debug("Pentair IPBridge connected to {}:{}", config.address, config.port);
        } catch (UnknownHostException e) {
            if (getThing().getStatus() != ThingStatus.OFFLINE) {
                String msg = String.format("unknown host name: %s, %s", config.address, e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
            }
            return false;
        } catch (IOException e) {
            if (getThing().getStatus() != ThingStatus.OFFLINE) {
                String msg = String.format("cannot open connection to %s, %s", config.address, e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
            }
            return false;
        }

        return true;
    }

    @Override
    protected synchronized void disconnect() {
        Socket socket = this.socket;

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                logger.debug("error when closing socket ", e);
            }
            socket = null;
        }
    }
}
