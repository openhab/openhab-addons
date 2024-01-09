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
package org.openhab.binding.bticinosmarther.internal.api;

import static org.eclipse.jetty.http.HttpMethod.*;
import static org.openhab.binding.bticinosmarther.internal.SmartherBindingConstants.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.bticinosmarther.internal.api.dto.Chronothermostat;
import org.openhab.binding.bticinosmarther.internal.api.dto.Enums.MeasureUnit;
import org.openhab.binding.bticinosmarther.internal.api.dto.Module;
import org.openhab.binding.bticinosmarther.internal.api.dto.ModuleStatus;
import org.openhab.binding.bticinosmarther.internal.api.dto.Plant;
import org.openhab.binding.bticinosmarther.internal.api.dto.Plants;
import org.openhab.binding.bticinosmarther.internal.api.dto.Program;
import org.openhab.binding.bticinosmarther.internal.api.dto.Subscription;
import org.openhab.binding.bticinosmarther.internal.api.dto.Topology;
import org.openhab.binding.bticinosmarther.internal.api.exception.SmartherAuthorizationException;
import org.openhab.binding.bticinosmarther.internal.api.exception.SmartherGatewayException;
import org.openhab.binding.bticinosmarther.internal.api.exception.SmartherTokenExpiredException;
import org.openhab.binding.bticinosmarther.internal.model.ModuleSettings;
import org.openhab.binding.bticinosmarther.internal.util.ModelUtil;
import org.openhab.binding.bticinosmarther.internal.util.StringUtil;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * The {@code SmartherApi} class is used to communicate with the BTicino/Legrand API gateway.
 *
 * @author Fabio Possieri - Initial contribution
 */
@NonNullByDefault
public class SmartherApi {

    private static final String CONTENT_TYPE = "application/json";
    private static final String BEARER = "Bearer ";

    // API gateway request headers
    private static final String HEADER_ACCEPT = "Accept";
    // API gateway request attributes
    private static final String ATTR_FUNCTION = "function";
    private static final String ATTR_MODE = "mode";
    private static final String ATTR_PROGRAMS = "programs";
    private static final String ATTR_NUMBER = "number";
    private static final String ATTR_SETPOINT = "setPoint";
    private static final String ATTR_VALUE = "value";
    private static final String ATTR_UNIT = "unit";
    private static final String ATTR_ACTIVATION_TIME = "activationTime";
    private static final String ATTR_ENDPOINT_URL = "EndPointUrl";
    // API gateway operation paths
    private static final String PATH_PLANTS = "/plants";
    private static final String PATH_TOPOLOGY = PATH_PLANTS + "/%s/topology";
    private static final String PATH_MODULE = "/chronothermostat/thermoregulation/addressLocation/plants/%s/modules/parameter/id/value/%s";
    private static final String PATH_PROGRAMS = "/programlist";
    private static final String PATH_SUBSCRIPTIONS = "/subscription";
    private static final String PATH_SUBSCRIBE = PATH_PLANTS + "/%s/subscription";
    private static final String PATH_UNSUBSCRIBE = PATH_SUBSCRIBE + "/%s";

    private final Logger logger = LoggerFactory.getLogger(SmartherApi.class);

    private final OAuthClientService oAuthClientService;
    private final String oAuthSubscriptionKey;
    private final SmartherApiConnector connector;

    /**
     * Constructs a {@code SmartherApi} to the API gateway with the specified OAuth2 attributes (subscription key and
     * client service), scheduler service and http client.
     *
     * @param clientService
     *            the OAuth2 authorization client service to be used
     * @param subscriptionKey
     *            the OAuth2 subscription key to be used with the given client service
     * @param scheduler
     *            the scheduler to be used to reschedule calls when rate limit exceeded or call not succeeded
     * @param httpClient
     *            the http client to be used to make http calls to the API gateway
     */
    public SmartherApi(final OAuthClientService clientService, final String subscriptionKey,
            final ScheduledExecutorService scheduler, final HttpClient httpClient) {
        this.oAuthClientService = clientService;
        this.oAuthSubscriptionKey = subscriptionKey;
        this.connector = new SmartherApiConnector(scheduler, httpClient);
    }

    /**
     * Returns the plants registered under the Smarther account the bridge has been configured with.
     *
     * @return the list of registered plants, or an empty {@link List} in case of no plants found
     *
     * @throws SmartherGatewayException in case of communication issues with the API gateway
     */
    public List<Plant> getPlants() throws SmartherGatewayException {
        try {
            final ContentResponse response = requestBasic(GET, PATH_PLANTS);
            if (response.getStatus() == HttpStatus.NO_CONTENT_204) {
                return new ArrayList<>();
            } else {
                return ModelUtil.gsonInstance().fromJson(response.getContentAsString(), Plants.class).getPlants();
            }
        } catch (JsonSyntaxException e) {
            throw new SmartherGatewayException(e.getMessage());
        }
    }

    /**
     * Returns the chronothermostat modules registered in the given plant.
     *
     * @param plantId
     *            the identifier of the plant
     *
     * @return the list of registered modules, or an empty {@link List} in case the plant contains no module
     *
     * @throws SmartherGatewayException in case of communication issues with the API gateway
     */
    public List<Module> getPlantModules(String plantId) throws SmartherGatewayException {
        try {
            final ContentResponse response = requestBasic(GET, String.format(PATH_TOPOLOGY, plantId));
            final Topology topology = ModelUtil.gsonInstance().fromJson(response.getContentAsString(), Topology.class);
            return topology.getModules();
        } catch (JsonSyntaxException e) {
            throw new SmartherGatewayException(e.getMessage());
        }
    }

    /**
     * Returns the current status of a given chronothermostat module.
     *
     * @param plantId
     *            the identifier of the plant
     * @param moduleId
     *            the identifier of the chronothermostat module inside the plant
     *
     * @return the current status of the chronothermostat module
     *
     * @throws SmartherGatewayException in case of communication issues with the API gateway
     */
    public ModuleStatus getModuleStatus(String plantId, String moduleId) throws SmartherGatewayException {
        try {
            final ContentResponse response = requestModule(GET, plantId, moduleId, null);
            ModuleStatus moduleStatus = ModelUtil.gsonInstance().fromJson(response.getContentAsString(),
                    ModuleStatus.class);
            return Objects.requireNonNull(moduleStatus);
        } catch (JsonSyntaxException e) {
            throw new SmartherGatewayException(e.getMessage());
        }
    }

    /**
     * Sends new settings to be applied to a given chronothermostat module.
     *
     * @param settings
     *            the module settings to be applied
     *
     * @return {@code true} if the settings have been successfully applied, {@code false} otherwise
     *
     * @throws SmartherGatewayException in case of communication issues with the API gateway
     */
    public boolean setModuleStatus(ModuleSettings settings) throws SmartherGatewayException {
        // Prepare request payload
        Map<String, Object> rootMap = new IdentityHashMap<>();
        rootMap.put(ATTR_FUNCTION, settings.getFunction().getValue());
        rootMap.put(ATTR_MODE, settings.getMode().getValue());
        switch (settings.getMode()) {
            case AUTOMATIC:
                // {"function":"heating","mode":"automatic","programs":[{"number":0}]}
                Map<String, Integer> programMap = new IdentityHashMap<>();
                programMap.put(ATTR_NUMBER, Integer.valueOf(settings.getProgram()));
                List<Map<String, Integer>> programsList = new ArrayList<>();
                programsList.add(programMap);
                rootMap.put(ATTR_PROGRAMS, programsList);
                break;
            case MANUAL:
                // {"function":"heating","mode":"manual","setPoint":{"value":0.0,"unit":"C"},"activationTime":"X"}
                QuantityType<Temperature> newTemperature = settings.getSetPointTemperature(SIUnits.CELSIUS);
                if (newTemperature == null) {
                    throw new SmartherGatewayException("Invalid temperature unit transformation");
                }
                Map<String, Object> setPointMap = new IdentityHashMap<>();
                setPointMap.put(ATTR_VALUE, newTemperature.doubleValue());
                setPointMap.put(ATTR_UNIT, MeasureUnit.CELSIUS.getValue());
                rootMap.put(ATTR_SETPOINT, setPointMap);
                rootMap.put(ATTR_ACTIVATION_TIME, settings.getActivationTime());
                break;
            case BOOST:
                // {"function":"heating","mode":"boost","activationTime":"X"}
                rootMap.put(ATTR_ACTIVATION_TIME, settings.getActivationTime());
                break;
            case OFF:
                // {"function":"heating","mode":"off"}
                break;
            case PROTECTION:
                // {"function":"heating","mode":"protection"}
                break;
        }
        final String jsonPayload = ModelUtil.gsonInstance().toJson(rootMap);

        // Send request to server
        final ContentResponse response = requestModule(POST, settings.getPlantId(), settings.getModuleId(),
                jsonPayload);
        return (response.getStatus() == HttpStatus.OK_200);
    }

    /**
     * Returns the automatic mode programs registered for the given chronothermostat module.
     *
     * @param plantId
     *            the identifier of the plant
     * @param moduleId
     *            the identifier of the chronothermostat module inside the plant
     *
     * @return the list of registered programs, or an empty {@link List} in case of no programs found
     *
     * @throws SmartherGatewayException in case of communication issues with the API gateway
     */
    public List<Program> getModulePrograms(String plantId, String moduleId) throws SmartherGatewayException {
        try {
            final ContentResponse response = requestModule(GET, plantId, moduleId, PATH_PROGRAMS, null);
            final ModuleStatus moduleStatus = ModelUtil.gsonInstance().fromJson(response.getContentAsString(),
                    ModuleStatus.class);

            final Chronothermostat chronothermostat = moduleStatus.toChronothermostat();
            return (chronothermostat != null) ? chronothermostat.getPrograms() : Collections.emptyList();
        } catch (JsonSyntaxException e) {
            throw new SmartherGatewayException(e.getMessage());
        }
    }

    /**
     * Returns the subscriptions registered to the C2C Webhook, where modules status notifications are currently sent
     * for all the plants.
     *
     * @return the list of registered subscriptions, or an empty {@link List} in case of no subscriptions found
     *
     * @throws SmartherGatewayException in case of communication issues with the API gateway
     */
    public List<Subscription> getSubscriptions() throws SmartherGatewayException {
        try {
            final ContentResponse response = requestBasic(GET, PATH_SUBSCRIPTIONS);
            if (response.getStatus() == HttpStatus.NO_CONTENT_204) {
                return new ArrayList<>();
            } else {
                List<Subscription> subscriptions = ModelUtil.gsonInstance().fromJson(response.getContentAsString(),
                        new TypeToken<List<Subscription>>() {
                        }.getType());
                if (subscriptions == null) {
                    throw new SmartherGatewayException("fromJson returned null");
                }
                return subscriptions;
            }
        } catch (JsonSyntaxException e) {
            throw new SmartherGatewayException(e.getMessage());
        }
    }

    /**
     * Subscribes a plant to the C2C Webhook to start receiving modules status notifications.
     *
     * @param plantId
     *            the identifier of the plant to be subscribed
     * @param notificationUrl
     *            the url notifications will have to be sent to for the given plant
     *
     * @return the identifier this subscription has been registered under
     *
     * @throws SmartherGatewayException in case of communication issues with the API gateway
     */
    public String subscribePlant(String plantId, String notificationUrl) throws SmartherGatewayException {
        try {
            // Prepare request payload
            Map<String, Object> rootMap = new IdentityHashMap<>();
            rootMap.put(ATTR_ENDPOINT_URL, notificationUrl);
            final String jsonPayload = ModelUtil.gsonInstance().toJson(rootMap);
            // Send request to server
            final ContentResponse response = requestBasic(POST, String.format(PATH_SUBSCRIBE, plantId), jsonPayload);
            // Handle response payload
            final Subscription subscription = ModelUtil.gsonInstance().fromJson(response.getContentAsString(),
                    Subscription.class);
            return subscription.getSubscriptionId();
        } catch (JsonSyntaxException e) {
            throw new SmartherGatewayException(e.getMessage());
        }
    }

    /**
     * Unsubscribes a plant from the C2C Webhook to stop receiving modules status notifications.
     *
     * @param plantId
     *            the identifier of the plant to be unsubscribed
     * @param subscriptionId
     *            the identifier of the subscription to be removed for the given plant
     *
     * @return {@code true} if the plant is successfully unsubscribed, {@code false} otherwise
     *
     * @throws SmartherGatewayException in case of communication issues with the API gateway
     */
    public boolean unsubscribePlant(String plantId, String subscriptionId) throws SmartherGatewayException {
        final ContentResponse response = requestBasic(DELETE, String.format(PATH_UNSUBSCRIBE, plantId, subscriptionId));
        return (response.getStatus() == HttpStatus.OK_200);
    }

    // ===========================================================================
    //
    // Internal API call handling methods
    //
    // ===========================================================================

    /**
     * Calls the API gateway with the given http method, request url and actual data.
     *
     * @param method
     *            the http method to make the call with
     * @param url
     *            the API operation url to call
     * @param requestData
     *            the actual data to send in the request body, may be {@code null}
     *
     * @return the response received from the API gateway
     *
     * @throws {@link SmartherGatewayException}
     *             in case of communication issues with the API gateway
     */
    private ContentResponse requestBasic(HttpMethod method, String url, @Nullable String requestData)
            throws SmartherGatewayException {
        return request(method, SMARTHER_API_URL + url, requestData);
    }

    /**
     * Calls the API gateway with the given http method and request url.
     *
     * @param method
     *            the http method to make the call with
     * @param url
     *            the API operation url to call
     *
     * @return the response received from the API gateway
     *
     * @throws {@link SmartherGatewayException}
     *             in case of communication issues with the API gateway
     */
    private ContentResponse requestBasic(HttpMethod method, String url) throws SmartherGatewayException {
        return requestBasic(method, url, null);
    }

    /**
     * Calls the API gateway with the given http method, plant id, module id, request path and actual data.
     *
     * @param method
     *            the http method to make the call with
     * @param plantId
     *            the identifier of the plant to use
     * @param moduleId
     *            the identifier of the module to use
     * @param path
     *            the API operation relative path to call, may be {@code null}
     * @param requestData
     *            the actual data to send in the request body, may be {@code null}
     *
     * @return the response received from the API gateway
     *
     * @throws {@link SmartherGatewayException}
     *             in case of communication issues with the API gateway
     */
    private ContentResponse requestModule(HttpMethod method, String plantId, String moduleId, @Nullable String path,
            @Nullable String requestData) throws SmartherGatewayException {
        final String url = String.format(PATH_MODULE, plantId, moduleId) + StringUtil.defaultString(path);
        return requestBasic(method, url, requestData);
    }

    /**
     * Calls the API gateway with the given http method, plant id, module id and actual data.
     *
     * @param method
     *            the http method to make the call with
     * @param plantId
     *            the identifier of the plant to use
     * @param moduleId
     *            the identifier of the module to use
     * @param requestData
     *            the actual data to send in the request body, may be {@code null}
     *
     * @return the response received from the API gateway
     *
     * @throws {@link SmartherGatewayException}
     *             in case of communication issues with the API gateway
     */
    private ContentResponse requestModule(HttpMethod method, String plantId, String moduleId,
            @Nullable String requestData) throws SmartherGatewayException {
        return requestModule(method, plantId, moduleId, null, requestData);
    }

    /**
     * Calls the API gateway with the given http method, request url and actual data.
     *
     * @param method
     *            the http method to make the call with
     * @param url
     *            the API operation url to call
     * @param requestData
     *            the actual data to send in the request body, may be {@code null}
     *
     * @return the response received from the API gateway
     *
     * @throws {@link SmartherGatewayException}
     *             in case of communication issues with the API gateway
     */
    private ContentResponse request(HttpMethod method, String url, @Nullable String requestData)
            throws SmartherGatewayException {
        logger.debug("Request: ({}) {} - {}", method, url, StringUtil.defaultString(requestData));
        Function<HttpClient, Request> call = httpClient -> httpClient.newRequest(url).method(method)
                .header(HEADER_ACCEPT, CONTENT_TYPE)
                .content(new StringContentProvider(StringUtil.defaultString(requestData)), CONTENT_TYPE);

        try {
            final AccessTokenResponse accessTokenResponse = oAuthClientService.getAccessTokenResponse();
            final String accessToken = (accessTokenResponse == null) ? null : accessTokenResponse.getAccessToken();

            if (accessToken == null || accessToken.isEmpty()) {
                throw new SmartherAuthorizationException(String
                        .format("No gateway accesstoken. Did you authorize smarther via %s ?", AUTH_SERVLET_ALIAS));
            } else {
                return requestWithRetry(call, accessToken);
            }
        } catch (SmartherGatewayException e) {
            throw e;
        } catch (OAuthException | OAuthResponseException e) {
            throw new SmartherAuthorizationException(e.getMessage(), e);
        } catch (IOException e) {
            throw new SmartherGatewayException(e.getMessage(), e);
        }
    }

    /**
     * Manages a generic call to the API gateway using the given authorization access token.
     * Retries the call if the access token is expired (refreshing it on behalf of further calls).
     *
     * @param call
     *            the http call to make
     * @param accessToken
     *            the authorization access token to use
     *
     * @return the response received from the API gateway
     *
     * @throws {@link OAuthException}
     *             in case of issues during the OAuth process
     * @throws {@link OAuthResponseException}
     *             in case of response issues during the OAuth process
     * @throws {@link IOException}
     *             in case of I/O issues of some sort
     */
    private ContentResponse requestWithRetry(final Function<HttpClient, Request> call, final String accessToken)
            throws OAuthException, OAuthResponseException, IOException {
        try {
            return this.connector.request(call, this.oAuthSubscriptionKey, BEARER + accessToken);
        } catch (SmartherTokenExpiredException e) {
            // Retry with new access token
            try {
                return this.connector.request(call, this.oAuthSubscriptionKey,
                        BEARER + this.oAuthClientService.refreshToken().getAccessToken());
            } catch (SmartherTokenExpiredException ex) {
                // This should never happen in normal conditions
                throw new SmartherAuthorizationException(String.format("Cannot refresh token: %s", ex.getMessage()));
            }
        }
    }
}
