/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.data.EventType;
import org.openhab.binding.netatmo.internal.api.data.ModuleType;
import org.openhab.binding.netatmo.internal.api.dto.NAEvent;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeDataModule;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeEvent;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.deserialization.NAObjectMap;
import org.openhab.binding.netatmo.internal.handler.capability.SecurityCapability;
import org.openhab.binding.netatmo.internal.handler.channelhelper.AbstractChannelHelper;
import org.openhab.binding.netatmo.internal.handler.propertyhelper.PropertyHelper;
import org.openhab.binding.netatmo.internal.providers.NetatmoDescriptionProvider;
import org.openhab.binding.netatmo.internal.webhook.NetatmoServlet;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateOption;

/**
 * {@link PersonHandler} is the class handling Person things
 *
 * @author Ing. Peter Weiss - Initial contribution
 *
 */
@NonNullByDefault
public class PersonHandler extends NetatmoHandler {
    private Optional<SecurityCapability> securityCap = Optional.empty();

    public PersonHandler(Bridge bridge, List<AbstractChannelHelper> channelHelpers, ApiBridge apiBridge,
            NetatmoDescriptionProvider descriptionProvider, NetatmoServlet webhookServlet) {
        super(bridge, channelHelpers, apiBridge, descriptionProvider, webhookServlet);
    }

    @Override
    protected PropertyHelper getPropertyHelper() {
        return new PropertyHelper(getThing());
    }

    @Override
    public void initialize() {
        super.initialize();

        NetatmoHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler instanceof HomeHandler) {
            HomeHandler homeHandler = (HomeHandler) bridgeHandler;
            securityCap = homeHandler.getSecurityCap();
            homeHandler.getHomeCap().ifPresent(cap -> {
                NAObjectMap<NAHomeDataModule> modules = cap.getModules();
                updateStatus(ThingStatus.ONLINE);
                descriptionProvider.setStateOptions(
                        new ChannelUID(getThing().getUID(), GROUP_PERSON_EVENT, CHANNEL_EVENT_CAMERA_ID),
                        modules.values().stream().filter(module -> module.getType() == ModuleType.NACamera)
                                .map(p -> new StateOption(p.getId(), p.getName())).collect(Collectors.toList()));
            });
        }
    }

    @Override
    protected List<NAObject> updateReadings() throws NetatmoException {
        List<NAObject> result = new ArrayList<>();
        securityCap.ifPresent(cap -> {
            Collection<NAHomeEvent> events = cap.getPersonEvents(getId());
            if (!events.isEmpty()) {
                result.add(events.iterator().next());
            }
        });
        return result;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if ((command instanceof OnOffType) && CHANNEL_PERSON_AT_HOME.equals(channelUID.getIdWithoutGroup())) {
            securityCap.ifPresent(cap -> cap.setPersonAway(getId(), OnOffType.OFF.equals(command)));
        } else {
            super.handleCommand(channelUID, command);
        }
    }

    @Override
    public void setNewData(NAObject newData) {
        if (newData instanceof NAEvent) {
            NAEvent event = (NAEvent) newData;
            EventType eventType = event.getEventType();
            if (eventType.appliesOn(ModuleType.NAPerson)) {
                triggerChannel(CHANNEL_HOME_EVENT, eventType.name());
            }
        }
        super.setNewData(newData);
    }
}
