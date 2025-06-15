/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.DeviceAddress;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.device.InsteonScene;
import org.openhab.binding.insteon.internal.device.X10Address;
import org.openhab.binding.insteon.internal.device.X10Device;
import org.openhab.binding.insteon.internal.transport.PortListener;
import org.openhab.binding.insteon.internal.transport.message.Direction;
import org.openhab.binding.insteon.internal.transport.message.FieldException;
import org.openhab.binding.insteon.internal.transport.message.InvalidMessageTypeException;
import org.openhab.binding.insteon.internal.transport.message.Msg;
import org.openhab.binding.insteon.internal.transport.message.MsgDefinitionRegistry;
import org.openhab.binding.insteon.internal.utils.HexUtils;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.StringsCompleter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * The {@link DebugCommand} represents an Insteon console debug command
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class DebugCommand extends InsteonCommand implements PortListener {
    private static final String NAME = "debug";
    private static final String DESCRIPTION = "Insteon debug commands";

    private static final String LIST_MONITORED = "listMonitored";
    private static final String START_MONITORING = "startMonitoring";
    private static final String STOP_MONITORING = "stopMonitoring";
    private static final String SEND_BROADCAST_MESSAGE = "sendBroadcastMessage";
    private static final String SEND_STANDARD_MESSAGE = "sendStandardMessage";
    private static final String SEND_EXTENDED_MESSAGE = "sendExtendedMessage";
    private static final String SEND_EXTENDED_2_MESSAGE = "sendExtended2Message";
    private static final String SEND_X10_MESSAGE = "sendX10Message";
    private static final String SEND_IM_MESSAGE = "sendIMMessage";

    private static final List<String> SUBCMDS = List.of(LIST_MONITORED, START_MONITORING, STOP_MONITORING,
            SEND_BROADCAST_MESSAGE, SEND_STANDARD_MESSAGE, SEND_EXTENDED_MESSAGE, SEND_EXTENDED_2_MESSAGE,
            SEND_X10_MESSAGE, SEND_IM_MESSAGE);

    private static final String ALL_OPTION = "--all";

    private static final String MSG_EVENTS_FILE_PREFIX = "message-events";

    private static enum MessageType {
        STANDARD,
        EXTENDED,
        EXTENDED_2
    }

    private final Logger logger = LoggerFactory.getLogger(DebugCommand.class);

    private boolean monitoring = false;
    private boolean monitorAllDevices = false;
    private Set<DeviceAddress> monitoredAddresses = new HashSet<>();
    private @Nullable X10Address lastX10Address;

    public DebugCommand(InsteonCommandExtension commandExtension) {
        super(NAME, DESCRIPTION, commandExtension);
    }

    @Override
    public List<String> getUsages() {
        return List.of(buildCommandUsage(LIST_MONITORED, "list monitored Insteon/X10 device(s)"),
                buildCommandUsage(START_MONITORING + " " + ALL_OPTION + "|<address>",
                        "start logging message events for Insteon/X10 device(s) in separate file(s)"),
                buildCommandUsage(STOP_MONITORING + " " + ALL_OPTION + "|<address>",
                        "stop logging message events for Insteon/X10 device(s) in separate file(s)"),
                buildCommandUsage(SEND_BROADCAST_MESSAGE + " <group> <cmd1> <cmd2>",
                        "send an Insteon broadcast message to a group"),
                buildCommandUsage(SEND_STANDARD_MESSAGE + " <address> <cmd1> <cmd2>",
                        "send an Insteon standard message to a device"),
                buildCommandUsage(SEND_EXTENDED_MESSAGE + " <address> <cmd1> <cmd2> [<data1> ... <data13>]",
                        "send an Insteon extended message with standard crc to a device"),
                buildCommandUsage(SEND_EXTENDED_2_MESSAGE + " <address> <cmd1> <cmd2> [<data1> ... <data12>]",
                        "send an Insteon extended message with a two-byte crc to a device"),
                buildCommandUsage(SEND_X10_MESSAGE + " <address> <cmd>", "send an X10 message to a device"),
                buildCommandUsage(SEND_IM_MESSAGE + " <name> [<data1> <data2> ...]",
                        "send an IM message to the modem"));
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length == 0) {
            printUsage(console);
            return;
        }

        switch (args[0]) {
            case LIST_MONITORED:
                if (args.length == 1) {
                    listMonitoredDevices(console);
                } else {
                    printUsage(console, args[0]);
                }
                break;
            case START_MONITORING:
                if (args.length == 2) {
                    startMonitoring(console, args[1]);
                } else {
                    printUsage(console, args[0]);
                }
                break;
            case STOP_MONITORING:
                if (args.length == 2) {
                    stopMonitoring(console, args[1]);
                } else {
                    printUsage(console, args[0]);
                }
                break;
            case SEND_BROADCAST_MESSAGE:
                if (args.length == 4) {
                    sendBroadcastMessage(console, args);
                } else {
                    printUsage(console, args[0]);
                }
                break;
            case SEND_STANDARD_MESSAGE:
                if (args.length == 4) {
                    sendDirectMessage(console, MessageType.STANDARD, args);
                } else {
                    printUsage(console, args[0]);
                }
                break;
            case SEND_EXTENDED_MESSAGE:
                if (args.length >= 4 && args.length <= 17) {
                    sendDirectMessage(console, MessageType.EXTENDED, args);
                } else {
                    printUsage(console, args[0]);
                }
                break;
            case SEND_EXTENDED_2_MESSAGE:
                if (args.length >= 4 && args.length <= 16) {
                    sendDirectMessage(console, MessageType.EXTENDED_2, args);
                } else {
                    printUsage(console, args[0]);
                }
                break;
            case SEND_X10_MESSAGE:
                if (args.length == 3) {
                    sendX10Message(console, args);
                } else {
                    printUsage(console, args[0]);
                }
                break;
            case SEND_IM_MESSAGE:
                if (args.length >= 2) {
                    sendIMMessage(console, args);
                } else {
                    printUsage(console, args[0]);
                }
                break;
            default:
                console.println("Unknown command '" + args[0] + "'");
                printUsage(console);
                break;
        }
    }

    @Override
    public boolean complete(String[] args, int cursorArgumentIndex, int cursorPosition, List<String> candidates) {
        List<String> strings = List.of();
        if (cursorArgumentIndex == 0) {
            strings = SUBCMDS;
        } else if (cursorArgumentIndex == 1) {
            switch (args[0]) {
                case START_MONITORING:
                    strings = monitorAllDevices ? List.of()
                            : Stream.concat(Stream.of(ALL_OPTION),
                                    Stream.concat(Stream.of(getModem().getAddress()),
                                            getModem().getDB().getDevices().stream())
                                            .filter(address -> !monitoredAddresses.contains(address))
                                            .map(InsteonAddress::toString))
                                    .toList();
                    break;
                case STOP_MONITORING:
                    strings = monitorAllDevices ? List.of(ALL_OPTION)
                            : monitoredAddresses.stream().map(DeviceAddress::toString).toList();
                    break;
                case SEND_BROADCAST_MESSAGE:
                    strings = getModem().getDB().getBroadcastGroups().stream().map(String::valueOf).toList();
                    break;
                case SEND_STANDARD_MESSAGE:
                case SEND_EXTENDED_MESSAGE:
                case SEND_EXTENDED_2_MESSAGE:
                    strings = getModem().getDB().getDevices().stream().map(InsteonAddress::toString).toList();
                    break;
                case SEND_X10_MESSAGE:
                    strings = getModem().getX10Devices().stream().map(X10Device::getAddress).map(X10Address::toString)
                            .toList();
                    break;
                case SEND_IM_MESSAGE:
                    strings = MsgDefinitionRegistry.getInstance().getDefinitions().entrySet().stream()
                            .filter(entry -> entry.getValue().getDirection() == Direction.TO_MODEM).map(Entry::getKey)
                            .toList();
                    break;
            }
        }

        return new StringsCompleter(strings, false).complete(args, cursorArgumentIndex, cursorPosition, candidates);
    }

    @Override
    public void disconnected() {
        // do nothing
    }

    @Override
    public void messageReceived(Msg msg) {
        logMessageEvent(msg);
    }

    @Override
    public void messageSent(Msg msg) {
        logMessageEvent(msg);
    }

    private DeviceAddress getMsgEventAddress(Msg msg) throws FieldException {
        if (msg.isX10()) {
            X10Address address = msg.isX10Address() ? msg.getX10Address() : lastX10Address;
            if (address == null) {
                throw new FieldException("unknown x10 address");
            }
            lastX10Address = address;
            return address;
        } else if (msg.isInsteon()) {
            return msg.isInbound() && !msg.isReply() ? msg.getInsteonAddress("fromAddress")
                    : !msg.isAllLinkBroadcast() ? msg.getInsteonAddress("toAddress") : getModem().getAddress();
        } else {
            return getModem().getAddress();
        }
    }

    private Path getMsgEventsFilePath(DeviceAddress address) {
        String name = address.toString().toLowerCase().replace(".", "");
        if (address instanceof X10Address) {
            name = "x10-" + name;
        }
        return getBindingDataFilePath(MSG_EVENTS_FILE_PREFIX + "-" + name + ".log");
    }

    private void clearMonitorFiles() {
        getBindingDataFilePaths(MSG_EVENTS_FILE_PREFIX).map(Path::toFile).forEach(File::delete);
    }

    private void truncateMonitorFile(DeviceAddress address) {
        try {
            Path path = getMsgEventsFilePath(address);

            Files.createDirectories(path.getParent());
            Files.writeString(path, "", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            logger.warn("failed to truncate message event file", e);
        }
    }

    private void logMessageEvent(Msg msg) {
        try {
            DeviceAddress address = getMsgEventAddress(msg);
            if (monitorAllDevices || monitoredAddresses.contains(address)) {
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
                String line = timestamp + " " + msg + System.lineSeparator();
                Path path = getMsgEventsFilePath(address);

                Files.createDirectories(path.getParent());
                Files.writeString(path, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }
        } catch (FieldException e) {
            logger.warn("failed to parse message", e);
        } catch (IOException e) {
            logger.warn("failed to write to message event file", e);
        }
    }

    private void listMonitoredDevices(Console console) {
        String addresses = monitoredAddresses.stream().map(DeviceAddress::toString).collect(Collectors.joining(", "));
        if (!addresses.isEmpty()) {
            console.println("The monitored device(s) are: " + addresses);
        } else if (monitorAllDevices) {
            console.println("All devices are monitored.");
        } else {
            console.println("Not monitoring any devices.");
        }
    }

    private void startMonitoring(Console console, String arg) {
        if (monitorAllDevices) {
            console.println("Already monitoring all devices.");
        } else if (ALL_OPTION.equals(arg)) {
            monitorAllDevices = true;
            monitoredAddresses.clear();
            console.println("Started monitoring all devices.");
            console.println("Message events logged in " + getBindingDataDirPath());
            clearMonitorFiles();
        } else {
            DeviceAddress address = InsteonAddress.isValid(arg) ? new InsteonAddress(arg)
                    : X10Address.isValid(arg) ? new X10Address(arg) : null;
            if (address == null) {
                console.println("Invalid device address argument: " + arg);
            } else if (monitoredAddresses.add(address)) {
                console.println("Started monitoring the device " + address + ".");
                console.println("Message events logged in " + getMsgEventsFilePath(address));
                truncateMonitorFile(address);
            } else {
                console.println("Already monitoring the device " + address + ".");
            }
        }

        if (!monitoring) {
            getModem().getPort().registerListener(this);
            monitoring = true;
        }
    }

    private void stopMonitoring(Console console, String arg) {
        if (ALL_OPTION.equals(arg)) {
            if (monitorAllDevices) {
                monitorAllDevices = false;
                console.println("Stopped monitoring all devices.");
            } else {
                console.println("Not monitoring all devices.");
            }
        } else {
            DeviceAddress address = InsteonAddress.isValid(arg) ? new InsteonAddress(arg)
                    : X10Address.isValid(arg) ? new X10Address(arg) : null;
            if (monitorAllDevices) {
                console.println("Not monitoring individual devices.");
            } else if (address == null) {
                console.println("Invalid device address argument: " + arg);
            } else if (monitoredAddresses.remove(address)) {
                console.println("Stopped monitoring the device " + address + ".");
            } else {
                console.println("Not monitoring the device " + address + ".");
            }
        }

        if (!monitorAllDevices && monitoredAddresses.isEmpty()) {
            getModem().getPort().unregisterListener(this);
            monitoring = false;
        }
    }

    private void sendBroadcastMessage(Console console, String[] args) {
        if (!InsteonScene.isValidGroup(args[1])) {
            console.println("Invalid group argument: " + args[1]);
        } else if (!HexUtils.isValidHexStringArray(args, 2, args.length)) {
            console.println("Invalid hex argument(s).");
        } else if (!getModem().getDB().isComplete()) {
            console.println("Not ready to send messages yet.");
        } else {
            try {
                int group = Integer.parseInt(args[1]);
                byte cmd1 = (byte) HexUtils.toInteger(args[2]);
                byte cmd2 = (byte) HexUtils.toInteger(args[3]);
                Msg msg = Msg.makeBroadcastMessage(group, cmd1, cmd2);
                getModem().writeMessage(msg);
                console.println("Broadcast message sent to group " + group + ".");
                console.println(msg.toString());
            } catch (FieldException | InvalidMessageTypeException | NumberFormatException e) {
                console.println("Error while trying to create message.");
            }
        }
    }

    private void sendDirectMessage(Console console, MessageType messageType, String[] args) {
        if (!InsteonAddress.isValid(args[1])) {
            console.println("Invalid device address argument: " + args[1]);
        } else if (!HexUtils.isValidHexStringArray(args, 2, args.length)) {
            console.println("Invalid hex argument(s).");
        } else if (!getModem().getDB().isComplete()) {
            console.println("Not ready to send messages yet.");
        } else {
            try {
                InsteonAddress address = new InsteonAddress(args[1]);
                byte cmd1 = (byte) HexUtils.toInteger(args[2]);
                byte cmd2 = (byte) HexUtils.toInteger(args[3]);
                Msg msg;
                if (messageType == MessageType.STANDARD) {
                    msg = Msg.makeStandardMessage(address, cmd1, cmd2);
                } else {
                    byte[] data = HexUtils.toByteArray(args, 4, args.length);
                    boolean setCRC = getInsteonEngine(args[1]).supportsChecksum();
                    if (messageType == MessageType.EXTENDED) {
                        msg = Msg.makeExtendedMessage(address, cmd1, cmd2, data, setCRC);
                    } else {
                        msg = Msg.makeExtendedMessageCRC2(address, cmd1, cmd2, data);
                    }
                }
                getModem().writeMessage(msg);
                console.println("Direct message sent to device " + address + ".");
                console.println(msg.toString());
            } catch (FieldException | InvalidMessageTypeException | NumberFormatException e) {
                console.println("Error while trying to create message.");
            }
        }
    }

    private void sendX10Message(Console console, String[] args) {
        if (!X10Address.isValid(args[1])) {
            console.println("Invalid x10 address argument: " + args[1]);
        } else if (!HexUtils.isValidHexStringArray(args, 2, args.length)) {
            console.println("Invalid hex argument(s).");
        } else if (!getModem().getDB().isComplete()) {
            console.println("Not ready to send messages yet.");
        } else {
            try {
                X10Address address = new X10Address(args[1]);
                byte cmd = (byte) HexUtils.toInteger(args[2]);
                Msg maddr = Msg.makeX10AddressMessage(address);
                getModem().writeMessage(maddr);
                Msg mcmd = Msg.makeX10CommandMessage(cmd);
                getModem().writeMessage(mcmd);
                console.println("X10 message sent to device " + address + ".");
                console.println(maddr.toString());
                console.println(mcmd.toString());
            } catch (FieldException | InvalidMessageTypeException | NumberFormatException e) {
                console.println("Error while trying to create message.");
            }
        }
    }

    private void sendIMMessage(Console console, String[] args) {
        if (!HexUtils.isValidHexStringArray(args, 2, args.length)) {
            console.println("Invalid hex argument(s).");
        } else if (!getModem().getDB().isComplete()) {
            console.println("Not ready to send messages yet.");
        } else {
            try {
                Msg msg = Msg.makeMessage(args[1]);
                byte[] data = msg.getData();
                int headerLength = msg.getHeaderLength();
                for (int i = 0; i + 2 < args.length; i++) {
                    data[i + headerLength] = (byte) HexUtils.toInteger(args[i + 2]);
                }
                getModem().writeMessage(msg);
                console.println("IM message sent to the modem.");
                console.println(msg.toString());
            } catch (ArrayIndexOutOfBoundsException e) {
                console.println("Too many data bytes provided.");
            } catch (InvalidMessageTypeException e) {
                console.println("Error while trying to create message.");
            }
        }
    }
}
