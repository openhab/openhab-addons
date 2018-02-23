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
 * Class representing a Kodi PVR channel group
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class KodiPVRChannelGroup extends KodiBaseItem {
    /**
     * The PVR channel group id
     */
    private int channelGroupId;

    /**
     * The PVR channel type
     */
    private String channelType;

    public int getId() {
        return channelGroupId;
    }

    public void setId(final int channelGroupId) {
        this.channelGroupId = channelGroupId;
    }

    public String getChannelType() {
        return channelType;
    }

    public void setChannelType(final String channelType) {
        this.channelType = channelType;
    }
}
