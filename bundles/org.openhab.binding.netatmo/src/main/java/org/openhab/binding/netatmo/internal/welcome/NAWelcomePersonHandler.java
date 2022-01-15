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
package org.openhab.binding.netatmo.internal.welcome;

import static org.openhab.binding.netatmo.internal.APIUtils.*;
import static org.openhab.binding.netatmo.internal.ChannelTypeUtils.*;
import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.handler.NetatmoBridgeHandler;
import org.openhab.binding.netatmo.internal.handler.NetatmoModuleHandler;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

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
                nonNullList(eventResponse.getBody().getEventsList()).forEach(event -> {
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
        String url;
        switch (channelId) {
            case CHANNEL_WELCOME_PERSON_LASTSEEN:
                return getModule().map(m -> toDateTimeType(m.getLastSeen(), timeZoneProvider.getTimeZone()))
                        .orElse(UnDefType.UNDEF);
            case CHANNEL_WELCOME_PERSON_ATHOME:
                return getModule().map(m -> m.isOutOfSight() != null ? toOnOffType(!m.isOutOfSight()) : UnDefType.UNDEF)
                        .orElse(UnDefType.UNDEF);
            case CHANNEL_WELCOME_PERSON_AVATAR_URL:
                return toStringType(getAvatarURL());
            case CHANNEL_WELCOME_PERSON_AVATAR:
                url = getAvatarURL();
                return url != null ? toRawType(url) : UnDefType.UNDEF;
            case CHANNEL_WELCOME_PERSON_LASTMESSAGE:
                return (lastEvt.isPresent() && lastEvt.get().getMessage() != null)
                        ? toStringType(lastEvt.get().getMessage().replace("<b>", "").replace("</b>", ""))
                        : UnDefType.UNDEF;
            case CHANNEL_WELCOME_PERSON_LASTTIME:
                return lastEvt.isPresent() ? toDateTimeType(lastEvt.get().getTime(), timeZoneProvider.getTimeZone())
                        : UnDefType.UNDEF;
            case CHANNEL_WELCOME_PERSON_LASTEVENT:
                url = getLastEventURL();
                return url != null ? toRawType(url) : UnDefType.UNDEF;
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
