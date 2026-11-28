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
package org.openhab.binding.atv.internal.client.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.capability.Audio;
import org.openhab.binding.atv.internal.client.capability.AudioListener;
import org.openhab.binding.atv.internal.client.dto.OutputDevice;
import org.openhab.binding.atv.internal.client.exceptions.ProtocolError;

/**
 * Relay implementation for audio functionality.
 *
 * <p>
 * Volume, output device and per-device volume updates are received via the {@link CoreStateDispatcher}
 * ({@link UpdatedState#VOLUME}, {@link UpdatedState#OUTPUT_DEVICES}, {@link UpdatedState#OUTPUT_DEVICE_VOLUME}),
 * translated into {@link AudioListener} callbacks and deduplicated (no update is forwarded when the state did not
 * change).
 *
 * <p>
 * For {@link UpdatedState#OUTPUT_DEVICE_VOLUME} the message value is an {@link OutputDevice} carrying identifier
 * and new volume.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class AudioRelay extends BaseRelay<Audio> implements Audio {

    private final ListenerRegistry<AudioListener> listeners;

    // Both fields are only mutated from dispatcher callbacks, which run on the device loop
    private double volume;
    private List<OutputDevice> outputDevices = new ArrayList<>();

    /**
     * Creates a new relay audio instance.
     *
     * @param guard device guard blocking calls after close
     * @param coreDispatcher per-device state dispatcher delivering volume/output device updates
     * @param loop device loop used for listener notification
     */
    public AudioRelay(Guard guard, CoreStateDispatcher coreDispatcher, DeviceLoop loop) {
        super(new Relayer<>(Audio.class, AppleTVRelay.DEFAULT_PRIORITIES), guard);
        this.listeners = new ListenerRegistry<>(loop);
        coreDispatcher.listenTo(UpdatedState.VOLUME, this::volumeChanged);
        coreDispatcher.listenTo(UpdatedState.OUTPUT_DEVICES, this::outputDevicesChanged);
        coreDispatcher.listenTo(UpdatedState.OUTPUT_DEVICE_VOLUME, this::outputDeviceVolumeChanged);
    }

    private void volumeChanged(StateMessage message) {
        double newLevel = ((Number) message.value()).doubleValue();

        // Compute new state so we can know if we should update or not
        double oldLevel = volume;
        volume = newLevel;

        // Do not update state in case it didn't change
        if (Double.compare(newLevel, oldLevel) != 0) {
            listeners.fire(listener -> listener.volumeUpdate(oldLevel, newLevel));
        }
    }

    @SuppressWarnings("unchecked")
    private void outputDevicesChanged(StateMessage message) {
        List<OutputDevice> newDevices = new ArrayList<>((List<OutputDevice>) message.value());

        // Compute new state so we can know if we should update or not
        List<OutputDevice> oldDevices = outputDevices;
        outputDevices = newDevices;

        // Do not update state in case it didn't change
        if (!newDevices.equals(oldDevices)) {
            listeners.fire(listener -> listener.outputDevicesUpdate(oldDevices, newDevices));
        }
    }

    private void outputDeviceVolumeChanged(StateMessage message) {
        OutputDevice deviceState = (OutputDevice) message.value();

        int index = -1;
        for (int i = 0; i < outputDevices.size(); i++) {
            if (outputDevices.get(i).identifier().equals(deviceState.identifier())) {
                index = i;
                break;
            }
        }

        OutputDevice existing = index >= 0 ? outputDevices.get(index) : new OutputDevice(deviceState.identifier());
        double oldVolume = existing.volume();
        double newVolume = deviceState.volume();
        OutputDevice updated = new OutputDevice(existing.identifier(), existing.name(), newVolume);
        if (index >= 0) {
            outputDevices.set(index, updated);
        }
        if (Double.compare(newVolume, oldVolume) != 0) {
            listeners.fire(listener -> listener.volumeDeviceUpdate(updated, oldVolume, newVolume));
        }
    }

    @Override
    public CompletableFuture<Void> volumeUp() {
        guard.requireNotBlocked("volumeUp");
        return relayAsync(Capability.AUDIO_VOLUME_UP, Audio::volumeUp);
    }

    @Override
    public CompletableFuture<Void> volumeDown() {
        guard.requireNotBlocked("volumeDown");
        return relayAsync(Capability.AUDIO_VOLUME_DOWN, Audio::volumeDown);
    }

    @Override
    public double volume() {
        guard.requireNotBlocked("volume");
        double level = relayer.relay(Capability.AUDIO_VOLUME).volume();
        if (level >= 0.0 && level <= 100.0) {
            return level;
        }
        throw new ProtocolError("volume " + level + " is out of range");
    }

    @Override
    public CompletableFuture<Void> setVolume(double level, @Nullable OutputDevice outputDevice) {
        guard.requireNotBlocked("setVolume");
        if (level >= 0.0 && level <= 100.0) {
            return relayAsync(Capability.AUDIO_SET_VOLUME, audio -> audio.setVolume(level, outputDevice));
        }
        return CompletableFuture.failedFuture(new ProtocolError("volume " + level + " is out of range"));
    }

    @Override
    public List<OutputDevice> outputDevices() {
        guard.requireNotBlocked("outputDevices");
        return relayer.relay(Capability.AUDIO_OUTPUT_DEVICES).outputDevices();
    }

    @Override
    public CompletableFuture<Void> addOutputDevices(List<String> devices) {
        guard.requireNotBlocked("addOutputDevices");
        return relayAsync(Capability.AUDIO_ADD_OUTPUT_DEVICES, audio -> audio.addOutputDevices(devices));
    }

    @Override
    public CompletableFuture<Void> removeOutputDevices(List<String> devices) {
        guard.requireNotBlocked("removeOutputDevices");
        return relayAsync(Capability.AUDIO_REMOVE_OUTPUT_DEVICES, audio -> audio.removeOutputDevices(devices));
    }

    @Override
    public CompletableFuture<Void> setOutputDevices(List<String> devices) {
        guard.requireNotBlocked("setOutputDevices");
        return relayAsync(Capability.AUDIO_SET_OUTPUT_DEVICES, audio -> audio.setOutputDevices(devices));
    }

    @Override
    public void addListener(AudioListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(AudioListener listener) {
        listeners.remove(listener);
    }
}
