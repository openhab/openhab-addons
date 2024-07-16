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
package org.openhab.binding.dsmr.internal.device.connector;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for connectors. Reads data from an input stream. Subclasses should implement connection specific methods
 * and trigger the reading of the data.
 *
 * @author M. Volaart - Initial contribution
 * @author Hilbrand Bouwkamp - Major refactoring. Code moved around from other classes.
 */
@NonNullByDefault
class DSMRBaseConnector {

    private final Logger logger = LoggerFactory.getLogger(DSMRBaseConnector.class);

    /**
     * Listener to send received data and errors to.
     */
    private final DSMRConnectorListener dsmrConnectorListener;

    /**
     * 1Kbyte buffer for storing received data.
     */
    private final byte[] buffer = new byte[1024]; // 1K

    /**
     * Read lock to have 1 process reading at a time.
     */
    private final Object readLock = new Object();

    /**
     * Keeps track of the open state of the connector.
     */
    private boolean open;

    public DSMRBaseConnector(final DSMRConnectorListener connectorListener) {
        this.dsmrConnectorListener = connectorListener;
    }

    /**
     * Input stream reading the Serial port.
     */
    private @Nullable BufferedInputStream inputStream;

    /**
     * Opens the connector with the given stream to read data from.
     *
     * @param inputStream input stream to read data from
     * @throws IOException throws exception in case input stream is null
     */
    protected void open(@Nullable final InputStream inputStream) throws IOException {
        if (inputStream == null) {
            throw new IOException("Inputstream is null");
        }
        this.inputStream = new BufferedInputStream(inputStream);
        open = true;
    }

    /**
     * @return Returns true if connector is in state open
     */
    protected boolean isOpen() {
        return open;
    }

    /**
     * Closes the connector.
     */
    protected void close() {
        open = false;
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (final IOException ioe) {
                logger.debug("Failed to close reader", ioe);
            }
        }
        inputStream = null;
    }

    /**
     * Reads available data from the input stream.
     */
    protected void handleDataAvailable() {
        try {
            synchronized (readLock) {
                final BufferedInputStream localInputStream = inputStream;

                if (localInputStream != null) {
                    int bytesAvailable = localInputStream.available();
                    while (bytesAvailable > 0) {
                        final int bytesAvailableRead = localInputStream.read(buffer, 0,
                                Math.min(bytesAvailable, buffer.length));

                        if (open && bytesAvailableRead > 0) {
                            dsmrConnectorListener.handleData(buffer, bytesAvailableRead);
                        } else {
                            logger.debug("Expected bytes {} to read, but {} bytes were read", bytesAvailable,
                                    bytesAvailableRead);
                        }
                        bytesAvailable = localInputStream.available();
                    }
                }
            }
        } catch (final IOException e) {
            dsmrConnectorListener.handleError(DSMRErrorStatus.SERIAL_DATA_READ_ERROR,
                    Objects.requireNonNullElse(e.getMessage(), ""));
            logger.debug("Exception on read data", e);
        }
    }
}
