package org.openhab.binding.knx.handler;

import static org.openhab.binding.knx.KNXBindingConstants.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
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

public abstract class PhysicalActorThingHandler extends KNXBaseThingHandler {

    protected Logger logger = LoggerFactory.getLogger(PhysicalActorThingHandler.class);

    // List of all Configuration parameters
    public static final String READ = "read";
    public static final String INTERVAL = "interval";
    public static final String ADDRESS = "address";
    protected static long POLLING_INTERVAL = 60000;
    protected static long OPERATION_TIMEOUT = 5000;
    protected static long OPERATION_INTERVAL = 2000;
    protected static Random randomGenerator = new Random();
    protected boolean filledDescription = false;

    // list of addresses that have to read from the KNX bus pro-actively every INTERVAL seconds
    protected Set<GroupAddress> readAddresses = Collections.synchronizedSet(new HashSet<GroupAddress>());
    private ScheduledFuture<?> readJob;
    private ScheduledFuture<?> pollingJob;
    private ScheduledFuture<?> descriptionJob;

    protected List<GroupAddress> foundGroupAddresses = new ArrayList<GroupAddress>();

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
    static final int HARDWARE_TYPE = 78;

    public PhysicalActorThingHandler(Thing thing, ItemChannelLinkRegistry itemChannelLinkRegistry) {
        super(thing, itemChannelLinkRegistry);
    }

    @Override
    public void initialize() {
        super.initialize();

        // updateStatus(ThingStatus.OFFLINE);

        try {
            if ((String) getConfig().get(ADDRESS) != null) {
                address = new IndividualAddress((String) getConfig().get(ADDRESS));

                double factor = (randomGenerator.nextFloat() * 2 - 1);
                long pollingInterval = Math.round(POLLING_INTERVAL * (1 + 0.25 * factor));

                if (pollingJob == null || pollingJob.isCancelled()) {
                    logger.trace("'{}' will be polled every {} ms", getThing().getUID(), pollingInterval);
                    pollingJob = scheduler.scheduleWithFixedDelay(pollingRunnable, Math.round(pollingInterval / 4),
                            pollingInterval, TimeUnit.MILLISECONDS);
                }
            } else {
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (Exception e) {
            logger.error("An exception occurred while setting the Individual Address : '{}'", e.getMessage());
        }

        if ((Boolean) getConfig().get(READ) && readAddresses.size() > 0) {
            if (readJob == null || readJob.isCancelled()) {
                BigDecimal readInterval = (BigDecimal) getConfig().get(INTERVAL);
                if (readInterval != null && readInterval.intValue() > 0) {
                    readJob = scheduler.scheduleWithFixedDelay(readRunnable, 0, readInterval.intValue(),
                            TimeUnit.SECONDS);
                } else {
                    readJob = scheduler.schedule(readRunnable, 0, TimeUnit.SECONDS);
                }
            }
        }
    }

    @Override
    public void dispose() {
        if (readJob != null && !readJob.isCancelled()) {
            readJob.cancel(true);
            readJob = null;
        }

        if (pollingJob != null && !pollingJob.isCancelled()) {
            pollingJob.cancel(true);
            pollingJob = null;
        }

        if (descriptionJob != null && !descriptionJob.isCancelled()) {
            descriptionJob.cancel(true);
            descriptionJob = null;
        }
    }

    @Override
    public void onBridgeConnected(KNXBridgeBaseThingHandler bridge) {
        initialize();
    }

    @Override
    public boolean listensTo(GroupAddress destination) {
        return groupAddresses.contains(destination) || foundGroupAddresses.contains(destination);
    }

    @Override
    public void handleUpdate(ChannelUID channelUID, State newState) {

        if (bridgeHandler == null) {
            logger.warn("KNX bridge handler not found. Cannot handle updates without bridge.");
            return;
        }

        String dpt = getDPT(channelUID, newState);
        String address = getAddress(channelUID, newState);
        Type type = getType(channelUID, newState);

        switch (channelUID.getId()) {
            case CHANNEL_RESET: {
                if (address != null) {
                    restart();
                }
            }
            default: {
                bridgeHandler.writeToKNX(address, dpt, type);
            }
        }

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (bridgeHandler == null) {
            logger.warn("KNX bridge handler not found. Cannot handle command without bridge.");
            return;
        }

        String dpt = getDPT(channelUID, command);
        String address = getAddress(channelUID, command);
        Type type = getType(channelUID, command);

        if (command instanceof RefreshType) {

            logger.debug("Refreshing channel {}", channelUID);

            if (dpt != null && address != null) {

                if (KNXCoreTypeMapper.toTypeClass(dpt) == null) {
                    logger.warn("DPT " + dpt + " is not supported by the KNX binding.");
                    return;
                }

                GroupAddress groupAddress;
                try {
                    groupAddress = new GroupAddress(address);
                    if (readAddresses.contains(groupAddress)) {
                        Datapoint datapoint = new CommandDP(groupAddress, getThing().getUID().toString(), 0, dpt);
                        bridgeHandler.readDatapoint(datapoint, bridgeHandler.getReadRetriesLimit());
                    }
                } catch (KNXFormatException e) {
                    logger.warn("The datapoint for group address '{}' with DPT '{}' could not be initialised", address,
                            dpt);
                }
            }
        } else {
            switch (channelUID.getId()) {
                case CHANNEL_RESET: {
                    if (address != null) {
                        restart();
                    }
                }
                default: {
                    bridgeHandler.writeToKNX(address, dpt, type);
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

        if (listensTo(destination)) {

            String dpt = getDPT(destination);
            if (dpt != null) {
                Type type = bridge.getType(destination, dpt, asdu);
                if (type != null) {
                    processDataReceived(destination, type);
                } else {
                    final char[] hexCode = "0123456789ABCDEF".toCharArray();
                    StringBuilder sb = new StringBuilder(2 + asdu.length * 2);
                    sb.append("0x");
                    for (byte b : asdu) {
                        sb.append(hexCode[(b >> 4) & 0xF]);
                        sb.append(hexCode[(b & 0xF)]);
                    }

                    logger.warn(
                            "Ignoring KNX bus data: couldn't transform to an openHAB type (not supported). Destination='{}', dpt='{}', data='{}'",
                            new Object[] { destination.toString(), dpt, sb.toString() });
                    return;
                }
            } else {
                logger.warn("Ignoring KNX bus data: no DPT is defined for group address '{}'", destination);
            }
        }
    }

    abstract public void processDataReceived(GroupAddress destination, Type state);

    abstract public String getDPT(GroupAddress destination);

    abstract public String getDPT(ChannelUID channelUID, Type command);

    abstract public String getAddress(ChannelUID channelUID, Type command);

    abstract public Type getType(ChannelUID channelUID, Type command);

    private Runnable readRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (getThing().getStatus() == ThingStatus.ONLINE) {
                    logger.trace("Reading the 'Read'able Group Addresses of {}", getThing().getUID());
                    readAddress();
                }
            } catch (Exception e) {
                logger.debug("An exception occurred while reading group addresses for Thing '{}' : {}",
                        getThing().getUID(), e);
            }
        }
    };

    protected void readAddress() {
        if (getThing().getStatus() == ThingStatus.ONLINE && readAddresses != null) {
            for (GroupAddress readAddress : readAddresses) {
                Datapoint datapoint = new CommandDP(readAddress, getThing().getUID().toString(), 0,
                        getDPT(readAddress));
                bridgeHandler.readDatapoint(datapoint, bridgeHandler.getReadRetriesLimit());
            }
        }
    }

    public void restart() {
        if (address != null) {
            bridgeHandler.restartNetworkDevice(address);
        }
    }

    private Runnable pollingRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                if (address != null) {
                    logger.debug("Polling the individual address {}", address.toString());
                    boolean isReachable = bridgeHandler.isReachable(address);
                    if (isReachable) {
                        updateStatus(ThingStatus.ONLINE);
                        if (!filledDescription) {
                            if (descriptionJob == null || descriptionJob.isCancelled()) {
                                descriptionJob = scheduler.schedule(descriptionRunnable, 0, TimeUnit.MILLISECONDS);
                            }
                        }
                    } else {
                        updateStatus(ThingStatus.OFFLINE);
                    }
                }
            } catch (Exception e) {
                logger.debug("An exception occurred while testing the reachability of a Thing '{}' : {}",
                        getThing().getUID(), e);
            }
        }
    };

    private Runnable descriptionRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                logger.debug("Fetching device information for address {}", address.toString());

                Thread.sleep(OPERATION_INTERVAL);
                byte[] data = bridgeHandler.readDeviceDescription(address, 0, false, OPERATION_TIMEOUT);

                if (data != null) {
                    final DD0 dd = DeviceDescriptor.DD0.fromType0(data);

                    Map<String, String> properties = editProperties();
                    properties.put(FIRMWARE_TYPE, Firmware.getName(dd.getFirmwareType()));
                    properties.put(FIRMWARE_VERSION, Firmware.getName(dd.getFirmwareVersion()));
                    properties.put(FIRMWARE_SUBVERSION, Firmware.getName(dd.getSubcode()));
                    try {
                        updateProperties(properties);
                    } catch (Exception e) {
                        // TODO : ignore for now, but for Things created through the DSL, this should also NOT throw an
                        // exception! See forum discussions
                    }
                    logger.info("The device with address {} is of type {}, version {}, subversion {}",
                            new Object[] { address, Firmware.getName(dd.getFirmwareType()),
                                    Firmware.getName(dd.getFirmwareVersion()), Firmware.getName(dd.getSubcode()) });
                } else {
                    logger.warn("The KNX Actor with address {} does not expose a Device Descriptor");
                }

                // check if there is a Device Object in the KNX Actor
                Thread.sleep(OPERATION_INTERVAL);
                byte[] elements = bridgeHandler.readDeviceProperties(address, DEVICE_OBJECT, PID.OBJECT_TYPE, 0, 1,
                        false, OPERATION_TIMEOUT);
                if ((elements == null ? 0 : toUnsigned(elements)) == 1) {

                    Thread.sleep(OPERATION_INTERVAL);
                    String ManufacturerID = Manufacturer.getName(toUnsigned(bridgeHandler.readDeviceProperties(address,
                            DEVICE_OBJECT, PID.MANUFACTURER_ID, 1, 1, false, OPERATION_TIMEOUT)));
                    Thread.sleep(OPERATION_INTERVAL);
                    String serialNo = DataUnitBuilder.toHex(bridgeHandler.readDeviceProperties(address, DEVICE_OBJECT,
                            PID.SERIAL_NUMBER, 1, 1, false, OPERATION_TIMEOUT), "");
                    Thread.sleep(OPERATION_INTERVAL);
                    String hardwareType = DataUnitBuilder.toHex(bridgeHandler.readDeviceProperties(address,
                            DEVICE_OBJECT, HARDWARE_TYPE, 1, 1, false, OPERATION_TIMEOUT), " ");
                    Thread.sleep(OPERATION_INTERVAL);
                    String firmwareRevision = Integer.toString(toUnsigned(bridgeHandler.readDeviceProperties(address,
                            DEVICE_OBJECT, PID.FIRMWARE_REVISION, 1, 1, false, OPERATION_TIMEOUT)));

                    Map<String, String> properties = editProperties();
                    properties.put(MANUFACTURER_NAME, ManufacturerID);
                    properties.put(MANUFACTURER_SERIAL_NO, serialNo);
                    properties.put(MANUFACTURER_HARDWARE_TYPE, hardwareType);
                    properties.put(MANUFACTURER_FIRMWARE_REVISION, firmwareRevision);
                    try {
                        updateProperties(properties);
                    } catch (Exception e) {
                        // TODO : ignore for now, but for Things created through the DSL, this should also NOT throw an
                        // exception! See forum discussions
                    }
                    logger.info("Identified device {} as a {}, type {}, revision {}, serial number {}",
                            new Object[] { address, ManufacturerID, hardwareType, firmwareRevision, serialNo });
                } else {
                    logger.warn("The KNX Actor with address {} does not expose a Device Object", address);
                }

                // TODO : According to the KNX specs, devices should expose the PID.IO_LIST property in the DEVICE
                // object, but it seems that a lot, if not all, devices do not do this. In this list we can find out
                // what other kind of objects the device is exposing. Most devices do implement some set of objects, we
                // will just go ahead and try to read them out irrespective of what is in the IO_LIST

                Thread.sleep(OPERATION_INTERVAL);
                byte[] tableaddress = bridgeHandler.readDeviceProperties(address, ADDRESS_TABLE_OBJECT,
                        PID.TABLE_REFERENCE, 1, 1, false, OPERATION_TIMEOUT);

                if (tableaddress != null) {
                    Thread.sleep(OPERATION_INTERVAL);
                    elements = bridgeHandler.readDeviceMemory(address, toUnsigned(tableaddress), 1, false,
                            OPERATION_TIMEOUT);
                    if (elements != null) {
                        int numberOfElements = toUnsigned(elements);
                        logger.debug("The KNX Actor with address {} uses {} group addresses", address,
                                numberOfElements - 1);

                        byte[] addressData = null;
                        while (addressData == null) {
                            Thread.sleep(OPERATION_INTERVAL);
                            addressData = bridgeHandler.readDeviceMemory(address, toUnsigned(tableaddress) + 1, 2,
                                    false, OPERATION_TIMEOUT);
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
                                addressData = bridgeHandler.readDeviceMemory(address,
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

                Thread.sleep(OPERATION_INTERVAL);
                byte[] objecttableaddress = bridgeHandler.readDeviceProperties(address, GROUPOBJECT_OBJECT,
                        PID.TABLE_REFERENCE, 1, 1, true, OPERATION_TIMEOUT);

                if (objecttableaddress != null) {
                    Thread.sleep(OPERATION_INTERVAL);
                    elements = bridgeHandler.readDeviceMemory(address, toUnsigned(objecttableaddress), 1, false,
                            OPERATION_TIMEOUT);
                    if (elements != null) {
                        int numberOfElements = toUnsigned(elements);
                        logger.debug("The KNX Actor with address {} has {} objects", address, numberOfElements);

                        for (int i = 1; i < numberOfElements; i++) {
                            byte[] objectData = null;
                            while (objectData == null) {
                                Thread.sleep(OPERATION_INTERVAL);
                                objectData = bridgeHandler.readDeviceMemory(address,
                                        toUnsigned(objecttableaddress) + 1 + (i * 3), 3, false, OPERATION_TIMEOUT);

                                logger.debug("Byte 1 {}", String
                                        .format("%8s", Integer.toBinaryString(objectData[0] & 0xFF)).replace(' ', '0'));
                                logger.debug("Byte 2 {}", String
                                        .format("%8s", Integer.toBinaryString(objectData[1] & 0xFF)).replace(' ', '0'));
                                logger.debug("Byte 3 {}", String
                                        .format("%8s", Integer.toBinaryString(objectData[2] & 0xFF)).replace(' ', '0'));
                            }
                        }
                    }
                } else {
                    logger.warn("The KNX Actor with address {} does not expose a Group Object table", address);
                }

                filledDescription = true;

            } catch (Exception e) {
                logger.debug("An exception occurred while fetching the device description for a Thing '{}' : {}",
                        getThing().getUID(), e);
                e.printStackTrace();
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
        L0(Integer.valueOf(0), "Unloaded"),
        L1(Integer.valueOf(1), "Loaded"),
        L2(Integer.valueOf(2), "Loading"),
        L3(Integer.valueOf(3), "Error"),
        L4(Integer.valueOf(4), "Unloading"),
        L5(Integer.valueOf(5), "Load Completing");

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
        F0(Integer.valueOf(0), "BCU 1, BCU 2, BIM M113"),
        F1(Integer.valueOf(1), "Unidirectional devices"),
        F3(Integer.valueOf(3), "Property based device management"),
        F7(Integer.valueOf(7), "BIM M112"),
        F8(Integer.valueOf(8), "IR Decoder, TP1 legacy"),
        F9(Integer.valueOf(9), "Repeater, Coupler");

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
        M1(Integer.valueOf(1), "Siemens"),
        M2(Integer.valueOf(2), "ABB"),
        M4(Integer.valueOf(4), "Albrecht Jung"),
        M5(Integer.valueOf(5), "Bticino"),
        M6(Integer.valueOf(6), "Berker"),
        M7(Integer.valueOf(7), "Busch-Jaeger Elektro"),
        M8(Integer.valueOf(8), "GIRA Giersiepen"),
        M9(Integer.valueOf(9), "Hager Electro"),
        M10(Integer.valueOf(10), "INSTA ELEKTRO"),
        M11(Integer.valueOf(11), "LEGRAND Appareillage électrique"),
        M12(Integer.valueOf(12), "Merten"),
        M14(Integer.valueOf(14), "ABB SpA – SACE Division"),
        M22(Integer.valueOf(22), "Siedle & Söhne"),
        M24(Integer.valueOf(24), "Eberle"),
        M25(Integer.valueOf(25), "GEWISS"),
        M27(Integer.valueOf(27), "Albert Ackermann"),
        M28(Integer.valueOf(28), "Schupa GmbH"),
        M29(Integer.valueOf(29), "ABB SCHWEIZ"),
        M30(Integer.valueOf(30), "Feller"),
        M32(Integer.valueOf(32), "DEHN & SÖHNE"),
        M33(Integer.valueOf(33), "CRABTREE"),
        M36(Integer.valueOf(36), "Paul Hochköpper"),
        M37(Integer.valueOf(37), "Altenburger Electronic"),
        M41(Integer.valueOf(41), "Grässlin"),
        M42(Integer.valueOf(42), "Simon"),
        M44(Integer.valueOf(44), "VIMAR"),
        M45(Integer.valueOf(45), "Moeller Gebäudeautomation KG"),
        M46(Integer.valueOf(46), "Eltako"),
        M49(Integer.valueOf(49), "Bosch-Siemens Haushaltsgeräte"),
        M52(Integer.valueOf(52), "RITTO GmbH&Co.KG"),
        M53(Integer.valueOf(53), "Power Controls"),
        M55(Integer.valueOf(55), "ZUMTOBEL"),
        M57(Integer.valueOf(57), "Phoenix Contact"),
        M61(Integer.valueOf(61), "WAGO Kontakttechnik"),
        M66(Integer.valueOf(66), "Wieland Electric"),
        M67(Integer.valueOf(67), "Hermann Kleinhuis"),
        M69(Integer.valueOf(69), "Stiebel Eltron"),
        M71(Integer.valueOf(71), "Tehalit"),
        M72(Integer.valueOf(72), "Theben AG"),
        M73(Integer.valueOf(73), "Wilhelm Rutenbeck"),
        M75(Integer.valueOf(75), "Winkhaus"),
        M76(Integer.valueOf(76), "Robert Bosch"),
        M78(Integer.valueOf(78), "Somfy"),
        M80(Integer.valueOf(80), "Woertz"),
        M81(Integer.valueOf(81), "Viessmann Werke"),
        M82(Integer.valueOf(82), "Theodor HEIMEIER Metallwerk"),
        M83(Integer.valueOf(83), "Joh. Vaillant"),
        M85(Integer.valueOf(85), "AMP Deutschland"),
        M89(Integer.valueOf(89), "Bosch Thermotechnik GmbH"),
        M90(Integer.valueOf(90), "SEF - ECOTEC"),
        M92(Integer.valueOf(92), "DORMA GmbH + Co. KG"),
        M93(Integer.valueOf(93), "WindowMaster A/S"),
        M94(Integer.valueOf(94), "Walther Werke"),
        M95(Integer.valueOf(95), "ORAS"),
        M97(Integer.valueOf(97), "Dätwyler"),
        M98(Integer.valueOf(98), "Electrak"),
        M99(Integer.valueOf(99), "Techem"),
        M100(Integer.valueOf(100), "Schneider Electric Industries SAS"),
        M101(Integer.valueOf(101), "WHD Wilhelm Huber + Söhne"),
        M102(Integer.valueOf(102), "Bischoff Elektronik"),
        M104(Integer.valueOf(104), "JEPAZ"),
        M105(Integer.valueOf(105), "RTS Automation"),
        M106(Integer.valueOf(106), "EIBMARKT GmbH"),
        M107(Integer.valueOf(107), "WAREMA electronic GmbH"),
        M108(Integer.valueOf(108), "Eelectron"),
        M109(Integer.valueOf(109), "Belden Wire & Cable B.V."),
        M110(Integer.valueOf(110), "Becker-Antriebe GmbH"),
        M111(Integer.valueOf(111), "J.Stehle+Söhne GmbH"),
        M112(Integer.valueOf(112), "AGFEO"),
        M113(Integer.valueOf(113), "Zennio"),
        M114(Integer.valueOf(114), "TAPKO Technologies"),
        M115(Integer.valueOf(115), "HDL"),
        M116(Integer.valueOf(116), "Uponor"),
        M117(Integer.valueOf(117), "se Lightmanagement AG"),
        M118(Integer.valueOf(118), "Arcus-eds"),
        M119(Integer.valueOf(119), "Intesis"),
        M120(Integer.valueOf(120), "Herholdt Controls srl"),
        M121(Integer.valueOf(121), "Zublin AG"),
        M122(Integer.valueOf(122), "Durable Technologies"),
        M123(Integer.valueOf(123), "Innoteam"),
        M124(Integer.valueOf(124), "ise GmbH"),
        M125(Integer.valueOf(125), "TEAM FOR TRONICS"),
        M126(Integer.valueOf(126), "CIAT"),
        M127(Integer.valueOf(127), "Remeha BV"),
        M128(Integer.valueOf(128), "ESYLUX"),
        M129(Integer.valueOf(129), "BASALTE"),
        M130(Integer.valueOf(130), "Vestamatic"),
        M131(Integer.valueOf(131), "MDT technologies"),
        M132(Integer.valueOf(132), "Warendorfer Küchen GmbH"),
        M133(Integer.valueOf(133), "Video-Star"),
        M134(Integer.valueOf(134), "Sitek"),
        M135(Integer.valueOf(135), "CONTROLtronic"),
        M136(Integer.valueOf(136), "function Technology"),
        M137(Integer.valueOf(137), "AMX"),
        M138(Integer.valueOf(138), "ELDAT"),
        M139(Integer.valueOf(139), "VIKO"),
        M140(Integer.valueOf(140), "Pulse Technologies"),
        M141(Integer.valueOf(141), "Crestron"),
        M142(Integer.valueOf(142), "STEINEL professional"),
        M143(Integer.valueOf(143), "BILTON LED Lighting"),
        M144(Integer.valueOf(144), "denro AG"),
        M145(Integer.valueOf(145), "GePro"),
        M146(Integer.valueOf(146), "preussen automation"),
        M147(Integer.valueOf(147), "Zoppas Industries"),
        M148(Integer.valueOf(148), "MACTECH"),
        M149(Integer.valueOf(149), "TECHNO-TREND"),
        M150(Integer.valueOf(150), "FS Cables"),
        M151(Integer.valueOf(151), "Delta Dore"),
        M152(Integer.valueOf(152), "Eissound"),
        M153(Integer.valueOf(153), "Cisco"),
        M154(Integer.valueOf(154), "Dinuy"),
        M155(Integer.valueOf(155), "iKNiX"),
        M156(Integer.valueOf(156), "Rademacher Geräte-Elektronik GmbH & Co. KG"),
        M157(Integer.valueOf(157), "EGi Electroacustica General Iberica"),
        M158(Integer.valueOf(158), "Ingenium"),
        M159(Integer.valueOf(159), "ElabNET"),
        M160(Integer.valueOf(160), "Blumotix"),
        M161(Integer.valueOf(161), "Hunter Douglas"),
        M162(Integer.valueOf(162), "APRICUM"),
        M163(Integer.valueOf(163), "TIANSU Automation"),
        M164(Integer.valueOf(164), "Bubendorff"),
        M165(Integer.valueOf(165), "MBS GmbH"),
        M166(Integer.valueOf(166), "Enertex Bayern GmbH"),
        M167(Integer.valueOf(167), "BMS"),
        M168(Integer.valueOf(168), "Sinapsi"),
        M169(Integer.valueOf(169), "Embedded Systems SIA"),
        M170(Integer.valueOf(170), "KNX1"),
        M171(Integer.valueOf(171), "Tokka"),
        M172(Integer.valueOf(172), "NanoSense"),
        M173(Integer.valueOf(173), "PEAR Automation GmbH"),
        M174(Integer.valueOf(174), "DGA"),
        M175(Integer.valueOf(175), "Lutron"),
        M176(Integer.valueOf(176), "AIRZONE – ALTRA"),
        M177(Integer.valueOf(177), "Lithoss Design Switches"),
        M178(Integer.valueOf(178), "3ATEL"),
        M179(Integer.valueOf(179), "Philips Controls"),
        M180(Integer.valueOf(180), "VELUX A/S"),
        M181(Integer.valueOf(181), "LOYTEC"),
        M182(Integer.valueOf(182), "SBS S.p.A."),
        M183(Integer.valueOf(183), "SIRLAN Technologies"),
        M184(Integer.valueOf(184), "Bleu Comm' Azur"),
        M185(Integer.valueOf(185), "IT GmbH"),
        M186(Integer.valueOf(186), "RENSON"),
        M187(Integer.valueOf(187), "HEP Group"),
        M188(Integer.valueOf(188), "Balmart"),
        M189(Integer.valueOf(189), "GFS GmbH"),
        M190(Integer.valueOf(190), "Schenker Storen AG"),
        M191(Integer.valueOf(191), "Algodue Elettronica S.r.L."),
        M192(Integer.valueOf(192), "Newron System"),
        M193(Integer.valueOf(193), "maintronic"),
        M194(Integer.valueOf(194), "Vantage"),
        M195(Integer.valueOf(195), "Foresis"),
        M196(Integer.valueOf(196), "Research & Production Association SEM"),
        M197(Integer.valueOf(197), "Weinzierl Engineering GmbH"),
        M198(Integer.valueOf(198), "Möhlenhoff Wärmetechnik GmbH"),
        M199(Integer.valueOf(199), "PKC-GROUP Oyj"),
        M200(Integer.valueOf(200), "B.E.G."),
        M201(Integer.valueOf(201), "Elsner Elektronik GmbH"),
        M202(Integer.valueOf(202), "Siemens Building Technologies (HK/China) Ltd."),
        M204(Integer.valueOf(204), "Eutrac"),
        M205(Integer.valueOf(205), "Gustav Hensel GmbH & Co. KG"),
        M206(Integer.valueOf(206), "GARO AB"),
        M207(Integer.valueOf(207), "Waldmann Lichttechnik"),
        M208(Integer.valueOf(208), "SCHÜCO"),
        M209(Integer.valueOf(209), "EMU"),
        M210(Integer.valueOf(210), "JNet Systems AG"),
        M214(Integer.valueOf(214), "O.Y.L. Electronics"),
        M215(Integer.valueOf(215), "Galax System"),
        M216(Integer.valueOf(216), "Disch"),
        M217(Integer.valueOf(217), "Aucoteam"),
        M218(Integer.valueOf(218), "Luxmate Controls"),
        M219(Integer.valueOf(219), "Danfoss"),
        M220(Integer.valueOf(220), "AST GmbH"),
        M222(Integer.valueOf(222), "WILA Leuchten"),
        M223(Integer.valueOf(223), "b+b Automations- und Steuerungstechnik"),
        M225(Integer.valueOf(225), "Lingg & Janke"),
        M227(Integer.valueOf(227), "Sauter"),
        M228(Integer.valueOf(228), "SIMU"),
        M232(Integer.valueOf(232), "Theben HTS AG"),
        M233(Integer.valueOf(233), "Amann GmbH"),
        M234(Integer.valueOf(234), "BERG Energiekontrollsysteme GmbH"),
        M235(Integer.valueOf(235), "Hüppe Form Sonnenschutzsysteme GmbH"),
        M237(Integer.valueOf(237), "Oventrop KG"),
        M238(Integer.valueOf(238), "Griesser AG"),
        M239(Integer.valueOf(239), "IPAS GmbH"),
        M240(Integer.valueOf(240), "elero GmbH"),
        M241(Integer.valueOf(241), "Ardan Production and Industrial Controls Ltd."),
        M242(Integer.valueOf(242), "Metec Meßtechnik GmbH"),
        M244(Integer.valueOf(244), "ELKA-Elektronik GmbH"),
        M245(Integer.valueOf(245), "ELEKTROANLAGEN D. NAGEL"),
        M246(Integer.valueOf(246), "Tridonic Bauelemente GmbH"),
        M248(Integer.valueOf(248), "Stengler Gesellschaft"),
        M249(Integer.valueOf(249), "Schneider Electric (MG)"),
        M250(Integer.valueOf(250), "KNX Association"),
        M251(Integer.valueOf(251), "VIVO"),
        M252(Integer.valueOf(252), "Hugo Müller GmbH & Co KG"),
        M253(Integer.valueOf(253), "Siemens HVAC"),
        M254(Integer.valueOf(254), "APT"),
        M256(Integer.valueOf(256), "HighDom"),
        M257(Integer.valueOf(257), "Top Services"),
        M258(Integer.valueOf(258), "ambiHome"),
        M259(Integer.valueOf(259), "DATEC electronic AG"),
        M260(Integer.valueOf(260), "ABUS Security-Center"),
        M261(Integer.valueOf(261), "Lite-Puter"),
        M262(Integer.valueOf(262), "Tantron Electronic"),
        M263(Integer.valueOf(263), "Yönnet"),
        M264(Integer.valueOf(264), "DKX Tech"),
        M265(Integer.valueOf(265), "Viatron"),
        M266(Integer.valueOf(266), "Nautibus"),
        M268(Integer.valueOf(268), "Longchuang"),
        M269(Integer.valueOf(269), "Air-On AG"),
        M270(Integer.valueOf(270), "ib-company GmbH"),
        M271(Integer.valueOf(271), "SATION"),
        M272(Integer.valueOf(272), "Agentilo GmbH"),
        M273(Integer.valueOf(273), "Makel Elektrik"),
        M274(Integer.valueOf(274), "Helios Ventilatoren"),
        M275(Integer.valueOf(275), "Otto Solutions Pte Ltd"),
        M276(Integer.valueOf(276), "Airmaster"),
        M277(Integer.valueOf(277), "HEINEMANN GmbH"),
        M278(Integer.valueOf(278), "LDS"),
        M279(Integer.valueOf(279), "ASIN"),
        M280(Integer.valueOf(280), "Bridges"),
        M281(Integer.valueOf(281), "ARBONIA"),
        M282(Integer.valueOf(282), "KERMI"),
        M283(Integer.valueOf(283), "PROLUX"),
        M284(Integer.valueOf(284), "ClicHome"),
        M285(Integer.valueOf(285), "COMMAX"),
        M286(Integer.valueOf(286), "EAE"),
        M287(Integer.valueOf(287), "Tense"),
        M288(Integer.valueOf(288), "Seyoung Electronics"),
        M289(Integer.valueOf(289), "Lifedomus"),
        M290(Integer.valueOf(290), "EUROtronic Technology GmbH"),
        M291(Integer.valueOf(291), "tci"),
        M292(Integer.valueOf(292), "Rishun Electronic"),
        M293(Integer.valueOf(293), "Zipato"),
        M294(Integer.valueOf(294), "cm-security GmbH & Co KG"),
        M295(Integer.valueOf(295), "Qing Cables"),
        M296(Integer.valueOf(296), "LABIO"),
        M297(Integer.valueOf(297), "Coster Tecnologie Elettroniche S.p.A."),
        M298(Integer.valueOf(298), "E.G.E"),
        M299(Integer.valueOf(299), "NETxAutomation"),
        M300(Integer.valueOf(300), "tecalor"),
        M301(Integer.valueOf(301), "Urmet Electronics (Huizhou) Ltd."),
        M302(Integer.valueOf(302), "Peiying Building Control"),
        M303(Integer.valueOf(303), "BPT S.p.A. a Socio Unico"),
        M304(Integer.valueOf(304), "Kanontec - KanonBUS"),
        M305(Integer.valueOf(305), "ISER Tech"),
        M306(Integer.valueOf(306), "Fineline"),
        M307(Integer.valueOf(307), "CP Electronics Ltd"),
        M308(Integer.valueOf(308), "Servodan A/S"),
        M309(Integer.valueOf(309), "Simon"),
        M310(Integer.valueOf(310), "GM modular pvt. Ltd."),
        M311(Integer.valueOf(311), "FU CHENG Intelligence"),
        M312(Integer.valueOf(312), "NexKon"),
        M313(Integer.valueOf(313), "FEEL s.r.l"),
        M314(Integer.valueOf(314), "Not Assigned"),
        M315(Integer.valueOf(315), "Shenzhen Fanhai Sanjiang Electronics Co., Ltd."),
        M316(Integer.valueOf(316), "Jiuzhou Greeble"),
        M317(Integer.valueOf(317), "Aumüller Aumatic GmbH"),
        M318(Integer.valueOf(318), "Etman Electric"),
        M319(Integer.valueOf(319), "EMT Controls"),
        M320(Integer.valueOf(320), "ZidaTech AG"),
        M321(Integer.valueOf(321), "IDGS bvba"),
        M322(Integer.valueOf(322), "dakanimo"),
        M323(Integer.valueOf(323), "Trebor Automation AB"),
        M324(Integer.valueOf(324), "Satel sp. z o.o."),
        M325(Integer.valueOf(325), "Russound, Inc."),
        M326(Integer.valueOf(326), "Midea Heating & Ventilating Equipment CO LTD"),
        M327(Integer.valueOf(327), "Consorzio Terranuova"),
        M328(Integer.valueOf(328), "Wolf Heiztechnik GmbH"),
        M329(Integer.valueOf(329), "SONTEC"),
        M330(Integer.valueOf(330), "Belcom Cables Ltd."),
        M331(Integer.valueOf(331), "Guangzhou SeaWin Electrical Technologies Co., Ltd."),
        M332(Integer.valueOf(332), "Acrel"),
        M333(Integer.valueOf(333), "Franke Aquarotter GmbH"),
        M334(Integer.valueOf(334), "Orion Systems"),
        M335(Integer.valueOf(335), "Schrack Technik GmbH"),
        M336(Integer.valueOf(336), "INSPRID"),
        M337(Integer.valueOf(337), "Sunricher"),
        M338(Integer.valueOf(338), "Menred automation system(shanghai) Co.,Ltd."),
        M339(Integer.valueOf(339), "Aurex"),
        M340(Integer.valueOf(340), "Josef Barthelme GmbH & Co. KG"),
        M341(Integer.valueOf(341), "Architecture Numerique"),
        M342(Integer.valueOf(342), "UP GROUP"),
        M343(Integer.valueOf(343), "Teknos-Avinno"),
        M344(Integer.valueOf(344), "Ningbo Dooya Mechanic & Electronic Technology"),
        M345(Integer.valueOf(345), "Thermokon Sensortechnik GmbH"),
        M346(Integer.valueOf(346), "BELIMO Automation AG"),
        M347(Integer.valueOf(347), "Zehnder Group International AG"),
        M348(Integer.valueOf(348), "sks Kinkel Elektronik"),
        M349(Integer.valueOf(349), "ECE Wurmitzer GmbH"),
        M350(Integer.valueOf(350), "LARS"),
        M351(Integer.valueOf(351), "URC"),
        M352(Integer.valueOf(352), "LightControl"),
        M353(Integer.valueOf(353), "ShenZhen YM"),
        M354(Integer.valueOf(354), "MEAN WELL Enterprises Co. Ltd."),
        M355(Integer.valueOf(355), "OSix"),
        M356(Integer.valueOf(356), "AYPRO Technology"),
        M357(Integer.valueOf(357), "Hefei Ecolite Software"),
        M358(Integer.valueOf(358), "Enno"),
        M359(Integer.valueOf(359), "Ohosure");

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
