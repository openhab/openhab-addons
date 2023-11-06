/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link JsonPlayerState} encapsulate the GSON data of the player state
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class JsonPlayerState {
    public @Nullable PlayerInfo playerInfo;

    public static class PlayerInfo {
        public @Nullable String state;
        public @Nullable InfoText infoText;
        public @Nullable InfoText miniInfoText;
        public @Nullable Provider provider;
        public @Nullable Volume volume;
        public @Nullable MainArt mainArt;

        public @Nullable String queueId;
        public @Nullable String mediaId;

        public @Nullable Progress progress;

        public static class InfoText {
            public boolean multiLineMode;
            public @Nullable String subText1;
            public @Nullable String subText2;
            public @Nullable String title;
        }

        public static class Provider {
            public @Nullable String providerDisplayName;
            public @Nullable String providerName;
        }

        public static class Volume {
            public boolean muted;
            public int volume;
        }

        public static class MainArt {
            public @Nullable String altText;
            public @Nullable String artType;
            public @Nullable String contentType;
            public @Nullable String url;
        }

        public static class Progress {
            public @Nullable Boolean allowScrubbing;
            public @Nullable Object locationInfo;
            public @Nullable Long mediaLength;
            public @Nullable Long mediaProgress;
            public @Nullable Boolean showTiming;
            public @Nullable Boolean visible;
        }
    }
}
