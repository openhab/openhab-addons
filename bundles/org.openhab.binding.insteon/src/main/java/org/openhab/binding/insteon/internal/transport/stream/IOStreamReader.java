/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.insteon.internal.transport.stream;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.insteon.internal.transport.message.Msg;
import org.openhab.binding.insteon.internal.transport.message.MsgFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link IOStreamReader} represents an io stream reader
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class IOStreamReader implements Runnable {
    private static final int READ_BUFFER_SIZE = 1024;

    private final Logger logger = LoggerFactory.getLogger(IOStreamReader.class);

    private final IOStream stream;
    private final IOStreamListener listener;
    private final MsgFactory msgFactory = new MsgFactory();

    public IOStreamReader(IOStream stream, IOStreamListener listener) {
        this.stream = stream;
        this.listener = listener;
    }

    @Override
    public void run() {
        logger.trace("starting thread");
        byte[] buffer = new byte[READ_BUFFER_SIZE];
        try {
            while (!Thread.interrupted()) {
                logger.trace("checking for input data");
                // this call blocks until input data is available
                int len = stream.read(buffer);
                if (len > 0) {
                    msgFactory.addData(buffer, len);
                    processMessages();
                }
            }
        } catch (InterruptedException e) {
            logger.trace("got interrupted!");
        } catch (IOException e) {
            logger.trace("got an io exception", e);
            listener.disconnected();
        }
        logger.trace("exiting thread!");
    }

    private void processMessages() {
        // call msgFactory.processData() until it is done processing buffer
        while (!msgFactory.isDone()) {
            try {
                Msg msg = msgFactory.processData();
                if (msg != null) {
                    listener.messageReceived(msg);
                }
            } catch (IOException e) {
                // got bad data from modem,
                // unblock those waiting for ack
                listener.invalidMessageReceived();
            }
        }
    }
}
