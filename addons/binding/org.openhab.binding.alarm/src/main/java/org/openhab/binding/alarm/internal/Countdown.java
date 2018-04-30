/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.alarm.internal;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.common.ThreadPoolManager;

/**
 * Handles a countdown for the different alarm controller delays.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class Countdown {
    private static final String ALARM_POOL_NAME = "alarm";
    private ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool(ALARM_POOL_NAME);
    private ScheduledFuture<?> future;

    /**
     * Starts a countdown and gives feedback via the callback.
     */
    public void start(final int startFrom, CountdownCallback callback) {
        stop();
        future = scheduler.scheduleWithFixedDelay(new Runnable() {
            private int countdownValue = startFrom;

            @Override
            public void run() {
                if (countdownValue == 0) {
                    stop();
                    callback.finished();
                } else {
                    callback.countdownChanged(countdownValue--);
                }
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    /**
     * Stops a countdown.
     */
    public void stop() {
        if (future != null) {
            future.cancel(true);
        }
        future = null;
    }

    /**
     * Returns true, if a countdown is active.
     */
    public boolean isActive() {
        return future != null;
    }

    /**
     * Callback interface for a countdown.
     *
     * @author Gerhard Riegler - Initial contribution
     */
    public interface CountdownCallback {
        /**
         * Called every second with the current countdown value.
         */
        public void countdownChanged(int value);

        /**
         * Called when the countdown has been finished.
         */
        public void finished();
    }
}
