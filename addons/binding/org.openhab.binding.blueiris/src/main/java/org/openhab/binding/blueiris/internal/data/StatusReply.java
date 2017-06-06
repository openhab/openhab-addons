package org.openhab.binding.blueiris.internal.data;

import com.google.gson.annotations.Expose;

public class StatusReply {
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
        private int signal;
        @Expose
        private int profile;
        @Expose
        private int dio;
        @Expose
        private int lock;
        @Expose
        private String clips;
        @Expose
        private int warnings;
        @Expose
        private int alerts;
        @Expose
        private double cpu;
        @Expose
        private int mem;
        @Expose
        private String uptime;
        @Expose
        private String camera;

        public int getSignal() {
            return signal;
        }

        public int getProfile() {
            return profile;
        }

        public int getDio() {
            return dio;
        }

        public int getLock() {
            return lock;
        }

        public String getClips() {
            return clips;
        }

        public int getWarnings() {
            return warnings;
        }

        public int getAlerts() {
            return alerts;
        }

        public double getCpu() {
            return cpu;
        }

        public int getMem() {
            return mem;
        }

        public String getCamera() {
            return camera;
        }
    }
}
