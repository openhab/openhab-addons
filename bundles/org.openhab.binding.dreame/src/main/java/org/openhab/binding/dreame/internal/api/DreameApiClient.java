/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.dreame.internal.api;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.dreame.internal.model.DreameAction;
import org.openhab.binding.dreame.internal.model.DreameDevice;
import org.openhab.binding.dreame.internal.model.DreameMapData;
import org.openhab.binding.dreame.internal.model.DreameMowingStatistics;
import org.openhab.binding.dreame.internal.model.DreameMqttConfiguration;
import org.openhab.binding.dreame.internal.model.DreameProperty;
import org.openhab.binding.dreame.internal.model.DreameStatus;
import org.openhab.binding.dreame.internal.util.DreameDiagnostics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Implements Dreamehome OAuth login and account device discovery.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class DreameApiClient implements DreameMowerApi {
    private static final BigDecimal MIN_CUTTING_HEIGHT = new BigDecimal("3.0");
    private static final BigDecimal MAX_CUTTING_HEIGHT = new BigDecimal("7.0");
    private static final BigDecimal CUTTING_HEIGHT_STEP = new BigDecimal("0.5");
    private static final int LEGACY_PREFERENCE_LENGTH = 16;
    private final Logger logger = LoggerFactory.getLogger(DreameApiClient.class);
    private static final String LOGIN_PATH = "/dreame-auth/oauth/token";
    private static final String DEVICES_PATH = "/dreame-user-iot/iotuserbind/device/listV2";
    private static final String HISTORY_PATH = "/dreame-user-iot/iotstatus/history";
    private static final String DEVICE_DATA_PATH = "/dreame-user-iot/iotuserdata/getDeviceData";
    private static final String COMMAND_PATH_PREFIX = "/dreame-iot-com";
    private final DreameAuthenticationService authentication;
    private final DreameHttpTransport transport;
    private final DreameApiResponseParser responseParser = new DreameApiResponseParser();
    private final Gson gson = new Gson();
    private final AtomicInteger requestId = new AtomicInteger(1);

    public DreameApiClient(HttpClient httpClient) {
        this(httpClient, Clock.systemUTC());
    }

    DreameApiClient(HttpClient httpClient, Clock clock) {
        authentication = new DreameAuthenticationService(clock);
        transport = new DreameHttpTransport(httpClient, authentication);
    }

    public synchronized void login(String username, String password, String country) throws DreameCloudException {
        login(username, password, country, DreameCloudService.DREAMEHOME);
    }

    @Override
    public synchronized void login(String username, String password, String country, DreameCloudService cloudService)
            throws DreameCloudException {
        authentication.login(username, password, country, cloudService,
                body -> transport.post(LOGIN_PATH, body, false));
    }

    public synchronized List<DreameDevice> getDevices() throws DreameCloudException {
        ensureAuthenticated();
        JsonObject response = transport.post(DEVICES_PATH, null, true);
        assertSuccess(response, "Device query failed");
        return responseParser.parseDevices(response);
    }

    public synchronized DreameStatus getProperties(DreameDevice device, List<DreameProperty> properties)
            throws DreameCloudException {
        JsonArray parameters = new JsonArray();
        for (DreameProperty property : properties) {
            JsonObject parameter = new JsonObject();
            parameter.addProperty("did", Integer.toString(property.id()));
            parameter.addProperty("siid", property.serviceId());
            parameter.addProperty("piid", property.propertyId());
            parameters.add(parameter);
        }
        JsonElement result = sendCommand(device, "get_properties", parameters);
        if (!result.isJsonArray()) {
            throw new DreameCloudException("Cloud service returned invalid property data");
        }
        return responseParser.parseProperties(result.getAsJsonArray(), properties);
    }

    public synchronized DreameMowingStatistics getMowingStatistics(DreameDevice device) throws DreameCloudException {
        ensureAuthenticated();
        JsonObject request = new JsonObject();
        request.addProperty("uid", defaultIfBlank(device.masterUid(), authentication.userId()));
        request.addProperty("did", device.id());
        request.addProperty("from", 1687019188);
        request.addProperty("limit", 500);
        request.addProperty("siid", 4);
        request.addProperty("region", authentication.country());
        request.addProperty("type", 3);
        request.addProperty("eiid", 1);

        JsonObject response = transport.post(HISTORY_PATH, gson.toJson(request), true);
        assertSuccess(response, "Cloud history query failed");
        return responseParser.parseMowingStatistics(response);
    }

    public synchronized DreameMapData getMapData(DreameDevice device) throws DreameCloudException {
        ensureAuthenticated();
        JsonObject request = new JsonObject();
        request.addProperty("did", device.id());
        request.add("model", new JsonArray());
        JsonObject response = transport.post(DEVICE_DATA_PATH, gson.toJson(request), true);
        assertSuccess(response, "Cloud map query failed");

        JsonObject action = new JsonObject();
        action.addProperty("did", device.id());
        action.addProperty("siid", 2);
        action.addProperty("aiid", 50);
        JsonObject getter = new JsonObject();
        getter.addProperty("m", "g");
        getter.addProperty("t", "MAPL");
        JsonArray input = new JsonArray();
        input.add(getter);
        action.add("in", input);
        JsonElement mapListResult = sendCommand(device, "action", action);
        return responseParser.parseMapData(objectValue(response, "data"), mapListResult);
    }

    public synchronized void setProperty(DreameDevice device, DreameProperty property, boolean value)
            throws DreameCloudException {
        JsonObject parameter = new JsonObject();
        parameter.addProperty("did", device.id());
        parameter.addProperty("siid", property.serviceId());
        parameter.addProperty("piid", property.propertyId());
        parameter.addProperty("value", value);
        JsonArray parameters = new JsonArray();
        parameters.add(parameter);
        sendCommand(device, "set_properties", parameters);
    }

    public synchronized void callAction(DreameDevice device, DreameAction action) throws DreameCloudException {
        JsonObject parameters = new JsonObject();
        parameters.addProperty("did", device.id());
        parameters.addProperty("siid", action.serviceId());
        parameters.addProperty("aiid", action.actionId());
        parameters.add("in", new JsonArray());
        sendCommand(device, "action", parameters);
    }

    public synchronized void startZoneMowing(DreameDevice device, List<Integer> zoneIds) throws DreameCloudException {
        JsonArray regions = new JsonArray();
        zoneIds.forEach(regions::add);
        JsonObject data = new JsonObject();
        data.add("region", regions);
        JsonObject task = new JsonObject();
        task.addProperty("m", "a");
        task.addProperty("p", 0);
        task.addProperty("o", 102);
        task.add("d", data);
        JsonArray input = new JsonArray();
        input.add(task);
        JsonObject parameters = new JsonObject();
        parameters.addProperty("did", device.id());
        parameters.addProperty("siid", 2);
        parameters.addProperty("aiid", 50);
        parameters.add("in", input);
        JsonElement result = sendCommand(device, "action", parameters);
        if (result instanceof JsonObject object && object.has("code") && object.get("code").getAsInt() != 0) {
            throw new DreameCloudException("Cloud service rejected zone mowing");
        }
    }

    public synchronized BigDecimal getCuttingHeight(DreameDevice device, int mapIndex) throws DreameCloudException {
        JsonArray record = getMowingPreference(device, mapIndex);
        if (record.size() <= 4) {
            throw new DreameCloudException("Cloud service returned no cutting height");
        }
        return BigDecimal.valueOf(record.get(4).getAsInt(), 1);
    }

    public synchronized void setCuttingHeight(DreameDevice device, int mapIndex, BigDecimal height)
            throws DreameCloudException {
        JsonArray record = updatedCuttingHeightRecord(getMowingPreference(device, mapIndex), mapIndex, height);
        int status = setMowingPreference(device, record);
        if (status == -3 && record.size() > LEGACY_PREFERENCE_LENGTH) {
            JsonArray legacyRecord = new JsonArray();
            for (int index = 0; index < LEGACY_PREFERENCE_LENGTH; index++) {
                legacyRecord.add(record.get(index));
            }
            status = setMowingPreference(device, legacyRecord);
        }
        if (status != 0) {
            throw new DreameCloudException("Cloud service rejected cutting height with status " + status);
        }
    }

    private JsonArray getMowingPreference(DreameDevice device, int mapIndex) throws DreameCloudException {
        JsonObject data = new JsonObject();
        data.addProperty("idx", mapIndex);
        data.addProperty("region", 0);
        JsonObject getter = new JsonObject();
        getter.addProperty("m", "g");
        getter.addProperty("t", "PRE");
        getter.add("d", data);
        JsonArray input = new JsonArray();
        input.add(getter);
        JsonObject parameters = new JsonObject();
        parameters.addProperty("did", device.id());
        parameters.addProperty("siid", 2);
        parameters.addProperty("aiid", 50);
        parameters.add("in", input);
        return parseMowingPreference(sendCommand(device, "action", parameters));
    }

    static BigDecimal parseCuttingHeight(JsonElement result) throws DreameCloudException {
        JsonArray record = parseMowingPreference(result);
        if (record.size() <= 4) {
            throw new DreameCloudException("Cloud service returned no cutting height");
        }
        return BigDecimal.valueOf(record.get(4).getAsInt(), 1);
    }

    private static JsonArray parseMowingPreference(JsonElement result) throws DreameCloudException {
        if (!(result instanceof JsonObject object) || object.has("code") && object.get("code").getAsInt() != 0
                || !(object.get("out") instanceof JsonArray output)) {
            throw new DreameCloudException("Cloud service returned invalid mowing preferences");
        }
        for (JsonElement element : output) {
            if (element instanceof JsonObject entry && (!entry.has("r") || entry.get("r").getAsInt() == 0)
                    && entry.get("d") instanceof JsonArray record && record.size() > 4) {
                return record.deepCopy();
            }
        }
        throw new DreameCloudException("Cloud service returned no cutting height");
    }

    static JsonArray updatedCuttingHeightRecord(JsonArray source, int mapIndex, BigDecimal height)
            throws DreameCloudException {
        if (height.compareTo(MIN_CUTTING_HEIGHT) < 0 || height.compareTo(MAX_CUTTING_HEIGHT) > 0
                || height.remainder(CUTTING_HEIGHT_STEP).compareTo(BigDecimal.ZERO) != 0) {
            throw new DreameCloudException("Cutting height must be between 3 and 7 cm in 0.5 cm steps");
        }
        if (source.size() <= 4) {
            throw new DreameCloudException("Cloud service returned invalid mowing preferences");
        }
        JsonArray updated = source.deepCopy();
        updated.set(0, new com.google.gson.JsonPrimitive(0));
        updated.set(1, new com.google.gson.JsonPrimitive(mapIndex));
        updated.set(2, new com.google.gson.JsonPrimitive(0));
        updated.set(4, new com.google.gson.JsonPrimitive(height.movePointRight(1).intValueExact()));
        return updated;
    }

    private int setMowingPreference(DreameDevice device, JsonArray record) throws DreameCloudException {
        JsonObject setter = new JsonObject();
        setter.addProperty("m", "s");
        setter.addProperty("t", "PRE");
        setter.add("d", record);
        JsonArray input = new JsonArray();
        input.add(setter);
        JsonObject parameters = new JsonObject();
        parameters.addProperty("did", device.id());
        parameters.addProperty("siid", 2);
        parameters.addProperty("aiid", 50);
        parameters.add("in", input);
        JsonElement result = sendCommand(device, "action", parameters);
        if (result instanceof JsonObject object && object.get("out") instanceof JsonArray output) {
            for (JsonElement element : output) {
                if (element instanceof JsonObject entry && entry.has("r")) {
                    return entry.get("r").getAsInt();
                }
            }
        }
        throw new DreameCloudException("Cloud service returned invalid cutting-height confirmation");
    }

    public synchronized void logout() {
        authentication.logout();
    }

    private void ensureAuthenticated() throws DreameCloudException {
        authentication.ensureAuthenticated(body -> transport.post(LOGIN_PATH, body, false));
    }

    private JsonElement sendCommand(DreameDevice device, String method, JsonElement parameters)
            throws DreameCloudException {
        ensureAuthenticated();
        int id = requestId.incrementAndGet();
        JsonObject data = new JsonObject();
        data.addProperty("did", device.id());
        data.addProperty("id", id);
        data.addProperty("method", method);
        data.add("params", parameters);
        data.addProperty("from", "openHAB");

        JsonObject request = new JsonObject();
        request.addProperty("did", device.id());
        request.addProperty("id", id);
        request.add("data", data);

        logger.debug("Sending cloud method {} with request id {} to device {}", method, id,
                DreameDiagnostics.maskIdentifier(device.id()));

        JsonObject response = transport.post(commandPath(device), gson.toJson(request), true);
        assertSuccess(response, "Cloud command failed");
        JsonObject responseData = objectValue(response, "data");
        JsonElement result = responseData.get("result");
        if (result == null || result.isJsonNull()) {
            throw new DreameCloudException("Cloud command returned no result");
        }
        return result;
    }

    public synchronized DreameMqttConfiguration mqttConfiguration(DreameDevice device) throws DreameCloudException {
        ensureAuthenticated();
        if (authentication.userId().isBlank() || device.masterUid().isBlank() || device.bindDomain().isBlank()) {
            throw new DreameCloudException("Cloud service did not provide MQTT connection data");
        }
        int separator = device.bindDomain().lastIndexOf(':');
        if (separator < 1 || separator == device.bindDomain().length() - 1) {
            throw new DreameCloudException("Cloud service returned an invalid MQTT endpoint");
        }
        String host = device.bindDomain().substring(0, separator);
        int port;
        try {
            port = Integer.parseInt(device.bindDomain().substring(separator + 1));
        } catch (NumberFormatException e) {
            throw new DreameCloudException("Cloud service returned an invalid MQTT port", e);
        }
        String agent = Integer.toUnsignedString(requestId.incrementAndGet());
        String clientId = "p_" + device.masterUid() + "_" + agent + "_" + host;
        String topic = "/status/" + device.id() + "/" + device.masterUid() + "/" + device.model() + "/"
                + authentication.country() + "/";
        return new DreameMqttConfiguration(host, port, clientId, authentication.userId(), authentication.accessToken(),
                topic);
    }

    static String commandPath(DreameDevice device) {
        String host = device.bindDomain();
        String suffix = host.isBlank() ? "" : "-" + host.split("\\.", 2)[0];
        return COMMAND_PATH_PREFIX + suffix + "/device/sendCommand";
    }

    private static void assertSuccess(JsonObject response, String message) throws DreameCloudException {
        if (!response.has("code") || response.get("code").getAsInt() != 0) {
            throw new DreameCloudException(message);
        }
    }

    private static JsonObject objectValue(JsonObject object, String name) throws DreameCloudException {
        if (object.has(name) && object.get(name) instanceof JsonObject value) {
            return value;
        }
        throw new DreameCloudException("Cloud response is missing " + name);
    }

    private static String defaultIfBlank(String value, String fallback) {
        return value.isBlank() ? fallback : value;
    }
}
