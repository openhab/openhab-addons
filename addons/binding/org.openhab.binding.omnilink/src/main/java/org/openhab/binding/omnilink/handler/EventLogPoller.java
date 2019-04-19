/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.omnilink.handler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.Message;
import com.digitaldan.jomnilinkII.OmniInvalidResponseException;
import com.digitaldan.jomnilinkII.OmniUnknownMessageTypeException;
import com.digitaldan.jomnilinkII.MessageTypes.EventLogData;
import com.google.gson.Gson;

/**
 *
 * @author Craig Hamilton
 *
 */
public class EventLogPoller {

    private final static Logger logger = LoggerFactory.getLogger(AudioSourceHandler.class);

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public EventLogPoller(OmnilinkBridgeHandler bridgeHandler, int pollInterval) {
        if (pollInterval < 1) {
            throw new IllegalArgumentException("Poll interval must be greater than 0");
        } else {
            executorService.scheduleWithFixedDelay(new PollLog(bridgeHandler), pollInterval, pollInterval,
                    TimeUnit.SECONDS);
        }
    }

    public void shutdown() {
        logger.debug("Terminating EventLogPoller");
        executorService.shutdownNow();
    }

    private static class PollLog implements Runnable {

        private OmnilinkBridgeHandler bridgeHandler;

        private final Gson gson = new Gson();

        private int eventLogNumber = 0;

        private PollLog(OmnilinkBridgeHandler bridgeHandler) {
            this.bridgeHandler = bridgeHandler;
        }

        @Override
        public void run() {

            // On first run, direction is -1 (most recent event), after its 1 for the next log message

            try {
                Message message;
                do {
                    logger.debug("Polling for event log messages.");
                    int direction = eventLogNumber == 0 ? -1 : 1;
                    message = bridgeHandler.reqEventLogData(eventLogNumber, direction);
                    if (message.getMessageType() == Message.MESG_TYPE_EVENT_LOG_DATA) {
                        EventLogData logData = (EventLogData) message;
                        logger.debug("Processing event log message number: {}", logData.getEventNumber());
                        eventLogNumber = logData.getEventNumber();
                        String json = gson.toJson(logData);
                        bridgeHandler.eventLogMessage(json);
                    }
                } while (message.getMessageType() != Message.MESG_TYPE_END_OF_DATA);

            } catch (OmniInvalidResponseException | OmniUnknownMessageTypeException | BridgeOfflineException e) {
                logger.warn("Exception Polling Event Log", e);
            } catch (NullPointerException e) {
                logger.debug("NPE.  Omni connection probably not set up.", e);
            }

        }

    }

}
