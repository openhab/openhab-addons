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
package org.openhab.binding.arcam.internal.connection;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ArcamConnectionReader} class manages the Socket.
 *
 * @author Joep Admiraal - Initial contribution
 */
@NonNullByDefault
public class ArcamConnectionReader extends Thread {
    private Socket socket;
    private final Logger logger = LoggerFactory.getLogger(ArcamConnectionReader.class);
    private ArcamConnectionReaderListener listener;

    private ReentrantLock mutex = new ReentrantLock();
    private boolean shouldStop;

    public ArcamConnectionReader(Socket socket, ArcamConnectionReaderListener listener) {
        this.socket = socket;
        this.listener = listener;
    }

    private boolean shouldStop() {
        boolean result = false;
        mutex.lock();
        if (shouldStop) {
            result = true;
        }
        mutex.unlock();

        if (result) {
            logger.debug("Arcam Connection detected should stop");
        }
        return result;
    }

    @Override
    public void run() {
        ArcamResponseHandler responseHandler = new ArcamResponseHandler();
        try {
            InputStream input = socket.getInputStream();

            while (!shouldStop()) {
                // Checking with available() so we don't block with input.read() when no data is coming in.
                if (input.available() == 0) {
                    Thread.sleep(100);
                    continue;
                }

                byte responseData[] = new byte[1];
                int bytesRead = input.read(responseData);

                for (int j = 0; j < bytesRead; j++) {
                    ArcamResponse response = responseHandler.parseByte(responseData[j]);
                    if (response != null) {
                        listener.onResponse(response);
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            logger.warn("Something went wrong in the connectionReader. Message: {}", e.getMessage());
            listener.onConnReadError();
        }

        logger.debug("ACR thread done");
    }

    public void dispose() {
        mutex.lock();
        shouldStop = true;
        mutex.unlock();
        logger.debug("ACR dispose done");
    }
}
