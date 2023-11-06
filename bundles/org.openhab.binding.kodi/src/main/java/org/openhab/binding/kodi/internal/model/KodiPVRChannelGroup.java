/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
