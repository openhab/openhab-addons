/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.solaredge.internal.handler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solaredge.internal.command.AggregateDataUpdatePrivateApi;
import org.openhab.binding.solaredge.internal.command.AggregateDataUpdatePublicApi;
import org.openhab.binding.solaredge.internal.command.SolarEdgeCommand;
import org.openhab.binding.solaredge.internal.model.AggregatePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Polling worker class. This is responsible for periodic polling of sensor data.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
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
        // if no meter is present all data will be fetched by the 'LiveDataUpdateMeterless'
        if (handler.getConfiguration().isMeterInstalled()) {
            logger.debug("polling SolarEdge aggregate data {}", handler.getConfiguration());

            List<SolarEdgeCommand> commands = new ArrayList<>();

            if (handler.getConfiguration().isUsePrivateApi()) {
                commands.add(new AggregateDataUpdatePrivateApi(handler, AggregatePeriod.DAY));
                commands.add(new AggregateDataUpdatePrivateApi(handler, AggregatePeriod.WEEK));
                commands.add(new AggregateDataUpdatePrivateApi(handler, AggregatePeriod.MONTH));
                commands.add(new AggregateDataUpdatePrivateApi(handler, AggregatePeriod.YEAR));
            } else {
                commands.add(new AggregateDataUpdatePublicApi(handler, AggregatePeriod.DAY));
                commands.add(new AggregateDataUpdatePublicApi(handler, AggregatePeriod.WEEK));
                commands.add(new AggregateDataUpdatePublicApi(handler, AggregatePeriod.MONTH));
                commands.add(new AggregateDataUpdatePublicApi(handler, AggregatePeriod.YEAR));
            }

            for (SolarEdgeCommand command : commands) {
                handler.getWebInterface().enqueueCommand(command);
            }
        }
    }
}
