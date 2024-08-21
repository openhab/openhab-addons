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
package org.openhab.binding.logreader.internal.filereader;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.logreader.internal.filereader.api.FileReaderListener;
import org.openhab.binding.logreader.internal.filereader.api.LogFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for LogFileReader implementations. Implements base functions which are same for all LogFileReaders.
 *
 * @author Pauli Anttila - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractLogFileReader implements LogFileReader {
    private final Logger logger = LoggerFactory.getLogger(AbstractLogFileReader.class);

    private List<FileReaderListener> fileReaderListeners = new CopyOnWriteArrayList<>();

    @Override
    public boolean registerListener(FileReaderListener fileReaderListener) {
        Objects.requireNonNull(fileReaderListener, "It's not allowed to pass a null FileReaderListener.");
        return fileReaderListeners.contains(fileReaderListener) ? false : fileReaderListeners.add(fileReaderListener);
    }

    @Override
    public boolean unregisterListener(FileReaderListener fileReaderListener) {
        Objects.requireNonNull(fileReaderListener, "It's not allowed to pass a null FileReaderListener.");
        return fileReaderListeners.remove(fileReaderListener);
    }

    /**
     * Send file not found event to all registered listeners.
     *
     */
    public void sendFileNotFoundToListeners() {
        for (FileReaderListener fileReaderListener : fileReaderListeners) {
            try {
                fileReaderListener.fileNotFound();
            } catch (Exception e) {
                // catch all exceptions give all handlers a fair chance of handling the messages
                logger.debug("An exception occurred while calling the FileReaderListener. ", e);
            }
        }
    }

    /**
     * Send read log line to all registered listeners.
     *
     */
    public void sendLineToListeners(String line) {
        for (FileReaderListener fileReaderListener : fileReaderListeners) {
            try {
                fileReaderListener.handle(line);
            } catch (Exception e) {
                // catch all exceptions give all handlers a fair chance of handling the messages
                logger.debug("An exception occurred while calling the FileReaderListener. ", e);
            }
        }
    }

    /**
     * Send file rotation event to all registered listeners.
     *
     */
    public void sendFileRotationToListeners() {
        for (FileReaderListener fileReaderListener : fileReaderListeners) {
            try {
                fileReaderListener.fileRotated();
            } catch (Exception e) {
                // catch all exceptions give all handlers a fair chance of handling the messages
                logger.debug("An exception occurred while calling the FileReaderListener. ", e);
            }
        }
    }

    /**
     * Send exception event to all registered listeners.
     *
     */
    public void sendExceptionToListeners(Exception e) {
        for (FileReaderListener fileReaderListener : fileReaderListeners) {
            try {
                fileReaderListener.handle(e);
            } catch (Exception ex) {
                // catch all exceptions give all handlers a fair chance of handling the messages
                logger.debug("An exception occurred while calling the FileReaderListener. ", ex);
            }
        }
    }
}
