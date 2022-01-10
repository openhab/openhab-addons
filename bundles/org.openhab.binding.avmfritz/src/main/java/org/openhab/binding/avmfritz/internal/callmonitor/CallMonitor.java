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
package org.openhab.binding.avmfritz.internal.callmonitor;

import static org.openhab.binding.avmfritz.internal.AVMFritzBindingConstants.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.avmfritz.internal.handler.BoxHandler;
import org.openhab.core.library.types.StringListType;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles all communication with the Call Monitor port of the FRITZ!Box.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public class CallMonitor {

    protected final Logger logger = LoggerFactory.getLogger(CallMonitor.class);

    // port number to connect to FRITZ!Box
    private static final int MONITOR_PORT = 1012;

    private @Nullable CallMonitorThread monitorThread;
    private final ScheduledFuture<?> reconnectJob;

    private final String ip;
    private final BoxHandler handler;

    public CallMonitor(String ip, BoxHandler handler, ScheduledExecutorService scheduler) {
        this.ip = ip;
        this.handler = handler;
        reconnectJob = scheduler.scheduleWithFixedDelay(() -> {
            stopThread();

            // Wait before reconnect
            try {
                Thread.sleep(5000L);
            } catch (InterruptedException e) {
            }

            // create a new thread for listening to the FRITZ!Box
            CallMonitorThread thread = new CallMonitorThread("OH-binding-" + handler.getThing().getUID().getAsString());
            thread.start();
            this.monitorThread = thread;
        }, 0, 2, TimeUnit.HOURS);
        // initialize states of Call Monitor channels
        resetChannels();
    }

    /**
     * Reset channels.
     */
    public void resetChannels() {
        handler.updateState(CHANNEL_CALL_INCOMING, UnDefType.UNDEF);
        handler.updateState(CHANNEL_CALL_OUTGOING, UnDefType.UNDEF);
        handler.updateState(CHANNEL_CALL_ACTIVE, UnDefType.UNDEF);
        handler.updateState(CHANNEL_CALL_STATE, CALL_STATE_IDLE);
    }

    /**
     * Cancel the reconnect job.
     */
    public void dispose() {
        reconnectJob.cancel(true);
        stopThread();
    }

    public class CallMonitorThread extends Thread {

        // Socket to connect
        private @Nullable Socket socket;

        // Thread control flag
        private boolean interrupted = false;

        // time to wait before reconnecting
        private long reconnectTime = 60000L;

        public CallMonitorThread(String threadName) {
            super(threadName);
            setUncaughtExceptionHandler((thread, throwable) -> {
                logger.warn("Lost connection to FRITZ!Box because of an uncaught exception: ", throwable);
            });
        }

        @Override
        public void run() {
            while (!interrupted) {
                BufferedReader reader = null;
                try {
                    logger.debug("Call Monitor thread [{}] attempting connection to FRITZ!Box on {}:{}.",
                            Thread.currentThread().getId(), ip, MONITOR_PORT);
                    socket = new Socket(ip, MONITOR_PORT);
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    // reset the retry interval
                    reconnectTime = 60000L;
                } catch (IOException e) {
                    handler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Cannot connect to FRITZ!Box Call Monitor - make sure to enable it by dialing '#96*5*'!");
                    logger.debug("Error attempting to connect to FRITZ!Box. Retrying in {} seconds",
                            reconnectTime / 1000L, e);
                    try {
                        Thread.sleep(reconnectTime);
                    } catch (InterruptedException ex) {
                        interrupted = true;
                    }
                    // wait another more minute the next time
                    reconnectTime += 60000L;
                }
                if (reader != null) {
                    logger.debug("Connected to FRITZ!Box Call Monitor at {}:{}.", ip, MONITOR_PORT);
                    handler.setStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);
                    while (!interrupted) {
                        try {
                            if (reader.ready()) {
                                String line = reader.readLine();
                                if (line != null) {
                                    logger.debug("Received raw call string from FRITZ!Box: {}", line);
                                    CallEvent ce = new CallEvent(line);
                                    handleCallEvent(ce);
                                }
                            }
                        } catch (IOException e) {
                            if (interrupted) {
                                logger.debug("Lost connection to FRITZ!Box because of an interrupt.");
                            } else {
                                handler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                        "Lost connection to FRITZ!Box: " + e.getMessage());
                            }
                            break;
                        } finally {
                            try {
                                sleep(500L);
                            } catch (InterruptedException e) {
                                interrupted = true;
                            }
                        }
                    }
                }
            }
        }

        /**
         * Close socket and stop running thread.
         */
        @Override
        public void interrupt() {
            interrupted = true;
            if (socket != null) {
                try {
                    socket.close();
                    logger.debug("Socket to FRITZ!Box closed.");
                } catch (IOException e) {
                    logger.warn("Failed to close connection to FRITZ!Box.", e);
                }
            } else {
                logger.debug("Socket to FRITZ!Box not open, therefore not closing it.");
            }
        }

        /**
         * Handle call event and update item as required.
         *
         * @param ce call event to process
         */
        private void handleCallEvent(CallEvent ce) {
            switch (ce.getCallType()) {
                case CallEvent.CALL_TYPE_DISCONNECT:
                    // reset states of Call Monitor channels
                    resetChannels();
                    break;
                case CallEvent.CALL_TYPE_RING: // first event when call is incoming
                    handler.updateState(CHANNEL_CALL_INCOMING,
                            new StringListType(ce.getInternalNo(), ce.getExternalNo()));
                    handler.updateState(CHANNEL_CALL_OUTGOING, UnDefType.UNDEF);
                    handler.updateState(CHANNEL_CALL_ACTIVE, UnDefType.UNDEF);
                    handler.updateState(CHANNEL_CALL_STATE, CALL_STATE_RINGING);
                    break;
                case CallEvent.CALL_TYPE_CONNECT: // when call is answered/running
                    handler.updateState(CHANNEL_CALL_ACTIVE, new StringListType(ce.getExternalNo(), ""));
                    handler.updateState(CHANNEL_CALL_INCOMING, UnDefType.UNDEF);
                    handler.updateState(CHANNEL_CALL_OUTGOING, UnDefType.UNDEF);
                    handler.updateState(CHANNEL_CALL_STATE, CALL_STATE_ACTIVE);
                    break;
                case CallEvent.CALL_TYPE_CALL: // outgoing call
                    handler.updateState(CHANNEL_CALL_INCOMING, UnDefType.UNDEF);
                    handler.updateState(CHANNEL_CALL_OUTGOING,
                            new StringListType(ce.getExternalNo(), ce.getInternalNo()));
                    handler.updateState(CHANNEL_CALL_ACTIVE, UnDefType.UNDEF);
                    handler.updateState(CHANNEL_CALL_STATE, CALL_STATE_DIALING);
                    break;
            }
        }
    }

    public void stopThread() {
        logger.debug("Stopping Call Monitor thread...");
        CallMonitorThread thread = this.monitorThread;
        if (thread != null) {
            thread.interrupt();
            monitorThread = null;
        }
    }
}
