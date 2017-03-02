package org.openhab.binding.blueiris.internal.data;

import com.google.gson.annotations.Expose;

/**
 * The config reply with the blue iris camera data.
 *
 * @author David Bennett - Initial Contribution
 */
public class CamConfigReply {
    @Expose
    private String result;
    @Expose
    private Data data;

    public String getResult() {
        return result;
    }

    public Data getData() {
        return data;
    }

    public class Data {
        @Expose
        private boolean motion;
        @Expose
        private boolean schedule;
        @Expose
        private boolean ptzcycle;
        @Expose
        private boolean ptzevents;

        public boolean isMotion() {
            return motion;
        }

        public boolean isSchedule() {
            return schedule;
        }

        public boolean isPtzcycle() {
            return ptzcycle;
        }

        public boolean isPtzevents() {
            return ptzevents;
        }
    }
}
