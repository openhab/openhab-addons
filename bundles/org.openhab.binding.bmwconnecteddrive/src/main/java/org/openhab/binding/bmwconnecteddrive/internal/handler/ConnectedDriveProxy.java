/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.bmwconnecteddrive.internal.handler;

import static org.openhab.binding.bmwconnecteddrive.internal.utils.HTTPConstants.*;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.UrlEncoded;
import org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConfiguration;
import org.openhab.binding.bmwconnecteddrive.internal.VehicleConfiguration;
import org.openhab.binding.bmwconnecteddrive.internal.dto.NetworkError;
import org.openhab.binding.bmwconnecteddrive.internal.utils.BimmerConstants;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ConnectedDriveProxy} This class holds the important constants for the BMW Connected Drive Authorization.
 * They
 * are taken from the Bimmercode from github {@link https://github.com/bimmerconnected/bimmer_connected}
 * File defining these constants
 * {@link https://github.com/bimmerconnected/bimmer_connected/blob/master/bimmer_connected/account.py}
 * https://customer.bmwgroup.com/one/app/oauth.js
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ConnectedDriveProxy {
    private final Logger logger = LoggerFactory.getLogger(ConnectedDriveProxy.class);
    private static final int TIMEOUT_SEC = 10;
    private HttpClient http;
    private String authUri;
    private ConnectedDriveConfiguration configuration;
    private Token token = new Token();

    // Connected Drive APIs
    // Base URL without VIN
    String baseUrl;
    // APIs to be attached after VIN insertion
    String vehicleStatusAPI = "/status";
    String lastTripAPI = "/statistics/lastTrip";
    String allTripsAPI = "/statistics/allTrips";
    String chargeAPI = "/chargingprofile";
    String destinationAPI = "/destinations";
    String imageAPI = "/image";
    String rangeMapAPI = "/rangemap";

    String serviceExecutionAPI = "/executeService";
    String serviceExecutionStateAPI = "/serviceExecutionStatus?serviceType=";

    public ConnectedDriveProxy(HttpClient hc, ConnectedDriveConfiguration config) {
        http = hc;
        configuration = config;
        // generate URI for Authorization
        // https://customer.bmwgroup.com/one/app/oauth.js
        StringBuffer uri = new StringBuffer();
        uri.append("https://customer.bmwgroup.com");
        if (BimmerConstants.SERVER_NORTH_AMERICA.equals(configuration.region)) {
            uri.append("/gcdm/usa/oauth/authenticate");
        } else {
            uri.append("/gcdm/oauth/authenticate");
        }
        authUri = uri.toString();
        baseUrl = "https://" + getRegionServer() + "/webapi/v1/user/vehicles/";
    }

    public void request(String url, Optional<MultiMap<String>> params, ResponseCallback callback) {
        if (tokenUpdate()) {
            final StringBuffer completeUrl = new StringBuffer(url);
            if (params.isPresent()) {
                String urlEncodedData = UrlEncoded.encode(params.get(), Charset.defaultCharset(), false);
                completeUrl.append("?").append(urlEncodedData);
                // req.header(HttpHeader.CONTENT_TYPE, CONTENT_TYPE_URL_ENCODED);
                // req.header(CONTENT_LENGTH, Integer.toString(urlEncodedData.length()));
                // req.content(new StringContentProvider(urlEncodedData));
                // logger.info("URL has parameters {}", urlEncodedData);
                logger.info("Params for request: {}", urlEncodedData);
            } else {
                logger.info("No Params for request: {}", url);
            }
            logger.info("Complete URL {}", completeUrl.toString());
            Request req = http.newRequest(completeUrl.toString());
            req.header(HttpHeader.CONNECTION, KEEP_ALIVE);
            req.header(HttpHeader.AUTHORIZATION, token.getBearerToken());
            req.timeout(TIMEOUT_SEC, TimeUnit.SECONDS).send(new BufferingResponseListener() {
                @NonNullByDefault({})
                @Override
                public void onComplete(org.eclipse.jetty.client.api.Result result) {
                    if (result.getResponse().getStatus() != 200) {
                        NetworkError error = new NetworkError();
                        error.url = completeUrl.toString();
                        error.status = result.getResponse().getStatus();
                        error.reason = result.getResponse().getReason();
                        logger.warn("{}", error.toString());
                        callback.onError(error);
                    } else {
                        if (callback instanceof StringResponseCallback) {
                            ((StringResponseCallback) callback).onResponse(Optional.of(getContentAsString()));
                        } else {
                            ((ByteResponseCallback) callback).onResponse(Optional.of(getContent()));
                        }
                    }
                }
            });
        }
    }

    public void requestVehicles(StringResponseCallback callback) {
        request(baseUrl, Optional.empty(), callback);
    }

    public void requestVehcileStatus(VehicleConfiguration config, StringResponseCallback callback) {
        request(new StringBuffer(baseUrl).append(config.vin).append(vehicleStatusAPI).toString(), Optional.empty(),
                callback);
    }

    public void requestLastTrip(VehicleConfiguration config, StringResponseCallback callback) {
        request(new StringBuffer(baseUrl).append(config.vin).append(lastTripAPI).toString(), Optional.empty(),
                callback);
    }

    public void requestAllTrips(VehicleConfiguration config, StringResponseCallback callback) {
        request(new StringBuffer(baseUrl).append(config.vin).append(allTripsAPI).toString(), Optional.empty(),
                callback);
    }

    public void requestChargingProfile(VehicleConfiguration config, StringResponseCallback callback) {
        request(new StringBuffer(baseUrl).append(config.vin).append(chargeAPI).toString(), Optional.empty(), callback);
    }

    public void requestDestinations(VehicleConfiguration config, StringResponseCallback callback) {
        request(new StringBuffer(baseUrl).append(config.vin).append(destinationAPI).toString(), Optional.empty(),
                callback);
    }

    public void requestRangeMap(VehicleConfiguration config, Optional<MultiMap<String>> params,
            StringResponseCallback callback) {
        request(new StringBuffer(baseUrl).append(config.vin).append(rangeMapAPI).toString(), params, callback);
    }

    public void requestImage(VehicleConfiguration config, ByteResponseCallback callback) {
        // String localImageUrl = baseUrl + config.vin + imageAPI + "?width=" + config.imageSize + "&height="
        // + config.imageSize + "&view=" + config.imageViewport;

        // https://b2vapi.bmwgroup.com/webapi/v1/user/vehicles/WBY1Z81040V905639/image view=SIDE&width=2048&height=2048
        // https://b2vapi.bmwgroup.com/webapi/v1/user/vehicles/WBY1Z81040V905639/image?view=SIDE&width=2048&height=2048

        String localImageUrl = new StringBuffer(baseUrl).append(config.vin).append(imageAPI).toString();
        MultiMap<String> dataMap = new MultiMap<String>();
        dataMap.add("width", Integer.toString(config.imageSize));
        dataMap.add("height", Integer.toString(config.imageSize));
        dataMap.add("view", config.imageViewport);
        request(localImageUrl, Optional.of(dataMap), callback);
    }

    private String getRegionServer() {
        if (BimmerConstants.SERVER_MAP.containsKey(configuration.region)) {
            return BimmerConstants.SERVER_MAP.get(configuration.region);
        } else {
            return Constants.INVALID;
        }
    }

    RemoteServiceHandler getRemoteServiceHandler(VehicleHandler vehicleHandler) {
        return new RemoteServiceHandler(vehicleHandler, this, http);
    }

    // Token handling

    public synchronized boolean tokenUpdate() {
        if (token.isExpired() || !token.isValid()) {
            token = getToken();
            if (token.isExpired() || !token.isValid()) {
                logger.info("Token update failed!");
                return false;
            }
        }
        return true;
    }

    public Token getToken() {
        if (token.isExpired() || !token.isValid()) {
            token = getNewToken();
        }
        return token;
    }

    /**
     * Authorize at BMW Connected Drive Portal and get Token
     *
     * @return
     */
    public Token getNewToken() {
        http.setFollowRedirects(false);
        Request req = http.POST(authUri);

        req.header(HttpHeader.CONTENT_TYPE, CONTENT_TYPE_URL_ENCODED);
        req.header(HttpHeader.CONNECTION, KEEP_ALIVE);
        req.header(HttpHeader.HOST, BimmerConstants.SERVER_MAP.get(configuration.region));
        req.header(HttpHeader.AUTHORIZATION, BimmerConstants.AUTHORIZATION_VALUE);
        req.header(CREDENTIALS, BimmerConstants.CREDENTIAL_VALUES);

        MultiMap<String> dataMap = new MultiMap<String>();
        dataMap.add(CLIENT_ID, BimmerConstants.CLIENT_ID_VALUE);
        dataMap.add(RESPONSE_TYPE, TOKEN);
        dataMap.add(REDIRECT_URI, BimmerConstants.REDIRECT_URI_VALUE);
        dataMap.add(SCOPE, BimmerConstants.SCOPE_VALUES);
        dataMap.add(USERNAME, configuration.userName);
        dataMap.add(PASSWORD, configuration.password);
        String urlEncodedData = UrlEncoded.encode(dataMap, Charset.defaultCharset(), false);
        req.header(CONTENT_LENGTH, Integer.toString(urlEncodedData.length()));
        req.content(new StringContentProvider(urlEncodedData));
        try {
            ContentResponse contentResponse = req.timeout(TIMEOUT_SEC, TimeUnit.SECONDS).send();
            logger.info("Status {} ", contentResponse.getStatus());
            HttpFields fields = contentResponse.getHeaders();
            HttpField field = fields.getField(HttpHeader.LOCATION);
            http.setFollowRedirects(true);
            return getTokenFromUrl(field.getValue());
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.warn("Auth Exception: {}", e.getMessage());
        }
        http.setFollowRedirects(true);
        return new Token();
    }

    @SuppressWarnings("null")
    public Token getTokenFromUrl(String encodedUrl) {
        MultiMap<String> tokenMap = new MultiMap<String>();
        UrlEncoded.decodeTo(encodedUrl, tokenMap, StandardCharsets.US_ASCII);
        final Token token = new Token();
        tokenMap.forEach((key, value) -> {
            if (value.size() > 0) {
                String val = value.get(0);
                if (key.endsWith(ACCESS_TOKEN)) {
                    token.setToken(val.toString());
                } else if (key.equals(EXPIRES_IN)) {
                    token.setExpiration(Integer.parseInt(val.toString()));
                } else if (key.equals(TOKEN_TYPE)) {
                    token.setType(val.toString());
                }
            }
        });
        return token;
    }
}
