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
package org.openhab.binding.netatmo.internal.welcome;

import io.rudolph.netatmo.api.presence.model.Event;
import io.rudolph.netatmo.api.presence.model.Events;
import io.rudolph.netatmo.api.presence.model.Face;
import io.rudolph.netatmo.api.presence.model.Person;
import io.rudolph.netatmo.api.welcome.WelcomeConnector;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.netatmo.internal.handler.NetatmoModuleHandler;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;
import static org.openhab.binding.netatmo.internal.ChannelTypeUtils.toDateTimeType;
import static org.openhab.binding.netatmo.internal.ChannelTypeUtils.toStringType;


/**
 * {@link NAWelcomePersonHandler} is the class used to handle the Welcome Home Data
 *
 * @author Ing. Peter Weiss - Initial contribution
 *
 */
public class NAWelcomePersonHandler extends NetatmoModuleHandler<Person> {
    private String avatarURL;
    private Event lastEvent;

    public NAWelcomePersonHandler(@NonNull Thing thing) {
        super(thing);
    }

    @Override
    public void updateChannels(Object module) {
        if (isRefreshRequired()) {
            WelcomeConnector welcomeApi = getBridgeHandler().api.getWelcomeApi();
            Events events = welcomeApi.getLastEventOf(getParentId(), getId(), 10).executeSync();

            // Search the last event for this person
            events.getEventsList().forEach(event -> {
                if (event.getPersonId() != null && event.getPersonId().equalsIgnoreCase(getId())
                        && (lastEvent == null || lastEvent.getTime().isBefore(event.getTime()))) {
                    lastEvent = event;
                }
            });

            setRefreshRequired(false);
        }
        super.updateChannels(module);
    }

    @Override
    protected State getNAThingProperty(String channelId) {
        switch (channelId) {
            case CHANNEL_WELCOME_PERSON_LASTSEEN:
                return module != null ? toDateTimeType(module.getLastSeen()) : UnDefType.UNDEF;
            case CHANNEL_WELCOME_PERSON_ATHOME:
                return module != null ? module.getOutOfSight() ? OnOffType.OFF : OnOffType.ON : UnDefType.UNDEF;
            case CHANNEL_WELCOME_PERSON_AVATAR_URL:
                return toStringType(getAvatarURL());
            case CHANNEL_WELCOME_PERSON_AVATAR:
                return getAvatarURL() != null ? HttpUtil.downloadImage(getAvatarURL()) : UnDefType.UNDEF;
            case CHANNEL_WELCOME_PERSON_LASTMESSAGE:
                return (lastEvent != null && lastEvent.getMessage() != null)
                        ? toStringType(lastEvent.getMessage().replace("<b>", "").replace("</b>", ""))
                        : UnDefType.UNDEF;
            case CHANNEL_WELCOME_PERSON_LASTTIME:
                return lastEvent != null ? toDateTimeType(lastEvent.getTime()) : UnDefType.UNDEF;
            case CHANNEL_WELCOME_PERSON_LASTEVENT:
                return getLastEventURL() != null ? HttpUtil.downloadImage(getLastEventURL()) : UnDefType.UNDEF;
            case CHANNEL_WELCOME_PERSON_LASTEVENT_URL:
                return getLastEventURL() != null ? toStringType(getLastEventURL()) : UnDefType.UNDEF;
        }
        return super.getNAThingProperty(channelId);
    }

    private String getLastEventURL() {
        if (lastEvent != null && lastEvent.getSnapshot() != null) {
            return getBridgeHandler().getPictureUrl(lastEvent.getSnapshot().getId(), lastEvent.getSnapshot().getKey());
        }
        return null;
    }

    private String getAvatarURL() {
        if (avatarURL == null && module != null) {
            Face face = module.getFace();
            if (face != null) {
                avatarURL = getBridgeHandler().getPictureUrl(face.getId(), face.getKey());
            }
        }
        return avatarURL;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
        if ((command instanceof OnOffType) && (CHANNEL_WELCOME_PERSON_ATHOME.equalsIgnoreCase(channelUID.getId()))) {
            if ((OnOffType) command == OnOffType.OFF) {
                getBridgeHandler().api.getWelcomeApi().setPersonsAway(getParentId(), getId());
            } else {
                getBridgeHandler().api.getWelcomeApi().setPersonsHome(getParentId(), getId());
            }
            invalidateParentCacheAndRefresh();
        }
    }

}
