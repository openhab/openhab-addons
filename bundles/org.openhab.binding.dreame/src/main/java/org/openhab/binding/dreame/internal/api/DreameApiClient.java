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
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.dreame.internal.model.DreameAction;
import org.openhab.binding.dreame.internal.model.DreameDevice;
import org.openhab.binding.dreame.internal.model.DreameMapData;
import org.openhab.binding.dreame.internal.model.DreameMowingStatistics;
import org.openhab.binding.dreame.internal.model.DreameMqttConfiguration;
import org.openhab.binding.dreame.internal.model.DreameProperty;
import org.openhab.binding.dreame.internal.model.DreameStatus;
import org.openhab.binding.dreame.internal.model.DreameVacuumAction;
import org.openhab.binding.dreame.internal.model.DreameVacuumCapabilities;
import org.openhab.binding.dreame.internal.model.DreameVacuumProperties;
import org.openhab.binding.dreame.internal.model.DreameVacuumSetting;
import org.openhab.binding.dreame.internal.util.DreameDiagnostics;
import org.openhab.binding.dreame.internal.util.DreameVacuumDiagnostics;
import org.openhab.binding.dreame.internal.util.DreameVacuumMapImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

/**
 * Implements Dreamehome OAuth login and account device discovery.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class DreameApiClient implements DreameMowerApi, DreameVacuumApi {
    private static final BigDecimal MIN_CUTTING_HEIGHT = new BigDecimal("3.0");
    private static final BigDecimal MAX_CUTTING_HEIGHT = new BigDecimal("7.0");
    private static final BigDecimal CUTTING_HEIGHT_STEP = new BigDecimal("0.5");
    private static final int LEGACY_PREFERENCE_LENGTH = 16;
    private final Logger logger = LoggerFactory.getLogger(DreameApiClient.class);
    private static final String LOGIN_PATH = "/dreame-auth/oauth/token";
    private static final String DEVICES_PATH = "/dreame-user-iot/iotuserbind/device/listV2";
    private static final String HISTORY_PATH = "/dreame-user-iot/iotstatus/history";
    private static final String DEVICE_DATA_PATH = "/dreame-user-iot/iotuserdata/getDeviceData";
    private static final String FILE_DOWNLOAD_PATH = "/dreame-user-iot/iotfile/getDownloadUrl";
    private static final String COMMAND_PATH_PREFIX = "/dreame-iot-com";
    private final DreameAuthenticationService authentication;
    private final DreameHttpTransport transport;
    private final DreameApiResponseParser responseParser = new DreameApiResponseParser();
    private final Gson gson = new Gson();
    private final AtomicInteger requestId = new AtomicInteger(1);
    private final Map<String, Integer> vacuumCleaningModes = new HashMap<>();

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
        setProperty(device, property, new JsonPrimitive(value));
    }

    @Override
    public synchronized void setDnd(DreameDevice device, boolean enabled, @Nullable String taskConfiguration)
            throws DreameCloudException {
        if ("mova.mower.g2584d".equals(device.model())) {
            throw new DreameCloudException("Do not disturb commands are not yet supported for this mower model");
        }
        if (taskConfiguration == null) {
            setProperty(device, DreameProperty.DND, enabled);
            return;
        }
        JsonElement parsed;
        try {
            parsed = JsonParser.parseString(taskConfiguration);
        } catch (RuntimeException e) {
            throw new DreameCloudException("Invalid do not disturb task configuration", e);
        }
        if (!(parsed instanceof JsonArray tasks) || tasks.isEmpty() || !tasks.get(0).isJsonObject()) {
            throw new DreameCloudException("Unsupported do not disturb task configuration");
        }
        tasks.get(0).getAsJsonObject().addProperty("en", enabled);
        setProperty(device, DreameProperty.DND_TASK, new JsonPrimitive(gson.toJson(tasks)));
    }

    private void setProperty(DreameDevice device, DreameProperty property, JsonElement value)
            throws DreameCloudException {
        JsonObject parameter = new JsonObject();
        parameter.addProperty("did", device.id());
        parameter.addProperty("siid", property.serviceId());
        parameter.addProperty("piid", property.propertyId());
        parameter.add("value", value);
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

    @Override
    public synchronized DreameVacuumProperties getVacuumProperties(DreameDevice device, BooleanSupplier isCurrent)
            throws DreameCloudException {
        if (!DreameVacuumCapabilities.isSupported(device)) {
            throw new DreameCloudException("Vacuum properties are not supported for this model");
        }
        // Correlation IDs and addresses follow DreameVacuumProperty in the vacuum reference implementation.
        int[][] properties = { { 0, 2, 1 }, { 1, 2, 2 }, { 2, 3, 1 }, { 3, 3, 2 }, { 4, 4, 1 }, { 5, 4, 2 },
                { 6, 4, 3 }, { 7, 4, 4 }, { 8, 4, 5 }, { 9, 4, 7 }, { 10, 4, 23 }, { 11, 4, 25 }, { 12, 4, 40 },
                { 13, 4, 50 }, { 14, 9, 1 }, { 15, 9, 2 }, { 16, 10, 1 }, { 17, 10, 2 }, { 18, 11, 1 }, { 19, 11, 2 },
                { 20, 12, 2 }, { 21, 12, 3 }, { 22, 12, 4 }, { 23, 16, 1 }, { 24, 16, 2 }, { 25, 18, 1 }, { 26, 18, 2 },
                { 27, 20, 1 }, { 28, 20, 2 }, { 29, 15, 3 } };
        JsonArray parameters = new JsonArray();
        for (int[] property : properties) {
            JsonObject parameter = new JsonObject();
            parameter.addProperty("did", Integer.toString(property[0]));
            parameter.addProperty("siid", property[1]);
            parameter.addProperty("piid", property[2]);
            parameters.add(parameter);
        }
        JsonElement result = sendCommand(device, "get_properties", parameters, isCurrent);
        if (!(result instanceof JsonArray values)) {
            throw new DreameCloudException("Vacuum returned invalid property data");
        }
        Map<String, Integer> propertiesByAddress = DreameVacuumDiagnostics.readPropertyResults(values);
        Map<String, String> textProperties = DreameVacuumDiagnostics.readTextPropertyResults(values);
        if (propertiesByAddress.isEmpty() && textProperties.isEmpty()) {
            throw new DreameCloudException("Vacuum returned no valid status properties");
        }
        Integer cleaningMode = propertiesByAddress.get("4/23");
        if (cleaningMode != null) {
            vacuumCleaningModes.put(device.id(), cleaningMode);
        }
        return new DreameVacuumProperties(propertiesByAddress, textProperties);
    }

    @Override
    public synchronized @Nullable String getVacuumMapListObjectName(DreameDevice device, BooleanSupplier isCurrent)
            throws DreameCloudException {
        if (!DreameVacuumCapabilities.isSupported(device)) {
            throw new DreameCloudException("Vacuum map list is not supported for this model");
        }
        JsonObject parameter = new JsonObject();
        parameter.addProperty("did", "12");
        parameter.addProperty("siid", 6);
        parameter.addProperty("piid", 8);
        JsonArray parameters = new JsonArray();
        parameters.add(parameter);
        JsonElement result = sendCommand(device, "get_properties", parameters, isCurrent);
        if (!(result instanceof JsonArray values) || values.isEmpty()
                || !(values.get(0) instanceof JsonObject property)) {
            throw new DreameCloudException("Vacuum returned invalid map-list property data");
        }
        if (!property.has("code") || property.get("code").getAsInt() != 0
                || !(property.get("value") instanceof JsonPrimitive value) || !value.isString()) {
            return null;
        }
        return DreameVacuumMapImage.mapListObjectName(value.getAsString());
    }

    @Override
    public synchronized void callVacuumAction(DreameDevice device, DreameVacuumAction action, BooleanSupplier isCurrent)
            throws DreameCloudException {
        if (!DreameVacuumCapabilities.isSupported(device)) {
            throw new DreameCloudException("Vacuum commands are not supported for this model");
        }
        JsonObject parameters = new JsonObject();
        parameters.addProperty("did", device.id());
        parameters.addProperty("siid", action.serviceId());
        parameters.addProperty("aiid", action.actionId());
        JsonArray inputs = new JsonArray();
        String inputValue = action.inputValue();
        if (inputValue != null) {
            JsonObject input = new JsonObject();
            input.addProperty("piid", action.inputPropertyId());
            input.addProperty("value", inputValue);
            inputs.add(input);
        }
        parameters.add("in", inputs);
        JsonElement result = sendCommand(device, "action", parameters, isCurrent);
        if (!(result instanceof JsonObject object) || !(object.get("code") instanceof JsonPrimitive code)
                || !code.isNumber()) {
            throw new DreameCloudException("Vacuum command returned an invalid device result");
        }
        try {
            int value = code.getAsBigDecimal().intValueExact();
            if (value != 0) {
                logger.debug("Vacuum command {} rejected with code {}", action, value);
                throw new DreameCloudException("Vacuum command rejected with code " + value);
            }
        } catch (ArithmeticException | NumberFormatException e) {
            throw new DreameCloudException("Vacuum command returned an invalid result code");
        }
    }

    @Override
    public synchronized void setVacuumSetting(DreameDevice device, DreameVacuumSetting setting, int value,
            BooleanSupplier isCurrent) throws DreameCloudException {
        if (!DreameVacuumCapabilities.isSupported(device)) {
            throw new DreameCloudException("Vacuum settings are not supported for this model");
        }
        if (!isCurrent.getAsBoolean()) {
            throw new DreameCloudException("Vacuum setting cancelled before dispatch");
        }
        if (setting == DreameVacuumSetting.CLEANING_MODE) {
            int current = vacuumCleaningModes.getOrDefault(device.id(), 0x1400);
            int deviceMode = switch (value) {
                case 0 -> 2;
                case 1 -> 1;
                case 2 -> 0;
                default -> throw new DreameCloudException("Unsupported vacuum cleaning mode");
            };
            value = current & ~0x03 | deviceMode;
        }
        JsonObject property = new JsonObject();
        property.addProperty("did", device.id());
        property.addProperty("siid", setting.serviceId());
        property.addProperty("piid", setting.propertyId());
        String autoSwitchKey = setting.autoSwitchKey();
        if (autoSwitchKey == null) {
            property.addProperty("value", value);
        } else {
            JsonObject autoSwitch = new JsonObject();
            autoSwitch.addProperty("k", autoSwitchKey);
            autoSwitch.addProperty("v", value);
            property.addProperty("value", gson.toJson(autoSwitch));
        }
        JsonArray parameters = new JsonArray();
        parameters.add(property);
        JsonElement result = sendCommand(device, "set_properties", parameters, isCurrent);
        if (!(result instanceof JsonArray results) || results.size() != 1
                || !(results.get(0) instanceof JsonObject response)
                || !(response.get("code") instanceof JsonPrimitive code) || !code.isNumber()
                || !isExactInteger(code, 0)) {
            throw new DreameCloudException("Vacuum setting was not acknowledged");
        }
        if (setting == DreameVacuumSetting.CLEANING_MODE) {
            vacuumCleaningModes.put(device.id(), value);
        }
    }

    @Override
    public synchronized void cleanVacuumRooms(DreameDevice device, List<Integer> roomIds, int suctionLevel,
            int waterVolume, BooleanSupplier isCurrent) throws DreameCloudException {
        if (!DreameVacuumCapabilities.isSupported(device) || roomIds.isEmpty() || roomIds.size() > 32) {
            throw new DreameCloudException("Vacuum room cleaning is not supported for this request");
        }
        JsonArray selections = new JsonArray();
        for (int roomId : roomIds) {
            if (roomId < 1 || roomId > 63) {
                throw new DreameCloudException("Invalid vacuum room identifier");
            }
            JsonArray selection = new JsonArray();
            for (int value : new int[] { roomId, 1, suctionLevel, waterVolume, 1 }) {
                selection.add(value);
            }
            selections.add(selection);
        }
        JsonObject cleaning = new JsonObject();
        cleaning.add("selects", selections);
        JsonArray inputs = new JsonArray();
        JsonObject status = new JsonObject();
        status.addProperty("piid", 1);
        status.addProperty("value", 18);
        inputs.add(status);
        JsonObject properties = new JsonObject();
        properties.addProperty("piid", 10);
        properties.addProperty("value", gson.toJson(cleaning));
        inputs.add(properties);
        JsonObject parameters = new JsonObject();
        parameters.addProperty("did", device.id());
        parameters.addProperty("siid", 4);
        parameters.addProperty("aiid", 1);
        parameters.add("in", inputs);
        JsonElement result = sendCommand(device, "action", parameters, isCurrent);
        if (!(result instanceof JsonObject response) || !(response.get("code") instanceof JsonPrimitive code)
                || !code.isNumber() || !isExactInteger(code, 0)) {
            throw new DreameCloudException("Vacuum room cleaning was not acknowledged");
        }
    }

    @Override
    public synchronized @Nullable String getVacuumMap(DreameDevice device, BooleanSupplier isCurrent)
            throws DreameCloudException {
        if (!DreameVacuumCapabilities.isSupported(device)) {
            throw new DreameCloudException("Vacuum maps are not supported for this model");
        }
        JsonObject frameRequest = new JsonObject();
        frameRequest.addProperty("req_type", 1);
        frameRequest.addProperty("frame_type", "I");
        frameRequest.addProperty("force_type", 1);
        JsonObject input = new JsonObject();
        input.addProperty("piid", 2);
        input.addProperty("value", gson.toJson(frameRequest));
        JsonArray inputs = new JsonArray();
        inputs.add(input);
        JsonObject parameters = new JsonObject();
        parameters.addProperty("did", device.id());
        parameters.addProperty("siid", 6);
        parameters.addProperty("aiid", 1);
        parameters.add("in", inputs);
        JsonElement result = sendCommand(device, "action", parameters, isCurrent);
        if (!(result instanceof JsonObject object) || !(object.get("code") instanceof JsonPrimitive code)
                || !code.isNumber() || !(object.get("out") instanceof JsonArray output)) {
            throw new DreameCloudException("Vacuum map request returned an invalid result");
        }
        try {
            if (code.getAsBigDecimal().intValueExact() != 0) {
                throw new DreameCloudException("Vacuum map request was rejected");
            }
        } catch (ArithmeticException | NumberFormatException e) {
            throw new DreameCloudException("Vacuum map request returned an invalid result code");
        }
        String objectName = "";
        for (JsonElement element : output) {
            if (element instanceof JsonObject property && property.get("piid") instanceof JsonPrimitive piid
                    && piid.isNumber() && property.get("value") instanceof JsonPrimitive value && value.isString()) {
                String mapValue = value.getAsString();
                if (mapValue.isBlank()) {
                    continue;
                }
                if (isExactInteger(piid, 1)) {
                    return mapValue;
                }
                if (isExactInteger(piid, 3)) {
                    objectName = mapValue;
                } else if (isExactInteger(piid, 13)) {
                    String[] parts = mapValue.split(",", 3);
                    if (parts.length >= 2) {
                        if ("0".equals(parts[0])) {
                            return parts[1];
                        }
                        objectName = parts[1] + (parts.length == 3 ? "," + parts[2] : "");
                    }
                }
            }
        }
        return objectName.isBlank() ? null : downloadVacuumMap(device, objectName, isCurrent);
    }

    @Override
    public synchronized String getVacuumMapList(DreameDevice device, String objectName, BooleanSupplier isCurrent)
            throws DreameCloudException {
        if (!DreameVacuumCapabilities.isSupported(device)) {
            throw new DreameCloudException("Vacuum map lists are not supported for this model");
        }
        return downloadVacuumMap(device, objectName, isCurrent);
    }

    private String downloadVacuumMap(DreameDevice device, String objectName, BooleanSupplier isCurrent)
            throws DreameCloudException {
        validateMapObjectName(objectName);
        if (!isCurrent.getAsBoolean()) {
            throw new DreameCloudException("Map download cancelled before dispatch");
        }
        JsonObject request = new JsonObject();
        request.addProperty("did", device.id());
        request.addProperty("model", device.model());
        request.addProperty("filename", objectName);
        request.addProperty("region", authentication.country());
        JsonObject response = transport.post(FILE_DOWNLOAD_PATH, gson.toJson(request), true);
        assertSuccess(response, "Cloud map-file query failed");
        JsonElement data = response.get("data");
        if (!(data instanceof JsonPrimitive value) || !value.isString() || value.getAsString().isBlank()) {
            throw new DreameCloudException("Cloud map-file query returned no download URL");
        }
        String url = value.getAsString();
        validateMapDownloadUrl(url);
        if (!isCurrent.getAsBoolean()) {
            throw new DreameCloudException("Map download cancelled before transfer");
        }
        return transport.getFile(url);
    }

    private static void validateMapObjectName(String objectName) throws DreameCloudException {
        if (objectName.length() > 1024 || objectName.contains("..") || objectName.indexOf('\n') >= 0
                || objectName.indexOf('\r') >= 0 || !objectName.contains("/")) {
            throw new DreameCloudException("Vacuum returned an invalid map object name");
        }
    }

    private static void validateMapDownloadUrl(String value) throws DreameCloudException {
        try {
            URI url = new URI(value);
            String host = url.getHost();
            if (!"https".equalsIgnoreCase(url.getScheme()) || host == null || !isTrustedMapDownloadHost(host)
                    || url.getUserInfo() != null || url.getPort() != -1 && url.getPort() != 443) {
                throw new DreameCloudException("Cloud returned an invalid map download URL");
            }
        } catch (URISyntaxException e) {
            throw new DreameCloudException("Cloud returned an invalid map download URL", e);
        }
    }

    private static boolean isTrustedMapDownloadHost(String host) {
        String normalized = host.toLowerCase(Locale.ROOT);
        return "aliyuncs.com".equals(normalized) || normalized.endsWith(".aliyuncs.com")
                || "iot.dreame.tech".equals(normalized) || normalized.endsWith(".iot.dreame.tech");
    }

    private static boolean isExactInteger(JsonPrimitive value, int expected) {
        try {
            return value.getAsBigDecimal().intValueExact() == expected;
        } catch (ArithmeticException | NumberFormatException e) {
            return false;
        }
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

    @Override
    public synchronized void selectMap(DreameDevice device, int mapIndex) throws DreameCloudException {
        JsonElement result = sendCommand(device, "action", createMapSelectionParameters(device, mapIndex));
        if (result instanceof JsonObject object && object.has("code") && object.get("code").getAsInt() != 0) {
            throw new DreameCloudException("Cloud service rejected map selection");
        }
    }

    static JsonObject createMapSelectionParameters(DreameDevice device, int mapIndex) {
        if (mapIndex < 0) {
            throw new IllegalArgumentException("Map index must not be negative");
        }
        JsonObject data = new JsonObject();
        data.addProperty("idx", Integer.toString(mapIndex));
        JsonObject selection = new JsonObject();
        selection.addProperty("m", "a");
        selection.addProperty("p", 0);
        selection.addProperty("o", 200);
        selection.add("d", data);
        JsonArray input = new JsonArray();
        input.add(selection);
        JsonObject parameters = new JsonObject();
        parameters.addProperty("did", device.id());
        parameters.addProperty("siid", 2);
        parameters.addProperty("aiid", 50);
        parameters.add("in", input);
        return parameters;
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
        return sendCommand(device, method, parameters, () -> true);
    }

    private JsonElement sendCommand(DreameDevice device, String method, JsonElement parameters,
            BooleanSupplier isCurrent) throws DreameCloudException {
        ensureAuthenticated();
        if (!isCurrent.getAsBoolean()) {
            throw new DreameCloudException("Command cancelled before dispatch");
        }
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
