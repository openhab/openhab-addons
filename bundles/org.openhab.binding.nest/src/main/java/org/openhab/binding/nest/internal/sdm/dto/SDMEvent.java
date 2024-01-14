/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.nest.internal.sdm.dto;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link SDMEvent} is used for mapping the SDM event data received from the SDM API in messages pulled from a
 * Pub/Sub topic.
 *
 * @author Wouter Born - Initial contribution
 *
 * @see <a href="https://developers.google.com/nest/device-access/api/events">
 *      https://developers.google.com/nest/device-access/api/events</a>
 */
public class SDMEvent {

    /**
     * An object that details information about the relation update.
     */
    public static class SDMRelationUpdate {
        public SDMRelationUpdateType type;

        /**
         * The resource that the object now has a relation with.
         */
        public SDMResourceName subject;

        /**
         * The resource that triggered the event.
         */
        public SDMResourceName object;
    }

    public enum SDMRelationUpdateType {
        CREATED,
        DELETED,
        UPDATED
    }

    /**
     * An object that details information about the resource update.
     */
    public static class SDMResourceUpdate {
        public SDMResourceName name;
        public SDMTraits traits;
        public SDMResourceUpdateEvents events;
    }

    public static class SDMDeviceEvent {
        public String eventId;
        public String eventSessionId;
    }

    public static class SDMResourceUpdateEvents extends SDMTraits {
        @SerializedName("sdm.devices.events.CameraMotion.Motion")
        public SDMDeviceEvent cameraMotionEvent;

        @SerializedName("sdm.devices.events.CameraPerson.Person")
        public SDMDeviceEvent cameraPersonEvent;

        @SerializedName("sdm.devices.events.CameraSound.Sound")
        public SDMDeviceEvent cameraSoundEvent;

        @SerializedName("sdm.devices.events.DoorbellChime.Chime")
        public SDMDeviceEvent doorbellChimeEvent;

        public <T> Stream<SDMDeviceEvent> eventStream() {
            return Stream.of(cameraMotionEvent, cameraPersonEvent, cameraSoundEvent, doorbellChimeEvent)
                    .filter(Objects::nonNull);
        }

        public List<SDMDeviceEvent> eventList() {
            return eventStream().collect(Collectors.toList());
        }

        public Set<SDMDeviceEvent> eventSet() {
            return eventStream().collect(Collectors.toSet());
        }
    }

    /**
     * The unique identifier for the event.
     */
    public String eventId;

    /**
     * An object that details information about the relation update.
     */
    public SDMRelationUpdate relationUpdate;

    /**
     * An object that indicates resources that might have similar updates to this event.
     * The resource of the event itself (from the resourceUpdate object) will always be present in this object.
     */
    public List<SDMResourceName> resourceGroup;

    /**
     * An object that details information about the resource update.
     */
    public SDMResourceUpdate resourceUpdate;

    /**
     * The time when the event occurred.
     */
    public ZonedDateTime timestamp;

    /**
     * A unique, obfuscated identifier that represents the user.
     */
    public String userId;
}
