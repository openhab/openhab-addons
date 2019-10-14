package org.openhab.binding.kvv.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KVVStationHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(KVVStationHandler.class);

    private final KVVStationConfig config;

    /** the most recent set of departures */
    @Nullable
    private DepartureResult departures;

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
            final DepartureResult departures = ((KVVBridgeHandler) this.getBridge()).queryKVV(this.config);
            if (departures == null) {
                logger.warn("Failed to get departures for '" + this.thing.getUID().getAsString() + "'");
                updateStatus(ThingStatus.OFFLINE);
                return;
            }

            logger.info("Listing channels...");
            for (final Channel c : this.getThing().getChannels()) {
                logger.info(c.getUID().getAsString());
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
        if (departures == null) {
            return;
        }

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
    public class UpdateTask extends TimerTask {

        @Override
        public void run() {
            final DepartureResult departures = ((KVVBridgeHandler) getBridge()).queryKVV(config);
            if (departures != null) {
                setDepartures(departures);
            }
        }

    }
    
}