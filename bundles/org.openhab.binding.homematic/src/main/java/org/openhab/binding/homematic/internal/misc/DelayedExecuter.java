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
package org.openhab.binding.homematic.internal.misc;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.openhab.binding.homematic.internal.model.HmDatapointInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes a callback method either immediately or after a given delay for a datapoint.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class DelayedExecuter {
    private final Logger logger = LoggerFactory.getLogger(DelayedExecuter.class);

    private Map<HmDatapointInfo, Timer> delayedEvents = new HashMap<>();

    /**
     * Executes a callback method either immediately or after a given delay.
     */
    public void start(final HmDatapointInfo dpInfo, final double delay, final DelayedExecuterCallback callback)
            throws IOException, HomematicClientException {
        if (delay > 0.0) {
            synchronized (DelayedExecuter.class) {
                logger.debug("Delaying event for {} seconds: '{}'", delay, dpInfo);

                Timer timer = delayedEvents.get(dpInfo);
                if (timer != null) {
                    timer.cancel();
                }

                timer = new Timer();
                delayedEvents.put(dpInfo, timer);
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        logger.debug("Executing delayed event for '{}'", dpInfo);
                        delayedEvents.remove(dpInfo);
                        try {
                            callback.execute();
                        } catch (Exception ex) {
                            logger.error("{}", ex.getMessage(), ex);
                        }
                    }
                }, (long) (delay * 1000));
            }
        } else {
            callback.execute();
        }
    }

    /**
     * Stops all delayed events.
     */
    public void stop() {
        for (Timer timer : delayedEvents.values()) {
            timer.cancel();
        }
        delayedEvents.clear();
    }

    /**
     * Callback interface for the {@link DelayedExecuter}.
     *
     * @author Gerhard Riegler - Initial contribution
     */
    public interface DelayedExecuterCallback {

        public void execute() throws IOException, HomematicClientException;
    }
}
