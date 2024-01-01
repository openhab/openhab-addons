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
package org.openhab.binding.kodi.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Class representing a Kodi profile (see https://kodi.wiki/view/JSON-RPC_API/v8#Profiles.GetProfiles)
 *
 * @author Jan Hendriks - Initial contribution
 */
@NonNullByDefault
public class KodiProfile extends KodiBaseItem {
    private int lockmode;
    private String thumbnail = "";

    public int getLockmode() {
        return lockmode;
    }

    public void setLockmode(int lockmode) {
        this.lockmode = lockmode;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }
}
