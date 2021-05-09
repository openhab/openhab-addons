/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.juicenet.internal.api;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link JuiceNetApi} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class JuiceNetApi {
    private final Logger logger = LoggerFactory.getLogger(JuiceNetApi.class);

    private static final String API_HOST = "https://jbv1-api.emotorwerks.com/";
    private static final String API_ACCOUNT = API_HOST + "box_pin";
    private static final String API_DEVICE = API_HOST + "box_api_secure";

    protected String apiToken = "";
    protected JuiceNetHttp httpApi = new JuiceNetHttp();
    @Nullable
    protected ThingUID bridgeUID;

    public boolean initialize(String apiToken, ThingUID bridgeUID) throws JuiceNetApiException {
        this.apiToken = apiToken;
        this.bridgeUID = bridgeUID;

        httpApi = new JuiceNetHttp();
        logger.trace("JuiceNet API initialized");

        return true;
    } // initialize()

    public List<JuiceNetApiDevice> queryDeviceList() throws JuiceNetApiException, IOException, InterruptedException {
        Map<String, Object> params = new HashMap<>();
        HttpResponse<String> response;

        params.put("cmd", "get_account_units");
        params.put("device_id", bridgeUID.getAsString()); // bridgeUID.toString());
        params.put("account_token", apiToken);

        response = httpApi.httpPost(API_ACCOUNT, params);

        if (response.statusCode() != 200) {
            throw new JuiceNetApiException("Unable to retrieve device list, please check configuration.");
        }

        logger.trace("{}", response.body());

        JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
        boolean success = jsonResponse.get("success").getAsBoolean();

        if (!success) {
            throw new JuiceNetApiException("getDevices from JuiceNet failed, please check configuration.");
        }

        final JuiceNetApiDevice[] listDevices = new Gson().fromJson(jsonResponse.get("units").getAsJsonArray(),
                JuiceNetApiDevice[].class);

        return Arrays.asList(listDevices);
    }

    public JuiceNetApiDeviceStatus queryDeviceStatus(String token)
            throws JuiceNetApiException, IOException, InterruptedException {

        Map<String, Object> params = new HashMap<>();
        HttpResponse<String> response;

        params.put("cmd", "get_state");
        params.put("account_token", apiToken);
        params.put("device_id", bridgeUID.getAsString());
        params.put("token", token);

        response = httpApi.httpPost(API_DEVICE, params);

        if (response.statusCode() != 200) {
            throw new JuiceNetApiException("Unable to retrieve device status, please check configuration.");
        }

        logger.trace("{}", response.body());

        JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
        boolean success = jsonResponse.get("success").getAsBoolean();

        if (!success) {
            throw new JuiceNetApiException("getDeviceStatus from JuiceNet failed, please check configuration.");
        }

        final JuiceNetApiDeviceStatus deviceStatus = new Gson().fromJson(jsonResponse, JuiceNetApiDeviceStatus.class);

        return Objects.requireNonNull(deviceStatus);
    }

    public JuiceNetApiInfo queryInfo(String token) throws IOException, InterruptedException, JuiceNetApiException {
        Map<String, Object> params = new HashMap<>();
        HttpResponse<String> response;

        params.put("cmd", "get_info");
        params.put("account_token", apiToken);
        params.put("device_id", bridgeUID.getAsString());
        params.put("token", token);

        response = httpApi.httpPost(API_DEVICE, params);

        if (response.statusCode() != 200) {
            throw new JuiceNetApiException("Unable to retrieve device status, please check configuration.");
        }

        logger.trace("{}", response.body());

        final JuiceNetApiInfo info = new Gson().fromJson(response.body(), JuiceNetApiInfo.class);

        return Objects.requireNonNull(info);
    }

    public JuiceNetApiTouSchedule queryTOUSchedule(String token)
            throws IOException, InterruptedException, JuiceNetApiException {
        Map<String, Object> params = new HashMap<>();
        HttpResponse<String> response;

        params.put("cmd", "get_schedule");
        params.put("account_token", apiToken);
        params.put("device_id", bridgeUID.getAsString());
        params.put("token", token);

        response = httpApi.httpPost(API_DEVICE, params);

        if (response.statusCode() != 200) {
            throw new JuiceNetApiException("Unable to retrieve device TOU schedule, please check configuration.");
        }

        logger.trace("{}", response.body());

        JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
        boolean success = jsonResponse.get("success").getAsBoolean();

        if (!success) {
            throw new JuiceNetApiException("get_schedule from JuiceNet failed, please check configuration.");
        }

        final JuiceNetApiTouSchedule deviceTouSchedule = new Gson().fromJson(jsonResponse,
                JuiceNetApiTouSchedule.class);

        return Objects.requireNonNull(deviceTouSchedule);
    }

    public void setOverride(String token, int energy_at_plugin, Long override_time, int energy_to_add)
            throws IOException, InterruptedException, JuiceNetApiException {
        Map<String, Object> params = new HashMap<>();
        HttpResponse<String> response;

        params.put("cmd", "set_override");
        params.put("account_token", apiToken);
        params.put("device_id", bridgeUID.getAsString());
        params.put("token", token);
        params.put("energy_at_plugin", energy_at_plugin);
        params.put("override_time", override_time);
        params.put("energy_to_add", energy_to_add);

        response = httpApi.httpPost(API_DEVICE, params);

        if (response.statusCode() != 200) {
            throw new JuiceNetApiException("Unable to setOverride, please check configuration.");
        }

        logger.trace("{}", response.body());
    }

    public void setCurrentLimit(String token, int limit)
            throws IOException, InterruptedException, JuiceNetApiException {
        Map<String, Object> params = new HashMap<>();
        HttpResponse<String> response;

        params.put("cmd", "set_limit");
        params.put("account_token", apiToken);
        params.put("device_id", bridgeUID.getAsString());
        params.put("token", token);
        params.put("amperage", limit);

        response = httpApi.httpPost(API_DEVICE, params);

        if (response.statusCode() != 200) {
            throw new JuiceNetApiException("Unable to setOverride, please check configuration.");
        }

        logger.trace("{}", response.body());
    }

    public static class JuiceNetApiDevice {
        public String name = "";
        public String token = "";
        public String unit_id = "";
    }

    public static class JuiceNetApiDeviceStatus {
        public String ID = "";
        public Long info_timestamp = (long) 0;
        public boolean show_override;
        public String state = "";
        public JuiceNetApiDeviceChargingStatus charging = new JuiceNetApiDeviceChargingStatus();
        public JuiceNetApiDeviceLifetimeStatus lifetime = new JuiceNetApiDeviceLifetimeStatus();
        public int charging_time_left;
        public Long plug_unplug_time = (long) 0;
        public Long target_time = (long) 0;
        public Long unit_time = (long) 0;
        public int car_id;
        public int temperature;
    }

    public static class JuiceNetApiDeviceChargingStatus {
        public int amps_limit;
        public float amps_current;
        public int voltage;
        public int wh_energy;
        public int savings;
        public int watt_power;
        public int seconds_charging;
        public int wh_energy_at_plugin;
        public int wh_energy_to_add;
        public int flags;
    }

    public static class JuiceNetApiDeviceLifetimeStatus {
        public int wh_energy;
        public int savings;
    }

    public static class JuiceNetApiInfo {
        public String name = "";
        public String address = "";
        public String city = "";
        public String zip = "";
        public String country_code = "";
        public String ip = "";
        public int gascost;
        public int mpg;
        public int ecost;
        public int whpermile;
        public String timeZoneId = "";
        public int amps_wire_rating;
        public int amps_unit_rating;
        public JuiceNetApiCar[] cars = {};
    }

    public static class JuiceNetApiCar {
        public int car_id;
        public String description = "";
        public int battery_size_wh;
        public int battery_range_m;
        public int charging_rate_w;
        public String model_id = "";
    }

    public static class JuiceNetApiTouSchedule {
        public String type = "";
        public JuiceNetApiTouDay weekday = new JuiceNetApiTouDay();
        public JuiceNetApiTouDay weenend = new JuiceNetApiTouDay();
    }

    public static class JuiceNetApiTouDay {
        public int start;
        public int end;
        public int car_ready_by;
    }
}
