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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Media control flags used to indicate available controls.
 *
 * <p>
 * Modeled as bit-mask constants since Java lacks int flags enums.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class MediaControlFlags {

    /** No controls available. */
    public static final int NO_CONTROLS = 0x0000;
    /** Play control available. */
    public static final int PLAY = 0x0001;
    /** Pause control available. */
    public static final int PAUSE = 0x0002;
    /** Next track control available. */
    public static final int NEXT_TRACK = 0x0004;
    /** Previous track control available. */
    public static final int PREVIOUS_TRACK = 0x0008;
    /** Fast forward control available. */
    public static final int FAST_FORWARD = 0x0010;
    /** Rewind control available. */
    public static final int REWIND = 0x0020;
    // ? = 0x0040
    // ? = 0x0080
    /** Volume control available. */
    public static final int VOLUME = 0x0100;
    /** Skip forward control available. */
    public static final int SKIP_FORWARD = 0x0200;
    /** Skip backward control available. */
    public static final int SKIP_BACKWARD = 0x0400;

    private MediaControlFlags() {
    }
}
