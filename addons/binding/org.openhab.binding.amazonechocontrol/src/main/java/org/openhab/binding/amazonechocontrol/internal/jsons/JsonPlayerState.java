package org.openhab.binding.amazonechocontrol.internal.jsons;

import org.eclipse.jdt.annotation.Nullable;

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
