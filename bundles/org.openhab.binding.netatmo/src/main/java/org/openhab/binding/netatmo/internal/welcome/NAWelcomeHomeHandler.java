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
import java.util.function.Function;

import io.swagger.client.model.*;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.netatmo.internal.ChannelTypeUtils;
import org.openhab.binding.netatmo.internal.camera.CameraHandler;
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
    private final Logger logger = LoggerFactory.getLogger(NAWelcomeHomeHandler.class);

    private int iPersons = -1;
    private int iUnknowns = -1;
    private Optional<NAWelcomeEvent> lastEvent = Optional.empty();
    private boolean isNewLastEvent;
    private @Nullable Integer dataTimeStamp;

    public NAWelcomeHomeHandler(Thing thing) {
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

                Optional<NAWelcomeEvent> previousLastEvent = lastEvent;
                lastEvent = result.getEvents().stream().min(Comparator.comparingInt(NAWelcomeEvent::getTime));
                isNewLastEvent = previousLastEvent.isPresent() && !previousLastEvent.equals(lastEvent);
            }
        }
        return result;
    }

    @Override
    protected State getNAThingProperty(String channelId) {
        switch (channelId) {
            case CHANNEL_WELCOME_HOME_CITY:
                return getPlaceInfo(NAWelcomePlace::getCity);
            case CHANNEL_WELCOME_HOME_COUNTRY:
                return getPlaceInfo(NAWelcomePlace::getCountry);
            case CHANNEL_WELCOME_HOME_TIMEZONE:
                return getPlaceInfo(NAWelcomePlace::getTimezone);
            case CHANNEL_WELCOME_HOME_PERSONCOUNT:
                return iPersons != -1 ? new DecimalType(iPersons) : UnDefType.UNDEF;
            case CHANNEL_WELCOME_HOME_UNKNOWNCOUNT:
                return iUnknowns != -1 ? new DecimalType(iUnknowns) : UnDefType.UNDEF;
            case CHANNEL_WELCOME_EVENT_TYPE:
                return lastEvent.map(e -> toStringType(e.getType())).orElse(UnDefType.UNDEF);
            case CHANNEL_WELCOME_EVENT_TIME:
                return lastEvent.map(e -> toDateTimeType(e.getTime())).orElse(UnDefType.UNDEF);
            case CHANNEL_WELCOME_EVENT_CAMERAID:
                if (lastEvent.isPresent()) {
                    Optional<AbstractNetatmoThingHandler> camera = getBridgeHandler()
                            .findNAThing(lastEvent.get().getCameraId());
                    return camera.map(c -> toStringType(c.getThing().getLabel())).orElse(UnDefType.UNDEF);
                } else {
                    return UnDefType.UNDEF;
                }
            case CHANNEL_WELCOME_EVENT_PERSONID:
                if (lastEvent.isPresent()) {
                    Optional<AbstractNetatmoThingHandler> person = getBridgeHandler()
                            .findNAThing(lastEvent.get().getPersonId());
                    return person.map(p -> toStringType(p.getThing().getLabel())).orElse(UnDefType.UNDEF);
                } else {
                    return UnDefType.UNDEF;
                }
            case CHANNEL_WELCOME_EVENT_SNAPSHOT:
                return findSnapshotURL().map(url -> (State)HttpUtil.downloadImage(url)).orElse(UnDefType.UNDEF);
            case CHANNEL_WELCOME_EVENT_SNAPSHOT_URL:
                return findSnapshotURL().map(ChannelTypeUtils::toStringType).orElse(UnDefType.UNDEF);
            case CHANNEL_WELCOME_EVENT_VIDEO_URL:
                if (lastEvent.isPresent() && lastEvent.get().getVideoId() != null) {
                    String cameraId = lastEvent.get().getCameraId();
                    Optional<AbstractNetatmoThingHandler> thing = getBridgeHandler().findNAThing(cameraId);
                    if (thing.isPresent()) {
                        CameraHandler eventCamera = (CameraHandler) thing.get();
                        String streamUrl = eventCamera.getStreamURL(lastEvent.get().getVideoId());
                        if (streamUrl != null) {
                            return new StringType(streamUrl);
                        }
                    }
                }
                return UnDefType.UNDEF;
            case CHANNEL_WELCOME_EVENT_VIDEOSTATUS:
                return lastEvent.map(e -> toStringType(e.getVideoStatus())).orElse(UnDefType.UNDEF);
            case CHANNEL_WELCOME_EVENT_ISARRIVAL:
                return lastEvent.map(e -> toOnOffType(e.getIsArrival())).orElse(UnDefType.UNDEF);
            case CHANNEL_WELCOME_EVENT_MESSAGE:
                return findEventMessage().map(
                        m -> (State)new StringType(m.replace("<b>", "").replace("</b>", ""))
                ).orElse(UnDefType.UNDEF);
            case CHANNEL_WELCOME_EVENT_SUBTYPE:
                return lastEvent.map(e -> toDecimalType(e.getSubType())).orElse(UnDefType.UNDEF);
        }
        return super.getNAThingProperty(channelId);
    }

    @Override
    protected void triggerChannelIfRequired(String channelId) {
        if (isNewLastEvent) {
            if (CHANNEL_CAMERA_EVENT.equals(channelId)) {
                findDetectedObjectTypes(lastEvent).forEach(detectedType -> triggerChannel(channelId, detectedType));
            }
        }
        super.triggerChannelIfRequired(channelId);
    }

    private static Set<String> findDetectedObjectTypes(Optional<NAWelcomeEvent> eventOptional) {
        Set<String> detectedObjectTypes = new TreeSet<>();
        if (!eventOptional.isPresent()) {
            return detectedObjectTypes;
        }

        NAWelcomeEvent event = eventOptional.get();

        if (event.getPersonId() != null) {
            detectedObjectTypes.add(NAWelcomeSubEvent.TypeEnum.HUMAN.name());
        }

        if (NAWebhookCameraEvent.EventTypeEnum.MOVEMENT.toString().equals(event.getType())) {
            detectedObjectTypes.add(NAWebhookCameraEvent.EventTypeEnum.MOVEMENT.name());
        }

        event.getEventList().forEach(subEvent -> {
            String detectedObjectType = subEvent.getType().name();
            detectedObjectTypes.add(detectedObjectType);
        });
        return detectedObjectTypes;
    }

    private Optional<String> findEventMessage() {
        if (lastEvent.isPresent()) {
            @Nullable String message = lastEvent.get().getMessage();
            if (message != null) {
                return Optional.of(message);
            }

            return findFirstSubEvent(lastEvent).map(NAWelcomeSubEvent::getMessage);
        }
        return Optional.empty();
    }

    /**
     * Returns the Url of the picture
     *
     * @return Url of the picture or null
     */
    protected Optional<String> findSnapshotURL() {
        if (lastEvent.isPresent()) {
            @Nullable NAWelcomeSnapshot snapshot = lastEvent.get().getSnapshot();
            if (snapshot == null) {
                snapshot = findFirstSubEvent(lastEvent).map(NAWelcomeSubEvent::getSnapshot).orElse(null);
            }

            if (snapshot != null && snapshot.getId() != null && snapshot.getKey() != null) {
                return Optional.of(WELCOME_PICTURE_URL + "?" + WELCOME_PICTURE_IMAGEID + "=" + snapshot.getId() + "&"
                        + WELCOME_PICTURE_KEY + "=" + snapshot.getKey());
            } else {
                logger.debug("Unable to build snapshot url for Home : {}", getId());
            }
        }
        return Optional.empty();
    }

    @Override
    protected @Nullable Integer getDataTimestamp() {
        return dataTimeStamp;
    }

    private State getPlaceInfo(Function<NAWelcomePlace, String> infoGetFunction) {
        return Optional.ofNullable(device).map(
                d -> toStringType(infoGetFunction.apply(d.getPlace()))
        ).orElse(UnDefType.UNDEF);
    }

    private static Optional<NAWelcomeSubEvent> findFirstSubEvent(Optional<NAWelcomeEvent> event) {
        return event.map(NAWelcomeEvent::getEventList).flatMap(subEvents -> subEvents.stream().findFirst());
    }
}
