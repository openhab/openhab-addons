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
package org.openhab.binding.tado.swagger.codegen.api.client;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.tado.swagger.codegen.api.ApiException;
import org.openhab.binding.tado.swagger.codegen.api.auth.Authorizer;
import org.openhab.binding.tado.swagger.codegen.api.model.GenericZoneCapabilities;
import org.openhab.binding.tado.swagger.codegen.api.model.HomeInfo;
import org.openhab.binding.tado.swagger.codegen.api.model.HomePresence;
import org.openhab.binding.tado.swagger.codegen.api.model.HomeState;
import org.openhab.binding.tado.swagger.codegen.api.model.MobileDevice;
import org.openhab.binding.tado.swagger.codegen.api.model.Overlay;
import org.openhab.binding.tado.swagger.codegen.api.model.OverlayTemplate;
import org.openhab.binding.tado.swagger.codegen.api.model.User;
import org.openhab.binding.tado.swagger.codegen.api.model.Zone;
import org.openhab.binding.tado.swagger.codegen.api.model.ZoneState;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Static imported copy of class created by Swagger Codegen
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
public class HomeApi {
    private static final HttpClient CLIENT = new HttpClient(new SslContextFactory());

    private String baseUrl = "https://my.tado.com/api/v2";
    private int timeout = 5000;

    private Gson gson;
    private Authorizer authorizer;

    public HomeApi(Gson gson, Authorizer authorizer) {
        this.gson = gson;
        this.authorizer = authorizer;
    }

    public void deleteZoneOverlay(Long homeId, Long zoneId) throws IOException, ApiException {

        // verify the required parameter 'homeId' is set
        if (homeId == null) {
            throw new ApiException(400, "Missing the required parameter 'homeId' when calling deleteZoneOverlay");
        }

        // verify the required parameter 'zoneId' is set
        if (zoneId == null) {
            throw new ApiException(400, "Missing the required parameter 'zoneId' when calling deleteZoneOverlay");
        }

        startHttpClient(CLIENT);

        // create path and map variables
        String path = "/homes/{home_id}/zones/{zone_id}/overlay"
                .replaceAll("\\{" + "home_id" + "\\}", homeId.toString())
                .replaceAll("\\{" + "zone_id" + "\\}", zoneId.toString());

        Request request = CLIENT.newRequest(baseUrl + path).method(HttpMethod.DELETE).timeout(timeout,
                TimeUnit.MILLISECONDS);

        request.accept("application/json");
        request.header(HttpHeader.USER_AGENT, "openhab/swagger-java/1.0.0");

        if (authorizer != null) {
            authorizer.addAuthorization(request);
        }

        ContentResponse response;
        try {
            response = request.send();
        } catch (Exception e) {
            throw new IOException(e);
        }

        int statusCode = response.getStatus();
        if (statusCode >= HttpStatus.BAD_REQUEST_400) {
            throw new ApiException(response, "Operation deleteZoneOverlay failed with error " + statusCode);
        }
    }

    public HomeState homeState(Long homeId) throws IOException, ApiException {

        // verify the required parameter 'homeId' is set
        if (homeId == null) {
            throw new ApiException(400, "Missing the required parameter 'homeId' when calling homeState");
        }

        startHttpClient(CLIENT);

        // create path and map variables
        String path = "/homes/{home_id}/state".replaceAll("\\{" + "home_id" + "\\}", homeId.toString());

        Request request = CLIENT.newRequest(baseUrl + path).method(HttpMethod.GET).timeout(timeout,
                TimeUnit.MILLISECONDS);

        request.accept("application/json");
        request.header(HttpHeader.USER_AGENT, "openhab/swagger-java/1.0.0");

        if (authorizer != null) {
            authorizer.addAuthorization(request);
        }

        ContentResponse response;
        try {
            response = request.send();
        } catch (Exception e) {
            throw new IOException(e);
        }

        int statusCode = response.getStatus();
        if (statusCode >= HttpStatus.BAD_REQUEST_400) {
            throw new ApiException(response, "Operation homeState failed with error " + statusCode);
        }

        Type returnType = new TypeToken<HomeState>() {
        }.getType();
        return gson.fromJson(response.getContentAsString(), returnType);
    }

    public List<MobileDevice> listMobileDevices(Long homeId) throws IOException, ApiException {

        // verify the required parameter 'homeId' is set
        if (homeId == null) {
            throw new ApiException(400, "Missing the required parameter 'homeId' when calling listMobileDevices");
        }

        startHttpClient(CLIENT);

        // create path and map variables
        String path = "/homes/{home_id}/mobileDevices".replaceAll("\\{" + "home_id" + "\\}", homeId.toString());

        Request request = CLIENT.newRequest(baseUrl + path).method(HttpMethod.GET).timeout(timeout,
                TimeUnit.MILLISECONDS);

        request.accept("application/json");
        request.header(HttpHeader.USER_AGENT, "openhab/swagger-java/1.0.0");

        if (authorizer != null) {
            authorizer.addAuthorization(request);
        }

        ContentResponse response;
        try {
            response = request.send();
        } catch (Exception e) {
            throw new IOException(e);
        }

        int statusCode = response.getStatus();
        if (statusCode >= HttpStatus.BAD_REQUEST_400) {
            throw new ApiException(response, "Operation listMobileDevices failed with error " + statusCode);
        }

        Type returnType = new TypeToken<List<MobileDevice>>() {
        }.getType();
        return gson.fromJson(response.getContentAsString(), returnType);
    }

    public List<Zone> listZones(Long homeId) throws IOException, ApiException {

        // verify the required parameter 'homeId' is set
        if (homeId == null) {
            throw new ApiException(400, "Missing the required parameter 'homeId' when calling listZones");
        }

        startHttpClient(CLIENT);

        // create path and map variables
        String path = "/homes/{home_id}/zones".replaceAll("\\{" + "home_id" + "\\}", homeId.toString());

        Request request = CLIENT.newRequest(baseUrl + path).method(HttpMethod.GET).timeout(timeout,
                TimeUnit.MILLISECONDS);

        request.accept("application/json");
        request.header(HttpHeader.USER_AGENT, "openhab/swagger-java/1.0.0");

        if (authorizer != null) {
            authorizer.addAuthorization(request);
        }

        ContentResponse response;
        try {
            response = request.send();
        } catch (Exception e) {
            throw new IOException(e);
        }

        int statusCode = response.getStatus();
        if (statusCode >= HttpStatus.BAD_REQUEST_400) {
            throw new ApiException(response, "Operation listZones failed with error " + statusCode);
        }

        Type returnType = new TypeToken<List<Zone>>() {
        }.getType();
        return gson.fromJson(response.getContentAsString(), returnType);
    }

    public HomeInfo showHome(Long homeId) throws IOException, ApiException {

        // verify the required parameter 'homeId' is set
        if (homeId == null) {
            throw new ApiException(400, "Missing the required parameter 'homeId' when calling showHome");
        }

        startHttpClient(CLIENT);

        // create path and map variables
        String path = "/homes/{home_id}".replaceAll("\\{" + "home_id" + "\\}", homeId.toString());

        Request request = CLIENT.newRequest(baseUrl + path).method(HttpMethod.GET).timeout(timeout,
                TimeUnit.MILLISECONDS);

        request.accept("application/json");
        request.header(HttpHeader.USER_AGENT, "openhab/swagger-java/1.0.0");

        if (authorizer != null) {
            authorizer.addAuthorization(request);
        }

        ContentResponse response;
        try {
            response = request.send();
        } catch (Exception e) {
            throw new IOException(e);
        }

        int statusCode = response.getStatus();
        if (statusCode >= HttpStatus.BAD_REQUEST_400) {
            throw new ApiException(response, "Operation showHome failed with error " + statusCode);
        }

        Type returnType = new TypeToken<HomeInfo>() {
        }.getType();
        return gson.fromJson(response.getContentAsString(), returnType);
    }

    public User showUser() throws IOException, ApiException {

        startHttpClient(CLIENT);

        // create path and map variables
        String path = "/me";

        Request request = CLIENT.newRequest(baseUrl + path).method(HttpMethod.GET).timeout(timeout,
                TimeUnit.MILLISECONDS);

        request.accept("application/json");
        request.header(HttpHeader.USER_AGENT, "openhab/swagger-java/1.0.0");

        if (authorizer != null) {
            authorizer.addAuthorization(request);
        }

        ContentResponse response;
        try {
            response = request.send();
        } catch (Exception e) {
            throw new IOException(e);
        }

        int statusCode = response.getStatus();
        if (statusCode >= HttpStatus.BAD_REQUEST_400) {
            throw new ApiException(response, "Operation showUser failed with error " + statusCode);
        }

        Type returnType = new TypeToken<User>() {
        }.getType();
        return gson.fromJson(response.getContentAsString(), returnType);
    }

    public GenericZoneCapabilities showZoneCapabilities(Long homeId, Long zoneId) throws IOException, ApiException {

        // verify the required parameter 'homeId' is set
        if (homeId == null) {
            throw new ApiException(400, "Missing the required parameter 'homeId' when calling showZoneCapabilities");
        }

        // verify the required parameter 'zoneId' is set
        if (zoneId == null) {
            throw new ApiException(400, "Missing the required parameter 'zoneId' when calling showZoneCapabilities");
        }

        startHttpClient(CLIENT);

        // create path and map variables
        String path = "/homes/{home_id}/zones/{zone_id}/capabilities"
                .replaceAll("\\{" + "home_id" + "\\}", homeId.toString())
                .replaceAll("\\{" + "zone_id" + "\\}", zoneId.toString());

        Request request = CLIENT.newRequest(baseUrl + path).method(HttpMethod.GET).timeout(timeout,
                TimeUnit.MILLISECONDS);

        request.accept("application/json");
        request.header(HttpHeader.USER_AGENT, "openhab/swagger-java/1.0.0");

        if (authorizer != null) {
            authorizer.addAuthorization(request);
        }

        ContentResponse response;
        try {
            response = request.send();
        } catch (Exception e) {
            throw new IOException(e);
        }

        int statusCode = response.getStatus();
        if (statusCode >= HttpStatus.BAD_REQUEST_400) {
            throw new ApiException(response, "Operation showZoneCapabilities failed with error " + statusCode);
        }

        Type returnType = new TypeToken<GenericZoneCapabilities>() {
        }.getType();
        return gson.fromJson(response.getContentAsString(), returnType);
    }

    public OverlayTemplate showZoneDefaultOverlay(Long homeId, Long zoneId) throws IOException, ApiException {

        // verify the required parameter 'homeId' is set
        if (homeId == null) {
            throw new ApiException(400, "Missing the required parameter 'homeId' when calling showZoneDefaultOverlay");
        }

        // verify the required parameter 'zoneId' is set
        if (zoneId == null) {
            throw new ApiException(400, "Missing the required parameter 'zoneId' when calling showZoneDefaultOverlay");
        }

        startHttpClient(CLIENT);

        // create path and map variables
        String path = "/homes/{home_id}/zones/{zone_id}/defaultOverlay"
                .replaceAll("\\{" + "home_id" + "\\}", homeId.toString())
                .replaceAll("\\{" + "zone_id" + "\\}", zoneId.toString());

        Request request = CLIENT.newRequest(baseUrl + path).method(HttpMethod.GET).timeout(timeout,
                TimeUnit.MILLISECONDS);

        request.accept("application/json");
        request.header(HttpHeader.USER_AGENT, "openhab/swagger-java/1.0.0");

        if (authorizer != null) {
            authorizer.addAuthorization(request);
        }

        ContentResponse response;
        try {
            response = request.send();
        } catch (Exception e) {
            throw new IOException(e);
        }

        int statusCode = response.getStatus();
        if (statusCode >= HttpStatus.BAD_REQUEST_400) {
            throw new ApiException(response, "Operation showZoneDefaultOverlay failed with error " + statusCode);
        }

        Type returnType = new TypeToken<OverlayTemplate>() {
        }.getType();
        return gson.fromJson(response.getContentAsString(), returnType);
    }

    public Zone showZoneDetails(Long homeId, Long zoneId) throws IOException, ApiException {

        // verify the required parameter 'homeId' is set
        if (homeId == null) {
            throw new ApiException(400, "Missing the required parameter 'homeId' when calling showZoneDetails");
        }

        // verify the required parameter 'zoneId' is set
        if (zoneId == null) {
            throw new ApiException(400, "Missing the required parameter 'zoneId' when calling showZoneDetails");
        }

        startHttpClient(CLIENT);

        // create path and map variables
        String path = "/homes/{home_id}/zones/{zone_id}/details"
                .replaceAll("\\{" + "home_id" + "\\}", homeId.toString())
                .replaceAll("\\{" + "zone_id" + "\\}", zoneId.toString());

        Request request = CLIENT.newRequest(baseUrl + path).method(HttpMethod.GET).timeout(timeout,
                TimeUnit.MILLISECONDS);

        request.accept("application/json");
        request.header(HttpHeader.USER_AGENT, "openhab/swagger-java/1.0.0");

        if (authorizer != null) {
            authorizer.addAuthorization(request);
        }

        ContentResponse response;
        try {
            response = request.send();
        } catch (Exception e) {
            throw new IOException(e);
        }

        int statusCode = response.getStatus();
        if (statusCode >= HttpStatus.BAD_REQUEST_400) {
            throw new ApiException(response, "Operation showZoneDetails failed with error " + statusCode);
        }

        Type returnType = new TypeToken<Zone>() {
        }.getType();
        return gson.fromJson(response.getContentAsString(), returnType);
    }

    public Overlay showZoneOverlay(Long homeId, Long zoneId) throws IOException, ApiException {

        // verify the required parameter 'homeId' is set
        if (homeId == null) {
            throw new ApiException(400, "Missing the required parameter 'homeId' when calling showZoneOverlay");
        }

        // verify the required parameter 'zoneId' is set
        if (zoneId == null) {
            throw new ApiException(400, "Missing the required parameter 'zoneId' when calling showZoneOverlay");
        }

        startHttpClient(CLIENT);

        // create path and map variables
        String path = "/homes/{home_id}/zones/{zone_id}/overlay"
                .replaceAll("\\{" + "home_id" + "\\}", homeId.toString())
                .replaceAll("\\{" + "zone_id" + "\\}", zoneId.toString());

        Request request = CLIENT.newRequest(baseUrl + path).method(HttpMethod.GET).timeout(timeout,
                TimeUnit.MILLISECONDS);

        request.accept("application/json");
        request.header(HttpHeader.USER_AGENT, "openhab/swagger-java/1.0.0");

        if (authorizer != null) {
            authorizer.addAuthorization(request);
        }

        ContentResponse response;
        try {
            response = request.send();
        } catch (Exception e) {
            throw new IOException(e);
        }

        int statusCode = response.getStatus();
        if (statusCode >= HttpStatus.BAD_REQUEST_400) {
            throw new ApiException(response, "Operation showZoneOverlay failed with error " + statusCode);
        }

        Type returnType = new TypeToken<Overlay>() {
        }.getType();
        return gson.fromJson(response.getContentAsString(), returnType);
    }

    public ZoneState showZoneState(Long homeId, Long zoneId) throws IOException, ApiException {

        // verify the required parameter 'homeId' is set
        if (homeId == null) {
            throw new ApiException(400, "Missing the required parameter 'homeId' when calling showZoneState");
        }

        // verify the required parameter 'zoneId' is set
        if (zoneId == null) {
            throw new ApiException(400, "Missing the required parameter 'zoneId' when calling showZoneState");
        }

        startHttpClient(CLIENT);

        // create path and map variables
        String path = "/homes/{home_id}/zones/{zone_id}/state".replaceAll("\\{" + "home_id" + "\\}", homeId.toString())
                .replaceAll("\\{" + "zone_id" + "\\}", zoneId.toString());

        Request request = CLIENT.newRequest(baseUrl + path).method(HttpMethod.GET).timeout(timeout,
                TimeUnit.MILLISECONDS);

        request.accept("application/json");
        request.header(HttpHeader.USER_AGENT, "openhab/swagger-java/1.0.0");

        if (authorizer != null) {
            authorizer.addAuthorization(request);
        }

        ContentResponse response;
        try {
            response = request.send();
        } catch (Exception e) {
            throw new IOException(e);
        }

        int statusCode = response.getStatus();
        if (statusCode >= HttpStatus.BAD_REQUEST_400) {
            throw new ApiException(response, "Operation showZoneState failed with error " + statusCode);
        }

        Type returnType = new TypeToken<ZoneState>() {
        }.getType();
        return gson.fromJson(response.getContentAsString(), returnType);
    }

    public void updatePresenceLock(Long homeId, HomePresence json) throws IOException, ApiException {

        // verify the required parameter 'homeId' is set
        if (homeId == null) {
            throw new ApiException(400, "Missing the required parameter 'homeId' when calling updatePresenceLock");
        }

        // verify the required parameter 'json' is set
        if (json == null) {
            throw new ApiException(400, "Missing the required parameter 'json' when calling updatePresenceLock");
        }

        startHttpClient(CLIENT);

        // create path and map variables
        String path = "/homes/{home_id}/presenceLock".replaceAll("\\{" + "home_id" + "\\}", homeId.toString());

        Request request = CLIENT.newRequest(baseUrl + path).method(HttpMethod.PUT).timeout(timeout,
                TimeUnit.MILLISECONDS);

        request.accept("application/json");
        request.header(HttpHeader.USER_AGENT, "openhab/swagger-java/1.0.0");

        if (authorizer != null) {
            authorizer.addAuthorization(request);
        }

        String serializedBody = gson.toJson(json);
        request.content(new StringContentProvider(serializedBody), "application/json");

        ContentResponse response;
        try {
            response = request.send();
        } catch (Exception e) {
            throw new IOException(e);
        }

        int statusCode = response.getStatus();
        if (statusCode >= HttpStatus.BAD_REQUEST_400) {
            throw new ApiException(response, "Operation updatePresenceLock failed with error " + statusCode);
        }
    }

    public Overlay updateZoneOverlay(Long homeId, Long zoneId, Overlay json) throws IOException, ApiException {

        // verify the required parameter 'homeId' is set
        if (homeId == null) {
            throw new ApiException(400, "Missing the required parameter 'homeId' when calling updateZoneOverlay");
        }

        // verify the required parameter 'zoneId' is set
        if (zoneId == null) {
            throw new ApiException(400, "Missing the required parameter 'zoneId' when calling updateZoneOverlay");
        }

        // verify the required parameter 'json' is set
        if (json == null) {
            throw new ApiException(400, "Missing the required parameter 'json' when calling updateZoneOverlay");
        }

        startHttpClient(CLIENT);

        // create path and map variables
        String path = "/homes/{home_id}/zones/{zone_id}/overlay"
                .replaceAll("\\{" + "home_id" + "\\}", homeId.toString())
                .replaceAll("\\{" + "zone_id" + "\\}", zoneId.toString());

        Request request = CLIENT.newRequest(baseUrl + path).method(HttpMethod.PUT).timeout(timeout,
                TimeUnit.MILLISECONDS);

        request.accept("application/json");
        request.header(HttpHeader.USER_AGENT, "openhab/swagger-java/1.0.0");

        if (authorizer != null) {
            authorizer.addAuthorization(request);
        }

        String serializedBody = gson.toJson(json);
        request.content(new StringContentProvider(serializedBody), "application/json");

        ContentResponse response;
        try {
            response = request.send();
        } catch (Exception e) {
            throw new IOException(e);
        }

        int statusCode = response.getStatus();
        if (statusCode >= HttpStatus.BAD_REQUEST_400) {
            throw new ApiException(response, "Operation updateZoneOverlay failed with error " + statusCode);
        }

        Type returnType = new TypeToken<Overlay>() {
        }.getType();
        return gson.fromJson(response.getContentAsString(), returnType);
    }

    private static void startHttpClient(HttpClient client) {
        if (!client.isStarted()) {
            try {
                client.start();
            } catch (Exception e) {
                // nothing we can do here
            }
        }
    }
}
