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
package org.openhab.binding.insteon.internal.command;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.InsteonBinding;
import org.openhab.binding.insteon.internal.InsteonBindingConstants;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.device.InsteonDevice;
import org.openhab.binding.insteon.internal.device.InsteonEngine;
import org.openhab.binding.insteon.internal.device.InsteonScene;
import org.openhab.binding.insteon.internal.device.X10;
import org.openhab.binding.insteon.internal.driver.PortListener;
import org.openhab.binding.insteon.internal.handler.InsteonBridgeHandler;
import org.openhab.binding.insteon.internal.message.FieldException;
import org.openhab.binding.insteon.internal.message.InvalidMessageTypeException;
import org.openhab.binding.insteon.internal.message.Msg;
import org.openhab.binding.insteon.internal.utils.ByteUtils;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Console commands for the Insteon binding
 *
 * @author Rob Nielsen - Initial contribution
 * @author Jeremy Setton - Improvements for openHAB 3 insteon binding
 */
@NonNullByDefault
@Component(service = ConsoleCommandExtension.class)
public class InsteonCommandExtension extends AbstractConsoleCommandExtension implements PortListener {
    private static final String LIST_DEVICES = "listDevices";
    private static final String LIST_SCENES = "listScenes";
    private static final String LIST_CHANNELS = "listChannels";
    private static final String LIST_MODEM_DATABASE = "listModemDatabase";
    private static final String LIST_DEVICE_DATABASE = "listDeviceDatabase";
    private static final String LIST_DEVICE_PRODUCT_DATA = "listDeviceProductData";
    private static final String LIST_MONITORED = "listMonitored";
    private static final String LINK_DEVICE = "linkDevice";
    private static final String UNLINK_DEVICE = "unlinkDevice";
    private static final String REFRESH_DEVICE = "refreshDevice";
    private static final String START_MONITORING = "startMonitoring";
    private static final String STOP_MONITORING = "stopMonitoring";
    private static final String SEND_BROADCAST_MESSAGE = "sendBroadcastMessage";
    private static final String SEND_STANDARD_MESSAGE = "sendStandardMessage";
    private static final String SEND_EXTENDED_MESSAGE = "sendExtendedMessage";
    private static final String SEND_EXTENDED_MESSAGE_2 = "sendExtendedMessage2";
    private static final String SEND_X10_MESSAGE = "sendX10Message";
    private static final String SEND_IM_MESSAGE = "sendIMMessage";
    private static final String SWITCH_MODEM = "switchModem";

    private static final String MSG_EVENTS_FILE_PREFIX = "messageEvents";

    private final Logger logger = LoggerFactory.getLogger(InsteonCommandExtension.class);

    private enum MessageType {
        STANDARD,
        EXTENDED,
        EXTENDED_2
    };

    private final ThingRegistry thingRegistry;

    private @Nullable InsteonBridgeHandler handler;
    private boolean monitoring = false;
    private boolean monitorAllDevices = false;
    private Set<InsteonAddress> monitoredAddresses = new HashSet<>();

    @Activate
    public InsteonCommandExtension(final @Reference ThingRegistry thingRegistry) {
        super("insteon", "Interact with the Insteon integration.");
        this.thingRegistry = thingRegistry;
    }

    @Override
    public void execute(String[] args, Console console) {
        InsteonBridgeHandler handler = getBridgeHandler();
        if (handler == null) {
            console.println("No Insteon bridge configured or enabled.");
            return;
        }

        if (args.length > 0) {
            switch (args[0]) {
                case LIST_DEVICES:
                    if (args.length == 1) {
                        listDevices(console);
                    } else {
                        printUsage(console);
                    }
                    break;
                case LIST_SCENES:
                    if (args.length == 1) {
                        listScenes(console);
                    } else {
                        printUsage(console);
                    }
                    break;
                case LIST_CHANNELS:
                    if (args.length >= 1 && args.length <= 2) {
                        listChannels(console, args.length == 1 ? null : args[1]);
                    } else {
                        printUsage(console);
                    }
                    break;
                case LIST_MODEM_DATABASE:
                    if (args.length == 1) {
                        listModemDatabase(console);
                    } else {
                        printUsage(console);
                    }
                    break;
                case LIST_DEVICE_DATABASE:
                    if (args.length == 2) {
                        listDeviceDatabase(console, args[1]);
                    } else {
                        printUsage(console);
                    }
                    break;
                case LIST_DEVICE_PRODUCT_DATA:
                    if (args.length == 2) {
                        listDeviceProductData(console, args[1]);
                    } else {
                        printUsage(console);
                    }
                    break;
                case LIST_MONITORED:
                    if (args.length == 1) {
                        listMonitoredDevices(console);
                    } else {
                        printUsage(console);
                    }
                    break;
                case LINK_DEVICE:
                    if (args.length >= 1 && args.length <= 2) {
                        linkDevice(console, args.length == 1 ? null : args[1]);
                    } else {
                        printUsage(console);
                    }
                    break;
                case UNLINK_DEVICE:
                    if (args.length == 2) {
                        unlinkDevice(console, args[1]);
                    } else {
                        printUsage(console);
                    }
                    break;
                case REFRESH_DEVICE:
                    if (args.length == 2) {
                        refreshDevice(console, args[1]);
                    } else {
                        printUsage(console);
                    }
                    break;
                case START_MONITORING:
                    if (args.length == 2) {
                        startMonitoring(console, args[1]);
                    } else {
                        printUsage(console);
                    }
                    break;
                case STOP_MONITORING:
                    if (args.length == 2) {
                        stopMonitoring(console, args[1]);
                    } else {
                        printUsage(console);
                    }
                    break;
                case SEND_BROADCAST_MESSAGE:
                    if (args.length == 4) {
                        sendBroadcastMessage(console, args);
                    } else {
                        printUsage(console);
                    }
                    break;
                case SEND_STANDARD_MESSAGE:
                    if (args.length == 4) {
                        sendDirectMessage(console, MessageType.STANDARD, args);
                    } else {
                        printUsage(console);
                    }
                    break;
                case SEND_EXTENDED_MESSAGE:
                    if (args.length >= 4 && args.length <= 17) {
                        sendDirectMessage(console, MessageType.EXTENDED, args);
                    } else {
                        printUsage(console);
                    }
                    break;
                case SEND_EXTENDED_MESSAGE_2:
                    if (args.length >= 4 && args.length <= 16) {
                        sendDirectMessage(console, MessageType.EXTENDED_2, args);
                    } else {
                        printUsage(console);
                    }
                    break;
                case SEND_X10_MESSAGE:
                    if (args.length == 3) {
                        sendX10Message(console, args);
                    } else {
                        printUsage(console);
                    }
                    break;
                case SEND_IM_MESSAGE:
                    if (args.length >= 2) {
                        sendIMMessage(console, args);
                    } else {
                        printUsage(console);
                    }
                    break;
                case SWITCH_MODEM:
                    if (args.length == 2) {
                        setBridgeHandler(console, args[1]);
                    } else {
                        printUsage(console);
                    }
                    break;
                default:
                    console.println("Unknown command '" + args[0] + "'");
                    printUsage(console);
                    break;
            }
        } else {
            printUsage(console);
        }
    }

    @Override
    public List<String> getUsages() {
        return Arrays.asList(new String[] {
                buildCommandUsage(LIST_DEVICES, "list configured Insteon/X10 devices with related channels and status"),
                buildCommandUsage(LIST_SCENES, "list configured Insteon scenes with related channels and status"),
                buildCommandUsage(LIST_CHANNELS + " [<thingId>]",
                        "list available channel ids with configuration and link state, optionally limiting to a thing"),
                buildCommandUsage(LIST_MODEM_DATABASE, "list Insteon PLM or hub database details"),
                buildCommandUsage(LIST_DEVICE_DATABASE + " <address>", "list a device all-link database records"),
                buildCommandUsage(LIST_DEVICE_PRODUCT_DATA + " <address>", "list a device product data"),
                buildCommandUsage(LIST_MONITORED, "list monitored device(s)"),
                buildCommandUsage(LINK_DEVICE + " [<address>]",
                        "link a device to the modem, optionally providing its address"),
                buildCommandUsage(UNLINK_DEVICE + " <address>", "unlink a device from the modem"),
                buildCommandUsage(REFRESH_DEVICE + " <address>", "refresh a device"),
                buildCommandUsage(START_MONITORING + " all|<address>",
                        "start logging message events for device(s) in separate file(s)"),
                buildCommandUsage(STOP_MONITORING + " all|<address>",
                        "stop logging message events for device(s) in separate file(s)"),
                buildCommandUsage(SEND_BROADCAST_MESSAGE + " <group> <cmd1> <cmd2>",
                        "send a broadcast message to a group"),
                buildCommandUsage(SEND_STANDARD_MESSAGE + " <address> <cmd1> <cmd2>",
                        "send a standard message to a device"),
                buildCommandUsage(SEND_EXTENDED_MESSAGE + " <address> <cmd1> <cmd2> [<data1> ... <data13>]",
                        "send an extended message with standard crc to a device"),
                buildCommandUsage(SEND_EXTENDED_MESSAGE_2 + " <address> <cmd1> <cmd2> [<data1> ... <data12>]",
                        "send an extended message with a two-byte crc to a device"),
                buildCommandUsage(SEND_X10_MESSAGE + " <address> <cmd>", "send an X10 message to a device"),
                buildCommandUsage(SEND_IM_MESSAGE + " <name> [<data1> <data2> ...]", "send an IM message to the modem"),
                buildCommandUsage(SWITCH_MODEM + " <address>",
                        "switch modem bridge to use if more than one configured and enabled") });
    }

    @Override
    public void disconnected() {
        // do nothing
    }

    @Override
    public void messageReceived(Msg msg) {
        InsteonAddress address = msg.getAddressOrNull(msg.isReply() ? "toAddress" : "fromAddress");
        if (address != null) {
            if (monitorAllDevices || monitoredAddresses.contains(address)) {
                logMessageEvent(address, msg);
            }
        }
    }

    @Override
    public void messageSent(Msg msg) {
        InsteonAddress address = msg.getAddressOrNull("toAddress");
        if (address != null) {
            if (monitorAllDevices || monitoredAddresses.contains(address)) {
                logMessageEvent(address, msg);
            }
        }
    }

    private String getMsgEventsFileName(String address) {
        return MSG_EVENTS_FILE_PREFIX + "-" + address.replace(".", "") + ".log";
    }

    private String getMsgEventsFilePath(String address) {
        return InsteonBindingConstants.BINDING_DATA_DIR + File.separator + getMsgEventsFileName(address);
    }

    private void clearMonitorFiles(String address) {
        File folder = new File(InsteonBindingConstants.BINDING_DATA_DIR);
        String prefix = "all".equalsIgnoreCase(address) ? MSG_EVENTS_FILE_PREFIX : getMsgEventsFileName(address);

        if (folder.isDirectory()) {
            Arrays.asList(folder.listFiles()).stream().filter(file -> file.getName().startsWith(prefix))
                    .forEach(File::delete);
        }
    }

    private void logMessageEvent(InsteonAddress address, Msg msg) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
        String pathname = getMsgEventsFilePath(address.toString());

        try {
            File file = new File(pathname);
            file.getParentFile().mkdirs();
            file.createNewFile();

            PrintStream ps = new PrintStream(new FileOutputStream(file, true));
            ps.println(timestamp + " " + msg.toString());
            ps.close();
        } catch (IOException e) {
            logger.warn("failed to write to message event file", e);
        }
    }

    private void listDevices(Console console) {
        Map<String, String> devicesInfo = getInsteonBinding().getDevicesInfo();
        if (devicesInfo.isEmpty()) {
            console.println("No device configured!");
        } else {
            console.println("There are " + devicesInfo.size() + " devices configured:");
            display(console, devicesInfo);
        }
    }

    private void linkDevice(Console console, @Nullable String address) {
        if (address == null) {
            console.println("Linking device...");
            console.println("Press the device SET button to link.");
            getInsteonBinding().getDriver().linkDevice(null);
        } else if (InsteonAddress.isValid(address)) {
            console.println("Linking device " + address + "...");
            getInsteonBinding().getDriver().linkDevice(new InsteonAddress(address));
        } else {
            console.println("Invalid device address " + address + ".");
        }
    }

    private void unlinkDevice(Console console, String address) {
        if (InsteonAddress.isValid(address)) {
            console.println("Unlinking device " + address + "...");
            getInsteonBinding().getDriver().unlinkDevice(new InsteonAddress(address));
        } else {
            console.println("Invalid device address " + address + ".");
        }
    }

    private void refreshDevice(Console console, String address) {
        InsteonDevice device = getInsteonBinding().getDevice(address);
        if (device == null) {
            console.println("The device address is not valid or configured!");
        } else {
            device.getLinkDB().setRefresh(true);
            device.setInsteonEngine(InsteonEngine.UNKNOWN);
            console.println("The device " + address + " is scheduled to be refreshed on its next poll.");
        }
    }

    private void listDeviceDatabase(Console console, String address) {
        List<String> deviceDBInfo = getInsteonBinding().getDeviceDBInfo(address);
        if (deviceDBInfo == null) {
            console.println("The device address is not valid or configured!");
        } else if (deviceDBInfo.isEmpty()) {
            console.println("The all-link database for device " + address + " is empty");
        } else {
            console.println(
                    "The all-link database for device " + address + " contains " + deviceDBInfo.size() + " records:");
            display(console, deviceDBInfo);
        }
    }

    private void listDeviceProductData(Console console, String address) {
        String deviceProductData = getInsteonBinding().getDeviceProductData(address);
        if (deviceProductData == null) {
            console.println("The device address is not valid or configured!");
        } else if (deviceProductData.isEmpty()) {
            console.println("The product data for device " + address + " is not available");
        } else {
            console.println("The product data for device " + address + " is:");
            console.println(deviceProductData);
        }
    }

    public void listModemDatabase(Console console) {
        InsteonAddress address = getInsteonBinding().getModemAddress();
        Map<String, String> modemDBInfo = getInsteonBinding().getModemDBInfo();
        if (modemDBInfo.isEmpty()) {
            console.println("The modem database is empty");
        } else {
            console.println("The modem database for " + address + " contains " + modemDBInfo.size() + " entries:");
            display(console, modemDBInfo);
        }
    }

    private void listScenes(Console console) {
        Map<String, String> scenesInfo = getInsteonBinding().getScenesInfo();
        if (scenesInfo.isEmpty()) {
            console.println("No scene configured!");
        } else {
            console.println("There are " + scenesInfo.size() + " scenes configured:");
            display(console, scenesInfo);
        }
    }

    private void listChannels(Console console, @Nullable String thingId) {
        Map<String, String> channelsInfo = getInsteonBinding().getChannelsInfo(thingId);
        if (channelsInfo.isEmpty()) {
            console.println("No channel found!");
        } else {
            console.println("There are " + channelsInfo.size() + " channels available:");
            display(console, channelsInfo);
        }
    }

    private void listMonitoredDevices(Console console) {
        String addresses = monitoredAddresses.stream().map(InsteonAddress::toString).collect(Collectors.joining(", "));
        if (!addresses.isEmpty()) {
            console.println("The monitored device(s) are: " + addresses);
        } else if (monitorAllDevices) {
            console.println("All devices are monitored.");
        } else {
            console.println("Not monitoring any devices.");
        }
    }

    private void startMonitoring(Console console, String address) {
        if ("all".equalsIgnoreCase(address)) {
            if (!monitorAllDevices) {
                monitorAllDevices = true;
                monitoredAddresses.clear();
                console.println("Started monitoring all devices.");
                console.println("Message events logged in " + InsteonBindingConstants.BINDING_DATA_DIR);
                clearMonitorFiles(address);
            } else {
                console.println("Already monitoring all devices.");
            }
        } else if (InsteonAddress.isValid(address)) {
            if (monitorAllDevices) {
                console.println("Already monitoring all devices.");
            } else if (monitoredAddresses.add(new InsteonAddress(address))) {
                console.println("Started monitoring the device " + address + ".");
                console.println("Message events logged in " + getMsgEventsFilePath(address));
                clearMonitorFiles(address);
            } else {
                console.println("Already monitoring the device " + address + ".");
            }
        } else {
            console.println("Invalid device address" + address + ".");
            return;
        }

        if (!monitoring) {
            getInsteonBinding().getDriver().addPortListener(this);
            monitoring = true;
        }
    }

    private void stopMonitoring(Console console, String address) {
        if (!monitoring) {
            console.println("Not monitoring any devices.");
            return;
        }

        if ("all".equalsIgnoreCase(address)) {
            if (monitorAllDevices) {
                monitorAllDevices = false;
                console.println("Stopped monitoring all devices.");
            } else {
                console.println("Not monitoring all devices.");
            }
        } else if (InsteonAddress.isValid(address)) {
            if (monitorAllDevices) {
                console.println("Not monitoring individual devices.");
            } else if (monitoredAddresses.remove(new InsteonAddress(address))) {
                console.println("Stopped monitoring the device " + address + ".");
            } else {
                console.println("Not monitoring the device " + address + ".");
                return;
            }
        } else {
            console.println("Invalid address device address " + address + ".");
            return;
        }

        if (!monitorAllDevices && monitoredAddresses.isEmpty()) {
            getInsteonBinding().getDriver().removePortListener(this);
            monitoring = false;
        }
    }

    private void sendBroadcastMessage(Console console, String[] args) {
        if (!InsteonScene.isValidGroup(args[1])) {
            console.println("Invalid group argument: " + args[1]);
            return;
        }

        if (!ByteUtils.isValidHexStringArray(Arrays.copyOfRange(args, 2, args.length))) {
            console.println("Invalid hex argument(s).");
            return;
        }

        if (!getInsteonBinding().isModemDBComplete()) {
            console.println("Not ready to send messages yet.");
            return;
        }

        try {
            int group = Integer.parseInt(args[1]);
            byte cmd1 = (byte) ByteUtils.hexStringToInteger(args[2]);
            byte cmd2 = (byte) ByteUtils.hexStringToInteger(args[3]);
            Msg msg = Msg.makeBroadcastMessage(group, cmd1, cmd2);
            getInsteonBinding().getDriver().writeMessage(msg);
            console.println("Broadcast message sent to group " + group + ".");
            console.println(msg.toString());
        } catch (FieldException | InvalidMessageTypeException | NumberFormatException e) {
            console.println("Error while trying to create message.");
        } catch (IOException e) {
            console.println("Failed to send message.");
        }
    }

    private void sendDirectMessage(Console console, MessageType messageType, String[] args) {
        if (!InsteonAddress.isValid(args[1])) {
            console.println("Invalid device address argument: " + args[1]);
            return;
        }

        if (!ByteUtils.isValidHexStringArray(Arrays.copyOfRange(args, 2, args.length))) {
            console.println("Invalid hex argument(s).");
            return;
        }

        if (!getInsteonBinding().isModemDBComplete()) {
            console.println("Not ready to send messages yet.");
            return;
        }

        try {
            InsteonAddress address = new InsteonAddress(args[1]);
            byte cmd1 = (byte) ByteUtils.hexStringToInteger(args[2]);
            byte cmd2 = (byte) ByteUtils.hexStringToInteger(args[3]);
            Msg msg;
            if (messageType == MessageType.STANDARD) {
                msg = Msg.makeStandardMessage(address, cmd1, cmd2);
            } else {
                byte[] data = new byte[args.length - 4];
                for (int i = 0; i + 4 < args.length; i++) {
                    data[i] = (byte) ByteUtils.hexStringToInteger(args[i + 4]);
                }

                if (messageType == MessageType.EXTENDED) {
                    msg = Msg.makeExtendedMessage(address, cmd1, cmd2, data, true);
                } else {
                    msg = Msg.makeExtendedMessageCRC2(address, cmd1, cmd2, data);
                }
            }
            getInsteonBinding().getDriver().writeMessage(msg);
            console.println("Direct message sent to device " + address + ".");
            console.println(msg.toString());
        } catch (FieldException | InvalidMessageTypeException | NumberFormatException e) {
            console.println("Error while trying to create message.");
        } catch (IOException e) {
            console.println("Failed to send message.");
        }
    }

    private void sendX10Message(Console console, String[] args) {
        if (!X10.isValidAddress(args[1])) {
            console.println("Invalid device address argument: " + args[1]);
            return;
        }

        if (!ByteUtils.isValidHexStringArray(Arrays.copyOfRange(args, 2, args.length))) {
            console.println("Invalid hex argument(s).");
            return;
        }

        if (!getInsteonBinding().isModemDBComplete()) {
            console.println("Not ready to send messages yet.");
            return;
        }

        try {
            InsteonAddress address = new InsteonAddress(args[1]);
            byte cmd = (byte) ByteUtils.hexStringToInteger(args[2]);
            Msg maddr = Msg.makeX10Message(address.getX10Code(), X10.Flag.ADDRESS.code());
            getInsteonBinding().getDriver().writeMessage(maddr);
            Msg mcmd = Msg.makeX10Message(cmd, X10.Flag.COMMAND.code());
            getInsteonBinding().getDriver().writeMessage(mcmd);
            console.println("X10 message sent to device " + address + ".");
            console.println(maddr.toString());
            console.println(mcmd.toString());
        } catch (FieldException | InvalidMessageTypeException | NumberFormatException e) {
            console.println("Error while trying to create message.");
        } catch (IOException e) {
            console.println("Failed to send message.");
        }
    }

    private void sendIMMessage(Console console, String[] args) {
        if (!ByteUtils.isValidHexStringArray(Arrays.copyOfRange(args, 2, args.length))) {
            console.println("Invalid hex argument(s).");
            return;
        }

        if (!getInsteonBinding().isModemDBComplete()) {
            console.println("Not ready to send messages yet.");
            return;
        }

        try {
            Msg msg = Msg.makeMessage(args[1]);
            byte[] data = msg.getData();
            int headerLength = msg.getHeaderLength();
            for (int i = 0; i + 2 < args.length; i++) {
                data[i + headerLength] = (byte) ByteUtils.hexStringToInteger(args[i + 2]);
            }
            getInsteonBinding().getDriver().writeMessage(msg);
            console.println("IM message sent to the modem.");
            console.println(msg.toString());
        } catch (ArrayIndexOutOfBoundsException e) {
            console.println("Too many data bytes provided.");
        } catch (InvalidMessageTypeException e) {
            console.println("Error while trying to create message.");
        } catch (IOException e) {
            console.println("Failed to send message.");
        }
    }

    private @Nullable InsteonBridgeHandler getBridgeHandler() {
        InsteonBridgeHandler handler = this.handler;
        if (handler == null) {
            handler = getBridgeHandlers().findFirst().orElse(null);
            this.handler = handler;
        }
        return handler;
    }

    private @Nullable InsteonBridgeHandler getBridgeHandler(InsteonAddress address) {
        return getBridgeHandlers().filter(handler -> handler.getInsteonBinding().getModemAddress().equals(address))
                .findFirst().orElse(null);
    }

    private Stream<InsteonBridgeHandler> getBridgeHandlers() {
        return thingRegistry.getAll().stream().filter(Thing::isEnabled).map(Thing::getHandler)
                .filter(InsteonBridgeHandler.class::isInstance).map(InsteonBridgeHandler.class::cast);
    }

    private void setBridgeHandler(Console console, String address) {
        if (!InsteonAddress.isValid(address)) {
            console.println("Invalid modem address " + address + ".");
            return;
        }

        InsteonBridgeHandler handler = getBridgeHandler(new InsteonAddress(address));
        if (handler != null) {
            this.handler = handler;
            console.println("Using Insteon bridge " + handler.getThing().getUID().getAsString());
        } else {
            this.handler = null;
            console.println("No Insteon bridge configured or enabled for modem " + address + ".");
        }
    }

    private InsteonBinding getInsteonBinding() {
        InsteonBridgeHandler handler = this.handler;
        Objects.requireNonNull(handler);
        return handler.getInsteonBinding();
    }

    private void display(Console console, List<String> info) {
        info.forEach(console::println);
    }

    private void display(Console console, Map<String, String> info) {
        info.entrySet().stream().sorted(Entry.comparingByKey()).map(Entry::getValue).forEach(console::println);
    }
}
