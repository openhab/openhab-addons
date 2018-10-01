/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.domintell.internal.handler;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.domintell.internal.protocol.DomintellRegistry;
import org.openhab.binding.domintell.internal.protocol.model.Item;
import org.openhab.binding.domintell.internal.protocol.model.ItemKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DomintellVariableItemGroupHandler} class is a handler for Domintell item groups
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class DomintellVariableItemGroupHandler extends DomintellItemGroupHandler {
    /**
     * Class logger
     */
    private final Logger logger = LoggerFactory.getLogger(DomintellVariableItemGroupHandler.class);

    public DomintellVariableItemGroupHandler(Thing thing, DomintellRegistry registry) {
        super(thing, registry);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        ItemKey key = getItemKeyForChannel(thing.getChannel(channelUID.getId()));
        logger.debug("Execute command on item: {}->{}", key, command);
        if (OnOffType.ON.equals(command)) {
            getItemGroup().setOutput(key);
        } else if (OnOffType.OFF == command) {
            getItemGroup().resetOutput(key);
        } else {
            super.handleCommand(channelUID, command);
        }
    }

    protected void updateChannel(Item item, Channel channel) {
        logger.trace("Updating channel from item: {}->{}", item, channel.getUID().getId());
        updateState(channel.getUID(), (Boolean) item.getValue() ? OnOffType.ON : OnOffType.OFF);
    }
}
