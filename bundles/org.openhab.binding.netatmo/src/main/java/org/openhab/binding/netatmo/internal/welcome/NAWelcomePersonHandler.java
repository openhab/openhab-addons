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
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
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
@NonNullByDefault
public class NAWelcomePersonHandler extends NetatmoModuleHandler<NAWelcomePerson> {
    private @Nullable String avatarURL;
    private @Nullable NAWelcomeEvent lastEvent;

    public NAWelcomePersonHandler(Thing thing, final TimeZoneProvider timeZoneProvider) {
        super(thing, timeZoneProvider);
    }

    @Override
    public void updateChannels(Object module) {
        if (isRefreshRequired()) {
            getApi().ifPresent(api -> {
                NAWelcomeEventResponse eventResponse = api.getlasteventof(getParentId(), getId(), 10);

                // Search the last event for this person
                List<NAWelcomeEvent> rawEventList = eventResponse.getBody().getEventsList();
                rawEventList.forEach(event -> {
                    if (event.getPersonId() != null && event.getPersonId().equalsIgnoreCase(getId())
                            && (lastEvent == null || lastEvent.getTime() < event.getTime())) {
                        lastEvent = event;
                    }
                });
            });

            setRefreshRequired(false);
        }
        super.updateChannels(module);
    }

    @Override
    protected State getNAThingProperty(String channelId) {
        Optional<NAWelcomeEvent> lastEvt = getLastEvent();
        switch (channelId) {
            case CHANNEL_WELCOME_PERSON_LASTSEEN:
                return getModule().map(m -> toDateTimeType(m.getLastSeen(), timeZoneProvider.getTimeZone()))
                        .orElse(UnDefType.UNDEF);
            case CHANNEL_WELCOME_PERSON_ATHOME:
                return getModule()
                        .map(m -> m.getOutOfSight() != null ? toOnOffType(!m.getOutOfSight()) : UnDefType.UNDEF)
                        .orElse(UnDefType.UNDEF);
            case CHANNEL_WELCOME_PERSON_AVATAR_URL:
                return toStringType(getAvatarURL());
            case CHANNEL_WELCOME_PERSON_AVATAR:
                return getAvatarURL() != null ? HttpUtil.downloadImage(getAvatarURL()) : UnDefType.UNDEF;
            case CHANNEL_WELCOME_PERSON_LASTMESSAGE:
                return (lastEvt.isPresent() && lastEvt.get().getMessage() != null)
                        ? toStringType(lastEvt.get().getMessage().replace("<b>", "").replace("</b>", ""))
                        : UnDefType.UNDEF;
            case CHANNEL_WELCOME_PERSON_LASTTIME:
                return lastEvt.isPresent() ? toDateTimeType(lastEvt.get().getTime(), timeZoneProvider.getTimeZone())
                        : UnDefType.UNDEF;
            case CHANNEL_WELCOME_PERSON_LASTEVENT:
                return getLastEventURL() != null ? HttpUtil.downloadImage(getLastEventURL()) : UnDefType.UNDEF;
            case CHANNEL_WELCOME_PERSON_LASTEVENT_URL:
                return getLastEventURL() != null ? toStringType(getLastEventURL()) : UnDefType.UNDEF;
        }
        return super.getNAThingProperty(channelId);
    }

    private @Nullable String getLastEventURL() {
        Optional<NetatmoBridgeHandler> handler = getBridgeHandler();
        Optional<NAWelcomeEvent> lastEvt = getLastEvent();
        if (handler.isPresent() && lastEvt.isPresent() && lastEvt.get().getSnapshot() != null) {
            return handler.get().getPictureUrl(lastEvt.get().getSnapshot().getId(),
                    lastEvt.get().getSnapshot().getKey());
        }
        return null;
    }

    private @Nullable String getAvatarURL() {
        Optional<NetatmoBridgeHandler> handler = getBridgeHandler();
        Optional<NAWelcomePerson> person = getModule();
        if (handler.isPresent() && avatarURL == null && person.isPresent()) {
            NAWelcomeFace face = person.get().getFace();
            if (face != null) {
                avatarURL = handler.get().getPictureUrl(face.getId(), face.getKey());
            }
        }
        return avatarURL;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
        if ((command instanceof OnOffType) && (CHANNEL_WELCOME_PERSON_ATHOME.equalsIgnoreCase(channelUID.getId()))) {
            getApi().ifPresent(api -> {
                if ((OnOffType) command == OnOffType.OFF) {
                    api.setpersonsaway(getParentId(), getId());
                } else {
                    api.setpersonshome(getParentId(), "[\"" + getId() + "\"]");
                }
                invalidateParentCacheAndRefresh();
            });
        }
    }

    private Optional<WelcomeApi> getApi() {
        return getBridgeHandler().flatMap(handler -> handler.getWelcomeApi());
    }

    private Optional<NAWelcomeEvent> getLastEvent() {
        NAWelcomeEvent evt = lastEvent;
        return evt != null ? Optional.of(evt) : Optional.empty();
    }
}
