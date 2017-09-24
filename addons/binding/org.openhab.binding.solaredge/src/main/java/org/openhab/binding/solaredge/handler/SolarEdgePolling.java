/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.solaredge.handler;

import org.openhab.binding.solaredge.internal.command.AggregateDataUpdate;
import org.openhab.binding.solaredge.internal.command.LiveDataUpdate;
import org.openhab.binding.solaredge.internal.command.SolarEdgeCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Polling worker class. This is responsible for periodic polling of sensor data.
 *
 * @author afriese - Initial Contribution
 */
public class SolarEdgePolling implements Runnable {
    /**
     * Logger
     */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Handler for delegation to callbacks.
     */
    private final SolarEdgeHandler handler;

    /**
     * Constructor.
     *
     * @param handler
     * @param config
     */
    public SolarEdgePolling(SolarEdgeHandler handler) {
        this.handler = handler;
    }

    /**
     * Poll the SolarEdge Webservice one time per call.
     */
    @Override
    public void run() {
        if (handler.getWebInterface() != null) {
            logger.debug("polling SolarEdge {}", handler.getConfiguration());

            SolarEdgeCommand ldu = new LiveDataUpdate(handler);
            SolarEdgeCommand adu = new AggregateDataUpdate(handler);
            try {
                handler.getWebInterface().executeCommand(ldu);
                handler.getWebInterface().executeCommand(adu);
            } catch (Exception e) {
                logger.error("Caught Error: {}", e.getMessage());
            }

        }
    }
}
