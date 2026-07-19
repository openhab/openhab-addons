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

import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.capability.Keyboard;
import org.openhab.binding.atv.internal.client.capability.KeyboardListener;
import org.openhab.binding.atv.internal.client.dto.KeyboardFocusState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Relay implementation for keyboard handling.
 *
 * <p>
 * Focus state updates are received via the {@link CoreStateDispatcher} ({@link UpdatedState#KEYBOARD_FOCUS}),
 * filtered to updates originating from the main protocol of this relay, deduplicated and forwarded to
 * {@link KeyboardListener} instances.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class KeyboardRelay extends BaseRelay<Keyboard> implements Keyboard {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyboardRelay.class);

    private final ListenerRegistry<KeyboardListener> listeners;

    // Only mutated from dispatcher callbacks, which run on the device loop
    private KeyboardFocusState focusState = KeyboardFocusState.Unknown;

    /**
     * Creates a new relay keyboard instance.
     *
     * @param guard device guard blocking calls after close
     * @param coreDispatcher per-device state dispatcher delivering focus state updates
     * @param loop device loop used for listener notification
     */
    public KeyboardRelay(Guard guard, CoreStateDispatcher coreDispatcher, DeviceLoop loop) {
        super(new Relayer<>(Keyboard.class, AppleTVRelay.DEFAULT_PRIORITIES), guard);
        this.listeners = new ListenerRegistry<>(loop);
        coreDispatcher.listenTo(UpdatedState.KEYBOARD_FOCUS, this::focusStateChanged,
                message -> relayer.mainProtocol().map(main -> main == message.protocol()).orElse(false));
    }

    private void focusStateChanged(StateMessage message) {
        KeyboardFocusState newState = (KeyboardFocusState) message.value();

        KeyboardFocusState oldState = focusState;
        focusState = newState;

        if (newState != oldState) {
            LOGGER.debug("Focus state changed from {} to {}", oldState, newState);
            listeners.fire(listener -> listener.focusstateUpdate(oldState, newState));
        }
    }

    @Override
    public KeyboardFocusState textFocusState() {
        guard.requireNotBlocked("textFocusState");
        return relayer.relay(Capability.KEYBOARD_TEXT_FOCUS_STATE).textFocusState();
    }

    @Override
    public CompletableFuture<String> textGet() {
        guard.requireNotBlocked("textGet");
        return relayAsync(Capability.KEYBOARD_TEXT_GET, Keyboard::textGet);
    }

    @Override
    public CompletableFuture<Void> textClear() {
        guard.requireNotBlocked("textClear");
        return relayAsync(Capability.KEYBOARD_TEXT_CLEAR, Keyboard::textClear);
    }

    @Override
    public CompletableFuture<Void> textAppend(String text) {
        guard.requireNotBlocked("textAppend");
        return relayAsync(Capability.KEYBOARD_TEXT_APPEND, keyboard -> keyboard.textAppend(text));
    }

    @Override
    public CompletableFuture<Void> textSet(String text) {
        guard.requireNotBlocked("textSet");
        return relayAsync(Capability.KEYBOARD_TEXT_SET, keyboard -> keyboard.textSet(text));
    }

    @Override
    public void addListener(KeyboardListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(KeyboardListener listener) {
        listeners.remove(listener);
    }
}
