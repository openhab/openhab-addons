/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.modbus.handler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.internal.config.ModbusPollerConfiguration;
import org.openhab.binding.modbus.internal.handler.Poller2Channel;
import org.openhab.binding.modbus.internal.handler.Poller2ChannelConfiguration;
import org.openhab.core.io.transport.modbus.AsyncModbusFailure;
import org.openhab.core.io.transport.modbus.AsyncModbusReadResult;
import org.openhab.core.io.transport.modbus.ModbusReadRequestBlueprint;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;

/**
 * Handler for the poller-owned channel model.
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class ModbusPoller2ThingHandler extends ModbusPollerThingHandler {

    private volatile Map<ChannelUID, Poller2Channel> channels = Map.of();
    private volatile int pollStart;

    public ModbusPoller2ThingHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public synchronized void initialize() {
        ModbusPollerConfiguration configuration = getConfigAs(ModbusPollerConfiguration.class);
        pollStart = configuration.getStart();
        Map<ChannelUID, Poller2Channel> parsedChannels = new LinkedHashMap<>();
        for (Channel channel : getThing().getChannels()) {
            Poller2ChannelConfiguration parsed = Poller2ChannelConfiguration.create(channel.getUID().getId(),
                    channel.getConfiguration().getProperties(), configuration.getStart(), configuration.getLength());
            Optional<String> error = parsed.error();
            if (error.isPresent()) {
                channels = Map.of();
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, error.get());
                return;
            }
            parsedChannels.put(channel.getUID(), parsed.channel().orElseThrow());
        }
        channels = Map.copyOf(parsedChannels);
        super.initialize();
    }

    @Override
    protected void onPollResult(AsyncModbusReadResult result) {
        result.getRegisters().ifPresent(registers -> channels
                .forEach((uid, channel) -> updateState(uid, channel.acceptRegisters(registers, pollStart))));
    }

    @Override
    protected void onPollFailure(AsyncModbusFailure<ModbusReadRequestBlueprint> failure) {
        channels.forEach((uid, channel) -> updateState(uid, channel.acceptReadFailure()));
    }
}
