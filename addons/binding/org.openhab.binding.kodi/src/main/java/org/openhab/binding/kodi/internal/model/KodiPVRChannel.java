/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
