/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.easee.internal.handler;

import static org.openhab.binding.easee.internal.EaseeBindingConstants.CHANNEL_TYPEPREFIX_RW;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.easee.internal.Utils;
import org.openhab.binding.easee.internal.command.EaseeCommand;
import org.openhab.binding.easee.internal.config.EaseeConfiguration;
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
 * public interface of the {@link EaseeThingHandler}. provides some default implementations which can be used by both
 * BridgeHandlers and ThingHandlers.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public interface EaseeThingHandler extends ThingHandler, ChannelProvider {

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
    EaseeConfiguration getBridgeConfiguration();

    /**
     * passes a new command o the command queue
     *
     * @param command to be queued/executed
     */
    void enqueueCommand(EaseeCommand command);

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
        if (!channelType.startsWith(CHANNEL_TYPEPREFIX_RW)) {
            getLogger().info("channel '{}' does not support write access - value to set '{}'",
                    channelUID.getIdWithoutGroup(), command);
            throw new UnsupportedOperationException(
                    "channel (" + channelUID.getIdWithoutGroup() + ") does not support write access");
        }

        if (getThing().getStatus() != ThingStatus.ONLINE) {
            getLogger().debug("Thing is not online, thus no commands will be handled");
            return;
        }
        enqueueCommand(buildEaseeCommand(command, channel));
    }

    /**
     * builds the EaseeCommand which can be send to the webinterface.
     *
     * @param command the openhab raw command received from the framework
     * @param channel the channel which belongs to the command.
     * @return
     */
    default EaseeCommand buildEaseeCommand(Command command, Channel channel) {
        throw new UnsupportedOperationException("buildEaseeCommand not implemented/supported by this thing type");
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
        ChannelGroupUID channelGroupUID = new ChannelGroupUID(thingUID, groupId);
        Channel channel = getThing().getChannel(new ChannelUID(channelGroupUID, channelId));
        return channel;
    }
}
