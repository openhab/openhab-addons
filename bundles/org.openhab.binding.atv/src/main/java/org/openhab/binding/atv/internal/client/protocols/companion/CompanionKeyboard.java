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

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.capability.Keyboard;
import org.openhab.binding.atv.internal.client.capability.KeyboardListener;
import org.openhab.binding.atv.internal.client.core.Capability;
import org.openhab.binding.atv.internal.client.core.CapabilitySource;
import org.openhab.binding.atv.internal.client.core.Core;
import org.openhab.binding.atv.internal.client.core.ListenerRegistry;
import org.openhab.binding.atv.internal.client.core.UpdatedState;
import org.openhab.binding.atv.internal.client.dto.KeyboardFocusState;

/**
 * Implementation of the virtual keyboard API for Companion.
 *
 * <p>
 * Focus state is tracked from the {@code _tiStarted}/{@code _tiStopped} events (and the
 * forwarded {@code _tiStart} response — {@code _tiStarted} is not sent when the session
 * starts while a text field is already focused); text operations go through the RTI text
 * input commands.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class CompanionKeyboard implements Keyboard, CapabilitySource {

    private final CompanionApi api;
    private final Core core;
    private final ListenerRegistry<KeyboardListener> listeners;

    private volatile KeyboardFocusState focusState = KeyboardFocusState.Unknown;

    /**
     * Creates a new instance.
     *
     * @param api Companion API
     * @param core protocol context
     */
    public CompanionKeyboard(CompanionApi api, Core core) {
        this.api = api;
        this.core = core;
        this.listeners = new ListenerRegistry<>(core.loop());
        // _tiStarted will not be sent if session is started while already focused
        api.listenTo("_tiStarted", this::handleTextInput);
        api.listenTo("_tiStopped", this::handleTextInput);
        // _tiStart is actually a command that we forward the response of
        api.listenTo("_tiStart", this::handleTextInput);
    }

    private void handleTextInput(Map<String, Object> data) {
        KeyboardFocusState newState = data.containsKey("_tiD") ? KeyboardFocusState.Focused
                : KeyboardFocusState.Unfocused;
        KeyboardFocusState oldState = focusState;
        focusState = newState;
        core.stateDispatcher().dispatch(UpdatedState.KEYBOARD_FOCUS, newState);
        if (oldState != newState) {
            listeners.fire(listener -> listener.focusstateUpdate(oldState, newState));
        }
    }

    @Override
    public KeyboardFocusState textFocusState() {
        return focusState;
    }

    @Override
    public CompletableFuture<String> textGet() {
        return api.textInputCommand("", false).thenApply(text -> text == null ? "" : text);
    }

    @Override
    public CompletableFuture<Void> textClear() {
        return api.textInputCommand("", true).thenRun(() -> {
        });
    }

    @Override
    public CompletableFuture<Void> textAppend(String text) {
        return api.textInputCommand(text, false).thenRun(() -> {
        });
    }

    @Override
    public CompletableFuture<Void> textSet(String text) {
        return api.textInputCommand(text, true).thenRun(() -> {
        });
    }

    @Override
    public void addListener(KeyboardListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(KeyboardListener listener) {
        listeners.remove(listener);
    }

    @Override
    public Set<Capability> capabilities() {
        return Set.of(Capability.KEYBOARD_TEXT_FOCUS_STATE, Capability.KEYBOARD_TEXT_GET,
                Capability.KEYBOARD_TEXT_CLEAR, Capability.KEYBOARD_TEXT_APPEND, Capability.KEYBOARD_TEXT_SET);
    }
}
