/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.internal.welcome;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;
import static org.openhab.binding.netatmo.internal.ChannelTypeUtils.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.netatmo.handler.NetatmoModuleHandler;

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

    public NAWelcomePersonHandler(@NonNull Thing thing) {
        super(thing);
    }

    @Override
    public void updateChannels(Object module) {
        if (isRefreshRequired()) {
            WelcomeApi welcomeApi = getBridgeHandler().getWelcomeApi();
            NAWelcomeEventResponse eventResponse = welcomeApi.getlasteventof(getParentId(), getId(), 10);

            // Search the last event for this person
            List<NAWelcomeEvent> rawEventList = eventResponse.getBody().getEventsList();
            rawEventList.forEach(event -> {
                if (event.getPersonId() != null && event.getPersonId().equalsIgnoreCase(getId())
                        && (lastEvent == null || lastEvent.getTime() < event.getTime())) {
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
            NAWelcomeFace face = module.getFace();
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
                getBridgeHandler().getWelcomeApi().setpersonsaway(getParentId(), getId());
                requestParentRefresh();
                // } else {
                // Experimental, this method is not documented in the API but **seems** to work
                // Playing to much with it seems to lead to connection refused
                // getBridgeHandler().getWelcomeApi().setpersonshome(getParentId(), "[\"" + getId() + "\"]");
            }
        }
    }

}
