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

import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.dto.KeyboardFocusState;
import org.openhab.binding.atv.internal.client.exceptions.NotSupportedError;

/**
 * API for virtual keyboard handling.
 *
 * <p>
 * Listener interface: {@link KeyboardListener}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public interface Keyboard {

    /**
     * Returns the current virtual keyboard focus state.
     *
     * @return focus state
     * @throws NotSupportedError if not supported
     */
    default KeyboardFocusState textFocusState() {
        throw new NotSupportedError("textFocusState is not supported");
    }

    /**
     * Gets the current virtual keyboard text.
     *
     * @return future completing with current text (may be {@code null})
     */
    default CompletableFuture<String> textGet() {
        return CompletableFuture.failedFuture(new NotSupportedError("textGet is not supported"));
    }

    /**
     * Clears the virtual keyboard text.
     *
     * @return future completing when text has been cleared
     */
    default CompletableFuture<Void> textClear() {
        return CompletableFuture.failedFuture(new NotSupportedError("textClear is not supported"));
    }

    /**
     * Inputs (appends) text into the virtual keyboard.
     *
     * @param text text to append
     * @return future completing when text has been appended
     */
    default CompletableFuture<Void> textAppend(String text) {
        return CompletableFuture.failedFuture(new NotSupportedError("textAppend is not supported"));
    }

    /**
     * Replaces the text in the virtual keyboard.
     *
     * @param text new text
     * @return future completing when text has been set
     */
    default CompletableFuture<Void> textSet(String text) {
        return CompletableFuture.failedFuture(new NotSupportedError("textSet is not supported"));
    }

    /**
     * Adds a listener receiving keyboard focus updates.
     *
     * @param listener listener to add
     */
    void addListener(KeyboardListener listener);

    /**
     * Removes a previously added listener.
     *
     * @param listener listener to remove
     */
    void removeListener(KeyboardListener listener);
}
