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
package org.openhab.binding.freeboxos.internal.handler;

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.TIMESTAMP_POSTFIX;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        node.getMinRefresh().map(Endpoint::refresh).ifPresent(rate -> {
            Configuration thingConfig = editConfiguration();
            thingConfig.put(ApiConsumerConfiguration.REFRESH_INTERVAL, Integer.toString(rate / 1000));
            updateConfiguration(thingConfig);
        });

        properties.putAll(node.props());

        getThing().getChannels().forEach(channel -> {
            Configuration conf = channel.getConfiguration();
            String channelId = channel.getUID().getIdWithoutGroup();
            node.type().endpoints().stream().filter(ep -> ep.name().equals(channelId))
                    .forEach(endPoint -> conf.put(endPoint.epType().asConfId(), endPoint.id()));
            internalConfigureChannel(channelId, conf, node.type().endpoints());
        });
    }

    protected void internalConfigureChannel(String channelId, Configuration conf, List<Endpoint> endpoints) {
        if (channelId.endsWith(TIMESTAMP_POSTFIX)) {
            String baseEndpoint = channelId.replace(TIMESTAMP_POSTFIX, "");
            endpoints.stream().filter(ep -> ep.name().equals(baseEndpoint)).forEach(ep -> {
                conf.put(ep.name(), ep.id());
                conf.put("signal", ep.id());
            });
        }
    }

    @Override
    protected void internalPoll() throws FreeboxException {
        HomeManager homeManager = getManager(HomeManager.class);
        HomeNode node = homeManager.getHomeNode(getClientId());
        List<Channel> linkedChannels = getThing().getChannels().stream().filter(channel -> isLinked(channel.getUID()))
                .toList();

        for (Channel channel : linkedChannels) {
            State result = null;
            Integer slotId = getSlotId(channel.getConfiguration(), EpType.SIGNAL.asConfId());
            if (slotId instanceof Integer) {
                EndpointState state = homeManager.getEndpointsState(getClientId(), slotId);
                Optional<Endpoint> endPoint = node.getEndpoint(slotId);
                if (state != null) {
                    result = getChannelState(channel.getUID().getIdWithoutGroup(), state, endPoint);
                }
            }
            updateState(channel.getUID(), result != null ? result : UnDefType.UNDEF);
        }
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

    protected abstract State getChannelState(String channelId, EndpointState state, Optional<Endpoint> endPoint);
}
