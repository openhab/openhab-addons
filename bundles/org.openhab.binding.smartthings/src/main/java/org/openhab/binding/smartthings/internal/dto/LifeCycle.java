package org.openhab.binding.smartthings.internal.dto;

public class LifeCycle {
    public String lifecycle;
    public String excutionId;
    public String appId;
    public String locale;
    public String version;

    public record confirmationData(String appId, String confirmationUrl) {
    }

    public confirmationData confirmationData;

    public record client(String os, String version, String language, String displayMode, String timeZoneOffset,
            String samsungAccountId, String mobileDeviceId) {
    }

    public client client;

    public record configurationData(String installedAppId, String phase, String pageId, String previousPageId) {
    }

    public configurationData configurationData;

    public Data updateData;
    public Data executeData;
    public Data eventData;

    public class Data {
        public String authToken;
        public String refreshToken;

        public class InstalledApp {
            public String installedAppId;
            public String locationId;
            public String[] permissions;
            public String[] previousPermissions;
        }

        public class Event {
            public String eventTime;
            public String evnetType;

            public class DeviceEvent {
                public String eventId;
                public String locationId;
                public String ownerId;
                public String ownerType;
                public String deviceId;
                public String componentId;
                public String capability;
                public String attribute;
                public String value;
                public String valueType;
                public Boolean stateChange;
                public String subscriptionName;
            }

            public DeviceEvent deviceEvent;
        }

        public InstalledApp installedApp;
        public Event[] events;
    }
}
