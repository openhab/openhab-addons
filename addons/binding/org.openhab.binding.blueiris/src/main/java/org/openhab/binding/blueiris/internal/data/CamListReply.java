package org.openhab.binding.blueiris.internal.data;

import java.util.List;

import com.google.gson.annotations.Expose;

/**
 * cam list data from blue iris.
 *
 * @author David Bennett - Initial COntribution
 *
 */
public class CamListReply {

    @Expose
    private String result;
    @Expose
    private List<Data> data;

    /**
     * Create the cam list reply information
     */
    public CamListReply() {
    }

    /**
     * @return The details about the cameras.
     */
    public List<Data> getCameras() {
        return data;
    }

    @Override
    public String toString() {
        return "CamListReply [result=" + result + ", data=" + data + "]";
    }

    /**
     * Data in the array from the cam list reply.
     */
    public static class Data {
        @Expose
        private String optionDisplay;
        @Expose
        private String optionValue;
        @Expose
        private Double FPS;
        @Expose
        private Integer color;
        @Expose
        private Integer clipsCreated;
        @Expose
        private boolean webcast;
        @Expose
        private boolean isAlerting;
        @Expose
        private boolean isEnabled;
        @Expose
        private boolean isOnline;
        @Expose
        private boolean isMotion;
        @Expose
        private boolean isNoSignal;
        @Expose
        private boolean isPaused;
        @Expose
        private boolean isTriggered;
        @Expose
        private boolean isRecording;
        @Expose
        private boolean isYellow;
        @Expose
        private String profile;
        @Expose
        private boolean ptz;
        @Expose
        private boolean audio;
        @Expose
        private Integer width;
        @Expose
        private Integer height;
        @Expose
        private Integer nTriggers;
        @Expose
        private Integer nNoSignal;
        @Expose
        private Integer nClips;
        @Expose
        private Integer pause;
        @Expose
        private Integer nAlerts;
        @Expose
        private Integer newalerts;
        @Expose
        private Integer alertutc;

        public String getOptionDisplay() {
            return optionDisplay;
        }

        public String getOptionValue() {
            return optionValue;
        }

        public Double getFPS() {
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

        public Integer getNumTriggers() {
            return nTriggers;
        }

        public Integer getNumNoSignal() {
            return nNoSignal;
        }

        public Integer getNumClips() {
            return nClips;
        }

        public boolean isWebcast() {
            return webcast;
        }

        public Integer getPause() {
            return pause;
        }

        public Integer getNumAlerts() {
            return nAlerts;
        }

        public Integer getNewAlerts() {
            return newalerts;
        }

        public Integer getAlertUTC() {
            return alertutc;
        }

        @Override
        public String toString() {
            return "Data [optionDisplay=" + optionDisplay + ", optionValue=" + optionValue + ", FPS=" + FPS + ", color="
                    + color + ", clipsCreated=" + clipsCreated + ", webcast=" + webcast + ", isAlerting=" + isAlerting
                    + ", isEnabled=" + isEnabled + ", isOnline=" + isOnline + ", isMotion=" + isMotion + ", isNoSignal="
                    + isNoSignal + ", isPaused=" + isPaused + ", isTriggered=" + isTriggered + ", isRecording="
                    + isRecording + ", isYellow=" + isYellow + ", profile=" + profile + ", ptz=" + ptz + ", audio="
                    + audio + ", width=" + width + ", height=" + height + ", nTriggers=" + nTriggers + ", nNoSignal="
                    + nNoSignal + ", nClips=" + nClips + ", pause=" + pause + ", nAlerts=" + nAlerts + ", newalerts="
                    + newalerts + ", alertutc=" + alertutc + "]";
        }
    }
}
