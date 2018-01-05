/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.handler;

import static org.openhab.binding.knx.KNXBindingConstants.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.Type;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.knx.KNXBindingConstants;
import org.openhab.binding.knx.KNXTypeMapper;
import org.openhab.binding.knx.internal.channel.CommandSpec;
import org.openhab.binding.knx.internal.channel.KNXChannelTypes;
import org.openhab.binding.knx.internal.channel.KNXChannelType;
import org.openhab.binding.knx.internal.channel.ListenSpec;
import org.openhab.binding.knx.internal.channel.ReadSpec;
import org.openhab.binding.knx.internal.channel.ResponseSpec;
import org.openhab.binding.knx.internal.client.DeviceInspector;
import org.openhab.binding.knx.internal.client.KNXClient;
import org.openhab.binding.knx.internal.client.DeviceInspector.Result;
import org.openhab.binding.knx.internal.config.DeviceConfig;
import org.openhab.binding.knx.internal.dpt.KNXCoreTypeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.datapoint.CommandDP;
import tuwien.auto.calimero.datapoint.Datapoint;
import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.exception.KNXFormatException;

/**
 * The {@link DeviceThingHandler} is responsible for handling commands and state updates sent to and received from the
 * bus and updating the channels correspondingly.
 *
 * @author Simon Kaufmann - Initial contribution and API
 */
@NonNullByDefault
public class DeviceThingHandler extends BaseThingHandler implements GroupAddressListener {

    private final Logger logger = LoggerFactory.getLogger(DeviceThingHandler.class);
    private final Random random = new Random();

    private final KNXTypeMapper typeHelper = new KNXCoreTypeMapper();

    private final Set<GroupAddress> groupAddresses = new HashSet<>();
    private boolean filledDescription = false;

    private final Map<GroupAddress, @Nullable ScheduledFuture<?>> readFutures = new HashMap<>();
    private @Nullable ScheduledFuture<?> pollingJob;
    private @Nullable Future<?> descriptionJob;

    private @Nullable IndividualAddress address;
    private int readInterval;

    public DeviceThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        DeviceConfig config = getConfigAs(DeviceConfig.class);
        initializeGroupAddresses();
        try {
            if (StringUtils.isNotBlank(config.getAddress())) {
                address = new IndividualAddress(config.getAddress());

                long pingInterval = config.getPingInterval().longValue();
                long initialPingDelay = Math.round(pingInterval * random.nextFloat());

                ScheduledFuture<?> pollingJob = this.pollingJob;
                if ((pollingJob == null || pollingJob.isCancelled())) {
                    logger.debug("'{}' will be polled every {}s", getThing().getUID(), pingInterval);
                    this.pollingJob = getScheduler().scheduleWithFixedDelay(() -> pollDeviceStatus(), initialPingDelay,
                            pingInterval, TimeUnit.SECONDS);
                }

                readInterval = config.getReadInterval().intValue();
            } else {
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (KNXFormatException e) {
            logger.debug("An exception occurred while setting the individual address '{}'", config.getAddress(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getLocalizedMessage());
        }
        getClient().registerGroupAddressListener(this);
        scheduleReadJobs();
    }

    private ScheduledExecutorService getScheduler() {
        return getBridgeHandler().getScheduler();
    }

    private void initializeGroupAddresses() {
        forAllChannels((selector, channelConfiguration) -> {
            groupAddresses.addAll(selector.getReadAddresses(channelConfiguration));
            groupAddresses.addAll(selector.getWriteAddresses(channelConfiguration));
            groupAddresses.addAll(selector.getListenAddresses(channelConfiguration));
        });
    }

    private KNXChannelType getKNXChannelType(Channel channel) {
        return KNXChannelTypes.getType(channel.getChannelTypeUID());
    }

    private KNXBridgeBaseThingHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            KNXBridgeBaseThingHandler handler = (KNXBridgeBaseThingHandler) bridge.getHandler();
            if (handler != null) {
                return handler;
            }
        }
        throw new IllegalStateException("The bridge must not be null and must be initialized");
    }

    private KNXClient getClient() {
        return getBridgeHandler().getClient();
    }

    @Override
    public void dispose() {
        if (pollingJob != null) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
        if (descriptionJob != null) {
            descriptionJob.cancel(true);
            descriptionJob = null;
        }
        cancelReadFutures();
        getClient().unregisterGroupAddressListener(this);
    }

    private void cancelReadFutures() {
        for (ScheduledFuture<?> future : readFutures.values()) {
            if (future != null && !future.isDone()) {
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

    private void scheduleReadJobs() {
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
        List<ReadSpec> readSpecs = selector.getReadSpec(configuration);
        for (ReadSpec readSpec : readSpecs) {
            for (GroupAddress groupAddress : readSpec.getReadAddresses()) {
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
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            getClient().registerGroupAddressListener(this);
            scheduleReadJobs();
        } else if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            cancelReadFutures();
            getClient().unregisterGroupAddressListener(this);
        }
    }

    @Override
    public boolean listensTo(GroupAddress destination) {
        return groupAddresses.contains(destination);
    }

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
                        CommandSpec commandSpec = selector.getCommandSpec(channelConfiguration, command);
                        if (commandSpec != null) {
                            getClient().writeToKNX(commandSpec);
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

    @Override
    public void onGroupRead(KNXClient client, IndividualAddress source, GroupAddress destination, byte[] asdu) {
        logger.trace("Thing '{}' received a Group Read Request telegram from '{}' for destination '{}'",
                getThing().getUID(), source, destination);

        for (Channel channel : getThing().getChannels()) {
            if (isControl(channel.getUID())) {
                withKNXType(channel, (selector, configuration) -> {
                    ResponseSpec responseSpec = selector.getResponseSpec(configuration, destination);
                    if (responseSpec != null) {
                        postCommand(channel.getUID().getId(), RefreshType.REFRESH);
                    }
                });
            }
        }
    }

    @Override
    public void onGroupReadResponse(KNXClient client, IndividualAddress source, GroupAddress destination, byte[] asdu) {
        // Group Read Responses are treated the same as Group Write telegrams
        onGroupWrite(client, source, destination, asdu);
    }

    @Override
    public void onGroupWrite(KNXClient client, IndividualAddress source, GroupAddress destination, byte[] asdu) {
        logger.debug("Thing '{}' received a Group Write telegram from '{}' for destination '{}'", getThing().getUID(),
                source, destination);

        for (Channel channel : getThing().getChannels()) {
            withKNXType(channel, (selector, configuration) -> {
                ListenSpec listenSpec = selector.getListenSpec(configuration, destination);
                if (listenSpec != null) {
                    logger.trace("Thing '{}' processes a Group Write telegram for destination '{}' for channel '{}'",
                            getThing().getUID(), destination, channel.getUID());
                    processDataReceived(destination, asdu, listenSpec, channel.getUID());
                }
            });
        }
    }

    private final Map<ChannelUID, @Nullable ScheduledFuture<?>> channelFutures = new HashMap<>();

    private void processDataReceived(GroupAddress destination, byte[] asdu, ListenSpec listenSpec,
            ChannelUID channelUID) {
        if (!isDPTSupported(listenSpec.getDPT())) {
            logger.warn("DPT '{}' is not supported by the KNX binding.", listenSpec.getDPT());
            return;
        }

        Datapoint datapoint = new CommandDP(destination, getThing().getUID().toString(), 0, listenSpec.getDPT());
        Type type = typeHelper.toType(datapoint, asdu);

        if (type != null) {
            if (KNXBindingConstants.CHANNEL_DIMMER_CONTROL.equals(getChannelTypeUID(channelUID).getId())
                    && (type instanceof UnDefType || type instanceof IncreaseDecreaseType)) {
                // special handling for dimmer-control
                if (UnDefType.UNDEF.equals(type)) {
                    ScheduledFuture<?> future = channelFutures.remove(channelUID);
                    if (future != null) {
                        future.cancel(false);
                    }
                } else if (type instanceof IncreaseDecreaseType) {
                    ScheduledFuture<?> future = scheduler.scheduleWithFixedDelay(() -> {
                        postCommand(channelUID, (Command) type);
                    }, 0, 500, TimeUnit.MILLISECONDS);
                    ScheduledFuture<?> previousFuture = channelFutures.put(channelUID, future);
                    if (previousFuture != null) {
                        previousFuture.cancel(true);
                    }
                }
            } else {
                if (type instanceof Command) {
                    postCommand(channelUID, (Command) type);
                } else {
                    // drop
                }
            }
        } else {
            String s = asduToHex(asdu);
            logger.warn(
                    "Ignoring KNX bus data: couldn't transform to any Type (destination='{}', datapoint='{}', data='{}')",
                    destination, datapoint, s);
        }
    }

    private String asduToHex(byte[] asdu) {
        final char[] hexCode = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder(2 + asdu.length * 2);
        sb.append("0x");
        for (byte b : asdu) {
            sb.append(hexCode[(b >> 4) & 0xF]);
            sb.append(hexCode[(b & 0xF)]);
        }
        return sb.toString();
    }

    public void restart() {
        if (address != null) {
            getClient().restartNetworkDevice(address);
        }
    }

    private void pollDeviceStatus() {
        try {
            if (address != null && getClient().isConnected()) {
                logger.debug("Polling individual address '{}'", address);
                boolean isReachable = getClient().isReachable(address);
                if (isReachable) {
                    updateStatus(ThingStatus.ONLINE);
                    DeviceConfig config = getConfigAs(DeviceConfig.class);
                    if (!filledDescription && config.getFetch()) {
                        Future<?> descriptionJob = this.descriptionJob;
                        if (descriptionJob == null || descriptionJob.isCancelled()) {
                            this.descriptionJob = getScheduler().submit(() -> {
                                filledDescription = describeDevice(address);
                            });
                        }
                    }
                } else {
                    updateStatus(ThingStatus.OFFLINE);
                }
            }
        } catch (KNXException e) {
            logger.debug("An error occurred while testing the reachability of a thing '{}'", getThing().getUID(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
        }
    }

    private boolean describeDevice(@Nullable IndividualAddress address) {
        if (address == null) {
            return false;
        }
        DeviceInspector inspector = new DeviceInspector(getClient().getDeviceInfoClient(), address);
        Result result = inspector.readDeviceInfo();
        if (result != null) {
            Map<String, String> properties = editProperties();
            properties.putAll(result.getProperties());
            updateProperties(properties);
            return true;
        }
        return false;
    }

    private boolean isDPTSupported(@Nullable String dpt) {
        return typeHelper.toTypeClass(dpt) != null;
    }

}
