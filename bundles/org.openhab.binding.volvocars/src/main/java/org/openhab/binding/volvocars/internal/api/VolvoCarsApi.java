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
package org.openhab.binding.volvocars.internal.api;

import static org.openhab.binding.volvocars.internal.VolvoCarsBindingConstants.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.volvocars.internal.api.dto.CommandResult;
import org.openhab.binding.volvocars.internal.api.dto.Location;
import org.openhab.binding.volvocars.internal.api.dto.ValueField;
import org.openhab.binding.volvocars.internal.api.dto.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * Client for the Volvo Cars Connected Vehicle API v2, Energy API v2 and Location API v1.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public class VolvoCarsApi {
    private static final String API_KEY_HEADER = "vcc-api-key";
    private static final int REQUEST_TIMEOUT_SECONDS = 30;

    private final Logger logger = LoggerFactory.getLogger(VolvoCarsApi.class);
    private final Gson gson = new Gson();

    private final HttpClient httpClient;
    private final VolvoCarsOAuthClient oAuthClient;
    private final String apiKey;

    public VolvoCarsApi(HttpClient httpClient, VolvoCarsOAuthClient oAuthClient, String apiKey) {
        this.httpClient = httpClient;
        this.oAuthClient = oAuthClient;
        this.apiKey = apiKey;
    }

    /**
     * @return the VINs of all vehicles connected to the account
     */
    public List<String> getVehicles() throws VolvoCarsException {
        List<String> vins = new ArrayList<>();
        for (JsonElement element : getDataArray(request(HttpMethod.GET, CONNECTED_VEHICLE_ENDPOINT, null))) {
            if (element instanceof JsonObject vehicle && vehicle.get("vin") instanceof JsonElement vin
                    && vin.isJsonPrimitive()) {
                vins.add(vin.getAsString());
            }
        }
        return vins;
    }

    public Vehicle getVehicle(String vin) throws VolvoCarsException {
        JsonObject data = getDataObject(request(HttpMethod.GET, vehicleUrl(CONNECTED_VEHICLE_ENDPOINT, vin, ""), null));
        Vehicle vehicle = parse(data, Vehicle.class);
        if (vehicle == null) {
            throw new VolvoCarsException("Empty vehicle details");
        }
        return vehicle;
    }

    /**
     * Reads one of the Connected Vehicle API resources returning a map of values, e.g. <code>doors</code>,
     * <code>windows</code> or <code>statistics</code>.
     */
    public Map<String, ValueField> getValues(String vin, String resource) throws VolvoCarsException {
        return toValueFields(
                getDataObject(request(HttpMethod.GET, vehicleUrl(CONNECTED_VEHICLE_ENDPOINT, vin, resource), null)));
    }

    /**
     * Reads the energy state. Unlike the Connected Vehicle API, the response is not wrapped into a
     * <code>data</code> object and values the vehicle does not support carry <code>status: ERROR</code>.
     */
    public Map<String, ValueField> getEnergyState(String vin) throws VolvoCarsException {
        return toValueFields(request(HttpMethod.GET, vehicleUrl(ENERGY_ENDPOINT, vin, "state"), null));
    }

    /**
     * @return the names of all energy state values the vehicle supports, or an empty set if the vehicle does not
     *         support the Energy API at all
     */
    public Set<String> getSupportedEnergyCapabilities(String vin) throws VolvoCarsException {
        JsonObject response = request(HttpMethod.GET, vehicleUrl(ENERGY_ENDPOINT, vin, "capabilities"), null);
        JsonObject capabilities = response.get("getEnergyState") instanceof JsonObject wrapped ? wrapped : response;
        Set<String> supported = new HashSet<>();
        if (!isSupported(capabilities)) {
            return supported;
        }
        for (Map.Entry<String, JsonElement> entry : capabilities.entrySet()) {
            if (entry.getValue() instanceof JsonObject capability && isSupported(capability)) {
                // the capability is called chargingSystemStatus while the state value is chargingStatus
                supported.add("chargingSystemStatus".equals(entry.getKey()) ? "chargingStatus" : entry.getKey());
            }
        }
        return supported;
    }

    public @Nullable Location getLocation(String vin) throws VolvoCarsException {
        return parse(getDataObject(request(HttpMethod.GET, vehicleUrl(LOCATION_ENDPOINT, vin, "location"), null)),
                Location.class);
    }

    /**
     * @return the commands supported by the vehicle, e.g. <code>LOCK</code> or <code>CLIMATIZATION_START</code>
     */
    public Set<String> getAvailableCommands(String vin) throws VolvoCarsException {
        Set<String> commands = new HashSet<>();
        for (JsonElement element : getDataArray(
                request(HttpMethod.GET, vehicleUrl(CONNECTED_VEHICLE_ENDPOINT, vin, "commands"), null))) {
            if (element instanceof JsonObject command && command.get("command") instanceof JsonElement name
                    && name.isJsonPrimitive()) {
                commands.add(name.getAsString());
            }
        }
        return commands;
    }

    /**
     * Invokes a command such as <code>lock</code>, <code>climatization-start</code> or <code>honk-flash</code>.
     */
    public CommandResult executeCommand(String vin, String command, @Nullable JsonObject body)
            throws VolvoCarsException {
        JsonObject data = getDataObject(
                request(HttpMethod.POST, vehicleUrl(CONNECTED_VEHICLE_ENDPOINT, vin, "commands/" + command),
                        body != null ? body : new JsonObject()));
        CommandResult result = parse(data, CommandResult.class);
        return result != null ? result : new CommandResult();
    }

    private JsonObject request(HttpMethod method, String url, @Nullable JsonObject body) throws VolvoCarsException {
        String accessToken = oAuthClient.getAccessToken();
        Request request = httpClient.newRequest(url).method(method)
                .header(HttpHeader.AUTHORIZATION, "Bearer " + accessToken).header(API_KEY_HEADER, apiKey)
                .header(HttpHeader.ACCEPT, "application/json").timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (body != null) {
            request.content(new StringContentProvider("application/json", gson.toJson(body), StandardCharsets.UTF_8));
        }
        try {
            ContentResponse response = request.send();
            int status = response.getStatus();
            String content = response.getContentAsString();
            logger.trace("{} {} returned HTTP {}: {}", method, url, status, content);
            if (status == HttpStatus.UNAUTHORIZED_401) {
                oAuthClient.invalidateAccessToken();
            }
            if (status != HttpStatus.OK_200) {
                throw new VolvoCarsException("Volvo API returned HTTP " + status + errorDetails(content), status,
                        false);
            }
            return content.isBlank() ? new JsonObject() : JsonParser.parseString(content).getAsJsonObject();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new VolvoCarsException("Request was interrupted", e);
        } catch (ExecutionException | TimeoutException e) {
            throw new VolvoCarsException("Request failed: " + e.getMessage(), e);
        } catch (JsonParseException | IllegalStateException e) {
            throw new VolvoCarsException("Invalid response: " + e.getMessage(), e);
        }
    }

    private String errorDetails(String content) {
        try {
            if (JsonParser.parseString(content) instanceof JsonObject response
                    && response.get("error") instanceof JsonObject error) {
                StringBuilder details = new StringBuilder();
                // the Energy API reports a code instead of a description
                for (String key : List.of("code", "message", "description")) {
                    if (error.get(key) instanceof JsonElement value && value.isJsonPrimitive()) {
                        details.append(details.isEmpty() ? ": " : " ").append(value.getAsString());
                    }
                }
                return details.toString();
            }
        } catch (JsonParseException e) {
            // no structured error details available
        }
        return "";
    }

    private Map<String, ValueField> toValueFields(JsonObject data) {
        Map<String, ValueField> values = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : data.entrySet()) {
            if (entry.getValue() instanceof JsonObject object) {
                ValueField field = parse(object, ValueField.class);
                if (field != null) {
                    values.put(entry.getKey(), field);
                }
            }
        }
        return values;
    }

    private <T> @Nullable T parse(JsonObject object, Class<T> clazz) {
        try {
            return gson.fromJson(object, clazz);
        } catch (JsonParseException e) {
            logger.debug("Unable to parse {}: {}", clazz.getSimpleName(), e.getMessage());
            return null;
        }
    }

    private static boolean isSupported(JsonObject object) {
        return object.get("isSupported") instanceof JsonElement supported && supported.isJsonPrimitive()
                && supported.getAsBoolean();
    }

    /**
     * The responses are wrapped into a <code>data</code> object, but the specification documents some resources
     * without it, so fall back to the response itself.
     */
    private static JsonObject getDataObject(JsonObject response) {
        return response.get("data") instanceof JsonObject data ? data : response;
    }

    private static Iterable<JsonElement> getDataArray(JsonObject response) {
        return response.get("data") instanceof JsonElement data && data.isJsonArray() ? data.getAsJsonArray()
                : List.of();
    }

    private static String vehicleUrl(String endpoint, String vin, String resource) {
        String url = endpoint + "/" + URLEncoder.encode(vin, StandardCharsets.UTF_8);
        return resource.isEmpty() ? url : url + "/" + resource;
    }
}
