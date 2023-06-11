/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.logreader.internal.filereader.api.FileReaderException;
import org.openhab.binding.logreader.internal.filereader.api.LogFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Apache Tailer based log file reader implementation.
 *
 * @author Pauli Anttila - Initial contribution
 */
@NonNullByDefault
public class FileTailer extends AbstractLogFileReader implements LogFileReader {
    private final Logger logger = LoggerFactory.getLogger(FileTailer.class);

    private @Nullable Tailer tailer;
    private @Nullable ExecutorService executor;

    TailerListener logListener = new TailerListenerAdapter() {

        @Override
        public void handle(@Nullable String line) {
            if (line == null) {
                return;
            }

            sendLineToListeners(line);
        }

        @Override
        public void fileNotFound() {
            sendFileNotFoundToListeners();
        }

        @Override
        public void handle(@Nullable Exception e) {
            if (e == null) {
                return;
            }

            sendExceptionToListeners(e);
        }

        @Override
        public void fileRotated() {
            sendFileRotationToListeners();
        }
    };

    @Override
    public void start(String filePath, long refreshRate) throws FileReaderException {
        Tailer localTailer = new Tailer(new File(filePath), logListener, refreshRate, true, false, true);
        executor = Executors.newSingleThreadExecutor();
        try {
            logger.debug("Start executor");
            executor.execute(localTailer);
            logger.debug("Executor started");
            this.tailer = localTailer;
        } catch (Exception e) {
            throw new FileReaderException(e);
        }
    }

    @Override
    public void stop() {
        logger.debug("Shutdown");
        if (tailer != null) {
            tailer.stop();
        }
        if (executor != null) {
            executor.shutdown();
        }
        logger.debug("Shutdown complite");
    }
}
