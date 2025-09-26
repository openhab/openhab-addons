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
package org.openhab.binding.smartthings.internal.api;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.sse.InboundSseEvent;
import javax.ws.rs.sse.SseEventSource;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.smartthings.internal.SmartthingsBindingConstants;
import org.openhab.binding.smartthings.internal.dto.AppRequest;
import org.openhab.binding.smartthings.internal.dto.AppResponse;
import org.openhab.binding.smartthings.internal.dto.Event;
import org.openhab.binding.smartthings.internal.dto.EventRegistration;
import org.openhab.binding.smartthings.internal.dto.OAuthConfigRequest;
import org.openhab.binding.smartthings.internal.dto.SmartthingsApp;
import org.openhab.binding.smartthings.internal.dto.SmartthingsCapability;
import org.openhab.binding.smartthings.internal.dto.SmartthingsDevice;
import org.openhab.binding.smartthings.internal.dto.SmartthingsLocation;
import org.openhab.binding.smartthings.internal.dto.SmartthingsRoom;
import org.openhab.binding.smartthings.internal.dto.SmartthingsStatus;
import org.openhab.binding.smartthings.internal.handler.SmartthingsBridgeHandler;
import org.openhab.binding.smartthings.internal.handler.SmartthingsThingHandler;
import org.openhab.binding.smartthings.internal.type.SmartthingsException;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.jaxrs.client.SseEventSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Class to handle Smartthings Web Api calls.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class SmartthingsApi {

    private final Logger logger = LoggerFactory.getLogger(SmartthingsApi.class);

    private final SmartthingsNetworkConnector networkConnector;
    private final SmartthingsBridgeHandler bridgeHandler;
    private final OAuthClientService oAuthClientService;
    private final ClientBuilder clientBuilder;
    private final SseEventSourceFactory eventSourceFactory;

    private static final String APP_NAME = "openhabnew0160";
    private Gson gson = new Gson();
    private String baseUrl = "https://api.smartthings.com/v1";

    private String deviceEndPoint = "/devices";
    private String appEndPoint = "/apps";
    private String locationEndPoint = "/locations";
    private String roomsEndPoint = "/rooms";
    private String capabilitiesEndPoint = "/capabilities";

    private Dictionary<String, SseEventSource> sseEvents;

    /**
     * Constructor.
     *
     * @param httpClientFactory The httpClientFactory
     * @param OAuthClientService The oAuthClientService
     * @param token The token to access the API
     */
    public SmartthingsApi(HttpClientFactory httpClientFactory, SmartthingsBridgeHandler bridgeHandler,
            SmartthingsNetworkConnector networkConnector, OAuthClientService oAuthClientService,
            ClientBuilder clientBuilder, SseEventSourceFactory eventSourceFactory) {
        this.networkConnector = networkConnector;
        this.bridgeHandler = bridgeHandler;
        this.oAuthClientService = oAuthClientService;
        this.clientBuilder = clientBuilder;
        this.eventSourceFactory = eventSourceFactory;
        this.sseEvents = new Hashtable<String, SseEventSource>();
    }

    public SmartthingsDevice[] getAllDevices() throws SmartthingsException {
        SmartthingsDevice[] devices = doRequest(SmartthingsDevice[].class, baseUrl + deviceEndPoint);
        return devices;
    }

    public AppResponse setupApp(String redirectUrl) throws SmartthingsException {
        SmartthingsApp[] appList = getAllApps();

        Optional<SmartthingsApp> appOptional = Arrays.stream(appList).filter(x -> APP_NAME.equals(x.appName))
                .findFirst();

        if (appOptional.isPresent()) {
            SmartthingsApp app = appOptional.get(); // Get it from optional
            app = getApp(app.appId);

            AppResponse result = new AppResponse();
            result.app = app;
            result.oauthClientId = null;
            result.oauthClientSecret = null;

            return result;
        } else {
            AppResponse result = createApp(redirectUrl);
            return result;
        }
    }

    public SmartthingsCapability[] getAllCapabilities() throws SmartthingsException {
        try {
            String uri = baseUrl + capabilitiesEndPoint;
            SmartthingsCapability[] listCapabilities = doRequest(SmartthingsCapability[].class, uri);
            return listCapabilities;
        } catch (final Exception e) {
            throw new SmartthingsException("SmartthingsApi : Unable to retrieve capabilities", e);
        }
    }

    public SmartthingsCapability getCapability(String capabilityId, String version,
            @Nullable SmartthingsNetworkCallback<SmartthingsCapability> cb) throws SmartthingsException {
        try {
            String uri = baseUrl + capabilitiesEndPoint + "/" + capabilityId + "/" + version;
            SmartthingsCapability capabilitie = doRequest(SmartthingsCapability.class, uri, cb);
            return capabilitie;
        } catch (final Exception e) {
            throw new SmartthingsException("SmartthingsApi : Unable to retrieve capability", e);
        }
    }

    public SmartthingsLocation[] getAllLocations() throws SmartthingsException {
        try {
            String uri = baseUrl + locationEndPoint;
            SmartthingsLocation[] listLocations = doRequest(SmartthingsLocation[].class, uri);
            return listLocations;
        } catch (final Exception e) {
            throw new SmartthingsException("SmartthingsApi : Unable to retrieve locations", e);
        }
    }

    public SmartthingsLocation getLocation(String locationId) throws SmartthingsException {
        try {
            String uri = baseUrl + locationEndPoint + "/" + locationId;

            SmartthingsLocation loc = doRequest(SmartthingsLocation.class, uri);

            return loc;
        } catch (final Exception e) {
            throw new SmartthingsException("SmartthingsApi : Unable to retrieve location", e);
        }
    }

    public SmartthingsRoom[] getRooms(String locationId) throws SmartthingsException {
        try {
            String uri = baseUrl + locationEndPoint + "/" + locationId + roomsEndPoint;
            SmartthingsRoom[] listRooms = doRequest(SmartthingsRoom[].class, uri);
            return listRooms;
        } catch (final Exception e) {
            throw new SmartthingsException("SmartthingsApi : Unable to retrieve rooms", e);
        }
    }

    public SmartthingsRoom getRoom(String locationId, String roomId) throws SmartthingsException {
        try {
            String uri = baseUrl + locationEndPoint + "/" + locationId + roomsEndPoint + "/" + roomId;

            SmartthingsRoom loc = doRequest(SmartthingsRoom.class, uri);

            return loc;
        } catch (final Exception e) {
            throw new SmartthingsException("SmartthingsApi : Unable to retrieve room", e);
        }
    }

    public SmartthingsApp[] getAllApps() throws SmartthingsException {
        try {
            String uri = baseUrl + appEndPoint;

            SmartthingsApp[] listApps = doRequest(SmartthingsApp[].class, uri);

            logger.info("");
            return listApps;
        } catch (final Exception e) {
            throw new SmartthingsException("SmartthingsApi : Unable to retrieve apps", e);
        }
    }

    public SmartthingsApp getApp(String appId) throws SmartthingsException {
        try {
            String uri = baseUrl + appEndPoint + "/" + appId;

            SmartthingsApp app = doRequest(SmartthingsApp.class, uri);

            return app;
        } catch (final Exception e) {
            throw new SmartthingsException("SmartthingsApi : Unable to retrieve app", e);
        }
    }

    public AppResponse createApp(String redirectUrl) throws SmartthingsException {
        try {
            String uri = baseUrl + appEndPoint + "?signatureType=ST_PADLOCK&requireConfirmation=true";
            String realRedirectUrl = redirectUrl + "/cb";

            String appName = APP_NAME;
            AppRequest appRequest = new AppRequest();
            appRequest.appName = appName;
            appRequest.displayName = appName;
            appRequest.description = "Desc " + appName;
            appRequest.appType = "WEBHOOK_SMART_APP";
            appRequest.webhookSmartApp = new AppRequest.webhookSmartApp(realRedirectUrl);
            appRequest.classifications = new String[1];
            appRequest.classifications[0] = "AUTOMATION";

            String body = gson.toJson(appRequest);
            AppResponse appResponse = doRequest(AppResponse.class, uri, body, false);

            return appResponse;
        } catch (final Exception e) {
            throw new SmartthingsException("SmartthingsApi : Unable to create app", e);
        }
    }

    public void createAppOAuth(String appId) throws SmartthingsException {
        try {
            String uri = baseUrl + appEndPoint + "/" + appId + "/oauth";

            OAuthConfigRequest oAuthConfig = new OAuthConfigRequest();
            oAuthConfig.clientName = "Openhab Integration";
            oAuthConfig.scope = new String[1];
            oAuthConfig.scope[0] = "r:devices:*";

            String body = gson.toJson(oAuthConfig);
            doRequest(JsonObject.class, uri, body, true);

            logger.info("");

            // return appResponse;
        } catch (final Exception e) {
            throw new SmartthingsException("SmartthingsApi : Unable to create oauth settings", e);
        }
    }

    public boolean isAuthorized() {
        final AccessTokenResponse accessTokenResponse = getAccessTokenResponse();

        return accessTokenResponse != null && accessTokenResponse.getAccessToken() != null
                && accessTokenResponse.getRefreshToken() != null;
    }

    protected @Nullable AccessTokenResponse getAccessTokenByClientCredentials() {
        try {
            OAuthClientService lcOAuthService = this.oAuthClientService;
            return lcOAuthService.getAccessTokenByClientCredentials(SmartthingsBindingConstants.SMARTTHINGS_SCOPES);
        } catch (OAuthException | IOException | OAuthResponseException | RuntimeException e) {
            logger.debug("Exception checking authorization: ", e);
            return null;
        }
    }

    protected @Nullable AccessTokenResponse getAccessTokenResponse() {
        try {
            OAuthClientService lcOAuthService = this.oAuthClientService;
            return lcOAuthService.getAccessTokenResponse();
        } catch (OAuthException | IOException | OAuthResponseException | RuntimeException e) {
            logger.debug("Exception checking authorization: ", e);
            return null;
        }
    }

    public String getToken() throws SmartthingsException {
        AccessTokenResponse accessTokenResponse = getAccessTokenResponse();

        // Store token is about to expire, ask for a new one.
        if (accessTokenResponse != null && accessTokenResponse.isExpired(Instant.now(), 1200)) {
            accessTokenResponse = null;
        }

        if (accessTokenResponse == null) {
            accessTokenResponse = getAccessTokenByClientCredentials();
        }

        final String accessToken = accessTokenResponse == null ? null : accessTokenResponse.getAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            throw new SmartthingsException(
                    "No Smartthings accesstoken. Did you authorize Smartthings via /connectsmartthings ?");
        }

        return accessToken;
    }

    public void sendCommand(String deviceId, String jsonMsg) throws SmartthingsException {
        try {
            String uri = baseUrl + deviceEndPoint + "/" + deviceId + "/commands";
            doRequest(JsonObject.class, uri, jsonMsg, false);
        } catch (final Exception e) {
            throw new SmartthingsException("SmartthingsApi : Unable to send command", e);
        }
    }

    public @Nullable SmartthingsStatus getStatus(String deviceId) throws SmartthingsException {
        try {
            String uri = baseUrl + deviceEndPoint + "/" + deviceId + "/status";

            SmartthingsStatus res = doRequest(SmartthingsStatus.class, uri, null, false);
            return res;
        } catch (final Exception e) {
            throw new SmartthingsException("SmartthingsApi : Unable to send status", e);
        }
    }

    public <T> T doRequest(Class<T> resultClass, String uri) throws SmartthingsException {
        return doRequest(resultClass, uri, null, null, false);
    }

    public <T> T doRequest(Class<T> resultClass, String uri, @Nullable SmartthingsNetworkCallback<T> callback)
            throws SmartthingsException {
        return doRequest(resultClass, uri, null, callback, false);
    }

    public <T> T doRequest(Class<T> resultClass, String uri, @Nullable String body, Boolean update)
            throws SmartthingsException {
        return doRequest(resultClass, uri, body, null, update);
    }

    public <T> T doRequest(Class<T> resultClass, String uri, @Nullable String body,
            @Nullable SmartthingsNetworkCallback<T> callback, Boolean update) throws SmartthingsException {
        try {
            HttpMethod httpMethod = HttpMethod.GET;
            if (body != null) {
                if (update) {
                    httpMethod = HttpMethod.PUT;
                } else {
                    httpMethod = HttpMethod.POST;
                }
            }
            T res = networkConnector.doRequest(resultClass, uri, callback, getToken(), body, httpMethod);
            return res;
        } catch (final Exception e) {
            throw new SmartthingsException("SmartthingsApi : Unable to do request", e);
        }
    }

    public void registerSubscription() {
        try {
            String installedAppId = "22d02ddc-5794-4347-99f1-75bae79bcefe";
            String subscriptionUri = "https://api.smartthings.com/subscriptions";

            SmartthingsLocation[] locationsObj = this.getAllLocations();

            String[] locations = new String[locationsObj.length];
            for (int idx = 0; idx < locationsObj.length; idx++) {
                locations[idx] = locationsObj[idx].locationId;
            }

            String[] eventTypes = { "DEVICE_EVENT", "DEVICE_LIFECYCLE_EVENT", "DEVICE_HEALTH_EVENT" };
            EventRegistration evtReg = new EventRegistration();
            evtReg.name = "Openhab sub";
            evtReg.version = 20250122;
            evtReg.clientDeviceId = "iapp_" + installedAppId;
            evtReg.subscriptionFilters = new EventRegistration.SubscriptionFilters[1];

            evtReg.subscriptionFilters[0] = new EventRegistration.SubscriptionFilters("LOCATIONIDS", locations,
                    eventTypes);

            String body = gson.toJson(evtReg);
            logger.info("body: {}", body);

            JsonObject result = networkConnector.doRequest(JsonObject.class, subscriptionUri, null, getToken(), body,
                    HttpMethod.POST);
            String uri = result.get("registrationUrl").getAsString();

            String token = getToken();
            Client client = clientBuilder.build().register(new ClientRequestFilter() {
                @Override
                public void filter(@Nullable ClientRequestContext requestContext) throws IOException {
                    if (requestContext != null) {
                        requestContext.getHeaders().add("Authorization", "Bearer " + token);
                    }
                }
            });

            String targetUrl = UriBuilder.fromUri(uri).build().toString();
            WebTarget target = client.target(targetUrl);

            SseEventSource source = eventSourceFactory.newBuilder(target).build();
            source.register(event -> onEvent(event), error -> onError(error), () -> onComplete());
            sseEvents.put(locations[0], source);
            source.open();

            logger.debug("result");
        } catch (Exception ex) {
            logger.debug("ex: {}", ex.toString());
        }
    }

    public void onEvent(InboundSseEvent event) {
        String data = event.readData();

        if (!data.contains("DEVICE_EVENT")) {
            return;
        }

        logger.info("Received: {}", event.readData());
        try {
            Event evt = gson.fromJson(data, Event.class);

            if (evt == null) {
                logger.info("Event decoding is null:");
                return;
            }
            String deviceId = evt.deviceEvent.deviceId;
            String componentId = evt.deviceEvent.componentId;
            String capa = evt.deviceEvent.capability;
            String attr = evt.deviceEvent.attribute;
            String value = evt.deviceEvent.value;

            Bridge bridge = bridgeHandler.getThing();
            List<Thing> things = bridge.getThings();

            Optional<Thing> theThingOpt = things.stream().filter(x -> x.getProperties().containsValue(deviceId))
                    .findFirst();
            if (theThingOpt.isPresent()) {
                Thing theThing = theThingOpt.get();

                ThingHandler handler = theThing.getHandler();
                SmartthingsThingHandler smarthingsHandler = (SmartthingsThingHandler) handler;
                if (smarthingsHandler != null) {
                    smarthingsHandler.refreshDevice(theThing.getThingTypeUID().getId(), componentId, capa, attr, value);
                }
            }
        } catch (Exception ex) {
            logger.info("Unable to decode json: {}", ex.toString());
            return;
        }
    }

    public void onError(Throwable onError) {
        logger.info("Exception: {}", onError.toString());
    }

    public void onComplete() {
        logger.info("Stream closed.");
    }

    public SmartthingsNetworkConnector getNetworkConnector() {
        return networkConnector;
    }
}
