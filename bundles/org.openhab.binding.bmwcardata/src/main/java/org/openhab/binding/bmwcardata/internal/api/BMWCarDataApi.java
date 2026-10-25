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
package org.openhab.binding.bmwcardata.internal.api;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.bmwcardata.internal.api.dto.BasicData;
import org.openhab.binding.bmwcardata.internal.api.dto.Container;
import org.openhab.binding.bmwcardata.internal.api.dto.Container.ContainerList;
import org.openhab.binding.bmwcardata.internal.api.dto.TelematicValue;
import org.openhab.binding.bmwcardata.internal.api.dto.TelematicValue.TelematicDataResponse;
import org.openhab.binding.bmwcardata.internal.api.dto.VehicleMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

/**
 * The {@link BMWCarDataApi} is the client of the BMW CarData REST API. Every request counts towards the daily request
 * quota of the customer, so callers have to use it sparingly.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public class BMWCarDataApi {

    private static final String API_BASE_URL = "https://api-cardata.bmwgroup.com";
    private static final String API_VERSION = "v1";
    private static final int TIMEOUT_SECONDS = 30;
    private static final Pattern VIN_PATTERN = Pattern.compile("[A-HJ-NPR-Z0-9]{17}");
    private static final Pattern CONTAINER_ID_PATTERN = Pattern.compile("[A-Za-z0-9_-]+");

    /**
     * Supplies a currently valid access token.
     */
    @FunctionalInterface
    public interface AccessTokenProvider {
        String getAccessToken() throws BMWCarDataException;
    }

    private final Logger logger = LoggerFactory.getLogger(BMWCarDataApi.class);
    private final HttpClient httpClient;
    private final Gson gson;
    private final AccessTokenProvider tokenProvider;
    private final RequestBudget budget;
    private final String baseUrl;

    public BMWCarDataApi(HttpClient httpClient, Gson gson, AccessTokenProvider tokenProvider, RequestBudget budget) {
        this(httpClient, gson, tokenProvider, budget, API_BASE_URL);
    }

    BMWCarDataApi(HttpClient httpClient, Gson gson, AccessTokenProvider tokenProvider, RequestBudget budget,
            String baseUrl) {
        this.httpClient = httpClient;
        this.gson = gson;
        this.tokenProvider = tokenProvider;
        this.budget = budget;
        this.baseUrl = baseUrl;
    }

    public static boolean isValidVin(String vin) {
        return VIN_PATTERN.matcher(vin).matches();
    }

    public List<VehicleMapping> getVehicleMappings() throws BMWCarDataException {
        String content = send(HttpMethod.GET, "/customers/vehicles/mappings", null, null);
        try {
            JsonElement root = JsonParser.parseString(content);
            JsonArray items;
            if (root.isJsonArray()) {
                items = root.getAsJsonArray();
            } else if (root.isJsonObject() && root.getAsJsonObject().get("mappings") instanceof JsonArray array) {
                items = array;
            } else if (root.isJsonObject() && root.getAsJsonObject().has("vin")) {
                // the API specification declares a single mapping as response
                items = new JsonArray();
                items.add(root);
            } else {
                throw new BMWCarDataException("Unexpected vehicle mappings response");
            }
            List<VehicleMapping> mappings = new ArrayList<>();
            for (JsonElement item : items) {
                VehicleMapping mapping = gson.fromJson(item, VehicleMapping.class);
                if (mapping != null) {
                    mappings.add(mapping);
                }
            }
            return mappings;
        } catch (JsonParseException | IllegalStateException e) {
            throw new BMWCarDataException("Malformed vehicle mappings response", e);
        }
    }

    public BasicData getBasicData(String vin) throws BMWCarDataException {
        return parse(send(HttpMethod.GET, "/customers/vehicles/" + checkVin(vin) + "/basicData", null, null),
                BasicData.class);
    }

    public List<Container> getContainers() throws BMWCarDataException {
        List<Container> containers = parse(send(HttpMethod.GET, "/customers/containers", null, null),
                ContainerList.class).containers;
        return containers != null ? containers : List.of();
    }

    /**
     * Creates a container for the given technical descriptors.
     *
     * @return the id of the new container
     */
    public String createContainer(String name, String purpose, Collection<String> descriptors)
            throws BMWCarDataException {
        JsonObject body = new JsonObject();
        body.addProperty("name", name);
        body.addProperty("purpose", purpose);
        JsonArray descriptorArray = new JsonArray();
        descriptors.forEach(descriptorArray::add);
        body.add("technicalDescriptors", descriptorArray);

        String containerId = parse(send(HttpMethod.POST, "/customers/containers", null, gson.toJson(body)),
                Container.class).containerId;
        if (containerId == null) {
            throw new BMWCarDataException("Container creation response did not contain a container id");
        }
        return containerId;
    }

    public void deleteContainer(String containerId) throws BMWCarDataException {
        send(HttpMethod.DELETE, "/customers/containers/" + checkContainerId(containerId), null, null);
    }

    /**
     * Retrieves the latest values of all descriptors of a container.
     *
     * @return the values by technical descriptor
     */
    public Map<String, TelematicValue> getTelematicData(String vin, String containerId) throws BMWCarDataException {
        Map<String, TelematicValue> data = parse(send(HttpMethod.GET,
                "/customers/vehicles/" + checkVin(vin) + "/telematicData", checkContainerId(containerId), null),
                TelematicDataResponse.class).telematicData;
        return data != null ? data : Map.of();
    }

    private String send(HttpMethod method, String path, @Nullable String containerId, @Nullable String jsonBody)
            throws BMWCarDataException {
        String accessToken = tokenProvider.getAccessToken();
        // counted only once the request can actually be sent
        if (!budget.tryAcquire()) {
            throw BMWCarDataException.budgetExhausted(budget.used(), budget.budget());
        }
        logger.debug("BMW CarData request {} {} ({} of {} requests used in 24 hours)", method, redactVin(path),
                budget.used(), budget.budget());
        Request request = httpClient.newRequest(baseUrl + path).method(method)
                .header(HttpHeader.AUTHORIZATION, "Bearer " + accessToken).header(HttpHeader.ACCEPT, "application/json")
                .header("x-version", API_VERSION).timeout(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (containerId != null) {
            request.param("containerId", containerId);
        }
        if (jsonBody != null) {
            request.content(new StringContentProvider("application/json", jsonBody, StandardCharsets.UTF_8));
        }
        ContentResponse response;
        try {
            response = request.send();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BMWCarDataException("Interrupted", e);
        } catch (ExecutionException e) {
            throw new BMWCarDataException("Request to BMW CarData failed: " + e.getMessage(), e);
        } catch (TimeoutException e) {
            throw new BMWCarDataException("Request to BMW CarData timed out", e);
        }
        int status = response.getStatus();
        if (status == 401) {
            throw new BMWCarDataAuthException("Access token rejected", status);
        }
        if (status < 200 || status >= 300) {
            String content = response.getContentAsString();
            BMWCarDataException exception = new BMWCarDataException("BMW CarData request " + method + " "
                    + redactVin(path) + " failed with HTTP " + status + ": " + errorOf(status, content), status,
                    errorIdOf(content));
            if (exception.isRateLimited()) {
                // BMW counts requests the binding does not know of, e.g. those of other tools using the account
                budget.markExhausted();
            }
            throw exception;
        }
        return response.getContentAsString();
    }

    private static @Nullable String errorIdOf(@Nullable String content) {
        if (content == null || content.isBlank()) {
            return null;
        }
        try {
            JsonElement root = JsonParser.parseString(content);
            if (root.isJsonObject() && root.getAsJsonObject().get("exveErrorId") instanceof JsonPrimitive errorId) {
                return errorId.getAsString();
            }
        } catch (JsonParseException e) {
            // not a structured error response
        }
        return null;
    }

    private <T> T parse(@Nullable String content, Class<T> type) throws BMWCarDataException {
        try {
            @Nullable
            T dto = content == null ? null : gson.fromJson(content, type);
            if (dto == null) {
                throw new BMWCarDataException("Empty response");
            }
            return dto;
        } catch (JsonParseException e) {
            throw new BMWCarDataException("Malformed response", e);
        }
    }

    private static String errorOf(int status, @Nullable String content) {
        if (content == null || content.isBlank()) {
            return "<no content>";
        }
        // gateway errors come as HTML pages, which tell no more than the status
        if (content.stripLeading().startsWith("<")) {
            return HttpStatus.getMessage(status);
        }
        // error responses are short structured messages, but must not flood the log
        String excerpt = content.length() > 200 ? content.substring(0, 200) : content;
        return redactVin(excerpt);
    }

    private static String redactVin(String text) {
        return VIN_PATTERN.matcher(text).replaceAll(match -> match.group().substring(0, 3) + "***");
    }

    private static String checkVin(String vin) throws BMWCarDataException {
        if (!isValidVin(vin)) {
            throw new BMWCarDataException("Invalid VIN");
        }
        return vin;
    }

    private static String checkContainerId(String containerId) throws BMWCarDataException {
        if (!CONTAINER_ID_PATTERN.matcher(containerId).matches()) {
            throw new BMWCarDataException("Invalid container id");
        }
        return containerId;
    }
}
