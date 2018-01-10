/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.amazonechocontrol.internal.jsons;

import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link JsonPlayerState} encapsulate the GSON data of the player state
 *
 * @author Michael Geramb - Initial contribution
 */
public class JsonPlayerState {
    public PlayerInfo playerInfo;

    public class PlayerInfo {
        public String state;
        public @Nullable InfoText infoText;
        public @Nullable InfoText miniInfoText;
        public @Nullable Provider provider;
        public @Nullable Volume volume;
        public @Nullable MainArt mainArt;

        public String queueId;
        public String mediaId;

        public class InfoText {
            public boolean multiLineMode;
            public String subText1;
            public String subText2;
            public String title;

        }

        public class Provider {
            public String providerDisplayName;
            public String providerName;
        }

        public class Volume {
            public boolean muted;
            public int volume;
        }

        public class MainArt {
            public String altText;
            public String artType;
            public String contentType;
            public String url;
        }

    }

}
