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
package org.openhab.binding.hue.internal.console;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.HueBindingConstants;
import org.openhab.binding.hue.internal.handler.Clip2BridgeHandler;
import org.openhab.binding.hue.internal.handler.HueBridgeHandler;
import org.openhab.binding.hue.internal.handler.HueGroupHandler;
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
 * The {@link HueCommandExtension} is responsible for handling console commands
 *
 * @author Laurent Garnier - Initial contribution
 * @author Andrew Fiddian-Green - Added CLIP 2 console commands
 */

@NonNullByDefault
@Component(service = ConsoleCommandExtension.class)
public class HueCommandExtension extends AbstractConsoleCommandExtension implements ConsoleCommandCompleter {

    private static final String USER_NAME = "username";
    private static final String SCENES = "scenes";
    private static final String APPLICATION_KEY = "applicationkey";
    private static final String DEVICES = "devices";
    private static final StringsCompleter SUBCMD_COMPLETER = new StringsCompleter(
            List.of(USER_NAME, SCENES, APPLICATION_KEY, DEVICES), false);
    private static final StringsCompleter SCENES_COMPLETER = new StringsCompleter(List.of(SCENES), false);

    private final ThingRegistry thingRegistry;

    @Activate
    public HueCommandExtension(final @Reference ThingRegistry thingRegistry) {
        super("hue", "Interact with the Hue binding.");
        this.thingRegistry = thingRegistry;
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length == 2) {
            Thing thing = getThing(args[0]);
            ThingHandler thingHandler = null;
            HueBridgeHandler bridgeHandler = null;
            HueGroupHandler groupHandler = null;
            Clip2BridgeHandler clip2BridgeHandler = null;
            if (thing != null) {
                thingHandler = thing.getHandler();
                if (thingHandler instanceof Clip2BridgeHandler) {
                    clip2BridgeHandler = (Clip2BridgeHandler) thingHandler;
                } else if (thingHandler instanceof HueBridgeHandler) {
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
            } else if (bridgeHandler == null && groupHandler == null && clip2BridgeHandler == null) {
                console.println("'" + args[0] + "' is neither a Hue BridgeUID nor a Hue groupThingUID");
                printUsage(console);
            } else {
                if (bridgeHandler != null) {
                    switch (args[1]) {
                        case USER_NAME:
                            String userName = bridgeHandler.getUserName();
                            console.println("Your user name is " + (userName != null ? userName : "undefined"));
                            return;
                        case SCENES:
                            bridgeHandler.listScenesForConsole().forEach(console::println);
                            return;
                    }
                } else if (groupHandler != null) {
                    switch (args[1]) {
                        case SCENES:
                            groupHandler.listScenesForConsole().forEach(console::println);
                            return;
                    }
                } else if (clip2BridgeHandler != null) {
                    switch (args[1]) {
                        case APPLICATION_KEY:
                            console.println(clip2BridgeHandler.consoleGetApplicationKey());
                            return;
                        case SCENES:
                            clip2BridgeHandler.consoleGetScenes().forEach(console::println);
                            return;
                        case DEVICES:
                            clip2BridgeHandler.consoleGetDevices().forEach(console::println);
                            return;
                    }
                }
            }
        }
        printUsage(console);
    }

    @Override
    public List<String> getUsages() {
        return Arrays.asList(new String[] { buildCommandUsage("<bridgeUID> " + USER_NAME, "show the user name"),
                buildCommandUsage("<bridgeUID> " + APPLICATION_KEY, "show the CLIP 2 application key"),
                buildCommandUsage("<bridgeUID> " + SCENES, "list all the scenes with their id"),
                buildCommandUsage("<bridgeUID> " + DEVICES, "list all the CLIP 2 devices with their id"),
                buildCommandUsage("<groupThingUID> " + SCENES, "list all the scenes from this group with their id") });
    }

    @Override
    public @Nullable ConsoleCommandCompleter getCompleter() {
        return this;
    }

    @Override
    public boolean complete(String[] args, int cursorArgumentIndex, int cursorPosition, List<String> candidates) {
        if (cursorArgumentIndex <= 0) {
            return new StringsCompleter(thingRegistry.getAll().stream()
                    .filter(t -> HueBindingConstants.THING_TYPE_BRIDGE.equals(t.getThingTypeUID())
                            || HueBindingConstants.THING_TYPE_CLIP2.equals(t.getThingTypeUID())
                            || HueBindingConstants.THING_TYPE_GROUP.equals(t.getThingTypeUID()))
                    .map(t -> t.getUID().getAsString()).collect(Collectors.toList()), true).complete(args,
                            cursorArgumentIndex, cursorPosition, candidates);
        } else if (cursorArgumentIndex == 1) {
            Thing thing = getThing(args[0]);
            if (thing != null && (HueBindingConstants.THING_TYPE_BRIDGE.equals(thing.getThingTypeUID())
                    || HueBindingConstants.THING_TYPE_CLIP2.equals(thing.getThingTypeUID()))) {
                return SUBCMD_COMPLETER.complete(args, cursorArgumentIndex, cursorPosition, candidates);
            } else if (thing != null && HueBindingConstants.THING_TYPE_GROUP.equals(thing.getThingTypeUID())) {
                return SCENES_COMPLETER.complete(args, cursorArgumentIndex, cursorPosition, candidates);
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
