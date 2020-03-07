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
package org.openhab.binding.goecharger.internal.handler;

import static org.openhab.binding.goecharger.internal.GoEChargerBindingConstants.MAX_AMPERE;
import static org.openhab.binding.goecharger.internal.GoEChargerBindingConstants.PWM_SIGNAL;
import static org.openhab.binding.goecharger.internal.GoEChargerBindingConstants.ERROR;
import static org.openhab.binding.goecharger.internal.GoEChargerBindingConstants.ALLOW_CHARGING;
import static org.openhab.binding.goecharger.internal.GoEChargerBindingConstants.CABLE_ENCODING;
import static org.openhab.binding.goecharger.internal.GoEChargerBindingConstants.PHASES;
import static org.openhab.binding.goecharger.internal.GoEChargerBindingConstants.TEMPERATURE;
import static org.openhab.binding.goecharger.internal.GoEChargerBindingConstants.SESSION_CHARGE_CONSUMPTION;
import static org.openhab.binding.goecharger.internal.GoEChargerBindingConstants.SESSION_CHARGE_CONSUMPTION_LIMIT;
import static org.openhab.binding.goecharger.internal.GoEChargerBindingConstants.TOTAL_CONSUMPTION;
import static org.openhab.binding.goecharger.internal.GoEChargerBindingConstants.FIRMWARE;
import static org.openhab.binding.goecharger.internal.GoEChargerBindingConstants.VOLTAGE_L1;
import static org.openhab.binding.goecharger.internal.GoEChargerBindingConstants.VOLTAGE_L2;
import static org.openhab.binding.goecharger.internal.GoEChargerBindingConstants.VOLTAGE_L3;
import static org.openhab.binding.goecharger.internal.GoEChargerBindingConstants.CURRENT_L1;
import static org.openhab.binding.goecharger.internal.GoEChargerBindingConstants.CURRENT_L2;
import static org.openhab.binding.goecharger.internal.GoEChargerBindingConstants.CURRENT_L3;
import static org.openhab.binding.goecharger.internal.GoEChargerBindingConstants.POWER_L1;
import static org.openhab.binding.goecharger.internal.GoEChargerBindingConstants.POWER_L2;
import static org.openhab.binding.goecharger.internal.GoEChargerBindingConstants.POWER_L3;
import static org.openhab.binding.goecharger.internal.GoEChargerBindingConstants.ACCESS_STATE;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.goecharger.internal.GoEChargerBindingConstants;
import org.openhab.binding.goecharger.internal.GoEChargerConfiguration;
import org.openhab.binding.goecharger.internal.api.GoEStatusResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;

/**
 * The {@link GoEChargerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Samuel Brucksch - Initial contribution
 */
@NonNullByDefault
public class GoEChargerHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(GoEChargerHandler.class);

    private @Nullable GoEChargerConfiguration config;

    private final Gson gson;

    private @Nullable ScheduledFuture<?> refreshJob;
    private int retryCounter = 0;

    private HttpClient httpClient;

    public GoEChargerHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        gson = new Gson();
        this.httpClient = httpClient;
    }

    public State getValue(String channelId, GoEStatusResponseDTO goeResponse) {
        if (goeResponse != null) {
            switch (channelId) {
                case MAX_AMPERE:
                    return new QuantityType<>(goeResponse.maxChargeAmps, SmartHomeUnits.AMPERE);
                case PWM_SIGNAL:
                    // TODO more readable string value?
                    return new DecimalType(goeResponse.pwmSignal);
                case ERROR:
                    String error = null;
                    switch (goeResponse.errorCode) {
                        case 0:
                            error = "NONE";
                            break;
                        case 1:
                            error = "RCCB";
                            break;
                        case 3:
                            error = "PHASE";
                            break;
                        case 8:
                            error = "NO_GROUND";
                            break;
                        default:
                            error = "INTERNAL";
                            break;
                    }
                    return new StringType(error);
                case ACCESS_STATE:
                    String accessState = null;
                    switch (goeResponse.accessState) {
                        case 0:
                            accessState = "OPEN";
                            break;
                        case 1:
                            accessState = "RFID";
                            break;
                        case 2:
                            accessState = "AWATTAR";
                            break;
                        case 3:
                            accessState = "TIMER";
                            break;
                        default:
                            accessState = "UNKNOWN";
                            break;
                    }
                    return new StringType(accessState);
                case ALLOW_CHARGING:
                    return goeResponse.allowCharging == 1 ? OnOffType.ON : OnOffType.OFF;
                case CABLE_ENCODING:
                    return new QuantityType<>(goeResponse.cableEncoding, SmartHomeUnits.AMPERE);
                case PHASES:
                    int count = 0;
                    if (goeResponse.energy[4] > 0) { // amps P1
                        count++;
                    }
                    if (goeResponse.energy[5] > 0) { // amps P2
                        count++;
                    }
                    if (goeResponse.energy[6] > 0) { // amps P3
                        count++;
                    }
                    return new DecimalType(count);
                case TEMPERATURE:
                    return new QuantityType<>(goeResponse.temperature, SIUnits.CELSIUS);
                case SESSION_CHARGE_CONSUMPTION:
                    return new QuantityType<>((Double) (goeResponse.sessionChargeConsumption / 360000d),
                            SmartHomeUnits.KILOWATT_HOUR);
                case SESSION_CHARGE_CONSUMPTION_LIMIT:
                    return new QuantityType<>((Double) (goeResponse.sessionChargeConsumptionLimit / 10d),
                            SmartHomeUnits.KILOWATT_HOUR);
                case TOTAL_CONSUMPTION:
                    return new QuantityType<>((Double) (goeResponse.totalChargeConsumption / 10d),
                            SmartHomeUnits.KILOWATT_HOUR);
                case FIRMWARE:
                    return new StringType(goeResponse.firmware);
                case VOLTAGE_L1:
                    return new QuantityType<>(goeResponse.energy[0], SmartHomeUnits.VOLT);
                case VOLTAGE_L2:
                    return new QuantityType<>(goeResponse.energy[1], SmartHomeUnits.VOLT);
                case VOLTAGE_L3:
                    return new QuantityType<>(goeResponse.energy[2], SmartHomeUnits.VOLT);
                case CURRENT_L1:
                    return new QuantityType<>((Double) (goeResponse.energy[4] / 10d), SmartHomeUnits.AMPERE);
                case CURRENT_L2:
                    return new QuantityType<>((Double) (goeResponse.energy[5] / 10d), SmartHomeUnits.AMPERE);
                case CURRENT_L3:
                    return new QuantityType<>((Double) (goeResponse.energy[6] / 10d), SmartHomeUnits.AMPERE);
                case POWER_L1:
                    return new QuantityType<>((Double) (goeResponse.energy[7] / 10d),
                            SmartHomeUnits.WATT);
                case POWER_L2:
                    return new QuantityType<>((Double) (goeResponse.energy[8] / 10d),
                            SmartHomeUnits.WATT);
                case POWER_L3:
                    return new QuantityType<>((Double) (goeResponse.energy[9] / 10d),
                            SmartHomeUnits.WATT);
            }
        }
        return UnDefType.UNDEF;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            refresh();
            return;
        }

        String key = null;
        String value = null;
        switch (channelUID.getId()) {
            case MAX_AMPERE:
                key = "amp";
                value = ((QuantityType<?>)command).intValue() + "";
                break;
            case SESSION_CHARGE_CONSUMPTION_LIMIT:
                key = "dwo";
                value = ((QuantityType<?>)command).intValue()*10  + "";
                break;
            case ALLOW_CHARGING:
                key = "alw";
                value = command == OnOffType.ON ? "1" : "0";
                break;
        }
        if (key != null) {
            sendData(key, value);
        } else {
            logger.warn("Could not update channel {} because it is read only", channelUID.getId());
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(GoEChargerConfiguration.class);

        updateStatus(ThingStatus.UNKNOWN);

        startAutomaticRefresh();
        logger.debug("Finished initializing!");
    }

    private String getUrl(String type) {
        String urlStr = type;
        return urlStr.replace("%IP%", StringUtils.trimToEmpty(config.ip));
    }

    private void sendData(String key, String value) {
        String urlStr = getUrl(GoEChargerBindingConstants.MQTT_URL).replace("%KEY%", key).replace("%VALUE%", value);
        logger.debug("POST URL = {}", urlStr);

        String result = null;
        try {
            ContentResponse contentResponse = httpClient.newRequest(urlStr).method(HttpMethod.POST)
                    .timeout(5, TimeUnit.SECONDS).send();

            result = contentResponse.getContentAsString();
            // TODO check if value really changed
            logger.debug("Response: {}", result);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "No response received on command");
            return;
        }

        if (result != null) {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
            return;
        }
    }

    /**
     * Request new data from Go-E charger
     *
     * @return the Go-E charger object mapping the JSON response or null in case of
     *         error
     */
    @Nullable
    private GoEStatusResponseDTO getGoEData() {
        GoEStatusResponseDTO result = null;

        String urlStr = getUrl(GoEChargerBindingConstants.API_URL);
        logger.debug("GET URL = {}", urlStr);

        try {
            ContentResponse contentResponse = httpClient.newRequest(urlStr).method(HttpMethod.GET)
                    .timeout(5, TimeUnit.SECONDS).send();

            result = gson.fromJson(contentResponse.getContentAsString(), GoEStatusResponseDTO.class);
            // logger.debug("Response: {}", contentResponse.getContentAsString());
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            retryCounter++;
            if (retryCounter == 1) {
                logger.warn("Error in getting data from Go-E charger, retrying once");
                return getGoEData();
            }
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "No response received");
            result = null;
        }

        if (result != null) {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
            return result;
        }

        return null;
    }

    private void refresh() {
        // Request new GoE data
        retryCounter = 0;
        GoEStatusResponseDTO goeResponse = getGoEData();

        // Update all channels from the updated GoE data
        getThing().getChannels().forEach(channel -> updateState(channel.getUID().getId(), getValue(channel.getUID().getId(), goeResponse)));
    }

    private void startAutomaticRefresh() {
        if (refreshJob == null || refreshJob.isCancelled()) {
            GoEChargerConfiguration config = getConfigAs(GoEChargerConfiguration.class);
            int delay = config.refreshInterval.intValue();
            logger.debug("Running refresh job with delay {} s", delay);
            refreshJob = scheduler.scheduleWithFixedDelay(this::refresh, 0, delay, TimeUnit.SECONDS);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the Go-E Charger handler.");

        final ScheduledFuture<?> refreshJob = this.refreshJob;
        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            this.refreshJob = null;
        }
    }
}
