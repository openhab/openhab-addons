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

    public void startWriter() {
        future = executor.submit(this);
    }

    @Override
    public void run() {

        while (!stopWriting) {
            try {
                MySensorsMessage msg = mysCon.pollMySensorsOutboundQueue();

                if (msg != null) {
                    if (msg.getNextSend() < System.currentTimeMillis()) {
                        // if we request an ACK we will wait for it and keep the message in the queue (at the end)
                        // otherwise we remove the message from the queue
                        if (msg.getAck() == 1) {
                            msg.setRetries(msg.getRetries() + 1);
                            if (!(msg.getRetries() >= MySensorsBindingConstants.MYSENSORS_NUMBER_OF_RETRIES)) {
                                msg.setNextSend(System.currentTimeMillis()
                                        + MySensorsBindingConstants.MYSENSORS_RETRY_TIMES[msg.getRetries()]);
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
    public void revertToOldStatus(MySensorsStatusUpdateEvent event) {
        // TODO Auto-generated method stub

    }
}
