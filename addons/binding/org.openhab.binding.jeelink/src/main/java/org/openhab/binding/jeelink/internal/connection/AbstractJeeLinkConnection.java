/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jeelink.internal.connection;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.util.Arrays;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.openhab.binding.jeelink.internal.JeeLinkHandler;
import org.openhab.binding.jeelink.internal.config.JeeLinkConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for a connection to a JeeLink.
 * Manages ReadingListeners, finds out the sketch name and allows to propagate read lines.
 *
 * @author Volker Bier - Initial contribution
 */
public abstract class AbstractJeeLinkConnection implements JeeLinkConnection {
    private final Logger logger = LoggerFactory.getLogger(AbstractJeeLinkConnection.class);

    protected final ConnectionListener connectionListener;

    protected String port;

    private String[] initCommands;
    private AtomicBoolean initialized = new AtomicBoolean(false);

    public AbstractJeeLinkConnection(String port, ConnectionListener listener) {
        this.port = port;
        connectionListener = listener;
    }

    @Override
    public String getPort() {
        return port;
    }

    /**
     * returns the stream that can be used to write the init commands to the receiver.
     */
    protected abstract OutputStream getInitStream() throws IOException;

    protected void notifyOpen() {
        connectionListener.connectionOpened();
    }

    protected void notifyAbort(String cause) {
        connectionListener.connectionAborted(cause);
        initialized.set(false);
    }

    public void propagateLine(String line) throws IOException {
        logger.trace("Read line from port {}: {}", port, line);

        connectionListener.handleInput(line);
    }

    @Override
    public void sendInitCommands(String commands) {
        try {
            if (commands != null && !commands.trim().isEmpty()) {
                initCommands = commands.split(";");

                if (logger.isDebugEnabled()) {
                    logger.debug("Initializing device on port {} with commands {} ", port,
                            Arrays.toString(initCommands));
                }

                OutputStream initStream = getInitStream();
                if (initStream == null) {
                    throw new IOException(
                            "Connection on port " + port + " did not provide an init stream for writing init commands");
                }

                // do not close the writer as this closes the underlying stream, and
                // in case of tcp connections, the underlying socket
                OutputStreamWriter w = new OutputStreamWriter(initStream);
                for (String cmd : initCommands) {
                    w.write(cmd);
                }
                w.flush();
            }
        } catch (IOException ex) {
            logger.debug("Error writing to output stream!", ex);
            closeConnection();
            notifyAbort("propagate: " + ex.getMessage());
        }
    }

    public static JeeLinkConnection createFor(JeeLinkConfig config, ScheduledExecutorService scheduler,
            JeeLinkHandler h) throws ConnectException {
        JeeLinkConnection connection;

        if (config.serialPort != null && config.baudRate != null) {
            connection = new JeeLinkSerialConnection(config.serialPort, config.baudRate, h);
        } else if (config.ipAddress != null && config.port != null) {
            connection = new JeeLinkTcpConnection(config.ipAddress + ":" + config.port, scheduler, h);
        } else {
            throw new ConnectException("Connection configuration incomplete");
        }

        return connection;
    }
}
