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

    private Object holdingThread = null;

    private boolean iVersionResponse = false;

    public void startReader(Object o) {
        future = executor.submit(this);
        holdingThread = o;
    }

    @Override
    public void run() {
        String line = null;

        while (!stopReader) {
            // Is there something to read?

            try {
                line = reads.readLine();
                logger.debug(line);
                MySensorsMessage msg = MySensorsMessageParser.parse(line);
                if (msg != null) {
                    if (!iVersionResponse && MySensorsMessageParser.isIVersionMessage(msg)) {
                        handleIncomingVersionMessage(msg.msg);
                    }

                    MySensorsStatusUpdateEvent event = new MySensorsStatusUpdateEvent(msg);
                    for (MySensorsUpdateListener mySensorsEventListener : mysCon.updateListeners) {
                        mySensorsEventListener.statusUpdateReceived(event);
                    }
                }
            } catch (Exception e) {
                // FIXME this exception has to be fixed, is not normal to have exception: Underlying input stream
                // returned zero bytes
                // logger.error("exception on reading from serial port, message: {}", e.getMessage());
            }

        }

    }

    private void handleIncomingVersionMessage(String message) {
        iVersionResponse = true;
        synchronized (holdingThread) {
            holdingThread.notify();
        }
        logger.debug("Good,Gateway is up and running! (Ver:{})", message);
    }

    public boolean isIVersionMessageArrived() {
        return iVersionResponse;
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
    public void revertToOldStatus(MySensorsStatusUpdateEvent event) {
        // TODO Auto-generated method stub

    }

}
