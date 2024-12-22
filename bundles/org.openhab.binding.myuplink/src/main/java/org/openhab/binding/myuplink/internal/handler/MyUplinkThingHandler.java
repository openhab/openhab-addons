/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.myuplink.internal.handler;

import static org.openhab.binding.myuplink.internal.MyUplinkBindingConstants.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.myuplink.internal.Utils;
import org.openhab.binding.myuplink.internal.command.MyUplinkCommand;
import org.openhab.binding.myuplink.internal.config.MyUplinkConfiguration;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;

/**
 * public interface of the {@link MyUplinkThingHandler}. provides some default implementations which can be used by both
 * BridgeHandlers and ThingHandlers.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public interface MyUplinkThingHandler extends ThingHandler, ChannelProvider {

    /**
     * just to avoid usage of static loggers.
     *
     * @return
     */
    Logger getLogger();

    /**
     * method which updates the channels. needs to be implemented by thing types which have channels.
     *
     * @param values key-value list where key is the channel
     */
    default void updateChannelStatus(Map<Channel, State> values) {
        getLogger().debug("updateChannelStatus not implemented/supported by this thing type");
    }

    /**
     * return the bridge's configuration
     *
     * @return
     */
    MyUplinkConfiguration getBridgeConfiguration();

    /**
     * passes a new command o the command queue
     *
     * @param command to be queued/executed
     */
    void enqueueCommand(MyUplinkCommand command);

    /**
     * default implementation to handle commands
     *
     * @param channelUID
     * @param command
     */
    @Override
    default void handleCommand(ChannelUID channelUID, Command command) {
        getLogger().debug("command for {}: {}", channelUID, command);

        if (command instanceof RefreshType) {
            return;
        }

        String group = channelUID.getGroupId();
        group = group == null ? "" : group;
        Channel channel = getChannel(group, channelUID.getIdWithoutGroup());
        if (channel == null) {
            // this should not happen
            getLogger().warn("channel not found: {}", channelUID);
            return;
        }

        String channelType = Utils.getChannelTypeId(channel);
        if (!channelType.startsWith(CHANNEL_TYPE_PREFIX_RW)) {
            getLogger().warn("channel '{}', type '{}' does not support write access - value to set '{}'",
                    channelUID.getIdWithoutGroup(), channelType, command);
            throw new UnsupportedOperationException(
                    "channel (" + channelUID.getIdWithoutGroup() + ") does not support write access");
        }

        if (getThing().getStatus() != ThingStatus.ONLINE) {
            getLogger().debug("Thing is not online, thus no commands will be handled");
            return;
        }

        try {
            enqueueCommand(buildMyUplinkCommand(command, channel));
        } catch (UnsupportedOperationException e) {
            getLogger().warn("Unsupported command: {}", e.getMessage());
        }
    }

    /**
     * builds the MyUplinkCommand which can be send to the webinterface.
     *
     * @param command the openhab raw command received from the framework
     * @param channel the channel which belongs to the command.
     * @return
     */
    default MyUplinkCommand buildMyUplinkCommand(Command command, Channel channel) {
        throw new UnsupportedOperationException("buildMyUplinkCommand not implemented/supported by this thing type");
    }

    /**
     * determines the channel for a given groupId and channelId.
     *
     * @param groupId groupId of the channel
     * @param channelId channelId of the channel
     */
    @Override
    default @Nullable Channel getChannel(String groupId, String channelId) {
        ThingUID thingUID = this.getThing().getUID();
        ChannelUID channelUID;
        if (!groupId.isEmpty()) {
            ChannelGroupUID channelGroupUID = new ChannelGroupUID(thingUID, groupId);
            channelUID = new ChannelUID(channelGroupUID, channelId);
        } else {
            channelUID = new ChannelUID(thingUID, channelId);
        }
        return getThing().getChannel(channelUID);
    }
}
