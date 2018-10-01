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
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.domintell.internal.protocol.DomintellRegistry;
import org.openhab.binding.domintell.internal.protocol.model.Item;
import org.openhab.binding.domintell.internal.protocol.model.module.ReleyModule;

/**
 * The {@link DomintellReleyModuleHandler} class is handler for all reley type Domintell modules
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class DomintellReleyModuleHandler extends DomintellModuleHandler {
    public DomintellReleyModuleHandler(Thing thing, DomintellRegistry registry) {
        super(thing, registry);
    }

    @Override
    protected void updateChannel(Item item, Channel channel) {
        Boolean value = (Boolean) item.getValue();
        updateState(channel.getUID(), value != null && value ? OnOffType.ON : OnOffType.OFF);
    }

    public ReleyModule getModule() {
        return (ReleyModule) super.getModule();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        int channelIdx = Integer.parseInt(channelUID.getId());
        if (OnOffType.OFF == command) {
            getModule().resetOutput(channelIdx);
        } else if (OnOffType.ON == command) {
            getModule().setOutput(channelIdx);
        } else if (command == RefreshType.REFRESH) {
            refreshChannelFromItem(channelUID, channelIdx);
        }
    }
}
