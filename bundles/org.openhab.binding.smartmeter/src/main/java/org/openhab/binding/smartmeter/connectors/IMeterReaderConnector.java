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
