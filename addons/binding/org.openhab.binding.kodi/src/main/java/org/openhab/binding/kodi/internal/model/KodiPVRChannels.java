/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.kodi.internal.model;

import java.util.List;

/**
 * Class representing a Kodi PVR channel list
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class KodiPVRChannels {
    /**
     * The PVR channel group id of the related {@link KodiPVRChannelGroup}
     */
    private int channelGroupId;

    /**
     * A list of {@link KodiPVRChannel}s
     */
    private List<KodiPVRChannel> channels;

    public int getChannelGroupId() {
        return channelGroupId;
    }

    public void setChannelGroupId(int channelGroupId) {
        this.channelGroupId = channelGroupId;
    }

    public List<KodiPVRChannel> getChannels() {
        return channels;
    }

    public void setChannels(List<KodiPVRChannel> channels) {
        this.channels = channels;
    }
}
