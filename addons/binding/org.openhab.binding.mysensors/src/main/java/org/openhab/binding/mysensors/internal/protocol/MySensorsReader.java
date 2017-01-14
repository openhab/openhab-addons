/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.openhab.binding.mysensors.internal.event.MySensorsEventType;
import org.openhab.binding.mysensors.internal.event.MySensorsStatusUpdateEvent;
import org.openhab.binding.mysensors.internal.event.MySensorsUpdateListener;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the reader (IP & serial) that receives the messages from the MySensors network.
 *
 * @author Andrea Cioni
 * @author Tim Oberföll
 *
 */
public class MySensorsReader implements MySensorsUpdateListener, Runnable {

    protected Logger logger = LoggerFactory.getLogger(MySensorsReader.class);

    protected ExecutorService executor = Executors.newSingleThreadExecutor();
    protected Future<?> future = null;

    protected MySensorsBridgeConnection mysCon = null;
    protected InputStream inStream = null;
    protected BufferedReader reads = null;

    protected boolean stopReader = false;

    /**
     * Starts the reader process that will receive the messages from the MySensors network.
     */
    public void startReader() {
        future = executor.submit(this);
    }

    @Override
    public void run() {
        Thread.currentThread().setName(MySensorsReader.class.getName());
        String line = null;

        while (!stopReader) {
            // Is there something to read?

            try {
                if (!reads.ready()) {
                    Thread.sleep(10);
                    continue;
                }
                line = reads.readLine();

                // We lost connection
                if (line == null) {
                    logger.warn("Connection to Gateway lost!");
                    mysCon.requestDisconnection(true);
                    break;
                }

                logger.debug(line);
                MySensorsMessage msg = MySensorsMessageParser.parse(line);
                if (msg != null) {
                    MySensorsStatusUpdateEvent event = new MySensorsStatusUpdateEvent(
                            MySensorsEventType.INCOMING_MESSAGE, msg);
                    mysCon.broadCastEvent(event);
                }
            } catch (Exception e) {
                logger.error("({}) on reading from connection, message: {}", e, getClass(), e.getMessage());
            }

        }

    }

    /**
     * Stops the reader process of the bridge that receives messages from the MySensors network.
     */
    public void stopReader() {

        logger.debug("Stopping Reader thread");

        this.stopReader = true;

        if (future != null) {
            future.cancel(true);
            future = null;
        }

        if (executor != null) {
            executor.shutdown();
            executor.shutdownNow();
            executor = null;
        }

        try {
            if (reads != null) {
                reads.close();
                reads = null;
            }

            if (inStream != null) {
                inStream.close();
                inStream = null;
            }
        } catch (IOException e) {
            logger.error("Cannot close reader stream");
        }

    }

    @Override
    public void statusUpdateReceived(MySensorsStatusUpdateEvent event) {

    }

}
