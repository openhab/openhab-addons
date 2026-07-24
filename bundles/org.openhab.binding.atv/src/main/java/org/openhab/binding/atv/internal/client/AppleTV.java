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
package org.openhab.binding.atv.internal.client;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.capability.Apps;
import org.openhab.binding.atv.internal.client.capability.Audio;
import org.openhab.binding.atv.internal.client.capability.Features;
import org.openhab.binding.atv.internal.client.capability.Keyboard;
import org.openhab.binding.atv.internal.client.capability.Metadata;
import org.openhab.binding.atv.internal.client.capability.Power;
import org.openhab.binding.atv.internal.client.capability.PushUpdater;
import org.openhab.binding.atv.internal.client.capability.RemoteControl;
import org.openhab.binding.atv.internal.client.capability.Stream;
import org.openhab.binding.atv.internal.client.capability.TouchGestures;
import org.openhab.binding.atv.internal.client.capability.UserAccounts;
import org.openhab.binding.atv.internal.client.conf.BaseService;
import org.openhab.binding.atv.internal.client.dto.DeviceInfo;
import org.openhab.binding.atv.internal.client.dto.Protocol;
import org.openhab.binding.atv.internal.client.settings.Settings;

/**
 * Representation of a connected Apple TV.
 *
 * <p>
 * Listener interface: {@link DeviceListener}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public interface AppleTV {

    /**
     * Initiates connection to the device.
     *
     * @return future completing when the connection has been established
     */
    CompletableFuture<Void> connect();

    /**
     * Closes the connection and releases allocated resources.
     *
     * @return future completing when all resources have been released
     */
    CompletableFuture<Void> close();

    /**
     * Returns the protocols that are currently connected.
     *
     * <p>
     * A connect can be partial (an asleep device may keep only some protocols, typically Companion,
     * reachable), so this can hold fewer protocols than the device advertises.
     *
     * @return the set of connected protocols; empty when not connected
     */
    default Set<Protocol> connectedProtocols() {
        return Set.of();
    }

    /**
     * Returns device settings used by the library.
     *
     * @return device settings
     */
    Settings settings();

    /**
     * Returns general device information.
     *
     * @return device information
     */
    DeviceInfo deviceInfo();

    /**
     * Returns the main service used to connect to the Apple TV.
     *
     * @return service used to connect
     */
    BaseService service();

    /**
     * Returns the API for controlling the Apple TV.
     *
     * @return remote control API
     */
    RemoteControl remoteControl();

    /**
     * Returns the API for retrieving metadata from the Apple TV.
     *
     * @return metadata API
     */
    Metadata metadata();

    /**
     * Returns the API for handling push updates from the Apple TV.
     *
     * @return push updater API
     */
    PushUpdater pushUpdater();

    /**
     * Returns the API for streaming media.
     *
     * @return stream API
     */
    Stream stream();

    /**
     * Returns the API for power management.
     *
     * @return power API
     */
    Power power();

    /**
     * Returns the features interface.
     *
     * @return features API
     */
    Features features();

    /**
     * Returns the apps interface.
     *
     * @return apps API
     */
    Apps apps();

    /**
     * Returns the user accounts interface.
     *
     * @return user accounts API
     */
    UserAccounts userAccounts();

    /**
     * Returns the audio interface.
     *
     * @return audio API
     */
    Audio audio();

    /**
     * Returns the keyboard interface.
     *
     * @return keyboard API
     */
    Keyboard keyboard();

    /**
     * Returns the touch gestures interface.
     *
     * @return touch gestures API
     */
    TouchGestures touch();

    /**
     * Adds a listener receiving device updates.
     *
     * @param listener listener to add
     */
    void addListener(DeviceListener listener);

    /**
     * Removes a previously added listener.
     *
     * @param listener listener to remove
     */
    void removeListener(DeviceListener listener);
}
