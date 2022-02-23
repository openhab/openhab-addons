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
package org.openhab.binding.goechargerapiv2.internal.handler;

import static org.openhab.binding.goechargerapiv2.internal.GoEChargerAPIv2BindingConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import javax.measure.quantity.ElectricCurrent;
import javax.measure.quantity.Energy;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.goechargerapiv2.internal.GoEChargerAPIv2BindingConstants;
import org.openhab.binding.goechargerapiv2.internal.GoEChargerAPIv2Configuration;
import org.openhab.binding.goechargerapiv2.internal.api.GoEChargerAPIv2StatusResponseDTO;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link GoEChargerAPIv2Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Reinhard Plaim - Initial contribution
 */
@NonNullByDefault
public class GoEChargerAPIv2Handler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(GoEChargerAPIv2Handler.class);

    private @Nullable GoEChargerAPIv2Configuration config;

    private List<String> allChannels = new ArrayList<>();

    private final Gson gson = new Gson();

    private @Nullable ScheduledFuture<?> refreshJob;

    private final HttpClient httpClient;

    private String filter = "";

    public GoEChargerAPIv2Handler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    private State getValue(String channelId, GoEChargerAPIv2StatusResponseDTO goeResponse) {
        switch (channelId) {
            case MAX_CURRENT:
                if (goeResponse.maxCurrent == null) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>(goeResponse.maxCurrent, Units.AMPERE);
            case CHARGING_PHASES:
                if (goeResponse.chargingPhases == null) {
                    return UnDefType.UNDEF;
                }
                var phases = 1;
                if (goeResponse.chargingPhases == 2) {
                    phases = 3;
                }
                return new DecimalType(phases);
            case PWM_SIGNAL:
                if (goeResponse.pwmSignal == null) {
                    return UnDefType.UNDEF;
                }
                String pwmSignal = null;
                switch (goeResponse.pwmSignal) {
                    case 0:
                        pwmSignal = "UNKNOWN/ERROR";
                    case 1:
                        pwmSignal = "IDLE";
                        break;
                    case 2:
                        pwmSignal = "CHARGING";
                        break;
                    case 3:
                        pwmSignal = "WAITING_FOR_CAR";
                        break;
                    case 4:
                        pwmSignal = "COMPLETE";
                        break;
                    case 5:
                        pwmSignal = "ERROR";
                    default:
                }
                return new StringType(pwmSignal);
            case ERROR:
                if (goeResponse.errorCode == null) {
                    return UnDefType.UNDEF;
                }
                String error = null;
                switch (goeResponse.errorCode) {
                    case 0:
                        error = "UNKNOWN/ERROR";
                    case 1:
                        error = "IDLE";
                        break;
                    case 2:
                        error = "CHARGING";
                        break;
                    case 3:
                        error = "WAITING_FOR_CAR";
                        break;
                    case 4:
                        error = "COMPLETE";
                        break;
                    case 5:
                        error = "ERROR";
                    default:
                }
                return new StringType(error);
            case ALLOW_CHARGING:
                return goeResponse.allowCharging == true ? OnOffType.ON : OnOffType.OFF;
            case CABLE_ENCODING:
                if (goeResponse.cableEncoding == null) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>(goeResponse.cableEncoding, Units.AMPERE);
            case TEMPERATURE1:
                if (goeResponse.temperature == null) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>(goeResponse.temperature[0], SIUnits.CELSIUS);
            case TEMPERATURE2:
                if (goeResponse.temperature == null) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>(goeResponse.temperature[1], SIUnits.CELSIUS);
            case SESSION_CHARGE_CONSUMPTION:
                if (goeResponse.sessionChargeConsumption == null) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>((Double) (goeResponse.sessionChargeConsumption / 1000d), Units.KILOWATT_HOUR);
            case SESSION_CHARGE_CONSUMPTION_LIMIT:
                if (goeResponse.sessionChargeConsumptionLimit == null) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>((Double) (goeResponse.sessionChargeConsumptionLimit / 1000d),
                        Units.KILOWATT_HOUR);
            case TOTAL_CONSUMPTION:
                if (goeResponse.totalChargeConsumption == null) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>((Double) (goeResponse.totalChargeConsumption / 1000d), Units.KILOWATT_HOUR);
            case FIRMWARE:
                if (goeResponse.firmware == null) {
                    return UnDefType.UNDEF;
                }
                return new StringType(goeResponse.firmware);
            case VOLTAGE_L1:
                if (goeResponse.energy == null) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>(goeResponse.energy[0], Units.VOLT);
            case VOLTAGE_L2:
                if (goeResponse.energy == null) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>(goeResponse.energy[1], Units.VOLT);
            case VOLTAGE_L3:
                if (goeResponse.energy == null) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>(goeResponse.energy[2], Units.VOLT);
            case CURRENT_L1:
                if (goeResponse.energy == null) {
                    return UnDefType.UNDEF;
                }
                // values come in as A*10, 41 means 4.1A -> TODO: still for api v2?
                return new QuantityType<>((Double) (goeResponse.energy[4] / 10d), Units.AMPERE);
            case CURRENT_L2:
                if (goeResponse.energy == null) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>((Double) (goeResponse.energy[5] / 10d), Units.AMPERE);
            case CURRENT_L3:
                if (goeResponse.energy == null) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>((Double) (goeResponse.energy[6] / 10d), Units.AMPERE);
            case POWER_L1:
                if (goeResponse.energy == null) {
                    return UnDefType.UNDEF;
                }
                // values come in as kW*10, 41 means 4.1kW -> TODO: still for api v2?
                return new QuantityType<>(goeResponse.energy[7] * 100, Units.WATT);
            case POWER_L2:
                if (goeResponse.energy == null) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>(goeResponse.energy[8] * 100, Units.WATT);
            case POWER_L3:
                if (goeResponse.energy == null) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>(goeResponse.energy[9] * 100, Units.WATT);
        }
        return UnDefType.UNDEF;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            // we can not update single channels and refresh is triggered automatically
            // anyways
            return;
        }

        String key = null;
        String value = null;
        switch (channelUID.getId()) {
            case MAX_CURRENT:
                key = "amp";
                if (command instanceof DecimalType) {
                    value = String.valueOf(((DecimalType) command).intValue());
                } else if (command instanceof QuantityType<?>) {
                    value = String.valueOf(((QuantityType<ElectricCurrent>) command).toUnit(Units.AMPERE).intValue());
                }
                break;
            case SESSION_CHARGE_CONSUMPTION_LIMIT:
                key = "dwo";
                if (command instanceof DecimalType) {
                    value = String.valueOf(((DecimalType) command).intValue() * 1000);
                } else if (command instanceof QuantityType<?>) {
                    value = String
                            .valueOf(((QuantityType<Energy>) command).toUnit(Units.KILOWATT_HOUR).intValue() * 1000);
                }
                break;
            case CHARGING_PHASES:
                key = "psm";
                if (command instanceof DecimalType) {
                    var phases = 1;
                    var help = (DecimalType) command;
                    if (help.intValue() == 3) {
                        phases = 2; // set value 2 for 3 phases
                    }
                    value = String.valueOf(phases);
                }
                break;
            default:
        }
        if (key != null && value != null) {
            sendData(key, value);
        } else {
            logger.warn("Could not update channel {} with key {} and value {}", channelUID.getId(), key, value);
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(GoEChargerAPIv2Configuration.class);
        allChannels = getThing().getChannels().stream().map(channel -> channel.getUID().getId())
                .collect(Collectors.toList());

        logger.info("Number of channels found: {}", allChannels.size());

        filter = "?filter=";
        var declaredFields = GoEChargerAPIv2StatusResponseDTO.class.getDeclaredFields();
        for (var field : declaredFields) {
            filter += field.getAnnotation(SerializedName.class).value() + ",";
        }
        filter = filter.substring(0, filter.length() - 1);

        updateStatus(ThingStatus.UNKNOWN);

        startAutomaticRefresh();
        logger.debug("Finished initializing!");

        // These logging types should be primarily used by bindings
        // logger.trace("Example trace message");
        // logger.debug("Example debug message");
        // logger.warn("Example warn message");
        //
        // Logging to INFO should be avoided normally.
        // See https://www.openhab.org/docs/developer/guidelines.html#f-logging
    }

    private String getUrl(String type) {
        return type.replace("%IP%", StringUtils.trimToEmpty(config.ip));
    }

    private void sendData(String key, String value) {
        String urlStr = getUrl(GoEChargerAPIv2BindingConstants.SET_URL).replace("%KEY%", key).replace("%VALUE%", value);
        logger.debug("sendData GET URL = {}", urlStr);

        try {
            ContentResponse contentResponse = httpClient.newRequest(urlStr).method(HttpMethod.GET)
                    .timeout(5, TimeUnit.SECONDS).send();

            String response = contentResponse.getContentAsString();
            logger.debug("sendData GET Response: {}", response);

            var statusCode = contentResponse.getStatus();
            if (!(statusCode == 200 || statusCode == 204)) {
                logger.error("Could not send data: {}, StatusCode: {}", response, statusCode);
            }
        } catch (InterruptedException | TimeoutException | ExecutionException | JsonSyntaxException e) {
            logger.error("Could not send data: {}, {}", urlStr, e.toString());
        }
    }

    /**
     * Request new data from Go-E charger
     *
     * @return the Go-E charger object mapping the JSON response or null in case of
     *         error
     * @throws ExecutionException
     * @throws TimeoutException
     * @throws InterruptedException
     */
    @Nullable
    private GoEChargerAPIv2StatusResponseDTO getGoEData()
            throws InterruptedException, TimeoutException, ExecutionException, JsonSyntaxException {
        String urlStr = getUrl(GoEChargerAPIv2BindingConstants.API_URL);
        urlStr = urlStr + filter;

        logger.debug("getGoEData GET URL = {}", urlStr);

        ContentResponse contentResponse = httpClient.newRequest(urlStr).method(HttpMethod.GET)
                .timeout(5, TimeUnit.SECONDS).send();

        String response = contentResponse.getContentAsString();
        logger.debug("getGoEData GET Response: {}", response);
        return gson.fromJson(response, GoEChargerAPIv2StatusResponseDTO.class);
    }

    private void updateChannelsAndStatus(@Nullable GoEChargerAPIv2StatusResponseDTO goeResponse,
            @Nullable String message) {
        if (goeResponse == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, message);
            allChannels.forEach(channel -> updateState(channel, UnDefType.UNDEF));
        } else {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
            allChannels.forEach(channel -> {
                updateState(channel, getValue(channel, goeResponse));
            });
        }
    }

    private void refresh() {
        // Request new GoE data
        try {
            GoEChargerAPIv2StatusResponseDTO goeResponse = getGoEData();
            updateChannelsAndStatus(goeResponse, null);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            updateChannelsAndStatus(null, e.getMessage());
        }
    }

    private void startAutomaticRefresh() {
        if (refreshJob == null || refreshJob.isCancelled()) {
            GoEChargerAPIv2Configuration config = getConfigAs(GoEChargerAPIv2Configuration.class);
            int delay = config.refreshInterval;
            logger.debug("Running refresh job with delay {} s", delay);
            refreshJob = scheduler.scheduleWithFixedDelay(this::refresh, 0, delay, TimeUnit.SECONDS);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the Go-E Charger APIv2 handler.");

        final ScheduledFuture<?> refreshJob = this.refreshJob;
        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            this.refreshJob = null;
        }
    }
}
