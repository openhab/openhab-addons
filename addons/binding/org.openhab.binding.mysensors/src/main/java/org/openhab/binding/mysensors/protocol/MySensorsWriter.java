/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.protocol;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.openhab.binding.mysensors.MySensorsBindingConstants;
import org.openhab.binding.mysensors.handler.MySensorsStatusUpdateEvent;
import org.openhab.binding.mysensors.handler.MySensorsUpdateListener;
import org.openhab.binding.mysensors.internal.MySensorsBridgeConnection;
import org.openhab.binding.mysensors.internal.MySensorsMessage;
import org.openhab.binding.mysensors.internal.MySensorsMessageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MySensorsWriter implements MySensorsUpdateListener, Runnable {
    protected Logger logger = LoggerFactory.getLogger(MySensorsWriter.class);

    protected boolean stopWriting = false;
    protected long lastSend = System.currentTimeMillis();
    protected PrintWriter outs = null;
    protected OutputStream outStream = null;
    protected MySensorsBridgeConnection mysCon = null;

    protected ExecutorService executor = Executors.newSingleThreadExecutor();
    protected Future<?> future = null;

    private static final MySensorsMessage I_VERSION_MESSAGE = new MySensorsMessage(0, 0, 3, 0, 0, 2, "");

    protected int sendDelay = 0;

    public void startWriter() {
        future = executor.submit(this);

        // Send the I_VERSION message
        mysCon.addMySensorsOutboundMessage(I_VERSION_MESSAGE);
    }

    @Override
    public void run() {

        while (!stopWriting) {
            if (!mysCon.pauseWriter) {
                try {
                    MySensorsMessage msg = mysCon.pollMySensorsOutboundQueue();

                    if (msg != null) {
                        if (msg.getNextSend() < System.currentTimeMillis()
                                && (lastSend + sendDelay) < System.currentTimeMillis()) {
                            // if we request an ACK we will wait for it and keep the message in the queue (at the end)
                            // otherwise we remove the message from the queue
                            if (msg.getAck() == 1) {
                                msg.setRetries(msg.getRetries() + 1);
                                if (!(msg.getRetries() > MySensorsBindingConstants.MYSENSORS_NUMBER_OF_RETRIES)) {
                                    msg.setNextSend(System.currentTimeMillis()
                                            + MySensorsBindingConstants.MYSENSORS_RETRY_TIMES[msg.getRetries() - 1]);
                                    mysCon.addMySensorsOutboundMessage(msg);
                                } else {
                                    logger.warn("NO ACK from nodeId: " + msg.getNodeId());
                                    if (msg.getOldMsg().isEmpty()) {
                                        logger.debug("No old status know to revert to!");
                                    } else if (msg.getRevert() == 1) {
                                        logger.debug("Reverting status!");
                                        msg.setMsg(msg.getOldMsg());
                                        MySensorsStatusUpdateEvent event = new MySensorsStatusUpdateEvent(msg);
                                        for (MySensorsUpdateListener mySensorsEventListener : mysCon.updateListeners) {
                                            mySensorsEventListener.statusUpdateReceived(event);
                                        }
                                    } else if (msg.getRevert() == 0) {
                                        logger.debug("Not reverted due to configuration!");
                                    }
                                    continue;
                                }
                            }
                            String output = MySensorsMessageParser.generateAPIString(msg);
                            logger.debug("Sending to MySensors: {}", output.trim());

                            sendMessage(output);
                            lastSend = System.currentTimeMillis();
                        } else {
                            // Is not time for send again...
                            mysCon.addMySensorsOutboundMessage(msg);
                        }
                    } else {
                        logger.warn("Message returned from queue is null");
                    }

                } catch (InterruptedException e) {
                    logger.warn("Writer thread interrupted");
                }
            }
        }
    }

    protected void sendMessage(String output) {
        outs.println(output);
        outs.flush();
    }

    public void stopWriting() {

        logger.debug("Stopping Writer thread");

        this.stopWriting = true;

        if (future != null) {
            future.cancel(true);
        }

        if (executor != null) {
            executor.shutdown();
            executor.shutdownNow();
        }

        try {
            if (outs != null) {
                outs.flush();
                outs.close();
            }

            if (outStream != null) {
                outStream.close();
            }
        } catch (IOException e) {
            logger.error("Cannot close writer stream");
        }

    }

    @Override
    public void statusUpdateReceived(MySensorsStatusUpdateEvent event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void disconnectEvent() {

    }
}
