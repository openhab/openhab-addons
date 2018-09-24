/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.solaredge.handler;

import org.openhab.binding.solaredge.internal.command.LiveDataUpdateMeterless;
import org.openhab.binding.solaredge.internal.command.LiveDataUpdatePrivateApi;
import org.openhab.binding.solaredge.internal.command.LiveDataUpdatePublicApi;
import org.openhab.binding.solaredge.internal.command.SolarEdgeCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Polling worker class. This is responsible for periodic polling of sensor data.
 *
 * @author Alexander Friese - initial contribution
 */
public class SolarEdgeLiveDataPolling implements Runnable {
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
     * @param handler handler which handles results of polling
     */
    public SolarEdgeLiveDataPolling(SolarEdgeHandler handler) {
        this.handler = handler;
    }

    /**
     * Poll the SolarEdge Webservice one time per call.
     */
    @Override
    public void run() {
        if (handler.getWebInterface() != null) {
            logger.debug("polling SolarEdge live data {}", handler.getConfiguration());

            SolarEdgeCommand ldu;

            if (handler.getConfiguration().isUsePrivateApi()) {
                ldu = new LiveDataUpdatePrivateApi(handler);
            } else {
                if (handler.getConfiguration().isMeterInstalled()) {
                    ldu = new LiveDataUpdatePublicApi(handler);
                } else {
                    ldu = new LiveDataUpdateMeterless(handler);
                }
            }

            handler.getWebInterface().enqueueCommand(ldu);
        }
    }
}
