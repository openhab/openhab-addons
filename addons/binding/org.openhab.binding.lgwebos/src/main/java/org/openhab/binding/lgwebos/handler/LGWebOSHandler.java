/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lgwebos.handler;

import static org.openhab.binding.lgwebos.LGWebOSBindingConstants.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.lgwebos.internal.ChannelHandler;
import org.openhab.binding.lgwebos.internal.LauncherApplication;
import org.openhab.binding.lgwebos.internal.MediaControlPlayer;
import org.openhab.binding.lgwebos.internal.MediaControlStop;
import org.openhab.binding.lgwebos.internal.PowerControlPower;
import org.openhab.binding.lgwebos.internal.TVControlChannel;
import org.openhab.binding.lgwebos.internal.TVControlChannelName;
import org.openhab.binding.lgwebos.internal.ToastControlToast;
import org.openhab.binding.lgwebos.internal.VolumeControlMute;
import org.openhab.binding.lgwebos.internal.VolumeControlVolume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.connectsdk.device.ConnectableDevice;
import com.connectsdk.device.ConnectableDeviceListener;
import com.connectsdk.discovery.DiscoveryManager;
import com.connectsdk.discovery.DiscoveryManagerListener;
import com.connectsdk.service.DeviceService;
import com.connectsdk.service.DeviceService.PairingType;
import com.connectsdk.service.command.ServiceCommandError;

/**
 * The {@link LGWebOSHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Sebastian Prehn - initial contribution
 */
public class LGWebOSHandler extends BaseThingHandler implements ConnectableDeviceListener, DiscoveryManagerListener {

    private final Logger logger = LoggerFactory.getLogger(LGWebOSHandler.class);
    private final DiscoveryManager discoveryManager;

    // ChannelID to CommandHandler Map
    private final Map<String, ChannelHandler> channelHandlers;

    public LGWebOSHandler(@NonNull Thing thing, DiscoveryManager discoveryManager) {
        super(thing);
        this.discoveryManager = discoveryManager;
        Map<String, ChannelHandler> handlers = new HashMap<>();
        handlers.put(CHANNEL_VOLUME, new VolumeControlVolume());
        handlers.put(CHANNEL_POWER, new PowerControlPower());
        handlers.put(CHANNEL_MUTE, new VolumeControlMute());
        handlers.put(CHANNEL_CHANNEL, new TVControlChannel());
        handlers.put(CHANNEL_CHANNEL_NAME, new TVControlChannelName());
        handlers.put(CHANNEL_APP_LAUNCHER, new LauncherApplication());
        handlers.put(CHANNEL_MEDIA_STOP, new MediaControlStop());
        handlers.put(CHANNEL_TOAST, new ToastControlToast());
        handlers.put(CHANNEL_MEDIA_PLAYER, new MediaControlPlayer());
        channelHandlers = Collections.unmodifiableMap(handlers);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand({},{}) is called", channelUID, command);
        ChannelHandler handler = channelHandlers.get(channelUID.getId());
        if (handler == null) {
            logger.warn(
                    "Unable to handle command {}. No handler found for channel {}. This must not happen. Please report as a bug.",
                    command, channelUID);
            return;
        }
        Optional<ConnectableDevice> device = getDevice();
        if (!device.isPresent()) {
            logger.debug("Device {} not found - most likely is is currently offline. Details: Channel {}, Command {}.",
                    getDeviceId(), channelUID.getId(), command);
        }
        handler.onReceiveCommand(device.orElse(null), channelUID.getId(), this, command);
    }

    private Optional<ConnectableDevice> getDevice() {
        String id = getDeviceId();
        return discoveryManager.getCompatibleDevices().values().stream().filter(device -> id.equals(device.getId()))
                .findFirst();
    }

    @Override
    public void initialize() {
        discoveryManager.addListener(this);

        Optional<ConnectableDevice> device = getDevice();
        if (!device.isPresent()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "TV is off");
        } else {
            device.get().addListener(this);
            if (device.get().isConnected()) {
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Connected");
            } else {
                updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "Connecting ...");
                device.get().connect();
                // on success onDeviceReady will be called,
                // if pairing is required onPairingRequired,
                // otherwise onConnectionFailed
            }
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        getDevice().ifPresent(device -> device.removeListener(this));
        discoveryManager.removeListener(this);
    }
    // Connectable Device Listener

    @Override
    public void onDeviceReady(ConnectableDevice device) { // this gets called on connection success
        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Connected");
        refreshAllChannelSubscriptions(device);
        channelHandlers.forEach((k, v) -> v.onDeviceReady(device, k, this));
    }

    @Override
    public void onDeviceDisconnected(ConnectableDevice device) {
        logger.debug("Device disconnected: {}", device);
        for (Map.Entry<String, ChannelHandler> e : channelHandlers.entrySet()) {
            e.getValue().onDeviceRemoved(device, e.getKey(), this);
            e.getValue().removeAnySubscription(device);
        }
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "TV is off");
    }

    @Override
    public void onPairingRequired(ConnectableDevice device, DeviceService service, PairingType pairingType) {
        updateStatus(thing.getStatus(), ThingStatusDetail.CONFIGURATION_PENDING, "Pairing Required");
    }

    @Override
    public void onCapabilityUpdated(ConnectableDevice device, List<String> added, List<String> removed) {
        logger.debug("Capabilities updated: {} - added: {} - removed: {}", device, added, removed);
        refreshAllChannelSubscriptions(device);
    }

    @Override
    public void onConnectionFailed(ConnectableDevice device, ServiceCommandError error) {
        logger.debug("Connection failed: {} - error: {}", device, error.getMessage());
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "Connection Failed: " + error.getMessage());
    }

    // callback methods for commandHandlers
    public void postUpdate(String channelUID, State state) {
        updateState(channelUID, state);
    }

    public boolean isChannelInUse(String channelId) {
        return isLinked(channelId);
    }

    // channel linking modifications

    @Override
    public void channelLinked(ChannelUID channelUID) {
        refreshChannelSubscription(channelUID);
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        refreshChannelSubscription(channelUID);
    }

    // private helpers

    /**
     * Refresh channel subscription for one specific channel.
     *
     * @param channelUID must not be <code>null</code>
     */
    private void refreshChannelSubscription(ChannelUID channelUID) {
        String channelId = channelUID.getId();
        getDevice().ifPresent(device -> {
            // may be called even if the device is not currently connected
            if (device.isConnected()) {
                channelHandlers.get(channelId).refreshSubscription(device, channelId, this);
            }
        });
    }

    /**
     * Refresh channel subscriptions on all handlers.
     *
     * @param device must not be <code>null</code>
     */
    private void refreshAllChannelSubscriptions(ConnectableDevice device) {
        channelHandlers.forEach((k, v) -> v.refreshSubscription(device, k, this));
    }

    // just to make sure, this device is registered, if it was powered off during initialization
    @Override
    public void onDeviceAdded(DiscoveryManager manager, ConnectableDevice device) {
        String id = getDeviceId();
        if (device.getId().equals(id)) {
            device.addListener(this);
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Device Ready");
            device.connect();
        }
    }

    @Override
    public void onDeviceUpdated(DiscoveryManager manager, ConnectableDevice device) {
        String id = getDeviceId();
        if (device.getId().equals(id)) {
            device.addListener(this);
        }
    }

    private String getDeviceId() {
        return getConfig().get(PROPERTY_DEVICE_ID).toString();
    }

    @Override
    public void onDeviceRemoved(DiscoveryManager manager, ConnectableDevice device) {
        // NOP
    }

    @Override
    public void onDiscoveryFailed(DiscoveryManager manager, ServiceCommandError error) {
        // NOP
    }
}
