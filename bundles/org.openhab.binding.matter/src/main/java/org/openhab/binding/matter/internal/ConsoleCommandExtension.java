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
package org.openhab.binding.matter.internal;

import static org.openhab.binding.matter.internal.MatterBindingConstants.BINDING_ID;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.matter.internal.bridge.MatterBridge;
import org.openhab.binding.matter.internal.client.MatterWebsocketService;
import org.openhab.binding.matter.internal.client.dto.ws.ActiveSessionInformation;
import org.openhab.binding.matter.internal.handler.ControllerHandler;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link ConsoleCommandExtension} class provides console commands for managing Matter controllers and bridges.
 *
 * @author Dan Cunningham - Initial contribution
 */
@Component(service = org.openhab.core.io.console.extensions.ConsoleCommandExtension.class)
@NonNullByDefault
public class ConsoleCommandExtension extends AbstractConsoleCommandExtension {
    private static final String CONTROLLER = "controller";
    private static final String BRIDGE = "bridge";
    private static final String COMMON = "common";

    private final MatterHandlerFactory handlerFactory;
    private final MatterBridge matterBridge;
    private final MatterWebsocketService matterWebsocketService;

    @Activate
    public ConsoleCommandExtension(@Reference MatterHandlerFactory handlerFactory, @Reference MatterBridge matterBridge,
            @Reference MatterWebsocketService matterWebsocketService) {
        super(BINDING_ID, "Matter Console Commands");
        this.handlerFactory = handlerFactory;
        this.matterBridge = matterBridge;
        this.matterWebsocketService = matterWebsocketService;
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length == 0) {
            printUsage(console);
            return;
        }

        String command = args[0];
        switch (command) {
            case CONTROLLER:
                handleControllerCommand(args, console);
                break;
            case BRIDGE:
                handleBridgeCommand(args, console);
                break;
            case COMMON:
                handleCommonCommand(args, console);
                break;
            default:
                console.println("Unknown command '" + command + "'");
                printUsage(console);
                break;
        }
    }

    private void handleControllerCommand(String[] args, Console console) {
        if (args.length < 2) {
            console.println("Invalid use of controller command. Usage: controller <controller_id> <subcommand>");
            printUsage(console);
            return;
        }

        String controllerId = args[1];
        if ("list".equals(controllerId)) {
            listControllers(console);
            return;
        }

        if (args.length < 3) {
            console.println("Invalid use of controller command. Usage: controller <controller_id> <subcommand>");
            printUsage(console);
            return;
        }
        String subcommand = args[2];
        Optional<ControllerHandler> controllerHandler = getControllerHandler(controllerId);

        if (controllerHandler.isEmpty()) {
            console.println("Controller '" + controllerId + "' not found.");
            return;
        }

        switch (subcommand) {
            case "nodes":
                listNodes(console, controllerId);
                break;
            case "commission":
                if (args.length < 4) {
                    console.println("Missing pairing code for commission command");
                    printUsage(console);
                    return;
                }
                // controllerHandler.get().commission(args[3]);
                console.println("Commissioning node '" + args[3] + "' for controller '" + controllerId + "'");
                break;
            case "decommission":
                if (args.length < 4) {
                    console.println("Missing node ID for decommission command");
                    printUsage(console);
                    return;
                }
                // controllerHandler.get().decommission(args[3]);
                console.println("Decommissioning node '" + args[3] + "' for controller '" + controllerId + "'");
                break;
            case "resetStorage":
                // controllerHandler.get().resetStorage();
                console.println("Resetting storage for controller '" + controllerId + "'");
                break;
            case "sessionInfo":
                listSessionInfo(console, controllerId);
                break;
            case "rpc":
                if (args.length < 4) {
                    console.println("Missing RPC command");
                    printUsage(console);
                    return;
                }
                // handleRpcCommand(console, controllerId, args[3]);
                break;
            default:
                console.println("Unknown controller subcommand '" + subcommand + "'");
                printUsage(console);
                break;
        }
    }

    private void handleBridgeCommand(String[] args, Console console) {
        if (args.length < 2) {
            console.println("Invalid use of bridge command. Usage: bridge <command>");
            printUsage(console);
            return;
        }

        String subcommand = args[1];

        switch (subcommand) {
            case "fabrics":
                listFabrics(console);
                break;
            case "removeFabric":
                if (args.length < 3) {
                    console.println("Missing fabric ID for removeFabric command");
                    printUsage(console);
                    return;
                }
                console.println("Removing fabric '" + args[2] + "'");
                matterBridge.removeFabric(args[2]);
                listFabrics(console);
                break;
            case "allowCommissioning":
                matterBridge.allowCommissioning();
                break;
            case "resetStorage":
                matterBridge.resetStorage();
                break;
            case "rpc":
                if (args.length < 3) {
                    console.println("Missing RPC command");
                    printUsage(console);
                    return;
                }
                handleRpcCommand(console, args[2]);
                break;
            default:
                console.println("Unknown bridge subcommand '" + subcommand + "'");
                printUsage(console);
                break;
        }
    }

    private void handleCommonCommand(String[] args, Console console) {
        if (args.length < 2) {
            console.println("Invalid use of common command. Usage: common <subcommand>");
            printUsage(console);
            return;
        }

        String subcommand = args[1];

        switch (subcommand) {
            case "restartNode":
                restartNode(console);
                break;
            default:
                console.println("Unknown common subcommand '" + subcommand + "'");
                printUsage(console);
                break;
        }
    }

    private Optional<ControllerHandler> getControllerHandler(String id) {
        return handlerFactory.getControllers().stream()
                .filter(handler -> handler.getThing().getUID().getId().equals(id)).findAny();
    }

    private void listControllers(Console console) {
        Set<ControllerHandler> controllerHandlers = handlerFactory.getControllers();
        controllerHandlers.forEach(handler -> console.println(
                "ControllerId: " + handler.getThing().getUID().getId() + " ('" + handler.getThing().getLabel() + "')"));
    }

    private void listNodes(Console console, String controllerId) {
        Optional<ControllerHandler> controllerHandler = getControllerHandler(controllerId);
        if (controllerHandler.isPresent()) {
            console.println("Listing nodes for controller '" + controllerId + "'");
            controllerHandler.get().getLinkedNodes()
                    .forEach((nodeId, nodeHandler) -> console.println("Node: " + nodeId));
        } else {
            console.println("Controller '" + controllerId + "' not found.");
        }
    }

    private void listSessionInfo(Console console, String controllerId) {
        Optional<ControllerHandler> controllerHandler = getControllerHandler(controllerId);
        if (controllerHandler.isPresent()) {
            console.println("Listing session info for controller '" + controllerId + "'");
            controllerHandler.get().getClient().getSessionInformation().thenAccept(sessions -> {
                for (ActiveSessionInformation session : sessions) {
                    console.println("Session: " + session.name + " " + session.nodeId + " " + session.peerNodeId + " "
                            + session.fabric + " " + session.isPeerActive + " " + session.secure + " "
                            + session.lastInteractionTimestamp + " " + session.lastActiveTimestamp + " "
                            + session.numberOfActiveSubscriptions);
                }
            });
        } else {
            console.println("Controller '" + controllerId + "' not found.");
        }
    }

    private void listFabrics(Console console) {
        try {
            console.println(matterBridge.listFabrics());
        } catch (InterruptedException | ExecutionException e) {
            console.println("Failed to list fabrics: " + e.getMessage());
        }
    }

    private void handleRpcCommand(Console console, String command) {
    }

    private void restartNode(Console console) {
        matterWebsocketService.restart();
        console.println("Node server restarting...");
    }

    @Override
    public List<String> getUsages() {
        return Arrays.asList(buildCommandUsage(CONTROLLER + " list", "List all controllers"),
                buildCommandUsage(CONTROLLER + " <controller_id> nodes", "List all nodes for the specified controller"),
                buildCommandUsage(CONTROLLER + " <controller_id> commission <pairing_code>", "Commission a new node"),
                buildCommandUsage(CONTROLLER + " <controller_id> decommission <node_id>", "Decommission a node"),
                buildCommandUsage(CONTROLLER + " <controller_id> resetStorage", "Reset controller storage"),
                buildCommandUsage(CONTROLLER + " <controller_id> sessionInfo", "List session information"),
                buildCommandUsage(CONTROLLER + " <controller_id> rpc <command>", "Execute RPC command"),
                buildCommandUsage(BRIDGE + " fabrics", "List all fabrics"),
                buildCommandUsage(BRIDGE + " removeFabric <fabric_id>", "Remove a fabric"),
                buildCommandUsage(BRIDGE + " allowCommissioning <true|false>", "Set commissioning mode"),
                buildCommandUsage(BRIDGE + " resetStorage",
                        "Reset bridge storage (WARNING: This will delete all fabrics!)"),
                buildCommandUsage(BRIDGE + " rpc <command>", "Execute RPC command"),
                buildCommandUsage(COMMON + " restartNode", "Restart the NodeJs server"));
    }
}
