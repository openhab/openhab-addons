/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.PROPERTY_MAX_EVENT_TIME;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.NetatmoDescriptionProvider;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.HomeApi;
import org.openhab.binding.netatmo.internal.api.ModuleType;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.dto.NAEvent;
import org.openhab.binding.netatmo.internal.api.dto.NAHome;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeEvent;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeSecurity;
import org.openhab.binding.netatmo.internal.api.dto.NAPerson;
import org.openhab.binding.netatmo.internal.api.dto.NAWebhookEvent;
import org.openhab.binding.netatmo.internal.api.dto.NAWelcome;
import org.openhab.binding.netatmo.internal.channelhelper.AbstractChannelHelper;
import org.openhab.binding.netatmo.internal.deserialization.NAObjectMap;
import org.openhab.binding.netatmo.internal.webhook.NetatmoServlet;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link HomeSecurityHandler} is the class used to handle the plug
 * device of a thermostat set
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class HomeSecurityHandler extends NetatmoDeviceHandler {
    private final Logger logger = LoggerFactory.getLogger(HomeSecurityHandler.class);

    private @Nullable NetatmoServlet webhookServlet;
    private ZonedDateTime maxEventTime;
    private List<NAPerson> knownPersons = List.of();
    private NAObjectMap<NAWelcome> cameras = new NAObjectMap<NAWelcome>();

    public HomeSecurityHandler(Bridge bridge, List<AbstractChannelHelper> channelHelpers, ApiBridge apiBridge,
            NetatmoDescriptionProvider descriptionProvider) {
        super(bridge, channelHelpers, apiBridge, descriptionProvider);
        String lastEvent = editProperties().get(PROPERTY_MAX_EVENT_TIME);
        maxEventTime = lastEvent != null ? ZonedDateTime.parse(lastEvent) : Instant.EPOCH.atZone(ZoneOffset.UTC);
    }

    @Override
    public void initialize() {
        super.initialize();
        NetatmoServlet servlet = this.webhookServlet;
        if (servlet != null) {
            servlet.registerDataListener(config.id, this);
        }
    }

    @Override
    protected NAHome updateReadings() throws NetatmoException {
        HomeApi api = apiBridge.getRestManager(HomeApi.class);
        if (api != null) {
            NAHome home = api.getHomes(config.id).get(0);
            if (home instanceof NAHomeSecurity) {
                this.knownPersons = ((NAHomeSecurity) home).getKnownPersons();
                this.cameras = ((NAHomeSecurity) home).getCameras();
                return home;
            }
        }
        throw new NetatmoException("No api available to access Welcome Home");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            super.handleCommand(channelUID, command);
        } else {

        }
    }

    @Override
    protected void updateChildModules() {
        super.updateChildModules();
        if (naThing instanceof NAHomeSecurity) {
            NAHomeSecurity localNaThing = (NAHomeSecurity) naThing;
            // localNaThing.getCameras().forEach(entry -> notifyListener(entry.getId(), entry));
            // NAObjectMap<NAPerson> persons = localNaThing.getPersons();
            // if (persons != null) {
            // persons.entrySet().forEach(entry -> notifyListener(entry.getKey(), entry.getValue()));
            // }
            localNaThing.getEvents().stream().filter(e -> e.getTime().isAfter(maxEventTime))
                    .sorted(Comparator.comparing(NAHomeEvent::getTime)).forEach(event -> {
                        String personId = event.getPersonId();
                        if (personId != null) {
                            notifyListener(personId, event);
                        }
                        notifyListener(event.getCameraId(), event);
                        maxEventTime = event.getTime();
                    });

            updateProperty(PROPERTY_MAX_EVENT_TIME, maxEventTime.toString());
        }
    }

    public void setWebHookServlet(NetatmoServlet servlet) {
        this.webhookServlet = servlet;
    }

    @Override
    public void dispose() {
        NetatmoServlet servlet = this.webhookServlet;
        if (servlet != null) {
            servlet.unregisterDataListener(this);
        }
        super.dispose();
    }

    @Override
    public void setEvent(NAEvent event) {
        if (event instanceof NAWebhookEvent) {
            NAWebhookEvent whEvent = (NAWebhookEvent) event;
            Set<String> modules = new HashSet<>();
            if (whEvent.getEventType().appliesOn(ModuleType.NACamera)
                    || whEvent.getEventType().appliesOn(ModuleType.NOC)) {
                modules.add(whEvent.getCameraId());
            }
            if (event.getEventType().appliesOn(ModuleType.NAPerson)) {
                modules.addAll(whEvent.getPersons().keySet());
            }
            modules.forEach(module -> notifyListener(module, whEvent));
        } else {
            super.setEvent(event);
        }
    }

    public void callSetPersonAway(String personId, boolean away) {
        if (away) {
            tryApiCall(() -> apiBridge.getHomeApi().setpersonsaway(config.id, personId));
        } else {
            tryApiCall(() -> apiBridge.getHomeApi().setpersonshome(config.id, personId));
        }
    }

    public List<NAPerson> getKnownPersons() {
        return this.knownPersons;
    }

    public NAObjectMap<NAWelcome> getCameras() {
        return this.cameras;
    }

    public List<NAHomeEvent> getLastEventOf(String personId) {
        List<NAHomeEvent> events = new ArrayList<>();
        apiBridge.getSecurityApi().ifPresent(api -> {
            try {
                events.addAll(api.getLastEventOf(config.id, personId));
            } catch (NetatmoException e) {
                logger.warn("Error retrieving last events of person '{}' : {}", personId, e.getMessage());
            }
        });
        return events;
    }
}
