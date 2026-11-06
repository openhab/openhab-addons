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

/**
 * Names of internal device states that can be updated, dispatched via
 * {@link CoreStateDispatcher}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public enum UpdatedState {
    /** Playing state in metadata was updated. */
    PLAYING,

    /** Volume was updated. */
    VOLUME,

    /** Keyboard focus was updated. */
    KEYBOARD_FOCUS,

    /** AirPlay output devices were updated. */
    OUTPUT_DEVICES,

    /** Output device volume was updated. */
    OUTPUT_DEVICE_VOLUME
}
