/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.domintell.internal.handler;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.openhab.binding.domintell.internal.protocol.DomintellRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.openhab.binding.domintell.internal.DomintellBindingConstants.CHANNEL_TYPE_CONTACT;

/**
* The {@link DomintellPushButtonModuleHandler} class is the handler of all physical push button modules
*
* @author Gabor Bicskei - Initial contribution
*/
public class DomintellPushButtonModuleHandler extends DomintellContactModuleHandler {
    /**
     * Class logger
     */
    private final Logger logger = LoggerFactory.getLogger(DomintellPushButtonModuleHandler.class);

    /**
     * Constructor
     *
     * @param thing Parent thing
     * @param registry Domintell item registry
     */
    public DomintellPushButtonModuleHandler(Thing thing, DomintellRegistry registry) {
        super(thing, registry);
        logger.debug("Push button module handler created: {}", getModule().getModuleKey());
    }

    /**
     * Thing channels depend on the push button type. Missing channels are created during the handler initialization.
     */
    @Override
    public void initialize() {
        @Nullable ThingHandlerCallback callback = getCallback();
        if (callback != null) {
            int contactChannelNum = getModule().getItems().size();
            if (thing.getChannels().size() < contactChannelNum + 1) {
                logger.debug("Missing push button channels: {}", getModule().getModuleKey());
                List<Channel> channels = new ArrayList<>();
                for (int i = 0; i < contactChannelNum; i++) {
                    String channelId = String.valueOf(i + 1);
                    Channel c = thing.getChannel(channelId);
                    if (c != null) {
                        channels.add(c);
                    } else {
                        channels.add(callback.createChannelBuilder(new ChannelUID(thing.getUID(), channelId), CHANNEL_TYPE_CONTACT).build());
                        logger.debug("Push button channel created: {}->{}", getModule().getModuleKey(), channelId);
                    }
                }

                updateThing(editThing().withChannels(channels).build());
            } else {
                logger.debug("Push button module has the channels: {}", getModule().getModuleKey());
            }
        }
        super.initialize();
    }
}
