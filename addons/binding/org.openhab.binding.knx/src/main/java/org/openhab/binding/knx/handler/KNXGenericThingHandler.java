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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.knx.GroupAddressListener;
import org.openhab.binding.knx.IndividualAddressListener;
import org.openhab.binding.knx.internal.channel.KNXChannelSelectorProxy;
import org.openhab.binding.knx.internal.channel.KNXChannelSelectorProxy.KNXChannelSelector;
import org.openhab.binding.knx.internal.dpt.KNXCoreTypeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.DataUnitBuilder;
import tuwien.auto.calimero.DeviceDescriptor;
import tuwien.auto.calimero.DeviceDescriptor.DD0;
import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.datapoint.CommandDP;
import tuwien.auto.calimero.datapoint.Datapoint;
import tuwien.auto.calimero.exception.KNXFormatException;
import tuwien.auto.calimero.mgmt.PropertyAccess.PID;

/**
 * The {@link KNXGenericThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Karel Goderis - Initial contribution
 */
public class KNXGenericThingHandler extends BaseThingHandler
        implements IndividualAddressListener, GroupAddressListener {

    private final Logger logger = LoggerFactory.getLogger(KNXGenericThingHandler.class);

    protected KNXChannelSelectorProxy knxChannelSelectorProxy = new KNXChannelSelectorProxy();

    protected ItemChannelLinkRegistry itemChannelLinkRegistry;
    protected ArrayList<ChannelUID> blockedChannels = new ArrayList<ChannelUID>();

    // the physical address of the KNX actor represented by this Thing
    protected IndividualAddress address;

    // group addresses the handler is monitoring
    protected Set<GroupAddress> groupAddresses = new HashSet<GroupAddress>();

    // group addresses read out from the device's firmware tables
    protected Set<GroupAddress> foundGroupAddresses = new HashSet<GroupAddress>();

    private ArrayList<ScheduledFuture<?>> readFutures = new ArrayList<ScheduledFuture<?>>();
    private ScheduledFuture<?> pollingJob;
    private ScheduledFuture<?> descriptionJob;

    ScheduledExecutorService knxScheduler;

    private static final long POLLING_INTERVAL = 60000;
    private static final long OPERATION_TIMEOUT = 5000;
    private static final long OPERATION_INTERVAL = 2000;
    private static final Random RANDOM_GENERATOR = new Random();
    private boolean filledDescription = false;

    // Memory addresses for device information
    static final int MEM_DOA = 0x0102; // length 2
    static final int MEM_MANUFACTURERID = 0x0104;
    static final int MEM_DEVICETYPE = 0x0105; // length 2
    static final int MEM_VERSION = 0x0107;
    static final int MEM_PEI = 0x0109;
    static final int MEM_RUNERROR = 0x010d;
    static final int MEM_GROUPOBJECTABLEPTR = 0x0112;
    static final int MEM_PROGRAMPTR = 0x0114;
    static final int MEM_GROUPADDRESSTABLE = 0x0116; // max. length 233

    // Interface Object indexes
    private static final int DEVICE_OBJECT = 0; // Device Object
    private static final int ADDRESS_TABLE_OBJECT = 1; // Addresstable Object
    private static final int ASSOCIATION_TABLE_OBJECT = 2; // Associationtable Object
    private static final int APPLICATION_PROGRAM_TABLE = 3; // Application Program Object
    private static final int INTERFACE_PROGRAM_OBJECT = 4; // Interface Program Object
    private static final int GROUPOBJECT_OBJECT = 9; // Group Object Object
    private static final int KNXNET_IP_OBJECT = 11; // KNXnet/IP Parameter Object

    // Property IDs for device information;
    private static final int HARDWARE_TYPE = 78;

    public KNXGenericThingHandler(Thing thing, ItemChannelLinkRegistry registry) {
        super(thing);
        this.itemChannelLinkRegistry = registry;
    }

    @Override
    public void initialize() {

        knxScheduler = ((KNXBridgeBaseThingHandler) getBridge().getHandler()).getScheduler();
        logger.trace("Setting the scheduler for {} to {}", getThing().getUID(), knxScheduler.toString());

        try {
            if (StringUtils.isNotBlank((String) getConfig().get(ADDRESS)) && ((Boolean) getConfig().get(FETCH))) {
                address = new IndividualAddress((String) getConfig().get(ADDRESS));

                double factor = (RANDOM_GENERATOR.nextFloat() * 2 - 1);
                long pollingInterval = Math.round(POLLING_INTERVAL * (1 + 0.25 * factor));

                if ((pollingJob == null || pollingJob.isCancelled())) {
                    logger.trace("'{}' will be polled every {} ms", getThing().getUID(), pollingInterval);
                    pollingJob = knxScheduler.scheduleWithFixedDelay(pollingRunnable, pollingInterval / 4,
                            pollingInterval, TimeUnit.MILLISECONDS);
                }
            } else {
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (Exception e) {
            logger.error("An exception occurred while setting the individual address '{}' : '{}'",
                    getConfig().get(ADDRESS), e.getMessage(), e);
        }

        for (Channel channel : getThing().getChannels()) {
            Configuration channelConfiguration = channel.getConfiguration();

            KNXChannelSelector selector = KNXChannelSelector
                    .getValueSelectorFromChannelTypeId(channel.getChannelTypeUID().getId());

            if (selector != null) {
                try {
                    groupAddresses
                            .addAll(knxChannelSelectorProxy.getReadAddresses(selector, channelConfiguration, null));
                    groupAddresses
                            .addAll(knxChannelSelectorProxy.getWriteAddresses(selector, channelConfiguration, null));
                    groupAddresses
                            .addAll(knxChannelSelectorProxy.getTransmitAddresses(selector, channelConfiguration, null));
                    groupAddresses
                            .addAll(knxChannelSelectorProxy.getUpdateAddresses(selector, channelConfiguration, null));
                } catch (KNXFormatException e) {
                    logger.error(
                            "An exception occurred while adding a group address to the addresses to be listened to : '{}'",
                            e.getMessage(), e);
                }
            } else {
                logger.error("The Channel Type {} is not implemented", channel.getChannelTypeUID().getId());
            }
        }

        ((KNXBridgeBaseThingHandler) getBridge().getHandler()).registerGroupAddressListener(this);

        scheduleReadJobs();
    }

    @Override
    public void dispose() {

        if (pollingJob != null && !pollingJob.isCancelled()) {
            pollingJob.cancel(true);
            pollingJob = null;
        }

        if (descriptionJob != null && !descriptionJob.isCancelled()) {
            descriptionJob.cancel(true);
            descriptionJob = null;
        }
    }

    private void cancelReadFutures() {
        if (readFutures != null) {
            for (ScheduledFuture<?> future : readFutures) {
                if (!future.isDone()) {
                    future.cancel(true);
                }
            }
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {

        Configuration channelConfiguration = getThing().getChannel(channelUID.getId()).getConfiguration();
        Boolean mustRead = (Boolean) channelConfiguration.get(READ);
        BigDecimal readInterval = (BigDecimal) channelConfiguration.get(INTERVAL);

        KNXChannelSelector selector = KNXChannelSelector.getValueSelectorFromChannelTypeId(
                getThing().getChannel(channelUID.getId()).getChannelTypeUID().getId());

        if (selector != null) {
            try {
                for (GroupAddress address : knxChannelSelectorProxy.getReadAddresses(selector, channelConfiguration,
                        null)) {
                    if (mustRead || readInterval.intValue() > 0) {
                        scheduleReadJob(address,
                                knxChannelSelectorProxy.getDPT(address, selector, channelConfiguration, null), true,
                                BigDecimal.ZERO);
                    }
                }
            } catch (KNXFormatException e) {
                logger.error("An exception occurred while scheduling a read job : '{}'", e.getMessage(), e);
            }
        } else {
            logger.error("The Channel Type {} is not implemented",
                    getThing().getChannel(channelUID.getId()).getChannelTypeUID().getId());
        }
    }

    private void scheduleReadJobs() {

        cancelReadFutures();

        for (Channel channel : getThing().getChannels()) {
            Configuration channelConfiguration = channel.getConfiguration();
            Boolean mustRead = (Boolean) channelConfiguration.get(READ);
            BigDecimal readInterval = (BigDecimal) channelConfiguration.get(INTERVAL);

            KNXChannelSelector selector = KNXChannelSelector
                    .getValueSelectorFromChannelTypeId(channel.getChannelTypeUID().getId());

            if (selector != null) {
                try {
                    for (GroupAddress address : knxChannelSelectorProxy.getReadAddresses(selector, channelConfiguration,
                            null)) {

                        scheduleReadJob(address,
                                knxChannelSelectorProxy.getDPT(address, selector, channelConfiguration, null), mustRead,
                                readInterval);
                    }
                } catch (KNXFormatException e) {
                    logger.error("An exception occurred while scheduling a read job : '{}'", e.getMessage(), e);
                }
            } else {
                logger.warn("The Channel Type {} is not implemented", channel.getChannelTypeUID().getId());
            }
        }
    }

    private void scheduleReadJob(GroupAddress groupAddress, String dpt, boolean mustRead, BigDecimal readInterval) {

        if (mustRead && knxScheduler != null) {
            knxScheduler.schedule(new ReadRunnable(groupAddress, dpt), 0, TimeUnit.SECONDS);
        }

        if (readInterval != null && readInterval.intValue() > 0 && knxScheduler != null) {
            readFutures.add(knxScheduler.scheduleWithFixedDelay(new ReadRunnable(groupAddress, dpt),
                    readInterval.intValue(), readInterval.intValue(), TimeUnit.SECONDS));
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);

        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            ((KNXBridgeBaseThingHandler) getBridge().getHandler()).registerGroupAddressListener(this);
            scheduleReadJobs();
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
        } else if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            cancelReadFutures();
            ((KNXBridgeBaseThingHandler) getBridge().getHandler()).unregisterGroupAddressListener(this);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @Override
    public boolean listensTo(IndividualAddress source) {
        if (address != null) {
            return address.equals(source);
        } else {
            return false;
        }
    }

    @Override
    public boolean listensTo(GroupAddress destination) {
        return groupAddresses.contains(destination) || foundGroupAddresses.contains(destination);
    }

    @Override
    public void handleRemoval() {

        cancelReadFutures();

        KNXBridgeBaseThingHandler bridgeHandler = (KNXBridgeBaseThingHandler) getBridge().getHandler();
        if (bridgeHandler != null) {
            bridgeHandler.unregisterGroupAddressListener(this);
        }

        updateStatus(ThingStatus.REMOVED);
    }

    @Override
    public void handleUpdate(ChannelUID channelUID, State newState) {

        if (((KNXBridgeBaseThingHandler) getBridge().getHandler()) == null) {
            logger.warn("KNX bridge handler not found. Cannot handle updates without bridge.");
        }

        logger.trace("Handling a State ({}) update for Channel {}", newState, channelUID.getId());

        // There are multiple ways to prevent circular loops between the KNX bus and the OH runtume. The first option is
        // to include https://github.com/eclipse/smarthome/pull/2881 in the ESH runtime, and configure manually the
        // desired behavior Item by Item. A second option is to use a trace mechanism that tracks what States and
        // Commands were received in the near past, and then eliminate those that resemble a duplicae event. The last
        // option is to make the KNXThingHandlers use the ItemChannelLinkRegistry infrastructure to detect what Items
        // Channels are bound to, and put Channels that are originating from other KNX Things into a blocked list, and
        // filter these out when handleCommand and handleUpdate are called.
        //
        // The first option was vetoed. The second option does not yield deterministic behavior. The code for the second
        // option is still included for discussion purposes but is commented out. The default "look-back" interval of
        // 500ms not adequate, and putting a higher value leads to missed events. The third option is the only one
        // remaining that provided a behavior similar to the KNX 1.x binding, which in fact only passes on States and
        // Command to Channels that are bound to the Item and that are not originating from the KNX binding, e.g.
        // "inter"-binding bridging is allowed, but "intra"-binding bridging is filtered out. The third option is

        // "Second option"
        // if (((KNXBridgeBaseThingHandler) getBridge().getHandler()).hasEvent(channelUID, newState, 50, 500)) {
        // return;
        // } else {
        // ((KNXBridgeBaseThingHandler) getBridge().getHandler()).logEvent(channelUID, newState);
        // }

        // "Third option"
        if (blockedChannels.contains(channelUID)) {
            logger.trace("Removing channel '{}' from the list of blocked channels", channelUID);
            blockedChannels.remove(channelUID);
            return;
        }

        switch (channelUID.getId()) {
            case CHANNEL_RESET: {
                if (address != null) {
                    restart();
                }
                break;
            }
            default: {
                Channel theChannel = getThing().getChannel(channelUID.getId());
                if (theChannel != null) {
                    KNXChannelSelector selector = KNXChannelSelector
                            .getValueSelectorFromChannelTypeId(theChannel.getChannelTypeUID().getId());

                    if (selector != null) {
                        try {
                            Configuration channelConfiguration = getThing().getChannel(channelUID.getId())
                                    .getConfiguration();

                            if (channelConfiguration != null) {
                                Type convertedType = knxChannelSelectorProxy.convertType(selector, channelConfiguration,
                                        newState);
                                logger.trace("State to Channel {} {} {} {}/{} : {} -> {}", channelUID.getId(),
                                        getThing().getChannel(channelUID.getId()).getConfiguration().get(DPT),
                                        getThing().getChannel(channelUID.getId()).getAcceptedItemType(),
                                        getThing().getChannel(channelUID.getId()).getConfiguration().get(READ),
                                        getThing().getChannel(channelUID.getId()).getConfiguration().get(WRITE),
                                        newState, convertedType);
                                if (convertedType != null) {
                                    for (GroupAddress address : knxChannelSelectorProxy.getWriteAddresses(selector,
                                            channelConfiguration, convertedType)) {
                                        ((KNXBridgeBaseThingHandler) getBridge().getHandler()).writeToKNX(address,
                                                knxChannelSelectorProxy.getDPT(address, selector, channelConfiguration,
                                                        convertedType),
                                                convertedType);
                                    }
                                }
                            } else {
                                logger.warn("The configuration of Channel '{}' is empty", channelUID.getId());
                            }

                        } catch (KNXFormatException e) {
                            logger.error("An exception occurred while writing to the KNX bus : '{}'", e.getMessage(),
                                    e);
                        }
                    } else {
                        logger.error("The Channel Type {} is not implemented",
                                getThing().getChannel(channelUID.getId()).getChannelTypeUID().getId());
                    }
                } else {
                    logger.warn("The Channel with UID '{}' does not exist on Thing '{}'", channelUID.getId(),
                            getThing().getUID());
                }
                break;
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (((KNXBridgeBaseThingHandler) getBridge().getHandler()) == null) {
            logger.warn("KNX bridge handler not found. Cannot handle commands without bridge.");
        }

        logger.trace("Handling a Command ({})  for Channel {}", command, channelUID.getId());

        // if (((KNXBridgeBaseThingHandler) getBridge().getHandler()).hasEvent(channelUID, command, 50, 500)) {
        // return;
        // } else {
        // ((KNXBridgeBaseThingHandler) getBridge().getHandler()).logEvent(channelUID, command);
        // }

        if (blockedChannels.contains(channelUID)) {
            logger.trace("Remvoing channel '{}' from the list of blocked channels", channelUID);
            blockedChannels.remove(channelUID);
            return;
        }
        if (command instanceof RefreshType) {

            logger.debug("Refreshing channel {}", channelUID);

            Channel theChannel = getThing().getChannel(channelUID.getId());
            if (theChannel != null) {
                KNXChannelSelector selector = KNXChannelSelector
                        .getValueSelectorFromChannelTypeId(theChannel.getChannelTypeUID().getId());

                if (selector != null) {
                    try {
                        Configuration channelConfiguration = getThing().getChannel(channelUID.getId())
                                .getConfiguration();

                        if (channelConfiguration != null) {
                            for (GroupAddress address : knxChannelSelectorProxy.getReadAddresses(selector,
                                    channelConfiguration, command)) {
                                scheduleReadJob(address, knxChannelSelectorProxy.getDPT(address, selector,
                                        channelConfiguration, command), true, BigDecimal.ZERO);
                            }
                        } else {
                            logger.warn("The configuration of channel '{}' is empty", channelUID.getId());
                        }
                    } catch (KNXFormatException e) {
                        logger.error("An exception occurred while writing to the KNX bus : '{}'", e.getMessage(), e);
                    }
                } else {
                    logger.error("The Channel Type {} is not implemented",
                            getThing().getChannel(channelUID.getId()).getChannelTypeUID().getId());
                }
            } else {
                logger.warn("The Channel with UID '{}' does not exist on Thing '{}'", channelUID.getId(),
                        getThing().getUID());
            }
        } else {
            switch (channelUID.getId()) {
                case CHANNEL_RESET: {
                    if (address != null) {
                        restart();
                    }
                    break;
                }
                default: {

                    Channel theChannel = getThing().getChannel(channelUID.getId());
                    if (theChannel != null) {

                        KNXChannelSelector selector = KNXChannelSelector
                                .getValueSelectorFromChannelTypeId(theChannel.getChannelTypeUID().getId());

                        if (selector != null) {
                            try {
                                Configuration channelConfiguration = getThing().getChannel(channelUID.getId())
                                        .getConfiguration();

                                if (channelConfiguration != null) {
                                    Type convertedType = knxChannelSelectorProxy.convertType(selector,
                                            channelConfiguration, command);

                                    logger.trace("Command to Channel {} {} {} {}/{} : {} -> {}", channelUID.getId(),
                                            getThing().getChannel(channelUID.getId()).getConfiguration().get(DPT),
                                            getThing().getChannel(channelUID.getId()).getAcceptedItemType(),
                                            getThing().getChannel(channelUID.getId()).getConfiguration().get(READ),
                                            getThing().getChannel(channelUID.getId()).getConfiguration().get(WRITE),
                                            command, convertedType);

                                    if (convertedType != null) {
                                        for (GroupAddress address : knxChannelSelectorProxy.getWriteAddresses(selector,
                                                channelConfiguration, convertedType)) {
                                            blockedChannels.add(channelUID);
                                            ((KNXBridgeBaseThingHandler) getBridge().getHandler())
                                                    .writeToKNX(address,
                                                            knxChannelSelectorProxy.getDPT(address, selector,
                                                                    channelConfiguration, convertedType),
                                                            convertedType);
                                        }
                                    }
                                } else {
                                    logger.warn("The configuration of channel '{}' is empty", channelUID.getId());
                                }
                            } catch (KNXFormatException e) {
                                logger.error("An exception occurred while writing to the KNX bus : '{}'",
                                        e.getMessage(), e);
                            }
                        } else {
                            logger.warn("The Channel Type {} is not implemented",
                                    getThing().getChannel(channelUID.getId()).getChannelTypeUID().getId());
                        }
                    } else {
                        logger.warn("The Channel with UID '{}' does not exist on Thing '{}'", channelUID.getId(),
                                getThing().getUID());
                    }
                }
            }
        }
    }

    @Override
    public void onGroupRead(KNXBridgeBaseThingHandler bridge, IndividualAddress source, GroupAddress destination,
            byte[] asdu) {
        // Nothing to do here - Software representations of physical actors should not respond to GroupRead requests, as
        // the physical device will be responding to these instead
    }

    @Override
    public void onGroupReadResponse(KNXBridgeBaseThingHandler bridge, IndividualAddress source,
            GroupAddress destination, byte[] asdu) {
        // Group Read Responses are treated the same as Group Write telegrams
        onGroupWrite(bridge, source, destination, asdu);
    }

    @Override
    public void onGroupWrite(KNXBridgeBaseThingHandler bridge, IndividualAddress source, GroupAddress destination,
            byte[] asdu) {

        logger.trace("Thing {} received a Group Write telegram from '{}' for destination '{}'", getThing().getUID(),
                source, destination);

        for (Channel channel : getThing().getChannels()) {
            KNXChannelSelector selector = KNXChannelSelector
                    .getValueSelectorFromChannelTypeId(channel.getChannelTypeUID().getId());

            if (selector != null) {
                try {
                    Configuration channelConfiguration = channel.getConfiguration();
                    Set<GroupAddress> addresses = knxChannelSelectorProxy.getReadAddresses(selector,
                            channelConfiguration, null);
                    addresses
                            .addAll(knxChannelSelectorProxy.getTransmitAddresses(selector, channelConfiguration, null));

                    if (addresses.contains(destination)) {
                        logger.trace("Thing {} processes a Group Write telegram for destination '{}' for channel '{}'",
                                getThing().getUID(), destination, channel.getUID());
                        processDataReceived(bridge, destination, asdu,
                                knxChannelSelectorProxy.getDPT(destination, selector, channelConfiguration, null),
                                channel.getUID());
                    }

                } catch (KNXFormatException e) {
                    logger.error("An exception occurred while writing to the KNX bus : '{}'", e.getMessage(), e);
                }
            }
        }
    }

    private void processDataReceived(KNXBridgeBaseThingHandler bridge, GroupAddress destination, byte[] asdu,
            String dpt, ChannelUID channelUID) {

        if (dpt != null) {

            if (KNXCoreTypeMapper.toTypeClass(dpt) == null) {
                logger.warn("DPT {} is not supported by the KNX binding.", dpt);
                return;
            }

            Datapoint datapoint = new CommandDP(destination, getThing().getUID().toString(), 0, dpt);
            Type type = bridge.getType(destination, dpt, asdu);

            if (type != null) {
                // bridge.logEvent(EventSource.EMPTY, channelUID, type);

                Set<String> linkedItems = itemChannelLinkRegistry.getLinkedItemNames(channelUID);
                for (String anItem : linkedItems) {
                    Set<ChannelUID> boundChannels = itemChannelLinkRegistry.getBoundChannels(anItem);
                    for (ChannelUID aBoundChannel : boundChannels) {
                        if (aBoundChannel.getBindingId().equals(getThing().getUID().getBindingId())
                                && !aBoundChannel.getAsString().equals(channelUID.getAsString())) {
                            logger.trace("Adding channel '{}' of Item '{}' the list of blocked channels", aBoundChannel,
                                    anItem);
                            blockedChannels.add(aBoundChannel);
                        }
                    }
                }
                if (type instanceof State) {
                    updateState(channelUID, (State) type);
                } else {
                    postCommand(channelUID, (Command) type);
                }
            } else {
                final char[] hexCode = "0123456789ABCDEF".toCharArray();
                StringBuilder sb = new StringBuilder(2 + asdu.length * 2);
                sb.append("0x");
                for (byte b : asdu) {
                    sb.append(hexCode[(b >> 4) & 0xF]);
                    sb.append(hexCode[(b & 0xF)]);
                }

                logger.warn(
                        "Ignoring KNX bus data: couldn't transform to an openHAB type (not supported). Destination='{}', datapoint='{}', data='{}'",
                        new Object[] { destination.toString(), datapoint.toString(), sb.toString() });
                return;
            }

        }
    }

    public void restart() {
        if (address != null) {
            ((KNXBridgeBaseThingHandler) getBridge().getHandler()).restartNetworkDevice(address);
        }
    }

    class ReadRunnable implements Runnable {

        private GroupAddress address;
        private String dpt;

        ReadRunnable(GroupAddress address, String dpt) {
            this.address = address;
            this.dpt = dpt;
        }

        @Override
        public void run() {
            try {
                if (getThing().getStatus() == ThingStatus.ONLINE && getBridge().getStatus() == ThingStatus.ONLINE) {

                    if (KNXCoreTypeMapper.toTypeClass(dpt) == null) {
                        logger.warn("DPT '{}' is not supported by the KNX binding", dpt);
                        return;
                    }

                    Datapoint datapoint = new CommandDP(address, getThing().getUID().toString(), 0, dpt);
                    ((KNXBridgeBaseThingHandler) getBridge().getHandler()).readDatapoint(datapoint,
                            ((KNXBridgeBaseThingHandler) getBridge().getHandler()).getReadRetriesLimit());
                }
            } catch (Exception e) {
                logger.error(
                        "An exception occurred while reading the group address '{}' with DPT '{}' for Thing '{}' : {}",
                        address, dpt, getThing().getUID(), e.getMessage(), e);
            }
        }
    };

    private Runnable pollingRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                if (address != null && getBridge().getStatus() == ThingStatus.ONLINE) {
                    logger.debug("Polling the individual address {}", address.toString());
                    boolean isReachable = ((KNXBridgeBaseThingHandler) getBridge().getHandler()).isReachable(address);
                    if (isReachable) {
                        updateStatus(ThingStatus.ONLINE);
                        if (!filledDescription) {
                            if (descriptionJob == null || descriptionJob.isCancelled()) {
                                descriptionJob = knxScheduler.schedule(descriptionRunnable, 0, TimeUnit.MILLISECONDS);
                            }
                        }
                    } else {
                        updateStatus(ThingStatus.OFFLINE);
                    }
                }
            } catch (Exception e) {
                logger.error("An exception occurred while testing the reachability of a Thing '{}' : {}",
                        getThing().getUID(), e);
            }
        }
    };

    private Runnable descriptionRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                if (getBridge().getStatus() == ThingStatus.ONLINE) {
                    logger.debug("Fetching device information for address {}", address.toString());

                    Thread.sleep(OPERATION_INTERVAL);
                    byte[] data = ((KNXBridgeBaseThingHandler) getBridge().getHandler()).readDeviceDescription(address,
                            0, false, OPERATION_TIMEOUT);

                    if (data != null) {
                        final DD0 dd = DeviceDescriptor.DD0.fromType0(data);

                        Map<String, String> properties = editProperties();
                        properties.put(FIRMWARE_TYPE, Firmware.getName(dd.getFirmwareType()));
                        properties.put(FIRMWARE_VERSION, Firmware.getName(dd.getFirmwareVersion()));
                        properties.put(FIRMWARE_SUBVERSION, Firmware.getName(dd.getSubcode()));
                        try {
                            updateProperties(properties);
                        } catch (Exception e) {
                            // TODO : ignore for now, but for Things created through the DSL, this should also NOT throw
                            // an exception! See forum discussions
                        }
                        logger.info("The device with address {} is of type {}, version {}, subversion {}", address,
                                Firmware.getName(dd.getFirmwareType()), Firmware.getName(dd.getFirmwareVersion()),
                                Firmware.getName(dd.getSubcode()));
                    } else {
                        logger.warn("The KNX Actor with address {} does not expose a Device Descriptor", address);
                    }

                    // check if there is a Device Object in the KNX Actor
                    Thread.sleep(OPERATION_INTERVAL);
                    byte[] elements = ((KNXBridgeBaseThingHandler) getBridge().getHandler()).readDeviceProperties(
                            address, DEVICE_OBJECT, PID.OBJECT_TYPE, 0, 1, false, OPERATION_TIMEOUT);
                    if ((elements == null ? 0 : toUnsigned(elements)) == 1) {

                        Thread.sleep(OPERATION_INTERVAL);
                        String ManufacturerID = Manufacturer.getName(
                                toUnsigned(((KNXBridgeBaseThingHandler) getBridge().getHandler()).readDeviceProperties(
                                        address, DEVICE_OBJECT, PID.MANUFACTURER_ID, 1, 1, false, OPERATION_TIMEOUT)));
                        Thread.sleep(OPERATION_INTERVAL);
                        String serialNo = DataUnitBuilder.toHex(
                                ((KNXBridgeBaseThingHandler) getBridge().getHandler()).readDeviceProperties(address,
                                        DEVICE_OBJECT, PID.SERIAL_NUMBER, 1, 1, false, OPERATION_TIMEOUT),
                                "");
                        Thread.sleep(OPERATION_INTERVAL);
                        String hardwareType = DataUnitBuilder.toHex(
                                ((KNXBridgeBaseThingHandler) getBridge().getHandler()).readDeviceProperties(address,
                                        DEVICE_OBJECT, HARDWARE_TYPE, 1, 1, false, OPERATION_TIMEOUT),
                                " ");
                        Thread.sleep(OPERATION_INTERVAL);
                        String firmwareRevision = Integer.toString(toUnsigned(
                                ((KNXBridgeBaseThingHandler) getBridge().getHandler()).readDeviceProperties(address,
                                        DEVICE_OBJECT, PID.FIRMWARE_REVISION, 1, 1, false, OPERATION_TIMEOUT)));

                        Map<String, String> properties = editProperties();
                        properties.put(MANUFACTURER_NAME, ManufacturerID);
                        properties.put(MANUFACTURER_SERIAL_NO, serialNo);
                        properties.put(MANUFACTURER_HARDWARE_TYPE, hardwareType);
                        properties.put(MANUFACTURER_FIRMWARE_REVISION, firmwareRevision);
                        try {
                            updateProperties(properties);
                        } catch (Exception e) {
                            // TODO : ignore for now, but for Things created through the DSL, this should also NOT throw
                            // an exception! See forum discussions
                        }
                        logger.info("Identified device {} as a {}, type {}, revision {}, serial number {}", address,
                                ManufacturerID, hardwareType, firmwareRevision, serialNo);
                    } else {
                        logger.warn("The KNX Actor with address {} does not expose a Device Object", address);
                    }

                    // TODO : According to the KNX specs, devices should expose the PID.IO_LIST property in the DEVICE
                    // object, but it seems that a lot, if not all, devices do not do this. In this list we can find out
                    // what other kind of objects the device is exposing. Most devices do implement some set of objects,
                    // we will just go ahead and try to read them out irrespective of what is in the IO_LIST

                    Thread.sleep(OPERATION_INTERVAL);
                    byte[] tableaddress = ((KNXBridgeBaseThingHandler) getBridge().getHandler()).readDeviceProperties(
                            address, ADDRESS_TABLE_OBJECT, PID.TABLE_REFERENCE, 1, 1, false, OPERATION_TIMEOUT);

                    if (tableaddress != null) {
                        Thread.sleep(OPERATION_INTERVAL);
                        elements = ((KNXBridgeBaseThingHandler) getBridge().getHandler()).readDeviceMemory(address,
                                toUnsigned(tableaddress), 1, false, OPERATION_TIMEOUT);
                        if (elements != null) {
                            int numberOfElements = toUnsigned(elements);
                            logger.debug("The KNX Actor with address {} uses {} group addresses", address,
                                    numberOfElements - 1);

                            byte[] addressData = null;
                            while (addressData == null) {
                                Thread.sleep(OPERATION_INTERVAL);
                                addressData = ((KNXBridgeBaseThingHandler) getBridge().getHandler()).readDeviceMemory(
                                        address, toUnsigned(tableaddress) + 1, 2, false, OPERATION_TIMEOUT);
                                if (addressData != null) {
                                    IndividualAddress individualAddress = new IndividualAddress(addressData);
                                    logger.debug(
                                            "The KNX Actor with address {} its real reported individual address is  {}",
                                            address, individualAddress);
                                }
                            }

                            for (int i = 1; i < numberOfElements; i++) {
                                addressData = null;
                                while (addressData == null) {
                                    Thread.sleep(OPERATION_INTERVAL);
                                    addressData = ((KNXBridgeBaseThingHandler) getBridge().getHandler())
                                            .readDeviceMemory(address, toUnsigned(tableaddress) + 1 + i * 2, 2, false,
                                                    OPERATION_TIMEOUT);
                                    if (addressData != null) {
                                        GroupAddress groupAddress = new GroupAddress(addressData);
                                        foundGroupAddresses.add(groupAddress);
                                    }
                                }
                            }

                            for (GroupAddress anAddress : foundGroupAddresses) {
                                logger.debug("The KNX Actor with address {} uses Group Address {}", address, anAddress);
                            }
                        }
                    } else {
                        logger.warn("The KNX Actor with address {} does not expose a Group Address table", address);
                    }

                    Thread.sleep(OPERATION_INTERVAL);
                    byte[] objecttableaddress = ((KNXBridgeBaseThingHandler) getBridge().getHandler())
                            .readDeviceProperties(address, GROUPOBJECT_OBJECT, PID.TABLE_REFERENCE, 1, 1, true,
                                    OPERATION_TIMEOUT);

                    if (objecttableaddress != null) {
                        Thread.sleep(OPERATION_INTERVAL);
                        elements = ((KNXBridgeBaseThingHandler) getBridge().getHandler()).readDeviceMemory(address,
                                toUnsigned(objecttableaddress), 1, false, OPERATION_TIMEOUT);
                        if (elements != null) {
                            int numberOfElements = toUnsigned(elements);
                            logger.debug("The KNX Actor with address {} has {} objects", address, numberOfElements);

                            for (int i = 1; i < numberOfElements; i++) {
                                byte[] objectData = null;
                                while (objectData == null) {
                                    Thread.sleep(OPERATION_INTERVAL);
                                    objectData = ((KNXBridgeBaseThingHandler) getBridge().getHandler())
                                            .readDeviceMemory(address, toUnsigned(objecttableaddress) + 1 + (i * 3), 3,
                                                    false, OPERATION_TIMEOUT);

                                    logger.debug("Byte 1 {}",
                                            String.format("%8s", Integer.toBinaryString(objectData[0] & 0xFF))
                                                    .replace(' ', '0'));
                                    logger.debug("Byte 2 {}",
                                            String.format("%8s", Integer.toBinaryString(objectData[1] & 0xFF))
                                                    .replace(' ', '0'));
                                    logger.debug("Byte 3 {}",
                                            String.format("%8s", Integer.toBinaryString(objectData[2] & 0xFF))
                                                    .replace(' ', '0'));
                                }
                            }
                        }
                    } else {
                        logger.warn("The KNX Actor with address {} does not expose a Group Object table", address);
                    }

                    filledDescription = true;
                }
            } catch (Exception e) {
                logger.error("An exception occurred while fetching the device description for a Thing '{}' : {}",
                        getThing().getUID(), e.getMessage(), e);
            }
        }

        private int toUnsigned(final byte[] data) {
            int value = data[0] & 0xff;
            if (data.length == 1) {
                return value;
            }
            value = value << 8 | data[1] & 0xff;
            if (data.length == 2) {
                return value;
            }
            value = value << 16 | data[2] & 0xff << 8 | data[3] & 0xff;
            return value;
        }
    };

    public enum LoadState {
        L0(0, "Unloaded"),
        L1(1, "Loaded"),
        L2(2, "Loading"),
        L3(3, "Error"),
        L4(4, "Unloading"),
        L5(5, "Load Completing");

        private int code;
        private String name;

        private LoadState(int code, String name) {
            this.code = code;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        public static String getName(int code) {
            for (LoadState c : LoadState.values()) {
                if (c.code == code) {
                    return c.name;
                }
            }
            return null;
        }

    };

    public enum Firmware {
        F0(0, "BCU 1, BCU 2, BIM M113"),
        F1(1, "Unidirectional devices"),
        F3(3, "Property based device management"),
        F7(7, "BIM M112"),
        F8(8, "IR Decoder, TP1 legacy"),
        F9(9, "Repeater, Coupler");

        private int code;
        private String name;

        private Firmware(int code, String name) {
            this.code = code;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        public static String getName(int code) {
            for (Firmware c : Firmware.values()) {
                if (c.code == code) {
                    return c.name;
                }
            }
            return null;
        }

    };

    public enum Manufacturer {
        M1(1, "Siemens"),
        M2(2, "ABB"),
        M4(4, "Albrecht Jung"),
        M5(5, "Bticino"),
        M6(6, "Berker"),
        M7(7, "Busch-Jaeger Elektro"),
        M8(8, "GIRA Giersiepen"),
        M9(9, "Hager Electro"),
        M10(10, "INSTA ELEKTRO"),
        M11(11, "LEGRAND Appareillage électrique"),
        M12(12, "Merten"),
        M14(14, "ABB SpA – SACE Division"),
        M22(22, "Siedle & Söhne"),
        M24(24, "Eberle"),
        M25(25, "GEWISS"),
        M27(27, "Albert Ackermann"),
        M28(28, "Schupa GmbH"),
        M29(29, "ABB SCHWEIZ"),
        M30(30, "Feller"),
        M32(32, "DEHN & SÖHNE"),
        M33(33, "CRABTREE"),
        M36(36, "Paul Hochköpper"),
        M37(37, "Altenburger Electronic"),
        M41(41, "Grässlin"),
        M42(42, "Simon"),
        M44(44, "VIMAR"),
        M45(45, "Moeller Gebäudeautomation KG"),
        M46(46, "Eltako"),
        M49(49, "Bosch-Siemens Haushaltsgeräte"),
        M52(52, "RITTO GmbH&Co.KG"),
        M53(53, "Power Controls"),
        M55(55, "ZUMTOBEL"),
        M57(57, "Phoenix Contact"),
        M61(61, "WAGO Kontakttechnik"),
        M66(66, "Wieland Electric"),
        M67(67, "Hermann Kleinhuis"),
        M69(69, "Stiebel Eltron"),
        M71(71, "Tehalit"),
        M72(72, "Theben AG"),
        M73(73, "Wilhelm Rutenbeck"),
        M75(75, "Winkhaus"),
        M76(76, "Robert Bosch"),
        M78(78, "Somfy"),
        M80(80, "Woertz"),
        M81(81, "Viessmann Werke"),
        M82(82, "Theodor HEIMEIER Metallwerk"),
        M83(83, "Joh. Vaillant"),
        M85(85, "AMP Deutschland"),
        M89(89, "Bosch Thermotechnik GmbH"),
        M90(90, "SEF - ECOTEC"),
        M92(92, "DORMA GmbH + Co. KG"),
        M93(93, "WindowMaster A/S"),
        M94(94, "Walther Werke"),
        M95(95, "ORAS"),
        M97(97, "Dätwyler"),
        M98(98, "Electrak"),
        M99(99, "Techem"),
        M100(100, "Schneider Electric Industries SAS"),
        M101(101, "WHD Wilhelm Huber + Söhne"),
        M102(102, "Bischoff Elektronik"),
        M104(104, "JEPAZ"),
        M105(105, "RTS Automation"),
        M106(106, "EIBMARKT GmbH"),
        M107(107, "WAREMA electronic GmbH"),
        M108(108, "Eelectron"),
        M109(109, "Belden Wire & Cable B.V."),
        M110(110, "Becker-Antriebe GmbH"),
        M111(111, "J.Stehle+Söhne GmbH"),
        M112(112, "AGFEO"),
        M113(113, "Zennio"),
        M114(114, "TAPKO Technologies"),
        M115(115, "HDL"),
        M116(116, "Uponor"),
        M117(117, "se Lightmanagement AG"),
        M118(118, "Arcus-eds"),
        M119(119, "Intesis"),
        M120(120, "Herholdt Controls srl"),
        M121(121, "Zublin AG"),
        M122(122, "Durable Technologies"),
        M123(123, "Innoteam"),
        M124(124, "ise GmbH"),
        M125(125, "TEAM FOR TRONICS"),
        M126(126, "CIAT"),
        M127(127, "Remeha BV"),
        M128(128, "ESYLUX"),
        M129(129, "BASALTE"),
        M130(130, "Vestamatic"),
        M131(131, "MDT technologies"),
        M132(132, "Warendorfer Küchen GmbH"),
        M133(133, "Video-Star"),
        M134(134, "Sitek"),
        M135(135, "CONTROLtronic"),
        M136(136, "function Technology"),
        M137(137, "AMX"),
        M138(138, "ELDAT"),
        M139(139, "VIKO"),
        M140(140, "Pulse Technologies"),
        M141(141, "Crestron"),
        M142(142, "STEINEL professional"),
        M143(143, "BILTON LED Lighting"),
        M144(144, "denro AG"),
        M145(145, "GePro"),
        M146(146, "preussen automation"),
        M147(147, "Zoppas Industries"),
        M148(148, "MACTECH"),
        M149(149, "TECHNO-TREND"),
        M150(150, "FS Cables"),
        M151(151, "Delta Dore"),
        M152(152, "Eissound"),
        M153(153, "Cisco"),
        M154(154, "Dinuy"),
        M155(155, "iKNiX"),
        M156(156, "Rademacher Geräte-Elektronik GmbH & Co. KG"),
        M157(157, "EGi Electroacustica General Iberica"),
        M158(158, "Ingenium"),
        M159(159, "ElabNET"),
        M160(160, "Blumotix"),
        M161(161, "Hunter Douglas"),
        M162(162, "APRICUM"),
        M163(163, "TIANSU Automation"),
        M164(164, "Bubendorff"),
        M165(165, "MBS GmbH"),
        M166(166, "Enertex Bayern GmbH"),
        M167(167, "BMS"),
        M168(168, "Sinapsi"),
        M169(169, "Embedded Systems SIA"),
        M170(170, "KNX1"),
        M171(171, "Tokka"),
        M172(172, "NanoSense"),
        M173(173, "PEAR Automation GmbH"),
        M174(174, "DGA"),
        M175(175, "Lutron"),
        M176(176, "AIRZONE – ALTRA"),
        M177(177, "Lithoss Design Switches"),
        M178(178, "3ATEL"),
        M179(179, "Philips Controls"),
        M180(180, "VELUX A/S"),
        M181(181, "LOYTEC"),
        M182(182, "SBS S.p.A."),
        M183(183, "SIRLAN Technologies"),
        M184(184, "Bleu Comm' Azur"),
        M185(185, "IT GmbH"),
        M186(186, "RENSON"),
        M187(187, "HEP Group"),
        M188(188, "Balmart"),
        M189(189, "GFS GmbH"),
        M190(190, "Schenker Storen AG"),
        M191(191, "Algodue Elettronica S.r.L."),
        M192(192, "Newron System"),
        M193(193, "maintronic"),
        M194(194, "Vantage"),
        M195(195, "Foresis"),
        M196(196, "Research & Production Association SEM"),
        M197(197, "Weinzierl Engineering GmbH"),
        M198(198, "Möhlenhoff Wärmetechnik GmbH"),
        M199(199, "PKC-GROUP Oyj"),
        M200(200, "B.E.G."),
        M201(201, "Elsner Elektronik GmbH"),
        M202(202, "Siemens Building Technologies (HK/China) Ltd."),
        M204(204, "Eutrac"),
        M205(205, "Gustav Hensel GmbH & Co. KG"),
        M206(206, "GARO AB"),
        M207(207, "Waldmann Lichttechnik"),
        M208(208, "SCHÜCO"),
        M209(209, "EMU"),
        M210(210, "JNet Systems AG"),
        M214(214, "O.Y.L. Electronics"),
        M215(215, "Galax System"),
        M216(216, "Disch"),
        M217(217, "Aucoteam"),
        M218(218, "Luxmate Controls"),
        M219(219, "Danfoss"),
        M220(220, "AST GmbH"),
        M222(222, "WILA Leuchten"),
        M223(223, "b+b Automations- und Steuerungstechnik"),
        M225(225, "Lingg & Janke"),
        M227(227, "Sauter"),
        M228(228, "SIMU"),
        M232(232, "Theben HTS AG"),
        M233(233, "Amann GmbH"),
        M234(234, "BERG Energiekontrollsysteme GmbH"),
        M235(235, "Hüppe Form Sonnenschutzsysteme GmbH"),
        M237(237, "Oventrop KG"),
        M238(238, "Griesser AG"),
        M239(239, "IPAS GmbH"),
        M240(240, "elero GmbH"),
        M241(241, "Ardan Production and Industrial Controls Ltd."),
        M242(242, "Metec Meßtechnik GmbH"),
        M244(244, "ELKA-Elektronik GmbH"),
        M245(245, "ELEKTROANLAGEN D. NAGEL"),
        M246(246, "Tridonic Bauelemente GmbH"),
        M248(248, "Stengler Gesellschaft"),
        M249(249, "Schneider Electric (MG)"),
        M250(250, "KNX Association"),
        M251(251, "VIVO"),
        M252(252, "Hugo Müller GmbH & Co KG"),
        M253(253, "Siemens HVAC"),
        M254(254, "APT"),
        M256(256, "HighDom"),
        M257(257, "Top Services"),
        M258(258, "ambiHome"),
        M259(259, "DATEC electronic AG"),
        M260(260, "ABUS Security-Center"),
        M261(261, "Lite-Puter"),
        M262(262, "Tantron Electronic"),
        M263(263, "Yönnet"),
        M264(264, "DKX Tech"),
        M265(265, "Viatron"),
        M266(266, "Nautibus"),
        M268(268, "Longchuang"),
        M269(269, "Air-On AG"),
        M270(270, "ib-company GmbH"),
        M271(271, "SATION"),
        M272(272, "Agentilo GmbH"),
        M273(273, "Makel Elektrik"),
        M274(274, "Helios Ventilatoren"),
        M275(275, "Otto Solutions Pte Ltd"),
        M276(276, "Airmaster"),
        M277(277, "HEINEMANN GmbH"),
        M278(278, "LDS"),
        M279(279, "ASIN"),
        M280(280, "Bridges"),
        M281(281, "ARBONIA"),
        M282(282, "KERMI"),
        M283(283, "PROLUX"),
        M284(284, "ClicHome"),
        M285(285, "COMMAX"),
        M286(286, "EAE"),
        M287(287, "Tense"),
        M288(288, "Seyoung Electronics"),
        M289(289, "Lifedomus"),
        M290(290, "EUROtronic Technology GmbH"),
        M291(291, "tci"),
        M292(292, "Rishun Electronic"),
        M293(293, "Zipato"),
        M294(294, "cm-security GmbH & Co KG"),
        M295(295, "Qing Cables"),
        M296(296, "LABIO"),
        M297(297, "Coster Tecnologie Elettroniche S.p.A."),
        M298(298, "E.G.E"),
        M299(299, "NETxAutomation"),
        M300(300, "tecalor"),
        M301(301, "Urmet Electronics (Huizhou) Ltd."),
        M302(302, "Peiying Building Control"),
        M303(303, "BPT S.p.A. a Socio Unico"),
        M304(304, "Kanontec - KanonBUS"),
        M305(305, "ISER Tech"),
        M306(306, "Fineline"),
        M307(307, "CP Electronics Ltd"),
        M308(308, "Servodan A/S"),
        M309(309, "Simon"),
        M310(310, "GM modular pvt. Ltd."),
        M311(311, "FU CHENG Intelligence"),
        M312(312, "NexKon"),
        M313(313, "FEEL s.r.l"),
        M314(314, "Not Assigned"),
        M315(315, "Shenzhen Fanhai Sanjiang Electronics Co., Ltd."),
        M316(316, "Jiuzhou Greeble"),
        M317(317, "Aumüller Aumatic GmbH"),
        M318(318, "Etman Electric"),
        M319(319, "EMT Controls"),
        M320(320, "ZidaTech AG"),
        M321(321, "IDGS bvba"),
        M322(322, "dakanimo"),
        M323(323, "Trebor Automation AB"),
        M324(324, "Satel sp. z o.o."),
        M325(325, "Russound, Inc."),
        M326(326, "Midea Heating & Ventilating Equipment CO LTD"),
        M327(327, "Consorzio Terranuova"),
        M328(328, "Wolf Heiztechnik GmbH"),
        M329(329, "SONTEC"),
        M330(330, "Belcom Cables Ltd."),
        M331(331, "Guangzhou SeaWin Electrical Technologies Co., Ltd."),
        M332(332, "Acrel"),
        M333(333, "Franke Aquarotter GmbH"),
        M334(334, "Orion Systems"),
        M335(335, "Schrack Technik GmbH"),
        M336(336, "INSPRID"),
        M337(337, "Sunricher"),
        M338(338, "Menred automation system(shanghai) Co.,Ltd."),
        M339(339, "Aurex"),
        M340(340, "Josef Barthelme GmbH & Co. KG"),
        M341(341, "Architecture Numerique"),
        M342(342, "UP GROUP"),
        M343(343, "Teknos-Avinno"),
        M344(344, "Ningbo Dooya Mechanic & Electronic Technology"),
        M345(345, "Thermokon Sensortechnik GmbH"),
        M346(346, "BELIMO Automation AG"),
        M347(347, "Zehnder Group International AG"),
        M348(348, "sks Kinkel Elektronik"),
        M349(349, "ECE Wurmitzer GmbH"),
        M350(350, "LARS"),
        M351(351, "URC"),
        M352(352, "LightControl"),
        M353(353, "ShenZhen YM"),
        M354(354, "MEAN WELL Enterprises Co. Ltd."),
        M355(355, "OSix"),
        M356(356, "AYPRO Technology"),
        M357(357, "Hefei Ecolite Software"),
        M358(358, "Enno"),
        M359(359, "Ohosure");

        private int code;
        private String name;

        private Manufacturer(int code, String name) {
            this.code = code;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        public static String getName(int code) {
            for (Manufacturer c : Manufacturer.values()) {
                if (c.code == code) {
                    return c.name;
                }
            }
            return null;
        }
    }
}
