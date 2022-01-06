/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.enturno.internal.connection;

import static java.util.stream.Collectors.groupingBy;
import static org.eclipse.jetty.http.HttpMethod.POST;
import static org.eclipse.jetty.http.HttpStatus.*;
import static org.openhab.binding.enturno.internal.EnturNoBindingConstants.TIME_ZONE;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.enturno.internal.EnturNoConfiguration;
import org.openhab.binding.enturno.internal.EnturNoHandler;
import org.openhab.binding.enturno.internal.model.EnturJsonData;
import org.openhab.binding.enturno.internal.model.estimated.EstimatedCalls;
import org.openhab.binding.enturno.internal.model.simplified.DisplayData;
import org.openhab.binding.enturno.internal.model.stopplace.StopPlace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link EnturNoConnection} is responsible for handling connection to Entur.no API
 *
 * @author Michal Kloc - Initial contribution
 */
@NonNullByDefault
public class EnturNoConnection {

    private final Logger logger = LoggerFactory.getLogger(EnturNoConnection.class);
    private static final String REQUEST_BODY = "realtime_request.graphql";
    private static final String PROPERTY_MESSAGE = "message";
    private static final String CONTENT_TYPE = "application/graphql";
    private static final String REQUIRED_CLIENT_NAME_HEADER = "ET-Client-Name";
    private static final String REQUIRED_CLIENT_NAME = "openHAB-enturnobinding";

    private static final String PARAM_STOPID = "stopid";
    private static final String PARAM_START_DATE_TIME = "startDateTime";

    private static final String REALTIME_URL = "https://api.entur.io/journey-planner/v2/graphql";

    private final EnturNoHandler handler;
    private final HttpClient httpClient;

    private final Gson gson = new Gson();

    public EnturNoConnection(EnturNoHandler handler, HttpClient httpClient) {
        this.handler = handler;
        this.httpClient = httpClient;
    }

    /**
     * Requests the real-time timetable for specified line and stop place
     *
     * @param stopPlaceId stop place id see https://en-tur.no
     * @return the real-time timetable
     * @throws JsonSyntaxException
     * @throws EnturCommunicationException
     * @throws EnturConfigurationException
     */
    public synchronized List<DisplayData> getEnturTimeTable(@Nullable String stopPlaceId, @Nullable String lineCode)
            throws JsonSyntaxException, EnturConfigurationException, EnturCommunicationException {
        if (stopPlaceId == null || stopPlaceId.isBlank()) {
            throw new EnturConfigurationException("Stop place id cannot be empty or null");
        } else if (lineCode == null || lineCode.isBlank()) {
            throw new EnturConfigurationException("Line code cannot be empty or null");
        }

        Map<String, String> params = getRequestParams(handler.getEnturNoConfiguration());

        EnturJsonData enturJsonData = gson.fromJson(getResponse(REALTIME_URL, params), EnturJsonData.class);

        if (enturJsonData == null) {
            throw new EnturCommunicationException("Error when deserializing response to EnturJsonData.class");
        }

        return processData(enturJsonData.data.stopPlace, lineCode);
    }

    private Map<String, String> getRequestParams(EnturNoConfiguration config) {
        Map<String, String> params = new HashMap<>();
        String stopPlaceId = config.getStopPlaceId();
        params.put(PARAM_STOPID, stopPlaceId == null ? "" : stopPlaceId.trim());
        params.put(PARAM_START_DATE_TIME, LocalDateTime.now(ZoneId.of(TIME_ZONE)).toString());

        return params;
    }

    private String getResponse(String url, Map<String, String> params) {
        try {
            if (logger.isTraceEnabled()) {
                logger.trace("Entur request: URL = '{}', graphQL parameters -> startTime = '{}', stopId = '{}'",
                        REALTIME_URL, params.get(PARAM_START_DATE_TIME), params.get(PARAM_STOPID));
            }

            Request request = httpClient.newRequest(url);
            request.method(POST);
            request.timeout(10, TimeUnit.SECONDS);
            request.header(HttpHeader.CONTENT_TYPE, CONTENT_TYPE);
            request.header(REQUIRED_CLIENT_NAME_HEADER, REQUIRED_CLIENT_NAME);
            request.content(new StringContentProvider(getRequestBody(params)));

            logger.trace("Request body: {}", getRequestBody(params));

            ContentResponse contentResponse = request.send();

            int httpStatus = contentResponse.getStatus();
            String content = contentResponse.getContentAsString();
            String errorMessage = "";
            logger.trace("Entur response: status = {}, content = '{}'", httpStatus, content);
            switch (httpStatus) {
                case OK_200:
                    return content;
                case BAD_REQUEST_400:
                case NOT_FOUND_404:
                    errorMessage = getErrorMessage(content);
                    logger.debug("Entur server responded with status code {}: {}", httpStatus, errorMessage);
                    throw new EnturConfigurationException(errorMessage);
                default:
                    errorMessage = getErrorMessage(content);
                    logger.debug("Entur server responded with status code {}: {}", httpStatus, errorMessage);
                    throw new EnturCommunicationException(errorMessage);
            }
        } catch (ExecutionException e) {
            String errorMessage = e.getLocalizedMessage();
            logger.debug("Exception occurred during execution: {}", errorMessage, e);
            throw new EnturCommunicationException(errorMessage, e);
        } catch (TimeoutException | IOException e) {
            logger.debug("Exception occurred during execution: {}", e.getLocalizedMessage(), e);
            throw new EnturCommunicationException(e.getLocalizedMessage(), e);
        } catch (InterruptedException e) {
            logger.debug("Execution interrupted: {}", e.getLocalizedMessage(), e);
            Thread.currentThread().interrupt();
            throw new EnturCommunicationException(e.getLocalizedMessage(), e);
        }
    }

    private String getErrorMessage(String response) {
        JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
        if (jsonResponse.has(PROPERTY_MESSAGE)) {
            return jsonResponse.get(PROPERTY_MESSAGE).getAsString();
        }
        return response;
    }

    private String getRequestBody(Map<String, String> params) throws IOException {
        try (InputStream inputStream = EnturNoConnection.class.getClassLoader().getResourceAsStream(REQUEST_BODY);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            String json = bufferedReader.lines().collect(Collectors.joining("\n"));

            return json.replaceAll("\\{stopPlaceId}", "" + params.get(PARAM_STOPID)).replaceAll("\\{startDateTime}",
                    "" + params.get(PARAM_START_DATE_TIME));
        }
    }

    private List<DisplayData> processData(StopPlace stopPlace, String lineCode) {
        Map<String, List<EstimatedCalls>> departures = stopPlace.estimatedCalls.stream()
                .filter(call -> StringUtils.equalsIgnoreCase(
                        StringUtils.trimToEmpty(call.serviceJourney.journeyPattern.line.publicCode),
                        StringUtils.trimToEmpty(lineCode)))
                .collect(groupingBy(call -> call.quay.id));

        List<DisplayData> processedData = new ArrayList<>();
        if (departures.keySet().size() > 0) {
            DisplayData processedData01 = getDisplayData(stopPlace, departures, 0);
            processedData.add(processedData01);
        }

        if (departures.keySet().size() > 1) {
            DisplayData processedData02 = getDisplayData(stopPlace, departures, 1);
            processedData.add(processedData02);
        }

        return processedData;
    }

    private DisplayData getDisplayData(StopPlace stopPlace, Map<String, List<EstimatedCalls>> departures,
            int quayIndex) {
        List<String> keys = new ArrayList<>(departures.keySet());
        DisplayData processedData = new DisplayData();
        List<EstimatedCalls> quayCalls = departures.get(keys.get(quayIndex));
        List<String> departureTimes = quayCalls.stream().map(eq -> eq.expectedDepartureTime).map(this::getIsoDateTime)
                .collect(Collectors.toList());

        List<String> estimatedFlags = quayCalls.stream().map(es -> es.realtime).collect(Collectors.toList());

        if (quayCalls.size() > quayIndex) {
            String lineCode = quayCalls.get(0).serviceJourney.journeyPattern.line.publicCode;
            String frontText = quayCalls.get(0).destinationDisplay.frontText;
            processedData.lineCode = lineCode;
            processedData.frontText = frontText;
            processedData.departures = departureTimes;
            processedData.estimatedFlags = estimatedFlags;
        }

        processedData.stopPlaceId = stopPlace.id;
        processedData.stopName = stopPlace.name;
        processedData.transportMode = stopPlace.transportMode;
        return processedData;
    }

    private String getIsoDateTime(String dateTimeWithoutColonInZone) {
        String dateTime = StringUtils.substringBeforeLast(dateTimeWithoutColonInZone, "+");
        String offset = StringUtils.substringAfterLast(dateTimeWithoutColonInZone, "+");

        StringBuilder builder = new StringBuilder();
        return builder.append(dateTime).append("+").append(StringUtils.substring(offset, 0, 2)).append(":00")
                .toString();
    }
}
