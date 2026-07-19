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
package org.openhab.binding.atv.internal.client.protocols.companion;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.capability.Features;
import org.openhab.binding.atv.internal.client.dto.FeatureInfo;
import org.openhab.binding.atv.internal.client.dto.FeatureName;
import org.openhab.binding.atv.internal.client.dto.FeatureState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the supported-feature functionality for Companion.
 *
 * <p>
 * Media control features follow the {@code _iMC} control flags, {@code PowerState} follows
 * whether power updates could be established, and the remaining supported features are
 * assumed available once the protocol is configured.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class CompanionFeatures implements Features {

    /** Mapping from feature to media control flag. */
    static final Map<FeatureName, Integer> MEDIA_CONTROL_MAP = createMediaControlMap();

    /** All features supported by Companion. */
    static final Set<FeatureName> SUPPORTED_FEATURES = createSupportedFeatures();

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanionFeatures.class);

    private final CompanionPower power;

    private volatile int controlFlags = MediaControlFlags.NO_CONTROLS;

    /**
     * Creates a new instance.
     *
     * @param api Companion API (subscribed to {@code _iMC} control flag updates)
     * @param power power implementation used for {@code PowerState} availability
     */
    public CompanionFeatures(CompanionApi api, CompanionPower power) {
        api.listenTo("_iMC", data -> {
            Long controlFlagsValue = CompanionProtocol.toLong(data.get("_mcF"));
            controlFlags = controlFlagsValue == null ? MediaControlFlags.NO_CONTROLS : controlFlagsValue.intValue();
            LOGGER.debug("Updated media control flags to {}", controlFlags);
        });
        this.power = power;
    }

    @Override
    public FeatureInfo getFeature(FeatureName featureName) {
        Integer mediaControlFlag = MEDIA_CONTROL_MAP.get(featureName);
        if (mediaControlFlag != null) {
            boolean isAvailable = (mediaControlFlag & controlFlags) != 0;
            return new FeatureInfo(isAvailable ? FeatureState.Available : FeatureState.Unavailable);
        }

        if (featureName == FeatureName.PowerState) {
            return new FeatureInfo(power.supportsPowerUpdates() ? FeatureState.Available : FeatureState.Unsupported);
        }

        // Assume these are available once the protocol is configured; there's no way
        // to verify them otherwise.
        if (SUPPORTED_FEATURES.contains(featureName)) {
            return new FeatureInfo(FeatureState.Available);
        }

        return new FeatureInfo(FeatureState.Unavailable);
    }

    private static Map<FeatureName, Integer> createMediaControlMap() {
        Map<FeatureName, Integer> map = new LinkedHashMap<>();
        map.put(FeatureName.Play, MediaControlFlags.PLAY);
        map.put(FeatureName.Pause, MediaControlFlags.PAUSE);
        map.put(FeatureName.Next, MediaControlFlags.NEXT_TRACK);
        map.put(FeatureName.Previous, MediaControlFlags.PREVIOUS_TRACK);
        map.put(FeatureName.Volume, MediaControlFlags.VOLUME);
        map.put(FeatureName.SetVolume, MediaControlFlags.VOLUME);
        map.put(FeatureName.SkipForward, MediaControlFlags.SKIP_FORWARD);
        map.put(FeatureName.SkipBackward, MediaControlFlags.SKIP_BACKWARD);
        return Map.copyOf(map);
    }

    private static Set<FeatureName> createSupportedFeatures() {
        Set<FeatureName> features = EnumSet.of(
                // App interface
                FeatureName.AppList, FeatureName.LaunchApp,
                // User account interface
                FeatureName.AccountList, FeatureName.SwitchAccount,
                // Power interface
                FeatureName.PowerState, FeatureName.TurnOn, FeatureName.TurnOff,
                // Remote control (navigation, i.e. HID)
                FeatureName.Up, FeatureName.Down, FeatureName.Left, FeatureName.Right, FeatureName.Select,
                FeatureName.Menu, FeatureName.Home, FeatureName.VolumeUp, FeatureName.VolumeDown, FeatureName.PlayPause,
                FeatureName.ChannelUp, FeatureName.ChannelDown, FeatureName.Screensaver, FeatureName.Guide,
                FeatureName.ControlCenter,
                // Keyboard interface
                FeatureName.TextFocusState, FeatureName.TextGet, FeatureName.TextClear, FeatureName.TextAppend,
                FeatureName.TextSet,
                // Touch interface
                FeatureName.Swipe, FeatureName.Action, FeatureName.Click);
        // Remote control (playback, i.e. Media Control)
        features.addAll(createMediaControlMap().keySet());
        return Set.copyOf(features);
    }
}
