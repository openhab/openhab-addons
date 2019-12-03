/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.kvv.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * KVVStationHandler represents a station and holds information about the trains
 * which will arrive soon.
 *
 * @author Maximilian Hess - Initial contribution
 */
@NonNullByDefault
public class KVVStationHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(KVVStationHandler.class);

    private final KVVStationConfig config;

    public KVVStationHandler(final Thing thing) {
        super(thing);
        logger.info("stationhandler!");
        this.config = getConfigAs(KVVStationConfig.class);
    }

    @Override
    public void initialize() {
        scheduler.execute(() -> {

            // creating channels
            logger.info("Creating channels...");
            final List<Channel> channels = new ArrayList<Channel>();
            for (int i = 0; i < this.config.maxTrains; i++) {
                channels.add(ChannelBuilder.create(
                    new ChannelUID(this.thing.getUID(), "train" + i + "-name"), "String").build());
                channels.add(ChannelBuilder.create(
                    new ChannelUID(this.thing.getUID(), "train" + i + "-destination"), "String").build());
                channels.add(ChannelBuilder.create(
                    new ChannelUID(this.thing.getUID(), "train" + i + "-eta"), "String").build());
            }
            this.updateThing(this.editThing().withChannels(channels).build());

            logger.info("Starting inital fetch");
            final Bridge bridge = getBridge();
            if (bridge == null) {
                logger.warn("Failed to get bridge (is null)");
                updateStatus(ThingStatus.OFFLINE);
                return;
            }

            final KVVBridgeHandler handler = (KVVBridgeHandler)  bridge.getHandler();
            if (handler == null) {
                logger.warn("Failed to get bridge handler (is null)");
                updateStatus(ThingStatus.OFFLINE);
                return;
            }

            final DepartureResult departures = handler.queryKVV(this.config);
            if (departures == null) {
                logger.warn("Failed to get departures for '{}'", this.thing.getUID().getAsString());
                updateStatus(ThingStatus.OFFLINE);
                return;
            }

            logger.info("Listing channels...");
            for (final Channel c : this.getThing().getChannels()) {
                logger.info("{}", c.getUID().getAsString());
            }
            
            this.setDepartures(departures);
            this.scheduler.scheduleWithFixedDelay(new UpdateTask(), 0, this.config.updateInterval,
                        TimeUnit.MILLISECONDS);
            updateStatus(ThingStatus.ONLINE);
            logger.info("Thing is online");
        });
    }

    /**
     * Updates the local list of departures.
     *
     * @param departures the new list of departures
     */
    private synchronized void setDepartures(final DepartureResult departures) {
        for (int i = 0; i < this.config.maxTrains; i++) {
            this.updateState(new ChannelUID(this.thing.getUID(), "train" + i + "-name"),
                    new StringType(departures.getDepartures().get(i).getRoute()));
            this.updateState(new ChannelUID(this.thing.getUID(), "train" + i + "-destination"),
                    new StringType(departures.getDepartures().get(i).getDestination()));
            this.updateState(new ChannelUID(this.thing.getUID(), "train" + i + "-eta"),
                    new StringType(departures.getDepartures().get(i).getTime()));
        }
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
    }

    /**
     * Holds a single {@link TimerTask} to fetch the latest departure data.
     *
     * @author Maximilian Hess - Initial contribution
     *
     */
    @NonNullByDefault
    public class UpdateTask extends TimerTask {

        @Override
        public void run() {
            final Bridge bridge = getBridge();
            if (bridge == null) {
                logger.warn("Failed to get bridge (is null)");
                return;
            }

            final KVVBridgeHandler handler = (KVVBridgeHandler)  bridge.getHandler();
            if (handler == null) {
                logger.warn("Failed to get bridge handler (is null)");
                return;
            }

            final DepartureResult departures = handler.queryKVV(config);
            if (departures != null) {
                setDepartures(departures);
            }
        }

    }
    
}
