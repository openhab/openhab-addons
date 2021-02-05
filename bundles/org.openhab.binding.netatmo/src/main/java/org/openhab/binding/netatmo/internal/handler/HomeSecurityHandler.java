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

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.NetatmoDescriptionProvider;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.ModuleType;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.SecurityApi;
import org.openhab.binding.netatmo.internal.api.dto.NAEvent;
import org.openhab.binding.netatmo.internal.api.dto.NAHome;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeEvent;
import org.openhab.binding.netatmo.internal.api.dto.NAPerson;
import org.openhab.binding.netatmo.internal.api.dto.NAWelcome;
import org.openhab.binding.netatmo.internal.channelhelper.AbstractChannelHelper;
import org.openhab.binding.netatmo.internal.webhook.NAWebhookEvent;
import org.openhab.binding.netatmo.internal.webhook.NetatmoServlet;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * {@link HomeSecurityHandler} is the class used to handle the plug
 * device of a thermostat set
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class HomeSecurityHandler extends NetatmoDeviceHandler {

    private @Nullable NetatmoServlet webhookServlet;
    private long maxEventTime;
    private List<NAPerson> knownPersons = List.of();
    private List<NAWelcome> cameras = List.of();

    public HomeSecurityHandler(Bridge bridge, List<AbstractChannelHelper> channelHelpers, ApiBridge apiBridge,
            TimeZoneProvider timeZoneProvider, NetatmoDescriptionProvider descriptionProvider) {
        super(bridge, channelHelpers, apiBridge, timeZoneProvider, descriptionProvider);
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
        SecurityApi api = apiBridge.getRestManager(SecurityApi.class);
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
        if (naThing instanceof NAHome) {
            NAHome localNaThing = (NAHome) naThing;
            localNaThing.getPersons().entrySet().forEach(entry -> notifyListener(entry.getKey(), entry.getValue()));

            localNaThing.getEvents().stream().filter(e -> e.getTime() > maxEventTime)
                    .sorted(Comparator.comparingLong(NAHomeEvent::getTime)).forEach(event -> {
                        String personId = event.getPersonId();
                        if (personId != null) {
                            notifyListener(personId, event);
                        }
                        notifyListener(event.getCameraId(), event);
                        maxEventTime = event.getTime();
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

    public List<NAWelcome> getCameras() {
        return this.cameras;
    }
}
