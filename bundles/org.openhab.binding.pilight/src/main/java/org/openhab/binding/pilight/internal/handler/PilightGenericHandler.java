/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.pilight.internal.handler;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pilight.internal.PilightChannelConfiguration;
import org.openhab.binding.pilight.internal.dto.Action;
import org.openhab.binding.pilight.internal.dto.Device;
import org.openhab.binding.pilight.internal.dto.Status;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PilightGenericHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Stefan Röllin - Initial contribution
 * @author Niklas Dörfler - Port pilight binding to openHAB 3 + add device discovery
 */
@NonNullByDefault
public class PilightGenericHandler extends PilightBaseHandler {

    private final Logger logger = LoggerFactory.getLogger(PilightGenericHandler.class);

    private final ChannelTypeRegistry channelTypeRegistry;

    private final Map<ChannelUID, Boolean> channelReadOnlyMap = new HashMap<>();

    public PilightGenericHandler(Thing thing, ChannelTypeRegistry channelTypeRegistry) {
        super(thing);
        this.channelTypeRegistry = channelTypeRegistry;
    }

    @Override
    public void initialize() {
        super.initialize();
        initializeReadOnlyChannels();
    }

    @Override
    protected void updateFromStatus(Status status) {
        for (Channel channel : thing.getChannels()) {
            PilightChannelConfiguration config = channel.getConfiguration().as(PilightChannelConfiguration.class);
            updateState(channel.getUID(),
                    getDynamicChannelState(channel, status.getValues().get(config.getProperty())));
        }
    }

    @Override
    void updateFromConfigDevice(Device device) {
    }

    @Override
    protected @Nullable Action createUpdateCommand(ChannelUID channelUID, Command command) {
        if (isChannelReadOnly(channelUID)) {
            logger.debug("Can't apply command '{}' to '{}' because channel is readonly.", command, channelUID.getId());
            return null;
        }

        logger.debug("Create update command for '{}' not implemented.", channelUID.getId());

        return null;
    }

    private State getDynamicChannelState(final Channel channel, final @Nullable String value) {
        final @Nullable String acceptedItemType = channel.getAcceptedItemType();

        if (value == null || acceptedItemType == null) {
            return UnDefType.UNDEF;
        }

        switch (acceptedItemType) {
            case CoreItemFactory.NUMBER:
                return new DecimalType(value);
            case CoreItemFactory.STRING:
                return StringType.valueOf(value);
            case CoreItemFactory.SWITCH:
                return OnOffType.from(value);
            default:
                logger.trace("Type '{}' for channel '{}' not implemented", channel.getAcceptedItemType(), channel);
                return UnDefType.UNDEF;
        }
    }

    private void initializeReadOnlyChannels() {
        channelReadOnlyMap.clear();
        for (Channel channel : thing.getChannels()) {
            final @Nullable ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
            if (channelTypeUID != null) {
                final @Nullable ChannelType channelType = channelTypeRegistry.getChannelType(channelTypeUID, null);

                if (channelType != null) {
                    logger.debug("initializeReadOnly {} {}", channelType, channelType.getState());
                }

                if (channelType != null) {
                    final @Nullable StateDescription state = channelType.getState();
                    if (state != null) {
                        channelReadOnlyMap.putIfAbsent(channel.getUID(), state.isReadOnly());
                    }
                }
            }
        }
    }

    private boolean isChannelReadOnly(ChannelUID channelUID) {
        Boolean isReadOnly = channelReadOnlyMap.get(channelUID);
        return isReadOnly != null ? isReadOnly : true;
    }
}
