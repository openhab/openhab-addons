/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.openhab.binding.mysensors.handler.MySensorsStatusUpdateEvent;
import org.openhab.binding.mysensors.handler.MySensorsUpdateListener;
import org.openhab.binding.mysensors.internal.MySensorsBridgeConnection;
import org.openhab.binding.mysensors.internal.MySensorsMessage;
import org.openhab.binding.mysensors.internal.MySensorsMessageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MySensorsReader implements MySensorsUpdateListener, Runnable {

    protected Logger logger = LoggerFactory.getLogger(MySensorsReader.class);

    protected ExecutorService executor = Executors.newSingleThreadExecutor();
    protected Future<?> future = null;

    protected MySensorsBridgeConnection mysCon = null;
    protected InputStream inStream = null;
    protected BufferedReader reads = null;

    protected boolean stopReader = false;

    public void startReader() {
        future = executor.submit(this);
    }

    @Override
    public void run() {
        String line = null;

        while (!stopReader) {
            // Is there something to read?

            try {
                line = reads.readLine();

                // We lost connection
                if (line == null) {
                    broadCastDisconnect();
                    break;
                }

                logger.debug(line);
                MySensorsMessage msg = MySensorsMessageParser.parse(line);
                if (msg != null) {
                    MySensorsStatusUpdateEvent event = new MySensorsStatusUpdateEvent(msg);
                    for (MySensorsUpdateListener mySensorsEventListener : mysCon.updateListeners) {
                        mySensorsEventListener.statusUpdateReceived(event);
                    }
                }
            } catch (Exception e) {
                logger.error("exception on reading from serial port, message: {}", e.getMessage());
            }

        }

    }

    private void broadCastDisconnect() {
        logger.warn("Connection to Gateway lost!");
        stopReader();

        for (MySensorsUpdateListener mySensorsEventListener : mysCon.updateListeners) {
            mySensorsEventListener.disconnectEvent();
        }
    }

    public void stopReader() {

        logger.debug("Stopping Reader thread");

        this.stopReader = true;

        if (future != null) {
            future.cancel(true);
        }

        if (executor != null) {
            executor.shutdown();
            executor.shutdownNow();
        }

        try {
            if (reads != null) {
                reads.close();
            }

            if (inStream != null) {
                inStream.close();
            }
        } catch (IOException e) {
            logger.error("Cannot close reader stream");
        }

    }

    @Override
    public void statusUpdateReceived(MySensorsStatusUpdateEvent event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void disconnectEvent() {
        stopReader();
    }

}
