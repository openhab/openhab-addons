/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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

    @Nullable
    private ScheduledFuture<?> pollingJob;

    public KVVStationHandler(final Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);

        final KVVStationConfig config = getConfigAs(KVVStationConfig.class);
        if (config == null) {
            logger.warn("Failed to get config (is null)");
            updateStatus(ThingStatus.OFFLINE);
            return;
        }

        final Bridge bridge = getBridge();
        if (bridge == null) {
            logger.warn("Failed to get bridge (is null)");
            updateStatus(ThingStatus.OFFLINE);
            return;
        }

        final KVVBridgeHandler handler = (KVVBridgeHandler) bridge.getHandler();
        if (handler == null) {
            logger.warn("Failed to get bridge handler (is null)");
            updateStatus(ThingStatus.OFFLINE);
            return;
        }

        // creating channels
        final List<Channel> channels = new ArrayList<Channel>();
        for (int i = 0; i < config.maxTrains; i++) {
            channels.add(ChannelBuilder.create(new ChannelUID(this.thing.getUID(), "train" + i + "-name"), "String")
                    .build());
            channels.add(ChannelBuilder
                    .create(new ChannelUID(this.thing.getUID(), "train" + i + "-destination"), "String").build());
            channels.add(
                    ChannelBuilder.create(new ChannelUID(this.thing.getUID(), "train" + i + "-eta"), "String").build());
        }
        this.updateThing(this.editThing().withChannels(channels).build());
        this.pollingJob = this.scheduler.scheduleWithFixedDelay(new UpdateTask(handler, config), 0,
                config.updateInterval, TimeUnit.MILLISECONDS);
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        if (this.pollingJob != null) {
            this.pollingJob.cancel(true);
        }
    }

    /**
     * Updates the local list of departures.
     *
     * @param departures the new list of departures
     */
    private synchronized void setDepartures(final DepartureResult departures, final int maxTrains) {
        for (int i = 0; i < maxTrains; i++) {
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

        private final KVVBridgeHandler handler;

        private final KVVStationConfig config;

        public UpdateTask(final KVVBridgeHandler handler, KVVStationConfig config) {
            this.handler = handler;
            this.config = config;
        }

        @Override
        public void run() {
            final DepartureResult departures = this.handler.queryKVV(config);
            if (departures != null) {
                setDepartures(departures, this.config.maxTrains);
            }
        }
    }
}
