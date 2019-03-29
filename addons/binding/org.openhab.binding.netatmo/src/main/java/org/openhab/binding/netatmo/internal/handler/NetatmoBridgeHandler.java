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
package org.openhab.binding.netatmo.internal.handler;

import io.rudolph.netatmo.NetatmoApi;
import io.rudolph.netatmo.api.presence.model.AppType;
import io.rudolph.netatmo.api.presence.model.PersonsEvent;
import io.rudolph.netatmo.api.presence.model.PersonsEventPerson;
import io.rudolph.netatmo.oauth2.model.Scope;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.*;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.netatmo.internal.config.NetatmoBridgeConfiguration;
import org.openhab.binding.netatmo.internal.webhook.WelcomeWebHookServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Stream;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

/**
 * {@link NetatmoBridgeHandler} is the handler for a Netatmo API and connects it
 * to the framework. The devices and modules uses the
 * {@link NetatmoBridgeHandler} to request informations about their status
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 */
public class NetatmoBridgeHandler extends BaseBridgeHandler {
    private Logger logger = LoggerFactory.getLogger(NetatmoBridgeHandler.class);

    public NetatmoBridgeConfiguration configuration;
    private ScheduledFuture<?> refreshJob;
    public NetatmoApi api;
    private WelcomeWebHookServlet webHookServlet;
    private List<NetatmoDataListener> dataListeners = new CopyOnWriteArrayList<>();

    public NetatmoBridgeHandler(@NonNull Bridge bridge, WelcomeWebHookServlet webHookServlet) {
        super(bridge);
        this.webHookServlet = webHookServlet;
        logger.debug("Initializing Netatmo API bridge handler.");

        configuration = getConfigAs(NetatmoBridgeConfiguration.class);

        api = new NetatmoApi(configuration.username,
                configuration.password,
                configuration.clientId,
                configuration.clientSecret,
                getApiScope(),
                configuration.authToken,
                configuration.refreshToken,
                true);

        api.getEnergyApi()
                .getHomesData().onError(s -> null)
                .executeAsync(homesDataBody -> {
            connectionSucceed();
            return null;
        });
    }

    private void connectionSucceed() {
        updateStatus(ThingStatus.ONLINE);
        String webHookURI = getWebHookURI();
        if (webHookURI != null) {
            webHookServlet.activate(this);
            logger.debug("Setting up Netatmo Welcome WebHook");
            api.getPresenceApi().addWebHook(webHookURI, WEBHOOK_APP);
        }
    }

    private List<Scope> getApiScope() {
        List<Scope> scopes = new ArrayList<>();

        if (configuration.readStation) {
            scopes.add(Scope.READ_STATION);
        }

        if (configuration.readThermostat) {
            scopes.add(Scope.READ_THERMOSTAT);
            scopes.add(Scope.WRITE_THERMOSTAT);
        }

        if (configuration.readHealthyHomeCoach) {
            scopes.add(Scope.READ_HOMECOACH);
        }

        if (configuration.readWelcome) {
            scopes.add(Scope.READ_CAMERA);
            scopes.add(Scope.ACCESS_CAMERA);
            scopes.add(Scope.WRITE_CAMERA);
        }

        return scopes;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Netatmo Bridge is read-only and does not handle commands");
    }

    @Override
    public void dispose() {
        logger.debug("Running dispose()");

        if (getWebHookURI() != null) {
            logger.debug("Releasing Netatmo Welcome WebHook");
            webHookServlet.deactivate();
            api.getPresenceApi().dropWebHook(WEBHOOK_APP);
        }

        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
    }

    /**
     * Returns the Url of the picture
     *
     * @return Url of the picture or UnDefType.UNDEF
     */
    public String getPictureUrl(String id, String key) {
        StringBuilder ret = new StringBuilder();
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

    public void webHookEvent(PersonsEvent event) {
        // This currently the only known event type but I suspect usage can grow in the future...
        if (event.getAppType() == AppType.CAMERA) {
            Set<AbstractNetatmoThingHandler> modules = new HashSet<>();
            if (WELCOME_EVENTS.contains(event.getType()) || PRESENCE_EVENTS.contains(event.getType())) {
                String cameraId = event.getCameraId();
                if (cameraId != null) {
                    Optional<AbstractNetatmoThingHandler> camera = findNAThing(cameraId);
                    camera.ifPresent(modules::add);
                }
            }
            if (HOME_EVENTS.contains(event.getType())) {
                String homeId = event.getHomeId();
                if (homeId != null) {
                    Optional<AbstractNetatmoThingHandler> home = findNAThing(homeId);
                    home.ifPresent(modules::add);
                }
            }
            if (PERSON_EVENTS.contains(event.getType())) {
                List<PersonsEventPerson> persons = event.getPersons();
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
                    triggerChannel(channel.getUID(), event.getType().toString());
                }
            });
        }
    }

    private String getWebHookURI() {
        String webHookURI = null;
        if (configuration.webHookUrl != null && configuration.readWelcome && webHookServlet != null) {
            webHookURI = configuration.webHookUrl + webHookServlet.getPath();
        }
        return webHookURI;
    }

    public boolean registerDataListener(@NonNull NetatmoDataListener dataListener) {
        return dataListeners.add(dataListener);
    }

    public boolean unregisterDataListener(@NonNull NetatmoDataListener dataListener) {
        return dataListeners.remove(dataListener);
    }

    public void checkForNewThings(Object data) {
        for (NetatmoDataListener dataListener : dataListeners) {
            dataListener.onDataRefreshed(data);
        }
    }
}
