/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smartmeter.connectors;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.reactivestreams.Publisher;

/**
 * Specifies the generic method to retrieve SML values from a device
 *
 * @author Mathias Gilhuber
 * @since 1.7.0
 */
@NonNullByDefault
public interface IMeterReaderConnector<T> {

    /**
     * Establishes the connection against the device and reads native encoded SML informations.
     * Ensures that a connection is opened and notifies any attached listeners
     *
     * @param serialParmeter
     * @param period hint for the connector to emit items in this time intervals.
     * @return native encoded SML informations from a device.
     * @throws IOException
     */
    Publisher<T> getMeterValues(byte @Nullable [] initMessage, Duration period, ExecutorService executor)
            throws IOException;

    /**
     * Open connection.
     *
     * @throws IOException
     *
     */
    void openConnection() throws IOException;

    /**
     * Close connection.
     *
     * @throws ConnectorException
     *
     */
    void closeConnection();
}
