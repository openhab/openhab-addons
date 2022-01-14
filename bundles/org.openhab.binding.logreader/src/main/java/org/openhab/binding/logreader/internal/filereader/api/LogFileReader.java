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
package org.openhab.binding.logreader.internal.filereader.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Interface for log file readers.
 *
 * @author Pauli Anttila - Initial contribution
 */
@NonNullByDefault
public interface LogFileReader {

    /**
     * Register listener.
     *
     * @param fileReaderListener callback implementation to register.
     * @return true if registering successfully done.
     */
    boolean registerListener(FileReaderListener fileReaderListener);

    /**
     * Unregister listener.
     *
     * @param fileReaderListener callback implementation to unregister.
     * @return true if unregistering successfully done.
     */
    boolean unregisterListener(FileReaderListener fileReaderListener);

    /**
     * Start log file reader.
     *
     * @param filePath file to read.
     * @param refreshRate how often file is read.
     * @throws FileReaderException
     */
    void start(String filePath, long refreshRate) throws FileReaderException;

    /**
     * Stop log file reader.
     */
    void stop();
}
