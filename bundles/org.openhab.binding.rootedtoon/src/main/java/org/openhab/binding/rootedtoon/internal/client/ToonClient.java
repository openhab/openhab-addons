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

package org.openhab.binding.rootedtoon.internal.client;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.rootedtoon.internal.client.model.PowerUsageInfo;
import org.openhab.binding.rootedtoon.internal.client.model.RatedValue;
import org.openhab.binding.rootedtoon.internal.client.model.RealtimeUsageInfo;
import org.openhab.binding.rootedtoon.internal.client.model.ThermostatInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * ToonClient handles the actual communication with the Toon, and fires events
 * which can be listened to by a listener.
 *
 * @author daanmeijer - Initial Contribution
 *
 */

public class ToonClient {
    public enum State {
        Manual(-1),
        Comfort(0),
        Home(1),
        Sleep(2),
        Away(3),
        Holiday(4);

        private final int toonState;

        State(int toonState) {
            this.toonState = toonState;
        }

        public int toonState() {
            return toonState;
        }

        public static State fromToonState(int toonState) {
            State result = Manual;
            byte b;
            int i;
            State[] arrayOfState;
            for (i = (arrayOfState = values()).length, b = 0; b < i;) {
                State state = arrayOfState[b];
                if (state.toonState == toonState) {
                    result = state;
                    break;
                }
                b++;
            }

            return result;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(ToonClient.class);

    protected Client client = ClientBuilder.newClient();

    protected WebTarget toonTarget;
    private final JsonParser jsonParser = new JsonParser();
    private final Gson gson = new Gson();

    List<ToonClientEventListener> listeners;

    public void setActiveState(State state) throws ToonConnectionException {
        Response response = toonTarget.path("happ_thermstat").queryParam("action", new Object[] { "changeSchemeState" })
                .queryParam("state", new Object[] { "2" })
                .queryParam("temperatureState", new Object[] { Integer.valueOf(state.toonState()) })
                .request(new MediaType[] { MediaType.APPLICATION_JSON_TYPE }).get();
        validateResponse(response);
    }

    public void setRoomSetpoint(double temperature) throws ToonConnectionException {
        double sanitized = Math.min(30.0D, Math.max(10.0D, Math.round(temperature * 2.0D) / 2.0D));
        logger.debug("Setting temperature to {}", temperature);
        Response response = toonTarget.path("happ_thermstat").queryParam("action", new Object[] { "roomSetpoint" })
                .queryParam("Setpoint", new Object[] { Integer.valueOf((int) (sanitized * 100.0D)) })
                .request(new MediaType[] { MediaType.APPLICATION_JSON_TYPE }).get();
        validateResponse(response);
    }

    public ToonClient(String url) {
        listeners = new LinkedList<>();
        toonTarget = client.target(url);
    }

    public void addListener(ToonClientEventListener listener) {
        listeners.add(listener);
    }

    public void requestThermostatInfo() throws ToonConnectionException {
        Response response = toonTarget.path("happ_thermstat").queryParam("action", new Object[] { "getThermostatInfo" })
                .request(new MediaType[] { MediaType.APPLICATION_JSON_TYPE }).get();

        JsonObject json = validateResponse(response);
        logger.debug("toon thermostate info: {}", json);
        ThermostatInfo result = gson.fromJson((JsonElement) json, ThermostatInfo.class);

        result.currentTemp /= 100.0D;
        result.currentSetpoint /= 100.0D;
        result.nextSetpoint /= 100.0D;

        for (ToonClientEventListener listener : listeners) {
            listener.newThermostatInfo(result);
        }
    }

    public void requestPowerUsageInfo() throws ToonConnectionException {
        Response response = toonTarget.path("happ_pwrusage").queryParam("action", new Object[] { "GetCurrentUsage" })
                .request(new MediaType[] { MediaType.APPLICATION_JSON_TYPE }).get();

        JsonObject json = validateResponse(response);
        logger.debug("toon power usage state: {}", json);
        PowerUsageInfo result = gson.fromJson((JsonElement) json, PowerUsageInfo.class);

        for (ToonClientEventListener listener : listeners) {
            listener.newPowerUsageInfo(result);
        }
    }

    public void requestRealtimeUsageInfo() throws ToonConnectionException {
        Response response = toonTarget.path("hdrv_zwave").queryParam("action", new Object[] { "getDevices.json" })
                .request(new MediaType[] { MediaType.APPLICATION_JSON_TYPE }).get();

        try {
            String strJson = response.readEntity(String.class);
            logger.debug("Realtime usage info response: {}", strJson);
            JsonObject json = jsonParser.parse(strJson).getAsJsonObject();

            RealtimeUsageInfo result = new RealtimeUsageInfo();

            for (Map.Entry<String, JsonElement> elm : (Iterable<Map.Entry<String, JsonElement>>) json.entrySet()) {
                JsonObject dev = elm.getValue().getAsJsonObject();

                try {
                    String str = dev.get("type").getAsString();
                    switch (str) {

                        case "HAE_METER_v3_1":
                        case "gas":
                            result.gas = new RatedValue(Double.valueOf(dev.get("CurrentGasFlow").getAsDouble()),
                                    Double.valueOf(dev.get("CurrentGasQuantity").getAsDouble()));
                            break;

                        case "HAE_METER_v3_2":
                        case "elec":
                            result.elec = new RatedValue(
                                    Double.valueOf(dev.get("CurrentElectricityFlow").getAsDouble()),
                                    Double.valueOf(dev.get("CurrentElectricityQuantity").getAsDouble()));
                            break;

                        case "HAE_METER_v3_3":
                        case "elec_solar":
                            double flow = Double.valueOf(dev.get("CurrentElectricityFlow").getAsDouble());
                            double total = Double.valueOf(dev.get("CurrentElectricityQuantity").getAsDouble());
                            result.elec_solar = new RatedValue(flow, total);
                            break;

                        case "HAE_METER_v3_4":
                        case "elec_delivered_nt":
                            result.elec_delivered_nt = new RatedValue(
                                    Double.valueOf(dev.get("CurrentElectricityFlow").getAsDouble()),
                                    Double.valueOf(dev.get("CurrentElectricityQuantity").getAsDouble()));
                            break;

                        case "HAE_METER_v3_5":
                        case "elec_received_nt":

                            result.elec_received_nt = new RatedValue(
                                    Double.valueOf(dev.get("CurrentElectricityFlow").getAsDouble()),
                                    Double.valueOf(dev.get("CurrentElectricityQuantity").getAsDouble()));
                            break;

                        case "HAE_METER_v3_6":
                        case "elec_delivered_lt":
                            result.elec_delivered_lt = new RatedValue(
                                    Double.valueOf(dev.get("CurrentElectricityFlow").getAsDouble()),
                                    Double.valueOf(dev.get("CurrentElectricityQuantity").getAsDouble()));
                            break;

                        case "HAE_METER_v3_7":
                        case "elec_received_lt":

                            result.elec_received_lt = new RatedValue(
                                    Double.valueOf(dev.get("CurrentElectricityFlow").getAsDouble()),
                                    Double.valueOf(dev.get("CurrentElectricityQuantity").getAsDouble()));
                            break;

                        case "HAE_METER_v3_8":
                        case "heat":
                            result.heat = new RatedValue(Double.valueOf(dev.get("CurrentHeatQuantity").getAsDouble()),
                                    null);
                            break;

                    }
                } catch (Exception exception) {
                    logger.warn("{}", dev);
                    logger.warn("Error while parsing getDevices.json output", exception);
                }
            }

            for (ToonClientEventListener listener : listeners) {
                listener.newRealtimeUsageInfo(result);
            }
        } catch (Exception e) {
            logger.warn("Error while processing the listeners", e);
            throw new ToonConnectionException(e.getMessage());
        }
    }

    private JsonObject validateResponse(Response response) throws ToonConnectionException {
        if (response.getStatus() != 200) {
            logger.debug("response status {}", Integer.valueOf(response.getStatus()));
            throw new ToonConnectionException("invalid toon response status: " + response.getStatus());
        }
        if (!response.hasEntity()) {
            logger.debug("empty response from toon");
            return new JsonObject();
        }
        String strJson = response.readEntity(String.class);

        JsonObject json = jsonParser.parse(strJson).getAsJsonObject();
        if (!json.get("result").getAsString().equals("ok")) {
            logger.debug("validateResponse {}", json);
            String error = json.get("error").getAsString();
            if (StringUtils.isEmpty(error)) {
                error = strJson;
            }
            throw new ToonConnectionException(error);
        }
        return json;
    }

    public boolean testConnection() {
        Response response = toonTarget.path("happ_thermstat").queryParam("action", new Object[] { "getThermostatInfo" })
                .request(new MediaType[] { MediaType.APPLICATION_JSON_TYPE }).get();
        return (response.getStatus() == 200);
    }

    public void setProgramEnabled(boolean wantedValue) throws ToonConnectionException {
        Response response = toonTarget.path("happ_thermstat").queryParam("action", new Object[] { "changeSchemeState" })
                .queryParam("state", new Object[] { wantedValue ? "1" : "0" })
                .request(new MediaType[] { MediaType.APPLICATION_JSON_TYPE }).get();
        validateResponse(response);
    }
}