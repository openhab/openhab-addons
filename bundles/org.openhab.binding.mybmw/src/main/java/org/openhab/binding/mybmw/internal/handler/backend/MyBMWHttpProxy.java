/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.mybmw.internal.handler.backend;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.UrlEncoded;
import org.openhab.binding.mybmw.internal.MyBMWBridgeConfiguration;
import org.openhab.binding.mybmw.internal.dto.charge.ChargingSessionsContainer;
import org.openhab.binding.mybmw.internal.dto.charge.ChargingStatisticsContainer;
import org.openhab.binding.mybmw.internal.dto.remote.ExecutionStatusContainer;
import org.openhab.binding.mybmw.internal.dto.vehicle.Vehicle;
import org.openhab.binding.mybmw.internal.dto.vehicle.VehicleBase;
import org.openhab.binding.mybmw.internal.dto.vehicle.VehicleStateContainer;
import org.openhab.binding.mybmw.internal.handler.MyBMWBridgeHandler;
import org.openhab.binding.mybmw.internal.handler.auth.MyBMWTokenController;
import org.openhab.binding.mybmw.internal.handler.enums.RemoteService;
import org.openhab.binding.mybmw.internal.utils.BimmerConstants;
import org.openhab.binding.mybmw.internal.utils.Constants;
import org.openhab.binding.mybmw.internal.utils.Converter;
import org.openhab.binding.mybmw.internal.utils.HTTPConstants;
import org.openhab.binding.mybmw.internal.utils.ImageProperties;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MyBMWHttpProxy} This class holds the important constants for the BMW Connected Drive Authorization.
 * They are taken from the Bimmercode from github
 * {@link https://github.com/bimmerconnected/bimmer_connected}
 * File defining these constants
 * {@link https://github.com/bimmerconnected/bimmer_connected/blob/master/bimmer_connected/account.py}
 * https://customer.bmwgroup.com/one/app/oauth.js
 *
 * @author Bernd Weymann - Initial contribution
 * @author Norbert Truchsess - edit and send of charge profile
 * @author Martin Grassl - refactoring
 * @author Mark Herwege - extended log anonymization
 */
@NonNullByDefault
public class MyBMWHttpProxy implements MyBMWProxy {
    private static final Pattern TIME_PATTERN = Pattern.compile("\\b(\\d{1,2}:\\d{2}:\\d{2})\\b");
    private static final Pattern DURATION_PATTERN = Pattern.compile("\\b(\\d+)");

    private final Logger logger = LoggerFactory.getLogger(MyBMWHttpProxy.class);

    private final HttpClient httpClient;
    private MyBMWBridgeConfiguration bridgeConfiguration;
    MyBMWTokenController myBMWTokenController;

    private Instant nextQuota = Instant.now();

    /**
     * URLs taken from
     * https://github.com/bimmerconnected/bimmer_connected/blob/master/bimmer_connected/const.py
     */
    private final String vehicleUrl;
    private final String vehicleStateUrl;
    private final String remoteCommandUrl;
    private final String remoteStatusUrl;

    public MyBMWHttpProxy(MyBMWBridgeHandler bridgeHandler, HttpClient httpClient, OAuthFactory oAuthFactory,
            MyBMWBridgeConfiguration bridgeConfiguration) {
        logger.trace("MyBMWHttpProxy - initialize");
        this.httpClient = httpClient;

        myBMWTokenController = new MyBMWTokenController(bridgeHandler, bridgeConfiguration, httpClient, oAuthFactory);

        this.bridgeConfiguration = bridgeConfiguration;

        vehicleUrl = "https://" + BimmerConstants.EADRAX_SERVER_MAP.get(bridgeConfiguration.getRegion())
                + BimmerConstants.API_VEHICLES;

        vehicleStateUrl = vehicleUrl + "/state";

        remoteCommandUrl = "https://" + BimmerConstants.EADRAX_SERVER_MAP.get(bridgeConfiguration.getRegion())
                + BimmerConstants.API_REMOTE_SERVICE_BASE_URL;
        remoteStatusUrl = remoteCommandUrl + "eventStatus";
        logger.trace("MyBMWHttpProxy - ready");
    }

    @Override
    public void setBridgeConfiguration(MyBMWBridgeConfiguration bridgeConfiguration) {
        this.bridgeConfiguration = bridgeConfiguration;
        myBMWTokenController.setBridgeConfiguration(bridgeConfiguration);
    }

    /**
     * requests all vehicles
     *
     * @return list of vehicles
     */
    @Override
    public List<Vehicle> requestVehicles() throws NetworkException {
        List<Vehicle> vehicles = new ArrayList<>();
        List<VehicleBase> vehiclesBase = requestVehiclesBase();

        for (VehicleBase vehicleBase : vehiclesBase) {
            VehicleStateContainer vehicleState = requestVehicleState(vehicleBase.getVin(),
                    vehicleBase.getAttributes().getBrand());

            Vehicle vehicle = new Vehicle();
            vehicle.setVehicleBase(vehicleBase);
            vehicle.setVehicleState(vehicleState);
            vehicles.add(vehicle);
        }

        return vehicles;
    }

    /**
     * request all vehicles for one specific brand and their state
     *
     * @param brand
     * @return the vehicles of one brand
     */
    @Override
    public List<VehicleBase> requestVehiclesBase(String brand) throws NetworkException {
        String vehicleResponseString = requestVehiclesBaseJson(brand);
        return JsonStringDeserializer.getVehicleBaseList(vehicleResponseString);
    }

    /**
     * request the raw JSON for the vehicle
     *
     * @param brand
     * @return the base vehicle information as JSON string
     */
    @Override
    public String requestVehiclesBaseJson(String brand) throws NetworkException {
        byte[] vehicleResponse = get(vehicleUrl, brand, null, HTTPConstants.CONTENT_TYPE_JSON);
        String vehicleResponseString = new String(vehicleResponse, Charset.defaultCharset());
        return vehicleResponseString;
    }

    /**
     * request vehicles for all possible brands
     *
     * @return the list of vehicles
     */
    @Override
    public List<VehicleBase> requestVehiclesBase() throws NetworkException {
        List<VehicleBase> vehicles = new ArrayList<>();

        for (String brand : BimmerConstants.REQUESTED_BRANDS) {
            try {
                vehicles.addAll(requestVehiclesBase(brand));

                Thread.sleep(10000);
            } catch (NetworkException ne) {
                logger.warn("error retrieving the base vehicles for brand {}: {}", brand, ne.getMessage());
                throw ne;
            } catch (InterruptedException ie) {
                throw new NetworkException(ie);
            }
        }

        return vehicles;
    }

    /**
     * request the vehicle image
     *
     * @param vin the vin of the vehicle
     * @param brand the brand of the vehicle
     * @param props the image properties
     * @return the image as a byte array
     */
    @Override
    public byte[] requestImage(String vin, String brand, ImageProperties props) throws NetworkException {
        final String localImageUrl = "https://" + BimmerConstants.EADRAX_SERVER_MAP.get(bridgeConfiguration.getRegion())
                + BimmerConstants.IMAGE_URL.replace(BimmerConstants.PARAM_VIN, vin) + props.viewport;
        return get(localImageUrl, brand, vin, HTTPConstants.CONTENT_TYPE_IMAGE);
    }

    /**
     * request the state for one specific vehicle
     *
     * @param vin
     * @param brand
     * @return the vehicle state
     */
    @Override
    public VehicleStateContainer requestVehicleState(String vin, String brand) throws NetworkException {
        String vehicleStateResponseString = requestVehicleStateJson(vin, brand);
        return JsonStringDeserializer.getVehicleState(vehicleStateResponseString);
    }

    /**
     * request the raw state as JSON for one specific vehicle
     *
     * @param vin
     * @param brand
     * @return the vehicle state as string
     */
    @Override
    public String requestVehicleStateJson(String vin, String brand) throws NetworkException {
        byte[] vehicleStateResponse = get(vehicleStateUrl, brand, vin, HTTPConstants.CONTENT_TYPE_JSON);
        String vehicleStateResponseString = new String(vehicleStateResponse, Charset.defaultCharset());
        return vehicleStateResponseString;
    }

    /**
     * request charge statistics for electric vehicles
     *
     * @param vin
     * @param brand
     * @return the charge statistics
     */
    @Override
    public ChargingStatisticsContainer requestChargeStatistics(String vin, String brand) throws NetworkException {
        String chargeStatisticsResponseString = requestChargeStatisticsJson(vin, brand);
        return JsonStringDeserializer.getChargingStatistics(new String(chargeStatisticsResponseString));
    }

    /**
     * request charge statistics for electric vehicles as JSON
     *
     * @param vin
     * @param brand
     * @return the charge statistics as JSON string
     */
    @Override
    public String requestChargeStatisticsJson(String vin, String brand) throws NetworkException {
        MultiMap<@Nullable String> chargeStatisticsParams = new MultiMap<>();
        chargeStatisticsParams.put("vin", vin);
        chargeStatisticsParams.put("currentDate", Converter.getCurrentISOTime());
        String params = UrlEncoded.encode(chargeStatisticsParams, StandardCharsets.UTF_8, false);
        String chargeStatisticsUrl = "https://" + BimmerConstants.EADRAX_SERVER_MAP.get(bridgeConfiguration.getRegion())
                + BimmerConstants.CHARGING_STATISTICS + params;
        byte[] chargeStatisticsResponse = get(chargeStatisticsUrl, brand, vin, HTTPConstants.CONTENT_TYPE_JSON);
        String chargeStatisticsResponseString = new String(chargeStatisticsResponse);
        return chargeStatisticsResponseString;
    }

    /**
     * request charge sessions for electric vehicles
     *
     * @param vin
     * @param brand
     * @return the charge sessions
     */
    @Override
    public ChargingSessionsContainer requestChargeSessions(String vin, String brand) throws NetworkException {
        String chargeSessionsResponseString = requestChargeSessionsJson(vin, brand);
        return JsonStringDeserializer.getChargingSessions(chargeSessionsResponseString);
    }

    /**
     * request charge sessions for electric vehicles as JSON string
     *
     * @param vin
     * @param brand
     * @return the charge sessions as JSON string
     */
    @Override
    public String requestChargeSessionsJson(String vin, String brand) throws NetworkException {
        MultiMap<@Nullable String> chargeSessionsParams = new MultiMap<>();
        chargeSessionsParams.put("vin", vin);
        chargeSessionsParams.put("maxResults", "40");
        chargeSessionsParams.put("include_date_picker", "true");
        String params = UrlEncoded.encode(chargeSessionsParams, StandardCharsets.UTF_8, false);
        String chargeSessionsUrl = "https://" + BimmerConstants.EADRAX_SERVER_MAP.get(bridgeConfiguration.getRegion())
                + BimmerConstants.CHARGING_SESSIONS + params;
        byte[] chargeSessionsResponse = get(chargeSessionsUrl, brand, vin, HTTPConstants.CONTENT_TYPE_JSON);
        String chargeSessionsResponseString = new String(chargeSessionsResponse);
        return chargeSessionsResponseString;
    }

    /**
     * execute a remote service call
     *
     * @param vin
     * @param brand
     * @param service the service which should be executed
     * @return the running service execution for status checks
     */
    @Override
    public ExecutionStatusContainer executeRemoteServiceCall(String vin, String brand, RemoteService service)
            throws NetworkException {
        String executionUrl = remoteCommandUrl + vin + "/" + service.getCommand();

        byte[] response = post(executionUrl, brand, vin, HTTPConstants.CONTENT_TYPE_JSON, service.getBody());

        return JsonStringDeserializer.getExecutionStatus(new String(response));
    }

    /**
     * check the status of a service call
     *
     * @param brand
     * @param eventid the ID of the currently running service execution
     * @return the running service execution for status checks
     */
    @Override
    public ExecutionStatusContainer executeRemoteServiceStatusCall(String brand, String eventId)
            throws NetworkException {
        String executionUrl = remoteStatusUrl + Constants.QUESTION + "eventId=" + eventId;

        byte[] response = post(executionUrl, brand, null, HTTPConstants.CONTENT_TYPE_JSON, null);

        return JsonStringDeserializer.getExecutionStatus(new String(response));
    }

    /**
     * prepares a GET request to the backend
     *
     * @param url
     * @param brand
     * @param vin
     * @param contentType
     * @return byte array of the response body
     */
    private byte[] get(String url, final String brand, @Nullable String vin, String contentType)
            throws NetworkException {
        return call(url, false, brand, vin, contentType, null);
    }

    /**
     * prepares a POST request to the backend
     *
     * @param url
     * @param brand
     * @param vin
     * @param contentType
     * @param body
     * @return byte array of the response body
     */
    private byte[] post(String url, final String brand, @Nullable String vin, String contentType, @Nullable String body)
            throws NetworkException {
        return call(url, true, brand, vin, contentType, body);
    }

    /**
     * executes the real call to the backend
     *
     * @param url
     * @param post boolean value indicating if it is a post request
     * @param brand
     * @param vin
     * @param contentType
     * @param body
     * @return byte array of the response body
     */
    private synchronized byte[] call(final String url, final boolean post, final String brand,
            final @Nullable String vin, final String contentType, final @Nullable String body) throws NetworkException {
        byte[] responseByteArray = "".getBytes();

        if (Instant.now().isBefore(nextQuota)) {
            throw new NetworkException(url, -1, "Quota Exceeded, waiting for quota renewal", body);
        }

        // return in case of unknown brand
        if (!BimmerConstants.REQUESTED_BRANDS.contains(brand.toLowerCase())) {
            logger.warn("Unknown Brand {}", brand);
            throw new NetworkException("Unknown Brand " + brand);
        }

        // if no token is available, no request can be triggered
        AccessTokenResponse tokenResponse = myBMWTokenController.getToken();
        String bearerToken = tokenResponse.getAccessToken();
        bearerToken = (bearerToken == null) || bearerToken.isEmpty() ? Constants.EMPTY
                : new StringBuilder(tokenResponse.getTokenType()).append(Constants.SPACE).append(bearerToken)
                        .toString();
        if (tokenResponse.isExpired(Instant.now(), 1)) {
            logger.warn("The login failed, no token is available");
            throw new NetworkException("The login failed, no token is available");
        }

        final Request req;

        if (post) {
            req = httpClient.POST(url);
        } else {
            req = httpClient.newRequest(url);
        }

        req.header(HttpHeader.AUTHORIZATION, bearerToken);
        req.header(HTTPConstants.HEADER_X_USER_AGENT, String.format(BimmerConstants.X_USER_AGENT, brand.toLowerCase(),
                BimmerConstants.APP_VERSIONS.get(bridgeConfiguration.getRegion()), bridgeConfiguration.getRegion()));
        req.header(HttpHeader.ACCEPT_LANGUAGE, bridgeConfiguration.getLanguage());
        req.header(HttpHeader.ACCEPT, contentType);
        req.header(HTTPConstants.HEADER_BMW_VIN, vin);

        try {
            ContentResponse response = req.timeout(HTTPConstants.HTTP_TIMEOUT_SEC, TimeUnit.SECONDS).send();
            if (response.getStatus() >= 300) {
                setNextQuotaAttempt(response);
                responseByteArray = "".getBytes();
                NetworkException exception = new NetworkException(url, response.getStatus(),
                        ResponseContentAnonymizer.anonymizeResponseContent(response.getContentAsString()), body);
                logResponse(ResponseContentAnonymizer.replaceVin(exception.getUrl(), vin), exception.getReason(),
                        ResponseContentAnonymizer.anonymizeResponseContent(body));
                throw exception;
            } else {
                responseByteArray = response.getContent();

                // don't print images
                if (!HTTPConstants.CONTENT_TYPE_IMAGE.equals(contentType)) {
                    logResponse(ResponseContentAnonymizer.replaceVin(url, vin),
                            ResponseContentAnonymizer.anonymizeResponseContent(response.getContentAsString()),
                            ResponseContentAnonymizer.anonymizeResponseContent(body));
                }
            }
        } catch (TimeoutException | ExecutionException e) {
            logResponse(ResponseContentAnonymizer.replaceVin(url, vin), e.getMessage(),
                    ResponseContentAnonymizer.anonymizeResponseContent(vin));
            throw new NetworkException(url, -1, null, body, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logResponse(ResponseContentAnonymizer.replaceVin(url, vin), e.getMessage(),
                    ResponseContentAnonymizer.anonymizeResponseContent(vin));
            throw new NetworkException(url, -1, null, body, e);
        }

        return responseByteArray;
    }

    private void setNextQuotaAttempt(ContentResponse response) {
        int status = response.getStatus();
        String message = JsonStringDeserializer.deserializeString(response.getContentAsString(),
                ResponseErrorContainer.class).message;
        Duration delay = Duration.ofSeconds(5); // minimum wait of 5 seconds
        switch (status) {
            case 403:
                // response contains the amount of time before quota is replenished
                try {
                    Matcher matcher = TIME_PATTERN.matcher(message);
                    if (matcher.find()) {
                        LocalTime time = LocalTime.parse(matcher.group(1), DateTimeFormatter.ISO_LOCAL_TIME);
                        delay = Duration.between(LocalTime.MIDNIGHT, time);
                    }
                } catch (DateTimeParseException e) {
                    // we couldn't parse, continue with default
                }
                break;
            case 429:
                // response may contain the number of seconds to wait before quota is replenished
                Matcher matcher = DURATION_PATTERN.matcher(message);
                if (matcher.find()) {
                    delay = Duration.ofSeconds(Long.valueOf(matcher.group(1)));
                } else {
                    delay = Duration.ofMinutes(5);
                }
                break;
            default:
                return;
        }
        logger.debug("next call allowed in {}:{}:{}", delay.toHours(), delay.toMinutesPart(), delay.toSecondsPart());
        nextQuota = Instant.now().plus(delay);
    }

    @Override
    public Instant getNextQuota() {
        return nextQuota;
    }

    private void logResponse(@Nullable String url, @Nullable String fingerprint, @Nullable String body) {
        logger.debug("###### Request URL - BEGIN ######");
        logger.debug("{}", url);
        logger.debug("###### Request Body - BEGIN ######");
        logger.debug("{}", body);
        logger.debug("###### Response Data - BEGIN ######");
        logger.debug("{}", fingerprint);
        logger.debug("###### Response Data - END ######");
    }
}
