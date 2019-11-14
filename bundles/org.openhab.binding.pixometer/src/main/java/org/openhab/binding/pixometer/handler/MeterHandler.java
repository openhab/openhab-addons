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
package org.openhab.binding.pixometer.handler;

import static org.openhab.binding.pixometer.internal.PixometerBindingConstants.*;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Energy;
import javax.measure.quantity.Volume;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.pixometer.internal.PixometerMeterConfiguration;
import org.openhab.binding.pixometer.internal.config.ReadingInstance;
import org.openhab.binding.pixometer.internal.serializer.CustomReadingInstanceDeserializer;
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
public class MeterHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(MeterHandler.class);

    private static final String API_VERSION = "v1";
    private static final String API_METER_ENDPOINT = "meters";
    private static final String API_READINGS_ENDPOINT = "readings";

    private final GsonBuilder gsonBuilder = new GsonBuilder().registerTypeAdapter(ReadingInstance.class,
            new CustomReadingInstanceDeserializer());
    private final Gson gson = gsonBuilder.create();

    private String resourceID;
    private String meterID;

    private ScheduledFuture<?> pollingJob;

    public MeterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // No handling needed, since we have only values to read.
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Pixometer handler '{}'", getThing().getUID());
        updateStatus(ThingStatus.UNKNOWN);

        try {
            PixometerMeterConfiguration config = getConfigAs(PixometerMeterConfiguration.class);
            setRessourceID(config.resourceId);

            Bridge b = this.getBridge();
            if (b == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                        "Could not find bridge (pixometer config). Did you choose one?");
                return;
            }

            String token = new StringBuilder("Bearer ")
                    .append(b.getConfiguration().get(CONFIG_BRIDGE_AUTH_TOKEN).toString()).toString();

            obtainMeterId(token);

            // Start polling job with the interval, that has been set up in the bridge
            int pollingPeriod = Integer.parseInt(b.getConfiguration().get(CONFIG_BRIDGE_REFRESH).toString());
            pollingJob = scheduler.scheduleWithFixedDelay(() -> {
                logger.debug("Try to refresh meter data");
                try {
                    updateMeter(token);
                } catch (RuntimeException r) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR);
                }
            }, 2, pollingPeriod, TimeUnit.MINUTES);
            logger.debug("Refresh job scheduled to run every {} hours. for '{}'", pollingPeriod, getThing().getUID());
        } catch (RuntimeException r) {
            logger.debug("Caught exception in ScheduledExecutorService of BridgeHandler. RuntimeException: ", r);
            logger.debug("Could not initialize Thing {}: ", getThing().getUID(), r);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR);
        }
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
    private void obtainMeterId(String token) {
        try {
            String url = getApiString(API_METER_ENDPOINT);

            Properties urlHeader = new Properties();
            urlHeader.put("CONTENT-TYPE", "application/json");
            urlHeader.put("Authorization", token);

            String urlResponse = HttpUtil.executeUrl("GET", url, urlHeader, null, null, 2000);
            JsonObject responseJson = (JsonObject) new JsonParser().parse(urlResponse);

            if (responseJson.has("meter_id")) {
                setMeterID(responseJson.get("meter_id").toString());
                updateStatus(ThingStatus.ONLINE);
                return;
            }

            String errorMsg = String.format("Invalid Api Response ( %s )", responseJson);

            throw new RuntimeException(errorMsg);
        } catch (RuntimeException | IOException e) {
            String errorMsg = String.format("Could not initialize Thing ( %s ). %s", this.getThing().getUID(),
                    e.getMessage());

            logger.debug(errorMsg, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorMsg);
        }
    }

    /**
     * requests a pre-filtered reading list for this specific meter and updates all corresponding channels
     *
     * @param token The current active access token
     */
    private void updateMeter(String token) {
        try {
            String url = getApiString(API_READINGS_ENDPOINT);

            Properties urlHeader = new Properties();
            urlHeader.put("CONTENT-TYPE", "application/json");
            urlHeader.put("Authorization", token);

            String urlResponse = HttpUtil.executeUrl("GET", url, urlHeader, null, null, 2000);

            ReadingInstance latestReading = gson.fromJson(new JsonParser().parse(urlResponse), ReadingInstance.class);

            // UoM - Update Quantity State depending on the thing type.

            ThingTypeUID thingtype = getThing().getThingTypeUID();

            if (THING_TYPE_ENERGYMETER.equals(thingtype)) {
                QuantityType<Energy> state = new QuantityType<>(latestReading.getValue(), SmartHomeUnits.KILOWATT_HOUR);
                updateState(CHANNEL_LAST_READING_VALUE, state);
            }

            if (thingtype.equals(THING_TYPE_GASMETER) || thingtype.equals(THING_TYPE_WATERMETER)) {
                QuantityType<Volume> state = new QuantityType<>(latestReading.getValue(), SIUnits.CUBIC_METRE);
                updateState(CHANNEL_LAST_READING_VALUE, state);
            }

            updateState(CHANNEL_LAST_READING_DATE, new DateTimeType(latestReading.getReadingDate().toString()));
            updateState(CHANNEL_LAST_REFRESH_DATE, new DateTimeType(ZonedDateTime.now().toInstant().toString()));
        } catch (Exception e) {
            logger.debug("Exception while updating Meter {}: ", getThing().getUID(), e);
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
