/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.solaredge.handler;

import org.openhab.binding.solaredge.internal.command.AggregateDataUpdate;
import org.openhab.binding.solaredge.internal.command.AggregatePeriod;
import org.openhab.binding.solaredge.internal.command.SolarEdgeCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Polling worker class. This is responsible for periodic polling of sensor data.
 *
 * @author Alexander Friese - initial contribution
 *
 */
public class SolarEdgeAggregateDataPolling implements Runnable {
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
    public SolarEdgeAggregateDataPolling(SolarEdgeHandler handler) {
        this.handler = handler;
    }

    /**
     * Poll the SolarEdge Webservice one time per call.
     */
    @Override
    public void run() {
        if (handler.getWebInterface() != null) {
            logger.debug("polling SolarEdge aggregate data {}", handler.getConfiguration());

            SolarEdgeCommand adu_day = new AggregateDataUpdate(handler, AggregatePeriod.DAY);
            SolarEdgeCommand adu_week = new AggregateDataUpdate(handler, AggregatePeriod.WEEK);
            SolarEdgeCommand adu_month = new AggregateDataUpdate(handler, AggregatePeriod.MONTH);
            SolarEdgeCommand adu_year = new AggregateDataUpdate(handler, AggregatePeriod.YEAR);

            try {
                handler.getWebInterface().executeCommand(adu_day);
                handler.getWebInterface().executeCommand(adu_week);
                handler.getWebInterface().executeCommand(adu_month);
                handler.getWebInterface().executeCommand(adu_year);
            } catch (Exception e) {
                logger.warn("Caught Exception: {}", e.getMessage());
            }

        }
    }
}
