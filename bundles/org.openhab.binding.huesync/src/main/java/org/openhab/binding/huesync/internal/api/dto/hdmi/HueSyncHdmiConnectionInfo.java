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
public class HueSyncHdmiConnectionInfo {
    /** Friendly name, not empty */
    public @Nullable String name;
    /**
     * Friendly type:
     * generic,
     * video,
     * game,
     * music,
     * xbox,
     * playstation,
     * nintendoswitch,
     * phone,
     * desktop,
     * laptop,
     * appletv,
     * roku,
     * shield,
     * chromecast,
     * firetv,
     * diskplayer,
     * settopbox,
     * satellite,
     * avreceiver,
     * soundbar,
     * hdmiswitch
     */
    public @Nullable String type;
    /**
     * unplugged,
     * plugged,
     * linked,
     * unknown
     */
    public @Nullable String status;
    /**
     * video,
     * game,
     * music
     */
    public @Nullable String lastSyncMode;
}
