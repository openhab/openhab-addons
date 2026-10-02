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
import java.util.concurrent.ScheduledFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ipcamera.internal.handler.IpCameraHandler;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.StateOption;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * The {@link ReolinkPtz} controls pan, tilt and presets of Reolink PTZ cameras through the Reolink HTTP API.
 * It is used for Reolink things whose GetAbility reply reports PTZ for the configured channel, which covers cameras
 * connected to a Reolink NVR or Home Hub where ONVIF PTZ is not usable.
 * <p>
 * The API only offers relative moves (Left/Right/Up/Down/Stop) and presets, so a percentage command is executed by
 * moving towards the target while polling GetPtzCurPos. The position is not polled otherwise, because every request
 * wakes up battery powered cameras.
 *
 * @author Gerhard Braun - Initial contribution
 */
@NonNullByDefault
public class ReolinkPtz {
    private static final int SPEED_FAST = 32;
    private static final int SPEED_SLOW = 8;
    private static final int STEP_MS = 400;
    private static final int SEEK_POLL_MS = 250;
    private static final int SEEK_TIMEOUT_MS = 20000;
    private static final int REFRESH_AFTER_STOP_MS = 800;
    private static final int REFRESH_AFTER_PRESET_MS = 4000;
    private static final double SLOW_ZONE = 0.15; // fraction of the range in which the slow speed is used
    private static final double TOLERANCE = 0.015; // fraction of the range that counts as target reached

    enum Axis {
        PAN,
        TILT
    }

    private final IpCameraHandler handler;
    private volatile boolean supported = false;
    private int lastPan = -1;
    private int lastTilt = -1;
    private int targetPan = -1;
    private int targetTilt = -1;
    private @Nullable Axis seekAxis = null;
    private String seekOp = "";
    private int seekSpeed = 0;
    private long seekDeadline = 0;
    private @Nullable ScheduledFuture<?> pollFuture;
    private @Nullable ScheduledFuture<?> stopFuture;

    public ReolinkPtz(IpCameraHandler handler) {
        this.handler = handler;
    }

    public boolean isSupported() {
        return supported;
    }

    public void setSupported(boolean supported) {
        this.supported = supported;
    }

    public static boolean isPtzChannel(String channelId) {
        return CHANNEL_PAN.equals(channelId) || CHANNEL_TILT.equals(channelId) || CHANNEL_GOTO_PRESET.equals(channelId);
    }

    /**
     * Stops pending work. The instance is reused when the thing is initialized again, so the state is reset here.
     */
    public synchronized void dispose() {
        cancel(pollFuture);
        cancel(stopFuture);
        pollFuture = null;
        stopFuture = null;
        seekAxis = null;
        seekOp = "";
        targetPan = -1;
        targetTilt = -1;
        supported = false;
    }

    private static void cancel(@Nullable ScheduledFuture<?> future) {
        if (future != null) {
            future.cancel(false);
        }
    }

    private int channel() {
        return handler.cameraConfig.getNvrChannel();
    }

    private int min(Axis axis) {
        return axis == Axis.PAN ? handler.cameraConfig.getPtzPanMin() : handler.cameraConfig.getPtzTiltMin();
    }

    private int max(Axis axis) {
        return axis == Axis.PAN ? handler.cameraConfig.getPtzPanMax() : handler.cameraConfig.getPtzTiltMax();
    }

    private void sendPtzCtrl(String op, int speed) {
        handler.sendHttpPOST("/api.cgi?cmd=PtzCtrl" + handler.reolinkAuth,
                "[{\"cmd\":\"PtzCtrl\",\"param\":{\"channel\":" + channel() + ",\"op\":\"" + op + "\",\"speed\":"
                        + speed + "}}]");
    }

    private void sendStop() {
        handler.sendHttpPOST("/api.cgi?cmd=PtzCtrl" + handler.reolinkAuth,
                "[{\"cmd\":\"PtzCtrl\",\"param\":{\"channel\":" + channel() + ",\"op\":\"Stop\"}}]");
    }

    public void requestPosition() {
        handler.sendHttpPOST("/api.cgi?cmd=GetPtzCurPos" + handler.reolinkAuth,
                "[{\"cmd\":\"GetPtzCurPos\",\"action\":0,\"param\":{\"PtzCurPos\":{\"channel\":" + channel() + "}}}]");
    }

    public void requestPresets() {
        handler.sendHttpPOST("/api.cgi?cmd=GetPtzPreset" + handler.reolinkAuth,
                "[{\"cmd\":\"GetPtzPreset\",\"action\":0,\"param\":{\"channel\":" + channel() + "}}]");
    }

    private void requestPositionLater(long delayMs) {
        handler.scheduleTask(this::requestPosition, delayMs);
    }

    public synchronized void handleCommand(ChannelUID channelUID, Command command) {
        String id = channelUID.getId();
        if (command instanceof RefreshType) {
            if (CHANNEL_GOTO_PRESET.equals(id)) {
                requestPresets();
            } else {
                requestPosition();
            }
            return;
        }
        switch (id) {
            case CHANNEL_GOTO_PRESET:
                abortSeek(false);
                try {
                    int preset = Integer.parseInt(command.toString().trim());
                    handler.sendHttpPOST("/api.cgi?cmd=PtzCtrl" + handler.reolinkAuth,
                            "[{\"cmd\":\"PtzCtrl\",\"param\":{\"channel\":" + channel() + ",\"op\":\"ToPos\",\"id\":"
                                    + preset + ",\"speed\":" + SPEED_FAST + "}}]");
                    requestPositionLater(REFRESH_AFTER_PRESET_MS);
                } catch (NumberFormatException e) {
                    handler.logger.warn("Reolink PTZ preset must be a number, got '{}'", command);
                }
                return;
            case CHANNEL_PAN:
            case CHANNEL_TILT:
                Axis axis = CHANNEL_PAN.equals(id) ? Axis.PAN : Axis.TILT;
                if (command instanceof IncreaseDecreaseType) {
                    abortSeek(false);
                    sendPtzCtrl(moveOp(axis, IncreaseDecreaseType.INCREASE.equals(command)), SPEED_FAST);
                    if (!handler.cameraConfig.getPtzContinuous()) {
                        cancel(stopFuture);
                        stopFuture = handler.scheduleTask(() -> {
                            sendStop();
                            requestPositionLater(REFRESH_AFTER_STOP_MS);
                        }, STEP_MS);
                    }
                } else if (OnOffType.OFF.equals(command)) {
                    abortSeek(true);
                    requestPositionLater(REFRESH_AFTER_STOP_MS);
                } else if (command instanceof PercentType percent) {
                    startSeek(axis, percent.doubleValue());
                } else {
                    handler.logger.debug("Unsupported command {} for Reolink PTZ channel {}", command, id);
                }
                return;
            default:
                return;
        }
    }

    /**
     * Uses the same directions as the ONVIF implementation and the README examples: INCREASE pans left and tilts
     * down, and INCREASE always moves towards a higher percentage.
     */
    static String moveOp(Axis axis, boolean increase) {
        if (axis == Axis.PAN) {
            return increase ? "Left" : "Right";
        }
        return increase ? "Down" : "Up";
    }

    static boolean isInverted(Axis axis) {
        // Ppos grows to the right, but INCREASE (towards 100 %) pans left. Tpos grows downwards like the percentage.
        return axis == Axis.PAN;
    }

    /**
     * Converts a camera position to a percentage of the configured range.
     */
    static int positionToPercent(int position, int min, int max, boolean inverted) {
        if (max <= min) {
            return 0;
        }
        double percent = (double) (position - min) / (max - min) * 100.0;
        if (inverted) {
            percent = 100.0 - percent;
        }
        return (int) Math.round(Math.max(0, Math.min(100, percent)));
    }

    static int percentToPosition(double percent, int min, int max, boolean inverted) {
        double fraction = Math.max(0, Math.min(100, percent)) / 100.0;
        if (inverted) {
            fraction = 1.0 - fraction;
        }
        return (int) Math.round(min + fraction * (max - min));
    }

    private void startSeek(Axis axis, double percent) {
        if (max(axis) <= min(axis)) {
            handler.logger.warn("Invalid Reolink PTZ range for {}: min {} must be lower than max {}", axis, min(axis),
                    max(axis));
            return;
        }
        int target = percentToPosition(percent, min(axis), max(axis), isInverted(axis));
        if (axis == Axis.PAN) {
            targetPan = target;
        } else {
            targetTilt = target;
        }
        if (seekAxis == null) {
            beginNextAxis();
        }
    }

    private synchronized void beginNextAxis() {
        if (targetPan >= 0) {
            seekAxis = Axis.PAN;
        } else if (targetTilt >= 0) {
            seekAxis = Axis.TILT;
        } else {
            seekAxis = null;
            return;
        }
        seekOp = "";
        seekSpeed = 0;
        seekDeadline = System.currentTimeMillis() + SEEK_TIMEOUT_MS;
        requestPosition();
    }

    private void abortSeek(boolean sendStop) {
        cancel(pollFuture);
        pollFuture = null;
        boolean wasMoving = !seekOp.isEmpty();
        seekAxis = null;
        seekOp = "";
        targetPan = -1;
        targetTilt = -1;
        if (sendStop || wasMoving) {
            sendStop();
        }
    }

    /**
     * Handles the reply of GetPtzCurPos.
     */
    public synchronized void handlePositionResponse(String content) {
        int[] position = parsePosition(content);
        if (position == null) {
            handler.logger.debug("Reolink GetPtzCurPos reply could not be parsed: {}", content);
            return;
        }
        lastPan = position[0];
        lastTilt = position[1];
        handler.setChannelState(CHANNEL_PAN,
                new PercentType(positionToPercent(lastPan, min(Axis.PAN), max(Axis.PAN), isInverted(Axis.PAN))));
        handler.setChannelState(CHANNEL_TILT,
                new PercentType(positionToPercent(lastTilt, min(Axis.TILT), max(Axis.TILT), isInverted(Axis.TILT))));
        continueSeek();
    }

    private void continueSeek() {
        Axis axis = seekAxis;
        if (axis == null) {
            return;
        }
        int current = axis == Axis.PAN ? lastPan : lastTilt;
        int target = axis == Axis.PAN ? targetPan : targetTilt;
        int range = max(axis) - min(axis);
        int diff = target - current;
        boolean timeout = System.currentTimeMillis() > seekDeadline;
        // Left lowers the pan position, Up lowers the tilt position.
        String op = axis == Axis.PAN ? (diff < 0 ? "Left" : "Right") : (diff < 0 ? "Up" : "Down");
        boolean reached = Math.abs(diff) <= Math.max(1, range * TOLERANCE);
        // Stop instead of reversing, to avoid oscillating around the target.
        boolean overshot = !seekOp.isEmpty() && !seekOp.equals(op);
        if (reached || overshot || timeout) {
            if (timeout) {
                handler.logger.debug("Reolink PTZ did not reach target {} (at {}) in time", target, current);
            }
            if (!seekOp.isEmpty()) {
                sendStop();
            }
            if (axis == Axis.PAN) {
                targetPan = -1;
            } else {
                targetTilt = -1;
            }
            seekAxis = null;
            seekOp = "";
            if (targetPan >= 0 || targetTilt >= 0) {
                pollFuture = handler.scheduleTask(this::beginNextAxis, REFRESH_AFTER_STOP_MS);
            } else {
                requestPositionLater(REFRESH_AFTER_STOP_MS);
            }
            return;
        }
        int speed = Math.abs(diff) < range * SLOW_ZONE ? SPEED_SLOW : SPEED_FAST;
        if (!op.equals(seekOp) || speed != seekSpeed) {
            sendPtzCtrl(op, speed);
            seekOp = op;
            seekSpeed = speed;
        }
        pollFuture = handler.scheduleTask(this::requestPosition, SEEK_POLL_MS);
    }

    /**
     * Handles the reply of GetPtzPreset and publishes the enabled presets as options of the gotoPreset channel.
     */
    public void handlePresetResponse(String content) {
        List<StateOption> options = parsePresets(content);
        if (options == null) {
            handler.logger.debug("Reolink GetPtzPreset reply could not be parsed: {}", content);
            return;
        }
        handler.stateDescriptionProvider
                .setStateOptions(new ChannelUID(handler.getThing().getUID(), CHANNEL_GOTO_PRESET), options);
        handler.logger.debug("Reolink PTZ presets: {}", options);
    }

    /**
     * @return {pan, tilt} from a GetPtzCurPos reply, or null if the reply does not contain a position
     */
    static int @Nullable [] parsePosition(String content) {
        JsonObject position = findValue(content, "PtzCurPos");
        if (position == null || !position.has("Ppos") || !position.has("Tpos")) {
            return null;
        }
        try {
            return new int[] { position.get("Ppos").getAsInt(), position.get("Tpos").getAsInt() };
        } catch (NumberFormatException | UnsupportedOperationException | IllegalStateException e) {
            return null;
        }
    }

    /**
     * @return the enabled presets of a GetPtzPreset reply, or null if the reply does not contain presets
     */
    static @Nullable List<StateOption> parsePresets(String content) {
        JsonObject value = findValue(content, null);
        if (value == null || !value.has("PtzPreset") || !value.get("PtzPreset").isJsonArray()) {
            return null;
        }
        List<StateOption> options = new ArrayList<>();
        for (JsonElement element : value.getAsJsonArray("PtzPreset")) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject preset = element.getAsJsonObject();
            try {
                if (!preset.has("id") || (preset.has("enable") && preset.get("enable").getAsInt() == 0)) {
                    continue;
                }
                String id = preset.get("id").getAsString();
                String name = preset.has("name") ? preset.get("name").getAsString() : "";
                options.add(new StateOption(id, name.isBlank() ? "Preset " + id : name));
            } catch (NumberFormatException | UnsupportedOperationException | IllegalStateException e) {
                // skip malformed entries
            }
        }
        return options;
    }

    private static @Nullable JsonObject findValue(String content, @Nullable String member) {
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
            if (!first.has("value") || !first.get("value").isJsonObject()) {
                return null;
            }
            JsonObject value = first.getAsJsonObject("value");
            if (member == null) {
                return value;
            }
            return value.has(member) && value.get(member).isJsonObject() ? value.getAsJsonObject(member) : null;
        } catch (JsonParseException | IllegalStateException e) {
            return null;
        }
    }
}
