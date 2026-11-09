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
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

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
 * @author Oleksandr Mishchuk - Initial contribution
 */
@NonNullByDefault
public class MotionBlindsHandler extends BaseThingHandler implements MessageListener {

    private static final int MIN_REFRESH_INTERVAL = 30;
    private static final int MAX_ANGLE = 180;
    private static final int IP_RESOLUTION_TIMEOUT_MS = 3000;
    private static final int MAX_TILT_MOVES = 2;
    private static final int STOP_CHECK_INTERVAL_MS = 1000;
    private static final int MAX_STOP_CHECKS = 6;

    private final Logger logger = LoggerFactory.getLogger(MotionBlindsHandler.class);
    private final Gson gson = new Gson();
    private final MotionBlindsCommunicationManager communicationManager;
    private final Object requestLock = new Object();
    private final Object statusLock = new Object();
    private final AtomicLong movementGeneration = new AtomicLong();

    private MotionBlindsConfiguration config = new MotionBlindsConfiguration();
    private String mac = "";
    private volatile String ipAddress = "";
    private volatile @Nullable CompletableFuture<String> ipResolution;
    private volatile String deviceType = "";
    private volatile @Nullable String token;
    private volatile @Nullable String accessToken;
    private boolean accessDenied;
    private @Nullable ScheduledFuture<?> pollJob;
    // Refresh, write and tilt tasks submitted to the scheduler, canceled on dispose
    private final Set<Future<?>> tasks = ConcurrentHashMap.newKeySet();
    private volatile @Nullable Object session;
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
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "@text/offline.config-error-invalid-mac");
            return;
        }
        if (!AccessTokenCalculator.isValidKey(config.key)) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "@text/offline.config-error-invalid-key");
            return;
        }

        String type = thing.getProperties().get(PROPERTY_DEVICE_TYPE);
        deviceType = type != null ? type : "";
        token = null;
        accessToken = null;
        slatTracker = new SlatTracker(config.tiltTravel, config.tiltSlack);

        Object currentSession = new Object();
        synchronized (statusLock) {
            session = currentSession;
            accessDenied = false;
            updateStatus(ThingStatus.UNKNOWN);
        }
        communicationManager.registerListener(mac, this);
        pollJob = scheduler.scheduleWithFixedDelay(() -> poll(currentSession), 0,
                Math.max(MIN_REFRESH_INTERVAL, config.refreshInterval), TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        // waits for an update of the Thing that is running, for example from a message the communication manager
        // dispatched before the listener is unregistered below; later updates see that the session ended
        synchronized (statusLock) {
            session = null;
        }
        ScheduledFuture<?> job = pollJob;
        if (job != null) {
            job.cancel(true);
            pollJob = null;
        }
        tasks.forEach(task -> task.cancel(true));
        tasks.clear();
        communicationManager.unregisterListener(mac, this);
        token = null;
        accessToken = null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Object currentSession = session;
        if (currentSession == null) {
            logger.debug("Ignoring command {} for channel {}: motor {} is not initialized", command, channelUID, mac);
            return;
        }
        if (command instanceof RefreshType) {
            submit(() -> poll(currentSession));
            return;
        }

        if (CHANNEL_TILT.equals(channelUID.getId())) {
            Integer tilt = tiltPercent(command);
            if (tilt == null) {
                logger.debug("Unsupported command {} for channel {}", command, channelUID);
                return;
            }
            long generation = movementGeneration.incrementAndGet();
            submit(() -> tilt(currentSession, tilt, generation));
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
        long generation = movementGeneration.incrementAndGet();
        submit(() -> writeIfCurrent(currentSession, generation, data));
    }

    private void submit(Runnable task) {
        tasks.removeIf(Future::isDone);
        tasks.add(scheduler.submit(task));
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
     * Turn the slats to the given tilt at the current height, see {@link SlatTracker}.
     * From fully lowered this can take two moves.
     */
    private void tilt(Object session, int tiltPercent, long generation) {
        for (int move = 1; move <= MAX_TILT_MOVES; move++) {
            Map<String, Object> data = slatTracker.tiltCommand(tiltPercent);
            if (data == null) {
                poll(session);
                data = slatTracker.tiltCommand(tiltPercent);
            }
            if (data == null) {
                logger.debug("Unable to tilt motor {}: position is not known", mac);
                return;
            }
            if (data.isEmpty()) {
                return;
            }
            if (!writeIfCurrent(session, generation, data)) {
                return;
            }
            if (move == MAX_TILT_MOVES || thing.getStatus() != ThingStatus.ONLINE
                    || !waitUntilStopped(session, generation)) {
                return;
            }
        }
    }

    /**
     * Send a movement unless a newer movement command arrived
     **/
    private boolean writeIfCurrent(Object session, long generation, Map<String, Object> data) {
        synchronized (requestLock) {
            if (generation != movementGeneration.get()) {
                logger.debug("Command {} to motor {} is superseded by a newer command", data, mac);
                return false;
            }
            write(session, data);
            return true;
        }
    }

    /**
     * Read the status until the motor angle does not change anymore.
     *
     * @return {@code false} if the tilt was superseded or interrupted, the status could not be read, or the motor
     *         was still moving after all checks: a tilt move takes 1-2 seconds, so the blind is then moved by
     *         something else (remote, app) and a further move would interfere with it
     */
    private boolean waitUntilStopped(Object session, long generation) {
        Integer previousAngle = null;
        for (int check = 0; check < MAX_STOP_CHECKS; check++) {
            try {
                Thread.sleep(STOP_CHECK_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
            if (generation != movementGeneration.get()) {
                logger.debug("Tilt of motor {} is superseded by a newer command", mac);
                return false;
            }
            if (!poll(session)) {
                return false;
            }
            Integer angle = slatTracker.getMotorAngle();
            if (angle != null && angle.equals(previousAngle)) {
                return true;
            }
            previousAngle = angle;
        }
        logger.debug("Motor {} is still moving after {} checks, tilt is not continued", mac, MAX_STOP_CHECKS);
        return false;
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

    private boolean poll(Object session) {
        try {
            request(session, MSG_READ_DEVICE, null);
            return true;
        } catch (IOException e) {
            requestFailed(session, e);
            return false;
        }
    }

    private void write(Object session, Map<String, Object> data) {
        try {
            request(session, MSG_WRITE_DEVICE, data);
        } catch (IOException e) {
            requestFailed(session, e);
        }
    }

    private void requestFailed(Object session, IOException e) {
        boolean published = publish(session, () -> {
            if (e instanceof AccessDeniedException) {
                accessDenied = true;
                updateAccessDeniedStatus();
            } else {
                updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        });
        if (!published) {
            logger.debug("Request to motor {} abandoned after dispose: {}", mac, e.getMessage());
        }
    }

    private boolean publish(Object session, Runnable update) {
        synchronized (statusLock) {
            if (!session.equals(this.session)) {
                return false;
            }
            update.run();
            return true;
        }
    }

    private void authenticated() {
        accessDenied = false;
        updateStatus(ThingStatus.ONLINE);
    }

    private void reachable() {
        if (accessDenied) {
            updateAccessDeniedStatus();
        } else {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    private void updateAccessDeniedStatus() {
        updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "@text/offline.config-error-access-denied");
    }

    private void ensureCurrent(Object session) throws IOException {
        if (!session.equals(this.session)) {
            throw new IOException("Handler of motor " + mac + " was disposed");
        }
    }

    private MotionBlindsMessage request(Object session, String msgType, @Nullable Map<String, Object> data)
            throws IOException {
        synchronized (requestLock) {
            for (int attempt = 1;; attempt++) {
                String currentAccessToken = getAccessToken(session);
                MotionBlindsMessage response = send(session,
                        new MotionBlindsRequest(msgType, mac, deviceType, currentAccessToken, data));
                // an answer that arrives after a dispose must not update the Thing anymore
                ensureCurrent(session);
                String actionResult = response.actionResult;
                if (actionResult == null) {
                    // the acknowledge of a WriteDevice carries the state before the command, it may arrive after
                    // the first Report of the movement and must not roll the state back
                    boolean carriesStatus = !MSG_WRITE_DEVICE.equals(msgType);
                    publish(session, () -> {
                        if (carriesStatus) {
                            handleStatus(response);
                        }
                        authenticated();
                    });
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

    private MotionBlindsMessage send(Object session, MotionBlindsRequest request) throws IOException {
        ensureCurrent(session);
        String currentIp = ipAddress;
        if (currentIp.isEmpty()) {
            String resolvedIp = resolveIpAddress();
            ensureCurrent(session);
            return communicationManager.sendRequest(resolvedIp, request, mac);
        }
        try {
            // only this motor's answer is accepted, so another device at a stale address leads to a new search
            return communicationManager.sendRequest(currentIp, request, mac);
        } catch (IOException e) {
            ensureCurrent(session);
            String resolvedIp;
            try {
                resolvedIp = resolveIpAddress();
            } catch (IOException resolveException) {
                throw e;
            }
            if (resolvedIp.equals(currentIp)) {
                throw e;
            }
            ensureCurrent(session);
            return communicationManager.sendRequest(resolvedIp, request, mac);
        }
    }

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

    private String getAccessToken(Object session) throws IOException {
        String current = accessToken;
        if (current != null) {
            return current;
        }
        String currentToken = token;
        if (currentToken == null || deviceType.isEmpty()) {
            currentToken = fetchDeviceList(session);
        }
        try {
            current = AccessTokenCalculator.calculate(config.key, currentToken);
        } catch (IllegalArgumentException e) {
            throw new IOException("Unable to calculate access token: " + e.getMessage());
        }
        accessToken = current;
        return current;
    }

    private String fetchDeviceList(Object session) throws IOException {
        MotionBlindsMessage response = send(session, new MotionBlindsRequest(MSG_GET_DEVICE_LIST));
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
        String deviceTypeProperty = type;
        publish(session, () -> {
            updateProperty(PROPERTY_DEVICE_TYPE, deviceTypeProperty);
            String protocolVersion = response.protocolVersion;
            if (protocolVersion != null) {
                updateProperty(PROPERTY_PROTOCOL_VERSION, protocolVersion);
            }
            String firmwareVersion = response.fwVersion;
            if (firmwareVersion != null) {
                updateProperty(Thing.PROPERTY_FIRMWARE_VERSION, firmwareVersion);
            }
        });
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

    @Override
    public void onMessage(MotionBlindsMessage message, String sourceIp) {
        // the communication manager may deliver a message it dispatched before the listener was unregistered
        Object currentSession = session;
        if (currentSession == null || message.actionResult != null) {
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
            publish(currentSession, () -> {
                handleStatus(message);
                reachable();
            });
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

    private static class AccessDeniedException extends IOException {
        private static final long serialVersionUID = 1L;

        AccessDeniedException(String actionResult) {
            super(actionResult);
        }
    }
}
