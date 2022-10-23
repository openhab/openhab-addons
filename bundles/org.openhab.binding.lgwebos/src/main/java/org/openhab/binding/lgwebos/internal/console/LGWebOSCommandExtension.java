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
package org.openhab.binding.lgwebos.internal.console;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgwebos.internal.LGWebOSBindingConstants;
import org.openhab.binding.lgwebos.internal.handler.LGWebOSHandler;
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
 * The {@link LGWebOSCommandExtension} is responsible for handling console commands
 *
 * @author Laurent Garnier - Initial contribution
 */

@NonNullByDefault
@Component(service = ConsoleCommandExtension.class)
public class LGWebOSCommandExtension extends AbstractConsoleCommandExtension implements ConsoleCommandCompleter {

    private static final String APPLICATIONS = "applications";
    private static final String CHANNELS = "channels";
    private static final String ACCESS_KEY = "accesskey";
    private static final StringsCompleter SUBCMD_COMPLETER = new StringsCompleter(
            List.of(APPLICATIONS, CHANNELS, ACCESS_KEY), false);

    private final ThingRegistry thingRegistry;

    @Activate
    public LGWebOSCommandExtension(final @Reference ThingRegistry thingRegistry) {
        super("lgwebos", "Interact with the LG webOS binding.");
        this.thingRegistry = thingRegistry;
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length == 2) {
            Thing thing = getThing(args[0]);
            ThingHandler thingHandler = null;
            LGWebOSHandler handler = null;
            if (thing != null) {
                thingHandler = thing.getHandler();
                if (thingHandler instanceof LGWebOSHandler) {
                    handler = (LGWebOSHandler) thingHandler;
                }
            }
            if (thing == null) {
                console.println("Bad thing id '" + args[0] + "'");
                printUsage(console);
            } else if (thingHandler == null) {
                console.println("No handler initialized for the thing id '" + args[0] + "'");
                printUsage(console);
            } else if (handler == null) {
                console.println("'" + args[0] + "' is not a LG webOS thing id");
                printUsage(console);
            } else {
                switch (args[1]) {
                    case APPLICATIONS:
                        handler.reportApplications().forEach(console::println);
                        break;
                    case CHANNELS:
                        handler.reportChannels().forEach(console::println);
                        break;
                    case ACCESS_KEY:
                        console.println("Your access key is " + handler.getKey());
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
        return Arrays.asList(new String[] { buildCommandUsage("<thingUID> " + APPLICATIONS, "list applications"),
                buildCommandUsage("<thingUID> " + CHANNELS, "list channels"),
                buildCommandUsage("<thingUID> " + ACCESS_KEY, "show the access key") });
    }

    @Override
    public @Nullable ConsoleCommandCompleter getCompleter() {
        return this;
    }

    @Override
    public boolean complete(String[] args, int cursorArgumentIndex, int cursorPosition, List<String> candidates) {
        if (cursorArgumentIndex <= 0) {
            return new StringsCompleter(thingRegistry.getAll().stream()
                    .filter(t -> LGWebOSBindingConstants.THING_TYPE_WEBOSTV.equals(t.getThingTypeUID()))
                    .map(t -> t.getUID().getAsString()).collect(Collectors.toList()), true).complete(args,
                            cursorArgumentIndex, cursorPosition, candidates);
        } else if (cursorArgumentIndex == 1) {
            Thing thing = getThing(args[0]);
            if (thing != null && LGWebOSBindingConstants.THING_TYPE_WEBOSTV.equals(thing.getThingTypeUID())) {
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
