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
package org.openhab.binding.ipcamera.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

/**
 * The {@link ChannelTracking} Can be used to find the handle for a HTTP channel if you know the URL. The reply can
 * optionally be stored for later use.
 *
 *
 * @author Matthew Skinner - Initial contribution
 */

@NonNullByDefault
public class ChannelTracking {
    private String storedReply = "";
    private String requestUrl = "";
    private Channel channel;

    public ChannelTracking(Channel channel, String requestUrl) {
        this.channel = channel;
        this.requestUrl = requestUrl;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public Channel getChannel() {
        return channel;
    }

    /**
     * Closes the channel, but keeps the HTTP reply stored in the tracker.
     *
     * @return ChannelFuture
     */
    public ChannelFuture closeChannel() {
        return channel.close();
    }

    public String getReply() {
        return storedReply;
    }

    public void setReply(String replyToStore) {
        storedReply = replyToStore;
    }

    public void setChannel(Channel ch) {
        channel = ch;
    }
}
