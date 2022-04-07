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
package org.openhab.binding.elroconnects.internal.console;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.elroconnects.internal.handler.ElroConnectsBridgeHandler;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link ElroConnectsCommandExtension} is responsible for handling console commands
 *
 * @author Mark Herwege - Initial contribution
 */

@NonNullByDefault
@Component(service = ConsoleCommandExtension.class)
public class ElroConnectsCommandExtension extends AbstractConsoleCommandExtension {

    private static final String CONNECTORS = "connectors";
    private static final String DEVICES = "devices";
    private static final String REFRESH = "refresh";
    private static final String RENAME = "rename";
    private static final String JOIN = "join";
    private static final String REPLACE = "replace";
    private static final String REMOVE = "remove";
    private static final String CANCEL = "cancel";

    private final ThingRegistry thingRegistry;

    @Activate
    public ElroConnectsCommandExtension(final @Reference ThingRegistry thingRegistry) {
        super("elroconnects", "Interact with the ELRO Connects binding");
        this.thingRegistry = thingRegistry;
    }

    @Override
    public void execute(String[] args, Console console) {
        if ((args.length < 1) || (args.length > 4)) {
            console.println("Invalid number of arguments");
            printUsage(console);
            return;
        }

        List<ElroConnectsBridgeHandler> bridgeHandlers = thingRegistry.getAll().stream()
                .filter(t -> t.getHandler() instanceof ElroConnectsBridgeHandler)
                .map(b -> ((ElroConnectsBridgeHandler) b.getHandler())).collect(Collectors.toList());

        if (CONNECTORS.equals(args[0])) {
            if (args.length > 1) {
                console.println("No extra argument allowed after 'connectors'");
                printUsage(console);
            } else if (bridgeHandlers.isEmpty()) {
                console.println("No K1 hubs added as a bridge");
            } else {
                bridgeHandlers.forEach(b -> console.printf("%s%n", b.getConnectorId()));
            }
            return;
        }

        Optional<ElroConnectsBridgeHandler> bridgeOptional = bridgeHandlers.stream()
                .filter(b -> b.getConnectorId().equals(args[0])).findAny();
        if (bridgeOptional.isEmpty()) {
            console.println("'" + args[0] + "' is not a valid connectorId for an ELRO Connects bridge");
            printUsage(console);
            return;
        }
        ElroConnectsBridgeHandler bridgeHandler = bridgeOptional.get();
        if (!ThingStatus.ONLINE.equals(bridgeHandler.getThing().getStatus())) {
            console.println("ELRO Connects bridge not online, no commands allowed");
            return;
        }

        if (args.length < 2) {
            console.println("Invalid number of arguments");
            printUsage(console);
            return;
        }

        switch (args[1]) {
            case REFRESH:
                if (args.length > 2) {
                    console.println("No extra argument allowed after '" + args[1] + "'");
                    printUsage(console);
                } else {
                    bridgeHandler.refreshFromConsole();
                }
                break;
            case DEVICES:
                if (args.length > 2) {
                    console.println("No extra argument allowed after '" + args[1] + "'");
                    printUsage(console);
                } else {
                    bridgeHandler.listDevicesFromConsole().forEach((id, name) -> console.printf("%5d %s%n", id, name));
                }
                break;
            case JOIN:
                if (args.length > 2) {
                    console.println("No extra argument allowed after '" + args[1] + "'");
                    printUsage(console);
                } else {
                    bridgeHandler.joinDeviceFromConsole();
                    console.println("Device join mode active");
                }
                break;
            case CANCEL:
                if (args.length < 3) {
                    console.println("Invalid number of arguments");
                    printUsage(console);
                } else if (JOIN.equals(args[2]) || REPLACE.equals(args[2])) {
                    if (args.length > 3) {
                        console.println("No extra argument allowed after '" + args[2] + "'");
                        printUsage(console);
                    } else {
                        bridgeHandler.cancelJoinDeviceFromConsole();
                        console.println("Device join mode inactive");
                    }
                } else {
                    console.println("Command argument '" + args[2] + "' not recognized");
                    printUsage(console);
                    return;
                }
                break;
            case REPLACE:
                if (args.length < 3) {
                    console.println("Invalid number of arguments");
                    printUsage(console);
                } else {
                    try {
                        if (args.length > 3) {
                            console.println("No extra argument allowed after '" + args[2] + "'");
                            printUsage(console);
                        } else if (!bridgeHandler.replaceDeviceFromConsole(Integer.valueOf(args[2]))) {
                            console.println("Command argument '" + args[2] + "' is not a known deviceId");
                            printUsage(console);
                        } else {
                            console.println("Device join mode active");
                        }
                    } catch (NumberFormatException e) {
                        console.println("Command argument '" + args[2] + "' is not a numeric deviceId");
                        printUsage(console);
                    }
                }
                break;
            case REMOVE:
                if (args.length < 3) {
                    console.println("Invalid number of arguments");
                    printUsage(console);
                } else {
                    try {
                        if (args.length > 3) {
                            console.println("No extra argument allowed after '" + args[2] + "'");
                            printUsage(console);
                        } else if (!bridgeHandler.removeDeviceFromConsole(Integer.valueOf(args[2]))) {
                            console.println("Command argument '" + args[2] + "' is not a known deviceId");
                            printUsage(console);
                        }
                    } catch (NumberFormatException e) {
                        console.println("Command argument '" + args[2] + "' is not a numeric deviceId");
                        printUsage(console);
                    }
                }
                break;
            case RENAME:
                if (args.length < 4) {
                    console.println("Invalid number of arguments");
                    printUsage(console);
                } else {
                    try {
                        if (args.length > 4) {
                            console.println("No extra argument allowed after '" + args[2] + " " + args[3] + "'");
                            printUsage(console);
                        } else if (!bridgeHandler.renameDeviceFromConsole(Integer.valueOf(args[2]), args[3])) {
                            console.println("Command argument '" + args[2] + "' is not a known deviceId");
                            printUsage(console);
                        }
                    } catch (NumberFormatException e) {
                        console.println("Command argument '" + args[2] + "' is not a numeric deviceId");
                        printUsage(console);
                    }
                }
                break;
            default:
                console.println("Command argument '" + args[1] + "' not recognized");
                printUsage(console);
        }
    }

    public void joinDeviceCancelled(Console console) {
        console.println("Device join mode inactive");
    }

    @Override
    public List<String> getUsages() {
        return Arrays.asList(new String[] { buildCommandUsage(CONNECTORS, "list all K1 hub connector ID's"),
                buildCommandUsage("<connectorId> " + REFRESH, "refresh device list, names and status"),
                buildCommandUsage("<connectorId> " + DEVICES, "list all devices connected to the K1 hub"),
                buildCommandUsage("<connectorId> " + RENAME + " <deviceId> <name>", "rename device with ID"),
                buildCommandUsage("<connectorId> " + JOIN,
                        "put K1 hub in device join mode, 3 short presses on the device will join it to the hub"),
                buildCommandUsage("<connectorId> " + CANCEL + " " + JOIN, "cancel K1 hub device join mode"),
                buildCommandUsage("<connectorId> " + REPLACE + " <deviceId>",
                        "replace device with ID by newly joined device, puts K1 hub in join mode"),
                buildCommandUsage("<connectorId> " + CANCEL + " " + REPLACE, "cancel K1 hub device replace mode"),
                buildCommandUsage("<connectorId> " + REMOVE + " <deviceId>", "remove device with ID from K1 hub") });
    }
}
