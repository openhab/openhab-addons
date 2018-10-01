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
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.domintell.internal.config.ModuleConfig;
import org.openhab.binding.domintell.internal.protocol.DomintellRegistry;
import org.openhab.binding.domintell.internal.protocol.ItemConfigChangeHandler;
import org.openhab.binding.domintell.internal.protocol.model.Item;
import org.openhab.binding.domintell.internal.protocol.model.ItemKey;
import org.openhab.binding.domintell.internal.protocol.model.SerialNumber;
import org.openhab.binding.domintell.internal.protocol.model.group.ItemGroup;
import org.openhab.binding.domintell.internal.protocol.model.group.ItemGroupType;
import org.openhab.binding.domintell.internal.protocol.model.module.Module;
import org.openhab.binding.domintell.internal.protocol.model.module.ModuleKey;
import org.openhab.binding.domintell.internal.protocol.model.type.ItemType;
import org.openhab.binding.domintell.internal.protocol.model.type.ModuleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.openhab.binding.domintell.internal.DomintellBindingConstants.*;

/**
 * The {@link DomintellItemGroupHandler} class is a common handler class for all grouped domintell items (e.g variables)
 *
 * @author Gabor Bicskei - Initial contribution
 */
public abstract class DomintellItemGroupHandler extends BaseThingHandler implements ItemConfigChangeHandler {
    private static final Map<ItemType, ChannelTypeUID> CHANNEL_TYPES = Collections.unmodifiableMap(new HashMap<ItemType, ChannelTypeUID>(4, 1.0f) {
        {
            put(ItemType.booleanVar, CHANNEL_TYPE_VARIABLE_BOOLEAN);
            put(ItemType.numericVar, CHANNEL_TYPE_VARIABLE_NUM);
        }
    });

    /**
     * Class logger
     */
    private final Logger logger = LoggerFactory.getLogger(DomintellItemGroupHandler.class);

    /**
     * Items groups
     */
    private ItemGroup itemGroup;

    /**
     * ItemKey/Channel map
     */
    private Map<ItemKey, Channel> channels = new HashMap<>();

    /**
     * Domintell item registry
     */
    private DomintellRegistry registry;

    /**
     * Constructor
     *
     * @param thing Parent thing
     * @param registry Domintell item registry
     */
    DomintellItemGroupHandler(Thing thing, DomintellRegistry registry) {
        super(thing);
        this.registry = registry;
        this.itemGroup = registry.getItemGroup(ItemGroupType.valueOf(thing.getUID().getId()));
        itemGroup.setStateChangeListener(this::itemStateChanged);
        itemGroup.setItemChangeListener(this);
        logger.debug("Group handler created: {}", itemGroup.getType());
    }

    private void touchItems(DomintellRegistry registry) {
        thing.getChannels().forEach(c->{
            ModuleConfig config = c.getConfiguration().as(ModuleConfig.class);
            Module module = registry.getDomintellModule(config.getModuleType(), config.getSerialNumber());
            module.updateState();
        });
    }

    /**
     * Handler for item chane events
     *
     * @param item Changed item
     */
    @Override
    public void groupItemsChanged(Item item) {
        @Nullable ThingHandlerCallback callback = getCallback();
        if (callback != null) {
            ItemKey key = item.getItemKey();
            logger.debug("Group item changed: {}", key);
            Channel oldCh = thing.getChannel(key.toId());
            if (oldCh == null) {
                Configuration config = new Configuration();
                config.put(CONFIG_MODULE_TYPE, key.getModuleKey().getModuleType().toString());
                config.put(CONFIG_SERIAL_NUMBER, key.getModuleKey().getSerialNumber().getAddressInt().toString());

                List<Channel> thingChannels = new ArrayList<>(thing.getChannels());
                ChannelUID channelUID = new ChannelUID(thing.getUID(), key.toId());
                Channel channel = callback.createChannelBuilder(channelUID, CHANNEL_TYPES.get(item.getType()))
                        .withConfiguration(config)
                        .withLabel(item.getLabel())
                        .withDescription(item.getDescription())
                        .build();
                thingChannels.add(channel);

                ThingBuilder thingBuilder = editThing();
                thingBuilder.withChannels(thingChannels);
                updateThing(thingBuilder.build());
                updateStatus(ThingStatus.ONLINE);
            }
        }
    }

    /**
     * Translates channel labels and descriptions for channels connected to items.
     */
    @Override
    public void groupItemsTranslated() {
        logger.debug("Translate group item channels: {}", itemGroup.getType());
        @Nullable ThingHandlerCallback callback = getCallback();
        if (callback != null) {
            //update description
            List<Channel> newChannels = new ArrayList<>();
            for (Item item : itemGroup.getItems()) {
                Channel c = channels.get(item.getItemKey());
                if (c != null) {
                    newChannels.add(callback.editChannel(thing, c.getUID())
                            .withLabel(item.getLabel())
                            .withConfiguration(c.getConfiguration())
                            .build());
                }
            }

            if (newChannels.size() > 0) {
                //order
                TreeMap<String, Channel> orderedMap = new TreeMap<>();
                newChannels.forEach(c -> orderedMap.put(c.getUID().getId(), c));

                ThingBuilder thingBuilder = editThing();
                ArrayList<Channel> orderedChannels = new ArrayList<>(orderedMap.values());

                thingBuilder.withChannels(orderedChannels);
                updateThing(thingBuilder.build());
                updateStatus(ThingStatus.ONLINE);
            }
        }
    }

    public ItemGroup getItemGroup() {
        return itemGroup;
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        mapChannels();
        touchItems(registry);
    }

    /**
     * Creates item key from channel configuration parameters
     *
     * @param channel Channel
     * @return The ItemKey
     */
    ItemKey getItemKeyForChannel(Channel channel) {
        try {
            String moduleTypeStr = (String) channel.getConfiguration().get(CONFIG_MODULE_TYPE);
            if (moduleTypeStr != null) {
                ModuleType moduleType = ModuleType.valueOf(moduleTypeStr);
                SerialNumber serialNumber = new SerialNumber((String) channel.getConfiguration().get(CONFIG_SERIAL_NUMBER));
                return new ItemKey(new ModuleKey(moduleType, serialNumber));
            }
            return null;
        } catch (IllegalArgumentException e) {
            logger.debug("Invalid channel configuration", e);
            return null;
        }
    }

    /**
     * Map channels by item key to help lookup
     */
    private void mapChannels() {
        channels = thing.getChannels().stream()
                .collect(Collectors.toMap(this::getItemKeyForChannel, Function.identity()));
    }

    /**
     * Callback for item change events
     *
     * @param item Updated item
     */
    private void itemStateChanged(Item item) {
        Channel channel = channels.get(item.getItemKey());
        if (channel != null) {
            updateChannel(item, channel);
        }
        updateStatus(ThingStatus.ONLINE);
    }

    /**
     * Handler specific channel update
     *
     * @param item Updated item
     * @param channel Affected channel
     */
    protected abstract void updateChannel(Item item, Channel channel);

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            logger.debug("Item state update command received: {}", itemGroup.getType());
            itemGroup.queryState(getItemKeyForChannel(thing.getChannel(channelUID.getId())));
        }
    }
}
