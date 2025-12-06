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
package org.openhab.binding.linkplay.internal.client.http.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Track metadata.
 * 
 * @author Dan Cunningham - Initial contribution
 */
public class TrackMetadata {

    @SerializedName("metaData")
    public MetaData metaData;

    public static class MetaData {
        public String album;
        public String title;
        public String subtitle;
        public String artist;

        @SerializedName("albumArtURI")
        public String albumArtUri;

        public String sampleRate;
        public String bitDepth;
        public String bitRate;
        public String trackId;

        @Override
        public String toString() {
            return "MetaData{" + "album='" + album + '\'' + ", title='" + title + '\'' + ", subtitle='" + subtitle
                    + '\'' + ", artist='" + artist + '\'' + ", albumArtUri='" + albumArtUri + '\'' + ", sampleRate='"
                    + sampleRate + '\'' + ", bitDepth='" + bitDepth + '\'' + ", bitRate='" + bitRate + '\''
                    + ", trackId='" + trackId + '\'' + '}';
        }
    }

    @Override
    public String toString() {
        return "TrackMetadata{" + "metaData=" + metaData + '}';
    }
}
