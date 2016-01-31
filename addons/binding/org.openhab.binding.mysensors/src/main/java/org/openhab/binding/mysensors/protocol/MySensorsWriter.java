package org.openhab.binding.mysensors.protocol;

import static org.openhab.binding.mysensors.MySensorsBindingConstants.*;

import java.io.PrintWriter;
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

public abstract class MySensorsWriter implements MySensorsUpdateListener, Runnable {
    protected Logger logger = LoggerFactory.getLogger(MySensorsWriter.class);

    protected boolean stopWriting = false;
    protected long lastSend = System.currentTimeMillis();
    protected PrintWriter outs = null;
    protected int sendDelay = 0;
    protected MySensorsBridgeConnection mysCon = null;

    protected ExecutorService executor = Executors.newSingleThreadExecutor();
    protected Future<?> future = null;

    public void startWriter() {
        future = executor.submit(this);
    }

    @Override
    public synchronized void run() {
        while (!stopWriting) {
            if (lastSend + sendDelay < System.currentTimeMillis()) {
                // Is there something to write?
                if (!mysCon.MySensorsMessageOutboundQueue.isEmpty()) {
                    MySensorsMessage msg = mysCon.MySensorsMessageOutboundQueue.get(0);
                    if (msg.getNextSend() < System.currentTimeMillis()) {
                        mysCon.MySensorsMessageOutboundQueue.remove(0);
                        // if we request an ACK we will wait for it and keep the message in the queue (at the end)
                        // otherwise we remove the message from the queue
                        if (msg.getAck() == 1) {
                            msg.setRetries(msg.getRetries() + 1);
                            if (!(msg.getRetries() >= MYSENSORS_NUMBER_OF_RETRIES)) {
                                msg.setNextSend(System.currentTimeMillis() + MYSENSORS_RETRY_TIMES[msg.getRetries()]);
                                mysCon.addMySensorsOutboundMessage(msg);
                            } else {
                                logger.warn("NO ACK from nodeId: " + msg.getNodeId());

                                // Revert to old state
                                MySensorsStatusUpdateEvent event = new MySensorsStatusUpdateEvent(msg);
                                for (MySensorsUpdateListener mySensorsEventListener : mysCon.updateListeners) {
                                    mySensorsEventListener.revertToOldStatus(event);
                                }

                                continue;
                            }
                        }
                        String output = MySensorsMessageParser.generateAPIString(msg);
                        logger.debug("Sending to MySensors: " + output);

                        sendMessage(output);

                    }
                    lastSend = System.currentTimeMillis();
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

        if (outs != null) {
            outs.flush();
            outs.close();
        }

    }

    @Override
    public void statusUpdateReceived(MySensorsStatusUpdateEvent event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void revertToOldStatus(MySensorsStatusUpdateEvent event) {
        // TODO Auto-generated method stub

    }
}
