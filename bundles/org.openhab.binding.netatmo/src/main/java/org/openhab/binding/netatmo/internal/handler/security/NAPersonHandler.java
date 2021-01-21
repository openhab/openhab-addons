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

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;
import static org.openhab.binding.netatmo.internal.utils.ChannelTypeUtils.*;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.doc.EventType;
import org.openhab.binding.netatmo.internal.api.doc.ModuleType;
import org.openhab.binding.netatmo.internal.api.dto.NAEvent;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.binding.netatmo.internal.api.home.NASnapshot;
import org.openhab.binding.netatmo.internal.channelhelper.AbstractChannelHelper;
import org.openhab.binding.netatmo.internal.handler.NetatmoDeviceHandler;
import org.openhab.binding.netatmo.internal.handler.energy.NADescriptionProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;

/**
 * {@link NAPersonHandler} is the class used to handle the Welcome Home Data
 *
 * @author Ing. Peter Weiss - Initial contribution
 *
 */
@NonNullByDefault
public class NAPersonHandler extends NetatmoDeviceHandler {

    public NAPersonHandler(Bridge bridge, List<AbstractChannelHelper> channelHelpers, @Nullable ApiBridge apiBridge,
            TimeZoneProvider timeZoneProvider, NADescriptionProvider descriptionProvider) {
        super(bridge, channelHelpers, apiBridge, timeZoneProvider, descriptionProvider);
    }

    private @Nullable NAHomeSecurityHandler getHomeHandler() {
        NetatmoDeviceHandler handler = super.getBridgeHandler(getBridge());
        return handler != null ? (NAHomeSecurityHandler) handler : null;
    }

    @Override
    public void setNAThing(NAThing naModule) {
        super.setNAThing(naModule);
        NAHomeSecurityHandler homeHandler = getHomeHandler();
        if (homeHandler != null) {
            descriptionProvider.setStateOptions(
                    new ChannelUID(getThing().getUID(), GROUP_PERSON_EVENT, CHANNEL_EVENT_CAMERA_ID),
                    homeHandler.getCameras().stream().map(p -> new StateOption(p.getId(), p.getName()))
                            .collect(Collectors.toList()));
        }
    }

    @Override
    public void setEvent(NAEvent event) {
        updateIfLinked(GROUP_PERSON_EVENT, CHANNEL_EVENT_TIME, toDateTimeType(event.getTime(), zoneId));
        updateIfLinked(GROUP_PERSON_EVENT, CHANNEL_EVENT_CAMERA_ID, toStringType(event.getCameraId()));
        updateIfLinked(GROUP_WELCOME_EVENT, CHANNEL_EVENT_SUBTYPE,
                event.getSubTypeDescription().map(d -> toStringType(d)).orElse(UnDefType.NULL));

        NASnapshot snapshot = event.getSnapshot();
        if (snapshot != null) {
            String url = snapshot.getUrl();
            updateIfLinked(GROUP_PERSON_EVENT, CHANNEL_EVENT_SNAPSHOT, toRawType(url));
            updateIfLinked(GROUP_PERSON_EVENT, CHANNEL_EVENT_SNAPSHOT_URL, toStringType(url));
        }

        EventType eventType = event.getEventType();
        if (eventType.appliesOn(ModuleType.NAPerson)) {
            updateIfLinked(GROUP_PERSON, CHANNEL_PERSON_AT_HOME, OnOffType.from(eventType == EventType.PERSON));
            triggerChannel(CHANNEL_HOME_EVENT, eventType.name());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if ((command instanceof OnOffType) && CHANNEL_PERSON_AT_HOME.equals(channelUID.getIdWithoutGroup())) {
            NAHomeSecurityHandler homeHandler = getHomeHandler();
            if (homeHandler != null) {
                homeHandler.callSetPersonAway(config.id, command == OnOffType.OFF);
            }
        } else {
            super.handleCommand(channelUID, command);
        }
    }
}
