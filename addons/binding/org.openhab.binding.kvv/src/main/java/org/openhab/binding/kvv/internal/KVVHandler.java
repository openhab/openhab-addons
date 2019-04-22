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
import java.util.Collection;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.type.ChannelDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
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
    private final KVVTrainChannelTypeProvider channelGroupProvider;

    /** the most up to date status */
    @Nullable
    private DepartureResult departures;

    /**
     * Creates a new {@link KVVHandler}
     *
     * @param thing the {@link Thing} which is referred to.
     */
    public KVVHandler(Thing thing) {
        super(thing);
        this.config = getConfigAs(KVVConfiguration.class);
        this.departures = null;
        this.channelGroupProvider = new KVVTrainChannelTypeProvider();

        for (int i = 0; i < this.config.count; i++) {
            this.channelGroupProvider.addChannelGroupType(i, config.stationId.replaceAll(":", ""));
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

    }

    /**
     * Refreshes all {@link Channel Channels}.
     */
    public void refreshAll() {
        final Collection<ChannelGroupType> channelGroups = this.channelGroupProvider.getChannelGroupTypes(null);
        if (channelGroups == null) {
            return;
        }
        for (final ChannelGroupType group : channelGroups) {
            for (final ChannelDefinition channel : group.getChannelDefinitions()) {
                this.handleRefresh(group, channel);
            }
        }
    }

    /**
     * Refreshes a specific {@link Channel}
     *
     * @param channelUID the {@link ChannelUID} of the {@link Channel}
     */
    @SuppressWarnings("null")
    private void handleRefresh(final ChannelGroupType group, final ChannelDefinition channel) {
        final ChannelUID uid = new ChannelUID(group.getUID().getAsString() + ":train:" + channel.getId());

        if (this.departures == null) {
            logger.warn("failed to update state of '" + uid.getAsString() + "': no departures available");
            updateState(uid, new StringType(""));
            return;
        }

        final String[] tokens = group.getUID().getAsString().split("-");
        final int departureId = Integer.parseInt(tokens[tokens.length - 1].replaceAll("[^0-9]", ""));
        if (this.departures.getDepartures().size() < departureId) {
            logger.warn(
                    "failed to update state of '" + uid.getAsString() + "': train with this id does not exist. only '"
                            + this.departures.getDepartures().size() + "' available");
            updateState(uid, new StringType(""));
            return;
        }

        final Departure departure = this.departures.getDepartures().get(departureId);
        if (departure == null) {
            updateState(uid, new StringType(""));
            return;
        }
        logger.info("Refreshing " + uid + ": " + channel.getId());
        switch (channel.getId()) {
            case "name":
                updateState(uid, new StringType(departure.getRoute()));
                break;
            case "destination":
                updateState(uid, new StringType(departure.getDestination()));
                break;
            case "eta":
                updateState(uid, new StringType(departure.getTime()));
                break;
            default:
                updateState(uid, new StringType(""));
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
        scheduler.execute(() -> {
            logger.info("Starting inital fetch");
            final UpdateTask updateThread = new UpdateTask();
            final DepartureResult departures = updateThread.get();
            if (departures != null) {
                this.setDepartures(departures);

                // Schedule update and refresh tasks
                this.scheduler.scheduleWithFixedDelay(new UpdateTask(), 0, this.config.updateInterval,
                        TimeUnit.MILLISECONDS);
                this.scheduler.scheduleWithFixedDelay(new RefreshTask(), 1000, this.config.updateInterval,
                        TimeUnit.MILLISECONDS);
                updateStatus(ThingStatus.ONLINE);
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
                + KVVBindingConstants.API_KEY + "&maxInfos=" + config.count;

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
                logger.error("Failed to connect to '" + this.url + "'", e);
                return null;
            }
        }

        @Override
        public void run() {
            setDepartures(this.get());
        }

    }
}
