/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.InsteonBindingConstants;
import org.openhab.binding.insteon.internal.InsteonLegacyBinding;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.device.LegacyDevice;
import org.openhab.binding.insteon.internal.device.LegacyDeviceFeature;
import org.openhab.binding.insteon.internal.handler.InsteonLegacyNetworkHandler;
import org.openhab.binding.insteon.internal.transport.LegacyPortListener;
import org.openhab.binding.insteon.internal.transport.message.FieldException;
import org.openhab.binding.insteon.internal.transport.message.InvalidMessageTypeException;
import org.openhab.binding.insteon.internal.transport.message.Msg;
import org.openhab.binding.insteon.internal.utils.HexUtils;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;

/**
 *
 * The {@link InsteonLegacyCommandExtension} is responsible for handling legacy console commands
 *
 * @author Rob Nielsen - Initial contribution
 * @author Jeremy Setton - Rewrite insteon binding
 */
@NonNullByDefault
public class InsteonLegacyCommandExtension extends AbstractConsoleCommandExtension implements LegacyPortListener {
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
    }

    private final ThingRegistry thingRegistry;

    @Nullable
    private Console console;
    private boolean monitoring = false;
    private boolean monitorAllDevices = false;
    private Set<InsteonAddress> monitoredAddresses = new HashSet<>();

    public InsteonLegacyCommandExtension(final ThingRegistry thingRegistry) {
        super(InsteonBindingConstants.BINDING_ID, "Interact with the Insteon integration.");
        this.thingRegistry = thingRegistry;
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length > 0) {
            InsteonLegacyNetworkHandler handler = getLegacyNetworkHandler();
            if (handler == null) {
                console.println("No Insteon legacy network bridge configured.");
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
        return List.of(
                buildCommandUsage(DISPLAY_DEVICES,
                        "display legacy devices that are online, along with available channels"),
                buildCommandUsage(DISPLAY_CHANNELS,
                        "display legacy channels that are linked, along with configuration information"),
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
                        "send extended message with a two byte crc to a device"));
    }

    @Override
    public void msg(Msg msg) {
        try {
            if (monitorAllDevices || monitoredAddresses.contains(msg.getInsteonAddress("fromAddress"))) {
                String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
                Console console = this.console;
                if (console != null) {
                    console.println(date + " " + msg.toString());
                }
            }
        } catch (FieldException ignored) {
            // ignore message with no address field
        }
    }

    public boolean isAvailable() {
        return getLegacyNetworkHandler() != null;
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
            console.println("Not monitoring any devices.");
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
            getInsteonBinding().getDriver().addPortListener(this);

            this.console = console;
            monitoring = true;
        }
    }

    private void stopMonitoring(Console console, String addr) {
        if (!monitoring) {
            console.println("Not monitoring any devices.");
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
            getInsteonBinding().getDriver().removePortListener(this);
            this.console = null;
            monitoring = false;
        }
    }

    private void sendMessage(Console console, MessageType messageType, String[] args) {
        LegacyDevice device = new LegacyDevice();
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
            InsteonAddress address = (InsteonAddress) device.getAddress();
            byte flags = (byte) HexUtils.toInteger(args[2]);
            byte cmd1 = (byte) HexUtils.toInteger(args[3]);
            byte cmd2 = (byte) HexUtils.toInteger(args[4]);
            Msg msg;
            if (messageType == MessageType.STANDARD) {
                msg = Msg.makeStandardMessage(address, flags, cmd1, cmd2);
            } else {
                byte[] data = new byte[args.length - 5];
                for (int i = 0; i + 5 < args.length; i++) {
                    data[i] = (byte) HexUtils.toInteger(args[i + 5]);
                }

                msg = Msg.makeExtendedMessage(address, flags, cmd1, cmd2, data, false);
                if (messageType == MessageType.EXTENDED) {
                    msg.setCRC();
                } else {
                    msg.setCRC2();
                }
            }
            device.enqueueMessage(msg, new LegacyDeviceFeature(device, "console"));
        } catch (FieldException | InvalidMessageTypeException e) {
            console.println("Error while trying to create message.");
        }
    }

    private @Nullable InsteonLegacyNetworkHandler getLegacyNetworkHandler() {
        return thingRegistry.getAll().stream().filter(Thing::isEnabled).map(Thing::getHandler)
                .filter(InsteonLegacyNetworkHandler.class::isInstance).map(InsteonLegacyNetworkHandler.class::cast)
                .findFirst().orElse(null);
    }

    private InsteonLegacyBinding getInsteonBinding() {
        InsteonLegacyNetworkHandler handler = getLegacyNetworkHandler();
        if (handler == null) {
            throw new IllegalArgumentException("No Insteon legacy network bridge configured.");
        }

        return handler.getInsteonBinding();
    }
}
