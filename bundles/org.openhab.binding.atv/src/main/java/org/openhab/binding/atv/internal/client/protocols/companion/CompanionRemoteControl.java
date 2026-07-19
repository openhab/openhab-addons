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

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.capability.RemoteControl;
import org.openhab.binding.atv.internal.client.core.Capability;
import org.openhab.binding.atv.internal.client.core.CapabilitySource;
import org.openhab.binding.atv.internal.client.dto.InputAction;
import org.openhab.binding.atv.internal.client.exceptions.NotSupportedError;
import org.openhab.binding.atv.internal.client.exceptions.ProtocolError;

/**
 * Implementation of the remote control API for Companion.
 *
 * <p>
 * Navigation buttons are sent as HID commands (button down/up pairs), playback commands
 * as media control ({@code _mcc}) commands. Volume buttons are exposed through
 * {@link CompanionAudio} instead, matching the interface split.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class CompanionRemoteControl implements RemoteControl, CapabilitySource {

    /** Default skip interval, as seen in the TV Remote App. */
    static final double DEFAULT_SKIP_TIME = 10;

    private static final Duration HOLD_DELAY = Duration.ofSeconds(1);

    private final CompanionApi api;

    /**
     * Creates a new instance.
     *
     * @param api Companion API
     */
    public CompanionRemoteControl(CompanionApi api) {
        this.api = api;
    }

    @Override
    public CompletableFuture<Void> up(InputAction action) {
        return pressButton(HidCommand.Up, action);
    }

    @Override
    public CompletableFuture<Void> down(InputAction action) {
        return pressButton(HidCommand.Down, action);
    }

    @Override
    public CompletableFuture<Void> left(InputAction action) {
        return pressButton(HidCommand.Left, action);
    }

    @Override
    public CompletableFuture<Void> right(InputAction action) {
        return pressButton(HidCommand.Right, action);
    }

    @Override
    public CompletableFuture<Void> select(InputAction action) {
        return pressButton(HidCommand.Select, action);
    }

    @Override
    public CompletableFuture<Void> menu(InputAction action) {
        return pressButton(HidCommand.Menu, action);
    }

    @Override
    public CompletableFuture<Void> home(InputAction action) {
        return pressButton(HidCommand.Home, action);
    }

    @Override
    public CompletableFuture<Void> playPause() {
        return pressButton(HidCommand.PlayPause, InputAction.SingleTap);
    }

    @Override
    public CompletableFuture<Void> play() {
        return api.mediaControlCommand(MediaControlCommand.Play, null).thenRun(() -> {
        });
    }

    @Override
    public CompletableFuture<Void> pause() {
        return api.mediaControlCommand(MediaControlCommand.Pause, null).thenRun(() -> {
        });
    }

    @Override
    public CompletableFuture<Void> next() {
        return api.mediaControlCommand(MediaControlCommand.NextTrack, null).thenRun(() -> {
        });
    }

    @Override
    public CompletableFuture<Void> previous() {
        return api.mediaControlCommand(MediaControlCommand.PreviousTrack, null).thenRun(() -> {
        });
    }

    @Override
    public CompletableFuture<Void> skipForward(Duration timeInterval) {
        double seconds = durationToSeconds(timeInterval);
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("_skpS", seconds > 0 ? seconds : DEFAULT_SKIP_TIME);
        return api.mediaControlCommand(MediaControlCommand.SkipBy, args).thenRun(() -> {
        });
    }

    @Override
    public CompletableFuture<Void> skipBackward(Duration timeInterval) {
        double seconds = durationToSeconds(timeInterval);
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("_skpS", seconds > 0 ? -seconds : -DEFAULT_SKIP_TIME);
        return api.mediaControlCommand(MediaControlCommand.SkipBy, args).thenRun(() -> {
        });
    }

    @Override
    public CompletableFuture<Void> channelUp() {
        return pressButton(HidCommand.ChannelIncrement, InputAction.SingleTap);
    }

    @Override
    public CompletableFuture<Void> channelDown() {
        return pressButton(HidCommand.ChannelDecrement, InputAction.SingleTap);
    }

    @Override
    public CompletableFuture<Void> screensaver() {
        return pressButton(HidCommand.Screensaver, InputAction.SingleTap);
    }

    @Override
    public CompletableFuture<Void> guide() {
        return pressButton(HidCommand.Guide, InputAction.SingleTap);
    }

    @Override
    public CompletableFuture<Void> controlCenter() {
        return pressButton(HidCommand.PageDown, InputAction.SingleTap);
    }

    private CompletableFuture<Void> pressButton(HidCommand command, InputAction action) {
        return CompletableFuture.runAsync(() -> {
            switch (action) {
                case SingleTap -> {
                    CompanionApi.join(api.hidCommand(true, command));
                    CompanionApi.join(api.hidCommand(false, command));
                }
                case Hold -> {
                    CompanionApi.join(api.hidCommand(true, command));
                    sleep(HOLD_DELAY.toMillis());
                    CompanionApi.join(api.hidCommand(false, command));
                }
                case DoubleTap -> {
                    // First press
                    CompanionApi.join(api.hidCommand(true, command));
                    CompanionApi.join(api.hidCommand(false, command));
                    // Second press
                    CompanionApi.join(api.hidCommand(true, command));
                    CompanionApi.join(api.hidCommand(false, command));
                }
                default -> throw new NotSupportedError("unsupported input action: " + action);
            }
        }, api.blockingExecutor());
    }

    private static double durationToSeconds(Duration duration) {
        return duration == null ? 0.0 : duration.toMillis() / 1000.0;
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ProtocolError("interrupted", e);
        }
    }

    @Override
    public Set<Capability> capabilities() {
        return Set.of(Capability.RC_UP, Capability.RC_DOWN, Capability.RC_LEFT, Capability.RC_RIGHT,
                Capability.RC_SELECT, Capability.RC_MENU, Capability.RC_HOME, Capability.RC_PLAY,
                Capability.RC_PLAY_PAUSE, Capability.RC_PAUSE, Capability.RC_NEXT, Capability.RC_PREVIOUS,
                Capability.RC_SKIP_FORWARD, Capability.RC_SKIP_BACKWARD, Capability.RC_CHANNEL_UP,
                Capability.RC_CHANNEL_DOWN, Capability.RC_SCREENSAVER, Capability.RC_GUIDE,
                Capability.RC_CONTROL_CENTER);
    }
}
