/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import static org.openhab.binding.netatmo.internal.ChannelTypeUtils.*;
import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.i18n.TimeZoneProvider;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.netatmo.internal.handler.NetatmoBridgeHandler;
import org.openhab.binding.netatmo.internal.handler.NetatmoModuleHandler;

import io.swagger.client.api.WelcomeApi;
import io.swagger.client.model.NAWelcomeEvent;
import io.swagger.client.model.NAWelcomeEventResponse;
import io.swagger.client.model.NAWelcomeFace;
import io.swagger.client.model.NAWelcomePerson;

/**
 * {@link NAWelcomePersonHandler} is the class used to handle the Welcome Home Data
 *
 * @author Ing. Peter Weiss - Initial contribution
 *
 */
public class NAWelcomePersonHandler extends NetatmoModuleHandler<NAWelcomePerson> {
    private String avatarURL;
    private NAWelcomeEvent lastEvent;

    public NAWelcomePersonHandler(@NonNull Thing thing, final TimeZoneProvider timeZoneProvider) {
        super(thing, timeZoneProvider);
    }

    private @Nullable WelcomeApi getWelcomeApi() {
        NetatmoBridgeHandler bridgeHandler = getBridgeHandler();
        return bridgeHandler == null ? null : bridgeHandler.getWelcomeApi();
    }

    @Override
    public void updateChannels(Object module) {
        if (isRefreshRequired()) {
            WelcomeApi welcomeApi = getWelcomeApi();
            if (welcomeApi != null) {
                NAWelcomeEventResponse eventResponse = welcomeApi.getlasteventof(getParentId(), getId(), 10);

                // Search the last event for this person
                List<NAWelcomeEvent> rawEventList = eventResponse.getBody().getEventsList();
                rawEventList.forEach(event -> {
                    if (event.getPersonId() != null && event.getPersonId().equalsIgnoreCase(getId())
                            && (lastEvent == null || lastEvent.getTime() < event.getTime())) {
                        lastEvent = event;
                    }
                });
            }

            setRefreshRequired(false);
        }
        super.updateChannels(module);
    }

    @Override
    protected State getNAThingProperty(@NonNull String channelId) {
        NAWelcomePerson module = this.module;
        switch (channelId) {
            case CHANNEL_WELCOME_PERSON_LASTSEEN:
                return module != null ? toDateTimeType(module.getLastSeen(), timeZoneProvider.getTimeZone())
                        : UnDefType.UNDEF;
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
                return lastEvent != null ? toDateTimeType(lastEvent.getTime(), timeZoneProvider.getTimeZone())
                        : UnDefType.UNDEF;
            case CHANNEL_WELCOME_PERSON_LASTEVENT:
                return getLastEventURL() != null ? HttpUtil.downloadImage(getLastEventURL()) : UnDefType.UNDEF;
            case CHANNEL_WELCOME_PERSON_LASTEVENT_URL:
                return getLastEventURL() != null ? toStringType(getLastEventURL()) : UnDefType.UNDEF;
        }
        return super.getNAThingProperty(channelId);
    }

    private String getLastEventURL() {
        NetatmoBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler != null && lastEvent != null && lastEvent.getSnapshot() != null) {
            return bridgeHandler.getPictureUrl(lastEvent.getSnapshot().getId(), lastEvent.getSnapshot().getKey());
        }
        return null;
    }

    private String getAvatarURL() {
        NetatmoBridgeHandler bridgeHandler = getBridgeHandler();
        NAWelcomePerson module = this.module;
        if (bridgeHandler != null && avatarURL == null && module != null) {
            NAWelcomeFace face = module.getFace();
            if (face != null) {
                avatarURL = bridgeHandler.getPictureUrl(face.getId(), face.getKey());
            }
        }
        return avatarURL;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
        WelcomeApi welcomeApi = getWelcomeApi();
        if (welcomeApi != null && (command instanceof OnOffType)
                && (CHANNEL_WELCOME_PERSON_ATHOME.equalsIgnoreCase(channelUID.getId()))) {
            if ((OnOffType) command == OnOffType.OFF) {
                welcomeApi.setpersonsaway(getParentId(), getId());
            } else {
                welcomeApi.setpersonshome(getParentId(), "[\"" + getId() + "\"]");
            }
            invalidateParentCacheAndRefresh();
        }
    }
}
