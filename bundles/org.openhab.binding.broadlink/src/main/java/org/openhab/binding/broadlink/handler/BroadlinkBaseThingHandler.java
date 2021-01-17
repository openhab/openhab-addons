/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.broadlink.handler;

import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.broadlink.internal.*;
import org.openhab.binding.broadlink.internal.BroadlinkConfiguration;
import org.openhab.binding.broadlink.internal.discovery.DeviceRediscoveryAgent;
import org.openhab.binding.broadlink.internal.discovery.DeviceRediscoveryListener;
import org.openhab.binding.broadlink.internal.socket.NetworkTrafficObserver;
import org.openhab.binding.broadlink.internal.socket.RetryableSocket;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;

/**
 * Abstract superclass of all supported Broadlink devices.
 *
 * @author John Marshall/Cato Sognen - Initial contribution
 */
@NonNullByDefault
public abstract class BroadlinkBaseThingHandler extends BaseThingHandler implements DeviceRediscoveryListener {

    private static final String INITIAL_DEVICE_ID = "00000000";

    @Nullable
    private RetryableSocket socket;

    @Nullable // Can be injected for test purposes
    private NetworkTrafficObserver networkTrafficObserver;

    private int count;

    protected BroadlinkConfiguration thingConfig;
    protected final ThingLogger thingLogger;
    @Nullable
    private ScheduledFuture<?> refreshHandle;
    private boolean authenticated = false;
    // These get handed to us by the device after successful authentication:
    private byte[] deviceId;
    private byte[] deviceKey;

    public BroadlinkBaseThingHandler(Thing thing, Logger logger) {
        super(thing);
        this.thingLogger = new ThingLogger(thing, logger);
        this.thingConfig = getConfigAs(BroadlinkConfiguration.class);
        count = (new Random()).nextInt(65535);

        thingLogger.logInfo(String.format("constructed: resetting deviceKey to '%s', length %d",
                BroadlinkBindingConstants.BROADLINK_AUTH_KEY, BroadlinkBindingConstants.BROADLINK_AUTH_KEY.length()));
        thingLogger.logInfo("(HINT: this should start '0976', end '8b02' and have length 32)");
        this.deviceId = HexUtils.hexToBytes(INITIAL_DEVICE_ID);
        this.deviceKey = HexUtils.hexToBytes(BroadlinkBindingConstants.BROADLINK_AUTH_KEY);

        this.socket = new RetryableSocket(thingConfig, thingLogger);
    }

    // For test purposes
    void setSocket(RetryableSocket socket) {
        this.socket = socket;
    }

    void setNetworkTrafficObserver(NetworkTrafficObserver networkTrafficObserver) {
        this.networkTrafficObserver = networkTrafficObserver;
    }

    private boolean hasAuthenticated() {
        return authenticated;
    }

    public void initialize() {
        thingLogger.logDebug("initializing polling");

        updateItemStatus();

        if (thingConfig.getPollingInterval() != 0) {
            refreshHandle = scheduler.scheduleWithFixedDelay(new Runnable() {

                public void run() {
                    updateItemStatus();
                }
            }, 1L, thingConfig.getPollingInterval(), TimeUnit.SECONDS);
        }
    }

    public void thingUpdated(Thing thing) {
        thingLogger.logDebug("thingUpdated");
        forceOffline(ThingStatusDetail.CONFIGURATION_PENDING, "Thing has been updated, will reconnect soon");
        // Refetch the config NOW before we come back up again...
        this.thingConfig = getConfigAs(BroadlinkConfiguration.class);
    }

    public void dispose() {
        thingLogger.logDebug(getThing().getLabel() + " is being disposed");
        if (refreshHandle != null && !refreshHandle.isDone()) {
            thingLogger.logDebug("Cancelling refresh task");
            boolean cancelled = refreshHandle.cancel(true);
            thingLogger.logDebug("Cancellation successful: " + cancelled);
        }
        if (socket != null) {
            socket.close();
        }
        super.dispose();
    }

    private boolean authenticate() {
        thingLogger.logTrace("Authenticating");
        authenticated = false;
        // When authenticating, we must ALWAYS use the initial values
        this.deviceId = HexUtils.hexToBytes(INITIAL_DEVICE_ID);
        this.deviceKey = HexUtils.hexToBytes(BroadlinkBindingConstants.BROADLINK_AUTH_KEY);

        try {
            byte authRequest[] = buildMessage((byte) 0x65, BroadlinkProtocol.buildAuthenticationPayload(), -1);
            byte response[] = sendAndReceiveDatagram(authRequest, "authentication");
            byte decryptResponse[] = decodeDevicePacket(response);
            this.deviceId = BroadlinkProtocol.getDeviceId(decryptResponse);
            this.deviceKey = BroadlinkProtocol.getDeviceKey(decryptResponse);

            // Update the properties, so that these values can be seen in the UI:
            Map<String, String> properties = editProperties();
            properties.put("id", HexUtils.bytesToHex(deviceId));
            properties.put("key", HexUtils.bytesToHex(deviceKey));
            updateProperties(properties);
            thingLogger.logDebug(String.format("Authenticated with id '%s' and key '%s'", HexUtils.bytesToHex(deviceId),
                    HexUtils.bytesToHex(deviceKey)));
            authenticated = true;
            return true;
        } catch (Exception e) {
            thingLogger.logError("Authentication failed: ", e);
            return false;
        }
    }

    protected byte[] sendAndReceiveDatagram(byte message[], String purpose) {
        return socket.sendAndReceive(message, purpose);
    }

    protected byte[] buildMessage(byte command, byte payload[]) throws IOException {
        return buildMessage(command, payload, thingConfig.getDeviceType());
    }

    private byte[] buildMessage(byte command, byte payload[], int deviceType) throws IOException {
        count = count + 1 & 0xffff;

        if (networkTrafficObserver != null) {
            networkTrafficObserver.onCommandSent(command);
            networkTrafficObserver.onBytesSent(payload);
        }

        return BroadlinkProtocol.buildMessage(command, payload, count, thingConfig.getMAC(), deviceId,
                HexUtils.hexToBytes(BroadlinkBindingConstants.BROADLINK_IV), deviceKey, deviceType);
    }

    protected byte[] decodeDevicePacket(byte[] responseBytes) throws IOException {
        byte[] rxBytes = BroadlinkProtocol.decodePacket(responseBytes, this.deviceKey,
                BroadlinkBindingConstants.BROADLINK_IV);

        if (networkTrafficObserver != null) {
            networkTrafficObserver.onBytesReceived(rxBytes);
        }
        return rxBytes;
    }

    public void handleCommand(ChannelUID channelUID, Command command) {
        thingLogger.logDebug("handleCommand " + command.toString());
        if (command instanceof RefreshType) {
            thingLogger.logTrace("Refresh requested, updating item status ...");

            updateItemStatus();
        }
    }

    // Can be implemented by devices that should do something on being found; e.g. perform a first status query
    protected boolean onBroadlinkDeviceBecomingReachable() {
        return true;
    }

    // Implemented by devices that can update the openHAB state
    // model. Return false if something went wrong that requires
    // a change in the device's online state
    protected boolean getStatusFromDevice() {
        return true;
    }

    public void updateItemStatus() {
        thingLogger.logTrace("updateItemStatus; checking host availability at " + thingConfig.getIpAddress());
        if (NetworkUtils.hostAvailabilityCheck(thingConfig.getIpAddress(), 3000)) {
            thingLogger.logTrace("updateItemStatus; host found at " + thingConfig.getIpAddress());
            if (!Utils.isOnline(getThing())) {
                thingLogger.logTrace("updateItemStatus; device not currently online, resolving");
                transitionToOnline();
            } else {
                // Normal operation ...
                boolean gotStatusOk = getStatusFromDevice();
                if (!gotStatusOk) {
                    if (thingConfig.isIgnoreFailedUpdates()) {
                        thingLogger.logWarn(
                                "Problem getting status. Not marking offline because configured to ignore failed updates ...");
                    } else {
                        thingLogger.logError("Problem getting status. Marking as offline ...");
                        forceOffline(ThingStatusDetail.GONE, "Problem getting status");
                    }
                }
            }
        } else {
            if (thingConfig.isStaticIp()) {
                if (!Utils.isOffline(getThing())) {
                    thingLogger.logDebug("Statically-IP-addressed device not found at " + thingConfig.getIpAddress());
                    forceOffline(ThingStatusDetail.NONE, "Couldn't find statically-IP-addressed device");
                }
            } else {
                thingLogger.logDebug(
                        String.format("Dynamic IP device not found at %s, will search...", thingConfig.getIpAddress()));
                DeviceRediscoveryAgent dra = new DeviceRediscoveryAgent(thingConfig, this);
                dra.attemptRediscovery();
                thingLogger.logDebug("Asynchronous dynamic IP device search initiated...");
            }
        }
    }

    public void onDeviceRediscovered(String newIpAddress) {
        thingLogger.logInfo("Rediscovered this device at IP " + newIpAddress);
        thingConfig.setIpAddress(newIpAddress);
        transitionToOnline();
    }

    public void onDeviceRediscoveryFailure() {
        if (!Utils.isOffline(getThing())) {
            thingLogger.logDebug("Dynamically-IP-addressed device not found after network scan. Marking offline");
            forceOffline(ThingStatusDetail.NONE, "Couldn't rediscover device");
        }
    }

    private void transitionToOnline() {
        if (!hasAuthenticated()) {
            thingLogger.logDebug(
                    "We've never actually successfully authenticated with this device in this session. Doing so now");
            if (authenticate()) {
                thingLogger.logDebug("Authenticated with newly-detected device, will now get its status");
            } else {
                thingLogger.logError(
                        "Attempting to authenticate prior to getting device status FAILED. Will mark as offline");
                forceOffline(ThingStatusDetail.COMMUNICATION_ERROR, "Couldn't authenticate");
                return;
            }
        }
        if (onBroadlinkDeviceBecomingReachable()) {
            thingLogger.logDebug("Offline -> Online");
            updateStatus(ThingStatus.ONLINE);
        } else {
            thingLogger.logError("Device became reachable but had trouble getting status. Marking as offline ...");
            forceOffline(ThingStatusDetail.COMMUNICATION_ERROR, "Trouble getting status");
        }
    }

    private void forceOffline(ThingStatusDetail detail, String reason) {
        thingLogger.logWarn("Online -> Offline due to: " + reason);
        authenticated = false; // This session is dead; we'll need to re-authenticate next time
        updateStatus(ThingStatus.OFFLINE, detail, reason);
        if (socket != null) {
            socket.close();
        }
    }
}
