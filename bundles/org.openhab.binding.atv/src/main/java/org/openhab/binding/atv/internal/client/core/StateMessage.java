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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.dto.Protocol;

/**
 * Message sent when the state of something changed.
 *
 * <p>
 * The runtime type of {@code value} depends on {@code state}:
 * <ul>
 * <li>{@link UpdatedState#PLAYING} → {@code Playing}</li>
 * <li>{@link UpdatedState#VOLUME} → {@code Double}</li>
 * <li>{@link UpdatedState#KEYBOARD_FOCUS} → {@code KeyboardFocusState}</li>
 * <li>{@link UpdatedState#OUTPUT_DEVICES} → {@code List<OutputDevice>}</li>
 * <li>{@link UpdatedState#OUTPUT_DEVICE_VOLUME} → {@code Double}</li>
 * </ul>
 *
 * @param protocol protocol the update originated from
 * @param state which state was updated
 * @param value new value, type depending on {@code state}
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public record StateMessage(Protocol protocol, UpdatedState state, Object value) {

    @Override
    public String toString() {
        return "[" + protocol.name() + "." + state.name() + " -> " + value + "]";
    }
}
