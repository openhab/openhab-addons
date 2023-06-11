/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.pixometer.handler;

import static org.openhab.binding.pixometer.internal.PixometerBindingConstants.*;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Energy;
import javax.measure.quantity.Volume;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pixometer.internal.config.PixometerMeterConfiguration;
import org.openhab.binding.pixometer.internal.config.ReadingInstance;
import org.openhab.binding.pixometer.internal.data.MeterState;
import org.openhab.binding.pixometer.internal.serializer.CustomReadingInstanceDeserializer;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link MeterHandler} is responsible for handling data and measurements of a meter thing
 *
 * @author Jerome Luckenbach - Initial contribution
 */
@NonNullByDefault
public class MeterHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(MeterHandler.class);

    private static final String API_VERSION = "v1";
    private static final String API_METER_ENDPOINT = "meters";
    private static final String API_READINGS_ENDPOINT = "readings";

    private final GsonBuilder gsonBuilder = new GsonBuilder().registerTypeAdapter(ReadingInstance.class,
            new CustomReadingInstanceDeserializer());
    private final Gson gson = gsonBuilder.create();

    private @NonNullByDefault({}) String resourceID;
    private @NonNullByDefault({}) String meterID;
    private @NonNullByDefault({}) ExpiringCache<@Nullable MeterState> cache;

    private @Nullable ScheduledFuture<?> pollingJob;

    public MeterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (command instanceof RefreshType) {
                updateMeter(channelUID, cache.getValue());
            } else {
                logger.debug("The pixometer binding is read-only and can not handle command {}", command);
            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Pixometer handler '{}'", getThing().getUID());
        updateStatus(ThingStatus.UNKNOWN);

        PixometerMeterConfiguration config = getConfigAs(PixometerMeterConfiguration.class);
        setRessourceID(config.resourceId);

        cache = new ExpiringCache<>(Duration.ofMinutes(60), this::refreshCache);

        Bridge b = this.getBridge();
        if (b == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Could not find bridge (pixometer config). Did you choose one?");
            return;
        }

        obtainMeterId();

        // Start polling job with the interval, that has been set up in the bridge
        int pollingPeriod = Integer.parseInt(b.getConfiguration().get(CONFIG_BRIDGE_REFRESH).toString());
        pollingJob = scheduler.scheduleWithFixedDelay(() -> {
            logger.debug("Try to refresh meter data");
            try {
                updateMeter(cache.getValue());
            } catch (RuntimeException r) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        }, 2, pollingPeriod, TimeUnit.MINUTES);
        logger.debug("Refresh job scheduled to run every {} minutes for '{}'", pollingPeriod, getThing().getUID());
    }

    /**
     * @return returns the auth token or null for error handling if the bridge was not found.
     */
    private @Nullable String getTokenFromBridge() {
        Bridge b = this.getBridge();
        if (b == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Could not find bridge (pixometer config). Did you choose one?");
            return null;
        }

        return new StringBuilder("Bearer ").append(((AccountHandler) b.getHandler()).getAuthToken()).toString();
    }

    @Override
    public void dispose() {
        if (pollingJob != null) {
            pollingJob.cancel(true);
        }
        super.dispose();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("Bridge Status updated to {} for device: {}", bridgeStatusInfo.getStatus(), getThing().getUID());
        if (bridgeStatusInfo.getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, bridgeStatusInfo.getDescription());
        }
    }

    /**
     * Requests the corresponding meter data and stores the meterId internally for later usage
     *
     * @param token The current active auth token
     */
    private void obtainMeterId() {
        try {
            String token = getTokenFromBridge();

            if (token == null) {
                throw new IOException(
                        "Auth token has not been delivered.\n API request can't get executed without authentication.");
            }

            String url = getApiString(API_METER_ENDPOINT);

            Properties urlHeader = new Properties();
            urlHeader.put("CONTENT-TYPE", "application/json");
            urlHeader.put("Authorization", token);

            String urlResponse = HttpUtil.executeUrl("GET", url, urlHeader, null, null, 2000);
            JsonObject responseJson = (JsonObject) JsonParser.parseString(urlResponse);

            if (responseJson.has("meter_id")) {
                setMeterID(responseJson.get("meter_id").toString());
                updateStatus(ThingStatus.ONLINE);
                return;
            }

            String errorMsg = String.format("Invalid Api Response ( %s )", responseJson);

            throw new IOException(errorMsg);
        } catch (IOException e) {
            String errorMsg = String.format("Could not initialize Thing ( %s ). %s", this.getThing().getUID(),
                    e.getMessage());

            logger.debug(errorMsg, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorMsg);
        }
    }

    /**
     * Checks if a channel is linked and redirects to the updateMeter method if link is existing
     *
     * @param channelUID the channel requested for refresh
     * @param meterState a meterState instance with current values
     */
    private void updateMeter(ChannelUID channelUID, @Nullable MeterState meterState) throws IOException {
        if (!isLinked(channelUID)) {
            throw new IOException("Channel is not linked.");
        }
        updateMeter(meterState);
    }

    /**
     * updates all corresponding channels
     *
     * @param token The current active access token
     */
    private void updateMeter(@Nullable MeterState meterState) {
        try {
            if (meterState == null) {
                throw new IOException("Meter state has not been delivered to update method. Can't update channels.");
            }

            ThingTypeUID thingtype = getThing().getThingTypeUID();

            if (THING_TYPE_ENERGYMETER.equals(thingtype)) {
                QuantityType<Energy> state = new QuantityType<>(meterState.getReadingValue(), Units.KILOWATT_HOUR);
                updateState(CHANNEL_LAST_READING_VALUE, state);
            }

            if (thingtype.equals(THING_TYPE_GASMETER) || thingtype.equals(THING_TYPE_WATERMETER)) {
                QuantityType<Volume> state = new QuantityType<>(meterState.getReadingValue(), SIUnits.CUBIC_METRE);
                updateState(CHANNEL_LAST_READING_VALUE, state);
            }

            updateState(CHANNEL_LAST_READING_DATE, meterState.getLastReadingDate());
            updateState(CHANNEL_LAST_REFRESH_DATE, meterState.getLastRefreshTime());
        } catch (IOException e) {
            logger.debug("Exception while updating Meter {}: {}", getThing().getUID(), e.getMessage(), e);
        }
    }

    private @Nullable MeterState refreshCache() {
        try {
            String url = getApiString(API_READINGS_ENDPOINT);

            Properties urlHeader = new Properties();
            urlHeader.put("CONTENT-TYPE", "application/json");
            urlHeader.put("Authorization", getTokenFromBridge());

            String urlResponse = HttpUtil.executeUrl("GET", url, urlHeader, null, null, 2000);

            ReadingInstance latestReading = gson.fromJson(JsonParser.parseString(urlResponse), ReadingInstance.class);

            return new MeterState(Objects.requireNonNull(latestReading));
        } catch (IOException e) {
            logger.debug("Exception while refreshing cache for Meter {}: {}", getThing().getUID(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * Generates a url string based on the given api endpoint
     *
     * @param endpoint The choosen api endpoint
     * @return The generated url string
     */
    private String getApiString(String endpoint) {
        StringBuilder sb = new StringBuilder(API_BASE_URL);
        sb.append(API_VERSION).append("/");

        switch (endpoint) {
            case API_METER_ENDPOINT:
                sb.append(API_METER_ENDPOINT).append("/");
                sb.append(this.getRessourceID()).append("/?");
                break;
            case API_READINGS_ENDPOINT:
                sb.append(API_READINGS_ENDPOINT).append("/");
                sb.append("?meter_ressource_id=").append(this.getRessourceID());
                sb.append("&o=-reading_date").append("&");
                break;
        }

        sb.append("format=json");
        return sb.toString();
    }

    /**
     * Getters and Setters
     */

    public String getRessourceID() {
        return resourceID;
    }

    private void setRessourceID(String ressourceID) {
        this.resourceID = ressourceID;
    }

    public String getMeterID() {
        return meterID;
    }

    private void setMeterID(String meterID) {
        this.meterID = meterID;
    }
}
