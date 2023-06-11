/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

/**
 * Class representing a Kodi PVR channel
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class KodiPVRChannel extends KodiBaseItem {
    /**
     * The PVR channel id
     */
    private int channelId;
    /**
     * The PVR channel group id
     */
    private int channelGroupId;

    public int getId() {
        return channelId;
    }

    public void setId(int channelId) {
        this.channelId = channelId;
    }

    public int getChannelGroupId() {
        return channelGroupId;
    }

    public void setChannelGroupId(int channelGroupId) {
        this.channelGroupId = channelGroupId;
    }
}
