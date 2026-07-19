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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.dto.KeyboardFocusState;

/**
 * Listener interface for keyboard updates.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public interface KeyboardListener {

    /**
     * Informs that the keyboard focus state was updated.
     *
     * @param oldState previous focus state
     * @param newState new focus state
     */
    void focusstateUpdate(KeyboardFocusState oldState, KeyboardFocusState newState);
}
