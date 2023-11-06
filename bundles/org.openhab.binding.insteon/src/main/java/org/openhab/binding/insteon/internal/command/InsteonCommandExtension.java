/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.InsteonBinding;
import org.openhab.binding.insteon.internal.device.DeviceFeature;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.device.InsteonDevice;
import org.openhab.binding.insteon.internal.handler.InsteonNetworkHandler;
import org.openhab.binding.insteon.internal.message.FieldException;
import org.openhab.binding.insteon.internal.message.InvalidMessageTypeException;
import org.openhab.binding.insteon.internal.message.Msg;
import org.openhab.binding.insteon.internal.message.MsgListener;
import org.openhab.binding.insteon.internal.utils.Utils;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 *
 * Console commands for the Insteon binding
 *
 * @author Rob Nielsen - Initial contribution
 */
@NonNullByDefault
@Component(service = ConsoleCommandExtension.class)
public class InsteonCommandExtension extends AbstractConsoleCommandExtension implements MsgListener {
    private static final String DISPLAY_DEVICES = "display_devices";
    private static final String DISPLAY_CHANNELS = "display_channels";
    private static final String DISPLAY_LOCAL_DATABASE = "display_local_database";
    private static final String DISPLAY_MONITORED = "display_monitored";
    private static final String START_MONITORING = "start_monitoring";
    private static final String STOP_MONITORING = "stop_monitoring";
    private static final String SEND_STANDARD_MESSAGE = "send_standard_message";
    private static final String SEND_EXTENDED_MESSAGE = "send_extended_message";
    private static final String SEND_EXTENDED_MESSAGE_2 = "send_extended_message_2";

    private enum MessageType {
        STANDARD,
        EXTENDED,
        EXTENDED_2
    };

    @Nullable
    private InsteonNetworkHandler handler;
    @Nullable
    private Console console;
    private boolean monitoring = false;
    private boolean monitorAllDevices = false;
    private Set<InsteonAddress> monitoredAddresses = new HashSet<>();

    public InsteonCommandExtension() {
        super("insteon", "Interact with the Insteon integration.");
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length > 0) {
            InsteonNetworkHandler handler = this.handler; // fix eclipse warnings about nullable
            if (handler == null) {
                console.println("No Insteon network bridge configured.");
            } else {
                switch (args[0]) {
                    case DISPLAY_DEVICES:
                        if (args.length == 1) {
                            handler.displayDevices(console);
                        } else {
                            printUsage(console);
                        }
                        break;
                    case DISPLAY_CHANNELS:
                        if (args.length == 1) {
                            handler.displayChannels(console);
                        } else {
                            printUsage(console);
                        }
                        break;
                    case DISPLAY_LOCAL_DATABASE:
                        if (args.length == 1) {
                            handler.displayLocalDatabase(console);
                        } else {
                            printUsage(console);
                        }
                        break;
                    case DISPLAY_MONITORED:
                        if (args.length == 1) {
                            displayMonitoredDevices(console);
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
                    case SEND_STANDARD_MESSAGE:
                        if (args.length == 5) {
                            sendMessage(console, MessageType.STANDARD, args);
                        } else {
                            printUsage(console);
                        }
                        break;
                    case SEND_EXTENDED_MESSAGE:
                        if (args.length >= 5 && args.length <= 18) {
                            sendMessage(console, MessageType.EXTENDED, args);
                        } else {
                            printUsage(console);
                        }
                        break;
                    case SEND_EXTENDED_MESSAGE_2:
                        if (args.length >= 5 && args.length <= 17) {
                            sendMessage(console, MessageType.EXTENDED_2, args);
                        } else {
                            printUsage(console);
                        }
                        break;
                    default:
                        console.println("Unknown command '" + args[0] + "'");
                        printUsage(console);
                        break;
                }
            }
        } else {
            printUsage(console);
        }
    }

    @Override
    public List<String> getUsages() {
        return Arrays.asList(new String[] {
                buildCommandUsage(DISPLAY_DEVICES, "display devices that are online, along with available channels"),
                buildCommandUsage(DISPLAY_CHANNELS,
                        "display channels that are linked, along with configuration information"),
                buildCommandUsage(DISPLAY_LOCAL_DATABASE, "display Insteon PLM or hub database details"),
                buildCommandUsage(DISPLAY_MONITORED, "display monitored device(s)"),
                buildCommandUsage(START_MONITORING + " all|address",
                        "start displaying messages received from device(s)"),
                buildCommandUsage(STOP_MONITORING + " all|address", "stop displaying messages received from device(s)"),
                buildCommandUsage(SEND_STANDARD_MESSAGE + " address flags cmd1 cmd2",
                        "send standard message to a device"),
                buildCommandUsage(SEND_EXTENDED_MESSAGE + " address flags cmd1 cmd2 [up to 13 bytes]",
                        "send extended message to a device"),
                buildCommandUsage(SEND_EXTENDED_MESSAGE_2 + " address flags cmd1 cmd2 [up to 12 bytes]",
                        "send extended message with a two byte crc to a device") });
    }

    @Override
    public void msg(Msg msg) {
        if (monitorAllDevices || monitoredAddresses.contains(msg.getAddr("fromAddress"))) {
            String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
            Console console = this.console;
            if (console != null) {
                console.println(date + " " + msg.toString());
            }
        }
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
    public void setInsteonNetworkHandler(InsteonNetworkHandler handler) {
        this.handler = handler;
    }

    public void unsetInsteonNetworkHandler(InsteonNetworkHandler handler) {
        this.handler = null;
    }

    private void displayMonitoredDevices(Console console) {
        if (!monitoredAddresses.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (InsteonAddress insteonAddress : monitoredAddresses) {
                if (builder.length() == 0) {
                    builder = new StringBuilder("The individual device(s) ");
                } else {
                    builder.append(", ");
                }
                builder.append(insteonAddress);
            }
            console.println(builder.append(" are monitored").toString());
        } else if (monitorAllDevices) {
            console.println("All devices are monitored.");
        } else {
            console.println("Not mointoring any devices.");
        }
    }

    private void startMonitoring(Console console, String addr) {
        if ("all".equalsIgnoreCase(addr)) {
            if (!monitorAllDevices) {
                monitorAllDevices = true;
                monitoredAddresses.clear();
                console.println("Started monitoring all devices.");
            } else {
                console.println("Already monitoring all devices.");
            }
        } else {
            try {
                if (monitorAllDevices) {
                    console.println("Already monitoring all devices.");
                } else if (monitoredAddresses.add(new InsteonAddress(addr))) {
                    console.println("Started monitoring the device " + addr + ".");
                } else {
                    console.println("Already monitoring the device " + addr + ".");
                }
            } catch (IllegalArgumentException e) {
                console.println("Invalid device address" + addr + ".");
                return;
            }
        }

        if (!monitoring) {
            getInsteonBinding().getDriver().addMsgListener(this);

            this.console = console;
            monitoring = true;
        }
    }

    private void stopMonitoring(Console console, String addr) {
        if (!monitoring) {
            console.println("Not mointoring any devices.");
            return;
        }

        if ("all".equalsIgnoreCase(addr)) {
            if (monitorAllDevices) {
                monitorAllDevices = false;
                console.println("Stopped monitoring all devices.");
            } else {
                console.println("Not monitoring all devices.");
            }
        } else {
            try {
                if (monitorAllDevices) {
                    console.println("Not monitoring individual devices.");
                } else if (monitoredAddresses.remove(new InsteonAddress(addr))) {
                    console.println("Stopped monitoring the device " + addr + ".");
                } else {
                    console.println("Not monitoring the device " + addr + ".");
                    return;
                }
            } catch (IllegalArgumentException e) {
                console.println("Invalid address device address " + addr + ".");
                return;
            }
        }

        if (!monitorAllDevices && monitoredAddresses.isEmpty()) {
            getInsteonBinding().getDriver().removeListener(this);
            this.console = null;
            monitoring = false;
        }
    }

    private void sendMessage(Console console, MessageType messageType, String[] args) {
        InsteonDevice device = new InsteonDevice();
        device.setDriver(getInsteonBinding().getDriver());

        try {
            device.setAddress(new InsteonAddress(args[1]));
        } catch (IllegalArgumentException e) {
            console.println("Invalid device address" + args[1] + ".");
            return;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            if (!args[i].matches("\\p{XDigit}{1,2}")) {
                if (builder.length() > 0) {
                    builder.append(", ");
                }
                builder.append(args[i]);
            }
        }
        if (builder.length() != 0) {
            builder.append(" is not a valid hexadecimal byte.");
            console.print(builder.toString());
            return;
        }

        try {
            byte flags = (byte) Utils.fromHexString(args[2]);
            byte cmd1 = (byte) Utils.fromHexString(args[3]);
            byte cmd2 = (byte) Utils.fromHexString(args[4]);
            Msg msg;
            if (messageType == MessageType.STANDARD) {
                msg = device.makeStandardMessage(flags, cmd1, cmd2);
            } else {
                byte[] data = new byte[args.length - 5];
                for (int i = 0; i + 5 < args.length; i++) {
                    data[i] = (byte) Utils.fromHexString(args[i + 5]);
                }

                if (messageType == MessageType.EXTENDED) {
                    msg = device.makeExtendedMessage(flags, cmd1, cmd2, data);
                } else {
                    msg = device.makeExtendedMessageCRC2(flags, cmd1, cmd2, data);
                }
            }
            device.enqueueMessage(msg, new DeviceFeature(device, "console"));
        } catch (FieldException | InvalidMessageTypeException e) {
            console.println("Error while trying to create message.");
        }
    }

    private InsteonBinding getInsteonBinding() {
        InsteonNetworkHandler handler = this.handler;
        if (handler == null) {
            throw new IllegalArgumentException("No Insteon network bridge configured.");
        }

        return handler.getInsteonBinding();
    }
}
