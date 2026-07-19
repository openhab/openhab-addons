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

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.capability.RemoteControl;
import org.openhab.binding.atv.internal.client.core.Capability;
import org.openhab.binding.atv.internal.client.core.CapabilitySource;
import org.openhab.binding.atv.internal.client.dto.InputAction;
import org.openhab.binding.atv.internal.client.dto.RepeatState;
import org.openhab.binding.atv.internal.client.dto.ShuffleState;
import org.openhab.binding.atv.internal.client.exceptions.CommandError;
import org.openhab.binding.atv.internal.client.exceptions.NotSupportedError;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.CommandInfoOuterClass.Command;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.CommandInfoOuterClass.CommandInfo;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.Common.PlaybackState;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.ProtocolMessageOuterClass.ProtocolMessage;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.SendCommandResultMessageOuterClass.SendCommandResultMessage;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.SendCommandResultMessageOuterClass.SendError;

/**
 * Implementation of the remote control API for MRP, including the button-to-HID usage
 * table and the press/release/flush sequence for virtual HID key presses.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class MrpRemoteControl implements RemoteControl, CapabilitySource {

    /** Default skip interval when the app suggests none. */
    public static final int DEFAULT_SKIP_TIME = 15;

    /**
     * Button name to (usage page, usage) HID mapping.
     * Source: https://github.com/Daij-Djan/DDHidLib/blob/master/usb_hid_usages.txt
     */
    private static final Map<String, int[]> KEY_LOOKUP = Map.ofEntries(Map.entry("up", new int[] { 1, 0x8C }),
            Map.entry("down", new int[] { 1, 0x8D }), Map.entry("left", new int[] { 1, 0x8B }),
            Map.entry("right", new int[] { 1, 0x8A }), Map.entry("stop", new int[] { 12, 0xB7 }),
            Map.entry("next", new int[] { 12, 0xB5 }), Map.entry("previous", new int[] { 12, 0xB6 }),
            Map.entry("select", new int[] { 1, 0x89 }), Map.entry("menu", new int[] { 1, 0x86 }),
            Map.entry("topmenu", new int[] { 12, 0x60 }), Map.entry("home", new int[] { 12, 0x40 }),
            Map.entry("suspend", new int[] { 1, 0x82 }), Map.entry("wakeup", new int[] { 1, 0x83 }),
            Map.entry("volume_up", new int[] { 12, 0xE9 }), Map.entry("volume_down", new int[] { 12, 0xEA }));

    private final PlayerStateManager psm;
    private final MrpProtocol protocol;

    /**
     * Creates a new remote control.
     *
     * @param psm player state manager (used for command availability)
     * @param protocol protocol used to send messages
     */
    public MrpRemoteControl(PlayerStateManager psm, MrpProtocol protocol) {
        this.psm = psm;
        this.protocol = protocol;
    }

    /**
     * Sends a virtual HID key press (down + up, flushing with a generic message). Used by
     * both the remote control and {@link MrpAudio}.
     *
     * @param protocol protocol used to send messages
     * @param key key name from the {@code KEY_LOOKUP} table
     * @param action input action (single tap, double tap or hold)
     * @param flush whether to exchange a generic message afterwards as a flush
     * @return future completing when the press has been fully performed
     */
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    static CompletableFuture<Void> sendHidKey(MrpProtocol protocol, String key, InputAction action, boolean flush) {
        int[] keycode = KEY_LOOKUP.get(key);
        if (keycode == null) {
            return CompletableFuture.failedFuture(new NotSupportedError("unsupported key: " + key));
        }
        CompletableFuture<Void> result = new CompletableFuture<>();
        Thread.ofVirtual().name("mrp-hid-" + key).start(() -> {
            try {
                switch (action) {
                    case SingleTap -> doPress(protocol, keycode, false, flush);
                    case DoubleTap -> {
                        doPress(protocol, keycode, false, flush);
                        doPress(protocol, keycode, false, flush);
                    }
                    case Hold -> doPress(protocol, keycode, true, flush);
                    default -> throw new NotSupportedError("unsupported input action: " + action);
                }
                MrpFutures.completeVoid(result);
            } catch (Throwable t) {
                result.completeExceptionally(t);
            }
        });
        return result;
    }

    private static void doPress(MrpProtocol protocol, int[] keycode, boolean hold, boolean flush) {
        protocol.send(MrpMessages.sendHidEvent(keycode[0], keycode[1], true)).join();
        if (hold) {
            // Hardcoded hold time of one second
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("interrupted during hold", e);
            }
        }
        protocol.send(MrpMessages.sendHidEvent(keycode[0], keycode[1], false)).join();

        // Send and receive a generic message as some kind of "flush" mechanism
        if (flush) {
            protocol.sendAndReceive(MrpMessages.create(ProtocolMessage.Type.GENERIC_MESSAGE).build()).join();
        }
    }

    private CompletableFuture<Void> hidKey(String key, InputAction action) {
        return sendHidKey(protocol, key, action, true);
    }

    private CompletableFuture<Void> sendCommand(Command command) {
        return sendCommand(MrpMessages.command(command), command);
    }

    private CompletableFuture<Void> sendCommand(ProtocolMessage message, Command command) {
        return protocol.sendAndReceive(message).thenAccept(response -> {
            SendCommandResultMessage inner = (SendCommandResultMessage) MrpExtensions.extractInner(response);
            if (inner.getSendError() == SendError.Enum.NoError) {
                return;
            }
            throw new CommandError(command + " failed: SendError=" + inner.getSendError() + ", HandlerReturnStatus="
                    + inner.getHandlerReturnStatus());
        });
    }

    @Override
    public CompletableFuture<Void> up(InputAction action) {
        return hidKey("up", action);
    }

    @Override
    public CompletableFuture<Void> down(InputAction action) {
        return hidKey("down", action);
    }

    @Override
    public CompletableFuture<Void> left(InputAction action) {
        return hidKey("left", action);
    }

    @Override
    public CompletableFuture<Void> right(InputAction action) {
        return hidKey("right", action);
    }

    @Override
    public CompletableFuture<Void> play() {
        return sendCommand(Command.Play);
    }

    @Override
    public CompletableFuture<Void> playPause() {
        // Cannot use the feature interface here since it emulates the feature state
        @Nullable
        CommandInfo cmd = psm.playing().commandInfo(Command.TogglePlayPause);
        if (cmd != null && cmd.getEnabled()) {
            return sendCommand(Command.TogglePlayPause);
        }
        PlaybackState.@Nullable Enum state = psm.playing().playbackState();
        if (state == PlaybackState.Enum.Playing) {
            return pause();
        }
        if (state == PlaybackState.Enum.Paused) {
            return play();
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> pause() {
        return sendCommand(Command.Pause);
    }

    @Override
    public CompletableFuture<Void> stop() {
        return sendCommand(Command.Stop);
    }

    @Override
    public CompletableFuture<Void> next() {
        return sendCommand(Command.NextTrack);
    }

    @Override
    public CompletableFuture<Void> previous() {
        return sendCommand(Command.PreviousTrack);
    }

    @Override
    public CompletableFuture<Void> select(InputAction action) {
        return hidKey("select", action);
    }

    @Override
    public CompletableFuture<Void> menu(InputAction action) {
        return hidKey("menu", action);
    }

    /**
     * Presses the volume up key (volume keys are part of the {@code Audio} interface on
     * the Java relay).
     *
     * @return future completing when the key press has been sent
     */
    public CompletableFuture<Void> volumeUp() {
        return hidKey("volume_up", InputAction.SingleTap);
    }

    /**
     * Presses the volume down key.
     *
     * @return future completing when the key press has been sent
     */
    public CompletableFuture<Void> volumeDown() {
        return hidKey("volume_down", InputAction.SingleTap);
    }

    @Override
    public CompletableFuture<Void> home(InputAction action) {
        return hidKey("home", action);
    }

    @Override
    @Deprecated
    public CompletableFuture<Void> homeHold() {
        return hidKey("home", InputAction.Hold);
    }

    @Override
    public CompletableFuture<Void> topMenu() {
        return hidKey("topmenu", InputAction.SingleTap);
    }

    /**
     * Suspends the device (not part of the Java capability interfaces).
     *
     * @return future completing when the key press has been sent
     */
    public CompletableFuture<Void> suspend() {
        return hidKey("suspend", InputAction.SingleTap);
    }

    /**
     * Wakes up the device (not part of the Java capability interfaces).
     *
     * @return future completing when the key press has been sent
     */
    public CompletableFuture<Void> wakeup() {
        return hidKey("wakeup", InputAction.SingleTap);
    }

    @Override
    public CompletableFuture<Void> skipForward(Duration timeInterval) {
        return skipCommand(Command.SkipForward, timeInterval);
    }

    @Override
    public CompletableFuture<Void> skipBackward(Duration timeInterval) {
        return skipCommand(Command.SkipBackward, timeInterval);
    }

    private CompletableFuture<Void> skipCommand(Command command, Duration timeInterval) {
        @Nullable
        CommandInfo info = psm.playing().commandInfo(command);

        int skipInterval;
        if (timeInterval != null && timeInterval.toSeconds() > 0) {
            skipInterval = (int) timeInterval.toSeconds();
        } else if (info != null && info.getPreferredIntervalsCount() > 0) {
            // Pick the first preferred interval for simplicity
            skipInterval = (int) info.getPreferredIntervals(0);
        } else {
            skipInterval = DEFAULT_SKIP_TIME;
        }
        return sendCommand(MrpMessages.command(command, options -> options.setSkipInterval(skipInterval)), command);
    }

    @Override
    public CompletableFuture<Void> setPosition(Duration position) {
        return protocol.sendAndReceive(MrpMessages.seekToPosition(position.toSeconds())).thenAccept(response -> {
        });
    }

    @Override
    public CompletableFuture<Void> setShuffle(ShuffleState shuffleState) {
        return protocol.sendAndReceive(MrpMessages.shuffle(shuffleState)).thenAccept(response -> {
        });
    }

    @Override
    public CompletableFuture<Void> setRepeat(RepeatState repeatState) {
        return protocol.sendAndReceive(MrpMessages.repeat(repeatState)).thenAccept(response -> {
        });
    }

    @Override
    public Set<Capability> capabilities() {
        return Set.of(Capability.RC_UP, Capability.RC_DOWN, Capability.RC_LEFT, Capability.RC_RIGHT,
                Capability.RC_SELECT, Capability.RC_MENU, Capability.RC_HOME, Capability.RC_HOME_HOLD,
                Capability.RC_PLAY, Capability.RC_PLAY_PAUSE, Capability.RC_PAUSE, Capability.RC_STOP,
                Capability.RC_NEXT, Capability.RC_PREVIOUS, Capability.RC_TOP_MENU, Capability.RC_SKIP_FORWARD,
                Capability.RC_SKIP_BACKWARD, Capability.RC_SET_POSITION, Capability.RC_SET_SHUFFLE,
                Capability.RC_SET_REPEAT);
    }
}
