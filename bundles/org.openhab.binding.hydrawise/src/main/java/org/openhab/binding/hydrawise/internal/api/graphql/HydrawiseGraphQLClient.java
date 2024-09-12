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
package org.openhab.binding.hydrawise.internal.api.graphql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.hydrawise.internal.api.HydrawiseAuthenticationException;
import org.openhab.binding.hydrawise.internal.api.HydrawiseCommandException;
import org.openhab.binding.hydrawise.internal.api.HydrawiseConnectionException;
import org.openhab.binding.hydrawise.internal.api.graphql.dto.ControllerStatus;
import org.openhab.binding.hydrawise.internal.api.graphql.dto.Forecast;
import org.openhab.binding.hydrawise.internal.api.graphql.dto.Hardware;
import org.openhab.binding.hydrawise.internal.api.graphql.dto.Mutation;
import org.openhab.binding.hydrawise.internal.api.graphql.dto.MutationResponse;
import org.openhab.binding.hydrawise.internal.api.graphql.dto.MutationResponse.MutationResponseStatus;
import org.openhab.binding.hydrawise.internal.api.graphql.dto.MutationResponse.StatusCode;
import org.openhab.binding.hydrawise.internal.api.graphql.dto.QueryRequest;
import org.openhab.binding.hydrawise.internal.api.graphql.dto.QueryResponse;
import org.openhab.binding.hydrawise.internal.api.graphql.dto.ScheduledRuns;
import org.openhab.binding.hydrawise.internal.api.graphql.dto.Sensor;
import org.openhab.binding.hydrawise.internal.api.graphql.dto.Zone;
import org.openhab.binding.hydrawise.internal.api.graphql.dto.ZoneRun;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

/**
 *
 * @author Dan Cunningham - Initial contribution
 *
 */
@NonNullByDefault
public class HydrawiseGraphQLClient {
    private final Logger logger = LoggerFactory.getLogger(HydrawiseGraphQLClient.class);

    private final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(Zone.class, new ResponseDeserializer<Zone>())
            .registerTypeAdapter(ScheduledRuns.class, new ResponseDeserializer<ScheduledRuns>())
            .registerTypeAdapter(ZoneRun.class, new ResponseDeserializer<ZoneRun>())
            .registerTypeAdapter(Forecast.class, new ResponseDeserializer<Forecast>())
            .registerTypeAdapter(Sensor.class, new ResponseDeserializer<Forecast>())
            .registerTypeAdapter(ControllerStatus.class, new ResponseDeserializer<ControllerStatus>())
            .registerTypeAdapter(Hardware.class, new ResponseDeserializer<ControllerStatus>()).create();

    private static final String GRAPH_URL = "https://app.hydrawise.com/api/v2/graph";
    private static final String MUTATION_START_ZONE = "startZone(zoneId: %d) { status }";
    private static final String MUTATION_START_ZONE_CUSTOM = "startZone(zoneId: %d, customRunDuration: %d) { status }";
    private static final String MUTATION_START_ALL_ZONES = "startAllZones(controllerId: %d){ status }";
    private static final String MUTATION_START_ALL_ZONES_CUSTOM = "startAllZones(controllerId: %d, markRunAsScheduled: false, customRunDuration: %d ){ status }";
    private static final String MUTATION_STOP_ZONE = "stopZone(zoneId: %d) { status }";
    private static final String MUTATION_STOP_ALL_ZONES = "stopAllZones(controllerId: %d){ status }";
    private static final String MUTATION_SUSPEND_ZONE = "suspendZone(zoneId: %d, until: \"%s\"){ status }";
    private static final String MUTATION_SUSPEND_ALL_ZONES = "suspendAllZones(controllerId: %d, until: \"%s\"){ status }";
    private static final String MUTATION_RESUME_ZONE = "resumeZone(zoneId: %d){ status }";
    private static final String MUTATION_RESUME_ALL_ZONES = "resumeAllZones(controllerId: %d){ status }";

    private final HttpClient httpClient;
    private final OAuthClientService oAuthService;
    private String queryString = "";
    private String weatherString = "";

    public HydrawiseGraphQLClient(HttpClient httpClient, OAuthClientService oAuthService) {
        this.httpClient = httpClient;
        this.oAuthService = oAuthService;
    }

    /**
     * Sends a GrapQL query for controller data
     *
     * @return QueryResponse
     * @throws HydrawiseConnectionException
     * @throws HydrawiseAuthenticationException
     */
    public @Nullable QueryResponse queryControllers()
            throws HydrawiseConnectionException, HydrawiseAuthenticationException {
        try {
            return queryRequest(getQueryString());
        } catch (IOException e) {
            throw new HydrawiseConnectionException(e);
        }
    }

    /**
     * Sends a GrapQL query for controller data
     *
     * @return QueryResponse
     * @throws HydrawiseConnectionException
     * @throws HydrawiseAuthenticationException
     */
    public @Nullable QueryResponse queryWeather()
            throws HydrawiseConnectionException, HydrawiseAuthenticationException {
        try {
            return queryRequest(getWeatherString());
        } catch (IOException e) {
            throw new HydrawiseConnectionException(e);
        }
    }

    /**
     * Sends a GrapQL query for controller data
     *
     * @param queryString
     * @return QueryResponse
     * @throws HydrawiseConnectionException
     * @throws HydrawiseAuthenticationException
     */
    private @Nullable QueryResponse queryRequest(String queryString)
            throws HydrawiseConnectionException, HydrawiseAuthenticationException {
        QueryRequest query = new QueryRequest(queryString);
        String queryJson = gson.toJson(query);
        String response = sendGraphQLQuery(queryJson);
        try {
            return gson.fromJson(response, QueryResponse.class);
        } catch (JsonSyntaxException e) {
            throw new HydrawiseConnectionException("Invalid Response: " + response);
        }
    }

    /***
     * Stops a given relay
     *
     * @param relayId
     * @throws HydrawiseConnectionException
     * @throws HydrawiseAuthenticationException
     * @throws HydrawiseCommandException
     */
    public void stopRelay(int relayId)
            throws HydrawiseConnectionException, HydrawiseAuthenticationException, HydrawiseCommandException {
        sendGraphQLMutation(String.format(MUTATION_STOP_ZONE, relayId));
    }

    /**
     * Stops all relays on a given controller
     *
     * @param controllerId
     * @throws HydrawiseConnectionException
     * @throws HydrawiseAuthenticationException
     * @throws HydrawiseCommandException
     */
    public void stopAllRelays(int controllerId)
            throws HydrawiseConnectionException, HydrawiseAuthenticationException, HydrawiseCommandException {
        sendGraphQLMutation(String.format(MUTATION_STOP_ALL_ZONES, controllerId));
    }

    /**
     * Runs a relay for the default amount of time
     *
     * @param relayId
     * @throws HydrawiseConnectionException
     * @throws HydrawiseAuthenticationException
     * @throws HydrawiseCommandException
     */
    public void runRelay(int relayId)
            throws HydrawiseConnectionException, HydrawiseAuthenticationException, HydrawiseCommandException {
        sendGraphQLMutation(String.format(MUTATION_START_ZONE, relayId));
    }

    /**
     * Runs a relay for the given amount of seconds
     *
     * @param relayId
     * @param seconds
     * @throws HydrawiseConnectionException
     * @throws HydrawiseAuthenticationException
     * @throws HydrawiseCommandException
     */
    public void runRelay(int relayId, int seconds)
            throws HydrawiseConnectionException, HydrawiseAuthenticationException, HydrawiseCommandException {
        sendGraphQLMutation(String.format(MUTATION_START_ZONE_CUSTOM, relayId, seconds));
    }

    /**
     * Run all relays on a given controller for the default amount of time
     *
     * @param controllerId
     * @throws HydrawiseConnectionException
     * @throws HydrawiseAuthenticationException
     * @throws HydrawiseCommandException
     */
    public void runAllRelays(int controllerId)
            throws HydrawiseConnectionException, HydrawiseAuthenticationException, HydrawiseCommandException {
        sendGraphQLMutation(String.format(MUTATION_START_ALL_ZONES, controllerId));
    }

    /***
     * Run all relays on a given controller for the amount of seconds
     *
     * @param controllerId
     * @param seconds
     * @throws HydrawiseConnectionException
     * @throws HydrawiseAuthenticationException
     * @throws HydrawiseCommandException
     */
    public void runAllRelays(int controllerId, int seconds)
            throws HydrawiseConnectionException, HydrawiseAuthenticationException, HydrawiseCommandException {
        sendGraphQLMutation(String.format(MUTATION_START_ALL_ZONES_CUSTOM, controllerId, seconds));
    }

    /**
     * Suspends a given relay
     *
     * @param relayId
     * @throws HydrawiseConnectionException
     * @throws HydrawiseAuthenticationException
     * @throws HydrawiseCommandException
     */
    public void suspendRelay(int relayId, String until)
            throws HydrawiseConnectionException, HydrawiseAuthenticationException, HydrawiseCommandException {
        sendGraphQLMutation(String.format(MUTATION_SUSPEND_ZONE, relayId, until));
    }

    /**
     * Resumes a given relay
     *
     * @param relayId
     * @throws HydrawiseConnectionException
     * @throws HydrawiseAuthenticationException
     * @throws HydrawiseCommandException
     */
    public void resumeRelay(int relayId)
            throws HydrawiseConnectionException, HydrawiseAuthenticationException, HydrawiseCommandException {
        sendGraphQLMutation(String.format(MUTATION_RESUME_ZONE, relayId));
    }

    /**
     * Suspend all relays on a given controller for an amount of seconds
     *
     * @param controllerId
     * @param until
     * @throws HydrawiseConnectionException
     * @throws HydrawiseAuthenticationException
     * @throws HydrawiseCommandException
     */
    public void suspendAllRelays(int controllerId, String until)
            throws HydrawiseConnectionException, HydrawiseAuthenticationException, HydrawiseCommandException {
        sendGraphQLMutation(String.format(MUTATION_SUSPEND_ALL_ZONES, controllerId, until));
    }

    /**
     * Resumes all relays on a given controller
     *
     * @param controllerId
     * @throws HydrawiseConnectionException
     * @throws HydrawiseAuthenticationException
     * @throws HydrawiseCommandException
     */
    public void resumeAllRelays(int controllerId)
            throws HydrawiseConnectionException, HydrawiseAuthenticationException, HydrawiseCommandException {
        sendGraphQLMutation(String.format(MUTATION_RESUME_ALL_ZONES, controllerId));
    }

    private String sendGraphQLQuery(String content)
            throws HydrawiseConnectionException, HydrawiseAuthenticationException {
        return sendGraphQLRequest(content);
    }

    private void sendGraphQLMutation(String content)
            throws HydrawiseConnectionException, HydrawiseAuthenticationException, HydrawiseCommandException {
        Mutation mutation = new Mutation(content);
        logger.debug("Sending Mutation {}", gson.toJson(mutation));
        String response = sendGraphQLRequest(gson.toJson(mutation));
        logger.debug("Mutation response {}", response);
        try {
            MutationResponse mResponse = gson.fromJson(response, MutationResponse.class);
            if (mResponse == null) {
                throw new HydrawiseCommandException("Malformed response: " + response);
            }
            Optional<MutationResponseStatus> status = mResponse.data.values().stream().findFirst();
            if (status.isEmpty()) {
                throw new HydrawiseCommandException("Unknown response: " + response);
            }
            if (status.get().status != StatusCode.OK) {
                throw new HydrawiseCommandException("Command Status: " + status.get().status.name());
            }
        } catch (JsonSyntaxException e) {
            throw new HydrawiseConnectionException("Invalid Response: " + response);
        }
    }

    private String sendGraphQLRequest(String content)
            throws HydrawiseConnectionException, HydrawiseAuthenticationException {
        logger.trace("Sending Request: {}", content);
        ContentResponse response;
        final AtomicInteger responseCode = new AtomicInteger(0);
        final StringBuilder responseMessage = new StringBuilder();
        try {
            AccessTokenResponse token = oAuthService.getAccessTokenResponse();
            if (token == null) {
                throw new HydrawiseAuthenticationException("Login required");
            }
            response = httpClient.newRequest(GRAPH_URL).method(HttpMethod.POST)
                    .content(new StringContentProvider(content), "application/json")
                    .header("Authorization", token.getTokenType() + " " + token.getAccessToken())
                    .onResponseFailure(new Response.FailureListener() {
                        @Override
                        public void onFailure(@Nullable Response response, @Nullable Throwable failure) {
                            int status = response != null ? response.getStatus() : -1;
                            String reason = response != null ? response.getReason() : "Null response";
                            logger.trace("onFailure code: {} message: {}", status, reason);
                            responseCode.set(status);
                            responseMessage.append(reason);
                        }
                    }).send();
            String stringResponse = response.getContentAsString();
            logger.trace("Received Response: {}", stringResponse);
            int statusCode = response.getStatus();
            if (!HttpStatus.isSuccess(statusCode)) {
                throw new HydrawiseConnectionException(
                        "Request failed with HTTP status code: " + statusCode + " response: " + stringResponse,
                        statusCode, stringResponse);
            }
            return stringResponse;
        } catch (InterruptedException | TimeoutException | OAuthException | IOException e) {
            logger.debug("Could not send request", e);
            throw new HydrawiseConnectionException(e);
        } catch (OAuthResponseException e) {
            throw new HydrawiseAuthenticationException(e.getMessage());
        } catch (ExecutionException e) {
            // Hydrawise returns back a 40x status, but without a valid Realm , so jetty throws an exception,
            // this allows us to catch this in a callback and handle accordingly
            logger.debug("ExecutionException", e);
            logger.debug("ExecutionException {} {}", responseCode.get(), responseMessage);
            switch (responseCode.get()) {
                case 401:
                case 403:
                    throw new HydrawiseAuthenticationException(responseMessage.toString());
                default:
                    throw new HydrawiseConnectionException(e);
            }
        }
    }

    private String getQueryString() throws IOException {
        if (queryString.isBlank()) {
            queryString = getResourceString("query.graphql");
        }
        return queryString;
    }

    private String getWeatherString() throws IOException {
        if (weatherString.isBlank()) {
            weatherString = getResourceString("weather.graphql");
        }
        return weatherString;
    }

    private String getResourceString(String name) throws IOException {
        try (InputStream inputStream = HydrawiseGraphQLClient.class.getClassLoader().getResourceAsStream(name);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            return bufferedReader.lines().collect(Collectors.joining("\n"));
        }
    }

    class ResponseDeserializer<T> implements JsonDeserializer<T> {
        @Override
        @Nullable
        public T deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
            return new Gson().fromJson(je, type);
        }
    }
}
