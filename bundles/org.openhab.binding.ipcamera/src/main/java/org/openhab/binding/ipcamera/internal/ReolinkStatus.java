/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.ipcamera.internal;

import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ipcamera.internal.handler.IpCameraHandler;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * The {@link ReolinkStatus} reads status values of Reolink cameras via the HTTP API: connection and sleep state
 * (NVR / Home Hub), battery, storage and speaker volume. The values are read from the camera or hub without waking up
 * battery powered cameras and are only polled in a long interval.
 *
 * @author Gerhard Braun - Initial contribution
 */
@NonNullByDefault
public class ReolinkStatus {
    /** Interval of pollCameraRunnable in seconds. */
    public static final int POLL_CYCLE_SECONDS = 8;
    /** Battery, storage and volume are polled roughly every 5 minutes. */
    public static final int POLL_EVERY_CYCLES = 38;

    private static final Map<String, ChannelTypeUID> CHANNEL_TYPES = Map.of( //
            CHANNEL_CAMERA_ONLINE, new ChannelTypeUID(BINDING_ID, CHANNEL_CAMERA_ONLINE), //
            CHANNEL_CAMERA_SLEEPING, new ChannelTypeUID(BINDING_ID, CHANNEL_CAMERA_SLEEPING), //
            CHANNEL_BATTERY_LEVEL, new ChannelTypeUID("system", "battery-level"), //
            CHANNEL_BATTERY_LOW, new ChannelTypeUID("system", "low-battery"), //
            CHANNEL_BATTERY_CHARGING, new ChannelTypeUID(BINDING_ID, CHANNEL_BATTERY_CHARGING), //
            CHANNEL_STORAGE_USAGE, new ChannelTypeUID(BINDING_ID, CHANNEL_STORAGE_USAGE), //
            CHANNEL_STORAGE_ERROR, new ChannelTypeUID(BINDING_ID, CHANNEL_STORAGE_ERROR), //
            CHANNEL_SPEAKER_VOLUME, new ChannelTypeUID("system", "volume"));

    private final IpCameraHandler handler;
    private volatile boolean batterySupported = false;
    private volatile boolean initialized = false;
    private int cycle = 0;
    private int statusCycle = 0;

    public ReolinkStatus(IpCameraHandler handler) {
        this.handler = handler;
    }

    public void dispose() {
        initialized = false;
        batterySupported = false;
        cycle = 0;
        statusCycle = 0;
    }

    public static boolean isStatusChannel(String channelId) {
        return CHANNEL_TYPES.containsKey(channelId);
    }

    private int channel() {
        return handler.cameraConfig.getNvrChannel();
    }

    /**
     * Called after GetAbility was evaluated. Adds the supported channels to things that were created before these
     * channels existed and reads all values once.
     */
    public void initialize(boolean battery) {
        batterySupported = battery;
        initialized = true;
        List<String> add = new ArrayList<>(List.of(CHANNEL_CAMERA_ONLINE, CHANNEL_CAMERA_SLEEPING,
                CHANNEL_STORAGE_USAGE, CHANNEL_STORAGE_ERROR, CHANNEL_SPEAKER_VOLUME));
        if (battery) {
            add.addAll(List.of(CHANNEL_BATTERY_LEVEL, CHANNEL_BATTERY_LOW, CHANNEL_BATTERY_CHARGING));
        } else {
            removeChannels(List.of(CHANNEL_BATTERY_LEVEL, CHANNEL_BATTERY_LOW, CHANNEL_BATTERY_CHARGING));
        }
        for (String id : add) {
            ChannelTypeUID type = CHANNEL_TYPES.get(id);
            if (type != null) {
                handler.addMissingChannel(id, type);
            }
        }
        requestDeviceInfo();
        poll(true);
    }

    /**
     * Called from the regular poll loop. The connection and sleep state is answered by the NVR / Home Hub itself and
     * is polled in the shorter, configurable interval.
     */
    public void onPollCycle() {
        if (!initialized) {
            return;
        }
        if (++cycle >= POLL_EVERY_CYCLES) {
            cycle = 0;
            statusCycle = 0;
            poll(false);
            return;
        }
        int statusCycles = statusPollCycles(handler.cameraConfig.getChannelStatusInterval());
        if (statusCycles > 0 && ++statusCycle >= statusCycles) {
            statusCycle = 0;
            if (handler.isChannelLinked(CHANNEL_CAMERA_ONLINE) || handler.isChannelLinked(CHANNEL_CAMERA_SLEEPING)) {
                requestChannelStatus();
            }
        }
    }

    /**
     * @return the number of poll cycles between two connection state requests, or 0 if they are disabled
     */
    static int statusPollCycles(int intervalSeconds) {
        if (intervalSeconds <= 0) {
            return 0;
        }
        return Math.max(1, (intervalSeconds + POLL_CYCLE_SECONDS / 2) / POLL_CYCLE_SECONDS);
    }

    private void poll(boolean all) {
        if (all || handler.isChannelLinked(CHANNEL_CAMERA_ONLINE) || handler.isChannelLinked(CHANNEL_CAMERA_SLEEPING)) {
            requestChannelStatus();
        }
        if (batterySupported && (all || handler.isChannelLinked(CHANNEL_BATTERY_LEVEL)
                || handler.isChannelLinked(CHANNEL_BATTERY_LOW) || handler.isChannelLinked(CHANNEL_BATTERY_CHARGING))) {
            requestBattery();
        }
        if (all || handler.isChannelLinked(CHANNEL_STORAGE_USAGE) || handler.isChannelLinked(CHANNEL_STORAGE_ERROR)) {
            requestStorage();
        }
        if (all || handler.isChannelLinked(CHANNEL_SPEAKER_VOLUME)) {
            requestVolume();
        }
        // read on startup by the REFRESH of the linked channel, afterwards polled to follow changes in the app
        if (!all && handler.isChannelLinked(CHANNEL_ENABLE_AUDIO_ALARM)) {
            requestAudioAlarm();
        }
    }

    private void post(String cmd, String param) {
        handler.sendHttpPOST("/api.cgi?cmd=" + cmd + handler.reolinkAuth,
                "[{\"cmd\":\"" + cmd + "\",\"action\":0,\"param\":" + param + "}]");
    }

    private void requestChannelStatus() {
        post("GetChannelstatus", "{}");
    }

    private void requestBattery() {
        post("GetBatteryInfo", "{\"channel\":" + channel() + "}");
    }

    private void requestStorage() {
        post("GetHddInfo", "{}");
    }

    private void requestVolume() {
        post("GetAudioCfg", "{\"channel\":" + channel() + "}");
    }

    /**
     * Reads whether the siren is triggered by alarms (enableAudioAlarm).
     */
    public void requestAudioAlarm() {
        if (handler.getThing().getChannel(CHANNEL_ENABLE_AUDIO_ALARM) == null) {
            return;
        }
        if (handler.reolinkScheduleVersion == 1) {
            post("GetAudioAlarmV20", "{\"channel\":" + channel() + "}");
        } else {
            post("GetAudioAlarm", "{\"channel\":" + channel() + "}");
        }
    }

    private void requestDeviceInfo() {
        post("GetChnTypeInfo", "{\"channel\":" + channel() + "}");
    }

    public void handleCommand(String channelId, Command command) {
        if (command instanceof RefreshType) {
            switch (channelId) {
                case CHANNEL_CAMERA_ONLINE, CHANNEL_CAMERA_SLEEPING -> requestChannelStatus();
                case CHANNEL_BATTERY_LEVEL, CHANNEL_BATTERY_LOW, CHANNEL_BATTERY_CHARGING -> {
                    if (batterySupported) {
                        requestBattery();
                    }
                }
                case CHANNEL_STORAGE_USAGE, CHANNEL_STORAGE_ERROR -> requestStorage();
                case CHANNEL_SPEAKER_VOLUME -> requestVolume();
                default -> {
                }
            }
            return;
        }
        if (CHANNEL_SPEAKER_VOLUME.equals(channelId)) {
            int volume;
            if (command instanceof PercentType percent) {
                volume = percent.intValue();
            } else if (OnOffType.OFF.equals(command)) {
                volume = 0;
            } else if (OnOffType.ON.equals(command)) {
                volume = 100;
            } else {
                handler.logger.debug("Unsupported command {} for channel {}", command, channelId);
                return;
            }
            post("SetAudioCfg", "{\"AudioCfg\":{\"channel\":" + channel() + ",\"volume\":" + volume + "}}");
            handler.scheduleTask(this::requestVolume, 1000);
        }
    }

    public void handleChannelStatusResponse(String content) {
        JsonObject value = findValue(content);
        if (value == null) {
            handler.logger.debug("Reolink GetChannelstatus failed: {}", content);
            if (isNotSupported(content)) {
                removeChannels(List.of(CHANNEL_CAMERA_ONLINE, CHANNEL_CAMERA_SLEEPING));
            }
            return;
        }
        int[] status = parseChannelStatus(value, channel());
        if (status == null) {
            return;
        }
        handler.setChannelState(CHANNEL_CAMERA_ONLINE, OnOffType.from(status[0] == 1));
        handler.setChannelState(CHANNEL_CAMERA_SLEEPING, OnOffType.from(status[1] == 1));
    }

    public void handleBatteryResponse(String content) {
        JsonObject value = findValue(content);
        BatteryInfo battery = value == null ? null : parseBattery(value);
        if (battery == null) {
            handler.logger.debug("Reolink GetBatteryInfo reply could not be parsed: {}", content);
            return;
        }
        handler.setChannelState(CHANNEL_BATTERY_LEVEL, new DecimalType(battery.percent()));
        handler.setChannelState(CHANNEL_BATTERY_LOW, OnOffType.from(battery.low()));
        handler.setChannelState(CHANNEL_BATTERY_CHARGING, OnOffType.from(battery.charging()));
    }

    public void handleStorageResponse(String content) {
        JsonObject value = findValue(content);
        StorageInfo storage = value == null ? null : parseStorage(value);
        if (storage == null) {
            handler.logger.debug("Reolink GetHddInfo failed or reports no storage: {}", content);
            if (isNotSupported(content)) {
                removeChannels(List.of(CHANNEL_STORAGE_USAGE, CHANNEL_STORAGE_ERROR));
            } else if (value != null) {
                // no storage at the moment, keep the channels so that the values return when it is inserted again
                handler.setChannelState(CHANNEL_STORAGE_USAGE, UnDefType.UNDEF);
                handler.setChannelState(CHANNEL_STORAGE_ERROR, UnDefType.UNDEF);
            }
            return;
        }
        handler.setChannelState(CHANNEL_STORAGE_USAGE, new DecimalType(storage.usedPercent()));
        handler.setChannelState(CHANNEL_STORAGE_ERROR, OnOffType.from(storage.error()));
    }

    public void handleVolumeResponse(String content) {
        JsonObject value = findValue(content);
        Integer volume = value == null ? null : parseVolume(value);
        if (volume == null) {
            handler.logger.debug("Reolink GetAudioCfg failed: {}", content);
            if (isNotSupported(content)) {
                removeChannels(List.of(CHANNEL_SPEAKER_VOLUME));
            }
            return;
        }
        handler.setChannelState(CHANNEL_SPEAKER_VOLUME, new PercentType(Math.max(0, Math.min(100, volume))));
    }

    public void handleAudioAlarmResponse(String content) {
        JsonObject value = findValue(content);
        Boolean enabled = value == null ? null : parseAudioAlarmEnable(value);
        if (enabled == null) {
            // keep the state on errors, e.g. an expired login
            handler.logger.debug("Reolink GetAudioAlarmV20 reply could not be parsed: {}", content);
            return;
        }
        handler.setChannelState(CHANNEL_ENABLE_AUDIO_ALARM, OnOffType.from(enabled));
    }

    public void handleDeviceInfoResponse(String content) {
        JsonObject value = findValue(content);
        if (value == null) {
            return;
        }
        if (value.has("typeInfo")) {
            handler.setThingProperty(Thing.PROPERTY_MODEL_ID, value.get("typeInfo").getAsString());
        }
        if (value.has("firmVer")) {
            handler.setThingProperty(Thing.PROPERTY_FIRMWARE_VERSION, value.get("firmVer").getAsString());
        }
    }

    private void removeChannels(List<String> ids) {
        List<Channel> remove = new ArrayList<>();
        for (String id : ids) {
            Channel channel = handler.getThing().getChannel(id);
            if (channel != null) {
                remove.add(channel);
            }
        }
        handler.removeChannels(remove);
    }

    record BatteryInfo(int percent, boolean low, boolean charging) {
    }

    record StorageInfo(int usedPercent, boolean error) {
    }

    /**
     * @return {online, sleep} of the given channel, or null if the channel is not listed
     */
    static int @Nullable [] parseChannelStatus(JsonObject value, int channel) {
        if (!value.has("status") || !value.get("status").isJsonArray()) {
            return null;
        }
        for (JsonElement element : value.getAsJsonArray("status")) {
            if (element.isJsonObject()) {
                JsonObject status = element.getAsJsonObject();
                if (getInt(status, "channel", -1) == channel) {
                    return new int[] { getInt(status, "online", 0), getInt(status, "sleep", 0) };
                }
            }
        }
        return null;
    }

    static @Nullable BatteryInfo parseBattery(JsonObject value) {
        JsonObject battery = value.has("Battery") && value.get("Battery").isJsonObject()
                ? value.getAsJsonObject("Battery")
                : null;
        if (battery == null || !battery.has("batteryPercent")) {
            return null;
        }
        // chargeStatus: 0 = not charging, 1 = charging, 2 = fully charged
        return new BatteryInfo(getInt(battery, "batteryPercent", 0), getInt(battery, "lowPowerFlag", 0) != 0,
                getInt(battery, "chargeStatus", 0) == 1);
    }

    /**
     * Sums all storage devices. The Reolink API reports capacity and free space ("size") in MB. A device that is not
     * mounted or not formatted is reported as error.
     */
    static @Nullable StorageInfo parseStorage(JsonObject value) {
        if (!value.has("HddInfo") || !value.get("HddInfo").isJsonArray()) {
            return null;
        }
        JsonArray disks = value.getAsJsonArray("HddInfo");
        long capacity = 0;
        long free = 0;
        boolean error = false;
        for (JsonElement element : disks) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject disk = element.getAsJsonObject();
            capacity += getInt(disk, "capacity", 0);
            free += getInt(disk, "size", 0);
            if (getInt(disk, "mount", 0) != 1 || getInt(disk, "format", 0) != 1) {
                error = true;
            }
        }
        if (disks.isEmpty()) {
            return null;
        }
        int used = capacity > 0 ? (int) Math.round((capacity - free) * 100.0 / capacity) : 0;
        return new StorageInfo(Math.max(0, Math.min(100, used)), error);
    }

    /**
     * @return the "enable" flag of the "Audio" object of a GetAudioAlarmV20 reply, or null if it is missing
     */
    static @Nullable Boolean parseAudioAlarmEnable(JsonObject value) {
        if (!value.has("Audio") || !value.get("Audio").isJsonObject()) {
            return null;
        }
        JsonObject audio = value.getAsJsonObject("Audio");
        return audio.has("enable") ? getInt(audio, "enable", 0) != 0 : null;
    }

    static @Nullable Integer parseVolume(JsonObject value) {
        if (!value.has("AudioCfg") || !value.get("AudioCfg").isJsonObject()) {
            return null;
        }
        JsonObject audio = value.getAsJsonObject("AudioCfg");
        return audio.has("volume") ? getInt(audio, "volume", 0) : null;
    }

    /**
     * Only a "not support" error (rspCode -9) means the command is unavailable. Other errors, like an expired login,
     * must not remove channels.
     */
    static boolean isNotSupported(String content) {
        return rspCode(content) == -9;
    }

    /**
     * A "please login first" error (rspCode -6) means the token is not (or no longer) accepted.
     */
    public static boolean isLoginRequired(String content) {
        return content.contains("rspCode") && rspCode(content) == -6;
    }

    /**
     * @return the rspCode of an error reply, or 0 if the reply contains none
     */
    static int rspCode(String content) {
        try {
            JsonElement root = JsonParser.parseString(content);
            JsonElement first = root.isJsonArray() && !root.getAsJsonArray().isEmpty() ? root.getAsJsonArray().get(0)
                    : root;
            if (first.isJsonObject() && first.getAsJsonObject().has("error")
                    && first.getAsJsonObject().get("error").isJsonObject()) {
                return getInt(first.getAsJsonObject().getAsJsonObject("error"), "rspCode", 0);
            }
        } catch (JsonParseException | IllegalStateException e) {
            // not a JSON reply
        }
        return 0;
    }

    private static int getInt(JsonObject object, String member, int fallback) {
        try {
            return object.has(member) ? object.get(member).getAsInt() : fallback;
        } catch (NumberFormatException | UnsupportedOperationException | IllegalStateException e) {
            return fallback;
        }
    }

    /**
     * @return the value object of a successful reply, or null for errors and unparsable content
     */
    static @Nullable JsonObject findValue(String content) {
        try {
            JsonElement root = JsonParser.parseString(content);
            JsonObject first;
            if (root.isJsonArray()) {
                JsonArray array = root.getAsJsonArray();
                if (array.isEmpty() || !array.get(0).isJsonObject()) {
                    return null;
                }
                first = array.get(0).getAsJsonObject();
            } else if (root.isJsonObject()) {
                first = root.getAsJsonObject();
            } else {
                return null;
            }
            if (getInt(first, "code", -1) != 0 || !first.has("value") || !first.get("value").isJsonObject()) {
                return null;
            }
            return first.getAsJsonObject("value");
        } catch (JsonParseException | IllegalStateException e) {
            return null;
        }
    }
}
