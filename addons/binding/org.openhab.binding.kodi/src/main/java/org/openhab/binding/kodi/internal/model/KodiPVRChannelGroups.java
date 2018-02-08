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
 * Class representing a Kodi PVR channel group list
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class KodiPVRChannelGroups {
    /**
     * The PVR channel type
     */
    private String channelType;

    /**
     * A list of {@link KodiPVRChannelGroup}s
     */
    private List<KodiPVRChannelGroup> channelGroups;

    public String getChannelType() {
        return channelType;
    }

    public void setChannelType(final String channelType) {
        this.channelType = channelType;
    }

    public List<KodiPVRChannelGroup> getChannelgroups() {
        return channelGroups;
    }

    public void setChannelgroups(List<KodiPVRChannelGroup> channelGroups) {
        this.channelGroups = channelGroups;
    }
}
