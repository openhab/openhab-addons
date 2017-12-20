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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.knx.internal.channel.KNXChannelSelector;
import org.openhab.binding.knx.internal.channel.KNXChannelType;
import org.openhab.binding.knx.internal.client.KNXClient;
import org.openhab.binding.knx.internal.config.BasicConfig;
import org.openhab.binding.knx.internal.handler.DeviceInspector;
import org.openhab.binding.knx.internal.handler.DeviceInspector.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.datapoint.CommandDP;
import tuwien.auto.calimero.datapoint.Datapoint;
import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.exception.KNXFormatException;

/**
 * The {@link KNXBasicThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Karel Goderis - Initial contribution
 */
@NonNullByDefault
public class KNXBasicThingHandler extends BaseThingHandler implements IndividualAddressListener, GroupAddressListener {

    private final Random random = new Random();

    private final Logger logger = LoggerFactory.getLogger(KNXBasicThingHandler.class);

    // the physical address of the KNX actor represented by this Thing
    @Nullable
    protected IndividualAddress address;

    // group addresses the handler is monitoring
    protected Set<GroupAddress> groupAddresses = new HashSet<>();

    // group addresses read out from the device's firmware tables
    protected Set<GroupAddress> foundGroupAddresses = new HashSet<>();

    private final Map<GroupAddress, @Nullable ScheduledFuture<?>> readFutures = new HashMap<>();

    @Nullable
    private ScheduledFuture<?> pollingJob;

    @Nullable
    private ScheduledFuture<?> descriptionJob;

    private boolean filledDescription = false;

    private final TypeHelper typeHelper;

    public KNXBasicThingHandler(Thing thing, TypeHelper typeHelper) {
        super(thing);
        this.typeHelper = typeHelper;
    }

    @Override
    public void initialize() {
        BasicConfig config = getConfigAs(BasicConfig.class);

        initializeGroupAddresses();

        try {
            if (StringUtils.isNotBlank(config.getAddress())) {
                address = new IndividualAddress(config.getAddress());

                long pollingInterval = config.getInterval().longValue();
                long initialDelay = Math.round(pollingInterval * random.nextFloat());

                ScheduledFuture<?> pollingJob = this.pollingJob;
                if ((pollingJob == null || pollingJob.isCancelled())) {
                    logger.debug("'{}' will be polled every {}s", getThing().getUID(), pollingInterval);
                    this.pollingJob = getBridgeHandler().getScheduler().scheduleWithFixedDelay(() -> pollDeviceStatus(),
                            initialDelay, pollingInterval, TimeUnit.SECONDS);
                }
            } else {
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (KNXFormatException e) {
            logger.error("An exception occurred while setting the individual address '{}': {}", config.getAddress(),
                    e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getLocalizedMessage());
        }

        getClient().registerGroupAddressListener(this);
        scheduleReadJobs();
    }

    private void initializeGroupAddresses() {
        forAllChannels((selector, channelConfiguration) -> {
            groupAddresses.addAll(selector.getReadAddresses(channelConfiguration));
            groupAddresses.addAll(selector.getWriteAddresses(channelConfiguration, null));
            groupAddresses.addAll(selector.getTransmitAddresses(channelConfiguration, null));
            groupAddresses.addAll(selector.getUpdateAddresses(channelConfiguration, null));
        });
    }

    private KNXChannelType getKNXChannelType(Channel channel) {
        return KNXChannelSelector.getValueSelectorFromChannelTypeId(channel.getChannelTypeUID());
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
        withKNXType(channelUID.getId(), function);
    }

    private void withKNXType(String channelId, ChannelFunction function) {
        Channel channel = getThing().getChannel(channelId);
        if (channel == null) {
            logger.warn("Channel '{}' does not exist on thing '{}'", channelId, getThing().getUID());
            return;
        }
        withKNXType(channel, function);
    }

    private void withKNXType(Channel channel, ChannelFunction function) {
        try {
            KNXChannelType selector = getKNXChannelType(channel);
            function.apply(selector, channel.getConfiguration());
        } catch (KNXException e) {
            logger.error("An error occurred on channel {}: {}", channel.getUID(), e.getMessage(), e);
        }
    }

    private void forAllChannels(ChannelFunction function) {
        for (Channel channel : getThing().getChannels()) {
            withKNXType(channel, function);
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        withKNXType(channelUID, (selector, configuration) -> {
            Boolean mustRead = (Boolean) configuration.get(READ);
            BigDecimal readInterval = (BigDecimal) configuration.get(INTERVAL);
            for (GroupAddress address : selector.getReadAddresses(configuration)) {
                if (mustRead || readInterval.intValue() > 0) {
                    scheduleReadJob(address, selector.getDPT(address, configuration), true, BigDecimal.ZERO);
                }
            }
        });
    }

    private void scheduleReadJobs() {
        cancelReadFutures();

        for (Channel channel : getThing().getChannels()) {
            if (isLinked(channel.getUID().getId())) {
                withKNXType(channel, (selector, channelConfiguration) -> {
                    Boolean mustRead = (Boolean) channelConfiguration.get(READ);
                    BigDecimal readInterval = (BigDecimal) channelConfiguration.get(INTERVAL);
                    for (GroupAddress address : selector.getReadAddresses(channelConfiguration)) {
                        scheduleReadJob(address, selector.getDPT(address, channelConfiguration), mustRead,
                                readInterval);
                    }
                });
            }
        }
    }

    private void scheduleReadJob(GroupAddress groupAddress, @Nullable String dpt, boolean immediate,
            @Nullable BigDecimal readInterval) {
        boolean recurring = readInterval != null && readInterval.intValue() > 0;

        Runnable readRunnable = () -> {
            if (getThing().getStatus() == ThingStatus.ONLINE && getClient().isConnected()) {
                if (!typeHelper.isDPTSupported(dpt)) {
                    logger.warn("DPT '{}' is not supported by the KNX binding", dpt);
                    return;
                }
                Datapoint datapoint = new CommandDP(groupAddress, getThing().getUID().toString(), 0, dpt);
                getClient().readDatapoint(datapoint);
            }
        };
        if (immediate) {
            getBridgeHandler().getScheduler().schedule(readRunnable, 0, TimeUnit.SECONDS);
        }
        if (recurring && readInterval != null) {
            ScheduledFuture<?> future = readFutures.get(groupAddress);
            if (future == null || future.isDone() || future.isCancelled()) {
                int initialDelay = immediate ? 0 : readInterval.intValue();
                future = getBridgeHandler().getScheduler().scheduleWithFixedDelay(readRunnable, initialDelay,
                        readInterval.intValue(), TimeUnit.SECONDS);
                readFutures.put(groupAddress, future);
            }
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);

        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            getClient().registerGroupAddressListener(this);
            scheduleReadJobs();
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
        } else if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            cancelReadFutures();
            getClient().unregisterGroupAddressListener(this);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @Override
    public boolean listensTo(IndividualAddress source) {
        return address != null && address.equals(source);
    }

    @Override
    public boolean listensTo(GroupAddress destination) {
        return groupAddresses.contains(destination) || foundGroupAddresses.contains(destination);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Handling a Command ({})  for Channel {}", command, channelUID);
        if (command instanceof RefreshType) {
            logger.debug("Refreshing channel {}", channelUID);
            withKNXType(channelUID, (selector, channelConfiguration) -> {
                for (GroupAddress address : selector.getReadAddresses(channelConfiguration)) {
                    scheduleReadJob(address, selector.getDPT(address, channelConfiguration), true, BigDecimal.ZERO);
                }
            });
        } else {
            switch (channelUID.getId()) {
                case CHANNEL_RESET:
                    if (address != null) {
                        restart();
                    }
                    break;
                default:
                    sendToKNX(channelUID, command);
                    break;
            }
        }

    }

    private void sendToKNX(ChannelUID channelUID, Type type) {
        withKNXType(channelUID, (selector, channelConfiguration) -> {
            Type convertedType = selector.convertType(channelConfiguration, type);
            if (logger.isTraceEnabled()) {
                Channel channel = getThing().getChannel(channelUID.getId());
                if (channel != null) {
                    logger.trace("Sending to channel {} {} {} {}/{} : {} -> {}", channelUID.getId(),
                            channel.getConfiguration().get(DPT), channel.getAcceptedItemType(),
                            channel.getConfiguration().get(READ), channel.getConfiguration().get(WRITE), type,
                            convertedType);
                }
            }
            if (convertedType != null) {
                for (GroupAddress address : selector.getWriteAddresses(channelConfiguration, convertedType)) {
                    getClient().writeToKNX(address, selector.getDPT(address, channelConfiguration), convertedType);
                }
            }
        });
    }

    @Override
    public void onGroupRead(KNXClient client, IndividualAddress source, GroupAddress destination, byte[] asdu) {
        // Nothing to do here - Software representations of physical actors should not respond to GroupRead requests, as
        // the physical device will be responding to these instead
    }

    @Override
    public void onGroupReadResponse(KNXClient client, IndividualAddress source, GroupAddress destination, byte[] asdu) {
        // Group Read Responses are treated the same as Group Write telegrams
        onGroupWrite(client, source, destination, asdu);
    }

    @Override
    public void onGroupWrite(KNXClient client, IndividualAddress source, GroupAddress destination, byte[] asdu) {

        logger.trace("Thing {} received a Group Write telegram from '{}' for destination '{}'", getThing().getUID(),
                source, destination);

        for (Channel channel : getThing().getChannels()) {
            withKNXType(channel, (selector, channelConfiguration) -> {
                Set<GroupAddress> addresses = selector.getReadAddresses(channelConfiguration);
                addresses.addAll(selector.getTransmitAddresses(channelConfiguration, null));

                if (addresses.contains(destination)) {
                    logger.trace("Thing {} processes a Group Write telegram for destination '{}' for channel '{}'",
                            getThing().getUID(), destination, channel.getUID());
                    processDataReceived(destination, asdu, selector.getDPT(destination, channelConfiguration),
                            channel.getUID());
                }
            });
        }
    }

    private void processDataReceived(GroupAddress destination, byte[] asdu, @Nullable String dpt,
            ChannelUID channelUID) {
        if (dpt != null) {
            if (!typeHelper.isDPTSupported(dpt)) {
                logger.warn("DPT {} is not supported by the KNX binding.", dpt);
                return;
            }

            Datapoint datapoint = new CommandDP(destination, getThing().getUID().toString(), 0, dpt);
            Type type = typeHelper.getType(datapoint, asdu);

            if (type != null) {
                postCommand(channelUID, (Command) type);
            } else {
                final char[] hexCode = "0123456789ABCDEF".toCharArray();
                StringBuilder sb = new StringBuilder(2 + asdu.length * 2);
                sb.append("0x");
                for (byte b : asdu) {
                    sb.append(hexCode[(b >> 4) & 0xF]);
                    sb.append(hexCode[(b & 0xF)]);
                }

                logger.warn(
                        "Ignoring KNX bus data: couldn't transform to Type (not supported). Destination='{}', datapoint='{}', data='{}'",
                        destination, datapoint, sb);
                return;
            }
        }
    }

    public void restart() {
        if (address != null) {
            getClient().restartNetworkDevice(address);
        }
    }

    class ReadRunnable implements Runnable {

        private final GroupAddress address;

        @Nullable
        private final String dpt;

        ReadRunnable(GroupAddress address, @Nullable String dpt) {
            this.address = address;
            this.dpt = dpt;
        }

        @Override
        public void run() {
            if (getThing().getStatus() == ThingStatus.ONLINE && getClient().isConnected()) {
                if (!typeHelper.isDPTSupported(dpt)) {
                    logger.warn("DPT '{}' is not supported by the KNX binding", dpt);
                    return;
                }
                Datapoint datapoint = new CommandDP(address, getThing().getUID().toString(), 0, dpt);
                getClient().readDatapoint(datapoint);
            }
        }
    };

    private void pollDeviceStatus() {
        try {
            if (address != null && getClient().isConnected()) {
                logger.debug("Polling the individual address {}", address);
                boolean isReachable = getClient().isReachable(address);
                if (isReachable) {
                    updateStatus(ThingStatus.ONLINE);
                    BasicConfig config = getConfigAs(BasicConfig.class);
                    if (!filledDescription && config.getFetch()) {
                        ScheduledFuture<?> descriptionJob = this.descriptionJob;
                        if (descriptionJob == null || descriptionJob.isCancelled()) {
                            this.descriptionJob = getBridgeHandler().getScheduler().schedule(() -> {
                                IndividualAddress address = KNXBasicThingHandler.this.address;
                                if (address != null) {
                                    DeviceInspector inspector = new DeviceInspector(getClient().getDeviceInfoClient(),
                                            address);
                                    Result result = inspector.readDeviceInfo();
                                    if (result != null) {
                                        filledDescription = true;

                                        Map<String, String> properties = editProperties();
                                        properties.putAll(result.getProperties());
                                        updateProperties(properties);

                                        foundGroupAddresses.addAll(result.getGroupAddresses());
                                    }
                                }
                            }, 0, TimeUnit.MILLISECONDS);
                        }
                    }
                } else {
                    updateStatus(ThingStatus.OFFLINE);
                }
            }
        } catch (KNXException e) {
            logger.error("An error occurred while testing the reachability of a thing '{}' : {}", getThing().getUID(),
                    e.getLocalizedMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
        }
    }

}
