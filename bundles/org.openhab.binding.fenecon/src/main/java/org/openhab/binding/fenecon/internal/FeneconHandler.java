/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.fenecon.internal;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link FeneconHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Philipp Schneider - Initial contribution
 */
@NonNullByDefault
public class FeneconHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(FeneconHandler.class);

    private FeneconConfiguration config = new FeneconConfiguration();
    private @Nullable ScheduledFuture<?> pollingJob;

    private final List<String> channels = List.of("GridMode", "State", "EssSoc", "ConsumptionActivePower",
            "ProductionActivePower", "GridActivePower", "EssDischargePower", "GridBuyActiveEnergy",
            "GridSellActiveEnergy");

    private final HttpClient httpClient;
    private @Nullable Builder baseHttpRequest;

    public FeneconHandler(Thing thing) {
        super(thing);
        httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    @Override
    public void initialize() {
        config = getConfigAs(FeneconConfiguration.class);

        logger.debug("FENECON: initialize REST-API connection to {} with polling interval: {} sec", getBaseUrl(config),
                config.refreshInterval);

        updateStatus(ThingStatus.UNKNOWN);

        baseHttpRequest = createBaseHttpRequest(config);
        pollingJob = scheduler.scheduleWithFixedDelay(this::pollingCode, 0, config.refreshInterval, TimeUnit.SECONDS);
    }

    private static final String getBasicAuthHeader(String username, String password) {
        String valueToEncode = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }

    private String getBaseUrl(FeneconConfiguration config) {
        return "http://" + config.hostname + ":" + config.port + "/";
    }

    private Builder createBaseHttpRequest(FeneconConfiguration config) {
        String basicAuth = getBasicAuthHeader("x", config.password);
        return HttpRequest.newBuilder().timeout(Duration.ofSeconds(5)).header("Authorization", basicAuth)
                .header("Content-Type", "application/json").GET();
    }

    private void pollingCode() {
        for (String eachChannel : channels) {
            try {
                @SuppressWarnings("null")
                HttpRequest request = baseHttpRequest
                        .uri(new URI(getBaseUrl(config) + "rest/channel/_sum/" + eachChannel)).build();
                logger.trace("FENECON - request: {}", request);

                HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
                logger.trace("FENECON - response status code: {} body: {}", response.statusCode(), response.body());

                if (response.statusCode() > 300) {
                    // Authentication error
                    if (response.statusCode() == 401) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                                parseFeneconError(response));
                        return;
                    } else {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                                "http status code: " + response.statusCode());
                        return;
                    }
                } else {
                    processDataPoint(JsonParser.parseString(response.body()).getAsJsonObject());
                    updateStatus(ThingStatus.ONLINE);
                }
            } catch (URISyntaxException | JsonSyntaxException | IOException | InterruptedException err) {
                logger.trace("FENECON - connection problem on FENECON channel {}", eachChannel, err);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, err.getMessage());
                return;
            }
        }

        // Set last successful update cycle
        updateState("lastUpdate", new DateTimeType());
    }

    private void processDataPoint(JsonObject response) {
        // Example: {"address":"_sum/EssSoc","type":"INTEGER","accessMode":"RO","text":"Range
        // 0..100","unit":"%","value":99}
        String address = response.get("address").getAsString();
        String text = response.get("text").getAsString();
        String value = response.get("value").getAsString();

        switch (address) {
            case "_sum/State":
                // {"address":"_sum/State","type":"INTEGER","accessMode":"RO","text":"0:Ok, 1:Info, 2:Warning,
                // 3:Fault","unit":"","value":0}
                int begin = text.indexOf(value + ":");
                int end = text.indexOf(",", begin);
                updateState("state", new StringType(text.substring(begin + 2, end)));
                break;
            case "_sum/EssSoc":
                updateState("essSoc", new QuantityType<>(Integer.valueOf(value), Units.PERCENT));
                break;
            case "_sum/ConsumptionActivePower":
                updateState("consumptionActivePower", new QuantityType<>(Integer.valueOf(value), Units.WATT));
                break;
            case "_sum/ProductionActivePower":
                updateState("productionActivePower", new QuantityType<>(Integer.valueOf(value), Units.WATT));
                break;
            case "_sum/GridActivePower":
                // Grid exchange power. Negative values for sell-to-grid; positive for buy-from-grid"
                Integer gridValue = Integer.valueOf(value);
                int selltoGridPower = 0;
                int buyFromGridPower = 0;
                if (gridValue < 0) {
                    selltoGridPower = gridValue * -1;
                } else {
                    buyFromGridPower = gridValue;
                }
                updateState("sellToGridPower", new QuantityType<>(selltoGridPower, Units.WATT));
                updateState("buyFromGridPower", new QuantityType<>(buyFromGridPower, Units.WATT));
                break;
            case "_sum/EssDischargePower":
                // Actual AC-side battery discharge power of Energy Storage System.
                // Negative values for charge; positive for discharge
                Integer powerValue = Integer.valueOf(value);
                int chargerPower = 0;
                int dischargerPower = 0;
                if (powerValue < 0) {
                    chargerPower = powerValue * -1;
                } else {
                    dischargerPower = powerValue;
                }
                updateState("chargerPower", new QuantityType<>(chargerPower, Units.WATT));
                updateState("dischargerPower", new QuantityType<>(dischargerPower, Units.WATT));
                break;
            case "_sum/GridMode":
                // text":"1:On-Grid, 2:Off-Grid","unit":"","value":1
                Integer gridMod = Integer.valueOf(value);
                updateState("emergencyPowerMode", gridMod == 2 ? OnOffType.ON : OnOffType.OFF);
                break;
            case "_sum/GridSellActiveEnergy":
                // {"address":"_sum/GridSellActiveEnergy","type":"LONG","accessMode":"RO","text":"","unit":"Wh_Σ","value":374242}
                updateState("sellToGridEnergy", new QuantityType<>(Integer.valueOf(value), Units.WATT_HOUR));
                break;
            case "_sum/GridBuyActiveEnergy":
                // "address":"_sum/GridBuyActiveEnergy","type":"LONG","accessMode":"RO","text":"","unit":"Wh_Σ","value":1105}
                updateState("buyFromGridEnergy", new QuantityType<>(Integer.valueOf(value), Units.WATT_HOUR));
                break;
        }
    }

    private String parseFeneconError(HttpResponse<String> response) {
        // Example error Response
        // {"jsonrpc":"2.0","id":"00000000-0000-0000-0000-000000000000","error":{"code":1003,"message":"Authentication
        // failed","data":[]}}
        JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
        return jsonObject.getAsJsonObject("error").get("message").getAsString();
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> job = pollingJob;
        if (job != null) {
            job.cancel(true);
            pollingJob = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            // Noop
        }
    }
}
