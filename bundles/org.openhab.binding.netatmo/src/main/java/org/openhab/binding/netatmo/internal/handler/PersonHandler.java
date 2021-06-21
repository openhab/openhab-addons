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

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;
import static org.openhab.binding.netatmo.internal.utils.ChannelTypeUtils.*;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.NetatmoDescriptionProvider;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.EventType;
import org.openhab.binding.netatmo.internal.api.ModuleType;
import org.openhab.binding.netatmo.internal.api.dto.NAEvent;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeEvent;
import org.openhab.binding.netatmo.internal.api.dto.NAWelcome;
import org.openhab.binding.netatmo.internal.channelhelper.AbstractChannelHelper;
import org.openhab.binding.netatmo.internal.deserialization.NAObjectMap;
import org.openhab.binding.netatmo.internal.utils.ChannelTypeUtils;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link PersonHandler} is the class used to handle the Welcome Home Data
 *
 * @author Ing. Peter Weiss - Initial contribution
 *
 */
@NonNullByDefault
public class PersonHandler extends NetatmoEventDeviceHandler {
    private final Logger logger = LoggerFactory.getLogger(PersonHandler.class);

    public PersonHandler(Bridge bridge, List<AbstractChannelHelper> channelHelpers, ApiBridge apiBridge,
            NetatmoDescriptionProvider descriptionProvider) {
        super(bridge, channelHelpers, apiBridge, descriptionProvider);
    }

    @Override
    public void initialize() {
        super.initialize();
        getHomeHandler().ifPresent(h -> {
            List<NAHomeEvent> lastEvents = h.getLastEventOf(config.id);
            if (!lastEvents.isEmpty()) {
                setEvent(lastEvents.get(0));
            }
        });
    }

    public void setCameras(NAObjectMap<NAWelcome> cameras) {
        descriptionProvider.setStateOptions(
                new ChannelUID(getThing().getUID(), GROUP_PERSON_EVENT, CHANNEL_EVENT_CAMERA_ID), cameras.values()
                        .stream().map(p -> new StateOption(p.getId(), p.getName())).collect(Collectors.toList()));
    }

    @Override
    public void setEvent(NAEvent event) {
        logger.debug("Updating person  with event : {}", event.toString());

        updateIfLinked(GROUP_PERSON_EVENT, CHANNEL_EVENT_TIME, new DateTimeType(event.getTime()));
        updateIfLinked(GROUP_PERSON_EVENT, CHANNEL_EVENT_CAMERA_ID, toStringType(event.getCameraId()));
        updateIfLinked(GROUP_PERSON_EVENT, CHANNEL_EVENT_SUBTYPE,
                event.getSubTypeDescription().map(ChannelTypeUtils::toStringType).orElse(UnDefType.NULL));

        event.getSnapshot().ifPresent(snapshot -> {
            String url = snapshot.getUrl();
            updateIfLinked(GROUP_PERSON_EVENT, CHANNEL_EVENT_SNAPSHOT, toRawType(url));
            updateIfLinked(GROUP_PERSON_EVENT, CHANNEL_EVENT_SNAPSHOT_URL, toStringType(url));
        });

        EventType eventType = event.getEventType();
        if (eventType.appliesOn(ModuleType.NAPerson)) {
            updateIfLinked(GROUP_PERSON, CHANNEL_PERSON_AT_HOME, OnOffType.from(eventType == EventType.PERSON));
            triggerChannel(CHANNEL_HOME_EVENT, eventType.name());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if ((command instanceof OnOffType) && CHANNEL_PERSON_AT_HOME.equals(channelUID.getIdWithoutGroup())) {
            getHomeHandler().ifPresent(h -> h.callSetPersonAway(config.id, command == OnOffType.OFF));
        } else {
            super.handleCommand(channelUID, command);
        }
    }
}
