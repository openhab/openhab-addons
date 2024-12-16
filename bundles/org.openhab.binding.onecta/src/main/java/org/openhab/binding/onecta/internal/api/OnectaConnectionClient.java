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
package org.openhab.binding.onecta.internal.api;

import static org.openhab.binding.onecta.internal.api.OnectaProperties.*;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.core.MediaType;

import org.eclipse.jetty.client.HttpContentResponse;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.onecta.internal.OnectaConfiguration;
import org.openhab.binding.onecta.internal.api.dto.commands.CommandOnOf;
import org.openhab.binding.onecta.internal.api.dto.commands.CommandTrueFalse;
import org.openhab.binding.onecta.internal.api.dto.units.Unit;
import org.openhab.binding.onecta.internal.api.dto.units.Units;
import org.openhab.binding.onecta.internal.exception.DaikinCommunicationDataException;
import org.openhab.binding.onecta.internal.exception.DaikinCommunicationException;
import org.openhab.binding.onecta.internal.exception.DaikinCommunicationForbiddenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.*;

/**
 * @author Alexander Drent - Initial contributionF
 */
public class OnectaConnectionClient {

    private static final Logger logger = LoggerFactory.getLogger(OnectaConnectionClient.class);
    private static final String HTTPHEADER_X_API_KEY = "x-api-key";
    private static final String HTTPHEADER_BEARER = "Bearer %s";
    private static final String USER_AGENT_VALUE = "Daikin/1.6.1.4681 CFNetwork/1209 Darwin/20.2.0";
    private static final String HTTPHEADER_X_API_KEY_VALUE = "xw6gvOtBHq5b1pyceadRp6rujSNSZdjx2AqT03iC";
    private static final Long REQUEST_TIMEOUT = 60L; // 60 seconds

    private static JsonArray onectaCompleteJsonArrayData = new JsonArray();
    private static Units onectaUnitsData = new Units();
    private static OnectaSignInClient onectaSignInClient;
    private OnectaConfiguration onectaConfiguration = new OnectaConfiguration();

    public OnectaConnectionClient() {
        if (onectaSignInClient == null) {
            onectaSignInClient = new OnectaSignInClient();
        }
    }

    public Units getUnits() {
        return onectaUnitsData;
    }

    public void startConnecton(String userId, String password) throws DaikinCommunicationException {
        onectaSignInClient.signIn(userId, password);
    }

    public void restoreConnecton() throws DaikinCommunicationException {
        onectaSignInClient.fetchAccessToken();
    }

    public Boolean isOnline() {
        return onectaSignInClient.isOnline();
    }

    private Response doBearerRequestGet(Boolean refreshed) throws DaikinCommunicationException {
        Response response = null;
        logger.debug("doBearerRequestGet : Accesstoken refreshed {}", refreshed.toString());
        try {
            /*
             * if (!onectaSignInClient.isOnline()) {
             * onectaSignInClient.signIn();
             * }
             */
            response = onectaConfiguration.getHttpClient().newRequest(OnectaProperties.getBaseUrl(""))
                    .method(HttpMethod.GET)
                    .header(HttpHeader.AUTHORIZATION, String.format(HTTPHEADER_BEARER, onectaSignInClient.getToken()))
                    .header(HttpHeader.USER_AGENT, USER_AGENT_VALUE)
                    .header(HTTPHEADER_X_API_KEY, HTTPHEADER_X_API_KEY_VALUE).timeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
                    .send();

            if (response.getStatus() == HttpStatus.UNAUTHORIZED_401 && !refreshed) {
                onectaSignInClient.fetchAccessToken();
                response = doBearerRequestGet(true);
            }

        } catch (DaikinCommunicationException | ExecutionException | InterruptedException | TimeoutException e) {
            if (!refreshed) {
                try {
                    onectaSignInClient.fetchAccessToken();
                    response = doBearerRequestGet(true);
                } catch (DaikinCommunicationException ex) {
                    throw new DaikinCommunicationException(ex);
                }
            } else {
                throw new DaikinCommunicationException(e);
            }
        }

        return response;
    }

    private Response doBearerRequestPatch(String url, Object body) {
        return doBearerRequestPatch(url, body, false);
    }

    private Response doBearerRequestPatch(String url, Object body, Boolean refreshed) {
        Response response = null;
        try {
            /*
             * if (!onectaSignInClient.isOnline()) {
             * onectaSignInClient.signIn();
             * }
             */
            response = onectaConfiguration.getHttpClient().newRequest(url).method(HttpMethod.PATCH)
                    .content(new StringContentProvider(new Gson().toJson(body)), MediaType.APPLICATION_JSON)
                    .header(HttpHeader.AUTHORIZATION, String.format(HTTPHEADER_BEARER, onectaSignInClient.getToken()))
                    .header(HttpHeader.USER_AGENT, USER_AGENT_VALUE)
                    .header(HTTPHEADER_X_API_KEY, HTTPHEADER_X_API_KEY_VALUE).timeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
                    .send();

            logger.trace("Request : {}", response.getRequest().getURI().toString());
            logger.trace("Body    : {}", new Gson().toJson(body));
            logger.trace("Resonse : {}", ((HttpContentResponse) response).getContentAsString());

            if (response.getStatus() == HttpStatus.UNAUTHORIZED_401 && !refreshed) {
                onectaSignInClient.fetchAccessToken();
                response = doBearerRequestPatch(url, body, true);
            }
            return response;
        } catch (DaikinCommunicationException | ExecutionException | InterruptedException | TimeoutException e) {
            if (!refreshed) {
                try {
                    onectaSignInClient.fetchAccessToken();
                    response = doBearerRequestPatch(url, body, true);
                } catch (DaikinCommunicationException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        return response;
    }

    public void refreshUnitsData() throws DaikinCommunicationException {

        Response response = doBearerRequestGet(false);
        String responseString = ((HttpContentResponse) response).getContentAsString();

        logger.trace("Response body: {}", responseString);
        if (response.getStatus() == HttpStatus.OK_200) {
            try {
                onectaCompleteJsonArrayData = JsonParser.parseString(responseString).getAsJsonArray();
                onectaUnitsData.getAll().clear();
                for (int i = 0; i < onectaCompleteJsonArrayData.size(); i++) {
                    onectaUnitsData.getAll().add(Objects.requireNonNull(
                            new Gson().fromJson(onectaCompleteJsonArrayData.get(i).getAsJsonObject(), Unit.class)));
                }
            } catch (JsonSyntaxException ex) {
                logger.error("Response body: {}", responseString);
                throw new DaikinCommunicationDataException(
                        String.format("Not a valid json response from Onecta received: (%s)", ex.getMessage()));
            }
        } else {
            throw new DaikinCommunicationForbiddenException(
                    String.format("refreshUnitsData resonse (%s) : (%s)", response.getStatus(), responseString));
        }
    }

    public Unit getUnit(String unitId) {
        return onectaUnitsData.findById(unitId);
    }

    public JsonObject getRawData(String unitId) {
        JsonObject jsonObject = null;
        for (int i = 0; i < onectaCompleteJsonArrayData.size(); i++) {
            jsonObject = onectaCompleteJsonArrayData.get(i).getAsJsonObject();
            if (jsonObject.get("id").getAsString().equals(unitId)) {
                return jsonObject;
            }
        }

        return new JsonObject();
    }

    public void setPowerOnOff(String unitId, Enums.ManagementPoint managementPointType, Enums.OnOff value) {
        logger.debug("setPowerOnOff : {}, {}, {}", unitId, managementPointType.getValue(), value);
        CommandOnOf commandOnOf = new CommandOnOf(value);
        doBearerRequestPatch(getUrlOnOff(unitId, managementPointType), commandOnOf);
    }

    public void setPowerFulModeOnOff(String unitId, Enums.ManagementPoint managementPointType, Enums.OnOff value) {
        logger.debug("setPowerFulModeOnOff : {}, {}, {}", unitId, managementPointType.getValue(), value);
        CommandOnOf commandOnOf = new CommandOnOf(value);
        doBearerRequestPatch(getUrlPowerFulModeOnOff(unitId, managementPointType), commandOnOf);
    }

    public void setEconoMode(String unitId, Enums.ManagementPoint managementPointType, Enums.OnOff value) {
        logger.debug("setEconoMode: {}, {}, {}", unitId, managementPointType.getValue(), value);
        CommandOnOf commandOnOf = new CommandOnOf(value);
        doBearerRequestPatch(getEconoMode(unitId, managementPointType), commandOnOf);
    }

    public void setCurrentOperationMode(String unitId, Enums.ManagementPoint managementPointType,
            Enums.OperationMode operationMode) {
        logger.debug("setCurrentOperationMode : {}, {}, {}", unitId, managementPointType.getValue(),
                operationMode.getValue());
        doBearerRequestPatch(OnectaProperties.getOperationModeUrl(unitId, managementPointType),
                OnectaProperties.getOperationModeCommand(operationMode));
    }

    public void setCurrentTemperatureRoomSet(String unitId, String embeddedId, Enums.OperationMode currentMode,
            float value) {
        logger.debug("setCurrentTemperatureRoomSet : {}, {}, {}", unitId, embeddedId, currentMode.getValue());
        doBearerRequestPatch(OnectaProperties.getTemperatureControlUrl(unitId, embeddedId),
                OnectaProperties.getTemperatureRoomControlCommand(value, currentMode));
    }

    public void setCurrentTemperatureHotWaterSet(String unitId, String embeddedId, Enums.OperationMode currentMode,
            float value) {
        logger.debug("setCurrentTemperatureHotWaterSet : {}, {}, {}", unitId, embeddedId, currentMode.getValue());
        doBearerRequestPatch(OnectaProperties.getTemperatureControlUrl(unitId, embeddedId),
                OnectaProperties.getTemperatureHotWaterControlCommand(value, currentMode));
    }

    public void setFanSpeed(String unitId, String embeddedId, Enums.OperationMode currentMode,
            Enums.FanSpeed fanspeed) {
        logger.debug("setFanSpeed : {}, {}, {}", unitId, embeddedId, currentMode.getValue());
        doBearerRequestPatch(OnectaProperties.getTFanControlUrl(unitId, embeddedId),
                getTFanSpeedCurrentCommand(currentMode, fanspeed));
        if (fanspeed.getValueMode().equals(Enums.FanSpeedMode.FIXED.getValue())) {
            doBearerRequestPatch(OnectaProperties.getTFanControlUrl(unitId, embeddedId),
                    OnectaProperties.getTFanSpeedFixedCommand(currentMode, fanspeed));
        }
    }

    public void setCurrentFanDirection(String unitId, String embeddedId, Enums.OperationMode currentMode,
            Enums.FanMovement fanMovement) {
        logger.debug("setCurrentFanDirection : {}, {}, {}", unitId, embeddedId, currentMode.getValue());
        String url = getTFanControlUrl(unitId, embeddedId);
        switch (fanMovement) {
            case STOPPED:
                doBearerRequestPatch(url,
                        OnectaProperties.getTFanDirectionHorCommand(currentMode, Enums.FanMovementHor.STOPPED));

                doBearerRequestPatch(url,
                        OnectaProperties.getTFanDirectionVerCommand(currentMode, Enums.FanMovementVer.STOPPED));
                break;
            case VERTICAL:
                doBearerRequestPatch(url,
                        OnectaProperties.getTFanDirectionHorCommand(currentMode, Enums.FanMovementHor.STOPPED));

                doBearerRequestPatch(url,
                        OnectaProperties.getTFanDirectionVerCommand(currentMode, Enums.FanMovementVer.SWING));
                break;
            case HORIZONTAL:
                doBearerRequestPatch(url,
                        OnectaProperties.getTFanDirectionHorCommand(currentMode, Enums.FanMovementHor.SWING));

                doBearerRequestPatch(url,
                        OnectaProperties.getTFanDirectionVerCommand(currentMode, Enums.FanMovementVer.STOPPED));
                break;
            case VERTICAL_AND_HORIZONTAL:
                doBearerRequestPatch(url,
                        OnectaProperties.getTFanDirectionHorCommand(currentMode, Enums.FanMovementHor.SWING));

                doBearerRequestPatch(url,
                        OnectaProperties.getTFanDirectionVerCommand(currentMode, Enums.FanMovementVer.SWING));
                break;
            default:
                break;
        }
    }

    public void setCurrentFanDirectionHor(String unitId, String embeddedId, Enums.OperationMode currentMode,
            Enums.FanMovementHor fanMovement) {
        logger.debug("setCurrentFanDirectionHor : {}, {}, {}", unitId, embeddedId, currentMode.getValue());
        String url = getTFanControlUrl(unitId, embeddedId);
        doBearerRequestPatch(url, OnectaProperties.getTFanDirectionHorCommand(currentMode, fanMovement));
    }

    public void setCurrentFanDirectionVer(String unitId, String embeddedId, Enums.OperationMode currentMode,
            Enums.FanMovementVer fanMovement) {
        logger.debug("setCurrentFanDirectionVer : {}, {}, {}", unitId, embeddedId, currentMode.getValue());
        String url = getTFanControlUrl(unitId, embeddedId);
        doBearerRequestPatch(url, OnectaProperties.getTFanDirectionVerCommand(currentMode, fanMovement));
    }

    public void setStreamerMode(String unitId, String embeddedId, Enums.OnOff value) {
        logger.debug("setStreamerMode: {}, {}, {}", unitId, embeddedId, value);
        CommandOnOf commandOnOf = new CommandOnOf(value);
        doBearerRequestPatch(getStreamerMode(unitId, embeddedId), commandOnOf);
    }

    public void setHolidayMode(String unitId, String embeddedId, Enums.OnOff value) {
        logger.debug("setHolidayMode: {}, {}, {}", unitId, embeddedId, value);
        CommandTrueFalse commandTrueFalse = new CommandTrueFalse(value);
        doBearerRequestPatch(getHolidayMode(unitId, embeddedId), commandTrueFalse);
    }

    public void setDemandControl(String unitId, String embeddedId, Enums.DemandControl value) {
        logger.debug("setDemandControl: {}, {}, {}", unitId, embeddedId, value);
        doBearerRequestPatch(getTDemandControlUrl(unitId, embeddedId),
                OnectaProperties.getTDemandControlCommand(value));
    }

    public void setDemandControlFixedValue(String unitId, String embeddedId, Integer value) {
        logger.debug("setDemandControlFixedValue: {}, {}, {}", unitId, embeddedId, value);

        doBearerRequestPatch(getTDemandControlUrl(unitId, embeddedId),
                OnectaProperties.getTDemandControlFixedValueCommand(value));
    }

    public String getRefreshToken() {
        return onectaSignInClient.getRefreshToken();
    }

    public void setRefreshToken(String refreshToken) {
        onectaSignInClient.setRefreshToken(refreshToken);
    }

    public void setTargetTemperatur(String unitId, String embeddedId, Float value) {
        logger.debug("setRefreshToken: {}, {}, {}", unitId, embeddedId, value);
        doBearerRequestPatch(getTargetTemperaturUrl(unitId, embeddedId), getTargetTemperaturCommand(value));
    }

    public void setSetpointLeavingWaterOffset(String unitId, String embeddedId, Enums.OperationMode operationMode,
            Float value) {
        logger.debug("setRefreshToken: {}, {}, {}. {}", unitId, embeddedId, operationMode, value);
        doBearerRequestPatch(OnectaProperties.getTemperatureControlUrl(unitId, embeddedId),
                OnectaProperties.getSetpointLeavingWaterOffsetCommand(value, operationMode));
    }

    public void setSetpointLeavingWaterTemperature(String unitId, String embeddedId, Enums.OperationMode operationMode,
            Float value) {
        logger.debug("setRefreshToken: {}, {}, {}, {}", unitId, embeddedId, operationMode, value);
        doBearerRequestPatch(OnectaProperties.getTemperatureControlUrl(unitId, embeddedId),
                OnectaProperties.getSetpointLeavingWaterTemperatureCommand(value, operationMode));
    }
}
