/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * @author Ganesh Ingle <ganesh.ingle@asvilabs.com>
 */

package org.openhab.binding.wakeonlan.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.TypeParser;
import org.openhab.binding.wakeonlan.SmarthomeCommandHelper;
import org.slf4j.Logger;

/**
 * Helper class to send one or more commands to openhab items or channels
 *
 * @author Ganesh Ingle - Initial contribution
 *
 */
// @Component
public class SmarthomeCommandHelperImpl implements SmarthomeCommandHelper {

    private static Map<String, Long> lastCommandTimeByItem = new HashMap<String, Long>();
    public static final int COMMAND_DELAY_MS = 200; // Minimum delay between commands to same target
    // protected WakeOnLanHandlerFactory factory = null;
    // @Reference(policy = ReferencePolicy.DYNAMIC)
    protected volatile ThingRegistry thingRegistry;
    // @Reference(policy = ReferencePolicy.DYNAMIC)
    protected volatile ItemRegistry itemRegistry;
    // @Reference(policy = ReferencePolicy.DYNAMIC)
    protected volatile ItemChannelLinkRegistry itemChannelLinkRegistry;
    // @Reference(policy = ReferencePolicy.DYNAMIC)
    protected volatile EventPublisher eventPublisher;

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.wakeonlan.SmarthomeCommandHelper#handleCommands(java.lang.String, org.slf4j.Logger)
     */
    @Override
    public void handleCommands(String commandsIn, Logger logger) throws InterruptedException {
        String commands = commandsIn;
        if (commands.contains("//")) {
            commands = commands.split("//", 2)[0];
        }
        for (String cmd : commands.trim().split("&&")) {
            cmd = cmd.trim();
            handleCommand(cmd, logger);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.wakeonlan.SmarthomeCommandHelper#handleCommand(java.lang.String, org.slf4j.Logger)
     */
    @Override
    public void handleCommand(String cmd, final Logger logger) throws InterruptedException {
        boolean isDelay = false;
        if (cmd.toLowerCase().startsWith("delay")) {
            isDelay = true;
        }
        try {
            MethodCaller.call(new MethodCaller.ActionWithException<Void>() {
                @Override
                public Void call() throws InterruptedException {
                    doHandleCommand(cmd, logger);
                    return null;
                }
            }, isDelay ? 5000 : 2000);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof InterruptedException) {
                throw (InterruptedException) e.getCause();
            } else {
                logger.warn(e.getCause().toString(), e.getCause());
            }
        } catch (TimeoutException e) {
            logger.warn("Timeout while handling command '{}'. Error : '{}'", cmd, e.toString());
            logger.warn(e.toString(), e);
        }
    }

    private void doHandleCommand(@NonNull String cmdIn, @NonNull Logger logger) throws InterruptedException {
        String cmd = cmdIn.trim();
        String delayCmd = cmd.toLowerCase().trim();
        if (delayCmd.startsWith("delay")) {
            delayCmd = delayCmd.replace("delay", "");
            delayCmd = delayCmd.trim();
            Long delay = Long.parseLong(delayCmd);
            Thread.sleep(delay);
            return;
        }
        if (cmd.contains("//")) {
            cmd = cmd.split("//", 2)[0];
        }
        if (cmd == null || cmd.trim().equals("")) {
            return;
        }
        String[] parts = cmd.split("[ ]*[|][ ]*", 2);
        if (parts != null && (parts.length == 1 || parts.length == 2)) {
            String itemOrThingOrChannelName = parts[0];
            String command = null;
            if (parts.length == 2) {
                command = parts[1];
            }
            if (command == null || command.trim().length() == 0) {
                // command = "TOGGLE";
                logger.debug("Empty command");
                return;
            }
            command = command.trim();
            if (itemOrThingOrChannelName != null && command != null) {
                itemOrThingOrChannelName = itemOrThingOrChannelName.trim();
                command = command.trim();
                String[] uidParts = itemOrThingOrChannelName.split(":");
                ChannelUID channelUID = null;
                Thing targetDevice = null;
                logger.debug("Looking for device '{}'", itemOrThingOrChannelName);
                if (uidParts != null && (uidParts.length == 4 || uidParts.length == 5)) {
                    channelUID = new ChannelUID(itemOrThingOrChannelName);
                    logger.debug("Looking for thing which has channel '{}'", channelUID);
                    targetDevice = thingRegistry.get(channelUID.getThingUID());
                    if (targetDevice == null) {
                        logger.debug("Thing not found '{}'", channelUID.getThingUID());
                        if (uidParts.length == 4) {
                            channelUID = null;
                            logger.debug("Looking for thing '{}'", itemOrThingOrChannelName);
                            ThingUID tuid = new ThingUID(itemOrThingOrChannelName);
                            targetDevice = thingRegistry.get(tuid);
                            if (targetDevice == null) {
                                logger.debug("Thing not found '{}'", tuid);
                            } else {
                                logger.debug("Thing found '{}'", tuid);
                            }
                        }
                    } else {
                        logger.debug("Thing found '{}'", channelUID.getThingUID());
                    }
                } else if (uidParts != null && uidParts.length == 3) {
                    logger.debug("Looking for thing '{}'", itemOrThingOrChannelName);
                    ThingUID tuid = new ThingUID(itemOrThingOrChannelName);
                    targetDevice = thingRegistry.get(tuid);
                    if (targetDevice == null) {
                        logger.warn("Thing not found '{}'", tuid);
                    } else {
                        logger.debug("Thing found '{}'", tuid);
                    }
                }

                if (targetDevice != null && channelUID == null) {
                    // try to infer channel
                    if (targetDevice.getHandler() != null) {
                        logger.warn("Couldn't determine target channel for command '{}' and Thing '{}'",
                                targetDevice.getLabel(), command);
                        return;
                    }
                }

                postItemOrChannelCommand(itemOrThingOrChannelName, channelUID, targetDevice, command, logger);
            }
        } else {
            logger.warn("Invalid command '{}'", cmd);
        }
    }

    private void postItemOrChannelCommand(String commandTargetName, ChannelUID channelUIDIn, Thing tIn, String value,
            @NonNull Logger logger) {
        // ServiceReference<ItemRegistry> itemRegistryRef = bundleContext.getServiceReference(ItemRegistry.class);
        // ServiceReference<EventPublisher> eventPublisherRef = bundleContext.getServiceReference(EventPublisher.class);
        // ItemRegistry itemRegistry = bundleContext.getService(itemRegistryRef);
        // EventPublisher eventPublisher = bundleContext.getService(eventPublisherRef);
        ChannelUID channelUID = channelUIDIn;
        Thing t = tIn;
        Item item = null;
        int numItems = 1;
        ThingHandler h = null;
        Set<Item> items = null;
        if (t != null) {
            h = t.getHandler();
            items = (itemChannelLinkRegistry != null) ? itemChannelLinkRegistry.getLinkedItems(channelUID) : null;
            if (items != null && items.size() == 0) {
                items = null;
            }
            if (items != null && items.size() > 0) {
                item = items.iterator().next();
                numItems = items.size();
            }
        } else {
            logger.debug("Searching for Item '{}'", commandTargetName);
            try {
                if (itemRegistry != null) {
                    item = itemRegistry.getItem(commandTargetName);
                }
            } catch (ItemNotFoundException e) {
            } catch (NoSuchMethodError e) {
                logger.warn(e.toString(), e);
            }
            logger.debug("Item = '{}'", item);
            if (item == null) {
                // try to find Thing with given label or UID
                logger.debug("Searching for ChannelUID '{}'", commandTargetName);
                // Set<Thing> candidates = new HashSet<>();
                Channel channel = thingRegistry.getChannel(new ChannelUID(commandTargetName));
                if (channel != null) {
                    t = thingRegistry.get(channel.getUID().getThingUID());
                    if (t != null) {
                        h = t.getHandler();
                        logger.debug("Found Channel");
                        channelUID = channel.getUID();
                        logger.debug("Trying to find Items linked to Channel");
                        if (itemChannelLinkRegistry != null) {
                            items = itemChannelLinkRegistry.getLinkedItems(channelUID);
                            if (items != null && items.size() == 0) {
                                items = null;
                            }
                            if (items != null && items.size() > 0) {
                                item = items.iterator().next();
                                numItems = items.size();
                            }
                            logger.debug("Item = '{}'", item);
                        } else {
                            logger.warn("ItemChannelLinkRegistry not available");
                        }
                    }
                } else {
                    logger.warn("ChannelUID not found '{}'", commandTargetName);
                }
            }
        }
        Command command = null;
        if (item != null || t != null) {
            logger.debug("Item => {}", item);
            if (item != null) {
                logger.debug("Item Category => {}", item.getCategory());
                logger.debug("Item Type => {}", item.getClass().getName());
                logger.debug("Item State => {}", item.getState());
            }
            if (t != null) {
                logger.debug("Thing => {}", t.getUID());
            } else {
                logger.debug("Thing => not found");
            }

            if (item != null && numItems == 1) {
                command = TypeParser.parseCommand(item.getAcceptedCommandTypes(), value);
            } else if (item != null && items != null && numItems > 1) {
                for (Item i : items) {
                    command = TypeParser.parseCommand(i.getAcceptedCommandTypes(), value);
                    if (command != null) {
                        break;
                    }
                }
            } else {
                command = new StringType(value);
            }

            if (command != null) {
                String selectedTarget = null;
                if (item != null && (numItems == 1)) {
                    selectedTarget = item.getName();
                } else {
                    selectedTarget = channelUID.getAsString();
                }
                boolean first = false;
                boolean skip = true;

                synchronized (lastCommandTimeByItem) {
                    if (lastCommandTimeByItem.get(selectedTarget) == null) {
                        lastCommandTimeByItem.put(selectedTarget, System.currentTimeMillis());
                        first = true;
                    }
                    if (first || (System.currentTimeMillis()
                            - lastCommandTimeByItem.get(selectedTarget)) > COMMAND_DELAY_MS) {
                        lastCommandTimeByItem.put(selectedTarget, System.currentTimeMillis());
                        skip = false;
                    }
                }

                if (!skip) {
                    logger.debug("Posting command '{}' to target '{}'.", command.toString(), commandTargetName);
                    if (item != null && (numItems == 1)) {
                        postCommandToItemChannelsDirectly(item.getName(), command, logger);
                    } else {
                        postCommandToThingHandler(h, channelUID, command, logger);
                    }
                }
            } else {
                logger.warn("Invalid command '{}' on item or channel '{}'.", value, commandTargetName);
            }
        } else {
            logger.info("Command '{}' for the unknown device '{}'.", value, commandTargetName);
        }
    }

    private void postCommandToItemChannelsDirectly(String itemName, Command command, @NonNull Logger logger) {
        Set<ChannelUID> chTargets = (itemChannelLinkRegistry != null)
                ? itemChannelLinkRegistry.getBoundChannels(itemName)
                : null;
        if (chTargets != null && chTargets.size() > 0) {
            logger.debug("Posting command '{}' to channels of item '{}'.", command.toString(), itemName);
            for (ChannelUID ch : chTargets) {
                postCommandToThingHandler(null, ch, command, logger);
            }
        } else {
            postCommandToItemViaFramework(itemName, command, logger);
        }
    }

    protected void postCommandToItemViaFramework(String itemName, Command command, Logger logger) {
        logger.debug("Posting command '{}' to item '{}'.", command.toString(), itemName);
        eventPublisher.post(ItemEventFactory.createCommandEvent(itemName, command));
    }

    protected void postCommandToThingHandler(ThingHandler hIn, ChannelUID channelUID, Command command, Logger logger) {
        ThingHandler h = hIn;
        if (h == null && channelUID != null) {
            Channel ch = thingRegistry.getChannel(channelUID);
            if (ch != null) {
                Thing t = thingRegistry.get(ch.getUID().getThingUID());
                if (t != null) {
                    h = t.getHandler();
                } else {
                    logger.debug("Thing '{}' not found", channelUID.getThingUID());
                }
            } else {
                logger.debug("Channel '{}' not found", channelUID);
            }
        }
        if (h != null) {
            logger.debug("Posting command '{}' to channel '{}'.", command.toString(), channelUID.getAsString());
            h.handleCommand(channelUID, command);
        } else {
            logger.warn("Command '{}' on channel '{}' cannot be executed because Thing handler is unavailble.", command,
                    channelUID);
        }
    }
}
