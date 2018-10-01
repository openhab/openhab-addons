/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.domintell.internal.handler;

import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.domintell.internal.protocol.DomintellRegistry;
import org.openhab.binding.domintell.internal.protocol.model.Item;
import org.openhab.binding.domintell.internal.protocol.model.module.DimmerModule;

/**
 * The {@link DomintellDimmerModuleHandler} class is handler for all dimmer type Domintell modules
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class DomintellDimmerModuleHandler extends DomintellModuleHandler {
    public DomintellDimmerModuleHandler(Thing thing, DomintellRegistry registry) {
        super(thing, registry);
    }

    @Override
    protected void updateChannel(Item item, Channel channel) {
        Integer value = (Integer) item.getValue();
        if (value == null) {
            value = 0;
        }
        updateState(channel.getUID(), new PercentType(value));
    }

    public DimmerModule getModule() {
        return (DimmerModule) super.getModule();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        int channelIdx = Integer.parseInt(channelUID.getId());
        if (OnOffType.OFF == command) {
            getModule().off(channelIdx);
        } else if (OnOffType.ON == command) {
            getModule().on(channelIdx);
        } else if (command instanceof PercentType) {
            getModule().percent(channelIdx, ((PercentType) command).intValue());
        } else if (command == IncreaseDecreaseType.INCREASE) {
            getModule().increase(channelIdx);
        } else if (command == IncreaseDecreaseType.DECREASE) {
            getModule().decrease(channelIdx);
        } else if (command == RefreshType.REFRESH) {
            refreshChannelFromItem(channelUID, channelIdx);
        }
    }
}
