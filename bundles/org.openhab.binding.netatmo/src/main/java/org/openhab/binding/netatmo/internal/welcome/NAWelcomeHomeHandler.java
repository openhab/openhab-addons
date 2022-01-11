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

import java.util.Calendar;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.ChannelTypeUtils;
import org.openhab.binding.netatmo.internal.camera.CameraHandler;
import org.openhab.binding.netatmo.internal.handler.AbstractNetatmoThingHandler;
import org.openhab.binding.netatmo.internal.handler.NetatmoDeviceHandler;
import org.openhab.binding.netatmo.internal.webhook.NAWebhookCameraEvent;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.client.model.NAWelcomeEvent;
import io.swagger.client.model.NAWelcomeHome;
import io.swagger.client.model.NAWelcomePlace;
import io.swagger.client.model.NAWelcomeSnapshot;
import io.swagger.client.model.NAWelcomeSubEvent;

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
    private @Nullable NAWelcomeEvent lastEvent;
    private boolean isNewLastEvent;
    private @Nullable Integer dataTimeStamp;

    public NAWelcomeHomeHandler(Thing thing, final TimeZoneProvider timeZoneProvider) {
        super(thing, timeZoneProvider);
    }

    @Override
    protected Optional<NAWelcomeHome> updateReadings() {
        Optional<NAWelcomeHome> result = getBridgeHandler().flatMap(handler -> handler.getWelcomeDataBody(getId()))
                .map(dataBody -> nonNullStream(dataBody.getHomes())
                        .filter(device -> device.getId().equalsIgnoreCase(getId())).findFirst().orElse(null));
        // data time stamp is updated to now as WelcomeDataBody does not provide any information according to this need
        dataTimeStamp = (int) (Calendar.getInstance().getTimeInMillis() / 1000);
        result.ifPresent(home -> {
            nonNullList(home.getCameras()).forEach(camera -> childs.put(camera.getId(), camera));

            // Check how many persons are at home
            iPersons = 0;
            iUnknowns = 0;

            logger.debug("welcome home '{}' calculate Persons at home count", getId());
            nonNullList(home.getPersons()).forEach(person -> {
                iPersons += person.isOutOfSight() ? 0 : 1;
                if (person.getPseudo() != null) {
                    childs.put(person.getId(), person);
                } else {
                    iUnknowns += person.isOutOfSight() ? 0 : 1;
                }
            });

            NAWelcomeEvent previousLastEvent = lastEvent;
            lastEvent = nonNullStream(home.getEvents()).max(Comparator.comparingInt(NAWelcomeEvent::getTime))
                    .orElse(null);
            isNewLastEvent = previousLastEvent != null && !previousLastEvent.equals(lastEvent);
        });
        return result;
    }

    @Override
    protected State getNAThingProperty(String channelId) {
        Optional<NAWelcomeEvent> lastEvt = getLastEvent();
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
                return lastEvt.map(e -> toStringType(e.getType())).orElse(UnDefType.UNDEF);
            case CHANNEL_WELCOME_EVENT_TIME:
                return lastEvt.map(e -> toDateTimeType(e.getTime(), timeZoneProvider.getTimeZone()))
                        .orElse(UnDefType.UNDEF);
            case CHANNEL_WELCOME_EVENT_CAMERAID:
                if (lastEvt.isPresent()) {
                    return findNAThing(lastEvt.get().getCameraId()).map(c -> toStringType(c.getThing().getLabel()))
                            .orElse(UnDefType.UNDEF);
                } else {
                    return UnDefType.UNDEF;
                }
            case CHANNEL_WELCOME_EVENT_PERSONID:
                if (lastEvt.isPresent()) {
                    return findNAThing(lastEvt.get().getPersonId()).map(p -> toStringType(p.getThing().getLabel()))
                            .orElse(UnDefType.UNDEF);
                } else {
                    return UnDefType.UNDEF;
                }
            case CHANNEL_WELCOME_EVENT_SNAPSHOT:
                return findSnapshotURL().map(url -> toRawType(url)).orElse(UnDefType.UNDEF);
            case CHANNEL_WELCOME_EVENT_SNAPSHOT_URL:
                return findSnapshotURL().map(ChannelTypeUtils::toStringType).orElse(UnDefType.UNDEF);
            case CHANNEL_WELCOME_EVENT_VIDEO_URL:
                if (lastEvt.isPresent() && lastEvt.get().getVideoId() != null) {
                    String cameraId = lastEvt.get().getCameraId();
                    Optional<AbstractNetatmoThingHandler> thing = findNAThing(cameraId);
                    if (thing.isPresent()) {
                        CameraHandler eventCamera = (CameraHandler) thing.get();
                        Optional<String> streamUrl = eventCamera.getStreamURL(lastEvt.get().getVideoId());
                        if (streamUrl.isPresent()) {
                            return new StringType(streamUrl.get());
                        }
                    }
                }
                return UnDefType.UNDEF;
            case CHANNEL_WELCOME_EVENT_VIDEOSTATUS:
                return lastEvt.map(e -> e.getVideoId() != null ? toStringType(e.getVideoStatus()) : UnDefType.UNDEF)
                        .orElse(UnDefType.UNDEF);
            case CHANNEL_WELCOME_EVENT_ISARRIVAL:
                return lastEvt.map(e -> toOnOffType(e.isIsArrival())).orElse(UnDefType.UNDEF);
            case CHANNEL_WELCOME_EVENT_MESSAGE:
                return findEventMessage().map(m -> (State) new StringType(m.replace("<b>", "").replace("</b>", "")))
                        .orElse(UnDefType.UNDEF);
            case CHANNEL_WELCOME_EVENT_SUBTYPE:
                return lastEvt.map(e -> toDecimalType(e.getSubType())).orElse(UnDefType.UNDEF);
        }
        return super.getNAThingProperty(channelId);
    }

    @Override
    protected void triggerChannelIfRequired(String channelId) {
        if (isNewLastEvent) {
            if (CHANNEL_CAMERA_EVENT.equals(channelId)) {
                findDetectedObjectTypes(getLastEvent())
                        .forEach(detectedType -> triggerChannel(channelId, detectedType));
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

        if (NAWebhookCameraEvent.EventTypeEnum.MOVEMENT.toString().equals(event.getType())) {
            if (event.getPersonId() != null) {
                detectedObjectTypes.add(NAWelcomeSubEvent.TypeEnum.HUMAN.name());
            } else {
                Optional<NAWelcomeSubEvent.TypeEnum> detectedCategory = findDetectedCategory(event);
                if (detectedCategory.isPresent()) {
                    detectedObjectTypes.add(detectedCategory.get().name());
                } else {
                    detectedObjectTypes.add(NAWebhookCameraEvent.EventTypeEnum.MOVEMENT.name());
                }
            }
        }

        nonNullList(event.getEventList()).forEach(subEvent -> {
            String detectedObjectType = subEvent.getType().name();
            detectedObjectTypes.add(detectedObjectType);
        });
        return detectedObjectTypes;
    }

    private static Optional<NAWelcomeSubEvent.TypeEnum> findDetectedCategory(NAWelcomeEvent event) {
        NAWelcomeEvent.CategoryEnum category = event.getCategory();
        if (category != null) {
            // It is safe to convert the enum, both enums have the same values.
            return Optional.of(NAWelcomeSubEvent.TypeEnum.valueOf(category.name()));
        }
        return Optional.empty();
    }

    private Optional<String> findEventMessage() {
        Optional<NAWelcomeEvent> lastEvt = getLastEvent();
        if (lastEvt.isPresent()) {
            @Nullable
            String message = lastEvt.get().getMessage();
            if (message != null) {
                return Optional.of(message);
            }

            return lastEvt.flatMap(this::findFirstSubEvent).map(NAWelcomeSubEvent::getMessage);
        }
        return Optional.empty();
    }

    /**
     * Returns the Url of the picture
     *
     * @return Url of the picture or null
     */
    protected Optional<String> findSnapshotURL() {
        Optional<NAWelcomeEvent> lastEvt = getLastEvent();
        if (lastEvt.isPresent()) {
            @Nullable
            NAWelcomeSnapshot snapshot = lastEvt.get().getSnapshot();
            if (snapshot == null) {
                snapshot = lastEvt.flatMap(this::findFirstSubEvent).map(NAWelcomeSubEvent::getSnapshot).orElse(null);
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
    protected Optional<Integer> getDataTimestamp() {
        Integer timestamp = dataTimeStamp;
        return timestamp != null ? Optional.of(timestamp) : Optional.empty();
    }

    private State getPlaceInfo(Function<NAWelcomePlace, String> infoGetFunction) {
        return getDevice().map(d -> toStringType(infoGetFunction.apply(d.getPlace()))).orElse(UnDefType.UNDEF);
    }

    private Optional<NAWelcomeSubEvent> findFirstSubEvent(NAWelcomeEvent event) {
        return Optional.ofNullable(event).map(NAWelcomeEvent::getEventList)
                .flatMap(subEvents -> nonNullStream(subEvents).findFirst());
    }

    private Optional<NAWelcomeEvent> getLastEvent() {
        NAWelcomeEvent evt = lastEvent;
        return evt != null ? Optional.of(evt) : Optional.empty();
    }
}
