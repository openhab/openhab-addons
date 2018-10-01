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
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.domintell.internal.config.ModuleConfig;
import org.openhab.binding.domintell.internal.protocol.DomintellRegistry;
import org.openhab.binding.domintell.internal.protocol.ItemConfigChangeHandler;
import org.openhab.binding.domintell.internal.protocol.StateChangeListener;
import org.openhab.binding.domintell.internal.protocol.model.Item;
import org.openhab.binding.domintell.internal.protocol.model.ItemKey;
import org.openhab.binding.domintell.internal.protocol.model.module.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * The {@link DomintellModuleHandler} class is a base handler for all module type handlers
 *
 * @author Gabor Bicskei - Initial contribution
 */
public abstract class DomintellModuleHandler extends BaseThingHandler implements StateChangeListener, ItemConfigChangeHandler {
    /**
     * Class logger
     */
    private final Logger logger = LoggerFactory.getLogger(DomintellModuleHandler.class);

    /**
     * Domintell module
     */
    private Module module;

    DomintellModuleHandler(Thing thing, DomintellRegistry registry) {
        super(thing);
        ModuleConfig config = getConfigAs(ModuleConfig.class);
        this.module = registry.getDomintellModule(config.getModuleType(), config.getSerialNumber());
        module.setStateChangeListener(this);
        module.setConfigChangeListener(this);
    }


    public Module getModule() {
        return module;
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        module.updateState();
    }

    public void groupItemsChanged(Item item) {
        //no implementation in case of modules
    }

    /**
     * Set label and description for channels based on Domintell labels
     */
    @Override
    public void groupItemsTranslated() {
        @Nullable ThingHandlerCallback callback = getCallback();
        if (callback != null) {
            logger.debug("Translating channels for module: {}", module.getModuleKey());
            //update description
            List<Channel> newChannels = new ArrayList<>();
            for (Item item : module.getItems().values()) {
                Channel c = getChannelForItem(item);
                String newLabel = item.getLabel();
                String newDescription = item.getDescription();
                if (newLabel != null && c != null && !newLabel.equals(c.getLabel())
                        && newDescription != null && !newDescription.equals(c.getDescription())) {
                    newChannels.add(callback.editChannel(thing, c.getUID())
                            .withLabel(newLabel)
                            .withDescription(newDescription)
                            .withConfiguration(c.getConfiguration())
                            .build());
                } else {
                    //not changed
                    newChannels.add(c);
                }
            }

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

    /**
     * Find channel for intem
     *
     * @param item Item
     * @return Channel if found
     */
    protected @Nullable Channel getChannelForItem(Item item) {
        return thing.getChannel(item.getItemKey().getIoNumber().toString());
    }

    /**
     * Callback for item change events
     *
     * @param item Updated item
     */
    public void itemStateChanged(Item item) {
        if (getCallback() != null) {
            Channel channel = getChannelForItem(item);
            if (channel != null) {
                updateChannel(item, channel);
            }
            updateStatus(ThingStatus.ONLINE);
        }
    }

    /**
     * Update channel from Dommintell item
     *
     * @param item Updated item
     * @param channel Affected channel
     */
    protected abstract void updateChannel(Item item, Channel channel);

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            logger.trace("Request state refresh for module item: {}", module.getModuleKey());
            module.queryState();
        }
    }

    void refreshChannelFromItem(ChannelUID channelUID, int channelIdx) {
        Item item = getModule().getItems().get(new ItemKey(getModule().getModuleKey(), channelIdx));
        Channel channel = thing.getChannel(channelUID.getId());
        if (channel != null) {
            updateChannel(item, channel);
        }
    }
}
