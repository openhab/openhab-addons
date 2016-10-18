/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RokuInternalProtocol} is responsible for creating things and thing
 * handlers.
 *
 * @author Jarod Peters - Initial contribution
 */
public class RokuCommands {

    private final Logger logger = LoggerFactory.getLogger(RokuCommands.class);

    private RokuNetworkCalls rnc;

    public RokuCommands(String ip, Number port) {
        rnc = new RokuNetworkCalls(ip + ":" + port);
    }

    public void generateAction(ChannelUID channelUID) throws IOException {
        logger.debug("Action requested for channel: " + channelUID.getId());
        switch (channelUID.getId()) {
            case CHANNEL_HOME:
                rnc.postMethod("/keypress/Home");
                break;
            case CHANNEL_PLAY:
                rnc.postMethod("/keypress/Play");
                break;
            case CHANNEL_BACK:
                rnc.postMethod("/keypress/Back");
                break;
            case CHANNEL_REV:
                rnc.postMethod("/keypress/Rev");
                break;
            case CHANNEL_FWD:
                rnc.postMethod("/keypress/Fwd");
                break;
            case CHANNEL_SELECT:
                rnc.postMethod("/keypress/Select");
                break;
            case CHANNEL_LEFT:
                rnc.postMethod("/keypress/Left");
                break;
            case CHANNEL_RIGHT:
                rnc.postMethod("/keypress/Right");
                break;
            case CHANNEL_DOWN:
                rnc.postMethod("/keypress/Down");
                break;
            case CHANNEL_UP:
                rnc.postMethod("/keypress/Up");
                break;
            case CHANNEL_INSTANTREPLAY:
                rnc.postMethod("/keypress/InstantReplay");
                break;
            case CHANNEL_INFO:
                rnc.postMethod("/keypress/Info");
                break;
            case CHANNEL_BACKSPACE:
                rnc.postMethod("/keypress/Backspace");
                break;
            case CHANNEL_SEARCH:
                rnc.postMethod("/keypress/Search");
                break;
            case CHANNEL_ENTER:
                rnc.postMethod("/keypress/Enter");
                break;
            default:
                throw new IOException("No action corresponding to CHANNEL " + channelUID.getId() + " requested");
        }
    }
}
