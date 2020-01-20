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

package org.openhab.binding.rootedtoon.internal.client;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
            return this.toonState;
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
        Response response = this.toonTarget.path("happ_thermstat")
                .queryParam("action", new Object[] { "changeSchemeState" }).queryParam("state", new Object[] { "2" })
                .queryParam("temperatureState", new Object[] { Integer.valueOf(state.toonState()) })
                .request(new MediaType[] { MediaType.APPLICATION_JSON_TYPE }).get();
        validateResponse(response);
    }

    public static void main(String[] args) throws ToonConnectionException {
        ToonClient client = new ToonClient("http://toon");

        client.addListener(new ToonClientEventListener() {
            @Override
            public void newThermostatInfo(ThermostatInfo thermostatInfo) {
                System.out.println(thermostatInfo);
            }

            @Override
            public void newRealtimeUsageInfo(RealtimeUsageInfo usageInfo) {
                System.out.println(usageInfo);
            }

            @Override
            public void newPowerUsageInfo(PowerUsageInfo puInfo) {
                System.out.println(puInfo);
            }
        });
        client.requestThermostatInfo();
        client.requestRealtimeUsageInfo();
        client.requestPowerUsageInfo();
        client.setRoomSetpoint(20.0D);
    }

    public void setRoomSetpoint(double temperature) throws ToonConnectionException {
        double sanitized = Math.min(30.0D, Math.max(10.0D, Math.round(temperature * 2.0D) / 2.0D));
        logger.debug("Setting temperature to " + temperature);
        Response response = this.toonTarget.path("happ_thermstat").queryParam("action", new Object[] { "roomSetpoint" })
                .queryParam("Setpoint", new Object[] { Integer.valueOf((int) (sanitized * 100.0D)) })
                .request(new MediaType[] { MediaType.APPLICATION_JSON_TYPE }).get();
        validateResponse(response);
    }

    public ToonClient(String url) {
        this.listeners = new LinkedList<>();
        this.toonTarget = this.client.target(url);
    }

    public void addListener(ToonClientEventListener listener) {
        this.listeners.add(listener);
    }

    public void requestThermostatInfo() throws ToonConnectionException {
        Response response = this.toonTarget.path("happ_thermstat")
                .queryParam("action", new Object[] { "getThermostatInfo" })
                .request(new MediaType[] { MediaType.APPLICATION_JSON_TYPE }).get();

        JsonObject json = validateResponse(response);
        logger.debug("toon state: {}", json);
        ThermostatInfo result = this.gson.fromJson((JsonElement) json, ThermostatInfo.class);

        result.currentTemp /= 100.0D;
        result.currentSetpoint /= 100.0D;
        result.nextSetpoint /= 100.0D;

        for (ToonClientEventListener listener : this.listeners) {
            listener.newThermostatInfo(result);
        }
    }

    public void requestPowerUsageInfo() throws ToonConnectionException {
        Response response = this.toonTarget.path("happ_pwrusage")
                .queryParam("action", new Object[] { "GetCurrentUsage" })
                .request(new MediaType[] { MediaType.APPLICATION_JSON_TYPE }).get();

        JsonObject json = validateResponse(response);
        logger.debug("toon state: {}", json);
        PowerUsageInfo result = this.gson.fromJson((JsonElement) json, PowerUsageInfo.class);

        for (ToonClientEventListener listener : this.listeners) {
            listener.newPowerUsageInfo(result);
        }
    }

    public void requestRealtimeUsageInfo() throws ToonConnectionException {
        Response response = this.toonTarget.path("hdrv_zwave").queryParam("action", new Object[] { "getDevices.json" })
                .request(new MediaType[] { MediaType.APPLICATION_JSON_TYPE }).get();

        try {
            String strJson = response.readEntity(String.class);
            JsonObject json = this.jsonParser.parse(strJson).getAsJsonObject();

            RealtimeUsageInfo result = new RealtimeUsageInfo();

            for (Map.Entry<String, JsonElement> elm : (Iterable<Map.Entry<String, JsonElement>>) json.entrySet()) {
                JsonObject dev = elm.getValue().getAsJsonObject();

                try {
                    String str;
                    switch ((str = dev.get("type").getAsString()).hashCode()) {
                        case -1646462228:
                            if (!str.equals("elec_received_lt")) {
                                continue;
                            }

                            result.elec_received_lt = new RatedValue(
                                    Double.valueOf(dev.get("CurrentElectricityFlow").getAsDouble()),
                                    Double.valueOf(dev.get("CurrentElectricityQuantity").getAsDouble()));
                        case -1646462166:
                            if (!str.equals("elec_received_nt")) {
                                continue;
                            }
                            result.elec_received_nt = new RatedValue(
                                    Double.valueOf(dev.get("CurrentElectricityFlow").getAsDouble()),
                                    Double.valueOf(dev.get("CurrentElectricityQuantity").getAsDouble()));
                        case 102105:
                            if (!str.equals("gas")) {
                                continue;
                            }
                            result.gas = new RatedValue(Double.valueOf(dev.get("CurrentGasFlow").getAsDouble()),
                                    Double.valueOf(dev.get("CurrentGasQuantity").getAsDouble()));
                        case 3115909:
                            if (!str.equals("elec")) {
                                continue;
                            }
                            result.elec = new RatedValue(
                                    Double.valueOf(dev.get("CurrentElectricityFlow").getAsDouble()),
                                    Double.valueOf(dev.get("CurrentElectricityQuantity").getAsDouble()));
                        case 3198448:
                            if (!str.equals("heat")) {
                                continue;
                            }
                            result.heat = new RatedValue(Double.valueOf(dev.get("CurrentHeatQuantity").getAsDouble()),
                                    null);
                        case 123542663:
                            if (!str.equals("elec_solar")) {
                                continue;
                            }
                            result.elec_solar = new RatedValue(
                                    Double.valueOf(dev.get("CurrentElectricityFlow").getAsDouble()),
                                    Double.valueOf(dev.get("CurrentElectricityQuantity").getAsDouble()));
                        case 1930858909:
                            if (!str.equals("elec_delivered_lt")) {
                                continue;
                            }
                            result.elec_delivered_lt = new RatedValue(
                                    Double.valueOf(dev.get("CurrentElectricityFlow").getAsDouble()),
                                    Double.valueOf(dev.get("CurrentElectricityQuantity").getAsDouble()));
                        case 1930858971:
                            if (!str.equals("elec_delivered_nt")) {
                                continue;
                            }
                            result.elec_delivered_nt = new RatedValue(
                                    Double.valueOf(dev.get("CurrentElectricityFlow").getAsDouble()),
                                    Double.valueOf(dev.get("CurrentElectricityQuantity").getAsDouble()));
                    }
                } catch (Exception exception) {
                }
            }

            for (ToonClientEventListener listener : this.listeners) {
                listener.newRealtimeUsageInfo(result);
            }
        } catch (Exception e) {
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

        JsonObject json = this.jsonParser.parse(strJson).getAsJsonObject();
        if (!json.get("result").getAsString().equals("ok")) {
            logger.debug("validateResponse {}", json);
            throw new ToonConnectionException(json.get("error").getAsString());
        }
        return json;
    }

    public boolean testConnection() {
        Response response = this.toonTarget.path("happ_thermstat")
                .queryParam("action", new Object[] { "getThermostatInfo" })
                .request(new MediaType[] { MediaType.APPLICATION_JSON_TYPE }).get();
        return (response.getStatus() == 200);
    }

    public void setProgramEnabled(boolean wantedValue) throws ToonConnectionException {
        Response response = this.toonTarget.path("happ_thermstat")
                .queryParam("action", new Object[] { "changeSchemeState" })
                .queryParam("state", new Object[] { wantedValue ? "1" : "0" })
                .request(new MediaType[] { MediaType.APPLICATION_JSON_TYPE }).get();
        validateResponse(response);
    }
}