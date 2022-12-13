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
package org.openhab.binding.knx.internal.handler;

import static org.openhab.binding.knx.internal.KNXBindingConstants.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.KNXBindingConstants;
import org.openhab.binding.knx.internal.KNXTypeMapper;
import org.openhab.binding.knx.internal.channel.KNXChannelType;
import org.openhab.binding.knx.internal.channel.KNXChannelTypes;
import org.openhab.binding.knx.internal.client.AbstractKNXClient;
import org.openhab.binding.knx.internal.client.InboundSpec;
import org.openhab.binding.knx.internal.client.OutboundSpec;
import org.openhab.binding.knx.internal.config.DeviceConfig;
import org.openhab.binding.knx.internal.dpt.KNXCoreTypeMapper;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.KNXException;
import tuwien.auto.calimero.KNXFormatException;
import tuwien.auto.calimero.datapoint.CommandDP;
import tuwien.auto.calimero.datapoint.Datapoint;

/**
 * The {@link DeviceThingHandler} is responsible for handling commands and state updates sent to and received from the
 * bus and updating the channels correspondingly.
 *
 * @author Simon Kaufmann - Initial contribution and API
 */
@NonNullByDefault
public class DeviceThingHandler extends AbstractKNXThingHandler {

    private final Logger logger = LoggerFactory.getLogger(DeviceThingHandler.class);

    private final KNXTypeMapper typeHelper = new KNXCoreTypeMapper();
    private final Set<GroupAddress> groupAddresses = new HashSet<>();
    private final Set<GroupAddress> groupAddressesWriteBlockedOnce = new HashSet<>();
    private final Set<OutboundSpec> groupAddressesRespondingSpec = new HashSet<>();
    private final Map<GroupAddress, ScheduledFuture<?>> readFutures = new HashMap<>();
    private final Map<ChannelUID, ScheduledFuture<?>> channelFutures = new HashMap<>();
    private int readInterval;

    public DeviceThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
        DeviceConfig config = getConfigAs(DeviceConfig.class);
        readInterval = config.getReadInterval();
        initializeGroupAddresses();
    }

    private void initializeGroupAddresses() {
        forAllChannels((selector, channelConfiguration) -> {
            groupAddresses.addAll(selector.getReadAddresses(channelConfiguration));
            groupAddresses.addAll(selector.getWriteAddresses(channelConfiguration));
            groupAddresses.addAll(selector.getListenAddresses(channelConfiguration));
        });
    }

    @Override
    public void dispose() {
        cancelChannelFutures();
        freeGroupAdresses();
        super.dispose();
    }

    private void cancelChannelFutures() {
        for (ScheduledFuture<?> future : channelFutures.values()) {
            if (!future.isDone()) {
                future.cancel(true);
            }
        }
        channelFutures.clear();
    }

    private void freeGroupAdresses() {
        groupAddresses.clear();
        groupAddressesWriteBlockedOnce.clear();
        groupAddressesRespondingSpec.clear();
    }

    @Override
    protected void cancelReadFutures() {
        for (ScheduledFuture<?> future : readFutures.values()) {
            if (!future.isDone()) {
                future.cancel(true);
            }
        }
        readFutures.clear();
    }

    @FunctionalInterface
    private interface ChannelFunction {
        void apply(KNXChannelType channelType, Configuration configuration) throws KNXException;
    }

    private void withKNXType(ChannelUID channelUID, ChannelFunction function) {
        Channel channel = getThing().getChannel(channelUID.getId());
        if (channel == null) {
            logger.warn("Channel '{}' does not exist", channelUID);
            return;
        }
        withKNXType(channel, function);
    }

    private void withKNXType(Channel channel, ChannelFunction function) {
        try {
            KNXChannelType selector = getKNXChannelType(channel);
            function.apply(selector, channel.getConfiguration());
        } catch (KNXException e) {
            logger.warn("An error occurred on channel {}: {}", channel.getUID(), e.getMessage(), e);
        }
    }

    private void forAllChannels(ChannelFunction function) {
        for (Channel channel : getThing().getChannels()) {
            withKNXType(channel, function);
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        if (!isControl(channelUID)) {
            withKNXType(channelUID, (selector, configuration) -> {
                scheduleRead(selector, configuration);
            });
        }
    }

    @Override
    protected void scheduleReadJobs() {
        cancelReadFutures();
        for (Channel channel : getThing().getChannels()) {
            if (isLinked(channel.getUID().getId()) && !isControl(channel.getUID())) {
                withKNXType(channel, (selector, configuration) -> {
                    scheduleRead(selector, configuration);
                });
            }
        }
    }

    private void scheduleRead(KNXChannelType selector, Configuration configuration) throws KNXFormatException {
        List<InboundSpec> readSpecs = selector.getReadSpec(configuration);
        for (InboundSpec readSpec : readSpecs) {
            for (GroupAddress groupAddress : readSpec.getGroupAddresses()) {
                scheduleReadJob(groupAddress, readSpec.getDPT());
            }
        }
    }

    private void scheduleReadJob(GroupAddress groupAddress, String dpt) {
        if (readInterval > 0) {
            ScheduledFuture<?> future = readFutures.get(groupAddress);
            if (future == null || future.isDone() || future.isCancelled()) {
                future = getScheduler().scheduleWithFixedDelay(() -> readDatapoint(groupAddress, dpt), 0, readInterval,
                        TimeUnit.SECONDS);
                readFutures.put(groupAddress, future);
            }
        } else {
            getScheduler().submit(() -> readDatapoint(groupAddress, dpt));
        }
    }

    private void readDatapoint(GroupAddress groupAddress, String dpt) {
        if (getClient().isConnected()) {
            if (!isDPTSupported(dpt)) {
                logger.warn("DPT '{}' is not supported by the KNX binding", dpt);
                return;
            }
            Datapoint datapoint = new CommandDP(groupAddress, getThing().getUID().toString(), 0, dpt);
            getClient().readDatapoint(datapoint);
        }
    }

    @Override
    public boolean listensTo(GroupAddress destination) {
        return groupAddresses.contains(destination);
    }

    /** KNXIO remember controls, removeIf may be null */
    @SuppressWarnings("null")
    private void rememberRespondingSpec(OutboundSpec commandSpec, boolean add) {
        GroupAddress ga = commandSpec.getGroupAddress();
        groupAddressesRespondingSpec.removeIf(spec -> spec.getGroupAddress().equals(ga));
        if (add) {
            groupAddressesRespondingSpec.add(commandSpec);
        }
        logger.trace("rememberRespondingSpec handled commandSpec for '{}' size '{}' added '{}'", ga,
                groupAddressesRespondingSpec.size(), add);
    }

    /** Handling commands triggered from openHAB */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Handling command '{}' for channel '{}'", command, channelUID);
        if (command instanceof RefreshType && !isControl(channelUID)) {
            logger.debug("Refreshing channel '{}'", channelUID);
            withKNXType(channelUID, (selector, configuration) -> {
                scheduleRead(selector, configuration);
            });
        } else {
            switch (channelUID.getId()) {
                case CHANNEL_RESET:
                    if (address != null) {
                        restart();
                    }
                    break;
                default:
                    withKNXType(channelUID, (selector, channelConfiguration) -> {
                        OutboundSpec commandSpec = selector.getCommandSpec(channelConfiguration, typeHelper, command);
                        // only send GroupValueWrite to KNX if GA is not blocked once
                        if (commandSpec != null
                                && !groupAddressesWriteBlockedOnce.remove(commandSpec.getGroupAddress())) {
                            getClient().writeToKNX(commandSpec);
                            if (isControl(channelUID)) {
                                rememberRespondingSpec(commandSpec, true);
                            }
                        } else {
                            logger.debug(
                                    "None of the configured GAs on channel '{}' could handle the command '{}' of type '{}'",
                                    channelUID, command, command.getClass().getSimpleName());
                        }
                    });
                    break;
            }
        }
    }

    private boolean isControl(ChannelUID channelUID) {
        ChannelTypeUID channelTypeUID = getChannelTypeUID(channelUID);
        return CONTROL_CHANNEL_TYPES.contains(channelTypeUID.getId());
    }

    private ChannelTypeUID getChannelTypeUID(ChannelUID channelUID) {
        Channel channel = getThing().getChannel(channelUID.getId());
        Objects.requireNonNull(channel);
        ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
        Objects.requireNonNull(channelTypeUID);
        return channelTypeUID;
    }

    /** KNXIO */
    private void sendGroupValueResponse(Channel channel, GroupAddress destination) {
        Set<GroupAddress> rsa = getKNXChannelType(channel).getWriteAddresses(channel.getConfiguration());
        if (!rsa.isEmpty()) {
            logger.trace("onGroupRead size '{}'", rsa.size());
            withKNXType(channel, (selector, configuration) -> {
                Optional<OutboundSpec> os = groupAddressesRespondingSpec.stream().filter(spec -> {
                    GroupAddress groupAddress = spec.getGroupAddress();
                    if (groupAddress != null) {
                        return groupAddress.equals(destination);
                    }
                    return false;
                }).findFirst();
                if (os.isPresent()) {
                    logger.trace("onGroupRead respondToKNX '{}'", os.get().getGroupAddress());
                    /** KNXIO: sending real "GroupValueResponse" to the KNX bus. */
                    getClient().respondToKNX(os.get());
                }
            });
        }
    }

    /**
     * KNXIO, extended with the ability to respond on "GroupValueRead" telegrams with "GroupValueResponse" telegram
     */
    @Override
    public void onGroupRead(AbstractKNXClient client, IndividualAddress source, GroupAddress destination, byte[] asdu) {
        logger.trace("onGroupRead Thing '{}' received a GroupValueRead telegram from '{}' for destination '{}'",
                getThing().getUID(), source, destination);
        for (Channel channel : getThing().getChannels()) {
            if (isControl(channel.getUID())) {
                withKNXType(channel, (selector, configuration) -> {
                    OutboundSpec responseSpec = selector.getResponseSpec(configuration, destination,
                            RefreshType.REFRESH);
                    if (responseSpec != null) {
                        logger.trace("onGroupRead isControl -> postCommand");
                        // This event should be sent to KNX as GroupValueResponse immediately.
                        sendGroupValueResponse(channel, destination);
                        // Send REFRESH to openHAB to get this event for scripting with postCommand
                        // and remember to ignore/block this REFRESH to be sent back to KNX as GroupValueWrite after
                        // postCommand is done!
                        groupAddressesWriteBlockedOnce.add(destination);
                        postCommand(channel.getUID().getId(), RefreshType.REFRESH);
                    }
                });
            }
        }
    }

    @Override
    public void onGroupReadResponse(AbstractKNXClient client, IndividualAddress source, GroupAddress destination,
            byte[] asdu) {
        // GroupValueResponses are treated the same as GroupValueWrite telegrams
        logger.trace("onGroupReadResponse Thing '{}' processes a GroupValueResponse telegram for destination '{}'",
                getThing().getUID(), destination);
        onGroupWrite(client, source, destination, asdu);
    }

    /**
     * KNXIO, here value changes are set, coming from KNX OR openHAB.
     */
    @Override
    public void onGroupWrite(AbstractKNXClient client, IndividualAddress source, GroupAddress destination,
            byte[] asdu) {
        logger.debug("onGroupWrite Thing '{}' received a GroupValueWrite telegram from '{}' for destination '{}'",
                getThing().getUID(), source, destination);

        for (Channel channel : getThing().getChannels()) {
            withKNXType(channel, (selector, configuration) -> {
                InboundSpec listenSpec = selector.getListenSpec(configuration, destination);
                if (listenSpec != null) {
                    logger.trace(
                            "onGroupWrite Thing '{}' processes a GroupValueWrite telegram for destination '{}' for channel '{}'",
                            getThing().getUID(), destination, channel.getUID());
                    /**
                     * Remember current KNXIO outboundSpec only if it is a control channel.
                     */
                    if (isControl(channel.getUID())) {
                        logger.trace("onGroupWrite isControl");
                        Type type = typeHelper.toType(
                                new CommandDP(destination, getThing().getUID().toString(), 0, listenSpec.getDPT()),
                                asdu);
                        if (type != null) {
                            OutboundSpec commandSpec = selector.getCommandSpec(configuration, typeHelper, type);
                            if (commandSpec != null) {
                                rememberRespondingSpec(commandSpec, true);
                            }
                        }
                    }
                    processDataReceived(destination, asdu, listenSpec, channel.getUID());
                }
            });
        }
    }

    private void processDataReceived(GroupAddress destination, byte[] asdu, InboundSpec listenSpec,
            ChannelUID channelUID) {
        if (!isDPTSupported(listenSpec.getDPT())) {
            logger.warn("DPT '{}' is not supported by the KNX binding.", listenSpec.getDPT());
            return;
        }

        Datapoint datapoint = new CommandDP(destination, getThing().getUID().toString(), 0, listenSpec.getDPT());
        Type type = typeHelper.toType(datapoint, asdu);

        if (type != null) {
            if (isControl(channelUID)) {
                Channel channel = getThing().getChannel(channelUID.getId());
                Object repeat = channel != null ? channel.getConfiguration().get(KNXBindingConstants.REPEAT_FREQUENCY)
                        : null;
                int frequency = repeat != null ? ((BigDecimal) repeat).intValue() : 0;
                if (KNXBindingConstants.CHANNEL_DIMMER_CONTROL.equals(getChannelTypeUID(channelUID).getId())
                        && (type instanceof UnDefType || type instanceof IncreaseDecreaseType) && frequency > 0) {
                    // continuous dimming by the binding
                    if (UnDefType.UNDEF.equals(type)) {
                        ScheduledFuture<?> future = channelFutures.remove(channelUID);
                        if (future != null) {
                            future.cancel(false);
                        }
                    } else if (type instanceof IncreaseDecreaseType) {
                        ScheduledFuture<?> future = scheduler.scheduleWithFixedDelay(() -> {
                            postCommand(channelUID, (Command) type);
                        }, 0, frequency, TimeUnit.MILLISECONDS);
                        ScheduledFuture<?> previousFuture = channelFutures.put(channelUID, future);
                        if (previousFuture != null) {
                            previousFuture.cancel(true);
                        }
                    }
                } else {
                    if (type instanceof Command) {
                        logger.trace("processDataReceived postCommand new value '{}' for GA '{}'", asdu, address);
                        postCommand(channelUID, (Command) type);
                    }
                }
            } else {
                if (type instanceof State) {
                    updateState(channelUID, (State) type);
                }
            }
        } else {
            String s = asduToHex(asdu);
            logger.warn(
                    "Ignoring KNX bus data: couldn't transform to any Type (destination='{}', datapoint='{}', data='{}')",
                    destination, datapoint, s);
        }
    }

    private boolean isDPTSupported(@Nullable String dpt) {
        return typeHelper.toTypeClass(dpt) != null;
    }

    private KNXChannelType getKNXChannelType(Channel channel) {
        return KNXChannelTypes.getType(channel.getChannelTypeUID());
    }
}
