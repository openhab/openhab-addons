/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.EventSubType;
import org.openhab.binding.netatmo.internal.api.EventType;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.EventCategory;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.VideoStatus;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class NAHomeEvent extends NAEvent {
    private long time;
    private @Nullable String personId;
    private @Nullable EventCategory category;
    private @Nullable NASnapshot snapshot;
    private @Nullable String videoId;
    private @Nullable VideoStatus videoStatus;
    private boolean isArrival;

    @Override
    public long getTime() {
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
        VideoStatus status = videoStatus;
        return status != null ? status : VideoStatus.UNKNOWN;
    }

    @Override
    public Optional<EventSubType> getSubTypeDescription() {
        // Blend extra informations provided by this kind of event in subcategories...
        if (isArrival && type == EventType.PERSON) {
            this.subType = EventSubType.ARRIVAL.getSubType();
        } else {
            EventCategory localCategory = category;
            if (localCategory != null) {
                switch (localCategory) {
                    case ANIMAL:
                        this.subType = EventSubType.ANIMAL.getSubType();
                        break;
                    case HUMAN:
                        this.subType = EventSubType.HUMAN.getSubType();
                        break;
                    case VEHICLE:
                        this.subType = EventSubType.VEHICLE.getSubType();
                        break;
                }
            }
        }
        // ... and let ancestor do his work
        return super.getSubTypeDescription();
    }

    @Override
    public @Nullable NASnapshot getSnapshot() {
        return this.snapshot;
    }

    public void setTime(int eventTime) {
        this.time = eventTime;
    }
}
