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
package org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.channel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link TvChannelDto} class defines the Data Transfer Object
 * for the Philips TV API /activities/tv endpoint to get and switch tv channels.
 *
 * @author Benjamin Meyer - Initial contribution
 */
public class TvChannelDto {

    @JsonProperty
    private ChannelDto channel;

    @JsonProperty
    private ChannelListDto channelList;

    public ChannelDto getChannel() {
        return channel;
    }

    public ChannelListDto getChannelList() {
        return channelList;
    }

    public void setChannel(ChannelDto channelDto) {
        this.channel = channelDto;
    }

    public void setChannelList(ChannelListDto channelList) {
        this.channelList = channelList;
    }
}
