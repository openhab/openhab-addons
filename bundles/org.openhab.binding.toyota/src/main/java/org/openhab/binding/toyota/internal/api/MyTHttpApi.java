/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.toyota.internal.api;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.MimeTypes;
import org.openhab.binding.toyota.internal.ToyotaException;
import org.openhab.binding.toyota.internal.config.ApiBridgeConfiguration;
import org.openhab.binding.toyota.internal.deserialization.MyTDeserializer;
import org.openhab.binding.toyota.internal.dto.CredentialResponse;
import org.openhab.binding.toyota.internal.dto.CustomerProfile;
import org.openhab.binding.toyota.internal.dto.Vehicle;
import org.openhab.core.io.net.http.HttpClientFactory;

import com.google.gson.reflect.TypeToken;

/**
 * {@link MyTHttpApi} wraps the VolvoOnCall REST API.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class MyTHttpApi {
    private static final int TIMEOUT_MS = 10000;
    // https://github.com/TA2k/ioBroker.toyota/blob/master/main.js
    // Pas mal de endpoints à tester ici :
    // https://github.com/DurgNomis-drol/mytoyota/blob/master/mytoyota/api.py
    // https://github.com/calmjm/tojota/blob/master/tojota.py
    private static final String BASE_URL = "https://myt-agg.toyota-europe.com/cma/api";
    private static final String LOGIN_URL = BASE_URL + "/user/login";
    private static final String VEHICLE_LIST_URL = BASE_URL + "/user/%s/vehicle/details";
    private static final String VEHICLE_STATUS_URL = BASE_URL + "/users/%s/vehicles/%s/vehicleStatus";
    private static final String VEHICLE_LOCATION_URL = BASE_URL + "/users/%s/vehicle/location";
    private static final String VEHICLE_PARKING_URL = BASE_URL + "/users/%s/vehicles/%s/parking";
    private static final String VEHICLE_INFO_URL = BASE_URL + "/vehicle/%s/addtionalInfo";
    private static final String STATISTICS_URL = BASE_URL + "/v2/trips/summarize&from=2020-11-01&calendarInterval=week"; // ou
    // day

    // private static final String REMOTE_CONTROL_URL = "/vehicles/%s/remoteControl/status";

    private record Credentials(String password, String username) {
        public static Type ANSWER_CLASS = new TypeToken<CredentialResponse>() {
        }.getType();
    }

    private final MyTDeserializer deserializer;
    private final HttpClient httpClient;

    private Optional<String> token = Optional.empty();

    public MyTHttpApi(String clientName, MyTDeserializer deserializer, HttpClientFactory httpClientFactory)
            throws ToyotaException {
        this.deserializer = deserializer;
        this.httpClient = httpClientFactory.createHttpClient(clientName);
        try {
            httpClient.start();
        } catch (Exception e) {
            throw new ToyotaException("Unable to start Jetty HttpClient", e);
        }
    }

    public void dispose() throws Exception {
        httpClient.stop();
        token = Optional.empty();
    }

    public CustomerProfile initialize(ApiBridgeConfiguration configuration) throws ToyotaException {
        Credentials credentials = new Credentials(configuration.password, configuration.username);
        CredentialResponse response = getResponse(HttpMethod.POST, LOGIN_URL, Credentials.ANSWER_CLASS, credentials);
        token = Optional.of(response.token);
        return response.customerProfile;
    }

    public List<Vehicle> getVehicles(String uuid) throws ToyotaException {
        return getResponse(HttpMethod.GET, VEHICLE_LIST_URL.formatted(uuid), Vehicle.LIST_CLASS, null);
    }

    // private void backup() throws ToyotaException {
    // String result = getResponse(HttpMethod.GET, VEHICLE_LIST_URL.formatted(uuid.get()), null);
    // List<Vehicle> vehicles = deserializer.deserialize(Vehicle.LIST_CLASS, result);
    //
    // vin = Optional.of(vehicles.get(0).vin);
    //
    // result = getResponse(HttpMethod.GET, VEHICLE_STATUS_URL.formatted(uuid.get(), vin.get()), null);
    // StatusResponse status = deserializer.deserialize(StatusResponse.class, result);
    //
    // result = getResponse(HttpMethod.GET, VEHICLE_INFO_URL.formatted(vin.get()), null);
    // List<Metrics> metrics = deserializer.deserialize(Metrics.LIST_CLASS, result);
    //
    // result = getResponse(HttpMethod.GET, VEHICLE_LOCATION_URL.formatted(uuid.get()), null); //
    // LocationResponse location = deserializer.deserialize(LocationResponse.class, result);
    //
    // result = getResponse(HttpMethod.GET, VEHICLE_PARKING_URL.formatted(uuid.get(), vin.get()), null);
    // result = getResponse(HttpMethod.GET, STATISTICS_URL, null);
    // // result = getResponse(HttpMethod.GET, REMOTE_CONTROL_URL.formatted(vin.get()), null);
    // //
    // // {"timestamp":1688395628720,"status":400,"error":"Bad Request","message":"Missing request header 'VIN' for
    // // method parameter of type String","errorCode":"CMA400"}
    // logger.debug(metrics.toString());
    // }

    @SuppressWarnings("unchecked")
    private <T> T getResponse(HttpMethod method, String url, Type answerClazz, @Nullable Object jsonObject)
            throws ToyotaException {
        Request request = httpClient.newRequest(url) //
                .header(HttpHeader.ACCEPT_LANGUAGE, "de-DE") //
                .header(HttpHeader.ACCEPT, "*/*") //
                .header(HttpHeader.USER_AGENT, "MyT/4.10.0 iPhone10,5 iOS/14.8 CFNetwork/1240.0.4 Darwin/20.6.0")
                .header("x-tme-brand", "TOYOTA") //
                .header("x-tme-app-version", "4.10.0") //
                .header("x-tme-locale", "en-gb").timeout(TIMEOUT_MS, TimeUnit.MILLISECONDS);

        token.ifPresent(cookie -> request.header(HttpHeader.COOKIE, "iPlanetDirectoryPro=" + cookie));
        // uuid.ifPresent(id -> request.header("uuid", id));
        // vin.ifPresent(id -> request.header("VIN", id));

        if (jsonObject != null) {
            MimeTypes.Type jsonMimeType = MimeTypes.Type.APPLICATION_JSON;
            request.header(HttpHeader.CONTENT_TYPE, jsonMimeType.asString());
            String json = deserializer.toJson(jsonObject);
            ContentProvider content = new StringContentProvider(jsonMimeType.asString(), json,
                    jsonMimeType.getCharset());
            request.content(content);
        }

        try {
            ContentResponse contentResponse = request.method(method).send();
            String stringContent = contentResponse.getContentAsString();
            return (T) deserializer.deserializeSingle(answerClazz, stringContent);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new ToyotaException(e, "Error requesting %s", url);
        }
    }

    // private <T extends VocAnswer> T callUrl(HttpMethod method, String endpoint, Class<T> objectClass,
    // @Nullable String body) throws VolvoOnCallException {
    // try {
    // String url = endpoint.startsWith("http") ? endpoint : SERVICE_URL + endpoint;
    // String jsonResponse = method == HttpMethod.GET
    // ? cache.putIfAbsentAndGet(endpoint, () -> getResponse(method, url, body))
    // : getResponse(method, url, body);
    // if (jsonResponse == null) {
    // throw new IOException();
    // } else {
    // logger.debug("Request to `{}` answered : {}", url, jsonResponse);
    // T responseDTO = Objects.requireNonNull(gson.fromJson(jsonResponse, objectClass));
    // String error = responseDTO.getErrorLabel();
    // if (error != null) {
    // throw new VolvoOnCallException(error, responseDTO.getErrorDescription());
    // }
    // return responseDTO;
    // }
    // } catch (JsonSyntaxException | IOException e) {
    // throw new VolvoOnCallException(e);
    // }
    // }
    //
    // public <T extends VocAnswer> T getURL(String endpoint, Class<T> objectClass) throws VolvoOnCallException {
    // return callUrl(HttpMethod.GET, endpoint, objectClass, null);
    // }
    //
    // public @Nullable PostResponse postURL(String endpoint, @Nullable String body) throws VolvoOnCallException {
    // try {
    // return callUrl(HttpMethod.POST, endpoint, PostResponse.class, body);
    // } catch (VolvoOnCallException e) {
    // if (e.getType() == ErrorType.SERVICE_UNABLE_TO_START) {
    // logger.info("Unable to start service request sent to VoC");
    // return null;
    // } else {
    // throw e;
    // }
    // }
    // }
    //
    // public <T extends VocAnswer> T getURL(Class<T> objectClass, String vin) throws VolvoOnCallException {
    // String url = String.format("vehicles/%s/%s", vin, objectClass.getSimpleName().toLowerCase());
    // return getURL(url, objectClass);
    // }
}
