/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import static org.openhab.binding.goecharger.internal.GoEChargerBindingConstants.*;
import static org.openhab.binding.goecharger.internal.api.GoEStatusV2ApiKeys.*;
import static org.openhab.core.thing.ThingStatus.OFFLINE;
import static org.openhab.core.thing.ThingStatus.ONLINE;
import static org.openhab.core.thing.ThingStatusDetail.*;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.goecharger.internal.GoEChargerBindingConstants;
import org.openhab.binding.goecharger.internal.api.GoEStatusResponseBaseDTO;
import org.openhab.binding.goecharger.internal.api.GoEStatusResponseDTO;
import org.openhab.binding.goecharger.internal.api.GoEStatusResponseV2DTO;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link GoEChargerV2Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Samuel Brucksch - Initial contribution
 * @author Reinhard Plaim - Adapt to use API version 2
 */
@NonNullByDefault
public class GoEChargerV2Handler extends GoEChargerBaseHandler {

    private final Logger logger = LoggerFactory.getLogger(GoEChargerV2Handler.class);

    private String filter = "";

    public GoEChargerV2Handler(Thing thing, HttpClient httpClient) {
        super(thing, httpClient);
    }

    @Override
    protected State getValue(String channelId, GoEStatusResponseBaseDTO goeResponseBase) {
        var state = super.getValue(channelId, goeResponseBase);
        if (state != UnDefType.UNDEF) {
            return state;
        }

        var goeResponse = (GoEStatusResponseV2DTO) goeResponseBase;
        switch (channelId) {
            case PHASES:
                if (goeResponse.phases == null) {
                    return UnDefType.UNDEF;
                }
                return new DecimalType((goeResponse.phases == 2) ? 3 : 1);
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
            case TRANSACTION:
                if (goeResponse.transaction == null) {
                    return UnDefType.UNDEF;
                }
                return new DecimalType(goeResponse.transaction);
            case AWATTAR_MAX_PRICE:
                if (goeResponse.awattarMaxPrice == null) {
                    return UnDefType.UNDEF;
                }
                return new DecimalType(goeResponse.awattarMaxPrice);
            case ALLOW_CHARGING:
                if (goeResponse.allowCharging == null) {
                    return UnDefType.UNDEF;
                }
                return OnOffType.from(goeResponse.allowCharging);
            case TEMPERATURE_TYPE2_PORT:
                // It was reported that the temperature is invalid when only one value is returned
                // That's why it is checked that at least 2 values are returned
                if (goeResponse.temperatures == null || goeResponse.temperatures.length < 2) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>(goeResponse.temperatures[0], SIUnits.CELSIUS);
            case TEMPERATURE_CIRCUIT_BOARD:
                if (goeResponse.temperatures == null || goeResponse.temperatures.length < 2) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>(goeResponse.temperatures[1], SIUnits.CELSIUS);
            case SESSION_CHARGE_CONSUMPTION:
                if (goeResponse.sessionChargeConsumption == null) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>(goeResponse.sessionChargeConsumption / 1000d, Units.KILOWATT_HOUR);
            case SESSION_CHARGE_CONSUMPTION_LIMIT:
                if (goeResponse.sessionChargeConsumptionLimit == null) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>(goeResponse.sessionChargeConsumptionLimit / 1000d, Units.KILOWATT_HOUR);
            case TOTAL_CONSUMPTION:
                if (goeResponse.totalChargeConsumption == null) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>(goeResponse.totalChargeConsumption / 1000d, Units.KILOWATT_HOUR);
            case VOLTAGE_L1:
                if (goeResponse.energy == null || goeResponse.energy.length < 1) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>(goeResponse.energy[0], Units.VOLT);
            case VOLTAGE_L2:
                if (goeResponse.energy == null || goeResponse.energy.length < 2) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>(goeResponse.energy[1], Units.VOLT);
            case VOLTAGE_L3:
                if (goeResponse.energy == null || goeResponse.energy.length < 3) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>(goeResponse.energy[2], Units.VOLT);
            case CURRENT_L1:
                if (goeResponse.energy == null || goeResponse.energy.length < 5) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>(goeResponse.energy[4], Units.AMPERE);
            case CURRENT_L2:
                if (goeResponse.energy == null || goeResponse.energy.length < 6) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>(goeResponse.energy[5], Units.AMPERE);
            case CURRENT_L3:
                if (goeResponse.energy == null || goeResponse.energy.length < 7) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>(goeResponse.energy[6], Units.AMPERE);
            case POWER_L1:
                if (goeResponse.energy == null || goeResponse.energy.length < 8) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>(goeResponse.energy[7], Units.WATT);
            case POWER_L2:
                if (goeResponse.energy == null || goeResponse.energy.length < 9) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>(goeResponse.energy[8], Units.WATT);
            case POWER_L3:
                if (goeResponse.energy == null || goeResponse.energy.length < 10) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>(goeResponse.energy[9], Units.WATT);
            case POWER_ALL:
                if (goeResponse.energy == null || goeResponse.energy.length < 12) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>(goeResponse.energy[11], Units.WATT);
            case FORCE_STATE:
                if (goeResponse.forceState == null) {
                    return UnDefType.UNDEF;
                }
                return new DecimalType(goeResponse.forceState.toString());
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
                key = AMP;
                if (command instanceof DecimalType decimalCommand) {
                    value = String.valueOf(decimalCommand.intValue());
                } else if (command instanceof QuantityType<?> quantityCommand) {
                    value = String.valueOf(Objects.requireNonNull(quantityCommand.toUnit(Units.AMPERE)).intValue());
                }
                break;
            case AWATTAR_MAX_PRICE:
                key = AWP;
                if (command instanceof DecimalType decimalCommand) {
                    value = String.valueOf(decimalCommand);
                }
                break;
            case SESSION_CHARGE_CONSUMPTION_LIMIT:
                key = DWO;
                var multiplier = 1000;
                if (command instanceof DecimalType decimalCommand) {
                    value = String.valueOf(decimalCommand.intValue() * multiplier);
                } else if (command instanceof QuantityType<?> quantityCommand) {
                    value = String
                            .valueOf(Objects.requireNonNull(quantityCommand.toUnit(Units.KILOWATT_HOUR)).intValue()
                                    * multiplier);
                }
                break;
            case PHASES:
                key = PSM;
                if (command instanceof DecimalType decimalCommand) {
                    // set value 2 for 3 phases
                    value = decimalCommand.intValue() == 3 ? "2" : "1";
                }
                break;
            case FORCE_STATE:
                key = FRC;
                if (command instanceof DecimalType decimalCommand) {
                    value = String.valueOf(decimalCommand.intValue());
                }
                break;
            case TRANSACTION:
                key = TRX;
                if (command instanceof DecimalType decimalCommand) {
                    value = String.valueOf(decimalCommand.intValue());
                }
                break;
        }

        if (key != null && value != null) {
            sendData(key, value);
        } else {
            logger.warn("Could not update channel {} with key {} and value {}", channelUID.getId(), key, value);
        }
    }

    @Override
    public void initialize() {
        // only read needed parameters
        filter = "filter=";
        Field[] declaredFields = GoEStatusResponseV2DTO.class.getDeclaredFields();
        Field[] declaredFieldsBase = Objects.requireNonNull(GoEStatusResponseV2DTO.class.getSuperclass())
                .getDeclaredFields();

        for (Field field : declaredFields) {
            filter += Objects.requireNonNull(field.getAnnotation(SerializedName.class)).value() + ",";
        }
        for (Field field : declaredFieldsBase) {
            filter += Objects.requireNonNull(field.getAnnotation(SerializedName.class)).value() + ",";
        }
        filter = filter.substring(0, filter.length() - 1);

        super.initialize();
    }

    private String getReadUrl() throws IllegalArgumentException {
        if (config.ip instanceof String ip) {
            return GoEChargerBindingConstants.API_URL_V2.replace("%IP%", ip) + "?" + filter;
        } else if (config.serial instanceof String serial && config.token instanceof String token) {
            return GoEChargerBindingConstants.API_URL_CLOUD_V2.replace("%SERIAL%", serial).replace("%TOKEN%", token)
                    + "&" + filter;
        } else {
            throw new IllegalArgumentException("either ip or token+serial must be configured");
        }
    }

    private String getWriteUrl(String key, String value) throws IllegalArgumentException {
        if (config.ip instanceof String ip) {
            return GoEChargerBindingConstants.SET_URL_V2.replace("%IP%", ip).replace("%KEY%", key).replace("%VALUE%",
                    value);
        } else if (config.serial instanceof String serial && config.token instanceof String token) {
            return GoEChargerBindingConstants.SET_URL_CLOUD_V2.replace("%SERIAL%", serial).replace("%TOKEN%", token)
                    .replace("%KEY%", key).replace("%VALUE%", value);
        } else {
            throw new IllegalArgumentException("either ip or token+serial must be configured");
        }
    }

    private void sendData(String key, String value) {
        String urlStr;
        try {
            urlStr = getWriteUrl(key, value);
        } catch (IllegalArgumentException e) {
            updateStatus(OFFLINE, CONFIGURATION_ERROR, e.getMessage());
            return;
        }

        HttpMethod httpMethod = HttpMethod.GET;
        logger.trace("{} URL = {}", httpMethod, urlStr);

        try {
            ContentResponse contentResponse = httpClient.newRequest(urlStr).method(httpMethod)
                    .timeout(5, TimeUnit.SECONDS).send();
            String response = contentResponse.getContentAsString();

            logger.trace("{} Response: {}", httpMethod.toString(), response);

            var statusCode = contentResponse.getStatus();
            if (!(statusCode == 200 || statusCode == 202 || statusCode == 204)) {
                updateStatus(OFFLINE, COMMUNICATION_ERROR, "@text/unsuccessful.communication-error");
                logger.debug("Could not send data, Response {}, StatusCode: {}", response, statusCode);
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            updateStatus(OFFLINE, COMMUNICATION_ERROR, ie.toString());
            logger.debug("Could not send data: {}, {}", urlStr, ie.toString());
        } catch (TimeoutException | ExecutionException | JsonSyntaxException e) {
            updateStatus(OFFLINE, COMMUNICATION_ERROR, e.toString());
            logger.debug("Could not send data: {}, {}", urlStr, e.toString());
        }
    }

    /**
     * Retrieves data from the Go-E Charger API based on the configured API version.
     * Sends an HTTP GET request to the charger and parses the response into the appropriate DTO.
     *
     * @return A {@link GoEStatusResponseBaseDTO} object containing the parsed response data.
     *         Returns {@link GoEStatusResponseDTO} for API version 1 and {@link GoEStatusResponseV2DTO} for API version
     *         2.
     * @throws InterruptedException If the thread is interrupted while waiting for the response.
     * @throws TimeoutException If the request times out.
     * @throws ExecutionException If an exception occurs during the execution of the request.
     * @throws JsonSyntaxException If the response JSON cannot be parsed into the expected DTO.
     * @throws IllegalArgumentException If the response JSON is invalid or does not match the expected format.
     */
    @Override
    protected @Nullable GoEStatusResponseBaseDTO getGoEData() throws InterruptedException, TimeoutException,
            ExecutionException, JsonSyntaxException, IllegalArgumentException {
        String urlStr = getReadUrl();
        logger.trace("GET URL = {}", urlStr);

        ContentResponse contentResponse = httpClient.newRequest(urlStr).method(HttpMethod.GET)
                .timeout(5, TimeUnit.SECONDS).send();

        String response = contentResponse.getContentAsString();
        logger.trace("GET Response: {}", response);

        if (config.apiVersion == 1) {
            return gson.fromJson(response, GoEStatusResponseDTO.class);
        }
        return gson.fromJson(response, GoEStatusResponseV2DTO.class);
    }

    @Override
    protected void updateChannelsAndStatus(@Nullable GoEStatusResponseBaseDTO goeResponse, @Nullable String message) {
        if (goeResponse == null) {
            updateStatus(OFFLINE, COMMUNICATION_ERROR, message);
            allChannels.forEach(channel -> updateState(channel, UnDefType.UNDEF));
        } else {
            updateStatus(ONLINE);
            allChannels.forEach(channel -> updateState(channel, getValue(channel, goeResponse)));
        }
    }
}
