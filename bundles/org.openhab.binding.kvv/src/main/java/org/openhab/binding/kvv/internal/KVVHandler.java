/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.kvv.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.kvv.internal.DepartureResult.Departure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link KVVHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Maximilian Hess - Initial contribution
 */
public class KVVHandler extends BaseThingHandler {

    /** the logger object */
    private final Logger logger = LoggerFactory.getLogger(KVVHandler.class);

    /** the config of the binding */
    private KVVConfiguration config;

    /** the channels */
    private final List<ChannelUID> channels;

    /** the most up to date status */
    @Nullable
    private DepartureResult departures;

    /**
     * Creates a new {@link KVVHandler}
     *
     * @param thing the {@link Thing} which is refered to.
     */
    public KVVHandler(Thing thing) {
        super(thing);
        this.channels = new LinkedList<ChannelUID>();

        for (final Channel channel : thing.getChannels()) {
            this.channels.add(channel.getUID());
            logger.info(channel.getUID().getAsString());
        }

        for (final String key : thing.getConfiguration().getProperties().keySet()) {
            logger.info(key + " -> " + thing.getConfiguration().get(key));
        }

        this.config = getConfigAs(KVVConfiguration.class);
        this.departures = null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        this.handleRefresh(channelUID);
    }

    /**
     * Refreshes all {@link Channel Channels}
     */
    public void refreshAll() {
        for (final ChannelUID channel : this.channels) {
            this.handleRefresh(channel);
        }
    }

    /**
     * Refreshes a specific {@link Channel}
     *
     * @param channelUID the {@link ChannelUID} of the {@link Channel}
     */
    @SuppressWarnings("null")
    private void handleRefresh(ChannelUID channelUID) {
        final int id = Integer.parseInt(channelUID.getId().replaceAll("[^0-9]", ""));
        final String type = channelUID.getId().substring(channelUID.getId().lastIndexOf('_') + 1);

        logger.debug("Refresh for '" + channelUID.getId() + "'. Id is #" + id + ", type is '" + type + "'");

        if (this.departures == null) {
            logger.info("departure is null...");
            updateState(channelUID, new StringType(""));
            return;
        }

        if (this.departures.getDepartures().size() - 1 < id) {
            logger.info("Train with #" + id + " does not exist.");
            updateState(channelUID, new StringType(""));
            return;
        }

        final Departure departure = this.departures.getDepartures().get(id);
        if (departure == null) {
            updateState(channelUID, new StringType(""));
            return;
        }

        switch (type) {
            case "name":
                updateState(channelUID, new StringType(departure.getRoute()));
                break;
            case "destination":
                updateState(channelUID, new StringType(departure.getDestination()));
                break;
            case "eta":
                updateState(channelUID, new StringType(departure.getTime()));
                break;
            default:
                updateState(channelUID, new StringType(""));
        }
    }

    /**
     * Updates the local list of departures.
     *
     * @param departures the new list of departures
     */
    private synchronized void setDepartures(final DepartureResult departures) {
        this.departures = departures;

    }

    @Override
    public void initialize() {
        logger.error("Start initializing!");
        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            logger.info("Starting inital fetch");
            final UpdateTask updateThread = new UpdateTask();
            final DepartureResult departures = updateThread.get();
            if (departures != null) {
                this.setDepartures(departures);
                updateStatus(ThingStatus.ONLINE);

                // Schedule update and refresh tasks
                new Timer().schedule(new UpdateTask(), 0, this.config.updateInterval);
                new Timer().schedule(new RefreshTask(), 0, this.config.updateInterval);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        });

        logger.info("Finished initializing!");
    }

    /**
     * Holds a single {@link TimerTask} to refresh all of the {@link Channel Channels} in the background.
     *
     * @author <a href="mailto:mail@ne0h.de">Maximilian Hess</a>
     *
     */
    public class RefreshTask extends TimerTask {
        @Override
        public void run() {
            refreshAll();
        }
    }

    /**
     * Holds a single {@link TimerTask} to fetch the latest departure data.
     *
     * @author Maximilian Hess - Initial contribution
     *
     */
    public class UpdateTask extends TimerTask {

        /** the url of the KVV API */
        private final String url = KVVBindingConstants.API_URL + "/departures/bystop/" + config.stationId + "?key="
                + KVVBindingConstants.API_KEY;

        /**
         * Returns the latest {@link DepartureResult}.
         *
         * @return the latest {@link DepartureResult}.
         */
        public @Nullable DepartureResult get() {
            try {
                final HttpURLConnection conn = (HttpURLConnection) new URL(this.url.toString()).openConnection();
                conn.setRequestMethod("GET");
                final BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                final StringBuilder json = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    json.append(line);
                }

                return new Gson().fromJson(json.toString(), DepartureResult.class);
            } catch (IOException e) {
                logger.info("Failed to connect to '" + this.url + "'", e);
                return null;
            }
        }

        @Override
        public void run() {
            setDepartures(this.get());
        }

    }
}
