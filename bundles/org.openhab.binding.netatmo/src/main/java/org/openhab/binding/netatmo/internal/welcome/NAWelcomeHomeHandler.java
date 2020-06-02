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

import java.util.*;

import io.swagger.client.model.*;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.netatmo.internal.handler.AbstractNetatmoThingHandler;
import org.openhab.binding.netatmo.internal.handler.NetatmoDeviceHandler;
import org.openhab.binding.netatmo.internal.webhook.NAWebhookCameraEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link NAWelcomeHomeHandler} is the class used to handle the Welcome Home Data
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Ing. Peter Weiss - Welcome camera implementation
 *
 */
@NonNullByDefault
public class NAWelcomeHomeHandler extends NetatmoDeviceHandler<NAWelcomeHome> {
    private static final Logger logger = LoggerFactory.getLogger(NAWelcomeHomeHandler.class);

    private int iPersons = -1;
    private int iUnknowns = -1;
    @Nullable private NAWelcomeEvent lastEvent;
    private boolean isNewLastEvent;
    @Nullable private Integer dataTimeStamp;

    public NAWelcomeHomeHandler(@NonNull Thing thing) {
        super(thing);
    }

    @Override
    protected @Nullable NAWelcomeHome updateReadings() {
        @Nullable NAWelcomeHome result = null;
        NAWelcomeHomeData homeDataBody = getBridgeHandler().getWelcomeDataBody(getId());
        if (homeDataBody != null) {
            // data time stamp is updated to now as WelcomeDataBody does not provide any information according to this
            // need
            dataTimeStamp = (int) (Calendar.getInstance().getTimeInMillis() / 1000);
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

                @Nullable NAWelcomeEvent previousLastEvent = lastEvent;
                result.getEvents().forEach(event -> {
                    if (lastEvent == null || lastEvent.getTime() < event.getTime()) {
                        lastEvent = event;
                    }
                });

                isNewLastEvent = previousLastEvent != null && !previousLastEvent.equals(lastEvent);
            }
        }
        return result;
    }

    @Override
    protected State getNAThingProperty(@Nullable String channelId) {
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
                @Nullable String url = findSnapshotURL();
                if(url != null) {
                    return HttpUtil.downloadImage(url);
                }
                return UnDefType.UNDEF;
            case CHANNEL_WELCOME_EVENT_SNAPSHOT_URL:
                @Nullable String snapshotURL = findSnapshotURL();
                if(snapshotURL != null) {
                    return toStringType(snapshotURL);
                }
                return UnDefType.UNDEF;
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
                @Nullable String eventMessage = findEventMessage();
                return eventMessage != null
                        ? new StringType(eventMessage.replace("<b>", "").replace("</b>", ""))
                        : UnDefType.UNDEF;
            case CHANNEL_WELCOME_EVENT_SUBTYPE:
                return lastEvent != null ? toDecimalType(lastEvent.getSubType()) : UnDefType.UNDEF;
        }
        return super.getNAThingProperty(channelId);
    }

    @Override
    protected void triggerChannelIfRequired(String channelId) {
        if(isNewLastEvent) {
            if(CHANNEL_CAMERA_EVENT.equals(channelId)) {
                Set<String> detectedObjectTypes = findDetectedObjectTypes(lastEvent);
                for(String detectedType: detectedObjectTypes) {
                    triggerChannel(channelId, detectedType);
                }
            }
        }
        super.triggerChannelIfRequired(channelId);
    }

    private static Set<String> findDetectedObjectTypes(@Nullable NAWelcomeEvent event) {
        Set<String> detectedObjectTypes = new TreeSet<>();
        if(event == null) {
            return detectedObjectTypes;
        }

        if(event.getPersonId() != null) {
            detectedObjectTypes.add(CAMERA_EVENT_OPTION_HUMAN_DETECTED);
        }

        if(NAWebhookCameraEvent.EventTypeEnum.MOVEMENT.toString().equals(event.getType())) {
            detectedObjectTypes.add(CAMERA_EVENT_OPTION_MOVEMENT_DETECTED);
        }

        List<NAWelcomeSubEvent> subEvents = event.getEventList();
        for(NAWelcomeSubEvent subEvent: subEvents) {
            String detectedObjectType = translateDetectedObjectType(subEvent.getType());
            detectedObjectTypes.add(detectedObjectType);
        }
        return detectedObjectTypes;
    }

    private static String translateDetectedObjectType(NAWelcomeSubEvent.TypeEnum type) {
        switch (type) {
            case HUMAN: return CAMERA_EVENT_OPTION_HUMAN_DETECTED;
            case ANIMAL: return CAMERA_EVENT_OPTION_ANIMAL_DETECTED;
            case VEHICLE: return CAMERA_EVENT_OPTION_VEHICLE_DETECTED;
            default: throw new IllegalArgumentException("Unknown detected object type!");
        }
    }

    private @Nullable String findEventMessage() {
        if(lastEvent != null) {
            @Nullable String message = lastEvent.getMessage();
            if(message == null) {
                @Nullable NAWelcomeSubEvent firstSubEvent = findFirstSubEvent(lastEvent);
                if(firstSubEvent != null) {
                    message = firstSubEvent.getMessage();
                }
            }
            return message;
        }
        return null;
    }

    /**
     * Returns the Url of the picture
     *
     * @return Url of the picture or null
     */
    protected @Nullable String findSnapshotURL() {
        if(lastEvent != null) {
            NAWelcomeSnapshot snapshot = lastEvent.getSnapshot();
            if (snapshot == null) {
                @Nullable NAWelcomeSubEvent firstSubEvent = findFirstSubEvent(lastEvent);
                if (firstSubEvent != null) {
                    snapshot = firstSubEvent.getSnapshot();
                }
            }

            if (snapshot != null && snapshot.getId() != null && snapshot.getKey() != null) {
                return WELCOME_PICTURE_URL + "?" + WELCOME_PICTURE_IMAGEID + "=" + snapshot.getId() + "&"
                        + WELCOME_PICTURE_KEY + "=" + snapshot.getKey();
            } else {
                logger.debug("Unable to build snapshot url for Home : {}", getId());
            }
        }
        return null;
    }

    @Override
    protected @Nullable Integer getDataTimestamp() {
        return dataTimeStamp;
    }

    private static @Nullable NAWelcomeSubEvent findFirstSubEvent(@Nullable NAWelcomeEvent event) {
        if(event != null) {
            List<NAWelcomeSubEvent> subEvents = event.getEventList();
            if (subEvents != null && !subEvents.isEmpty()) {
                return subEvents.get(0);
            }
        }
        return null;
    }
}
