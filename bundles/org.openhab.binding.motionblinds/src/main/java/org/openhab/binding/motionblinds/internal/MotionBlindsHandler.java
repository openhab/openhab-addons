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
package org.openhab.binding.motionblinds.internal;

import static org.openhab.binding.motionblinds.internal.MotionBlindsBindingConstants.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.measure.quantity.Angle;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.motionblinds.internal.MotionBlindsCommunicationManager.MessageListener;
import org.openhab.binding.motionblinds.internal.dto.DeviceListEntry;
import org.openhab.binding.motionblinds.internal.dto.DeviceStatus;
import org.openhab.binding.motionblinds.internal.dto.MotionBlindsMessage;
import org.openhab.binding.motionblinds.internal.dto.MotionBlindsRequest;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link MotionBlindsHandler} controls a motor that is directly connected to Wi-Fi.
 *
 * <p>
 * Requests are authorized with an access token that is derived from the app key and a token that the motor hands
 * out in {@code GetDeviceListAck} and {@code Heartbeat} messages. When a request is rejected, the token is fetched
 * again and the request is retried once.
 *
 * <p>
 * The motor is identified by its MAC address only. Its IP address is learned from multicast messages and from the
 * answer to a multicast {@code GetDeviceList}, so it follows DHCP changes without configuration.
 *
 * @author Oleksandr Mishchuk - Initial contribution
 */
@NonNullByDefault
public class MotionBlindsHandler extends BaseThingHandler implements MessageListener {

    private static final int MIN_REFRESH_INTERVAL = 30;
    private static final int MAX_ANGLE = 180;
    private static final int IP_RESOLUTION_TIMEOUT_MS = 3000;
    private static final int MAX_TILT_MOVES = 2;
    /** A tilt move of a few percent takes 1-2 seconds; the motor has stopped when two checks agree */
    private static final int STOP_CHECK_INTERVAL_MS = 1000;
    private static final int MAX_STOP_CHECKS = 6;

    private final Logger logger = LoggerFactory.getLogger(MotionBlindsHandler.class);
    private final Gson gson = new Gson();
    private final MotionBlindsCommunicationManager communicationManager;
    private final Object requestLock = new Object();

    private MotionBlindsConfiguration config = new MotionBlindsConfiguration();
    private String mac = "";
    /** IP address the motor was last heard from; empty until it is found by its MAC address */
    private volatile String ipAddress = "";
    private volatile @Nullable CompletableFuture<String> ipResolution;
    private volatile String deviceType = "";
    private volatile @Nullable String token;
    private volatile @Nullable String accessToken;
    private @Nullable ScheduledFuture<?> pollJob;
    private SlatTracker slatTracker = new SlatTracker(3, 2);

    public MotionBlindsHandler(Thing thing, MotionBlindsCommunicationManager communicationManager) {
        super(thing);
        this.communicationManager = communicationManager;
    }

    @Override
    public void initialize() {
        config = getConfigAs(MotionBlindsConfiguration.class);
        mac = MotionBlindsCommunicationManager.normalizeMac(config.macAddress);
        ipAddress = "";

        if (mac.length() != 12) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.config-error-invalid-mac");
            return;
        }
        if (config.key.length() != 16) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.config-error-invalid-key");
            return;
        }

        String type = thing.getProperties().get(PROPERTY_DEVICE_TYPE);
        deviceType = type != null ? type : "";
        token = null;
        accessToken = null;
        slatTracker = new SlatTracker(config.tiltTravel, config.tiltSlack);

        updateStatus(ThingStatus.UNKNOWN);
        communicationManager.registerListener(mac, this);
        pollJob = scheduler.scheduleWithFixedDelay(this::poll, 0,
                Math.max(MIN_REFRESH_INTERVAL, config.refreshInterval), TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> job = pollJob;
        if (job != null) {
            job.cancel(true);
            pollJob = null;
        }
        communicationManager.unregisterListener(mac, this);
        token = null;
        accessToken = null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            scheduler.execute(this::poll);
            return;
        }

        if (CHANNEL_TILT.equals(channelUID.getId())) {
            Integer tilt = tiltPercent(command);
            if (tilt == null) {
                logger.debug("Unsupported command {} for channel {}", command, channelUID);
                return;
            }
            scheduler.execute(() -> tilt(tilt));
            return;
        }

        Map<String, Object> data = switch (channelUID.getId()) {
            case CHANNEL_POSITION -> positionCommand(command);
            case CHANNEL_ANGLE -> angleCommand(command);
            default -> null;
        };
        if (data == null) {
            logger.debug("Unsupported command {} for channel {}", command, channelUID);
            return;
        }
        scheduler.execute(() -> write(data));
    }

    private @Nullable Map<String, Object> positionCommand(Command command) {
        if (command instanceof UpDownType) {
            return Map.of("operation", command == UpDownType.UP ? OPERATION_OPEN : OPERATION_CLOSE);
        } else if (command == StopMoveType.STOP) {
            return Map.of("operation", OPERATION_STOP);
        } else if (command instanceof PercentType percent) {
            return Map.of("targetPosition", percent.intValue());
        }
        return null;
    }

    private @Nullable Integer tiltPercent(Command command) {
        if (command instanceof PercentType percent) {
            return percent.intValue();
        } else if (command instanceof OnOffType onOff) {
            return onOff == OnOffType.ON ? 100 : 0;
        }
        return null;
    }

    /**
     * Turn the slats to the given tilt at the current height, see {@link SlatTracker}. From fully lowered this can
     * take two moves; the state is read again after the first one.
     */
    private void tilt(int tiltPercent) {
        for (int move = 1; move <= MAX_TILT_MOVES; move++) {
            Map<String, Object> data = slatTracker.tiltCommand(tiltPercent);
            if (data == null) {
                // the state of the slats is not known yet
                poll();
                data = slatTracker.tiltCommand(tiltPercent);
            }
            if (data == null) {
                logger.debug("Unable to tilt motor {}: position is not known", mac);
                return;
            }
            if (data.isEmpty()) {
                return;
            }
            write(data);
            if (move == MAX_TILT_MOVES || thing.getStatus() != ThingStatus.ONLINE || !waitUntilStopped()) {
                return;
            }
        }
    }

    /**
     * Read the status until the motor angle does not change anymore.
     *
     * @return {@code false} if interrupted
     */
    private boolean waitUntilStopped() {
        Integer previousAngle = null;
        for (int check = 0; check < MAX_STOP_CHECKS; check++) {
            try {
                Thread.sleep(STOP_CHECK_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
            poll();
            Integer angle = slatTracker.getMotorAngle();
            if (angle != null && angle.equals(previousAngle)) {
                return true;
            }
            previousAngle = angle;
        }
        return true;
    }

    private @Nullable Map<String, Object> angleCommand(Command command) {
        Integer angle = null;
        if (command instanceof QuantityType<?> quantity) {
            QuantityType<?> degrees = quantity.toUnit(Units.DEGREE_ANGLE);
            if (degrees != null) {
                angle = degrees.intValue();
            }
        } else if (command instanceof DecimalType decimal) {
            angle = decimal.intValue();
        }
        return angle == null ? null : Map.of("targetAngle", Math.max(0, Math.min(MAX_ANGLE, angle)));
    }

    private void poll() {
        try {
            request(MSG_READ_DEVICE, null);
        } catch (AccessDeniedException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.config-error-access-denied");
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void write(Map<String, Object> data) {
        try {
            // the acknowledge carries the state before the command; the new state arrives with Report messages
            request(MSG_WRITE_DEVICE, data);
        } catch (AccessDeniedException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.config-error-access-denied");
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    /**
     * Send a {@code ReadDevice} or {@code WriteDevice} request. If the motor rejects the access token, a new token
     * is fetched and the request is retried once.
     */
    private MotionBlindsMessage request(String msgType, @Nullable Map<String, Object> data) throws IOException {
        synchronized (requestLock) {
            for (int attempt = 1;; attempt++) {
                String currentAccessToken = getAccessToken();
                MotionBlindsMessage response = send(
                        new MotionBlindsRequest(msgType, mac, deviceType, currentAccessToken, data));
                String actionResult = response.actionResult;
                if (actionResult == null) {
                    if (MSG_WRITE_DEVICE.equals(msgType)) {
                        // the acknowledge carries the state before the command, it may arrive after the first
                        // Report of the movement and must not roll the state back
                        updateStatus(ThingStatus.ONLINE);
                    } else {
                        handleStatus(response);
                    }
                    return response;
                }
                logger.debug("Motor {} rejected {}: {}", mac, msgType, actionResult);
                token = response.token;
                accessToken = null;
                if (attempt >= 2) {
                    throw new AccessDeniedException(actionResult);
                }
            }
        }
    }

    /**
     * Send a request to the motor. If the IP address is not known yet, or the motor does not answer on it anymore,
     * the motor is found by its MAC address with a multicast {@code GetDeviceList} and the request is sent (again) to
     * the address it answered from.
     */
    private MotionBlindsMessage send(MotionBlindsRequest request) throws IOException {
        String currentIp = ipAddress;
        if (currentIp.isEmpty()) {
            return communicationManager.sendRequest(resolveIpAddress(), request);
        }
        try {
            return communicationManager.sendRequest(currentIp, request);
        } catch (IOException e) {
            String resolvedIp;
            try {
                resolvedIp = resolveIpAddress();
            } catch (IOException resolveException) {
                throw e;
            }
            if (resolvedIp.equals(currentIp)) {
                throw e;
            }
            return communicationManager.sendRequest(resolvedIp, request);
        }
    }

    /**
     * Find the IP address of the motor: send a multicast {@code GetDeviceList} and wait for the answer of this
     * motor, which {@link #onMessage} receives.
     */
    private String resolveIpAddress() throws IOException {
        CompletableFuture<String> resolution = new CompletableFuture<>();
        ipResolution = resolution;
        try {
            logger.debug("Resolving IP address of motor {}", mac);
            communicationManager.sendDiscoveryRequest();
            return resolution.get(IP_RESOLUTION_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw new IOException("Motor " + mac + " was not found in the network");
        } catch (ExecutionException e) {
            throw new IOException("Unable to resolve IP address of motor " + mac, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while resolving IP address of motor " + mac);
        } finally {
            ipResolution = null;
        }
    }

    private String getAccessToken() throws IOException {
        String current = accessToken;
        if (current != null) {
            return current;
        }
        String currentToken = token;
        if (currentToken == null || deviceType.isEmpty()) {
            currentToken = fetchDeviceList();
        }
        try {
            current = AccessTokenCalculator.calculate(config.key, currentToken);
        } catch (IllegalArgumentException e) {
            throw new IOException("Unable to calculate access token: " + e.getMessage());
        }
        accessToken = current;
        return current;
    }

    /**
     * Fetch the token and device type from the motor.
     *
     * @return the token
     */
    private String fetchDeviceList() throws IOException {
        MotionBlindsMessage response = send(new MotionBlindsRequest(MSG_GET_DEVICE_LIST));
        String newToken = response.token;
        if (!MSG_GET_DEVICE_LIST_ACK.equals(response.msgType) || newToken == null) {
            throw new IOException("Unexpected response to " + MSG_GET_DEVICE_LIST + ": " + response.msgType);
        }

        String type = response.deviceType;
        for (DeviceListEntry entry : parseDeviceList(response.data)) {
            String entryMac = entry.mac;
            if (entryMac != null && mac.equals(MotionBlindsCommunicationManager.normalizeMac(entryMac))) {
                type = entry.deviceType;
            }
        }
        if (type == null) {
            throw new IOException("Motor did not report its device type");
        }
        if (!DEVICE_TYPES_WIFI.contains(type)) {
            logger.info("Motor {} reports unknown device type {}, trying to control it anyway", mac, type);
        }

        deviceType = type;
        token = newToken;
        updateProperty(PROPERTY_DEVICE_TYPE, type);
        String protocolVersion = response.protocolVersion;
        if (protocolVersion != null) {
            updateProperty(PROPERTY_PROTOCOL_VERSION, protocolVersion);
        }
        String firmwareVersion = response.fwVersion;
        if (firmwareVersion != null) {
            updateProperty(Thing.PROPERTY_FIRMWARE_VERSION, firmwareVersion);
        }
        return newToken;
    }

    private List<DeviceListEntry> parseDeviceList(@Nullable JsonElement data) {
        if (data == null || !data.isJsonArray()) {
            return List.of();
        }
        try {
            List<DeviceListEntry> entries = gson.fromJson(data, new TypeToken<List<DeviceListEntry>>() {
            }.getType());
            return entries != null ? entries : List.of();
        } catch (JsonParseException e) {
            logger.debug("Unable to parse device list: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Called by the {@link MotionBlindsCommunicationManager} for multicast messages of this motor.
     */
    @Override
    public void onMessage(MotionBlindsMessage message, String sourceIp) {
        if (message.actionResult != null) {
            return;
        }
        String previousIp = ipAddress;
        if (!sourceIp.equals(previousIp)) {
            if (!previousIp.isEmpty()) {
                logger.info("Motor {} changed its IP address from {} to {}", mac, previousIp, sourceIp);
            } else {
                logger.debug("Motor {} found at {}", mac, sourceIp);
            }
            ipAddress = sourceIp;
        }
        CompletableFuture<String> resolution = ipResolution;
        if (resolution != null) {
            resolution.complete(sourceIp);
        }
        String newToken = message.token;
        if (newToken != null && !newToken.equals(token)) {
            token = newToken;
            accessToken = null;
        }
        if (MSG_REPORT.equals(message.msgType) || MSG_HEARTBEAT.equals(message.msgType)) {
            handleStatus(message);
        }
    }

    private void handleStatus(MotionBlindsMessage message) {
        JsonElement data = message.data;
        if (data != null && data.isJsonObject()) {
            try {
                DeviceStatus status = gson.fromJson(data, DeviceStatus.class);
                if (status != null) {
                    updateChannels(status);
                }
            } catch (JsonParseException e) {
                logger.debug("Unable to parse status of motor {}: {}", mac, e.getMessage());
            }
        }
        updateStatus(ThingStatus.ONLINE);
    }

    private void updateChannels(DeviceStatus status) {
        Integer position = status.currentPosition;
        if (position != null) {
            updateState(CHANNEL_POSITION, new PercentType(Math.max(0, Math.min(100, position))));
        }
        Integer angle = status.currentAngle;
        if (angle != null) {
            updateState(CHANNEL_ANGLE, new QuantityType<Angle>(angle, Units.DEGREE_ANGLE));
        }
        slatTracker.update(position, angle);
        Integer tilt = slatTracker.getTilt();
        if (tilt != null) {
            updateState(CHANNEL_TILT, new PercentType(tilt));
        }
        Integer rssi = status.rssi;
        if (rssi != null) {
            updateState(CHANNEL_RSSI, new QuantityType<>(rssi, Units.DECIBEL_MILLIWATTS));
        }
    }

    /**
     * The motor rejected the access token even after fetching a new token, so the key must be wrong.
     */
    private static class AccessDeniedException extends IOException {
        private static final long serialVersionUID = 1L;

        AccessDeniedException(String actionResult) {
            super(actionResult);
        }
    }
}
