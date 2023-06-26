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
package org.openhab.binding.freeboxos.internal.handler;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager.Endpoint;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager.EndpointState;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager.EpType;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager.HomeNode;
import org.openhab.binding.freeboxos.internal.config.ApiConsumerConfiguration;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HomeNodeHandler} is the base class for handler of home node things.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public abstract class HomeNodeHandler extends ApiConsumerHandler {
    private final Logger logger = LoggerFactory.getLogger(HomeNodeHandler.class);

    public HomeNodeHandler(Thing thing) {
        super(thing);
    }

    @Override
    void initializeProperties(Map<String, String> properties) throws FreeboxException {
        HomeNode node = getManager(HomeManager.class).getHomeNode(getClientId());

        // Gets the lowest refresh time or else, we'll keep configuration default
        node.showEndpoints().stream().filter(ep -> ep.epType() == EpType.SIGNAL).filter(ep -> ep.refresh() != 0)
                .min(Comparator.comparing(Endpoint::refresh)).map(Endpoint::refresh).ifPresent(rate -> {
                    Configuration thingConfig = editConfiguration();
                    thingConfig.put(ApiConsumerConfiguration.REFRESH_INTERVAL, Integer.toString(rate / 1000));
                    updateConfiguration(thingConfig);
                });

        properties.putAll(node.props());

        getThing().getChannels().forEach(channel -> {
            Configuration conf = channel.getConfiguration();
            node.type().endpoints().stream().filter(ep -> ep.name().equals(channel.getUID().getIdWithoutGroup()))
                    .forEach(endPoint -> conf.put(endPoint.epType().asConfId(), endPoint.id()));
            internalConfigureChannel(channel.getUID().getIdWithoutGroup(), conf, node.type().endpoints());
        });
    }

    protected void internalConfigureChannel(String channelId, Configuration conf, List<Endpoint> endpoints) {
    }

    @Override
    protected void internalPoll() throws FreeboxException {
        HomeManager homeManager = getManager(HomeManager.class);
        getThing().getChannels().stream().filter(channel -> isLinked(channel.getUID())).forEach(channel -> {
            State result = UnDefType.UNDEF;
            Integer slotId = getSlotId(channel.getConfiguration(), EpType.SIGNAL.asConfId());
            if (slotId instanceof Integer) {
                try {
                    EndpointState state = homeManager.getEndpointsState(getClientId(), slotId);
                    if (state != null) {
                        result = getChannelState(homeManager, channel.getUID().getIdWithoutGroup(), state);
                    } else {
                        result = getChannelState(homeManager, channel.getUID().getIdWithoutGroup());
                    }
                } catch (FreeboxException e) {
                    logger.warn("Error updating channel: {}", e.getMessage());
                }
            } else {
                result = getChannelState(homeManager, channel.getUID().getIdWithoutGroup());
            }
            updateState(channel.getUID(), result);
        });
    }

    @Override
    protected boolean internalHandleCommand(String channelId, Command command) throws FreeboxException {
        Channel channel = getThing().getChannel(channelId);
        if (channel != null) {
            Configuration config = channel.getConfiguration();
            String channelWG = channel.getUID().getIdWithoutGroup();
            Integer slotId = getSlotId(config, EpType.SLOT.asConfId());
            HomeManager homeManager = getManager(HomeManager.class);
            return slotId instanceof Integer ? executeSlotCommand(homeManager, channelWG, command, config, slotId)
                    : executeChannelCommand(homeManager, channelWG, command, config);
        }
        return super.internalHandleCommand(channelId, command);
    }

    protected @Nullable Integer getSlotId(Configuration configuration, String endPoint) {
        Object slot = configuration.get(endPoint);
        return slot instanceof BigDecimal slotId ? slotId.intValue() : null;
    }

    protected boolean executeChannelCommand(HomeManager homeManager, String channelId, Command command,
            Configuration config) throws FreeboxException {
        return false;
    }

    protected boolean executeSlotCommand(HomeManager homeManager, String channelId, Command command,
            Configuration config, int slotId) throws FreeboxException {
        return false;
    }

    protected State getChannelState(HomeManager homeManager, String channelWG) {
        return UnDefType.UNDEF;
    }

    protected abstract State getChannelState(HomeManager homeManager, String channelId, EndpointState state);
}
