package org.openhab.binding.blueiris.internal.data;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * cam list data from blue iris.
 *
 * @author David Bennett - Initial COntribution
 *
 */
public class CamListReply {

    private List<Data> cameras;

    /**
     * Create the cam list reply information
     *
     * @param cameras The cameras as a reply
     */
    public CamListReply(Data[] cameras) {
        this.cameras = Lists.newArrayList(cameras);
    }

    /**
     * @return The details about the cameras.
     */
    public List<Data> getCameras() {
        return cameras;
    }

    @Override
    public String toString() {
        return "CamListReply [cameras=" + cameras + "]";
    }

    /**
     * Data in the array from the cam list reply.
     */
    public static class Data {
        private String optionsDisplay;
        private String optionsValue;
        private Integer FPS;
        private Integer color;
        private Integer clipsCreated;
        private boolean isAlerting;
        private boolean isEnabled;
        private boolean isOnline;
        private boolean isMotion;
        private boolean isNoSignal;
        private boolean isPaused;
        private boolean isTriggered;
        private boolean isRecording;
        private boolean isYellow;
        private String profile;
        private boolean ptz;
        private boolean audio;
        private Integer width;
        private Integer height;
        private Integer nTriggers;
        private Integer nNoSignal;
        private Integer nClips;

        public String getOptionsDisplay() {
            return optionsDisplay;
        }

        public String getOptionsValue() {
            return optionsValue;
        }

        public Integer getFPS() {
            return FPS;
        }

        public Integer getColor() {
            return color;
        }

        public Integer getClipsCreated() {
            return clipsCreated;
        }

        public boolean isAlerting() {
            return isAlerting;
        }

        public boolean isEnabled() {
            return isEnabled;
        }

        public boolean isOnline() {
            return isOnline;
        }

        public boolean isMotion() {
            return isMotion;
        }

        public boolean isNoSignal() {
            return isNoSignal;
        }

        public boolean isPaused() {
            return isPaused;
        }

        public boolean isTriggered() {
            return isTriggered;
        }

        public boolean isRecording() {
            return isRecording;
        }

        public boolean isYellow() {
            return isYellow;
        }

        public String getProfile() {
            return profile;
        }

        public boolean isPtz() {
            return ptz;
        }

        public boolean isAudio() {
            return audio;
        }

        public Integer getWidth() {
            return width;
        }

        public Integer getHeight() {
            return height;
        }

        public Integer getnTriggers() {
            return nTriggers;
        }

        public Integer getnNoSignal() {
            return nNoSignal;
        }

        public Integer getnClips() {
            return nClips;
        }

        @Override
        public String toString() {
            return "Data [optionsDisplay=" + optionsDisplay + ", optionsValue=" + optionsValue + ", FPS=" + FPS
                    + ", color=" + color + ", clipsCreated=" + clipsCreated + ", isAlerting=" + isAlerting
                    + ", isEnabled=" + isEnabled + ", isOnline=" + isOnline + ", isMotion=" + isMotion + ", isNoSignal="
                    + isNoSignal + ", isPaused=" + isPaused + ", isTriggered=" + isTriggered + ", isRecording="
                    + isRecording + ", isYellow=" + isYellow + ", profile=" + profile + ", ptz=" + ptz + ", audio="
                    + audio + ", width=" + width + ", height=" + height + ", nTriggers=" + nTriggers + ", nNoSignal="
                    + nNoSignal + ", nClips=" + nClips + "]";
        }
    }
}
