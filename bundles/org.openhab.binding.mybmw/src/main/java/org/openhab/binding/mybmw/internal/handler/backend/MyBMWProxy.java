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
package org.openhab.binding.mybmw.internal.handler.backend;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNull;
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
import org.openhab.binding.mybmw.internal.dto.network.NetworkException;
import org.openhab.binding.mybmw.internal.dto.remote.ExecutionStatusContainer;
import org.openhab.binding.mybmw.internal.dto.vehicle.Vehicle;
import org.openhab.binding.mybmw.internal.dto.vehicle.VehicleBase;
import org.openhab.binding.mybmw.internal.dto.vehicle.VehicleStateContainer;
import org.openhab.binding.mybmw.internal.handler.auth.MyBMWTokenController;
import org.openhab.binding.mybmw.internal.handler.enums.RemoteService;
import org.openhab.binding.mybmw.internal.utils.BimmerConstants;
import org.openhab.binding.mybmw.internal.utils.Constants;
import org.openhab.binding.mybmw.internal.utils.Converter;
import org.openhab.binding.mybmw.internal.utils.HTTPConstants;
import org.openhab.binding.mybmw.internal.utils.ImageProperties;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MyBMWProxy} This class holds the important constants for the BMW Connected Drive Authorization.
 * They are taken from the Bimmercode from github
 * {@link https://github.com/bimmerconnected/bimmer_connected}
 * File defining these constants
 * {@link https://github.com/bimmerconnected/bimmer_connected/blob/master/bimmer_connected/account.py}
 * https://customer.bmwgroup.com/one/app/oauth.js
 *
 * @author Bernd Weymann - Initial contribution
 * @author Norbert Truchsess - edit & send of charge profile
 * @author Martin Grassl - refactoring
 */
@NonNullByDefault
public class MyBMWProxy {
    private final Logger logger = LoggerFactory.getLogger(MyBMWProxy.class);
    private final HttpClient httpClient;
    private final MyBMWBridgeConfiguration configuration;
    private final MyBMWTokenController myBMWTokenHandler;

    /**
     * URLs taken from
     * https://github.com/bimmerconnected/bimmer_connected/blob/master/bimmer_connected/const.py
     */
    private final String vehicleUrl;
    private final String remoteCommandUrl;
    private final String remoteStatusUrl;

    public MyBMWProxy(HttpClientFactory httpClientFactory, MyBMWBridgeConfiguration config) {
        httpClient = httpClientFactory.getCommonHttpClient();

        myBMWTokenHandler = new MyBMWTokenController(config, httpClient);

        configuration = config;

        vehicleUrl = "https://" + BimmerConstants.EADRAX_SERVER_MAP.get(configuration.region)
                + BimmerConstants.API_VEHICLES;

        remoteCommandUrl = "https://" + BimmerConstants.EADRAX_SERVER_MAP.get(configuration.region)
                + BimmerConstants.API_REMOTE_SERVICE_BASE_URL;
        remoteStatusUrl = remoteCommandUrl + "eventStatus";
    }

    public List<@NonNull Vehicle> requestVehicles() throws NetworkException {
        List<@NonNull Vehicle> vehicles = new ArrayList<>();
        List<@NonNull VehicleBase> vehiclesBase = requestVehiclesBase();

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
     */
    public List<VehicleBase> requestVehiclesBase(String brand) throws NetworkException {
        byte[] vehicleResponse = get(vehicleUrl, brand, null, HTTPConstants.CONTENT_TYPE_JSON);
        String vehicleResponseString = new String(vehicleResponse, Charset.defaultCharset());

        return JsonStringDeserializer.getVehicleBaseList(vehicleResponseString);
    }

    /**
     * request vehicles for all possible brands
     *
     * @param callback
     */
    public List<VehicleBase> requestVehiclesBase() throws NetworkException {
        List<VehicleBase> vehicles = new ArrayList<>();

        for (String brand : BimmerConstants.REQUESTED_BRANDS) {
            vehicles.addAll(requestVehiclesBase(brand));
        }

        return vehicles;
    }

    /**
     * request the vehicle image
     *
     * @param config
     * @param props
     * @return
     */
    public byte[] requestImage(String vin, String brand, ImageProperties props) throws NetworkException {
        final String localImageUrl = "https://" + BimmerConstants.EADRAX_SERVER_MAP.get(configuration.region)
                + "/eadrax-ics/v3/presentation/vehicles/" + vin + "/images?carView=" + props.viewport;
        return get(localImageUrl, brand, vin, HTTPConstants.CONTENT_TYPE_IMAGE);
    }

    /**
     * request the state for one specific vehicle
     *
     * @param baseVehicle
     * @return
     */
    public VehicleStateContainer requestVehicleState(String vin, String brand) throws NetworkException {
        byte[] vehicleStateResponse = get(vehicleUrl + "/" + vin + "/state", brand, vin,
                HTTPConstants.CONTENT_TYPE_JSON);

        String vehicleStateResponseString = new String(vehicleStateResponse, Charset.defaultCharset());

        return JsonStringDeserializer.getVehicleState(vehicleStateResponseString);
    }

    /**
     * request charge statistics for electric vehicles
     *
     */
    public ChargingStatisticsContainer requestChargeStatistics(String vin, String brand) throws NetworkException {
        MultiMap<@Nullable String> chargeStatisticsParams = new MultiMap<>();
        chargeStatisticsParams.put("vin", vin);
        chargeStatisticsParams.put("currentDate", Converter.getCurrentISOTime());
        String params = UrlEncoded.encode(chargeStatisticsParams, StandardCharsets.UTF_8, false);
        String chargeStatisticsUrl = "https://" + BimmerConstants.EADRAX_SERVER_MAP.get(configuration.region)
                + "/eadrax-chs/v1/charging-statistics?" + params;
        byte[] chargeStatisticsResponse = get(chargeStatisticsUrl, brand, vin, HTTPConstants.CONTENT_TYPE_JSON);

        return JsonStringDeserializer.getChargeStatistics(new String(chargeStatisticsResponse));
    }

    /**
     * request charge sessions for electric vehicles
     *
     */
    public ChargingSessionsContainer requestChargeSessions(String vin, String brand) throws NetworkException {
        MultiMap<@Nullable String> chargeSessionsParams = new MultiMap<>();
        chargeSessionsParams.put("vin", vin);
        chargeSessionsParams.put("maxResults", "40");
        chargeSessionsParams.put("include_date_picker", "true");
        String params = UrlEncoded.encode(chargeSessionsParams, StandardCharsets.UTF_8, false);
        String chargeSessionsUrl = "https://" + BimmerConstants.EADRAX_SERVER_MAP.get(configuration.region)
                + "/eadrax-chs/v1/charging-sessions?" + params;

        byte[] chargeSessionsResponse = get(chargeSessionsUrl, brand, vin, HTTPConstants.CONTENT_TYPE_JSON);

        return JsonStringDeserializer.getChargeSessions(new String(chargeSessionsResponse));
    }

    public ExecutionStatusContainer executeRemoteServiceCall(String vin, String brand, RemoteService service)
            throws NetworkException {
        String executionUrl = remoteCommandUrl + vin + "/" + service.getCommand();

        byte[] response = post(executionUrl, brand, vin, HTTPConstants.CONTENT_TYPE_JSON, service.getBody());

        return JsonStringDeserializer.getExecutionStatus(new String(response));
    }

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
     * @param coding
     * @param params
     * @param brand
     * @param contentType
     * @return
     */
    private byte[] get(String url, final String brand, @Nullable String vin, String contentType)
            throws NetworkException {
        return call(url, false, brand, vin, contentType, null);
    }

    /**
     * prepares a POST request to the backend
     *
     * @param url
     * @param coding
     * @param params
     * @param brand
     * @param contentType
     * @return
     */
    private byte[] post(String url, final String brand, @Nullable String vin, String contentType, @Nullable String body)
            throws NetworkException {
        return call(url, true, brand, vin, contentType, body);
    }

    /**
     * executes the real call to the backend
     *
     * @param url
     * @param post
     * @param encoding
     * @param queryParams
     * @param brand
     * @param contentType
     * @return
     */
    private synchronized byte[] call(final String url, final boolean post, final String brand,
            final @Nullable String vin, final String contentType, final @Nullable String body) throws NetworkException {
        byte[] responseByteArray = "".getBytes();

        // return in case of unknown brand
        if (!BimmerConstants.REQUESTED_BRANDS.contains(brand.toLowerCase())) {
            logger.warn("Unknown Brand {}", brand);
            throw new NetworkException("Unknown Brand " + brand);
        }

        final Request req;

        if (post) {
            req = httpClient.POST(url);
        } else {
            req = httpClient.newRequest(url);
        }

        req.header(HttpHeader.AUTHORIZATION, myBMWTokenHandler.getToken().getBearerToken());
        req.header(HTTPConstants.HEADER_X_USER_AGENT,
                String.format(BimmerConstants.X_USER_AGENT, brand, configuration.region));
        req.header(HttpHeader.ACCEPT_LANGUAGE, configuration.language);
        req.header(HttpHeader.ACCEPT, contentType);

        try {
            ContentResponse response = req.timeout(HTTPConstants.HTTP_TIMEOUT_SEC, TimeUnit.SECONDS).send();
            if (response.getStatus() >= 300) {
                responseByteArray = "".getBytes();
                NetworkException exception = new NetworkException(url, response.getStatus(),
                        ResponseContentAnonymizer.anonymizeResponseContent(response.getContentAsString()), body);
                logResponse(ResponseContentAnonymizer.replaceVin(exception.getUrl(), vin), exception.getReason(),
                        ResponseContentAnonymizer.replaceVin(body, vin));
                throw exception;
            } else {
                responseByteArray = response.getContent();

                // don't print images
                if (!HTTPConstants.CONTENT_TYPE_IMAGE.equals(contentType)) {
                    logResponse(ResponseContentAnonymizer.replaceVin(url, vin),
                            ResponseContentAnonymizer.anonymizeResponseContent(response.getContentAsString()),
                            ResponseContentAnonymizer.replaceVin(body, vin));
                }
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logResponse(ResponseContentAnonymizer.replaceVin(url, vin), e.getMessage(),
                    ResponseContentAnonymizer.replaceVin(body, vin));
            throw new NetworkException(url, -1, null, body, e);
        }

        return responseByteArray;
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
