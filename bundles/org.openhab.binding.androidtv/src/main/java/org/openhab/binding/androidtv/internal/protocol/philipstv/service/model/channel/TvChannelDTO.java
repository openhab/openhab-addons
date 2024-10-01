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
package org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.channel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link TvChannelDTO} class defines the Data Transfer Object
 * for the Philips TV API /activities/tv endpoint to get and switch tv channels.
 *
 * @author Benjamin Meyer - Initial contribution
 * @author Ben Rosenblum - Merged into AndroidTV
 */
public class TvChannelDTO {

    @JsonProperty("channel")
    private ChannelDTO channel;

    @JsonProperty("channelList")
    private ChannelListDTO channelList;

    public TvChannelDTO() {
    }

    public TvChannelDTO(ChannelDTO channel, ChannelListDTO channelList) {
        this.channel = channel;
        this.channelList = channelList;
    }

    public ChannelDTO getChannel() {
        return channel;
    }

    public ChannelListDTO getChannelList() {
        return channelList;
    }

    public void setChannel(ChannelDTO channelDTO) {
        this.channel = channelDTO;
    }

    public void setChannelList(ChannelListDTO channelList) {
        this.channelList = channelList;
    }
}
