/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.measure.quantity.ElectricCurrent;
import javax.measure.quantity.Energy;

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
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
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

    @SuppressWarnings("PMD.SimplifyBooleanExpressions")
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
                var phases = "1";
                if (goeResponse.phases == 2) {
                    phases = "3";
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
            case TRANSACTION:
                if (goeResponse.transaction == null) {
                    return UnDefType.UNDEF;
                }
                return new DecimalType(goeResponse.transaction);
            case ALLOW_CHARGING:
                return goeResponse.allowCharging == true ? OnOffType.ON : OnOffType.OFF;
            case TEMPERATURE_TYPE2_PORT:
                if (goeResponse.temperatures == null) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>(goeResponse.temperatures[0], SIUnits.CELSIUS);
            case TEMPERATURE_CIRCUIT_BOARD:
                if (goeResponse.temperatures == null) {
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
                return new QuantityType<>((Double) (goeResponse.totalChargeConsumption / 1000d), Units.KILOWATT_HOUR);
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
                return new QuantityType<>(goeResponse.energy[4], Units.AMPERE);
            case CURRENT_L2:
                if (goeResponse.energy == null) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>(goeResponse.energy[5], Units.AMPERE);
            case CURRENT_L3:
                if (goeResponse.energy == null) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>(goeResponse.energy[6], Units.AMPERE);
            case POWER_L1:
                if (goeResponse.energy == null) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>(goeResponse.energy[7], Units.WATT);
            case POWER_L2:
                if (goeResponse.energy == null) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>(goeResponse.energy[8], Units.WATT);
            case POWER_L3:
                if (goeResponse.energy == null) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>(goeResponse.energy[9], Units.WATT);
            case POWER_ALL:
                if (goeResponse.energy == null) {
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
                key = "amp";
                if (command instanceof DecimalType) {
                    value = String.valueOf(((DecimalType) command).intValue());
                } else if (command instanceof QuantityType<?>) {
                    value = String.valueOf(((QuantityType<ElectricCurrent>) command).toUnit(Units.AMPERE).intValue());
                }
                break;
            case SESSION_CHARGE_CONSUMPTION_LIMIT:
                key = "dwo";
                var multiplier = 1000;
                if (command instanceof DecimalType) {
                    value = String.valueOf(((DecimalType) command).intValue() * multiplier);
                } else if (command instanceof QuantityType<?>) {
                    value = String.valueOf(
                            ((QuantityType<Energy>) command).toUnit(Units.KILOWATT_HOUR).intValue() * multiplier);
                }
                break;
            case PHASES:
                key = "psm";
                if (command instanceof DecimalType) {
                    var phases = 1;
                    var help = (DecimalType) command;
                    if (help.intValue() == 3) {
                        // set value 2 for 3 phases
                        phases = 2;
                    }
                    value = String.valueOf(phases);
                }
                break;
            case FORCE_STATE:
                key = "frc";
                if (command instanceof DecimalType) {
                    value = String.valueOf(((DecimalType) command).intValue());
                }
                break;
            case TRANSACTION:
                key = "trx";
                if (command instanceof DecimalType) {
                    value = String.valueOf(((DecimalType) command).intValue());
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
        filter = "?filter=";
        var declaredFields = GoEStatusResponseV2DTO.class.getDeclaredFields();
        var declaredFieldsBase = GoEStatusResponseV2DTO.class.getSuperclass().getDeclaredFields();

        for (var field : declaredFields) {
            filter += field.getAnnotation(SerializedName.class).value() + ",";
        }
        for (var field : declaredFieldsBase) {
            filter += field.getAnnotation(SerializedName.class).value() + ",";
        }
        filter = filter.substring(0, filter.length() - 1);

        super.initialize();
    }

    private String getReadUrl() {
        return GoEChargerBindingConstants.API_URL_V2.replace("%IP%", config.ip.toString()) + filter;
    }

    private String getWriteUrl(String key, String value) {
        return GoEChargerBindingConstants.SET_URL_V2.replace("%IP%", config.ip.toString()).replace("%KEY%", key)
                .replace("%VALUE%", value);
    }

    private void sendData(String key, String value) {
        String urlStr = getWriteUrl(key, value);
        logger.trace("POST URL = {}", urlStr);

        try {
            HttpMethod httpMethod = HttpMethod.GET;
            ContentResponse contentResponse = httpClient.newRequest(urlStr).method(httpMethod)
                    .timeout(5, TimeUnit.SECONDS).send();
            String response = contentResponse.getContentAsString();

            logger.trace("{} Response: {}", httpMethod.toString(), response);

            var statusCode = contentResponse.getStatus();
            if (!(statusCode == 200 || statusCode == 204)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/unsuccessful.communication-error");
                logger.debug("Could not send data, Response {}, StatusCode: {}", response, statusCode);
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ie.toString());
            logger.debug("Could not send data: {}, {}", urlStr, ie.toString());
        } catch (TimeoutException | ExecutionException | JsonSyntaxException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.toString());
            logger.debug("Could not send data: {}, {}", urlStr, e.toString());
        }
    }

    /**
     * Request new data from Go-eCharger
     *
     * @return the Go-eCharger object mapping the JSON response or null in case of
     *         error
     * @throws ExecutionException
     * @throws TimeoutException
     * @throws InterruptedException
     */
    @Nullable
    @Override
    protected GoEStatusResponseBaseDTO getGoEData()
            throws InterruptedException, TimeoutException, ExecutionException, JsonSyntaxException {
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

    protected void updateChannelsAndStatus(@Nullable GoEStatusResponseBaseDTO goeResponse, @Nullable String message) {
        if (goeResponse == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, message);
            allChannels.forEach(channel -> updateState(channel, UnDefType.UNDEF));
        } else {
            updateStatus(ThingStatus.ONLINE);
            allChannels
                    .forEach(channel -> updateState(channel, getValue(channel, (GoEStatusResponseV2DTO) goeResponse)));
        }
    }
}
