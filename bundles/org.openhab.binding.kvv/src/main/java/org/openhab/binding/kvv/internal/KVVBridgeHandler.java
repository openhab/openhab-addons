package org.openhab.binding.kvv.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.Gson;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
public class KVVBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(KVVBridgeHandler.class);

    public KVVBridgeHandler(final Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        scheduler.execute(() -> {
            updateStatus(ThingStatus.ONLINE);
        });
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

    }

    /**
     * Returns the latest {@link DepartureResult}.
     *
     * @return the latest {@link DepartureResult}.
     */
    public @Nullable DepartureResult queryKVV(final KVVStationConfig config) {
        final String url = KVVBindingConstants.API_URL + "/departures/bystop/" + config.stationId + "?key="
                            + KVVBindingConstants.API_KEY + "&maxInfos=" + config.maxTrains;
        try {
            final HttpURLConnection conn = (HttpURLConnection) new URL(url.toString()).openConnection();
            conn.setRequestMethod("GET");
            final BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            final StringBuilder json = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                json.append(line);
            }

            return new Gson().fromJson(json.toString(), DepartureResult.class);
        } catch (IOException e) {
            logger.error("Failed to connect to '" + url + "'", e);
            return null;
        }
    }

}
