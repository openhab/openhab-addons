/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.handler;

import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.netatmo.config.NetatmoBridgeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.client.ApiClient;
import io.swagger.client.api.StationApi;
import io.swagger.client.api.ThermostatApi;
import io.swagger.client.api.WelcomeApi;
import io.swagger.client.auth.OAuth;
import io.swagger.client.auth.OAuthFlow;
import io.swagger.client.model.NAStationDataBody;
import io.swagger.client.model.NAThermostatDataBody;
import retrofit.RestAdapter.LogLevel;

/**
 * {@link NetatmoBridgeHandler} is the handler for a Netatmo API and connects it
 * to the framework. The devices and modules uses the
 * {@link NetatmoBridgeHandler} to request informations about their status
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 * @author Ing. Peter Weiss - Welcome camera implementation
 *
 */
public class NetatmoBridgeHandler extends BaseBridgeHandler {
    private static Logger logger = LoggerFactory.getLogger(NetatmoBridgeHandler.class);
    private NetatmoBridgeConfiguration configuration;
    private ApiClient apiClient;
    private StationApi stationApi = null;
    private ThermostatApi thermostatApi = null;
    private WelcomeApi welcomeApi = null;

    public NetatmoBridgeHandler(Bridge bridge) {
        super(bridge);
        configuration = getConfigAs(NetatmoBridgeConfiguration.class);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Netatmo API bridge handler.");

        configuration = getConfigAs(NetatmoBridgeConfiguration.class);

        apiClient = new ApiClient();

        // We'll use TrustingOkHttpClient because Netatmo certificate is a StartTTLS
        // not trusted by default java certificate control mechanism
        OAuth auth = new OAuth(new TrustingOkHttpClient(),
                OAuthClientRequest.tokenLocation("https://api.netatmo.net/oauth2/token"));
        auth.setFlow(OAuthFlow.password);
        auth.setAuthenticationRequestBuilder(OAuthClientRequest.authorizationLocation(""));

        apiClient.getApiAuthorizations().put("password_oauth", auth);
        apiClient.getTokenEndPoint().setClientId(configuration.clientId).setClientSecret(configuration.clientSecret)
                .setUsername(configuration.username).setPassword(configuration.password);

        apiClient.configureFromOkclient(new TrustingOkHttpClient());
        apiClient.getTokenEndPoint().setScope(getApiScope());
        apiClient.getAdapterBuilder().setLogLevel(LogLevel.NONE);
        super.initialize();
    }

    private String getApiScope() {
        StringBuilder stringBuilder = new StringBuilder();

        if (configuration.readStation) {
            stringBuilder.append("read_station ");
        }

        if (configuration.readThermostat) {
            stringBuilder.append("read_thermostat write_thermostat ");
        }

        if (configuration.readWelcome) {
            stringBuilder.append("read_camera ");
        }

        if (configuration.accessWelcome) {
            stringBuilder.append("access_camera ");
        }

        if (configuration.writeWelcome) {
            stringBuilder.append("write_camera ");
        }

        return stringBuilder.toString().trim();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.warn("This Bridge is read-only and does not handle commands");
    }

    private StationApi getStationApi() {
        if (configuration.readStation && stationApi == null) {
            stationApi = apiClient.createService(StationApi.class);
        }
        return stationApi;
    }

    public ThermostatApi getThermostatApi() {
        if (configuration.readThermostat && thermostatApi == null) {
            thermostatApi = apiClient.createService(ThermostatApi.class);
        }
        return thermostatApi;
    }

    public NAStationDataBody getStationsDataBody(String equipmentId) {
        if (getStationApi() != null) {
            try {
                return getStationApi().getstationsdata(equipmentId).getBody();
            } catch (Exception e) {
                logger.warn("An error occured while calling station API : {}", e.getMessage());
                return null;
            }
        } else {
            return null;
        }
    }

    public NAThermostatDataBody getThermostatsDataBody(String equipmentId) {
        if (getThermostatApi() != null) {
            try {
                return getThermostatApi().getthermostatsdata(equipmentId).getBody();
            } catch (Exception e) {
                logger.warn("An error occured while calling thermostat API : {}", e.getMessage());
                return null;
            }
        } else {
            return null;
        }

    }

    public WelcomeApi getWelcomeApi() {
        if (configuration.readWelcome && welcomeApi == null) {
            welcomeApi = apiClient.createService(WelcomeApi.class);
        }
        return welcomeApi;
    }

    public int getWelcomeEventThings() {
        return configuration.welcomeEventThings;
    }

    public int getWelcomeUnknownPersonThings() {
        return configuration.welcomeUnknownPersonThings;
    }
}
