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
package org.openhab.binding.atv.internal.client.protocols.mrp;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.capability.Audio;
import org.openhab.binding.atv.internal.client.capability.AudioListener;
import org.openhab.binding.atv.internal.client.core.Capability;
import org.openhab.binding.atv.internal.client.core.CapabilitySource;
import org.openhab.binding.atv.internal.client.core.ListenerRegistry;
import org.openhab.binding.atv.internal.client.core.ProtocolStateDispatcher;
import org.openhab.binding.atv.internal.client.core.UpdatedState;
import org.openhab.binding.atv.internal.client.dto.InputAction;
import org.openhab.binding.atv.internal.client.dto.OutputDevice;
import org.openhab.binding.atv.internal.client.exceptions.ProtocolError;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.DeviceInfoMessageOuterClass.DeviceInfoMessage;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.ProtocolMessageOuterClass.ProtocolMessage;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.VolumeControlAvailabilityMessageOuterClass.VolumeCapabilities;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.VolumeControlAvailabilityMessageOuterClass.VolumeControlAvailabilityMessage;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.VolumeControlCapabilitiesDidChangeMessageOuterClass.VolumeControlCapabilitiesDidChangeMessage;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.VolumeDidChangeMessageOuterClass.VolumeDidChangeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of audio functionality for MRP.
 *
 * <p>
 * Volume control availability and capabilities come from
 * {@code VOLUME_CONTROL_AVAILABILITY_MESSAGE} /
 * {@code VOLUME_CONTROL_CAPABILITIES_DID_CHANGE_MESSAGE}, volume changes from
 * {@code VOLUME_DID_CHANGE_MESSAGE}, and the output-device list is derived from grouped
 * devices in the device information messages.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class MrpAudio implements Audio, CapabilitySource {

    private static final Logger LOGGER = LoggerFactory.getLogger(MrpAudio.class);
    private static final long EVENT_TIMEOUT_SECONDS = 5;

    private final MrpProtocol protocol;
    private final ProtocolStateDispatcher stateDispatcher;
    private final ListenerRegistry<AudioListener> listeners;

    private volatile @Nullable ProtocolMessage latestDeviceInfo;
    private volatile boolean volumeControlsAvailable;
    private volatile boolean volumeControlsAbsolute;
    private volatile boolean volumeControlsRelative;
    private volatile double volume;
    private volatile List<OutputDevice> outputDevices = List.of();

    private final AsyncEvent volumeEvent = new AsyncEvent();
    private final AsyncEvent outputDevicesEvent = new AsyncEvent();

    /**
     * Creates a new audio instance.
     *
     * @param protocol protocol used for messages and updates
     * @param stateDispatcher dispatcher for internal state updates
     */
    public MrpAudio(MrpProtocol protocol, ProtocolStateDispatcher stateDispatcher) {
        this.protocol = protocol;
        this.stateDispatcher = stateDispatcher;
        this.listeners = new ListenerRegistry<>(protocol.loop());
        protocol.listenTo(ProtocolMessage.Type.VOLUME_CONTROL_AVAILABILITY_MESSAGE, this::volumeControlAvailability);
        protocol.listenTo(ProtocolMessage.Type.VOLUME_CONTROL_CAPABILITIES_DID_CHANGE_MESSAGE,
                this::volumeControlChanged);
        protocol.listenTo(ProtocolMessage.Type.VOLUME_DID_CHANGE_MESSAGE, this::volumeDidChange);
        protocol.listenTo(ProtocolMessage.Type.DEVICE_INFO_MESSAGE, this::updateOutputDevices);
        protocol.listenTo(ProtocolMessage.Type.DEVICE_INFO_UPDATE_MESSAGE, this::updateOutputDevices);
    }

    /**
     * Returns the UID of our device: the cluster id when part of a cluster, otherwise the
     * device UID from device information.
     *
     * <p>
     * The latest received {@code DEVICE_INFO_UPDATE_MESSAGE} is taken into account
     * (not just the connect-time device info) so cluster changes are observed.
     *
     * @return the UID or {@code null} when no device information has been received
     */
    public @Nullable String deviceUid() {
        @Nullable
        ProtocolMessage message = latestDeviceInfo;
        if (message == null) {
            message = protocol.deviceInfo().orElse(null);
        }
        if (message == null) {
            return null;
        }
        DeviceInfoMessage inner = (DeviceInfoMessage) MrpExtensions.extractInner(message);
        String clusterId = inner.getClusterID();
        return clusterId.isEmpty() ? inner.getDeviceUID() : clusterId;
    }

    /**
     * Returns if audio controls are available.
     */
    public boolean isAvailable() {
        return volumeControlsAvailable && deviceUid() != null;
    }

    /**
     * Returns if absolute volume control is available.
     */
    public boolean isVolumeAbsolute() {
        return volumeControlsAbsolute;
    }

    /**
     * Returns if relative volume control is available.
     */
    public boolean isVolumeRelative() {
        return volumeControlsRelative;
    }

    private void volumeControlAvailability(ProtocolMessage message) {
        updateVolumeControls((VolumeControlAvailabilityMessage) MrpExtensions.extractInner(message));
    }

    private void volumeControlChanged(ProtocolMessage message) {
        VolumeControlCapabilitiesDidChangeMessage inner = (VolumeControlCapabilitiesDidChangeMessage) MrpExtensions
                .extractInner(message);
        // Make sure update is for our device (in case it changed for someone else)
        if (inner.getOutputDeviceUID().equals(deviceUid())) {
            updateVolumeControls(inner.getCapabilities());
        }
    }

    private void updateVolumeControls(VolumeControlAvailabilityMessage availability) {
        volumeControlsAvailable = availability.getVolumeControlAvailable();
        volumeControlsAbsolute = availability.getVolumeCapabilities() == VolumeCapabilities.Enum.Absolute
                || availability.getVolumeCapabilities() == VolumeCapabilities.Enum.Both;
        volumeControlsRelative = availability.getVolumeCapabilities() == VolumeCapabilities.Enum.Relative
                || availability.getVolumeCapabilities() == VolumeCapabilities.Enum.Both;
        LOGGER.debug("Volume control availability changed to {}", volumeControlsAvailable);
    }

    private void volumeDidChange(ProtocolMessage message) {
        VolumeDidChangeMessage inner = (VolumeDidChangeMessage) MrpExtensions.extractInner(message);

        // Make sure update is for our device (in case it changed for someone else)
        if (inner.getOutputDeviceUID().equals(deviceUid())) {
            double oldVolume = volume;
            volume = roundToOneDecimal(inner.getVolume() * 100.0);
            LOGGER.debug("Volume changed to {}", volume);
            stateDispatcher.dispatch(UpdatedState.VOLUME, volume);
            double newVolume = volume;
            listeners.fire(listener -> listener.volumeUpdate(oldVolume, newVolume));
        } else {
            double deviceVolume = roundToOneDecimal(inner.getVolume() * 100.0);
            LOGGER.debug("Volume changed to {} for output device {}", deviceVolume, inner.getOutputDeviceUID());
            stateDispatcher.dispatch(UpdatedState.OUTPUT_DEVICE_VOLUME,
                    new OutputDevice(inner.getOutputDeviceUID(), null, deviceVolume));
        }

        // There are no responses to the volume_up/down commands sent to the device, so
        // callers wait for the volume to change instead. Note only one event is tracked,
        // so a rapid second change can be missed by a waiter.
        volumeEvent.set();
        volumeEvent.clear();
    }

    private static double roundToOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    @Override
    public double volume() {
        return volume;
    }

    @Override
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public CompletableFuture<Void> setVolume(double level, @Nullable OutputDevice outputDevice) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        Thread.ofVirtual().name("mrp-set-volume").start(() -> {
            try {
                CompletableFuture<@Nullable Void> event = volumeEvent.waitFuture();
                if (outputDevice == null) {
                    @Nullable
                    String uid = deviceUid();
                    if (uid == null) {
                        throw new ProtocolError("no output device");
                    }
                    protocol.send(MrpMessages.setVolume(uid, (float) (level / 100.0))).join();
                    if (isVolumeAbsolute() && volume != level) {
                        event.get(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                    }
                } else {
                    protocol.send(MrpMessages.setVolume(outputDevice.identifier(), (float) (level / 100.0))).join();
                    if (outputDevice.volume() != level) {
                        event.get(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                    }
                }
                MrpFutures.completeVoid(result);
            } catch (Throwable t) {
                result.completeExceptionally(t);
            }
        });
        return result;
    }

    @Override
    public CompletableFuture<Void> volumeUp() {
        return volumeStep("volume_up", 100.0, 5.0);
    }

    @Override
    public CompletableFuture<Void> volumeDown() {
        return volumeStep("volume_down", 0.0, -5.0);
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    private CompletableFuture<Void> volumeStep(String key, double limit, double step) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        Thread.ofVirtual().name("mrp-" + key).start(() -> {
            try {
                if (isVolumeAbsolute() && volume == limit) {
                    MrpFutures.completeVoid(result);
                    return;
                }
                if (isVolumeRelative()) {
                    CompletableFuture<@Nullable Void> event = volumeEvent.waitFuture();
                    MrpRemoteControl.sendHidKey(protocol, key, InputAction.SingleTap, false).join();
                    if (isVolumeAbsolute()) {
                        event.get(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                    }
                } else if (isVolumeAbsolute()) {
                    double target = step > 0 ? Math.min(volume + step, 100.0) : Math.max(volume + step, 0.0);
                    setVolume(target, null).join();
                }
                MrpFutures.completeVoid(result);
            } catch (Throwable t) {
                result.completeExceptionally(t);
            }
        });
        return result;
    }

    private void updateOutputDevices(ProtocolMessage message) {
        latestDeviceInfo = message;
        DeviceInfoMessage inner = (DeviceInfoMessage) MrpExtensions.extractInner(message);
        List<OutputDevice> devices = new ArrayList<>();
        if (inner.getIsGroupLeader() && !inner.getIsProxyGroupPlayer()) {
            devices.add(new OutputDevice(inner.getUniqueIdentifier(), inner.getName(), 0.0));
        }
        for (DeviceInfoMessage device : inner.getGroupedDevicesList()) {
            devices.add(new OutputDevice(device.getDeviceUID(), device.getName(), 0.0));
        }
        List<OutputDevice> oldDevices = outputDevices;
        outputDevices = List.copyOf(devices);
        outputDevicesEvent.set();
        outputDevicesEvent.clear();
        stateDispatcher.dispatch(UpdatedState.OUTPUT_DEVICES, outputDevices);
        List<OutputDevice> newDevices = outputDevices;
        listeners.fire(listener -> listener.outputDevicesUpdate(oldDevices, newDevices));
    }

    @Override
    public List<OutputDevice> outputDevices() {
        return outputDevices;
    }

    @Override
    public CompletableFuture<Void> addOutputDevices(List<String> devices) {
        return modifyOutputDevices(MrpMessages.addOutputDevices(devices));
    }

    @Override
    public CompletableFuture<Void> removeOutputDevices(List<String> devices) {
        return modifyOutputDevices(MrpMessages.removeOutputDevices(devices));
    }

    @Override
    public CompletableFuture<Void> setOutputDevices(List<String> devices) {
        return modifyOutputDevices(MrpMessages.setOutputDevices(devices));
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    private CompletableFuture<Void> modifyOutputDevices(ProtocolMessage message) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        Thread.ofVirtual().name("mrp-output-devices").start(() -> {
            try {
                CompletableFuture<@Nullable Void> event = outputDevicesEvent.waitFuture();
                protocol.send(message).join();
                try {
                    event.get(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                } catch (java.util.concurrent.TimeoutException ignored) {
                    // timeout is not an error here; the request was still sent
                }
                MrpFutures.completeVoid(result);
            } catch (Throwable t) {
                result.completeExceptionally(t);
            }
        });
        return result;
    }

    @Override
    public void addListener(AudioListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(AudioListener listener) {
        listeners.remove(listener);
    }

    @Override
    public Set<Capability> capabilities() {
        return Set.of(Capability.AUDIO_VOLUME, Capability.AUDIO_SET_VOLUME, Capability.AUDIO_VOLUME_UP,
                Capability.AUDIO_VOLUME_DOWN, Capability.AUDIO_OUTPUT_DEVICES, Capability.AUDIO_ADD_OUTPUT_DEVICES,
                Capability.AUDIO_REMOVE_OUTPUT_DEVICES, Capability.AUDIO_SET_OUTPUT_DEVICES);
    }

    /** Test hook to pre-set the output-devices event. */
    void setOutputDevicesEventForTesting() {
        outputDevicesEvent.set();
    }
}
