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
import static org.openhab.binding.knx.internal.handler.DeviceConstants.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
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
import org.openhab.binding.knx.GroupAddressListener;
import org.openhab.binding.knx.IndividualAddressListener;
import org.openhab.binding.knx.internal.channel.KNXChannelSelector;
import org.openhab.binding.knx.internal.channel.KNXChannelType;
import org.openhab.binding.knx.internal.handler.BasicConfig;
import org.openhab.binding.knx.internal.handler.Firmware;
import org.openhab.binding.knx.internal.handler.Manufacturer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.DataUnitBuilder;
import tuwien.auto.calimero.DeviceDescriptor;
import tuwien.auto.calimero.DeviceDescriptor.DD0;
import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.datapoint.CommandDP;
import tuwien.auto.calimero.datapoint.Datapoint;
import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.exception.KNXFormatException;
import tuwien.auto.calimero.mgmt.PropertyAccess.PID;

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

    @Nullable
    private ScheduledExecutorService knxScheduler;

    private static final long OPERATION_TIMEOUT = 5000;
    private static final long OPERATION_INTERVAL = 2000;
    private boolean filledDescription = false;

    public KNXBasicThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        BasicConfig config = getConfigAs(BasicConfig.class);

        initializeGroupAddresses();

        knxScheduler = getBridgeHandler().getScheduler();
        try {
            if (StringUtils.isNotBlank(config.getAddress())) {
                address = new IndividualAddress(config.getAddress());

                long pollingInterval = config.getInterval().longValue();
                long initialDelay = Math.round(pollingInterval * random.nextFloat());

                if ((pollingJob == null || pollingJob.isCancelled())) {
                    logger.debug("'{}' will be polled every {}s", getThing().getUID(), pollingInterval);
                    pollingJob = knxScheduler.scheduleWithFixedDelay(() -> pollDeviceStatus(), initialDelay,
                            pollingInterval, TimeUnit.SECONDS);
                }
            } else {
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (KNXFormatException e) {
            logger.error("An exception occurred while setting the individual address '{}': {}", config.getAddress(),
                    e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getLocalizedMessage());
        }

        getBridgeHandler().registerGroupAddressListener(this);
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

        KNXBridgeBaseThingHandler bridgeHandler = getBridgeHandler();
        bridgeHandler.unregisterGroupAddressListener(this);
    }

    private void cancelReadFutures() {
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
        if (knxScheduler == null) {
            return;
        }

        boolean recurring = readInterval != null && readInterval.intValue() > 0;

        Runnable readRunnable = () -> {
            if (getThing().getStatus() == ThingStatus.ONLINE && getBridge().getStatus() == ThingStatus.ONLINE) {
                if (!getBridgeHandler().isDPTSupported(dpt)) {
                    logger.warn("DPT '{}' is not supported by the KNX binding", dpt);
                    return;
                }
                Datapoint datapoint = new CommandDP(groupAddress, getThing().getUID().toString(), 0, dpt);
                getBridgeHandler().readDatapoint(datapoint, getBridgeHandler().getReadRetriesLimit());
            }
        };
        if (immediate) {
            knxScheduler.schedule(readRunnable, 0, TimeUnit.SECONDS);
        }
        if (recurring && readInterval != null) {
            ScheduledFuture<?> future = readFutures.get(groupAddress);
            if (future == null || future.isDone() || future.isCancelled()) {
                int initialDelay = immediate ? 0 : readInterval.intValue();
                future = knxScheduler.scheduleWithFixedDelay(readRunnable, initialDelay, readInterval.intValue(),
                        TimeUnit.SECONDS);
                readFutures.put(groupAddress, future);
            }
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);

        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            getBridgeHandler().registerGroupAddressListener(this);
            scheduleReadJobs();
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
        } else if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            cancelReadFutures();
            getBridgeHandler().unregisterGroupAddressListener(this);
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
        if (getBridgeHandler() == null) {
            logger.warn("KNX bridge handler not found. Cannot handle commands without bridge.");
        }
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
                logger.trace("Sending to channel {} {} {} {}/{} : {} -> {}", channelUID.getId(),
                        getThing().getChannel(channelUID.getId()).getConfiguration().get(DPT),
                        getThing().getChannel(channelUID.getId()).getAcceptedItemType(),
                        getThing().getChannel(channelUID.getId()).getConfiguration().get(READ),
                        getThing().getChannel(channelUID.getId()).getConfiguration().get(WRITE), type, convertedType);
            }
            if (convertedType != null) {
                for (GroupAddress address : selector.getWriteAddresses(channelConfiguration, convertedType)) {
                    getBridgeHandler().writeToKNX(address, selector.getDPT(address, channelConfiguration),
                            convertedType);
                }
            }
        });
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
            withKNXType(channel, (selector, channelConfiguration) -> {
                Set<GroupAddress> addresses = selector.getReadAddresses(channelConfiguration);
                addresses.addAll(selector.getTransmitAddresses(channelConfiguration, null));

                if (addresses.contains(destination)) {
                    logger.trace("Thing {} processes a Group Write telegram for destination '{}' for channel '{}'",
                            getThing().getUID(), destination, channel.getUID());
                    processDataReceived(bridge, destination, asdu, selector.getDPT(destination, channelConfiguration),
                            channel.getUID());
                }
            });
        }
    }

    private void processDataReceived(KNXBridgeBaseThingHandler bridge, GroupAddress destination, byte[] asdu,
            @Nullable String dpt, ChannelUID channelUID) {

        if (dpt != null) {

            if (!bridge.isDPTSupported(dpt)) {
                logger.warn("DPT {} is not supported by the KNX binding.", dpt);
                return;
            }

            Datapoint datapoint = new CommandDP(destination, getThing().getUID().toString(), 0, dpt);
            Type type = bridge.getType(destination, dpt, asdu);

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
            getBridgeHandler().restartNetworkDevice(address);
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
            if (getThing().getStatus() == ThingStatus.ONLINE && getBridge().getStatus() == ThingStatus.ONLINE) {
                if (!getBridgeHandler().isDPTSupported(dpt)) {
                    logger.warn("DPT '{}' is not supported by the KNX binding", dpt);
                    return;
                }
                Datapoint datapoint = new CommandDP(address, getThing().getUID().toString(), 0, dpt);
                getBridgeHandler().readDatapoint(datapoint, getBridgeHandler().getReadRetriesLimit());
            }
        }
    };

    private void pollDeviceStatus() {
        try {
            if (address != null && getBridge().getStatus() == ThingStatus.ONLINE) {
                logger.debug("Polling the individual address {}", address);
                boolean isReachable = getBridgeHandler().isReachable(address);
                if (isReachable) {
                    updateStatus(ThingStatus.ONLINE);
                    BasicConfig config = getConfigAs(BasicConfig.class);
                    if (!filledDescription && config.getFetch()) {
                        if (descriptionJob == null || descriptionJob.isCancelled()) {
                            descriptionJob = knxScheduler.schedule(descriptionRunnable, 0, TimeUnit.MILLISECONDS);
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

    private final Runnable descriptionRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                if (getBridge().getStatus() == ThingStatus.ONLINE) {
                    IndividualAddress address = KNXBasicThingHandler.this.address;
                    if (address == null) {
                        return;
                    }
                    logger.debug("Fetching device information for address {}", address);

                    Thread.sleep(OPERATION_INTERVAL);
                    byte[] data = getBridgeHandler().readDeviceDescription(address, 0, false, OPERATION_TIMEOUT);

                    if (data != null) {
                        final DD0 dd = DeviceDescriptor.DD0.fromType0(data);

                        Map<String, String> properties = editProperties();
                        properties.put(FIRMWARE_TYPE, Firmware.getName(dd.getFirmwareType()));
                        properties.put(FIRMWARE_VERSION, Firmware.getName(dd.getFirmwareVersion()));
                        properties.put(FIRMWARE_SUBVERSION, Firmware.getName(dd.getSubcode()));
                        updateProperties(properties);
                        logger.info("The device with address {} is of type {}, version {}, subversion {}", address,
                                Firmware.getName(dd.getFirmwareType()), Firmware.getName(dd.getFirmwareVersion()),
                                Firmware.getName(dd.getSubcode()));
                    } else {
                        logger.warn("The KNX Actor with address {} does not expose a Device Descriptor", address);
                    }

                    // check if there is a Device Object in the KNX Actor
                    Thread.sleep(OPERATION_INTERVAL);
                    byte[] elements = getBridgeHandler().readDeviceProperties(address, DEVICE_OBJECT, PID.OBJECT_TYPE,
                            0, 1, false, OPERATION_TIMEOUT);
                    if ((elements == null ? 0 : toUnsigned(elements)) == 1) {

                        Thread.sleep(OPERATION_INTERVAL);
                        String ManufacturerID = Manufacturer
                                .getName(toUnsigned(getBridgeHandler().readDeviceProperties(address, DEVICE_OBJECT,
                                        PID.MANUFACTURER_ID, 1, 1, false, OPERATION_TIMEOUT)));
                        Thread.sleep(OPERATION_INTERVAL);
                        String serialNo = DataUnitBuilder.toHex(getBridgeHandler().readDeviceProperties(address,
                                DEVICE_OBJECT, PID.SERIAL_NUMBER, 1, 1, false, OPERATION_TIMEOUT), "");
                        Thread.sleep(OPERATION_INTERVAL);
                        String hardwareType = DataUnitBuilder.toHex(getBridgeHandler().readDeviceProperties(address,
                                DEVICE_OBJECT, HARDWARE_TYPE, 1, 1, false, OPERATION_TIMEOUT), " ");
                        Thread.sleep(OPERATION_INTERVAL);
                        String firmwareRevision = Integer
                                .toString(toUnsigned(getBridgeHandler().readDeviceProperties(address, DEVICE_OBJECT,
                                        PID.FIRMWARE_REVISION, 1, 1, false, OPERATION_TIMEOUT)));

                        Map<String, String> properties = editProperties();
                        properties.put(MANUFACTURER_NAME, ManufacturerID);
                        properties.put(MANUFACTURER_SERIAL_NO, serialNo);
                        properties.put(MANUFACTURER_HARDWARE_TYPE, hardwareType);
                        properties.put(MANUFACTURER_FIRMWARE_REVISION, firmwareRevision);
                        updateProperties(properties);
                        logger.info("Identified device {} as a {}, type {}, revision {}, serial number {}", address,
                                ManufacturerID, hardwareType, firmwareRevision, serialNo);
                    } else {
                        logger.warn("The KNX Actor with address {} does not expose a Device Object", address);
                    }

                    // According to the KNX specs, devices should expose the PID.IO_LIST property in the DEVICE
                    // object, but it seems that a lot, if not all, devices do not do this. In this list we can find out
                    // what other kind of objects the device is exposing. Most devices do implement some set of objects,
                    // we will just go ahead and try to read them out irrespective of what is in the IO_LIST

                    Thread.sleep(OPERATION_INTERVAL);
                    byte[] tableaddress = getBridgeHandler().readDeviceProperties(address, ADDRESS_TABLE_OBJECT,
                            PID.TABLE_REFERENCE, 1, 1, false, OPERATION_TIMEOUT);

                    if (tableaddress != null) {
                        Thread.sleep(OPERATION_INTERVAL);
                        elements = getBridgeHandler().readDeviceMemory(address, toUnsigned(tableaddress), 1, false,
                                OPERATION_TIMEOUT);
                        if (elements != null) {
                            int numberOfElements = toUnsigned(elements);
                            logger.debug("The KNX Actor with address {} uses {} group addresses", address,
                                    numberOfElements - 1);

                            byte[] addressData = null;
                            while (addressData == null) {
                                Thread.sleep(OPERATION_INTERVAL);
                                addressData = getBridgeHandler().readDeviceMemory(address, toUnsigned(tableaddress) + 1,
                                        2, false, OPERATION_TIMEOUT);
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
                                    addressData = getBridgeHandler().readDeviceMemory(address,
                                            toUnsigned(tableaddress) + 1 + i * 2, 2, false, OPERATION_TIMEOUT);
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
                    filledDescription = true;
                }
            } catch (Exception e) {
                logger.error("An exception occurred while fetching the device description for a Thing '{}' : {}",
                        getThing().getUID(), e.getMessage(), e);
            }
        }

        private int toUnsigned(final byte @Nullable [] data) {
            if (data == null) {
                return 0;
            }
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

}
