/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.modbus.handler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.internal.ModbusBindingConstantsInternal;
import org.openhab.binding.modbus.internal.ModbusPoller2ChannelTypeProvider;
import org.openhab.binding.modbus.internal.config.ModbusPollerConfiguration;
import org.openhab.binding.modbus.internal.handler.Poller2BitChannel;
import org.openhab.binding.modbus.internal.handler.Poller2BitChannelConfiguration;
import org.openhab.binding.modbus.internal.handler.Poller2Channel;
import org.openhab.binding.modbus.internal.handler.Poller2ChannelConfiguration;
import org.openhab.binding.modbus.internal.handler.Poller2RawChannel;
import org.openhab.binding.modbus.internal.handler.Poller2RawChannelConfiguration;
import org.openhab.binding.modbus.internal.handler.Poller2WriteChannel;
import org.openhab.binding.modbus.internal.handler.Poller2WriteChannelConfiguration;
import org.openhab.core.io.transport.modbus.AsyncModbusFailure;
import org.openhab.core.io.transport.modbus.AsyncModbusReadResult;
import org.openhab.core.io.transport.modbus.AsyncModbusWriteResult;
import org.openhab.core.io.transport.modbus.BitArray;
import org.openhab.core.io.transport.modbus.ModbusCommunicationInterface;
import org.openhab.core.io.transport.modbus.ModbusReadRequestBlueprint;
import org.openhab.core.io.transport.modbus.ModbusWriteRequestBlueprint;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for the poller-owned channel model.
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class ModbusPoller2ThingHandler extends ModbusPollerThingHandler {
    private final Logger logger = LoggerFactory.getLogger(ModbusPoller2ThingHandler.class);

    private final ModbusPoller2ChannelTypeProvider channelTypeProvider;
    private volatile Map<ChannelUID, Poller2Channel> channels = Map.of();
    private volatile Map<ChannelUID, Poller2BitChannel> bitChannels = Map.of();
    private volatile Map<ChannelUID, Poller2RawChannel> rawChannels = Map.of();
    private volatile Map<ChannelUID, Poller2WriteChannel> writeChannels = Map.of();
    private volatile int pollStart;

    public ModbusPoller2ThingHandler(Bridge bridge, ModbusPoller2ChannelTypeProvider channelTypeProvider) {
        super(bridge);
        this.channelTypeProvider = channelTypeProvider;
    }

    @Override
    public synchronized void initialize() {
        applyUiChannelTypeShims();
        ModbusPollerConfiguration configuration = getConfigAs(ModbusPollerConfiguration.class);
        String type = configuration.getType();
        if (type == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "poller2 requires a configured read type");
            return;
        }
        boolean registerPoller = ModbusBindingConstantsInternal.READ_TYPE_HOLDING_REGISTER.equals(type)
                || ModbusBindingConstantsInternal.READ_TYPE_INPUT_REGISTER.equals(type);
        boolean bitPoller = ModbusBindingConstantsInternal.READ_TYPE_COIL.equals(type)
                || ModbusBindingConstantsInternal.READ_TYPE_DISCRETE_INPUT.equals(type);
        if (!getThing().getChannels().isEmpty() && !registerPoller && !bitPoller) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "poller2 numeric channels require type 'holding' or 'input', not '%s'".formatted(type));
            return;
        }
        pollStart = configuration.getStart();
        Map<ChannelUID, Poller2Channel> parsedChannels = new LinkedHashMap<>();
        Map<ChannelUID, Poller2BitChannel> parsedBitChannels = new LinkedHashMap<>();
        Map<ChannelUID, Poller2RawChannel> parsedRawChannels = new LinkedHashMap<>();
        Map<ChannelUID, Poller2WriteChannel> parsedWriteChannels = new LinkedHashMap<>();
        List<String> configurationErrors = new ArrayList<>();
        for (Channel channel : getThing().getChannels()) {
            if (registerPoller) {
                if ("raw".equals(channel.getConfiguration().getProperties().get("valueType"))) {
                    Poller2RawChannelConfiguration parsed = Poller2RawChannelConfiguration.create(
                            channel.getUID().getId(), channel.getConfiguration().getProperties(),
                            configuration.getStart(), configuration.getLength());
                    parsed.error().ifPresent(configurationErrors::add);
                    parsed.channel().ifPresent(raw -> parsedRawChannels.put(channel.getUID(), raw));
                    continue;
                }
                Poller2ChannelConfiguration parsed = Poller2ChannelConfiguration.create(channel.getUID().getId(),
                        channel.getConfiguration().getProperties(), configuration.getStart(),
                        configuration.getLength());
                Optional<String> error = parsed.error();
                if (error.isPresent()) {
                    configurationErrors.add(error.get());
                    continue;
                }
                parsedChannels.put(channel.getUID(), parsed.channel().orElseThrow());
            } else if (bitPoller) {
                Poller2BitChannelConfiguration parsed = Poller2BitChannelConfiguration.create(channel.getUID().getId(),
                        channel.getConfiguration().getProperties(), configuration.getStart(),
                        configuration.getLength());
                Optional<String> error = parsed.error();
                if (error.isPresent()) {
                    configurationErrors.add(error.get());
                    continue;
                }
                parsedBitChannels.put(channel.getUID(), parsed.channel().orElseThrow());
            }
            Poller2WriteChannelConfiguration writeConfiguration = Poller2WriteChannelConfiguration
                    .create(channel.getUID().getId(), channel.getConfiguration().getProperties(), type);
            writeConfiguration.error().ifPresent(configurationErrors::add);
            writeConfiguration.channel().ifPresent(write -> parsedWriteChannels.put(channel.getUID(), write));
        }
        if (!configurationErrors.isEmpty()) {
            channels = Map.of();
            bitChannels = Map.of();
            rawChannels = Map.of();
            writeChannels = Map.of();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    String.join(System.lineSeparator(), configurationErrors));
            return;
        }
        channels = Map.copyOf(parsedChannels);
        bitChannels = Map.copyOf(parsedBitChannels);
        rawChannels = Map.copyOf(parsedRawChannels);
        writeChannels = Map.copyOf(parsedWriteChannels);
        super.initialize();
    }

    private void applyUiChannelTypeShims() {
        List<Channel> channels = getThing().getChannels();
        List<Channel> shimmedChannels = channels.stream().map(this::withUiChannelTypeShim).toList();
        if (!channels.equals(shimmedChannels)) {
            ThingBuilder builder = editThing();
            builder.withChannels(shimmedChannels);
            updateThing(builder.build());
        }
    }

    private Channel withUiChannelTypeShim(Channel channel) {
        if (channel.getChannelTypeUID() != null) {
            return channel;
        }
        ChannelTypeUID channelTypeUID = channelTypeProvider.getGeneratedChannelTypeUID(channel.getAcceptedItemType());
        return channelTypeUID == null ? channel : ChannelBuilder.create(channel).withType(channelTypeUID).build();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            refresh();
            return;
        }
        Poller2WriteChannel writeChannel = writeChannels.get(channelUID);
        if (writeChannel == null) {
            return;
        }
        ModbusReadRequestBlueprint readRequest = getRequest();
        ModbusCommunicationInterface communication = getCommunicationInterface();
        if (readRequest == null) {
            return;
        }
        writeChannel.requestFor(command, readRequest.getUnitID()).ifPresent(request -> {
            logger.trace("Submitting poller2 write task {}", request);
            communication.submitOneTimeWrite(request, this::onWriteResponse, this::onWriteFailure);
        });
    }

    private void onWriteResponse(AsyncModbusWriteResult result) {
        updateStatus(ThingStatus.ONLINE);
        reconcileAfterWrite();
    }

    private void onWriteFailure(AsyncModbusFailure<ModbusWriteRequestBlueprint> failure) {
        Exception cause = failure.getCause();
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "Error with write: %s: %s".formatted(cause.getClass().getName(), cause.getMessage()));
    }

    @Override
    protected void onPollResult(AsyncModbusReadResult result) {
        result.getRegisters().ifPresent(registers -> channels
                .forEach((uid, channel) -> updateState(uid, channel.acceptRegisters(registers, pollStart))));
        result.getRegisters().ifPresent(registers -> rawChannels
                .forEach((uid, channel) -> updateState(uid, channel.acceptRegisters(registers, pollStart))));
        result.getBits().ifPresent(bits -> updateBitChannels(bits));
    }

    private void updateBitChannels(BitArray bits) {
        bitChannels.forEach((uid, channel) -> updateState(uid, channel.acceptBits(bits, pollStart)));
    }

    @Override
    protected void onPollFailure(AsyncModbusFailure<ModbusReadRequestBlueprint> failure) {
        channels.forEach((uid, channel) -> updateState(uid, channel.acceptReadFailure()));
        bitChannels.forEach((uid, channel) -> updateState(uid, channel.acceptReadFailure()));
        rawChannels.forEach((uid, channel) -> updateState(uid, channel.acceptReadFailure()));
    }
}
