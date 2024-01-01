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
package org.openhab.binding.netatmo.internal.api.dto;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.ApiResponse;
import org.openhab.binding.netatmo.internal.api.BodyResponse;
import org.openhab.binding.netatmo.internal.api.data.EventSubType;
import org.openhab.binding.netatmo.internal.api.data.EventType;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.EventCategory;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.VideoStatus;

/**
 * The {@link HomeEvent} holds information transferred by the webhook about a home event.
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class HomeEvent extends Event {
    public class NAEventsDataResponse extends ApiResponse<BodyResponse<Home>> {
    }

    private ZonedDateTime time = ZonedDateTime.now();
    private @Nullable String personId;
    private EventCategory category = EventCategory.UNKNOWN;
    private @Nullable Snapshot snapshot;
    private @Nullable Snapshot vignette;
    private @Nullable String videoId;
    private VideoStatus videoStatus = VideoStatus.UNKNOWN;
    private boolean isArrival;
    private List<HomeEvent> subevents = List.of();

    @Override
    public ZonedDateTime getTime() {
        return time;
    }

    @Override
    public @Nullable String getPersonId() {
        return personId;
    }

    public @Nullable String getVideoId() {
        return videoId;
    }

    public VideoStatus getVideoStatus() {
        return videoStatus;
    }

    @Override
    public Optional<EventSubType> getSubTypeDescription() {
        // Blend extra information provided by this kind of event in subcategories...
        if (type == EventType.PERSON) {
            subType = isArrival ? EventSubType.PERSON_ARRIVAL.subType : EventSubType.PERSON_SEEN.subType;
        } else if (type == EventType.PERSON_HOME) {
            subType = EventSubType.PERSON_ARRIVAL.subType;
        } else if (type == EventType.PERSON_AWAY) {
            subType = EventSubType.PERSON_DEPARTURE.subType;
        } else if (type == EventType.HUMAN) {
            subType = EventSubType.MOVEMENT_HUMAN.subType;
        } else if (type == EventType.ANIMAL) {
            subType = EventSubType.MOVEMENT_ANIMAL.subType;
        } else {
            if (category == EventCategory.ANIMAL) {
                subType = EventSubType.MOVEMENT_ANIMAL.subType;
            } else if (category == EventCategory.HUMAN) {
                subType = EventSubType.MOVEMENT_HUMAN.subType;
            } else if (category == EventCategory.VEHICLE) {
                subType = EventSubType.MOVEMENT_VEHICLE.subType;
            }
        }
        // ... and let ancestor do his work
        return super.getSubTypeDescription();
    }

    @Override
    public @Nullable String getSnapshotUrl() {
        Snapshot image = snapshot;
        return image != null ? image.getUrl() : null;
    }

    public @Nullable String getVignetteUrl() {
        Snapshot image = vignette;
        return image != null ? image.getUrl() : null;
    }

    public List<HomeEvent> getSubevents() {
        return subevents;
    }

    public @Nullable Snapshot getVignette() {
        return vignette;
    }
}
