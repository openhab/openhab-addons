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
package org.openhab.binding.amazonechocontrol.internal.jsons;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link JsonMediaState} encapsulate the GSON data of the current media state
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class JsonMediaState {

    public @Nullable String clientId;
    public @Nullable String contentId;
    public @Nullable String contentType;
    public @Nullable String currentState;
    public @Nullable String imageURL;
    public boolean isDisliked;
    public boolean isLiked;
    public boolean looping;
    public @Nullable String mediaOwnerCustomerId;
    public boolean muted;
    public @Nullable String programId;
    public int progressSeconds;
    public @Nullable String providerId;
    public @Nullable List<QueueEntry> queue;
    public @Nullable String queueId;
    public @Nullable Integer queueSize;
    public @Nullable String radioStationId;
    public int radioVariety;
    public @Nullable String referenceId;
    public @Nullable String service;
    public boolean shuffling;
    // public long timeLastShuffled; parsing fails with some values, so do not use it
    public int volume;

    public static class QueueEntry {
        public @Nullable String album;
        public @Nullable String albumAsin;
        public @Nullable String artist;
        public @Nullable String asin;
        public @Nullable String cardImageURL;
        public @Nullable String contentId;
        public @Nullable String contentType;
        public int durationSeconds;
        public boolean feedbackDisabled;
        public @Nullable String historicalId;
        public @Nullable String imageURL;
        public int index;
        public boolean isAd;
        public boolean isDisliked;
        public boolean isFreeWithPrime;
        public boolean isLiked;
        public @Nullable String programId;
        public @Nullable String programName;
        public @Nullable String providerId;
        public @Nullable String queueId;
        public @Nullable String radioStationCallSign;
        public @Nullable String radioStationId;
        public @Nullable String radioStationLocation;
        public @Nullable String radioStationSlogan;
        public @Nullable String referenceId;
        public @Nullable String service;
        public @Nullable String startTime;
        public @Nullable String title;
        public @Nullable String trackId;
        public @Nullable String trackStatus;
    }
}
