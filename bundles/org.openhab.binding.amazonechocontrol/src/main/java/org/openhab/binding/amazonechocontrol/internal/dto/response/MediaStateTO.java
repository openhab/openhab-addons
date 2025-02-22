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

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

/**
 * The {@link MediaStateTO} encapsulates the state of a media content
 *
 * @author Jan N. Klug - Initial contribution
 */
public class MediaStateTO {

    public String clientId;
    public String contentId;
    public String contentType;
    public String currentState;
    public String imageURL;
    public boolean isDisliked;
    public boolean isLiked;
    public boolean looping;
    public String mediaOwnerCustomerId;
    public boolean muted;
    public String programId;
    public int progressSeconds;
    public String providerId;
    public List<MediaStateQueueEntryTO> queue = List.of();
    public String queueId;
    public int queueSize;
    public int radioVariety;
    public String referenceId;
    public String service;
    public boolean shuffling;
    public int volume;

    @Override
    public @NonNull String toString() {
        return "MediaStateTO{clientId='" + clientId + "', contentId='" + contentId + "', contentType='" + contentType
                + "', currentState='" + currentState + "', imageURL='" + imageURL + "', isDisliked=" + isDisliked
                + ", isLiked=" + isLiked + ", looping=" + looping + ", mediaOwnerCustomerId='" + mediaOwnerCustomerId
                + "', muted=" + muted + ", programId='" + programId + "', progressSeconds=" + progressSeconds
                + ", providerId='" + providerId + "', queue=" + queue + ", queueId='" + queueId + "', queueSize="
                + queueSize + "', radioVariety=" + radioVariety + ", referenceId='" + referenceId + "', service='"
                + service + "', shuffling=" + shuffling + ", volume=" + volume + "}";
    }
}
