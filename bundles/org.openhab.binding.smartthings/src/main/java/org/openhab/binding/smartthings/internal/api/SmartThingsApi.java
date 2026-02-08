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
import org.openhab.binding.smartthings.internal.SmartThingsBindingConstants;
import org.openhab.binding.smartthings.internal.dto.AppRequest;
import org.openhab.binding.smartthings.internal.dto.AppResponse;
import org.openhab.binding.smartthings.internal.dto.Event;
import org.openhab.binding.smartthings.internal.dto.EventRegistration;
import org.openhab.binding.smartthings.internal.dto.OAuthConfigRequest;
import org.openhab.binding.smartthings.internal.dto.SMEvent;
import org.openhab.binding.smartthings.internal.dto.SMEvent.device;
import org.openhab.binding.smartthings.internal.dto.SmartThingsApp;
import org.openhab.binding.smartthings.internal.dto.SmartThingsCapability;
import org.openhab.binding.smartthings.internal.dto.SmartThingsDevice;
import org.openhab.binding.smartthings.internal.dto.SmartThingsLocation;
import org.openhab.binding.smartthings.internal.dto.SmartThingsRoom;
import org.openhab.binding.smartthings.internal.dto.SmartThingsStatus;
import org.openhab.binding.smartthings.internal.handler.SmartThingsBridgeHandler;
import org.openhab.binding.smartthings.internal.handler.SmartThingsThingHandler;
import org.openhab.binding.smartthings.internal.type.SmartThingsException;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
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
 * Class to handle SmartThings Web API calls
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class SmartThingsApi {

    private final Logger logger = LoggerFactory.getLogger(SmartThingsApi.class);

    private final SmartThingsNetworkConnector networkConnector;
    private final SmartThingsBridgeHandler bridgeHandler;
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
    public SmartThingsApi(HttpClientFactory httpClientFactory, SmartThingsBridgeHandler bridgeHandler,
            SmartThingsNetworkConnector networkConnector, ClientBuilder clientBuilder,
            SseEventSourceFactory eventSourceFactory) {
        this.networkConnector = networkConnector;
        this.bridgeHandler = bridgeHandler;
        this.clientBuilder = clientBuilder;
        this.eventSourceFactory = eventSourceFactory;
        this.sseEvents = new Hashtable<String, SseEventSource>();
    }

    public SmartThingsDevice[] getAllDevices() throws SmartThingsException {
        SmartThingsDevice[] devices = doRequest(SmartThingsDevice[].class, baseUrl + deviceEndPoint);
        return devices;
    }

    public AppResponse setupApp(String redirectUrl) throws SmartThingsException {
        SmartThingsApp[] appList = getAllApps();

        Optional<SmartThingsApp> appOptional = Arrays.stream(appList).filter(x -> APP_NAME.equals(x.appName))
                .findFirst();

        if (appOptional.isPresent()) {
            SmartThingsApp app = appOptional.get(); // Get it from optional
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

    public SmartThingsCapability[] getAllCapabilities() throws SmartThingsException {
        try {
            String uri = baseUrl + capabilitiesEndPoint;
            SmartThingsCapability[] listCapabilities = doRequest(SmartThingsCapability[].class, uri);
            return listCapabilities;
        } catch (final Exception e) {
            throw new SmartThingsException("SmartThingsApi : Unable to retrieve capabilities", e);
        }
    }

    public SmartThingsCapability getCapability(String capabilityId, String version,
            @Nullable SmartThingsNetworkCallback<SmartThingsCapability> cb) throws SmartThingsException {
        try {
            String uri = baseUrl + capabilitiesEndPoint + "/" + capabilityId + "/" + version;
            SmartThingsCapability capabilitie = doRequest(SmartThingsCapability.class, uri, cb);
            return capabilitie;
        } catch (final Exception e) {
            throw new SmartThingsException(
                    String.format("SmartThingsApi : Unable to retrieve capability : %s", capabilityId), e);
        }
    }

    public SmartThingsLocation[] getAllLocations() throws SmartThingsException {
        try {
            String uri = baseUrl + locationEndPoint;
            SmartThingsLocation[] listLocations = doRequest(SmartThingsLocation[].class, uri);
            return listLocations;
        } catch (final Exception e) {
            throw new SmartThingsException("SmartThingsApi : Unable to retrieve locations", e);
        }
    }

    public SmartThingsLocation getLocation(String locationId) throws SmartThingsException {
        try {
            String uri = baseUrl + locationEndPoint + "/" + locationId;

            SmartThingsLocation loc = doRequest(SmartThingsLocation.class, uri);

            return loc;
        } catch (final Exception e) {
            throw new SmartThingsException(
                    String.format("SmartThingsApi : Unable to retrieve location : %s", locationId), e);
        }
    }

    public SmartThingsRoom[] getRooms(String locationId) throws SmartThingsException {
        try {
            String uri = baseUrl + locationEndPoint + "/" + locationId + roomsEndPoint;
            SmartThingsRoom[] listRooms = doRequest(SmartThingsRoom[].class, uri);
            return listRooms;
        } catch (final Exception e) {
            throw new SmartThingsException("SmartThingsApi : Unable to retrieve rooms", e);
        }
    }

    public SmartThingsRoom getRoom(String locationId, String roomId) throws SmartThingsException {
        try {
            String uri = baseUrl + locationEndPoint + "/" + locationId + roomsEndPoint + "/" + roomId;

            SmartThingsRoom loc = doRequest(SmartThingsRoom.class, uri);

            return loc;
        } catch (final Exception e) {
            throw new SmartThingsException(
                    String.format("SmartThingsApi : Unable to retrieve room : %s %s", locationId, roomId), e);
        }
    }

    public SmartThingsApp[] getAllApps() throws SmartThingsException {
        try {
            String uri = baseUrl + appEndPoint;

            SmartThingsApp[] listApps = doRequest(SmartThingsApp[].class, uri);

            logger.info("");
            return listApps;
        } catch (final Exception e) {
            throw new SmartThingsException("SmartThingsApi : Unable to retrieve apps", e);
        }
    }

    public SmartThingsApp getApp(String appId) throws SmartThingsException {
        try {
            String uri = baseUrl + appEndPoint + "/" + appId;

            SmartThingsApp app = doRequest(SmartThingsApp.class, uri);

            return app;
        } catch (final Exception e) {
            throw new SmartThingsException(String.format("SmartThingsApi : Unable to retrieve app : %s", appId), e);
        }
    }

    public AppResponse createApp(String redirectUrl) throws SmartThingsException {
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
            AppResponse appResponse = doRequest(HttpMethod.POST, AppResponse.class, uri, body);

            return appResponse;
        } catch (final Exception e) {
            throw new SmartThingsException("SmartThingsApi : Unable to create app", e);
        }
    }

    public void createAppOAuth(String appId) throws SmartThingsException {
        try {
            String uri = baseUrl + appEndPoint + "/" + appId + "/oauth";

            OAuthConfigRequest oAuthConfig = new OAuthConfigRequest();
            oAuthConfig.clientName = "openHAB Integration";
            oAuthConfig.scope = new String[1];
            oAuthConfig.scope[0] = "r:devices:*";

            String body = gson.toJson(oAuthConfig);
            doRequest(HttpMethod.PUT, JsonObject.class, uri, body);

            logger.info("");

            // return appResponse;
        } catch (final Exception e) {
            throw new SmartThingsException("SmartThingsApi : Unable to create oauth settings", e);
        }
    }

    public String getToken() throws SmartThingsException {
        AccessTokenResponse accessTokenResponse = bridgeHandler.getAccessTokenResponse();

        // Store token is about to expire, ask for a new one.
        if (accessTokenResponse != null && accessTokenResponse.isExpired(Instant.now(), 1200)) {
            accessTokenResponse = bridgeHandler.refreshToken();
        }

        if (accessTokenResponse == null) {
            accessTokenResponse = bridgeHandler.getAccessTokenByClientCredentials();
        }

        final String accessToken = accessTokenResponse == null ? null : accessTokenResponse.getAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            throw new SmartThingsException(
                    "No SmartThings accesstoken. Did you authorize SmartThings via /connectsmartthings ?");
        }

        return accessToken;
    }

    public void sendCommand(String deviceId, String jsonMsg) throws SmartThingsException {
        try {
            String uri = baseUrl + deviceEndPoint + "/" + deviceId + "/commands";
            doRequest(HttpMethod.POST, JsonObject.class, uri, jsonMsg);
        } catch (final Exception e) {
            throw new SmartThingsException(
                    String.format("SmartThingsApi : Unable to send command: %s %s", deviceId, jsonMsg), e);
        }
    }

    public @Nullable SmartThingsStatus getStatus(String deviceId) throws SmartThingsException {
        try {
            String uri = baseUrl + deviceEndPoint + "/" + deviceId + "/status";

            SmartThingsStatus res = doRequest(SmartThingsStatus.class, uri);
            return res;
        } catch (final Exception e) {
            throw new SmartThingsException(
                    String.format("SmartThingsApi : Unable to send status : device:%s", deviceId), e);
        }
    }

    public <T> T doRequest(Class<T> resultClass, String uri) throws SmartThingsException {
        return doRequest(HttpMethod.GET, resultClass, uri, null, null);
    }

    public <T> T doRequest(Class<T> resultClass, String uri, @Nullable SmartThingsNetworkCallback<T> callback)
            throws SmartThingsException {
        return doRequest(HttpMethod.GET, resultClass, uri, null, callback);
    }

    public <T> T doRequest(HttpMethod httpMethod, Class<T> resultClass, String uri, @Nullable String body)
            throws SmartThingsException {
        return doRequest(httpMethod, resultClass, uri, body, null);
    }

    public <T> T doRequest(HttpMethod httpMethod, Class<T> resultClass, String uri, @Nullable String body,
            @Nullable SmartThingsNetworkCallback<T> callback) throws SmartThingsException {
        try {
            return networkConnector.doRequest(resultClass, uri, callback, getToken(), body, httpMethod);
        } catch (final Exception e) {
            logger.trace("Request failed : {}", uri);
            throw new SmartThingsException("SmartThingsApi : Unable to do request", e);
        }
    }

    public boolean registerSSESubscription() {
        try {
            String appId = bridgeHandler.getAppId();
            String subscriptionUri = "https://api.smartthings.com/subscriptions";

            SmartThingsLocation[] locationsObj = this.getAllLocations();

            String[] locations = new String[locationsObj.length];
            for (int idx = 0; idx < locationsObj.length; idx++) {
                locations[idx] = locationsObj[idx].locationId;
            }

            String[] eventTypes = { "DEVICE_EVENT", "DEVICE_LIFECYCLE_EVENT", "DEVICE_HEALTH_EVENT" };
            EventRegistration evtReg = new EventRegistration();
            evtReg.name = "openHAB sub";
            evtReg.version = 20250122;
            evtReg.clientDeviceId = "iapp_" + appId;
            evtReg.subscriptionFilters = new EventRegistration.SubscriptionFilters[1];

            evtReg.subscriptionFilters[0] = new EventRegistration.SubscriptionFilters("LOCATIONIDS", locations,
                    eventTypes);

            String body = gson.toJson(evtReg);

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

            return true;

        } catch (Exception ex) {
            logger.debug("ex: {}", ex.toString());
        }
        return false;
    }

    public boolean registerCallbackSubscription() {
        try {
            String appId = bridgeHandler.getAppId();
            String subscriptionUri = "https://api.smartthings.com/v1/installedapps/" + appId + "/subscriptions";

            SmartThingsLocation[] locationsObj = this.getAllLocations();

            String[] locations = new String[locationsObj.length];
            for (int idx = 0; idx < locationsObj.length; idx++) {
                locations[idx] = locationsObj[idx].locationId;
            }

            // Remove old subscriptions before recreating them
            doRequest(HttpMethod.DELETE, JsonObject.class, subscriptionUri, null, null);
            doRequest(JsonObject.class, subscriptionUri);

            SmartThingsApi api = bridgeHandler.getSmartThingsApi();

            SmartThingsDevice[] devices = api.getAllDevices();
            for (SmartThingsDevice dev : devices) {
                try {
                    if (!dev.locationId.equals(locations[0])) {
                        continue;
                    }

                    SMEvent evt = new SMEvent();
                    evt.sourceType = SmartThingsBindingConstants.EVT_TYPE_DEVICE;
                    evt.device = new device(dev.deviceId, SmartThingsBindingConstants.GROUPD_ID_MAIN, true, null);

                    String body = gson.toJson(evt);
                    doRequest(HttpMethod.POST, JsonObject.class, subscriptionUri, body, null);
                } catch (SmartThingsException ex) {
                    logger.error("Unable to register subscriptions: {} {} ", ex.getMessage(), dev.deviceId);
                }
            }

            return true;

        } catch (SmartThingsException ex) {
            logger.error("Unable to register subscriptions: {}", ex.getMessage());
        }

        return false;
    }

    public void onEvent(InboundSseEvent event) {
        String data = event.readData();

        if (!data.contains("DEVICE_EVENT")) {
            return;
        }

        logger.info("Received: {}", event.readData());
        try {
            final Event evt = gson.fromJson(data, Event.class);

            if (evt != null) {
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
                    SmartThingsThingHandler smarthingsHandler = (SmartThingsThingHandler) handler;
                    if (smarthingsHandler != null) {
                        smarthingsHandler.refreshDevice(theThing.getThingTypeUID().getId(), componentId, capa, attr,
                                value);
                    }
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

    public SmartThingsNetworkConnector getNetworkConnector() {
        return networkConnector;
    }
}
