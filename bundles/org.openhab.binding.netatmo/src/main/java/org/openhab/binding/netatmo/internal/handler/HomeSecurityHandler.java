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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.NetatmoDescriptionProvider;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.HomeApi;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.dto.NAEvent;
import org.openhab.binding.netatmo.internal.api.dto.NAHome;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeEvent;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeSecurity;
import org.openhab.binding.netatmo.internal.api.dto.NAPerson;
import org.openhab.binding.netatmo.internal.api.dto.NAWelcome;
import org.openhab.binding.netatmo.internal.channelhelper.AbstractChannelHelper;
import org.openhab.binding.netatmo.internal.deserialization.NAObjectMap;
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
public class HomeSecurityHandler extends NetatmoEventDeviceHandler {
    private final Logger logger = LoggerFactory.getLogger(HomeSecurityHandler.class);

    // private @Nullable NetatmoServlet webhookServlet;
    // private ZonedDateTime maxEventTime;
    private List<NAPerson> knownPersons = List.of();
    private NAObjectMap<NAWelcome> cameras = new NAObjectMap<NAWelcome>();

    public HomeSecurityHandler(Bridge bridge, List<AbstractChannelHelper> channelHelpers, ApiBridge apiBridge,
            NetatmoDescriptionProvider descriptionProvider) {
        super(bridge, channelHelpers, apiBridge, descriptionProvider);
        // String lastEvent = editProperties().get(PROPERTY_MAX_EVENT_TIME);
        // maxEventTime = lastEvent != null ? ZonedDateTime.parse(lastEvent) : Instant.EPOCH.atZone(ZoneOffset.UTC);
    }

    // @Override
    // public void initialize() {
    // super.initialize();
    // NetatmoServlet servlet = this.webhookServlet;
    // if (servlet != null) {
    // servlet.registerDataListener(config.id, this);
    // }
    // }

    @Override
    protected NAHome updateReadings() throws NetatmoException {
        HomeApi api = apiBridge.getRestManager(HomeApi.class);
        if (api != null) {
            NAHome home = api.getHomes(config.id).iterator().next();
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
            localNaThing.getEvents().stream().filter(e -> e.getTime().isAfter(lastEventTime.get()))
                    .sorted(Comparator.comparing(NAHomeEvent::getTime)).forEach(event -> {
                        String personId = event.getPersonId();
                        if (personId != null) {
                            notifyListener(personId, event);
                        }
                        notifyListener(event.getCameraId(), event);
                        lastEventTime.set(event.getTime());
                    });
        }
    }

    // @Override
    // public void dispose() {
    // NetatmoServlet servlet = this.webhookServlet;
    // if (servlet != null) {
    // servlet.unregisterDataListener(this);
    // }
    // super.dispose();
    // }

    @Override
    public void setEvent(NAEvent event) {
        // if (event instanceof NAWebhookEvent) {
        // NAWebhookEvent whEvent = (NAWebhookEvent) event;
        // Set<String> modules = new HashSet<>();
        // if (whEvent.getEventType().appliesOn(ModuleType.NACamera)
        // || whEvent.getEventType().appliesOn(ModuleType.NOC)) {
        // modules.add(whEvent.getCameraId());
        // }
        // if (event.getEventType().appliesOn(ModuleType.NAPerson)) {
        // modules.addAll(whEvent.getPersons().keySet());
        // }
        // modules.forEach(module -> notifyListener(module, whEvent));
        // } else {
        // super.setEvent(event);
        // }
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
