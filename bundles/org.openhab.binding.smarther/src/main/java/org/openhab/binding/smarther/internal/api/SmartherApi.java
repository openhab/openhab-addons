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
package org.openhab.binding.smarther.internal.api;

import static org.eclipse.jetty.http.HttpMethod.*;
import static org.openhab.binding.smarther.internal.SmartherBindingConstants.*;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.smarthome.core.auth.client.oauth2.AccessTokenResponse;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthClientService;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthException;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthResponseException;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.openhab.binding.smarther.internal.api.exception.SmartherAuthorizationException;
import org.openhab.binding.smarther.internal.api.exception.SmartherGatewayException;
import org.openhab.binding.smarther.internal.api.exception.SmartherTokenExpiredException;
import org.openhab.binding.smarther.internal.api.model.Enums.MeasureUnit;
import org.openhab.binding.smarther.internal.api.model.ModelUtil;
import org.openhab.binding.smarther.internal.api.model.Module;
import org.openhab.binding.smarther.internal.api.model.ModuleSettings;
import org.openhab.binding.smarther.internal.api.model.ModuleStatus;
import org.openhab.binding.smarther.internal.api.model.Plant;
import org.openhab.binding.smarther.internal.api.model.Plants;
import org.openhab.binding.smarther.internal.api.model.Program;
import org.openhab.binding.smarther.internal.api.model.Subscription;
import org.openhab.binding.smarther.internal.api.model.Topology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.reflect.TypeToken;

/**
 * Class to handle BTicino/Legrand API gateway calls.
 *
 * @author Fabio Possieri - Initial contribution
 */
@NonNullByDefault
public class SmartherApi {

    private static final String CONTENT_TYPE = "application/json";
    private static final String BEARER = "Bearer ";

    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final String oAuthSubscriptionKey;
    private final OAuthClientService oAuthClientService;
    private final SmartherApiConnector connector;

    /**
     * Constructor.
     */
    public SmartherApi(String oAuthSubscriptionKey, OAuthClientService oAuthClientService,
            ScheduledExecutorService scheduler, HttpClient httpClient) {
        this.oAuthSubscriptionKey = oAuthSubscriptionKey;
        this.oAuthClientService = oAuthClientService;
        connector = new SmartherApiConnector(scheduler, httpClient);
    }

    /**
     * @return Calls API gateway and returns the list of plants or an empty list if nothing was returned
     */
    public List<Plant> getPlantList() {
        final ContentResponse response = requestBasic(GET, "plants");
        if (response.getStatus() == HttpStatus.NO_CONTENT_204) {
            return new ArrayList<Plant>();
        } else {
            return ModelUtil.gsonInstance().fromJson(response.getContentAsString(), Plants.class).getPlants();
        }
    }

    /**
     * @param plantId Identifier of the location plant to query the topology map for
     * @return Calls API gateway and returns the list of modules contained in the given location plant or an empty list
     *         if nothing was returned
     */
    public List<Module> getTopology(String plantId) {
        final ContentResponse response = requestBasic(GET, String.format("plants/%s/topology", plantId));
        final Topology topology = ModelUtil.gsonInstance().fromJson(response.getContentAsString(), Topology.class);

        return (topology.getModules() == null) ? Collections.emptyList() : topology.getModules();
    }

    /**
     * @param plantId Identifier of the location plant the module is contained in
     * @param moduleId Identifier of the module to query the status for
     * @return Calls API gateway and returns the module current status or an empty list if
     *         nothing was returned
     */
    public ModuleStatus getModuleStatus(String plantId, String moduleId) {
        final ContentResponse response = requestModule(GET, plantId, moduleId, "");
        return ModelUtil.gsonInstance().fromJson(response.getContentAsString(), ModuleStatus.class);
    }

    /**
     * @param settings The new settings to be sent to the API gateway in order to be remotely applied to the module
     * @return true if the call returned with success response, false otherwise
     */
    public boolean setModuleStatus(ModuleSettings settings) {
        // Prepare request payload
        Map<String, Object> rootMap = new IdentityHashMap<String, Object>();
        rootMap.put("function", settings.getFunction().getValue());
        rootMap.put("mode", settings.getMode().getValue());
        switch (settings.getMode()) {
            case AUTOMATIC:
                // {"function":"heating","mode":"automatic","programs":[{"number":0}]}
                Map<String, Integer> programMap = new IdentityHashMap<String, Integer>();
                programMap.put("number", Integer.valueOf(settings.getProgram()));
                List<Map<String, Integer>> programsList = new ArrayList<Map<String, Integer>>();
                programsList.add(programMap);
                rootMap.put("programs", programsList);
                break;
            case MANUAL:
                // {"function":"heating","mode":"manual","setPoint":{"value":0.0,"unit":"C"},"activationTime":"X"}
                QuantityType<Temperature> newTemperature = settings.getSetPointTemperature(SIUnits.CELSIUS);
                if (newTemperature == null) {
                    throw new SmartherGatewayException("Invalid temperature unit transformation");
                }
                Map<String, Object> setPointMap = new IdentityHashMap<String, Object>();
                setPointMap.put("value", newTemperature.doubleValue());
                setPointMap.put("unit", MeasureUnit.CELSIUS.getValue());
                rootMap.put("setPoint", setPointMap);
                rootMap.put("activationTime", settings.getActivationTime());
                break;
            case BOOST:
                // {"function":"heating","mode":"boost","activationTime":"X"}
                rootMap.put("activationTime", settings.getActivationTime());
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
     * @param plantId Identifier of the location plant the module is contained in
     * @param moduleId Identifier of the module to get the program list for
     * @return Calls API gateway and returns the module current program list or an empty list if
     *         nothing was returned
     */
    public List<Program> getProgramList(String plantId, String moduleId) {
        final ContentResponse response = requestModule(GET, plantId, moduleId, "programlist", "");
        final ModuleStatus moduleStatus = ModelUtil.gsonInstance().fromJson(response.getContentAsString(),
                ModuleStatus.class);

        return (moduleStatus.hasChronothermostat()) ? moduleStatus.toChronothermostat().getPrograms()
                : Collections.emptyList();
    }

    /**
     * @return Calls API gateway and returns the list of C2C subscriptions or an empty list if nothing was returned
     */
    public List<Subscription> getSubscriptionList() {
        final ContentResponse response = requestBasic(GET, "subscription");
        if (response.getStatus() == HttpStatus.NO_CONTENT_204) {
            return new ArrayList<Subscription>();
        } else {
            return ModelUtil.gsonInstance().fromJson(response.getContentAsString(),
                    new TypeToken<List<Subscription>>() {
                    }.getType());
        }
    }

    /**
     * @return Calls API gateway to subscribe a plant for C2C notifications and returns the subscription Id
     */
    public String subscribe(String plantId, String notificationUrl) {
        // Prepare request payload
        Map<String, Object> rootMap = new IdentityHashMap<String, Object>();
        rootMap.put("EndPointUrl", notificationUrl);
        final String jsonPayload = ModelUtil.gsonInstance().toJson(rootMap);
        // Send request to server
        final ContentResponse response = requestBasic(POST, String.format("plants/%s/subscription", plantId),
                jsonPayload);
        // Handle response payload
        final Subscription subscription = ModelUtil.gsonInstance().fromJson(response.getContentAsString(),
                Subscription.class);
        return subscription.getSubscriptionId();
    }

    /**
     * @return Calls API gateway to unsubscribe a plant from receiving C2C notifications
     */
    public boolean unsubscribe(String plantId, String subscriptionId) {
        final ContentResponse response = requestBasic(DELETE,
                String.format("plants/%s/subscription/%s", plantId, subscriptionId));
        return (response.getStatus() == HttpStatus.OK_200);
    }

    /**
     * Calls the BTicino/Legrand API gateway with the given method and appends the given url as parameters of the call.
     *
     * @param method Http method to perform
     * @param url url path to call to API gateway
     * @return the response give by API gateway
     */
    private ContentResponse requestBasic(HttpMethod method, String url) {
        return requestBasic(method, url, "");
    }

    /**
     * Calls the BTicino/Legrand API gateway with the given method and appends the given url as parameters of the call.
     *
     * @param method Http method to perform
     * @param url url path to call to API gateway
     * @param requestData data to pass along with the call as content
     * @return the response give by API gateway
     */
    private ContentResponse requestBasic(HttpMethod method, String url, String requestData) {
        return request(method, SMARTHER_API_URL + (url.isEmpty() ? "" : ('/' + url)), requestData);
    }

    /**
     * Calls the BTicino/Legrand API gateway with the given method for the given plant and module as parameters of the
     * call.
     *
     * @param method Http method to perform
     * @param plantId Location plant id to call to API gateway
     * @param moduleId Module id to call to API gateway
     * @param path Additional url path to call to API gateway
     * @param requestData data to pass along with the call as content
     * @return the response give by API gateway
     */
    private ContentResponse requestModule(HttpMethod method, String plantId, String moduleId, String path,
            String requestData) {
        final String url = String.format("/plants/%s/modules/parameter/id/value/%s", plantId, moduleId);
        return request(method, SMARTHER_MODULE_URL + url + (path.isEmpty() ? "" : ('/' + path)), requestData);
    }

    /**
     * Calls the BTicino/Legrand API gateway with the given method for the given plant and module as parameters of the
     * call.
     *
     * @param method Http method to perform
     * @param plantId Location plant id to call to API gateway
     * @param moduleId Module id to call to API gateway
     * @param requestData data to pass along with the call as content
     * @return the response give by API gateway
     */
    private ContentResponse requestModule(HttpMethod method, String plantId, String moduleId, String requestData) {
        return requestModule(method, plantId, moduleId, "", requestData);
    }

    /**
     * Calls the BTicino/Legrand API gateway with the given method and given url as parameters of the call.
     *
     * @param method Http method to perform
     * @param url url path to call to API gateway
     * @param requestData data to pass along with the call as content
     * @return the response given by API gateway
     */
    private ContentResponse request(HttpMethod method, String url, String requestData) {
        logger.debug("Request: ({}) {} - {}", method, url, requestData);
        final Function<HttpClient, Request> call = httpClient -> httpClient.newRequest(url).method(method)
                .header("Accept", CONTENT_TYPE).content(new StringContentProvider(requestData), CONTENT_TYPE);

        try {
            final AccessTokenResponse accessTokenResponse = oAuthClientService.getAccessTokenResponse();
            final String accessToken = (accessTokenResponse == null) ? null : accessTokenResponse.getAccessToken();

            if (accessToken == null || accessToken.isEmpty()) {
                throw new SmartherAuthorizationException(String
                        .format("No gateway accesstoken. Did you authorize smarther via %s ?", AUTH_SERVLET_ALIAS));
            } else {
                return requestWithRetry(call, accessToken);
            }
        } catch (IOException e) {
            throw new SmartherGatewayException(e.getMessage(), e);
        } catch (OAuthException | OAuthResponseException e) {
            throw new SmartherAuthorizationException(e.getMessage(), e);
        }
    }

    private ContentResponse requestWithRetry(final Function<HttpClient, Request> call, final String accessToken)
            throws OAuthException, IOException, OAuthResponseException {
        try {
            return connector.request(call, this.oAuthSubscriptionKey, BEARER + accessToken);
        } catch (SmartherTokenExpiredException e) {
            // Retry with new access token
            try {
                return connector.request(call, this.oAuthSubscriptionKey,
                        BEARER + oAuthClientService.refreshToken().getAccessToken());
            } catch (SmartherTokenExpiredException ex) {
                // This should never happen in normal conditions
                throw new SmartherAuthorizationException(String.format("Cannot refresh token: %s", ex.getMessage()));
            }
        }
    }

}
