/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.konnected.internal;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.konnected.internal.handler.KonnectedHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class exists so that there is a delay while all of the
 * items are linked to the channel and so that the put request
 * does not go out until there has been a "calming" in the adding
 * in the linking of the channels
 *
 *
 * @author Zachary Christiansen - Initial contribution
 */
public class KonnectedPutSettingsTimer {
    private Timer timer;
    private final Logger logger = LoggerFactory.getLogger(KonnectedHandler.class);

    public KonnectedPutSettingsTimer() {
        this.timer = new Timer();
    }

    /**
     * Creates a {@link TimerTask} that will wait 30 seconds before executing a
     * {@link doPut} Calling this method with cancel the previously created
     * timer which created but not yet executed by this method.
     *
     * @param urlAddress the address to send the request
     *
     * @param payload the json payload to include with the request
     *
     */

    public KonnectedPutSettingsTimer startTimer(String urlAddress, String payload) {
        this.timer.cancel();
        this.timer = new Timer();
        KonnectedTimerdoPut timerTask = new KonnectedTimerdoPut(urlAddress, payload);
        // set a new timer to run in 30 seconds
        try {
            logger.debug("setting a timeer with url: {} and payload: {}", urlAddress, payload);
            this.timer.schedule(timerTask, TimeUnit.SECONDS.toMillis(30));
        } catch (Exception e) {
            logger.debug("The timer encountered an exception: {}", e);
        }
        return this;
    }

    public void cancelTimer() {
        this.timer.cancel();
        // reset the timer so it can be recreated
        this.timer = new Timer();
    }

    class KonnectedTimerdoPut extends TimerTask {

        private String payload;
        private String urlAddress;
        private KonnectedHTTPUtils http;
        private KonnectedHandler handler;
        private final Logger logger = LoggerFactory.getLogger(KonnectedHandler.class);

        /**
         * Holds the {@link doPut} request with a sleep timer of one mintue to allow the konnected module to reset after
         * receiveing the request
         *
         * @param urlAddress the address to send the request
         *
         * @param payload the json payload to include with the request
         *
         */

        public KonnectedTimerdoPut(String urlAddress, String payload) {
            this.payload = payload;
            this.urlAddress = urlAddress;
            this.http = new KonnectedHTTPUtils();
        }

        @Override
        public void run() {
            try {
                String response = http.doPut(urlAddress, payload);
                logger.debug("The resposne of the put request was: {}", response);
            }

            catch (IOException e) {
                logger.debug("The put request encountered and exception: {}", e);
            }
            timer.cancel(); // Terminate the timer thread
            try {
                logger.debug("Pausing for one min to allow konnected module to reboot.");
                TimeUnit.MINUTES.sleep(1);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
