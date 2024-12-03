/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.huesync.internal.api.dto.hdmi;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * 
 * @author Patrik Gfeller - Initial Contribution
 * 
 */
@NonNullByDefault
public class HueSyncHdmi {
    public @Nullable HueSyncHdmiConnectionInfo input1;
    public @Nullable HueSyncHdmiConnectionInfo input2;
    public @Nullable HueSyncHdmiConnectionInfo input3;
    public @Nullable HueSyncHdmiConnectionInfo input4;

    public @Nullable HueSyncHdmiConnectionInfo output;

    /** <horizontal pixels> x <vertical pixels> @ <framerate fps> – <HDR> */
    public @Nullable String contentSpecs;

    /** Current content specs supported for video sync (video/game mode) */
    public boolean videoSyncSupported;
    /** Current content specs supported for audio sync (music mode) */
    public boolean audioSyncSupported;
}
