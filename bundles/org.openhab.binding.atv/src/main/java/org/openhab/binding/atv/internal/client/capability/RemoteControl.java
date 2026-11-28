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

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.dto.InputAction;
import org.openhab.binding.atv.internal.client.dto.RepeatState;
import org.openhab.binding.atv.internal.client.dto.ShuffleState;
import org.openhab.binding.atv.internal.client.exceptions.NotSupportedError;

/**
 * API used to control an Apple TV.
 *
 * <p>
 * All methods complete exceptionally with {@link NotSupportedError} unless overridden by a protocol
 * implementation that supports them.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public interface RemoteControl {

    /**
     * Presses key up.
     *
     * @param action type of input
     * @return future completing when command has been sent
     */
    default CompletableFuture<Void> up(InputAction action) {
        return notSupported("up");
    }

    /**
     * Presses key up with a single tap.
     *
     * @return future completing when command has been sent
     */
    default CompletableFuture<Void> up() {
        return up(InputAction.SingleTap);
    }

    /**
     * Presses key down.
     *
     * @param action type of input
     * @return future completing when command has been sent
     */
    default CompletableFuture<Void> down(InputAction action) {
        return notSupported("down");
    }

    /**
     * Presses key down with a single tap.
     *
     * @return future completing when command has been sent
     */
    default CompletableFuture<Void> down() {
        return down(InputAction.SingleTap);
    }

    /**
     * Presses key left.
     *
     * @param action type of input
     * @return future completing when command has been sent
     */
    default CompletableFuture<Void> left(InputAction action) {
        return notSupported("left");
    }

    /**
     * Presses key left with a single tap.
     *
     * @return future completing when command has been sent
     */
    default CompletableFuture<Void> left() {
        return left(InputAction.SingleTap);
    }

    /**
     * Presses key right.
     *
     * @param action type of input
     * @return future completing when command has been sent
     */
    default CompletableFuture<Void> right(InputAction action) {
        return notSupported("right");
    }

    /**
     * Presses key right with a single tap.
     *
     * @return future completing when command has been sent
     */
    default CompletableFuture<Void> right() {
        return right(InputAction.SingleTap);
    }

    /**
     * Selects the current option.
     *
     * @param action type of input
     * @return future completing when command has been sent
     */
    default CompletableFuture<Void> select(InputAction action) {
        return notSupported("select");
    }

    /**
     * Selects the current option with a single tap.
     *
     * @return future completing when command has been sent
     */
    default CompletableFuture<Void> select() {
        return select(InputAction.SingleTap);
    }

    /**
     * Presses key menu (go back to previous menu).
     *
     * @param action type of input
     * @return future completing when command has been sent
     */
    default CompletableFuture<Void> menu(InputAction action) {
        return notSupported("menu");
    }

    /**
     * Presses key menu with a single tap.
     *
     * @return future completing when command has been sent
     */
    default CompletableFuture<Void> menu() {
        return menu(InputAction.SingleTap);
    }

    /**
     * Presses key home (Home/TV button).
     *
     * @param action type of input
     * @return future completing when command has been sent
     */
    default CompletableFuture<Void> home(InputAction action) {
        return notSupported("home");
    }

    /**
     * Presses key home with a single tap.
     *
     * @return future completing when command has been sent
     */
    default CompletableFuture<Void> home() {
        return home(InputAction.SingleTap);
    }

    /**
     * Holds key home.
     *
     * @return future completing when command has been sent
     * @deprecated use {@link #home(InputAction)} with {@link InputAction#Hold} instead
     */
    @Deprecated
    default CompletableFuture<Void> homeHold() {
        return notSupported("homeHold");
    }

    /**
     * Presses key play.
     *
     * @return future completing when command has been sent
     */
    default CompletableFuture<Void> play() {
        return notSupported("play");
    }

    /**
     * Toggles between play and pause.
     *
     * @return future completing when command has been sent
     */
    default CompletableFuture<Void> playPause() {
        return notSupported("playPause");
    }

    /**
     * Presses key pause.
     *
     * @return future completing when command has been sent
     */
    default CompletableFuture<Void> pause() {
        return notSupported("pause");
    }

    /**
     * Presses key stop.
     *
     * @return future completing when command has been sent
     */
    default CompletableFuture<Void> stop() {
        return notSupported("stop");
    }

    /**
     * Changes to the next item.
     *
     * @return future completing when command has been sent
     */
    default CompletableFuture<Void> next() {
        return notSupported("next");
    }

    /**
     * Changes to the previous item.
     *
     * @return future completing when command has been sent
     */
    default CompletableFuture<Void> previous() {
        return notSupported("previous");
    }

    /**
     * Goes to the main menu (long press menu).
     *
     * @return future completing when command has been sent
     */
    default CompletableFuture<Void> topMenu() {
        return notSupported("topMenu");
    }

    /**
     * Skips forward a time interval.
     *
     * <p>
     * If the interval is zero or negative, a default or app-chosen time interval is used, which is typically 10,
     * 15, 30, etc. seconds.
     *
     * @param timeInterval interval to skip
     * @return future completing when command has been sent
     */
    default CompletableFuture<Void> skipForward(Duration timeInterval) {
        return notSupported("skipForward");
    }

    /**
     * Skips forward a default or app-chosen time interval.
     *
     * @return future completing when command has been sent
     */
    default CompletableFuture<Void> skipForward() {
        return skipForward(Duration.ZERO);
    }

    /**
     * Skips backward a time interval.
     *
     * <p>
     * If the interval is zero or negative, a default or app-chosen time interval is used, which is typically 10,
     * 15, 30, etc. seconds.
     *
     * @param timeInterval interval to skip
     * @return future completing when command has been sent
     */
    default CompletableFuture<Void> skipBackward(Duration timeInterval) {
        return notSupported("skipBackward");
    }

    /**
     * Skips backward a default or app-chosen time interval.
     *
     * @return future completing when command has been sent
     */
    default CompletableFuture<Void> skipBackward() {
        return skipBackward(Duration.ZERO);
    }

    /**
     * Seeks in the current playing media.
     *
     * @param position position to seek to
     * @return future completing when command has been sent
     */
    default CompletableFuture<Void> setPosition(Duration position) {
        return notSupported("setPosition");
    }

    /**
     * Changes shuffle mode.
     *
     * @param shuffleState new shuffle state
     * @return future completing when command has been sent
     */
    default CompletableFuture<Void> setShuffle(ShuffleState shuffleState) {
        return notSupported("setShuffle");
    }

    /**
     * Changes repeat state.
     *
     * @param repeatState new repeat state
     * @return future completing when command has been sent
     */
    default CompletableFuture<Void> setRepeat(RepeatState repeatState) {
        return notSupported("setRepeat");
    }

    /**
     * Selects the next channel.
     *
     * @return future completing when command has been sent
     */
    default CompletableFuture<Void> channelUp() {
        return notSupported("channelUp");
    }

    /**
     * Selects the previous channel.
     *
     * @return future completing when command has been sent
     */
    default CompletableFuture<Void> channelDown() {
        return notSupported("channelDown");
    }

    /**
     * Activates the screen saver.
     *
     * @return future completing when command has been sent
     */
    default CompletableFuture<Void> screensaver() {
        return notSupported("screensaver");
    }

    /**
     * Shows the EPG (electronic program guide).
     *
     * @return future completing when command has been sent
     */
    default CompletableFuture<Void> guide() {
        return notSupported("guide");
    }

    /**
     * Opens the control center.
     *
     * @return future completing when command has been sent
     */
    default CompletableFuture<Void> controlCenter() {
        return notSupported("controlCenter");
    }

    private static CompletableFuture<Void> notSupported(String command) {
        return CompletableFuture.failedFuture(new NotSupportedError(command + " is not supported"));
    }
}
