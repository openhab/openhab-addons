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
package org.openhab.binding.netatmo.internal.handler.security;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.PROPERTY_MAX_EVENT_TIME;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.doc.ModuleType;
import org.openhab.binding.netatmo.internal.api.dto.NAEvent;
import org.openhab.binding.netatmo.internal.api.home.HomeApi;
import org.openhab.binding.netatmo.internal.api.home.NAHome;
import org.openhab.binding.netatmo.internal.api.home.NAHomeEvent;
import org.openhab.binding.netatmo.internal.api.home.NAPerson;
import org.openhab.binding.netatmo.internal.api.security.NAWelcome;
import org.openhab.binding.netatmo.internal.api.security.SecurityApi;
import org.openhab.binding.netatmo.internal.channelhelper.AbstractChannelHelper;
import org.openhab.binding.netatmo.internal.handler.NetatmoDeviceHandler;
import org.openhab.binding.netatmo.internal.handler.energy.NADescriptionProvider;
import org.openhab.binding.netatmo.internal.webhook.NAWebhookEvent;
import org.openhab.binding.netatmo.internal.webhook.NetatmoServlet;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * {@link NAHomeSecurityHandler} is the class used to handle the plug
 * device of a thermostat set
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 *
 */
@NonNullByDefault
public class NAHomeSecurityHandler extends NetatmoDeviceHandler {

    private final HomeApi homeApi;

    private @Nullable SecurityApi api;
    private @Nullable NetatmoServlet webhookServlet;

    private long maxEventTime;

    private List<NAPerson> knownPersons = List.of();
    private List<NAWelcome> cameras = List.of();

    public NAHomeSecurityHandler(Bridge bridge, List<AbstractChannelHelper> channelHelpers, ApiBridge apiBridge,
            TimeZoneProvider timeZoneProvider, NADescriptionProvider descriptionProvider) {
        super(bridge, channelHelpers, apiBridge, timeZoneProvider, descriptionProvider);
        this.homeApi = apiBridge.getHomeApi();
        this.api = apiBridge.getRestManager(SecurityApi.class);
        String lastEvent = editProperties().get(PROPERTY_MAX_EVENT_TIME);
        maxEventTime = lastEvent != null ? Long.parseLong(lastEvent) : 0;
    }

    @Override
    public void initialize() {
        super.initialize();
        if (webhookServlet != null) {
            webhookServlet.registerDataListener(config.id, this);
        }
    }

    @Override
    protected NAHome updateReadings() throws NetatmoException {
        if (api != null) {
            NAHome home = api.getWelcomeHomeData(config.id);
            this.knownPersons = home.getKnownPersons();
            this.cameras = home.getChilds().values().stream().collect(Collectors.toList());
            return home;
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
        NAHome localNaThing = (NAHome) naThing;
        if (localNaThing != null) {
            localNaThing.getPersons().entrySet().forEach(entry -> notifyListener(entry.getKey(), entry.getValue()));

            List<NAHomeEvent> actualEvents = localNaThing.getEvents().stream().filter(e -> e.getTime() > maxEventTime)
                    .sorted(Comparator.comparingLong(NAHomeEvent::getTime)).collect(Collectors.toList());

            actualEvents.stream().forEach(event -> {
                String personId = event.getPersonId();
                if (personId != null) {
                    notifyListener(personId, event);
                }
                notifyListener(event.getCameraId(), event);
                maxEventTime = event.getTime() - 1;
            });

            updateProperty(PROPERTY_MAX_EVENT_TIME, Long.toString(maxEventTime));
        }
    }

    public void setWebHookServlet(NetatmoServlet servlet) {
        this.webhookServlet = servlet;
    }

    @Override
    public void dispose() {
        if (webhookServlet != null) {
            webhookServlet.unregisterDataListener(this);
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
            modules.forEach(module -> this.notifyListener(module, whEvent));
        } else {
            super.setEvent(event);
        }
    }

    public void callSetPersonAway(String personId, boolean away) {
        if (away) {
            tryApiCall(() -> homeApi.setpersonsaway(config.id, personId));
        } else {
            tryApiCall(() -> homeApi.setpersonshome(config.id, personId));
        }
    }

    public List<NAPerson> getKnownPersons() {
        return this.knownPersons;
    }

    public List<NAWelcome> getCameras() {
        return this.cameras;
    }
}
