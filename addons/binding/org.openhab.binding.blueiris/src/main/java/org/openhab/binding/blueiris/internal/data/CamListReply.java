package org.openhab.binding.blueiris.internal.data;

/**
 * cam list data from blue iris.
 *
 * @author David Bennett - Initial COntribution
 *
 */
public class CamListReply {
    public static class Data {
        private String optionsDisplay;
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

    }
}
