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

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.capability.RemoteControl;
import org.openhab.binding.atv.internal.client.dto.InputAction;
import org.openhab.binding.atv.internal.client.dto.RepeatState;
import org.openhab.binding.atv.internal.client.dto.ShuffleState;

/**
 * Relay implementation for the API used to control an Apple TV.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class RemoteControlRelay extends BaseRelay<RemoteControl> implements RemoteControl {

    /**
     * Creates a new relay remote control.
     *
     * @param guard device guard blocking calls after close
     */
    public RemoteControlRelay(Guard guard) {
        super(new Relayer<>(RemoteControl.class, AppleTVRelay.DEFAULT_PRIORITIES), guard);
    }

    @Override
    public CompletableFuture<Void> up(InputAction action) {
        guard.requireNotBlocked("up");
        return relayAsync(Capability.RC_UP, rc -> rc.up(action));
    }

    @Override
    public CompletableFuture<Void> down(InputAction action) {
        guard.requireNotBlocked("down");
        return relayAsync(Capability.RC_DOWN, rc -> rc.down(action));
    }

    @Override
    public CompletableFuture<Void> left(InputAction action) {
        guard.requireNotBlocked("left");
        return relayAsync(Capability.RC_LEFT, rc -> rc.left(action));
    }

    @Override
    public CompletableFuture<Void> right(InputAction action) {
        guard.requireNotBlocked("right");
        return relayAsync(Capability.RC_RIGHT, rc -> rc.right(action));
    }

    @Override
    public CompletableFuture<Void> select(InputAction action) {
        guard.requireNotBlocked("select");
        return relayAsync(Capability.RC_SELECT, rc -> rc.select(action));
    }

    @Override
    public CompletableFuture<Void> menu(InputAction action) {
        guard.requireNotBlocked("menu");
        return relayAsync(Capability.RC_MENU, rc -> rc.menu(action));
    }

    @Override
    public CompletableFuture<Void> home(InputAction action) {
        guard.requireNotBlocked("home");
        return relayAsync(Capability.RC_HOME, rc -> rc.home(action));
    }

    @Override
    @Deprecated
    public CompletableFuture<Void> homeHold() {
        guard.requireNotBlocked("homeHold");
        return relayAsync(Capability.RC_HOME_HOLD, RemoteControl::homeHold);
    }

    @Override
    public CompletableFuture<Void> play() {
        guard.requireNotBlocked("play");
        return relayAsync(Capability.RC_PLAY, RemoteControl::play);
    }

    @Override
    public CompletableFuture<Void> playPause() {
        guard.requireNotBlocked("playPause");
        return relayAsync(Capability.RC_PLAY_PAUSE, RemoteControl::playPause);
    }

    @Override
    public CompletableFuture<Void> pause() {
        guard.requireNotBlocked("pause");
        return relayAsync(Capability.RC_PAUSE, RemoteControl::pause);
    }

    @Override
    public CompletableFuture<Void> stop() {
        guard.requireNotBlocked("stop");
        return relayAsync(Capability.RC_STOP, RemoteControl::stop);
    }

    @Override
    public CompletableFuture<Void> next() {
        guard.requireNotBlocked("next");
        return relayAsync(Capability.RC_NEXT, RemoteControl::next);
    }

    @Override
    public CompletableFuture<Void> previous() {
        guard.requireNotBlocked("previous");
        return relayAsync(Capability.RC_PREVIOUS, RemoteControl::previous);
    }

    @Override
    public CompletableFuture<Void> topMenu() {
        guard.requireNotBlocked("topMenu");
        return relayAsync(Capability.RC_TOP_MENU, RemoteControl::topMenu);
    }

    @Override
    public CompletableFuture<Void> skipForward(Duration timeInterval) {
        guard.requireNotBlocked("skipForward");
        return relayAsync(Capability.RC_SKIP_FORWARD, rc -> rc.skipForward(timeInterval));
    }

    @Override
    public CompletableFuture<Void> skipBackward(Duration timeInterval) {
        guard.requireNotBlocked("skipBackward");
        return relayAsync(Capability.RC_SKIP_BACKWARD, rc -> rc.skipBackward(timeInterval));
    }

    @Override
    public CompletableFuture<Void> setPosition(Duration position) {
        guard.requireNotBlocked("setPosition");
        return relayAsync(Capability.RC_SET_POSITION, rc -> rc.setPosition(position));
    }

    @Override
    public CompletableFuture<Void> setShuffle(ShuffleState shuffleState) {
        guard.requireNotBlocked("setShuffle");
        return relayAsync(Capability.RC_SET_SHUFFLE, rc -> rc.setShuffle(shuffleState));
    }

    @Override
    public CompletableFuture<Void> setRepeat(RepeatState repeatState) {
        guard.requireNotBlocked("setRepeat");
        return relayAsync(Capability.RC_SET_REPEAT, rc -> rc.setRepeat(repeatState));
    }

    @Override
    public CompletableFuture<Void> channelUp() {
        guard.requireNotBlocked("channelUp");
        return relayAsync(Capability.RC_CHANNEL_UP, RemoteControl::channelUp);
    }

    @Override
    public CompletableFuture<Void> channelDown() {
        guard.requireNotBlocked("channelDown");
        return relayAsync(Capability.RC_CHANNEL_DOWN, RemoteControl::channelDown);
    }

    @Override
    public CompletableFuture<Void> screensaver() {
        guard.requireNotBlocked("screensaver");
        return relayAsync(Capability.RC_SCREENSAVER, RemoteControl::screensaver);
    }

    @Override
    public CompletableFuture<Void> guide() {
        guard.requireNotBlocked("guide");
        return relayAsync(Capability.RC_GUIDE, RemoteControl::guide);
    }

    @Override
    public CompletableFuture<Void> controlCenter() {
        guard.requireNotBlocked("controlCenter");
        return relayAsync(Capability.RC_CONTROL_CENTER, RemoteControl::controlCenter);
    }
}
