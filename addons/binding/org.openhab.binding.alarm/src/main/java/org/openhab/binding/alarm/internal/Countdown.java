/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.alarm.internal;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Handles a countdown for the different alarm controller delays.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class Countdown {
    private Timer timer;

    /**
     * Starts a countdown and gives feedback via the callback.
     */
    public void start(final int startFrom, CountdownCallback callback) {
        stop();
        timer = new Timer();
        timer.schedule(new TimerTask() {
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
        }, 0, 1000);

    }

    /**
     * Stops a countdown.
     */
    public void stop() {
        if (timer != null) {
            timer.cancel();
        }
        timer = null;
    }

    /**
     * Returns true, if a countdown is active.
     */
    public boolean isActive() {
        return timer != null;
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
