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
package org.openhab.binding.atv.internal.client.capability;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.dto.OutputDevice;
import org.openhab.binding.atv.internal.client.exceptions.NotSupportedError;

/**
 * API for audio functionality.
 *
 * <p>
 * Volume level is managed in percent where 0 is muted and 100 is max volume. Listener
 * interface: {@link AudioListener}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public interface Audio {

    /**
     * Returns the current volume level in percent, i.e. [0.0-100.0].
     *
     * @return current volume level
     * @throws NotSupportedError if not supported
     */
    default double volume() {
        throw new NotSupportedError("volume is not supported");
    }

    /**
     * Changes the current volume level.
     *
     * @param level new volume in percent, i.e. [0.0-100.0]
     * @param outputDevice output device to change volume on, or {@code null} for the main device
     * @return future completing when volume has been changed
     */
    default CompletableFuture<Void> setVolume(double level, @Nullable OutputDevice outputDevice) {
        return CompletableFuture.failedFuture(new NotSupportedError("setVolume is not supported"));
    }

    /**
     * Changes the current volume level on the main device.
     *
     * @param level new volume in percent, i.e. [0.0-100.0]
     * @return future completing when volume has been changed
     */
    default CompletableFuture<Void> setVolume(double level) {
        return setVolume(level, null);
    }

    /**
     * Increases volume by one step.
     *
     * <p>
     * Step size is device dependent, but usually around 2.5% of the total volume range. It is not necessarily
     * linear. The future completes when the volume change has been acknowledged by the device (when possible and
     * supported).
     *
     * @return future completing when volume has been changed
     */
    default CompletableFuture<Void> volumeUp() {
        return CompletableFuture.failedFuture(new NotSupportedError("volumeUp is not supported"));
    }

    /**
     * Decreases volume by one step.
     *
     * <p>
     * Step size is device dependent, but usually around 2.5% of the total volume range. It is not necessarily
     * linear. The future completes when the volume change has been acknowledged by the device (when possible and
     * supported).
     *
     * @return future completing when volume has been changed
     */
    default CompletableFuture<Void> volumeDown() {
        return CompletableFuture.failedFuture(new NotSupportedError("volumeDown is not supported"));
    }

    /**
     * Returns the current list of output devices.
     *
     * @return current output devices
     * @throws NotSupportedError if not supported
     */
    default List<OutputDevice> outputDevices() {
        throw new NotSupportedError("outputDevices is not supported");
    }

    /**
     * Adds output devices.
     *
     * @param devices identifiers of devices to add
     * @return future completing when devices have been added
     */
    default CompletableFuture<Void> addOutputDevices(List<String> devices) {
        return CompletableFuture.failedFuture(new NotSupportedError("addOutputDevices is not supported"));
    }

    /**
     * Removes output devices.
     *
     * @param devices identifiers of devices to remove
     * @return future completing when devices have been removed
     */
    default CompletableFuture<Void> removeOutputDevices(List<String> devices) {
        return CompletableFuture.failedFuture(new NotSupportedError("removeOutputDevices is not supported"));
    }

    /**
     * Sets output devices.
     *
     * @param devices identifiers of devices to use
     * @return future completing when devices have been set
     */
    default CompletableFuture<Void> setOutputDevices(List<String> devices) {
        return CompletableFuture.failedFuture(new NotSupportedError("setOutputDevices is not supported"));
    }

    /**
     * Adds a listener receiving audio updates.
     *
     * @param listener listener to add
     */
    void addListener(AudioListener listener);

    /**
     * Removes a previously added listener.
     *
     * @param listener listener to remove
     */
    void removeListener(AudioListener listener);
}
