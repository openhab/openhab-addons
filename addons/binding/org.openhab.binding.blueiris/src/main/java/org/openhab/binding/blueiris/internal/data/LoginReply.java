package org.openhab.binding.blueiris.internal.data;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LoginReply {
    @Expose
    private String result;
    @Expose
    private String session;
    @Expose
    Data data;

    public String getResult() {
        return result;
    }

    public String getSession() {
        return session;
    }

    public Data getData() {
        return data;
    }

    @Override
    public String toString() {
        return "LoginReply [result=" + result + ", session=" + session + ", data=" + data + "]";
    }

    public class Data {
        @Expose
        @SerializedName("system name")
        private String systemName;
        @Expose
        private boolean admin;
        @Expose
        private boolean streamtimelimit;
        @Expose
        private boolean dio;
        @Expose
        String version;
        @Expose
        Double latitude;
        @Expose
        Double longitude;
        @Expose
        List<String> streams;
        @Expose
        List<String> sounds;
        @Expose
        List<String> profiles;
        @Expose
        List<String> schedules;

        public String getSystemName() {
            return systemName;
        }

        public boolean isAdmin() {
            return admin;
        }

        public boolean isStreamtimelimit() {
            return streamtimelimit;
        }

        public boolean isDio() {
            return dio;
        }

        public String getVersion() {
            return version;
        }

        public Double getLatitude() {
            return latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public List<String> getStreams() {
            return streams;
        }

        public List<String> getSounds() {
            return sounds;
        }

        public List<String> getProfiles() {
            return profiles;
        }

        public List<String> getSchedules() {
            return schedules;
        }

        @Override
        public String toString() {
            return "Data [systemName=" + systemName + ", admin=" + admin + ", streamtimelimit=" + streamtimelimit
                    + ", dio=" + dio + ", version=" + version + ", latitude=" + latitude + ", longitude=" + longitude
                    + ", streams=" + streams + ", sounds=" + sounds + ", profiles=" + profiles + ", schedules="
                    + schedules + "]";
        }
    }
}