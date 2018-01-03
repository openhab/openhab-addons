/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.handler;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.netatmo.internal.WelcomeWebHookServlet;
import org.openhab.binding.netatmo.internal.config.NetatmoBridgeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.client.ApiClient;
import io.swagger.client.api.HealthyhomecoachApi;
import io.swagger.client.api.PartnerApi;
import io.swagger.client.api.StationApi;
import io.swagger.client.api.ThermostatApi;
import io.swagger.client.api.WelcomeApi;
import io.swagger.client.auth.OAuth;
import io.swagger.client.auth.OAuthFlow;
import io.swagger.client.model.NAHealthyHomeCoachDataBody;
import io.swagger.client.model.NAStationDataBody;
import io.swagger.client.model.NAThermostatDataBody;
import io.swagger.client.model.NAWebhookCameraEvent;
import io.swagger.client.model.NAWebhookCameraEventPerson;
import io.swagger.client.model.NAWelcomeHomeData;
import retrofit.RestAdapter.LogLevel;
import retrofit.RetrofitError;

/**
 * {@link NetatmoBridgeHandler} is the handler for a Netatmo API and connects it
 * to the framework. The devices and modules uses the
 * {@link NetatmoBridgeHandler} to request informations about their status
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 *
 */
public class NetatmoBridgeHandler extends BaseBridgeHandler {
    private Logger logger = LoggerFactory.getLogger(NetatmoBridgeHandler.class);

    public NetatmoBridgeConfiguration configuration;
    private ScheduledFuture<?> refreshJob;
    private APIMap apiMap;

    @NonNullByDefault
    private class APIMap extends HashMap<Class<?>, Object> {
        private static final long serialVersionUID = -2024031764691952343L;
        private ApiClient apiClient;

        public APIMap(ApiClient apiClient) {
            super();
            this.apiClient = apiClient;
        }

        public Object get(Class<?> apiClass) {
            if (!super.containsKey(apiClass)) {
                Object api = apiClient.createService(apiClass);
                super.put(apiClass, api);
            }
            return super.get(apiClass);
        }

    }

    public NetatmoBridgeHandler(@NonNull Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Netatmo API bridge handler.");

        configuration = getConfigAs(NetatmoBridgeConfiguration.class);
        scheduleTokenInitAndRefresh();
    }

    private void connectionSucceed() {
        updateStatus(ThingStatus.ONLINE);
        if (configuration.webHookUrl != null && configuration.readWelcome) {
            String webHookURI = configuration.webHookUrl + WelcomeWebHookServlet.PATH;
            logger.info("Setting up Netatmo Welcome WebHook to {}", webHookURI);
            getWelcomeApi().addwebhook(webHookURI, WEBHOOK_APP);
        }
    }

    private void scheduleTokenInitAndRefresh() {
        refreshJob = scheduler.scheduleAtFixedRate(() -> {
            logger.info("Initializing API Connection and scheduling token refresh every {}s",
                    configuration.reconnectInterval);
            initializeApiClient();

            try {
                // I use a connection to Netatmo API using PartnerAPI to ensure that API is reachable
                getPartnerApi().partnerdevices();
                connectionSucceed();
            } catch (RetrofitError e) {
                switch (e.getResponse().getStatus()) {
                    case 404: // If no partner station has been associated - likely to happen - we'll have this error
                              // but it means connection to API is OK
                        connectionSucceed();
                        break;
                    case 403: // Forbidden Access maybe too many requests ? Let's wait next cycle
                        logger.warn("Error 403 while connecting to Netatmo API, will retry in {} s",
                                configuration.reconnectInterval);
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_REGISTERING_ERROR,
                                "Netatmo Access Forbidden, will retry in " + configuration.reconnectInterval.toString()
                                        + " seconds.");
                        break;
                    default:
                        logger.error("Unable to connect Netatmo API : {}", e.getMessage(), e);
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                "Unable to connect Netatmo API : " + e.getLocalizedMessage());
                        return;
                }
            }
            // We'll do this every x seconds to guaranty token refresh
        }, 2, configuration.reconnectInterval, TimeUnit.SECONDS);
    }

    // We'll use TrustingOkHttpClient because Netatmo certificate is a StartTTLS
    // not trusted by default java certificate control mechanism
    private void initializeApiClient() throws RetrofitError {
        ApiClient apiClient = new ApiClient();

        OAuth auth = new OAuth(new TrustingOkHttpClient(),
                OAuthClientRequest.tokenLocation("https://api.netatmo.net/oauth2/token"));
        auth.setFlow(OAuthFlow.password);
        auth.setAuthenticationRequestBuilder(OAuthClientRequest.authorizationLocation(""));

        apiClient.getApiAuthorizations().put("password_oauth", auth);
        apiClient.getTokenEndPoint().setClientId(configuration.clientId).setClientSecret(configuration.clientSecret)
                .setUsername(configuration.username).setPassword(configuration.password).setScope(getApiScope());

        apiClient.configureFromOkclient(new TrustingOkHttpClient());
        apiClient.getAdapterBuilder().setLogLevel(logger.isDebugEnabled() ? LogLevel.FULL : LogLevel.NONE);

        apiMap = new APIMap(apiClient);
    }

    private String getApiScope() {
        List<String> scopes = new ArrayList<>();

        if (configuration.readStation) {
            scopes.add("read_station");
        }

        if (configuration.readThermostat) {
            scopes.add("read_thermostat");
            scopes.add("write_thermostat");
        }

        if (configuration.readHealthyHomeCoach) {
            scopes.add("read_homecoach");
        }

        if (configuration.readWelcome) {
            scopes.add("read_camera");
            scopes.add("access_camera");
            scopes.add("write_camera");
        }

        String result = String.join(" ", scopes);
        return result;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.warn("Netatmo Bridge is read-only and does not handle commands");
    }

    public PartnerApi getPartnerApi() {
        return (PartnerApi) apiMap.get(PartnerApi.class);
    }

    private StationApi getStationApi() {
        return (StationApi) apiMap.get(StationApi.class);
    }

    private HealthyhomecoachApi getHomeCoachApi() {
        return (HealthyhomecoachApi) apiMap.get(HealthyhomecoachApi.class);
    }

    public ThermostatApi getThermostatApi() {
        return (ThermostatApi) apiMap.get(ThermostatApi.class);
    }

    public WelcomeApi getWelcomeApi() {
        return (WelcomeApi) apiMap.get(WelcomeApi.class);
    }

    @Override
    public void dispose() {
        logger.debug("Running dispose()");

        if (configuration.webHookUrl != null) {
            logger.info("Releasing Netatmo Welcome WebHook");
            getWelcomeApi().dropwebhook(WEBHOOK_APP);
        }

        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
    }

    public NAStationDataBody getStationsDataBody(String equipmentId) {
        NAStationDataBody data = getStationApi().getstationsdata(equipmentId).getBody();
        updateStatus(ThingStatus.ONLINE);
        return data;
    }

    public NAHealthyHomeCoachDataBody getHomecoachDataBody(String equipmentId) {
        NAHealthyHomeCoachDataBody data = getHomeCoachApi().gethomecoachsdata(equipmentId).getBody();
        updateStatus(ThingStatus.ONLINE);
        return data;
    }

    public NAThermostatDataBody getThermostatsDataBody(String equipmentId) {
        NAThermostatDataBody data = getThermostatApi().getthermostatsdata(equipmentId).getBody();
        updateStatus(ThingStatus.ONLINE);
        return data;
    }

    public NAWelcomeHomeData getWelcomeDataBody(String homeId) {
        NAWelcomeHomeData data = getWelcomeApi().gethomedata(homeId, null).getBody();
        updateStatus(ThingStatus.ONLINE);
        return data;
    }

    /**
     * Returns the Url of the picture
     *
     * @return Url of the picture or UnDefType.UNDEF
     */
    public String getPictureUrl(String id, String key) {
        StringBuffer ret = new StringBuffer();
        if (id != null && key != null) {
            ret.append(WELCOME_PICTURE_URL).append("?").append(WELCOME_PICTURE_IMAGEID).append("=").append(id)
                    .append("&").append(WELCOME_PICTURE_KEY).append("=").append(key);
        }
        return ret.toString();
    }

    public Optional<AbstractNetatmoThingHandler> findNAThing(String searchedId) {
        List<Thing> things = getThing().getThings();
        Stream<AbstractNetatmoThingHandler> naHandlers = things.stream().map(Thing::getHandler)
                .filter(AbstractNetatmoThingHandler.class::isInstance).map(AbstractNetatmoThingHandler.class::cast)
                .filter(handler -> handler.matchesId(searchedId));
        return naHandlers.findAny();
    }

    public void webHookEvent(NAWebhookCameraEvent event) {
        // This currently the only known event type but I suspect webhook usage
        // can grow in the future...
        if (event.getAppType() == NAWebhookCameraEvent.AppTypeEnum.CAMERA) {
            Set<AbstractNetatmoThingHandler> modules = new HashSet<>();
            if (WELCOME_EVENTS.contains(event.getEventType()) || PRESENCE_EVENTS.contains(event.getEventType())) {
                String cameraId = event.getCameraId();
                if (cameraId != null) {
                    Optional<AbstractNetatmoThingHandler> camera = findNAThing(cameraId);
                    camera.ifPresent(aCamera -> modules.add(aCamera));
                }
            }
            if (HOME_EVENTS.contains(event.getEventType())) {
                String homeId = event.getHomeId();
                if (homeId != null) {
                    Optional<AbstractNetatmoThingHandler> home = findNAThing(homeId);
                    home.ifPresent(aHome -> modules.add(aHome));
                }
            }
            if (PERSON_EVENTS.contains(event.getEventType())) {
                List<NAWebhookCameraEventPerson> persons = event.getPersons();
                persons.forEach(person -> {
                    String personId = person.getId();
                    if (personId != null) {
                        Optional<AbstractNetatmoThingHandler> personHandler = findNAThing(personId);
                        personHandler.ifPresent(aPerson -> modules.add(aPerson));
                    }
                });
            }
            modules.forEach(module -> {
                Channel channel = module.getThing().getChannel(CHANNEL_WELCOME_HOME_EVENT);
                if (channel != null) {
                    triggerChannel(channel.getUID(), event.getEventType().toString());
                }
            });
        }
    }

}
