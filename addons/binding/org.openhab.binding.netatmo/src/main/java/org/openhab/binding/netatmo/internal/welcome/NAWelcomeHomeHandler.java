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

import java.util.Calendar;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.netatmo.handler.AbstractNetatmoThingHandler;
import org.openhab.binding.netatmo.handler.NetatmoDeviceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.client.model.NAWelcomeEvent;
import io.swagger.client.model.NAWelcomeHome;
import io.swagger.client.model.NAWelcomeHomeData;
import io.swagger.client.model.NAWelcomeSnapshot;

/**
 * {@link NAWelcomeHomeHandler} is the class used to handle the Welcome Home Data
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Ing. Peter Weiss - Welcome camera implementation
 *
 */
public class NAWelcomeHomeHandler extends NetatmoDeviceHandler<NAWelcomeHome> {
    private Logger logger = LoggerFactory.getLogger(NAWelcomeHomeHandler.class);

    private int iPersons = -1;
    private int iUnknowns = -1;
    private NAWelcomeEvent lastEvent;
    private Integer dataTimeStamp;

    public NAWelcomeHomeHandler(@NonNull Thing thing) {
        super(thing);
    }

    @Override
    protected NAWelcomeHome updateReadings() {
        NAWelcomeHome result = null;
        NAWelcomeHomeData homeDataBody = getBridgeHandler().getWelcomeDataBody(getId());
        if (homeDataBody != null) {
            // data time stamp is updated to now as WelcomeDataBody does not provide any information according to this
            // need
            dataTimeStamp = (int) Calendar.getInstance().getTimeInMillis() / 1000;
            result = homeDataBody.getHomes().stream().filter(device -> device.getId().equalsIgnoreCase(getId()))
                    .findFirst().get();
            if (result != null) {
                result.getCameras().forEach(camera -> childs.put(camera.getId(), camera));

                // Check how many persons are at home
                iPersons = 0;
                iUnknowns = 0;

                logger.debug("welcome home '{}' calculate Persons at home count", getId());
                result.getPersons().forEach(person -> {
                    iPersons += person.getOutOfSight() ? 0 : 1;
                    if (person.getPseudo() != null) {
                        childs.put(person.getId(), person);
                    } else {
                        iUnknowns += person.getOutOfSight() ? 0 : 1;
                    }
                });

                result.getEvents().forEach(event -> {
                    if (lastEvent == null || lastEvent.getTime() < event.getTime()) {
                        lastEvent = event;
                    }
                });
            }
        }
        return result;
    }

    @Override
    protected State getNAThingProperty(String channelId) {
        switch (channelId) {
            case CHANNEL_WELCOME_HOME_CITY:
                return device != null ? toStringType(device.getPlace().getCity()) : UnDefType.UNDEF;
            case CHANNEL_WELCOME_HOME_COUNTRY:
                return device != null ? toStringType(device.getPlace().getCountry()) : UnDefType.UNDEF;
            case CHANNEL_WELCOME_HOME_TIMEZONE:
                return device != null ? toStringType(device.getPlace().getTimezone()) : UnDefType.UNDEF;
            case CHANNEL_WELCOME_HOME_PERSONCOUNT:
                return iPersons != -1 ? new DecimalType(iPersons) : UnDefType.UNDEF;
            case CHANNEL_WELCOME_HOME_UNKNOWNCOUNT:
                return iUnknowns != -1 ? new DecimalType(iUnknowns) : UnDefType.UNDEF;
            case CHANNEL_WELCOME_EVENT_TYPE:
                return lastEvent != null ? toStringType(lastEvent.getType()) : UnDefType.UNDEF;
            case CHANNEL_WELCOME_EVENT_TIME:
                return lastEvent != null ? toDateTimeType(lastEvent.getTime()) : UnDefType.UNDEF;
            case CHANNEL_WELCOME_EVENT_CAMERAID:
                if (lastEvent != null) {
                    Optional<AbstractNetatmoThingHandler> camera = getBridgeHandler()
                            .findNAThing(lastEvent.getCameraId());
                    return camera.map(c -> toStringType(c.getThing().getLabel())).orElse(UnDefType.UNDEF);
                } else {
                    return UnDefType.UNDEF;
                }
            case CHANNEL_WELCOME_EVENT_PERSONID:
                if (lastEvent != null) {
                    Optional<AbstractNetatmoThingHandler> person = getBridgeHandler()
                            .findNAThing(lastEvent.getPersonId());
                    return person.map(p -> toStringType(p.getThing().getLabel())).orElse(UnDefType.UNDEF);
                } else {
                    return UnDefType.UNDEF;
                }
            case CHANNEL_WELCOME_EVENT_SNAPSHOT:
                if (lastEvent != null) {
                    String url = getSnapshotURL(lastEvent.getSnapshot());
                    return url != null ? HttpUtil.downloadImage(url) : UnDefType.UNDEF;
                } else {
                    return UnDefType.UNDEF;
                }
            case CHANNEL_WELCOME_EVENT_SNAPSHOT_URL:
                if (lastEvent != null) {
                    String snapshotURL = getSnapshotURL(lastEvent.getSnapshot());
                    return toStringType(snapshotURL);
                } else {
                    return UnDefType.UNDEF;
                }
            case CHANNEL_WELCOME_EVENT_VIDEO_URL:
                if (lastEvent != null && lastEvent.getVideoId() != null) {
                    String cameraId = lastEvent.getCameraId();
                    Optional<AbstractNetatmoThingHandler> thing = getBridgeHandler().findNAThing(cameraId);
                    if (thing.isPresent()) {
                        NAWelcomeCameraHandler eventCamera = (NAWelcomeCameraHandler) thing.get();
                        String streamUrl = eventCamera.getStreamURL(lastEvent.getVideoId());
                        if (streamUrl != null) {
                            return new StringType(streamUrl);
                        }
                    }
                }
                return UnDefType.UNDEF;
            case CHANNEL_WELCOME_EVENT_VIDEOSTATUS:
                return lastEvent != null ? toStringType(lastEvent.getVideoStatus()) : UnDefType.UNDEF;
            case CHANNEL_WELCOME_EVENT_ISARRIVAL:
                return lastEvent != null ? lastEvent.getIsArrival() != null ? OnOffType.ON : OnOffType.OFF
                        : UnDefType.UNDEF;
            case CHANNEL_WELCOME_EVENT_MESSAGE:
                return lastEvent != null && lastEvent.getMessage() != null
                        ? new StringType(lastEvent.getMessage().replace("<b>", "").replace("</b>", ""))
                        : UnDefType.UNDEF;
            case CHANNEL_WELCOME_EVENT_SUBTYPE:
                return lastEvent != null ? toDecimalType(lastEvent.getSubType()) : UnDefType.UNDEF;
        }
        return super.getNAThingProperty(channelId);
    }

    /**
     * Returns the Url of the picture
     *
     * @return Url of the picture or null
     */
    protected String getSnapshotURL(NAWelcomeSnapshot snapshot) {
        String result = null;

        if (snapshot != null && snapshot.getId() != null && snapshot.getKey() != null) {
            result = WELCOME_PICTURE_URL + "?" + WELCOME_PICTURE_IMAGEID + "=" + snapshot.getId() + "&"
                    + WELCOME_PICTURE_KEY + "=" + snapshot.getKey();
        } else {
            logger.debug("Unable to build snapshot url for Home : {}", getId());
        }
        return result;
    }

    @Override
    protected @Nullable Integer getDataTimestamp() {
        return dataTimeStamp;
    }

}
