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
import io.rudolph.netatmo.api.presence.model.PresenceHome;
import io.rudolph.netatmo.api.presence.model.SecurityHome;
import io.rudolph.netatmo.api.presence.model.Snapshot;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.netatmo.internal.handler.AbstractNetatmoThingHandler;
import org.openhab.binding.netatmo.internal.handler.NetatmoDeviceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Optional;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;
import static org.openhab.binding.netatmo.internal.ChannelTypeUtils.*;

/**
 * {@link NAWelcomeHomeHandler} is the class used to handle the Welcome Home Data
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Ing. Peter Weiss - Welcome camera implementation
 *
 */
public class NAWelcomeHomeHandler extends NetatmoDeviceHandler<PresenceHome> {
    private Logger logger = LoggerFactory.getLogger(NAWelcomeHomeHandler.class);

    private int iPersons = -1;
    private int iUnknowns = -1;
    private Event lastEvent;
    private Long dataTimeStamp;

    public NAWelcomeHomeHandler(@NonNull Thing thing) {
        super(thing);
    }

    @Override
    protected PresenceHome updateReadings() {
        PresenceHome result = null;
        SecurityHome homeDataBody = getBridgeHandler().api.getWelcomeApi().getHomeData(getId(), null).executeSync();
        if (homeDataBody != null) {
            // data time stamp is updated to now as WelcomeDataBody does not provide any information according to this
            // need
            dataTimeStamp = Calendar.getInstance().getTimeInMillis() / 1000;
            result = homeDataBody.getHomes().stream().filter(device -> device.getId().equalsIgnoreCase(getId()))
                    .findFirst().orElse(null);
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
                    if (lastEvent == null || lastEvent.getTime().isBefore(event.getTime())) {
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
                return lastEvent != null ? toStringType(lastEvent.getType().getValue()) : UnDefType.UNDEF;
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
                if (lastEvent != null && lastEvent.getSnapshot() != null
                        && lastEvent.getSnapshot().getId() != null
                        && lastEvent.getSnapshot().getKey() != null) {
                    String picture = getBridgeHandler().api
                            .getWelcomeApi()
                            .getCameraPicture(lastEvent.getSnapshot().getId(), lastEvent.getSnapshot().getKey())
                            .executeSync();
                    return picture != null ? new RawType(picture.getBytes()) : UnDefType.UNDEF;
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
                return lastEvent != null ? lastEvent.isArrival() != null ? OnOffType.ON : OnOffType.OFF
                        : UnDefType.UNDEF;
            case CHANNEL_WELCOME_EVENT_MESSAGE:
                return lastEvent != null && lastEvent.getMessage() != null
                        ? new StringType(lastEvent.getMessage().replace("<b>", "").replace("</b>", ""))
                        : UnDefType.UNDEF;
            case CHANNEL_WELCOME_EVENT_SUBTYPE:
                return lastEvent != null ? toDecimalType(lastEvent.getSubType().getValue()) : UnDefType.UNDEF;
        }
        return super.getNAThingProperty(channelId);
    }

    /**
     * Returns the Url of the picture
     *
     * @return Url of the picture or null
     */
    protected String getSnapshotURL(Snapshot snapshot) {
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
    protected @Nullable Long getDataTimestamp() {
        return dataTimeStamp;
    }

}
