/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.handler.capability;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;
import static org.openhab.binding.netatmo.internal.utils.ChannelTypeUtils.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.data.EventType;
import org.openhab.binding.netatmo.internal.api.data.ModuleType;
import org.openhab.binding.netatmo.internal.api.dto.Event;
import org.openhab.binding.netatmo.internal.api.dto.HomeDataModule;
import org.openhab.binding.netatmo.internal.api.dto.HomeEvent;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.api.dto.WebhookEvent;
import org.openhab.binding.netatmo.internal.handler.CommonInterface;
import org.openhab.binding.netatmo.internal.handler.channelhelper.ChannelHelper;
import org.openhab.binding.netatmo.internal.providers.NetatmoDescriptionProvider;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;

/**
 * {@link PersonCapability} gives the ability to handle Person specifics
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class PersonCapability extends HomeSecurityThingCapability {
    private final ChannelUID cameraChannelUID;
    private @Nullable ZonedDateTime lastEventTime;

    public PersonCapability(CommonInterface handler, NetatmoDescriptionProvider descriptionProvider,
            List<ChannelHelper> channelHelpers) {
        super(handler, descriptionProvider, channelHelpers);
        this.cameraChannelUID = new ChannelUID(thing.getUID(), GROUP_PERSON_LAST_EVENT, CHANNEL_EVENT_CAMERA_ID);
    }

    @Override
    protected void beforeNewData() {
        super.beforeNewData();
        getSecurityCapability().ifPresent(cap -> {
            Stream<HomeDataModule> cameras = cap.getModules().values().stream()
                    .filter(module -> module.getType() == ModuleType.WELCOME);
            descriptionProvider.setStateOptions(cameraChannelUID,
                    cameras.map(p -> new StateOption(p.getId(), p.getName())).toList());
        });
    }

    @Override
    public void handleCommand(String channelName, Command command) {
        if ((command instanceof OnOffType) && CHANNEL_PERSON_AT_HOME.equals(channelName)) {
            getSecurityCapability().ifPresent(cap -> cap.setPersonAway(handler.getId(), OnOffType.OFF.equals(command)));
        }
    }

    @Override
    protected void updateWebhookEvent(WebhookEvent event) {
        super.updateWebhookEvent(event);

        ThingUID thingUid = thing.getUID();

        handler.updateState(new ChannelUID(thingUid, GROUP_LAST_EVENT, CHANNEL_EVENT_SUBTYPE),
                event.getSubTypeDescription().map(d -> toStringType(d)).orElse(UnDefType.NULL));

        final String message = event.getName();
        handler.updateState(new ChannelUID(thingUid, GROUP_LAST_EVENT, CHANNEL_EVENT_MESSAGE),
                message == null || message.isBlank() ? UnDefType.NULL : toStringType(message));

        handler.updateState(new ChannelUID(thingUid, GROUP_LAST_EVENT, CHANNEL_EVENT_TIME),
                toDateTimeType(event.getTime()));

        handler.updateState(new ChannelUID(thingUid, GROUP_LAST_EVENT, CHANNEL_EVENT_SNAPSHOT),
                toRawType(event.getSnapshotUrl()));

        handler.updateState(cameraChannelUID, toStringType(event.getCameraId()));
    }

    @Override
    public void updateEvent(Event event) {
        super.updateEvent(event);
        EventType eventType = event.getEventType();
        ZonedDateTime localLast = lastEventTime;
        ZonedDateTime eventTime = event.getTime();
        if ((localLast != null && !eventTime.isAfter(localLast)) || !eventType.validFor(ModuleType.PERSON)) {
            return; // ignore incoming events if they are deprecated
        }
        lastEventTime = eventTime;
        handler.triggerChannel(CHANNEL_HOME_EVENT,
                event.getSubTypeDescription().map(st -> st.name()).orElse(event.getEventType().name()));
    }

    @Override
    public List<NAObject> updateReadings() {
        List<NAObject> result = new ArrayList<>();
        getSecurityCapability().ifPresent(cap -> {
            HomeEvent event = cap.getLastPersonEvent(handler.getId());
            if (event != null) {
                result.add(event);
            }
        });
        return result;
    }
}
