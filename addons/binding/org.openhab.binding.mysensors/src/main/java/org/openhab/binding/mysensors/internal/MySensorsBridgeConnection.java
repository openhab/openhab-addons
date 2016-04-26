/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.mysensors.handler.MySensorsUpdateListener;
import org.openhab.binding.mysensors.protocol.MySensorsReader;
import org.openhab.binding.mysensors.protocol.MySensorsWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MySensorsBridgeConnection {

    private Logger logger = LoggerFactory.getLogger(MySensorsBridgeConnection.class);

    public List<MySensorsUpdateListener> updateListeners;
    public boolean pauseWriter = false;

    private BlockingQueue<MySensorsMessage> outboundMessageQueue = null;

    protected boolean connected = false;

    private Object holdingThread = null;
    private boolean iVersionResponse = false;

    private boolean skipStartupCheck = false;

    public MySensorsBridgeConnection(boolean skipStartupCheck) {
        outboundMessageQueue = new LinkedBlockingQueue<MySensorsMessage>();
        updateListeners = new ArrayList<>();

        this.skipStartupCheck = skipStartupCheck;
    }

    /**
     * startup connection with bridge
     *
     * @return
     */
    public abstract boolean connect();

    /**
     * shutodown method that allows the correct disconnection with the used bridge
     *
     * @return
     */
    public abstract void disconnect();

    /**
     * Start thread managing the incoming/outgoing messages. It also have the task to test the connection to gateway by
     * sending a special message (I_VERSION) to it
     *
     * @return true if the gateway test pass successfully
     */
    protected boolean startReaderWriterThread(MySensorsReader reader, MySensorsWriter writer) {

        reader.startReader();
        writer.startWriter();

        holdingThread = this;

        if (!skipStartupCheck) {
            synchronized (holdingThread) {
                try {
                    if (!iVersionResponse) {
                        this.wait(5 * 1000); // wait 2s the reply for the I_VERSION message
                    }
                } catch (Exception e) {
                    logger.error("Exception on waiting for I_VERSION message", e);
                }
            }
        } else {
            logger.warn("Skipping I_VERSION connection test, not recommended...");
            iVersionResponse = true;
        }

        if (!iVersionResponse) {
            logger.error("Cannot start reading/writing thread, probably sync message (I_VERSION) not received");
            disconnect();
        }

        return iVersionResponse;
    }

    /**
     * @param listener An Object, that wants to listen on status updates
     */
    public void addUpdateListener(MySensorsUpdateListener listener) {
        synchronized (updateListeners) {
            updateListeners.add(listener);
        }
    }

    public void removeUpdateListener(MySensorsUpdateListener listener) {
        synchronized (updateListeners) {
            if (updateListeners.contains(listener)) {
                updateListeners.remove(listener);
            }
        }
    }

    public MySensorsMessage pollMySensorsOutboundQueue() throws InterruptedException {
        return outboundMessageQueue.poll(1, TimeUnit.DAYS);
    }

    public void addMySensorsOutboundMessage(MySensorsMessage msg) {
        addMySensorsOutboundMessage(msg, 1);
    }

    public void addMySensorsOutboundMessage(MySensorsMessage msg, int copy) {
        try {
            for (int i = 0; i < copy; i++) {
                outboundMessageQueue.put(msg);
            }
        } catch (InterruptedException e) {
            logger.error("Interrupted message while ruuning");
        }
    }

    public void removeMySensorsOutboundMessage(MySensorsMessage msg) {

        pauseWriter = true;

        Iterator<MySensorsMessage> iterator = outboundMessageQueue.iterator();
        if (iterator != null) {
            while (iterator.hasNext()) {
                MySensorsMessage msgInQueue = iterator.next();
                if (msgInQueue.getNodeId() == msg.getNodeId() && msgInQueue.getChildId() == msg.getChildId()
                        && msgInQueue.getMsgType() == msg.getMsgType() && msgInQueue.getSubType() == msg.getSubType()
                        && msgInQueue.getAck() == msg.getAck() && msgInQueue.getMsg().equals(msg.getMsg())) {
                    iterator.remove();
                } else {
                    logger.debug("Message NOT removed");
                }
            }
        }

        pauseWriter = false;
    }

    public void iVersionMessageReceived(String msg) {
        iVersionResponse = true;
        synchronized (holdingThread) {
            holdingThread.notify();
        }
        logger.debug("Good,Gateway is up and running! (Ver:{})", msg);
    }
}
