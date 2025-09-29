/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.unifiaccess.internal.dto;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

/**
 * Notification envelope for UniFi Access WebSocket stream.
 * 
 * @author Dan Cunningham - Initial contribution
 */
public class Notification {
    public String event;
    public String receiverId;
    public String eventObjectId;
    public Boolean saveToHistory;
    public JsonElement data;

    /**
     * access.remote_view data payload.
     * When a doorbell rings
     */
    public static class RemoteViewData {
        public String channel;
        public String token;
        public String deviceId;
        public String deviceType;
        public String deviceName;
        public String doorName;
        public String controllerId;
        public String floorName;
        public String requestId;
        public String clearRequestId;
        public String inOrOut;
        public long createTime;
        public int reasonCode;
        public List<String> doorGuardIds;
        public String connectedUahId;
        public String roomId;
        public String hostDeviceMac;
    }

    /**
     * access.remote_view.change data payload.
     * Doorbell status change
     */
    public static class RemoteViewChangeData {
        public Reason reason;
        public String remoteCallRequestId;

        /** Possible values for {@code reason_code} in access.remote_view.change events. */
        @JsonAdapter(Reason.Adapter.class)
        public enum Reason {
            DOORBELL_TIMED_OUT(105, "Doorbell timed out."),
            ADMIN_REJECTED_UNLOCK(106, "An admin rejected to unlock a door."),
            ADMIN_UNLOCK_SUCCEEDED(107, "An admin successfully unlocked a door."),
            VISITOR_CANCELED_DOORBELL(108, "A visitor canceled a doorbell."),
            ANSWERED_BY_ANOTHER_ADMIN(400, "Doorbell was answered by another admin."),
            UNKNOWN(-1, "Unknown");

            private final int code;
            private final String description;

            Reason(int code, String description) {
                this.code = code;
                this.description = description;
            }

            public int code() {
                return code;
            }

            public String description() {
                return description;
            }

            public static Reason fromCode(int code) {
                for (Reason value : values()) {
                    if (value.code == code) {
                        return value;
                    }
                }
                return UNKNOWN;
            }

            public static final class Adapter implements JsonDeserializer<Reason>, JsonSerializer<Reason> {
                @Override
                public Reason deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                        throws JsonParseException {
                    try {
                        int code = json.getAsInt();
                        return Reason.fromCode(code);
                    } catch (Exception e) {
                        return Reason.UNKNOWN;
                    }
                }

                @Override
                public JsonElement serialize(Reason src, Type typeOfSrc, JsonSerializationContext context) {
                    return new JsonPrimitive(src.code());
                }
            }
        }
    }

    /**
     * access.data.device.remote_unlock data payload.
     * Remote door unlock by admin
     */
    public static class RemoteUnlockData {
        public String uniqueId;
        public String name;
        public String upId;
        public String timezone;
        public String locationType;
        public String extraType;
        public String fullName;
        public int level;
        public String workTime;
        public String workTimeId;
        public Map<String, Object> extras;
    }

    public <T> @Nullable T dataAs(Gson gson, Class<T> type) {
        if (data == null || !data.isJsonObject()) {
            return null;
        }
        return gson.fromJson(data.getAsJsonObject(), type);
    }

    public RemoteViewData dataAsRemoteView(Gson gson) {
        return dataAs(gson, RemoteViewData.class);
    }

    public RemoteViewChangeData dataAsRemoteViewChange(Gson gson) {
        return dataAs(gson, RemoteViewChangeData.class);
    }

    public RemoteUnlockData dataAsRemoteUnlock(Gson gson) {
        return dataAs(gson, RemoteUnlockData.class);
    }

    /** access.data.device.update data payload (covers all observed fields). */
    public static class DeviceUpdateData {
        public String uniqueId;
        public String name;
        public String alias;
        public String deviceType;
        public String connectedUahId;
        public String locationId;
        public String firmware;
        public String version;
        public String ip;
        public String mac;
        public String hwType;
        public long startTime;
        public long lastSeen;
        public boolean securityCheck;
        public String source;
        public String bomRev;
        public String guid;
        public boolean needAdvisory;
        public boolean isAdopted;
        public boolean isManaged;
        public boolean isConnected;
        public boolean isOnline;
        public boolean isRebooting;
        public boolean isUnavailable;
        public boolean adopting;
        public Location location;
        public Location door;
        public Floor floor;
        public List<ConfigEntry> configEntries;
        public List<String> capabilities;
        public String resourceName;
        public String displayModel;
        public String revision;
        public long revisionUpdateTime;
        public long versionUpdateTime;
        public long firmwareUpdateTime;
        public long adoptTime;
        public JsonElement update;
        public UpdateManual updateManual;
        public List<Extension> extensions;
        public String model;
        public Images images;
        public String provisionTime;
        public String provisionPercent;
        public JsonElement template;

        public static class Location {
            public String uniqueId;
            public String name;
            public String upId;
            public String timezone;
            public String locationType;
            public String extraType;
            public String fullName;
            public int level;
            public String workTime;
            public String workTimeId;
            public JsonObject extras;
            public List<String> previousName;
        }

        public static class Floor {
            public String uniqueId;
            public String name;
            public String upId;
            public String timezone;
            public String locationType;
            public String extraType;
            public String fullName;
            public int level;
            public String workTime;
            public String workTimeId;
            public JsonObject extras;
            public JsonElement previousName;
        }

        public static class ConfigEntry {
            public String deviceId;
            public String key;
            public String value;
            public String tag;
            public String updateTime;
            public String createTime;
        }

        public static class UpdateManual {
            public DeviceVersionUpgradeStatus dvus;
            public String fromVersion;
            public JsonElement lastUpgradeStartTime;
            public JsonElement lastUpgradeSuccess;
            public String lastUpgradeFailureReason;
            public JsonElement lastDownloadStartTime;
            public JsonElement lastDownloadSuccess;
            public String lastDownloadFailureReason;
            public boolean downloaded;

            public static class DeviceVersionUpgradeStatus {
                public boolean completed;
                public boolean isWaiting;
                public boolean isPreparing;
                public boolean isUpgrading;
                public int upgradeSeconds;
                public boolean timedOut;
                public boolean failed;
                public String failureReason;
                public boolean isDownloading;
            }
        }

        public static class Extension {
            public String uniqueId;
            public String deviceId;
            public String extensionName;
            public String sourceId;
            public String targetType;
            public String targetValue;
            public String targetName;
            public List<TargetConfig> targetConfig;

            public static class TargetConfig {
                public String configTag;
                public String configKey;
                public JsonElement configValue;
            }
        }

        public static class Images {
            public String xs;
            public String s;
            public String m;
            public String l;
            public String xl;
            public String xxl;
            public String base;
        }
    }

    public static class LocationState {
        public String locationId;
        public DoorState.LockState lock;
        public DoorState.DoorPosition dps;
        public Boolean dpsConnected;
        public RemainUnlock remainUnlock;
        public List<Alarm> alarms;
        public Emergency emergency;
        public Boolean isUnavailable;
    }

    public static class Alarm {
        public String type;
    }

    public static class Emergency {
        public String software;
        public String hardware;
    }

    public static class RemainUnlock {
        public DoorState.LockState state;
        public Long until;
        public DoorState.DoorLockRuleType type;
    }

    /** access.data.v2.device.update payload (partial). */
    public static class DeviceUpdateV2Data {
        public String name;
        public String alias;
        public String id;
        public String ip;
        public String mac;
        public Boolean online;
        public Boolean adopting;
        public String deviceType;
        public String connectedHubId;
        public String locationId;
        public String firmware;
        public String version;
        public String guid;
        public Long startTime;
        public String hwType;
        public String revision;
        public JsonElement cap;
        public List<LocationState> locationStates; // is this ever present?
        public List<String> category;
    }

    /** access.data.v2.location.update payload (partial). */
    public static class LocationUpdateV2Data {
        public String id;
        public String locationType;
        public String name;
        public String upId;
        public JsonElement extras;
        public List<String> deviceIds;
        public LocationState state;
        public Thumbnail thumbnail;
        public Long lastActivity;

        public static class Thumbnail {
            public String type;
            public String url;
            public Long doorThumbnailLastUpdate;
        }
    }

    /**
     * Reusable simple references used across log payloads.
     */
    public static class Actor {
        public String id;
        public String type;
        public String displayName;
        public String firstName;
        public String lastName;
    }

    // this is use to point to a base access object like a door, floor, camera, config
    public static class BaseReference {
        public String id;
        public String type;
        public String displayName;
    }

    public static class CameraCaptureReference extends BaseReference {
        public String alternateId; // this is the camera ID used in the Protect binding !
        public String alternateName; // strange, actually a URL to a tiny thumbnail of the product picture ?
        public String videoUrl; // relative path to the video using the Unifi Protect App
        public String videoFileName; // interesting, this a reference to the recorded clip ?
        public String videoSource; // protect
        public String thumbnailUrl; // relative path to the thumbnail using the Unifi Protect App
    }

    /** access.logs.insights.add payload (partial). */
    public static class LogsInsightsAddData {
        public String logKey;
        public String eventType; // access.door.unlock....
        public String message;
        public Long published; // epoch millis
        public String result; // ACCESS / DENY / etc
        public JsonObject metadata;
    }

    /** access.logs.insights.add payload (typed). */
    public static class InsightLogsAddData {
        public String logKey; // dashboard.access.door.unlock.success
        public String eventType; // access.door.unlock
        public String message; // human-readable message
        public Long published; // epoch millis
        public String result; // ACCESS / DENY / etc
        public Metadata metadata;

        public static class Metadata {
            public Actor actor;
            public Authentication authentication;
            public BaseReference building;
            public CameraCaptureReference cameraCapture;
            public BaseReference device;
            public BaseReference deviceConfig;
            public BaseReference deviceUaHub;
            public BaseReference door;
            @SerializedName("fo")
            public BaseReference floor;
            public BaseReference openedDirection;
            public BaseReference openedMethod;
            public BaseReference userStatus;
        }

        public static class Authentication {
            public String id;
            public String type;
            public String displayName;
            public String credentialProvider; // REMOTE_THROUGH_UAH, TOUCH_PASS, NFC, PIN_CODE, etc...
        }
    }

    /** access.logs.add payload (partial). */
    public static class LogsAddData {
        @SerializedName("_id")
        public String id;

        @SerializedName("@timestamp")
        public String timestamp;

        @SerializedName("_source")
        public Source source;

        /** e.g. "access" */
        public String tag;

        public static class Source {
            public Actor actor;
            public Event event;
            public Authentication authentication;
            public List<Target> target;
        }

        public static class Event {
            public String type; // access.door.unlock
            public String displayMessage;
            public String result;
            public Long published; // epoch millis
            public String logKey; // dashboard.access.door.unlock.success
            public String logCategory; // Unlocks...
        }

        public static class Authentication {
            public String credentialProvider; // REMOTE_THROUGH_UAH, TOUCH_PASS, NFC, PIN_CODE, etc...
        }

        public static class Target extends BaseReference {
            public String alternateId;
            public String alternateName;
        }
    }

    /** access.base.info payload (partial). */
    public static class BaseInfoData {
        public Integer topLogCount;
    }

    public static class DoorBellData {
        public String deviceId;
        public String doorId;
        public String requestId;
    }

    public DeviceUpdateData dataAsDeviceUpdate(Gson gson) {
        return dataAs(gson, DeviceUpdateData.class);
    }

    public DeviceUpdateV2Data dataAsDeviceUpdateV2(Gson gson) {
        return dataAs(gson, DeviceUpdateV2Data.class);
    }

    public LocationUpdateV2Data dataAsLocationUpdateV2(Gson gson) {
        return dataAs(gson, LocationUpdateV2Data.class);
    }

    public LogsInsightsAddData dataAsInsightsAddData(Gson gson) {
        return dataAs(gson, LogsInsightsAddData.class);
    }

    public LogsAddData dataAsLogsAdd(Gson gson) {
        return dataAs(gson, LogsAddData.class);
    }

    public InsightLogsAddData dataAsInsightLogsAdd(Gson gson) {
        return dataAs(gson, InsightLogsAddData.class);
    }

    public BaseInfoData dataAsBaseInfo(Gson gson) {
        return dataAs(gson, BaseInfoData.class);
    }

    public DoorBellData dataAsDoorBell(Gson gson) {
        return dataAs(gson, DoorBellData.class);
    }
}
