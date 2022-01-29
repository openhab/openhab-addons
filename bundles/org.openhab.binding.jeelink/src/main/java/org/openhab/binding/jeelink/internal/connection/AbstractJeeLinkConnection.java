/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.jeelink.internal.connection;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.concurrent.atomic.AtomicBoolean;

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

    protected void notifyClosed() {
        connectionListener.connectionClosed();
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
    public void sendCommands(String commands) {
        try {
            if (commands != null && !commands.trim().isEmpty()) {
                // do not create in try-with-resources as this will
                // close the undelying socket for TCP connections
                OutputStream initStream = getInitStream();
                if (initStream == null) {
                    throw new IOException(
                            "Connection on port " + port + " did not provide an init stream for writing init commands");
                }

                // do not close the writer as this closes the underlying stream, and
                // in case of tcp connections, the underlying socket
                OutputStreamWriter w = new OutputStreamWriter(initStream);
                for (String cmd : commands.split(";")) {
                    logger.debug("Writing to device on port {}: {} ", port, cmd);

                    w.write(cmd + "\n");
                }
                w.flush();
            }
        } catch (IOException ex) {
            logger.debug("Error writing to output stream!", ex);
            closeConnection();
            notifyAbort("propagate: " + ex.getMessage());
        }
    }
}
