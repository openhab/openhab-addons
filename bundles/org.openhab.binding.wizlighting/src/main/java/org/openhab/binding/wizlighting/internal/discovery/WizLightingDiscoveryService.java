/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.wizlighting.internal.discovery;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.wizlighting.WizLightingBindingConstants;
import org.openhab.binding.wizlighting.handler.WizLightingMediator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.osgi.service.component.annotations.Component;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This is the {@link DiscoveryService} for the Wizlighting Items.
 *
 * @author Sriram Balakrishnan - Initial contribution
 *
 */
@Component(configurationPid = "discovery.wizlighting", service = DiscoveryService.class)
@NonNullByDefault
public class WizLightingDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(WizLightingDiscoveryService.class);
    private WizLightingMediator mediator;
    private @Nullable String inviteToken;
    private @Nullable Gson gson;
    private HttpClient httpClient;

    @Override
    protected void activate(Map<String, Object> configProperties) {
        if (configProperties != null) {
            @Nullable
            Object property = configProperties.get(WizLightingBindingConstants.DISCOVERY_INVITE_TOKEN);
            if (property != null) {
                this.inviteToken = String.valueOf(property);
            }
        }
    }

    /**
     * Used by OSGI to inject the mediator in the discovery service.
     *
     * @param mediator the mediator
     */
    public void setMediator(final WizLightingMediator mediator) {
        logger.trace("Mediator has been injected on discovery service.");

        this.mediator = mediator;
        mediator.setDiscoveryService(this);
    }

    /**
     * Used by OSGI to unset the mediator in the discovery service.
     *
     * @param mediator the mediator
     */
    public void unsetMediator(final WizLightingMediator mitsubishiMediator) {
        logger.trace("Mediator has been unsetted from discovery service.");

        this.mediator.setDiscoveryService(null);
        this.mediator = null;
    }

    /**
     * Constructor of the discovery service.
     *
     * @throws IllegalArgumentException if the timeout < 0
     */
    public WizLightingDiscoveryService() throws IllegalArgumentException {
        super(WizLightingBindingConstants.SUPPORTED_THING_TYPES_UIDS,
                WizLightingBindingConstants.DISCOVERY_TIMEOUT_SECONDS);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return WizLightingBindingConstants.SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    protected void startScan() {
        if (inviteToken == null) {
            logger.warn("Discovery Invite Token not configured. Cannot auto discover.");
            return;
        }

        try {
            httpClient = new HttpClient();
            httpClient.start();
            this.gson = (new GsonBuilder()).create();

            String accessToken = this.obtainAccessToken();

            if (accessToken == null) {
                logger.warn("Unable to obtain Access Token. Auto discovery not possible");
                return;
            }

            if (this.authorizeInviteToken(accessToken)) {
                int homeId = this.retrieveHomeId(accessToken);

                if (homeId != -1) {
                    HomeDTO.Light[] lights = this.retrieveHomeLights(accessToken, homeId);

                    if (lights != null) {
                        for (int i = 0; i < lights.length; i++) {
                            HomeDTO.Light light = lights[i];
                            logger.debug("Found Light {} with ip {} and mac {} ", light.discoveredLightId, light.discoveredLightIpAddress,
                                    light.discoveredLightMacAddress);
                            this.discoveredLight(light);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Auto-Discovery of WizLighting bulbs failed {} ", e.getMessage());
            // e.printStackTrace();
        }

    }

    protected @Nullable String obtainAccessToken()
            throws URISyntaxException, InterruptedException, TimeoutException, ExecutionException {
        Request request = httpClient.newRequest(WizLightingBindingConstants.DISCOVERY_API_OAUTH_URL)
                .header("Authorization", WizLightingBindingConstants.DISCOVERY_API_AUTHENTICATION_TOKEN)
                .header("Content-Type", "application/x-www-form-urlencoded").method(HttpMethod.POST)
                .content(new StringContentProvider("grant_type=urn:anonymous&scope=wiz_ios"));

        logger.debug("Request is {}", request.toString());
        ContentResponse response = request.send();
        logger.debug("Oauth Result {}", response.getContentAsString());

        TokenDTO data = gson.fromJson(response.getContentAsString(), TokenDTO.class);
        return data.accessToken;
    }

    protected boolean authorizeInviteToken(String accessToken)
            throws InterruptedException, TimeoutException, ExecutionException {
        AuthenticationStore a = httpClient.getAuthenticationStore();
        a.clearAuthentications();

        Request request = httpClient
                .newRequest(WizLightingBindingConstants.DISCOVERY_API_INVITE_TOKEN_URL + this.inviteToken)
                .header("Authorization", "Bearer " + accessToken).timeout(10, TimeUnit.SECONDS);

        ContentResponse response = request.send();
        String result = response.getContentAsString();
        logger.debug("Invite Result {}", result);
        return result.contains("\"success\":true");
    }

    protected int retrieveHomeId(String accessToken) throws InterruptedException, TimeoutException, ExecutionException {
        AuthenticationStore a = httpClient.getAuthenticationStore();
        a.clearAuthentications();

        Request request = httpClient.newRequest(WizLightingBindingConstants.DISCOVERY_API_USER_DETAILS_URL)
                .header("Authorization", "Bearer " + accessToken).timeout(10, TimeUnit.SECONDS);

        ContentResponse response = request.send();
        String result = response.getContentAsString();
        logger.debug("User Details Result {}", result);

        UserDTO data = gson.fromJson(response.getContentAsString(), UserDTO.class);
        return data.getHomeId();
    }

    protected HomeDTO.Light[] retrieveHomeLights(String accessToken, int homeId)
            throws InterruptedException, TimeoutException, ExecutionException {
        AuthenticationStore a = httpClient.getAuthenticationStore();
        a.clearAuthentications();

        Request request = httpClient.newRequest(WizLightingBindingConstants.DISCOVERY_API_HOME_DETAILS_URL + homeId)
                .header("Authorization", "Bearer " + accessToken).timeout(10, TimeUnit.SECONDS);

        ContentResponse response = request.send();
        String result = response.getContentAsString();
        logger.debug("Home Details Result {}", result);

        HomeDTO data = gson.fromJson(response.getContentAsString(), HomeDTO.class);
        return data.getLights();

    }

    /**
     * Method called by mediator, when receive one packet from one unknown Wifi Socket.
     *
     * @param bulbMacAddress the mac address from the device.
     * @param bulbIpAddress the host address from the device.
     */
    public void discoveredLight(final HomeDTO.Light light) {
        Map<String, Object> properties = new HashMap<>(2);
        if (light.discoveredLightMacAddress != null) {
            properties.put(WizLightingBindingConstants.BULB_MAC_ADDRESS_ARG, light.discoveredLightMacAddress);
        }
        if (light.discoveredLightIpAddress != null) {
            properties.put(WizLightingBindingConstants.BULB_IP_ADDRESS_ARG, light.discoveredLightIpAddress);
        }
        if (light.discoveredLightHomeId > 0) {
            properties.put(WizLightingBindingConstants.HOME_ID_ARG, String.valueOf(light.discoveredLightHomeId));
        }

        ThingUID newThingId = new ThingUID(WizLightingBindingConstants.THING_TYPE_WIZ_BULB, light.discoveredLightMacAddress);
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(newThingId).withProperties(properties)
                .withLabel("Wizlighting Bulb").withRepresentationProperty(light.discoveredLightMacAddress).build();

        logger.debug("Discovered new thing with mac address '{}' and host address '{}' and homeId '{}",
                light.discoveredLightMacAddress, light.discoveredLightIpAddress, light.discoveredLightHomeId);

        this.thingDiscovered(discoveryResult);
    }

    // SETTERS AND GETTERS
    /**
     * Gets the {@link WizLightingMediator} of this binding.
     *
     * @return {@link WizLightingMediator}.
     */
    public WizLightingMediator getMediator() {
        return this.mediator;
    }
}
