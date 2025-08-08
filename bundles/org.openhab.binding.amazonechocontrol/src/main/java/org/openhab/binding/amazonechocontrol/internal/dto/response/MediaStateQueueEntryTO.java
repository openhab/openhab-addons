/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.amazonechocontrol.internal.dto.response;

import org.eclipse.jdt.annotation.NonNull;

/**
 * The {@link MediaStateQueueEntryTO} encapsulates a queue entry in media content state
 *
 * @author Jan N. Klug - Initial contribution
 */
public class MediaStateQueueEntryTO {
    public String album;
    public String albumAsin;
    public String artist;
    public String asin;
    public String cardImageURL;
    public String contentId;
    public String contentType;
    public int durationSeconds;
    public boolean feedbackDisabled;
    public String historicalId;
    public String imageURL;
    public int index;
    public boolean isAd;
    public boolean isDisliked;
    public boolean isFreeWithPrime;
    public boolean isLiked;
    public String programId;
    public String programName;
    public String providerId;
    public String queueId;
    public String radioStationCallSign;
    public String radioStationId;
    public String radioStationLocation;
    public String radioStationSlogan;
    public String referenceId;
    public String service;
    public String startTime;
    public String title;
    public String trackId;
    public String trackStatus;

    @Override
    public @NonNull String toString() {
        return "MediaStateQueueEntryTO{album='" + album + "', albumAsin='" + albumAsin + "', artist='" + artist
                + "', asin='" + asin + "', cardImageURL='" + cardImageURL + "', contentId='" + contentId
                + "', contentType='" + contentType + "', durationSeconds=" + durationSeconds + ", feedbackDisabled="
                + feedbackDisabled + ", historicalId='" + historicalId + "', imageURL='" + imageURL + "', index="
                + index + ", isAd=" + isAd + ", isDisliked=" + isDisliked + ", isFreeWithPrime=" + isFreeWithPrime
                + ", isLiked=" + isLiked + ", programId='" + programId + "', programName='" + programName
                + "', providerId='" + providerId + "', queueId='" + queueId + "', radioStationCallSign='"
                + radioStationCallSign + "', radioStationId='" + radioStationId + "', radioStationLocation='"
                + radioStationLocation + "', radioStationSlogan='" + radioStationSlogan + "', referenceId='"
                + referenceId + "', service='" + service + "', startTime='" + startTime + "', title='" + title
                + "', trackId='" + trackId + "', trackStatus='" + trackStatus + "'}";
    }
}
