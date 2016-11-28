/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.handler.welcome;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.netatmo.config.NetatmoWelcomeConfiguration;
import org.openhab.binding.netatmo.internal.ChannelTypeUtils;

import io.swagger.client.model.NAWelcomeEvents;

/**
 * {@link NAWelcomeEventHandler} is the class used to handle the Welcome Event Data
 *
 * @author Ing. Peter Weiss - Welcome camera implementation
 *
 */
public class NAWelcomeEventHandler extends AbstractNetatmoWelcomeHandler {

    private NetatmoWelcomeConfiguration configuration;
    protected NAWelcomeEvents event;

    public NAWelcomeEventHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
        this.configuration = this.getConfigAs(NetatmoWelcomeConfiguration.class);
    }

    public String getParentId() {
        return configuration.getWelcomeHomeId();
    }

    public String getId() {
        return configuration.getWelcomeEventId();
    }

    @Override
    protected void updateChannels() {
        try {
            for (Thing thing : getBridgeHandler().getThing().getThings()) {
                ThingHandler thingHandler = thing.getHandler();
                if (thingHandler instanceof NAWelcomeHomeHandler) {
                    NAWelcomeHomeHandler welcomeHomeHandler = (NAWelcomeHomeHandler) thingHandler;
                    String parentId = welcomeHomeHandler.getId();
                    if (parentId != null && parentId.equals(getParentId())) {
                        int index = Integer.parseInt((getId().substring(getId().indexOf('#') + 1))) - 1;
                        this.event = getWelcomeHomes(getParentId()).getEvents().get(index);
                        super.updateChannels();
                        break;
                    }
                }
            }

        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, e.getMessage());
        }
    }

    @Override
    protected State getNAThingProperty(String chanelId) {
        try {
            switch (chanelId) {
                case CHANNEL_WELCOME_EVENT_ID:
                    return event.getId() != null ? new StringType(event.getId()) : UnDefType.UNDEF;
                case CHANNEL_WELCOME_EVENT_TYPE:
                    return event.getType() != null ? new StringType(event.getType()) : UnDefType.UNDEF;
                case CHANNEL_WELCOME_EVENT_TIME:
                    return event.getTime() != null ? ChannelTypeUtils.toDateTimeType(event.getTime()) : UnDefType.UNDEF;
                case CHANNEL_WELCOME_EVENT_CAMERAID:
                    return event.getCameraId() != null ? new StringType(event.getCameraId()) : UnDefType.UNDEF;
                case CHANNEL_WELCOME_EVENT_PERSONID:
                    return event.getPersonId() != null ? new StringType(event.getPersonId()) : UnDefType.UNDEF;
                case CHANNEL_WELCOME_EVENT_SNAPSHOTID:
                    return event.getSnapshot().getId() != null ? new StringType(event.getSnapshot().getId())
                            : UnDefType.UNDEF;
                case CHANNEL_WELCOME_EVENT_SNAPSHOTVERSION:
                    return event.getSnapshot().getVersion() != null ? new DecimalType(event.getSnapshot().getVersion())
                            : UnDefType.UNDEF;
                case CHANNEL_WELCOME_EVENT_SNAPSHOTKEY:
                    return event.getSnapshot().getKey() != null ? new StringType(event.getSnapshot().getKey())
                            : UnDefType.UNDEF;
                case CHANNEL_WELCOME_EVENT_VIDEOID:
                    return event.getVideoId() != null ? new StringType(event.getVideoId()) : UnDefType.UNDEF;
                case CHANNEL_WELCOME_EVENT_VIDEOSTATUS:
                    return event.getVideoStatus() != null ? new StringType(event.getVideoStatus()) : UnDefType.UNDEF;
                case CHANNEL_WELCOME_EVENT_ISARRIVAL:
                    return event.getIsArrival() != null ? (event.getIsArrival() ? OnOffType.ON : OnOffType.OFF)
                            : UnDefType.UNDEF;
                case CHANNEL_WELCOME_EVENT_MESSAGE:
                    return event.getMessage() != null ? new StringType(event.getMessage()) : UnDefType.UNDEF;
                case CHANNEL_WELCOME_EVENT_SUBTYPE:
                    return event.getSubType() != null ? new DecimalType(event.getSubType()) : UnDefType.UNDEF;

                case CHANNEL_WELCOME_EVENT_PICTURE_URL:
                    return event.getSnapshot() != null
                            ? getPictureUrl(event.getSnapshot().getId(), event.getSnapshot().getKey())
                            : UnDefType.UNDEF;
                case CHANNEL_WELCOME_EVENT_VIDEOPOOR_URL:
                    return getVideoUrl(POOR);
                case CHANNEL_WELCOME_EVENT_VIDEOLOW_URL:
                    return getVideoUrl(LOW);
                case CHANNEL_WELCOME_EVENT_VIDEOMEDIUM_URL:
                    return getVideoUrl(MEDIUM);
                case CHANNEL_WELCOME_EVENT_VIDEOHIGH_URL:
                    return getVideoUrl(HIGH);

                default:
                    return super.getNAThingProperty(chanelId);
            }
        } catch (Exception e) {
            return UnDefType.UNDEF;
        }
    }

    /**
     * Get the url of the live video strem
     *
     * @param i
     *
     * @return Url of the video stream or UnDefType.UNDEF
     */
    private State getVideoUrl(int iQuality) {
        State ret = UnDefType.UNDEF;

        if (event != null) {
            String sUrl = getVideoUrl(event.getCameraId());
            String sVideoID = event.getVideoId();
            if (sUrl != null && sVideoID != null) {
                switch (iQuality) {
                    case POOR:
                        sUrl += String.format(WELCOME_VOD_VIDEO_POOR, sVideoID);
                        break;
                    case LOW:
                        sUrl += String.format(WELCOME_VOD_VIDEO_LOW, sVideoID);
                        break;
                    case MEDIUM:
                        sUrl += String.format(WELCOME_VOD_VIDEO_MEDIUM, sVideoID);
                        break;
                    case HIGH:
                        sUrl += String.format(WELCOME_VOD_VIDEO_HIGH, sVideoID);
                        break;
                    default:
                        sUrl = null;
                        break;
                }

                if (sUrl != null) {
                    ret = new StringType(sUrl);
                }
            }
        }

        return ret;
    }

}