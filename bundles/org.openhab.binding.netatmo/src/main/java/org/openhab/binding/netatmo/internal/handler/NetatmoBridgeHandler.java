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
package org.openhab.binding.netatmo.internal.handler;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.netatmo.internal.config.NetatmoBridgeConfiguration;
import org.openhab.binding.netatmo.internal.webhook.NAWebhookCameraEvent;
import org.openhab.binding.netatmo.internal.webhook.NAWebhookCameraEventPerson;
import org.openhab.binding.netatmo.internal.webhook.WelcomeWebHookServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.okhttp.OkHttpClient;

import io.swagger.client.ApiClient;
import io.swagger.client.CollectionFormats.CSVParams;
import io.swagger.client.api.HealthyhomecoachApi;
import io.swagger.client.api.PartnerApi;
import io.swagger.client.api.StationApi;
import io.swagger.client.api.ThermostatApi;
import io.swagger.client.api.WelcomeApi;
import io.swagger.client.auth.OAuth;
import io.swagger.client.auth.OAuthFlow;
import io.swagger.client.model.NAHealthyHomeCoachDataBody;
import io.swagger.client.model.NAMeasureBodyElem;
import io.swagger.client.model.NAStationDataBody;
import io.swagger.client.model.NAThermostatDataBody;
import io.swagger.client.model.NAWelcomeHomeData;
import retrofit.RestAdapter.LogLevel;
import retrofit.RetrofitError;
import retrofit.RetrofitError.Kind;

/**
 * {@link NetatmoBridgeHandler} is the handler for a Netatmo API and connects it
 * to the framework. The devices and modules uses the
 * {@link NetatmoBridgeHandler} to request informations about their status
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 * @author Rob Nielsen - Added day, week, and month measurements to the weather station and modules
 *
 */
@NonNullByDefault
public class NetatmoBridgeHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(NetatmoBridgeHandler.class);

    public NetatmoBridgeConfiguration configuration = new NetatmoBridgeConfiguration();
    private @Nullable ScheduledFuture<?> refreshJob;
    private @Nullable APIMap apiMap;
    private @Nullable WelcomeWebHookServlet webHookServlet;
    private List<NetatmoDataListener> dataListeners = new CopyOnWriteArrayList<>();

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

    public NetatmoBridgeHandler(Bridge bridge, @Nullable WelcomeWebHookServlet webHookServlet) {
        super(bridge);
        this.webHookServlet = webHookServlet;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Netatmo API bridge handler.");

        configuration = getConfigAs(NetatmoBridgeConfiguration.class);
        scheduleTokenInitAndRefresh();
    }

    private void connectionSucceed() {
        updateStatus(ThingStatus.ONLINE);
        WelcomeWebHookServlet servlet = webHookServlet;
        String webHookURI = getWebHookURI();
        if (servlet != null && webHookURI != null) {
            getWelcomeApi().ifPresent(api -> {
                servlet.activate(this);
                logger.debug("Setting up Netatmo Welcome WebHook");
                api.addwebhook(webHookURI, WEBHOOK_APP);
            });
        }
    }

    private void scheduleTokenInitAndRefresh() {
        refreshJob = scheduler.scheduleWithFixedDelay(() -> {
            logger.debug("Initializing API Connection and scheduling token refresh every {}s",
                    configuration.reconnectInterval);
            try {
                initializeApiClient();
                // I use a connection to Netatmo API using PartnerAPI to ensure that API is reachable
                getPartnerApi().partnerdevices();
                connectionSucceed();
            } catch (RetrofitError e) {
                if (e.getKind() == Kind.NETWORK) {
                    logger.warn("Network error while connecting to Netatmo API, will retry in {} s",
                            configuration.reconnectInterval);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Netatmo Access Failed, will retry in " + configuration.reconnectInterval + " seconds.");
                } else {
                    switch (e.getResponse().getStatus()) {
                        case 404: // If no partner station has been associated - likely to happen - we'll have this
                                  // error
                                  // but it means connection to API is OK
                            connectionSucceed();
                            break;
                        case 403: // Forbidden Access maybe too many requests ? Let's wait next cycle
                            logger.warn("Error 403 while connecting to Netatmo API, will retry in {} s",
                                    configuration.reconnectInterval);
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                    "Netatmo Access Forbidden, will retry in " + configuration.reconnectInterval
                                            + " seconds.");
                            break;
                        default:
                            if (logger.isDebugEnabled()) {
                                // we also attach the stack trace
                                logger.error("Unable to connect Netatmo API : {}", e.getMessage(), e);
                            } else {
                                logger.error("Unable to connect Netatmo API : {}", e.getMessage());
                            }
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                    "Unable to connect Netatmo API : " + e.getLocalizedMessage());
                            return;
                    }
                }
            }
            // We'll do this every x seconds to guaranty token refresh
        }, 2, configuration.reconnectInterval, TimeUnit.SECONDS);
    }

    private void initializeApiClient() throws RetrofitError {
        ApiClient apiClient = new ApiClient();

        OAuth auth = new OAuth(new OkHttpClient(),
                OAuthClientRequest.tokenLocation("https://api.netatmo.net/oauth2/token"));
        auth.setFlow(OAuthFlow.password);
        auth.setAuthenticationRequestBuilder(OAuthClientRequest.authorizationLocation(""));

        apiClient.getApiAuthorizations().put("password_oauth", auth);
        apiClient.getTokenEndPoint().setClientId(configuration.clientId).setClientSecret(configuration.clientSecret)
                .setUsername(configuration.username).setPassword(configuration.password).setScope(getApiScope());

        apiClient.configureFromOkclient(new OkHttpClient());
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

        if (configuration.readPresence) {
            scopes.add("read_presence");
            scopes.add("access_presence");
        }

        return String.join(" ", scopes);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Netatmo Bridge is read-only and does not handle commands");
    }

    public @Nullable PartnerApi getPartnerApi() {
        APIMap map = apiMap;
        return map != null ? (PartnerApi) map.get(PartnerApi.class) : null;
    }

    public Optional<StationApi> getStationApi() {
        APIMap map = apiMap;
        return map != null ? Optional.of((StationApi) map.get(StationApi.class)) : Optional.empty();
    }

    public Optional<HealthyhomecoachApi> getHomeCoachApi() {
        APIMap map = apiMap;
        return map != null ? Optional.of((HealthyhomecoachApi) map.get(HealthyhomecoachApi.class)) : Optional.empty();
    }

    public Optional<ThermostatApi> getThermostatApi() {
        APIMap map = apiMap;
        return map != null ? Optional.of((ThermostatApi) map.get(ThermostatApi.class)) : Optional.empty();
    }

    public Optional<WelcomeApi> getWelcomeApi() {
        APIMap map = apiMap;
        return map != null ? Optional.of((WelcomeApi) map.get(WelcomeApi.class)) : Optional.empty();
    }

    @Override
    public void dispose() {
        logger.debug("Running dispose()");

        WelcomeWebHookServlet servlet = webHookServlet;
        if (servlet != null && getWebHookURI() != null) {
            getWelcomeApi().ifPresent(api -> {
                logger.debug("Releasing Netatmo Welcome WebHook");
                servlet.deactivate();
                api.dropwebhook(WEBHOOK_APP);
            });
        }

        ScheduledFuture<?> job = refreshJob;
        if (job != null) {
            job.cancel(true);
            refreshJob = null;
        }
    }

    public Optional<NAStationDataBody> getStationsDataBody(@Nullable String equipmentId) {
        Optional<NAStationDataBody> data = getStationApi()
                .map(api -> api.getstationsdata(equipmentId, false).getBody());
        updateStatus(ThingStatus.ONLINE);
        return data;
    }

    public List<Float> getStationMeasureResponses(String equipmentId, @Nullable String moduleId, String scale,
            List<String> types) {
        List<NAMeasureBodyElem> data = getStationApi().map(api -> api
                .getmeasure(equipmentId, scale, new CSVParams(types), moduleId, null, "last", 1, true, false).getBody())
                .orElse(null);
        updateStatus(ThingStatus.ONLINE);
        NAMeasureBodyElem element = (data != null && data.size() > 0) ? data.get(0) : null;
        return element != null ? element.getValue().get(0) : Collections.emptyList();
    }

    public Optional<NAHealthyHomeCoachDataBody> getHomecoachDataBody(@Nullable String equipmentId) {
        Optional<NAHealthyHomeCoachDataBody> data = getHomeCoachApi()
                .map(api -> api.gethomecoachsdata(equipmentId).getBody());
        updateStatus(ThingStatus.ONLINE);
        return data;
    }

    public Optional<NAThermostatDataBody> getThermostatsDataBody(@Nullable String equipmentId) {
        Optional<NAThermostatDataBody> data = getThermostatApi()
                .map(api -> api.getthermostatsdata(equipmentId).getBody());
        updateStatus(ThingStatus.ONLINE);
        return data;
    }

    public Optional<NAWelcomeHomeData> getWelcomeDataBody(@Nullable String homeId) {
        Optional<NAWelcomeHomeData> data = getWelcomeApi().map(api -> api.gethomedata(homeId, null).getBody());
        updateStatus(ThingStatus.ONLINE);
        return data;
    }

    /**
     * Returns the Url of the picture
     *
     * @return Url of the picture or UnDefType.UNDEF
     */
    public String getPictureUrl(@Nullable String id, @Nullable String key) {
        StringBuilder ret = new StringBuilder();
        if (id != null && key != null) {
            ret.append(WELCOME_PICTURE_URL).append("?").append(WELCOME_PICTURE_IMAGEID).append("=").append(id)
                    .append("&").append(WELCOME_PICTURE_KEY).append("=").append(key);
        }
        return ret.toString();
    }

    public Optional<AbstractNetatmoThingHandler> findNAThing(@Nullable String searchedId) {
        List<Thing> things = getThing().getThings();
        Stream<AbstractNetatmoThingHandler> naHandlers = things.stream().map(Thing::getHandler)
                .filter(AbstractNetatmoThingHandler.class::isInstance).map(AbstractNetatmoThingHandler.class::cast)
                .filter(handler -> handler.matchesId(searchedId));
        return naHandlers.findAny();
    }

    public void webHookEvent(NAWebhookCameraEvent event) {
        // This currently the only known event type but I suspect usage can grow in the future...
        if (event.getAppType() == NAWebhookCameraEvent.AppTypeEnum.CAMERA) {
            Set<AbstractNetatmoThingHandler> modules = new HashSet<>();
            if (WELCOME_EVENTS.contains(event.getEventType()) || PRESENCE_EVENTS.contains(event.getEventType())) {
                String cameraId = event.getCameraId();
                if (cameraId != null) {
                    Optional<AbstractNetatmoThingHandler> camera = findNAThing(cameraId);
                    camera.ifPresent(modules::add);
                }
            }
            if (HOME_EVENTS.contains(event.getEventType())) {
                String homeId = event.getHomeId();
                if (homeId != null) {
                    Optional<AbstractNetatmoThingHandler> home = findNAThing(homeId);
                    home.ifPresent(modules::add);
                }
            }
            if (PERSON_EVENTS.contains(event.getEventType())) {
                List<NAWebhookCameraEventPerson> persons = event.getPersons();
                persons.forEach(person -> {
                    String personId = person.getId();
                    if (personId != null) {
                        Optional<AbstractNetatmoThingHandler> personHandler = findNAThing(personId);
                        personHandler.ifPresent(modules::add);
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

    private @Nullable String getWebHookURI() {
        String webHookURI = null;
        WelcomeWebHookServlet webHookServlet = this.webHookServlet;
        if (configuration.webHookUrl != null && (configuration.readWelcome || configuration.readPresence)
                && webHookServlet != null) {
            webHookURI = configuration.webHookUrl + webHookServlet.getPath();
        }
        return webHookURI;
    }

    public boolean registerDataListener(NetatmoDataListener dataListener) {
        return dataListeners.add(dataListener);
    }

    public boolean unregisterDataListener(NetatmoDataListener dataListener) {
        return dataListeners.remove(dataListener);
    }

    public void checkForNewThings(Object data) {
        for (NetatmoDataListener dataListener : dataListeners) {
            dataListener.onDataRefreshed(data);
        }
    }
}
