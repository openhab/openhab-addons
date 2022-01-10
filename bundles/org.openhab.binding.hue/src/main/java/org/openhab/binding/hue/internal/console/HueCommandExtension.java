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
package org.openhab.binding.hue.internal.console;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.hue.internal.handler.HueBridgeHandler;
import org.openhab.binding.hue.internal.handler.HueGroupHandler;
import org.openhab.core.io.console.Console;
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
 * The {@link HueCommandExtension} is responsible for handling console commands
 *
 * @author Laurent Garnier - Initial contribution
 */

@NonNullByDefault
@Component(service = ConsoleCommandExtension.class)
public class HueCommandExtension extends AbstractConsoleCommandExtension {

    private static final String USER_NAME = "username";
    private static final String SCENES = "scenes";

    private final ThingRegistry thingRegistry;

    @Activate
    public HueCommandExtension(final @Reference ThingRegistry thingRegistry) {
        super("hue", "Interact with the hue binding.");
        this.thingRegistry = thingRegistry;
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length == 2) {
            Thing thing = null;
            try {
                ThingUID thingUID = new ThingUID(args[0]);
                thing = thingRegistry.get(thingUID);
            } catch (IllegalArgumentException e) {
                thing = null;
            }
            ThingHandler thingHandler = null;
            HueBridgeHandler bridgeHandler = null;
            HueGroupHandler groupHandler = null;
            if (thing != null) {
                thingHandler = thing.getHandler();
                if (thingHandler instanceof HueBridgeHandler) {
                    bridgeHandler = (HueBridgeHandler) thingHandler;
                } else if (thingHandler instanceof HueGroupHandler) {
                    groupHandler = (HueGroupHandler) thingHandler;
                }
            }
            if (thing == null) {
                console.println("Bad thing id '" + args[0] + "'");
                printUsage(console);
            } else if (thingHandler == null) {
                console.println("No handler initialized for the thingUID '" + args[0] + "'");
                printUsage(console);
            } else if (bridgeHandler == null && groupHandler == null) {
                console.println("'" + args[0] + "' is neither a Hue bridgeUID nor a Hue groupThingUID");
                printUsage(console);
            } else {
                switch (args[1]) {
                    case USER_NAME:
                        if (bridgeHandler != null) {
                            String userName = bridgeHandler.getUserName();
                            console.println("Your user name is " + (userName != null ? userName : "undefined"));
                        } else {
                            console.println("'" + args[0] + "' is not a Hue bridgeUID");
                            printUsage(console);
                        }
                        break;
                    case SCENES:
                        if (bridgeHandler != null) {
                            bridgeHandler.listScenesForConsole().forEach(console::println);
                        } else if (groupHandler != null) {
                            groupHandler.listScenesForConsole().forEach(console::println);
                        }
                        break;
                    default:
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
        return Arrays.asList(new String[] { buildCommandUsage("<bridgeUID> " + USER_NAME, "show the user name"),
                buildCommandUsage("<bridgeUID> " + SCENES, "list all the scenes with their id"),
                buildCommandUsage("<groupThingUID> " + SCENES, "list all the scenes from this group with their id") });
    }
}
