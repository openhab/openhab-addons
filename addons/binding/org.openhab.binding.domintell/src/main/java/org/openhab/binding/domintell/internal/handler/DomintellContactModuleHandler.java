/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.domintell.internal.handler;

import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.domintell.internal.config.ContactItemConfig;
import org.openhab.binding.domintell.internal.protocol.DomintellRegistry;
import org.openhab.binding.domintell.internal.protocol.model.Item;
import org.openhab.binding.domintell.internal.protocol.model.ItemKey;
import org.openhab.binding.domintell.internal.protocol.model.module.ContactModule;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

import static org.openhab.binding.domintell.internal.DomintellBindingConstants.CHANNEL_COMMAND;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * The {@link DomintellContactModuleHandler} class is handler class for all contact provider modules.
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class DomintellContactModuleHandler extends DomintellModuleHandler {
    //supported commands
    private static final String SHORT_PUSH = "ShortPush";
    private static final String LONG_PUSH = "LongPush";
    /**
     * Class logger
     */
    private final Logger logger = getLogger(DomintellContactModuleHandler.class);

    public DomintellContactModuleHandler(Thing thing, DomintellRegistry registry) {
        super(thing, registry);
        logger.debug("Contact module handler created: {}", getModule().getModuleKey());
    }

    @Override
    protected void updateChannel(Item item, Channel channel) {
        //logger.debug("Updating channel from item: {}->{}", item, channel.getUID().getId());
        ContactItemConfig config = channel.getConfiguration().as(ContactItemConfig.class);
        Boolean value = getItemValue(item, config);

        OpenClosedType state = value ? OpenClosedType.CLOSED : OpenClosedType.OPEN;
        updateState(channel.getUID(), state);

        if (state == OpenClosedType.CLOSED) {
            int resetTimeout = config.getResetTimeout();
            if (resetTimeout > 0) {
                scheduler.schedule(() -> {
                            updateState(channel.getUID(), OpenClosedType.OPEN);
                            Boolean b = getItemValue(item, config);
                            if (b) {
                                updateState(channel.getUID(), OpenClosedType.CLOSED);
                                logger.trace("Item channel reset: {}", item.getItemKey());
                            }
                        },
                        resetTimeout,
                        TimeUnit.SECONDS);
                logger.trace("Automatic reset requested for channel: {}", channel.getUID().getId());
            }
        }
    }

    private Boolean getItemValue(Item item, ContactItemConfig config) {
        Boolean value = (Boolean) item.getValue();
        if (value == null) {
            value = false;
        }
        if (config.isInverted()) {
            value = !value;
            logger.trace("Item value inverted: {}", item.getItemKey());
        }
        return value;
    }

    public ContactModule getModule() {
        return (ContactModule) super.getModule();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Command received on channel: {}->{}", channelUID, command);
        if (command instanceof StringType) {
            String cmd = command.toString();
            if (CHANNEL_COMMAND.equals(channelUID.getId()) && cmd != null) {
                try {
                    String[] parts = cmd.split("-");
                    int idx = Integer.parseInt(parts[1]);
                    logger.trace("Executing command: {}->{}", new ItemKey(getModule().getModuleKey(), idx), cmd);
                    if (SHORT_PUSH.equals(parts[0])) {
                        getModule().shortPush(idx);
                    } else if (LONG_PUSH.equals(parts[0])) {
                        getModule().longPush(idx);
                    } else {
                        logger.debug("Unknown command: {}->{}", new ItemKey(getModule().getModuleKey(), cmd));
                    }
                } catch (NumberFormatException e) {
                    logger.debug("Unknown command: {}->{}", new ItemKey(getModule().getModuleKey(), cmd));
                }
            }
        } else if (command == RefreshType.REFRESH) {
            if (!CHANNEL_COMMAND.equals(channelUID.getId())) {
                int idx = Integer.parseInt(channelUID.getId());
                refreshChannelFromItem(channelUID, idx);
            }
        }
    }
}
