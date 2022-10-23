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
package org.openhab.binding.powermax.internal.console;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.powermax.internal.PowermaxBindingConstants;
import org.openhab.binding.powermax.internal.handler.PowermaxBridgeHandler;
import org.openhab.binding.powermax.internal.state.PowermaxState;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.ConsoleCommandCompleter;
import org.openhab.core.io.console.StringsCompleter;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link PowermaxCommandExtension} is responsible for handling console commands
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
@Component(service = ConsoleCommandExtension.class)
public class PowermaxCommandExtension extends AbstractConsoleCommandExtension implements ConsoleCommandCompleter {

    private static final String INFO_SETUP = "info_setup";
    private static final String DOWNLOAD_SETUP = "download_setup";
    private static final String BRIDGE_STATE = "bridge_state";
    private static final StringsCompleter SUBCMD_COMPLETER = new StringsCompleter(
            List.of(INFO_SETUP, DOWNLOAD_SETUP, BRIDGE_STATE), false);

    private final ThingRegistry thingRegistry;

    @Activate
    public PowermaxCommandExtension(final @Reference ThingRegistry thingRegistry) {
        super("powermax", "Interact with the Powermax binding.");
        this.thingRegistry = thingRegistry;
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length >= 2) {
            Thing thing = getThing(args[0]);
            ThingHandler thingHandler = null;
            PowermaxBridgeHandler handler = null;
            if (thing != null) {
                thingHandler = thing.getHandler();
                if (thingHandler instanceof PowermaxBridgeHandler) {
                    handler = (PowermaxBridgeHandler) thingHandler;
                }
            }
            if (thing == null) {
                console.println("Bad thing id '" + args[0] + "'");
                printUsage(console);
            } else if (thingHandler == null) {
                console.println("No handler initialized for the thing id '" + args[0] + "'");
                printUsage(console);
            } else if (handler == null) {
                console.println("'" + args[0] + "' is not a powermax bridge id");
                printUsage(console);
            } else {
                switch (args[1]) {
                    case INFO_SETUP:
                        for (String line : handler.getInfoSetup().split("\n")) {
                            console.println(line);
                        }
                        break;
                    case DOWNLOAD_SETUP:
                        handler.downloadSetup();
                        console.println("Command '" + args[1] + "' handled.");
                        break;
                    case BRIDGE_STATE:
                        PowermaxState state = handler.getCurrentState();
                        if (state != null) {
                            for (String line : state.toString().split("\n")) {
                                console.println(line);
                            }
                        }
                        break;
                    default:
                        console.println("Unknown Powermax sub command '" + args[1] + "'");
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
        return Arrays.asList(new String[] { buildCommandUsage("<bridgeUID> " + INFO_SETUP, "information on setup"),
                buildCommandUsage("<bridgeUID> " + DOWNLOAD_SETUP, "download setup"),
                buildCommandUsage("<bridgeUID> " + BRIDGE_STATE, "show current state") });
    }

    @Override
    public @Nullable ConsoleCommandCompleter getCompleter() {
        return this;
    }

    @Override
    public boolean complete(String[] args, int cursorArgumentIndex, int cursorPosition, List<String> candidates) {
        if (cursorArgumentIndex <= 0) {
            return new StringsCompleter(thingRegistry.getAll().stream()
                    .filter(t -> PowermaxBindingConstants.BRIDGE_TYPE_SERIAL.equals(t.getThingTypeUID())
                            || PowermaxBindingConstants.BRIDGE_TYPE_IP.equals(t.getThingTypeUID()))
                    .map(t -> t.getUID().getAsString()).collect(Collectors.toList()), true).complete(args,
                            cursorArgumentIndex, cursorPosition, candidates);
        } else if (cursorArgumentIndex == 1) {
            Thing thing = getThing(args[0]);
            if (thing != null && (PowermaxBindingConstants.BRIDGE_TYPE_SERIAL.equals(thing.getThingTypeUID())
                    || PowermaxBindingConstants.BRIDGE_TYPE_IP.equals(thing.getThingTypeUID()))) {
                return SUBCMD_COMPLETER.complete(args, cursorArgumentIndex, cursorPosition, candidates);
            }
        }
        return false;
    }

    private @Nullable Thing getThing(String uid) {
        Thing thing = null;
        try {
            ThingUID thingUID = new ThingUID(uid);
            thing = thingRegistry.get(thingUID);
        } catch (IllegalArgumentException e) {
            thing = null;
        }
        return thing;
    }
}
