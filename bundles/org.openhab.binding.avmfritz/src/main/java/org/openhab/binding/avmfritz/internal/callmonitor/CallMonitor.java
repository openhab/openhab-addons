/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.StringListType;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.avmfritz.internal.AVMFritzBindingConstants;
import org.openhab.binding.avmfritz.internal.handler.BoxHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles all communication with the call monitor port of the fritzbox.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public class CallMonitor {

    protected final Logger logger = LoggerFactory.getLogger(CallMonitor.class);

    // port number to connect to fritzbox
    private final int MONITOR_PORT = 1012;

    private @Nullable CallMonitorThread monitorThread;
    private ScheduledFuture<?> reconnectJob;

    private String ip;
    private BoxHandler handler;

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

            // create a new thread for listening to the FritzBox
            CallMonitorThread thread = new CallMonitorThread();
            thread.setName("OH-binding-" + handler.getThing().getUID().getAsString());
            thread.start();
            this.monitorThread = thread;
        }, 0, 2, TimeUnit.HOURS);
    }

    /**
     * Cancel the reconnect job.
     */
    public void dispose() {
        reconnectJob.cancel(true);
    }

    public class CallMonitorThread extends Thread {

        // Socket to connect
        private @Nullable Socket socket;

        // Thread control flag
        private boolean interrupted = false;

        // time to wait before reconnecting
        private long reconnectTime = 60000L;

        public CallMonitorThread() {
        }

        @Override
        public void run() {
            while (!interrupted) {
                BufferedReader reader = null;
                try {
                    logger.debug("Callmonitor thread [{}] attempting connection to FritzBox on {}:{}.",
                            Thread.currentThread().getId(), ip, MONITOR_PORT);
                    socket = new Socket(ip, MONITOR_PORT);
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    // reset the retry interval
                    reconnectTime = 60000L;
                } catch (Exception e) {
                    handler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Cannot connect to Fritz!Box call monitor - make sure to enable it by dialing '#96*5'!");
                    logger.debug("Error attempting to connect to FritzBox. Retrying in {} seconds",
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
                    logger.debug("Connected to FritzBox call monitor at {}:{}.", ip, MONITOR_PORT);
                    handler.setStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);
                    while (!interrupted) {
                        try {
                            String line = reader.readLine();
                            if (line != null) {
                                logger.debug("Received raw call string from fbox: {}", line);
                                CallEvent ce = new CallEvent(line);
                                handleCallEvent(ce);
                            }
                        } catch (IOException e) {
                            if (interrupted) {
                                logger.debug("Lost connection to Fritzbox because of an interrupt.");
                            } else {
                                handler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                        "Lost connection to Fritz!Box: " + e.getMessage());
                            }
                            break;
                        } finally {
                            try {
                                sleep(1000L);
                            } catch (InterruptedException e) {
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
                    logger.debug("Socket to FritzBox closed.");
                } catch (IOException e) {
                    logger.warn("Failed to close connection to FritzBox.", e);
                }
            } else {
                logger.debug("Socket to FritzBox not open, therefore not closing it.");
            }
        }

        /**
         * Handle call event and update item as required.
         *
         * @param ce call event to process
         */
        private void handleCallEvent(CallEvent ce) {
            if (ce.getCallType().equals("DISCONNECT")) {
                // reset states of call monitor channels
                handler.updateState(AVMFritzBindingConstants.CHANNEL_CALL_INCOMING, UnDefType.UNDEF);
                handler.updateState(AVMFritzBindingConstants.CHANNEL_CALL_OUTGOING, UnDefType.UNDEF);
                handler.updateState(AVMFritzBindingConstants.CHANNEL_CALL_ACTIVE, UnDefType.UNDEF);
                handler.updateState(AVMFritzBindingConstants.CHANNEL_CALL_STATE,
                        AVMFritzBindingConstants.CALL_STATE_IDLE);
            } else if (ce.getCallType().equals("RING")) { // first event when call is incoming
                StringListType state = new StringListType(ce.getInternalNo(), ce.getExternalNo());
                handler.updateState(AVMFritzBindingConstants.CHANNEL_CALL_INCOMING, state);
                handler.updateState(AVMFritzBindingConstants.CHANNEL_CALL_OUTGOING, UnDefType.UNDEF);
                handler.updateState(AVMFritzBindingConstants.CHANNEL_CALL_ACTIVE, UnDefType.UNDEF);
                handler.updateState(AVMFritzBindingConstants.CHANNEL_CALL_STATE,
                        AVMFritzBindingConstants.CALL_STATE_RINGING);
            } else if (ce.getCallType().equals("CONNECT")) { // when call is answered/running
                StringListType state = new StringListType(ce.getExternalNo(), "");
                handler.updateState(AVMFritzBindingConstants.CHANNEL_CALL_ACTIVE, state);
                handler.updateState(AVMFritzBindingConstants.CHANNEL_CALL_INCOMING, UnDefType.UNDEF);
                handler.updateState(AVMFritzBindingConstants.CHANNEL_CALL_OUTGOING, UnDefType.UNDEF);
                handler.updateState(AVMFritzBindingConstants.CHANNEL_CALL_STATE,
                        AVMFritzBindingConstants.CALL_STATE_ACTIVE);
            } else if (ce.getCallType().equals("CALL")) { // outgoing call
                StringListType state = new StringListType(ce.getExternalNo(), ce.getInternalNo());
                handler.updateState(AVMFritzBindingConstants.CHANNEL_CALL_INCOMING, UnDefType.UNDEF);
                handler.updateState(AVMFritzBindingConstants.CHANNEL_CALL_OUTGOING, state);
                handler.updateState(AVMFritzBindingConstants.CHANNEL_CALL_ACTIVE, UnDefType.UNDEF);
                handler.updateState(AVMFritzBindingConstants.CHANNEL_CALL_STATE,
                        AVMFritzBindingConstants.CALL_STATE_DIALING);
            }
        }
    }

    public void stopThread() {
        logger.debug("Stopping call monitor thread...");
        if (monitorThread != null) {
            monitorThread.interrupt();
            monitorThread = null;
        }
    }

    public void startThread() {
        logger.debug("Starting call monitor thread...");
        if (monitorThread != null) {
            monitorThread.interrupt();
            monitorThread = null;
        }
        // create a new thread for listening to the FritzBox
        monitorThread = new CallMonitorThread();
        monitorThread.start();
    }
}
