/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pentair.handler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.openhab.binding.pentair.config.internal.PentairIPBridgeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for the IPBridge. Implements the connect and disconnect abstract methods of {@link PentairBaseBridgeHandler}
 *
 * @author Jeff James - Initial contribution
 *
 */
public class PentairIPBridgeHandler extends PentairBaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(PentairIPBridgeHandler.class);

    /** Socket object for connection */
    protected Socket socket;

    public PentairIPBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    protected synchronized void connect() {
        PentairIPBridgeConfig configuration = getConfigAs(PentairIPBridgeConfig.class);

        id = configuration.id;

        try {
            socket = new Socket(configuration.address, configuration.port);
            reader = new BufferedInputStream(socket.getInputStream());
            writer = new BufferedOutputStream(socket.getOutputStream());
            logger.info("Pentair IPBridge connected to {}:{}", configuration.address, configuration.port);
        } catch (UnknownHostException e) {
            String msg = String.format("unknown host name: %s", configuration.address);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
            return;
        } catch (IOException e) {
            String msg = String.format("cannot open connection to %s", configuration.address);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
            return;
        }

        parser = new Parser();
        thread = new Thread(parser);
        thread.start();

        if (socket != null && reader != null && writer != null) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Unable to connect");
        }
    }

    @Override
    protected synchronized void disconnect() {
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
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Error in closing reader");
            }
            reader = null;
        }

        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
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
