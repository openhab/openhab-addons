/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.cul.internal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.cul.CULCommunicationException;
import org.openhab.binding.cul.CULListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for all CULHandler which brings some convenience
 * regarding registering listeners and detecting forbidden messages.
 *
 * @author Till Klocke - Initial contribution
 * @author Johannes Goehr (johgoe) - Migration to OpenHab 3.0
 * @since 1.4.0
 */
@NonNullByDefault
public abstract class AbstractCULHandler<T extends CULConfig> implements CULHandlerInternal {

    private final Logger log = LoggerFactory.getLogger(AbstractCULHandler.class);

    /**
     * Thread which sends all queued commands to the CUL.
     *
     * @author Till Klocke
     * @author Johannes Goehr (johgoe) - Migration to OpenHab 3.0
     * @since 1.4.0
     *
     */
    private class SendThread extends Thread {

        private final Logger logger = LoggerFactory.getLogger(SendThread.class);

        @Override
        public void run() {
            while (!isInterrupted()) {
                String command = sendQueue.poll();
                if (command != null) {
                    if (!command.endsWith("\r\n")) {
                        command = command + "\r\n";
                    }
                    try {
                        writeMessage(command);
                    } catch (CULCommunicationException e) {
                        logger.error("Error while writing command to CUL", e);
                    }
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    logger.debug("Error while sleeping in SendThread", e);
                }
            }
        }
    }

    /**
     * Wrapper class wraps a CULListener and a received Strings and gets
     * executed by a executor in its own thread.
     *
     * @author Till Klocke
     * @since 1.4.0
     *
     */
    private static class NotifyDataReceivedRunner implements Runnable {

        private String message;
        private CULListener listener;

        public NotifyDataReceivedRunner(CULListener listener, String message) {
            this.message = message;
            this.listener = listener;
        }

        @Override
        public void run() {
            listener.dataReceived(message);
        }
    }

    /**
     * Wrapper class wraps a CULCreditListener and a credit10ms value and gets
     * executed by a executor in its own thread.
     *
     * @author Johannes Goehr (johgoe)
     *
     */
    private static class NotifyCreditChangedRunner implements Runnable {

        private int credit10ms;
        private CULCreditListener creditListener;

        public NotifyCreditChangedRunner(CULCreditListener creditListener, int credit10ms) {
            this.credit10ms = credit10ms;
            this.creditListener = creditListener;
        }

        @Override
        public void run() {
            creditListener.creditChanged(credit10ms);
        }
    }

    /**
     * Executor to handle received messages. Every listern should be called in
     * its own thread.
     */
    protected Executor receiveExecutor = Executors.newCachedThreadPool();
    protected SendThread sendThread = new SendThread();

    protected T config;

    protected List<CULListener> listeners = new ArrayList<CULListener>();
    protected List<CULCreditListener> creditListeners = new ArrayList<CULCreditListener>();

    protected Queue<String> sendQueue = new ConcurrentLinkedQueue<String>();
    protected int credit10ms = 0;
    protected @Nullable BufferedReader br;
    protected @Nullable BufferedWriter bw;

    protected AbstractCULHandler(T config) {
        this.config = config;
    }

    @Override
    public void registerListener(CULListener listener) {
        listeners.add(listener);
    }

    @Override
    public void unregisterListener(CULListener listener) {
        listeners.remove(listener);
    }

    @Override
    public boolean hasListeners() {
        return listeners.size() > 0;
    }

    @Override
    public void registerCreditListener(CULCreditListener creditListener) {
        creditListeners.add(creditListener);
    }

    @Override
    public void unregisterCreditListener(CULCreditListener creditListener) {
        creditListeners.remove(creditListener);
    }

    @Override
    public boolean hasCreditListeners() {
        return creditListeners.size() > 0;
    }

    @Override
    public void open() throws CULDeviceException {
        openHardware();
        sendThread.start();
    }

    @Override
    public void close() {
        sendThread.interrupt();
        closeHardware();
    }

    /**
     * initialize the CUL hardware and open the connection
     *
     * @throws CULDeviceException
     */
    protected abstract void openHardware() throws CULDeviceException;

    /**
     * Close the connection to the hardware and clean up all resources.
     */
    protected abstract void closeHardware();

    @Override
    public void send(String command) {
        if (isMessageAllowed(command)) {
            sendQueue.add(command);
        }
    }

    @Override
    public void sendWithoutCheck(String message) throws CULCommunicationException {
        sendQueue.add(message);
    }

    /**
     * Checks if the message would alter the RF mode of this device.
     *
     * @param message
     *            The message to check
     * @return true if the message doesn't alter the RF mode, false if it does.
     */
    protected boolean isMessageAllowed(String message) {
        if (message.startsWith("X") || message.startsWith("x")) {
            return false;
        }
        if (message.startsWith("Ar")) {
            return false;
        }
        return true;
    }

    /**
     * Notifies each CULListener about the received data in its own thread.
     *
     * @param data
     */
    protected void notifyDataReceived(String data) {
        for (final CULListener listener : listeners) {
            receiveExecutor.execute(new NotifyDataReceivedRunner(listener, data));
        }
    }

    protected void notifyError(Exception e) {
        for (CULListener listener : listeners) {
            listener.error(e);
        }
    }

    protected void notifyCreditChanged(int credit10ms) {
        for (final CULCreditListener creditListener : creditListeners) {
            receiveExecutor.execute(new NotifyCreditChangedRunner(creditListener, credit10ms));
        }
    }

    /**
     * read and process next line from underlying transport.
     *
     * @throws CULCommunicationException
     *             if
     */
    protected void processNextLine() throws CULCommunicationException {
        String deviceName = config.getDeviceAddress();
        BufferedReader br = this.br;
        if (br == null) {
            throw new CULCommunicationException("No input stream for " + deviceName);
        }
        try {
            String data = br.readLine();
            if (data == null) {
                log.error("EOF encountered for {}", deviceName);
                throw new CULCommunicationException("EOF encountered for " + deviceName);
            }

            log.debug("Received raw message from CUL: {}", data);
            if ("EOB".equals(data)) {
                log.warn("(EOB) End of Buffer. Last message lost. Try sending less messages per time slot to the CUL");
                return;
            } else if ("LOVF".equals(data)) {
                log.warn(
                        "(LOVF) Limit Overflow: Last message lost. You are using more than 1% transmitting time. Reduce the number of rf messages");
                return;
            } else if (data.matches("^\\d+\\s+\\d+")) {
                processCreditReport(data);
                return;
            }
            notifyDataReceived(data);
            BufferedWriter bw = this.bw;
            if (bw == null) {
                log.error("Can't reequest credit, BufferedWriter is NULL");
                return;
            }
            requestCreditReport(bw);
        } catch (SocketException e) {
            try {
                this.openHardware();
            } catch (CULDeviceException e1) {
                log.error("Exception while reading from CUL port {}", deviceName, e);
                notifyError(e);

                throw new CULCommunicationException(e);
            }
        } catch (IOException e) {
            log.error("Exception while reading from CUL port {}", deviceName, e);
            notifyError(e);

            throw new CULCommunicationException(e);
        }
    }

    /**
     * process data received from credit report
     *
     * @param data
     */
    private void processCreditReport(String data) {
        // Credit report received
        String[] report = data.split(" ");
        credit10ms = Integer.parseInt(report[report.length - 1]);
        log.debug("credit10ms = {}", credit10ms);
        notifyCreditChanged(credit10ms);
    }

    /**
     * get the remaining send time on channel as seen at the last send/receive
     * event.
     *
     * @return remaining send time in 10ms units
     */
    @Override
    public int getCredit10ms() {
        return credit10ms;
    }

    /**
     * write out request for a credit report directly to CUL
     */
    private void requestCreditReport(BufferedWriter bw) {
        /* this requests a report which provides credit10ms */
        log.debug("Requesting credit report");
        try {
            bw.write("X\r\n");
            bw.flush();
        } catch (IOException e) {
            log.error("Can't write report command to CUL", e);
        }
    }

    /**
     * Write a message to the CUL.
     *
     * @param message
     * @throws CULCommunicationException
     */
    private void writeMessage(String message) throws CULCommunicationException {
        String deviceName = config.getDeviceAddress();
        log.debug("Sending raw message to CUL {}:  '{}'", deviceName, message);
        BufferedWriter bw = this.bw;
        if (bw == null) {
            log.error("Can't write message, BufferedWriter is NULL");
            return;
        }
        synchronized (bw) {
            try {
                bw.write(message);
                bw.flush();
            } catch (IOException e) {
                log.error("Can't write to CUL {}", deviceName, e);
            }
            requestCreditReport(bw);
        }
    }

    @Override
    public T getConfig() {
        return config;
    }
}
