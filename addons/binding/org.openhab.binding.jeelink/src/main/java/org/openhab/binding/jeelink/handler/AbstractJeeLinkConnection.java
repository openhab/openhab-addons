/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jeelink.handler;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.openhab.binding.jeelink.config.JeeLinkConfig;
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

    protected final ArrayList<JeeLinkReadingConverter<?>> inputHandlers = new ArrayList<>();
    protected final ArrayList<ConnectionListener> connectionListeners = new ArrayList<>();

    protected String port;

    private String[] initCommands;
    private AtomicBoolean initialized = new AtomicBoolean(false);

    public AbstractJeeLinkConnection(String port) {
        this.port = port;
    }

    @Override
    public String getPort() {
        return port;
    }

    @Override
    public void addReadingConverter(JeeLinkReadingConverter<?> converter) {
        synchronized (inputHandlers) {
            if (!inputHandlers.contains(converter)) {
                logger.debug("Added converter {}.", converter);
                inputHandlers.add(converter);
            }
        }
    }

    @Override
    public void removeReadingConverters() {
        synchronized (inputHandlers) {
            inputHandlers.clear();
        }
    }

    @Override
    public void addConnectionListener(ConnectionListener listener) {
        synchronized (connectionListeners) {
            if (!connectionListeners.contains(listener)) {
                logger.debug("Added connection listener {}.", listener);
                connectionListeners.add(listener);
            }
        }
    }

    @Override
    public void removeConnectionListener(ConnectionListener listener) {
        synchronized (connectionListeners) {
            connectionListeners.remove(listener);
        }
    }

    protected void notifyOpen() {
        synchronized (connectionListeners) {
            for (ConnectionListener l : connectionListeners) {
                l.connectionOpened();
            }
        }
    }

    protected void notifyAbort(String cause) {
        synchronized (connectionListeners) {
            for (ConnectionListener l : connectionListeners) {
                l.connectionAborted(cause);
            }
        }

        initialized.set(false);
    }

    public void propagateLine(String line) throws IOException {
        logger.trace("Read line from port {}: {}", port, line);

        synchronized (inputHandlers) {
            for (JeeLinkReadingConverter<?> l : inputHandlers) {
                if (l.handleInput(line) != null && !initialized.getAndSet(true)) {
                    initializeDevice();
                }
            }
        }
    }

    @Override
    public void setInitCommands(String initCommands) {
        if (initCommands != null && !initCommands.trim().isEmpty()) {
            this.initCommands = initCommands.split(";");
        }
    }

    private void initializeDevice() throws IOException {
        if (initCommands != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Initializing device on port {} with commands {} ", port, Arrays.toString(initCommands));
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
        }
    }

    public static JeeLinkConnection createFor(JeeLinkConfig config, ScheduledExecutorService scheduler)
            throws ConnectException {
        JeeLinkConnection connection;

        if (config.serialPort != null && config.baudRate != null) {
            connection = new JeeLinkSerialConnection(config.serialPort, config.baudRate);
        } else if (config.ipAddress != null && config.port != null) {
            connection = new JeeLinkTcpConnection(config.ipAddress + ":" + config.port, scheduler);
        } else {
            throw new ConnectException("Connection configuration incomplete");
        }

        return connection;
    }
}
