/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
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
 * Specifies the generic method to retrieve values from a device
 *
 * @author Matthias Steigenberger - Initial contribution
 * @author Mathias Gilhuber - Also-By
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
     */
    Publisher<T> getMeterValues(byte @Nullable [] initMessage, Duration period, ExecutorService executor);

    /**
     * Opens the connection to the serial port.
     *
     * @throws IOException Whenever something goes wrong while opening the connection.
     *
     */
    void openConnection() throws IOException;

    /**
     * Closes the connection to the serial port.
     *
     */
    void closeConnection();
}
