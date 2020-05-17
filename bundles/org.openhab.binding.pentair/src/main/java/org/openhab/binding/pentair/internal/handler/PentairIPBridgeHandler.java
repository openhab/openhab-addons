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
package org.openhab.binding.pentair.internal.handler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.openhab.binding.pentair.internal.config.PentairIPBridgeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for the IPBridge. Implements the connect and disconnect abstract methods of {@link PentairBaseBridgeHandler}
 *
 * @author Jeff James - Initial contribution
 *
 */
@NonNullByDefault
public class PentairIPBridgeHandler extends PentairBaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(PentairIPBridgeHandler.class);

    public PentairIPBridgeConfig config = new PentairIPBridgeConfig();

    /** Socket object for connection */
    @Nullable
    protected Socket socket;

    public PentairIPBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    protected synchronized int connect() {
        logger.debug("PenatiarIPBridgeHander: connect");

        config = getConfigAs(PentairIPBridgeConfig.class);

        this.id = config.id;
        this.discovery = config.discovery;

        try {
            Socket socket = new Socket(config.address, config.port);
            this.socket = socket;

            reader = new BufferedInputStream(socket.getInputStream());
            writer = new BufferedOutputStream(socket.getOutputStream());

            logger.info("Pentair IPBridge connected to {}:{}", config.address, config.port);
        } catch (UnknownHostException e) {
            String msg = String.format("unknown host name: %s", config.address);
            logger.debug("{}", msg);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
            return -1;
        } catch (IOException e) {
            String msg = String.format("cannot open connection to %s", config.address);
            logger.debug("{}", msg);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
            return -2;
        }

        parser = new Parser();
        Thread thread = new Thread(parser);
        this.thread = thread;
        thread.start();

        if (socket != null && reader != null && writer != null) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            logger.debug("connect: socket, reader or writer is null");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Unable to connect");
        }

        return 0;
    }

    @SuppressWarnings("null")
    @Override
    protected synchronized void disconnect() {
        logger.debug("PentairIPBridgeHandler: disconnect");
        updateStatus(ThingStatus.OFFLINE);

        if (thread != null) {
            try {
                thread.interrupt();
                thread.join(); // wait for thread to complete
            } catch (InterruptedException e) {
                // do nothing
            }
            thread = null;
            parser = null;
        }

        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                logger.debug("disconnect: IOException");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Error in closing reader");
            }
            reader = null;
        }

        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                logger.debug("disconnect: IOException");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Error in closing writer");
            }
            writer = null;
        }

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                logger.error("error when closing socket ", e);
            }
            socket = null;
        }
    }
}
