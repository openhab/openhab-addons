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

import java.io.InputStream;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.capability.Apps;
import org.openhab.binding.atv.internal.client.capability.Audio;
import org.openhab.binding.atv.internal.client.capability.Keyboard;
import org.openhab.binding.atv.internal.client.capability.Metadata;
import org.openhab.binding.atv.internal.client.capability.Power;
import org.openhab.binding.atv.internal.client.capability.RemoteControl;
import org.openhab.binding.atv.internal.client.capability.Stream;
import org.openhab.binding.atv.internal.client.capability.TouchGestures;
import org.openhab.binding.atv.internal.client.capability.UserAccounts;
import org.openhab.binding.atv.internal.client.dto.InputAction;
import org.openhab.binding.atv.internal.client.dto.MediaMetadata;
import org.openhab.binding.atv.internal.client.dto.OutputDevice;
import org.openhab.binding.atv.internal.client.dto.RepeatState;
import org.openhab.binding.atv.internal.client.dto.ShuffleState;
import org.openhab.binding.atv.internal.client.dto.TouchAction;

/**
 * One entry per overridable capability method on the public capability interfaces.
 *
 * <p>
 * Each constant maps explicitly to the <em>primary</em> interface method that a protocol implementation
 * overrides to provide the capability. Convenience overloads that merely delegate (e.g. {@code up()} without an
 * {@code InputAction}, or {@code playUrl(String)}) are not separate capabilities. Listener registration methods
 * ({@code addListener}/{@code removeListener}) are mandatory and therefore not capabilities either.
 *
 * <p>
 * Protocol implementations advertise their supported subset via {@link CapabilitySource}; test suites verify
 * honesty of that declaration by reflection (an advertised capability must correspond to an actually overridden
 * method, and vice versa).
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public enum Capability {

    // RemoteControl
    RC_UP(RemoteControl.class, "up", InputAction.class),
    RC_DOWN(RemoteControl.class, "down", InputAction.class),
    RC_LEFT(RemoteControl.class, "left", InputAction.class),
    RC_RIGHT(RemoteControl.class, "right", InputAction.class),
    RC_SELECT(RemoteControl.class, "select", InputAction.class),
    RC_MENU(RemoteControl.class, "menu", InputAction.class),
    RC_HOME(RemoteControl.class, "home", InputAction.class),
    RC_HOME_HOLD(RemoteControl.class, "homeHold"),
    RC_PLAY(RemoteControl.class, "play"),
    RC_PLAY_PAUSE(RemoteControl.class, "playPause"),
    RC_PAUSE(RemoteControl.class, "pause"),
    RC_STOP(RemoteControl.class, "stop"),
    RC_NEXT(RemoteControl.class, "next"),
    RC_PREVIOUS(RemoteControl.class, "previous"),
    RC_TOP_MENU(RemoteControl.class, "topMenu"),
    RC_SKIP_FORWARD(RemoteControl.class, "skipForward", Duration.class),
    RC_SKIP_BACKWARD(RemoteControl.class, "skipBackward", Duration.class),
    RC_SET_POSITION(RemoteControl.class, "setPosition", Duration.class),
    RC_SET_SHUFFLE(RemoteControl.class, "setShuffle", ShuffleState.class),
    RC_SET_REPEAT(RemoteControl.class, "setRepeat", RepeatState.class),
    RC_CHANNEL_UP(RemoteControl.class, "channelUp"),
    RC_CHANNEL_DOWN(RemoteControl.class, "channelDown"),
    RC_SCREENSAVER(RemoteControl.class, "screensaver"),
    RC_GUIDE(RemoteControl.class, "guide"),
    RC_CONTROL_CENTER(RemoteControl.class, "controlCenter"),

    // Metadata
    METADATA_DEVICE_ID(Metadata.class, "deviceId"),
    METADATA_ARTWORK(Metadata.class, "artwork", Integer.class, Integer.class),
    METADATA_ARTWORK_ID(Metadata.class, "artworkId"),
    METADATA_PLAYING(Metadata.class, "playing"),
    METADATA_APP(Metadata.class, "app"),

    // Power
    POWER_STATE(Power.class, "powerState"),
    POWER_REFRESH(Power.class, "refreshPowerState"),
    POWER_TURN_ON(Power.class, "turnOn", boolean.class),
    POWER_TURN_OFF(Power.class, "turnOff", boolean.class),

    // Audio
    AUDIO_VOLUME(Audio.class, "volume"),
    AUDIO_SET_VOLUME(Audio.class, "setVolume", double.class, OutputDevice.class),
    AUDIO_VOLUME_UP(Audio.class, "volumeUp"),
    AUDIO_VOLUME_DOWN(Audio.class, "volumeDown"),
    AUDIO_OUTPUT_DEVICES(Audio.class, "outputDevices"),
    AUDIO_ADD_OUTPUT_DEVICES(Audio.class, "addOutputDevices", List.class),
    AUDIO_REMOVE_OUTPUT_DEVICES(Audio.class, "removeOutputDevices", List.class),
    AUDIO_SET_OUTPUT_DEVICES(Audio.class, "setOutputDevices", List.class),

    // Apps
    APPS_APP_LIST(Apps.class, "appList"),
    APPS_LAUNCH_APP(Apps.class, "launchApp", String.class),

    // UserAccounts
    ACCOUNTS_ACCOUNT_LIST(UserAccounts.class, "accountList"),
    ACCOUNTS_SWITCH_ACCOUNT(UserAccounts.class, "switchAccount", String.class),

    // Keyboard
    KEYBOARD_TEXT_FOCUS_STATE(Keyboard.class, "textFocusState"),
    KEYBOARD_TEXT_GET(Keyboard.class, "textGet"),
    KEYBOARD_TEXT_CLEAR(Keyboard.class, "textClear"),
    KEYBOARD_TEXT_APPEND(Keyboard.class, "textAppend", String.class),
    KEYBOARD_TEXT_SET(Keyboard.class, "textSet", String.class),

    // TouchGestures
    TOUCH_SWIPE(TouchGestures.class, "swipe", int.class, int.class, int.class, int.class, int.class),
    TOUCH_ACTION(TouchGestures.class, "action", int.class, int.class, TouchAction.class),
    TOUCH_CLICK(TouchGestures.class, "click", InputAction.class),

    // Stream
    STREAM_CLOSE(Stream.class, "close"),
    STREAM_PLAY_URL(Stream.class, "playUrl", String.class, Map.class),
    STREAM_STREAM_FILE(Stream.class, "streamFile", String.class, MediaMetadata.class, Map.class),
    STREAM_STREAM_BUFFER(Stream.class, "streamFile", InputStream.class, MediaMetadata.class, Map.class);

    private final Class<?> interfaceClass;
    private final String methodName;
    private final Class<?>[] parameterTypes;

    Capability(Class<?> interfaceClass, String methodName, Class<?>... parameterTypes) {
        this.interfaceClass = interfaceClass;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
    }

    /**
     * Returns the capability interface this belongs to.
     *
     * @return capability interface class
     */
    public Class<?> interfaceClass() {
        return interfaceClass;
    }

    /**
     * Returns the name of the primary interface method backing this capability.
     *
     * @return method name
     */
    public String methodName() {
        return methodName;
    }

    /**
     * Returns the parameter types of the primary interface method backing this capability.
     *
     * @return parameter types (defensive copy)
     */
    public Class<?>[] parameterTypes() {
        return parameterTypes.clone();
    }

    /**
     * Resolves the primary interface {@link Method} backing this capability.
     *
     * @return the interface method
     * @throws IllegalStateException if the registry entry does not match the interface (indicates a programming
     *             error in this enum)
     */
    public Method method() {
        try {
            return interfaceClass.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Capability " + name() + " does not resolve on " + interfaceClass, e);
        }
    }
}
