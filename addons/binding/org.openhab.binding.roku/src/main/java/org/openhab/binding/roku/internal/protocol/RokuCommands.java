/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.roku.internal.protocol;

import static org.openhab.binding.roku.RokuBindingConstants.*;

import java.io.IOException;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RokuCommands} is responsible sending a command to a Roku device via {@link RokueNetworkCalls}.
 *
 * @author Jarod Peters - Initial contribution
 * @author Shawn Wilsher - Use HttpUtil class
 */
public class RokuCommands {

    private final Logger logger = LoggerFactory.getLogger(RokuCommands.class);

    private final String ip;
    private final Number port;

    public RokuCommands(String ip, Number port) {
        this.ip = ip;
        this.port = port;
    }

    public void generateAction(ChannelUID channelUID) throws IOException {
        logger.debug("Action requested for channel: " + channelUID.getId());
        switch (channelUID.getId()) {
            case CHANNEL_HOME:
                postMethod("/keypress/Home");
                break;
            case CHANNEL_PLAY:
                postMethod("/keypress/Play");
                break;
            case CHANNEL_BACK:
                postMethod("/keypress/Back");
                break;
            case CHANNEL_REV:
                postMethod("/keypress/Rev");
                break;
            case CHANNEL_FWD:
                postMethod("/keypress/Fwd");
                break;
            case CHANNEL_SELECT:
                postMethod("/keypress/Select");
                break;
            case CHANNEL_LEFT:
                postMethod("/keypress/Left");
                break;
            case CHANNEL_RIGHT:
                postMethod("/keypress/Right");
                break;
            case CHANNEL_DOWN:
                postMethod("/keypress/Down");
                break;
            case CHANNEL_UP:
                postMethod("/keypress/Up");
                break;
            case CHANNEL_INSTANTREPLAY:
                postMethod("/keypress/InstantReplay");
                break;
            case CHANNEL_INFO:
                postMethod("/keypress/Info");
                break;
            case CHANNEL_BACKSPACE:
                postMethod("/keypress/Backspace");
                break;
            case CHANNEL_SEARCH:
                postMethod("/keypress/Search");
                break;
            case CHANNEL_ENTER:
                postMethod("/keypress/Enter");
                break;
            default:
                throw new IOException("No action corresponding to CHANNEL " + channelUID.getId() + " requested");
        }
    }

    private void postMethod(String url) throws IOException {
        HttpUtil.executeUrl("POST", "http://" + ip + ":" + port + url, 5000);
    }
}
