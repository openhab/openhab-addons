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
package org.openhab.binding.linky.internal.console;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.linky.internal.LinkyBindingConstants;
import org.openhab.binding.linky.internal.handler.LinkyHandler;
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
 * The {@link LinkyCommandExtension} is responsible for handling console commands
 *
 * @author Laurent Garnier - Initial contribution
 */

@NonNullByDefault
@Component(service = ConsoleCommandExtension.class)
public class LinkyCommandExtension extends AbstractConsoleCommandExtension implements ConsoleCommandCompleter {

    private static final String REPORT = "report";
    private static final StringsCompleter SUBCMD_COMPLETER = new StringsCompleter(List.of(REPORT), false);

    private final ThingRegistry thingRegistry;

    @Activate
    public LinkyCommandExtension(final @Reference ThingRegistry thingRegistry) {
        super(LinkyBindingConstants.BINDING_ID, "Interact with the Linky binding.");
        this.thingRegistry = thingRegistry;
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length >= 2) {
            Thing thing = getThing(args[0]);
            ThingHandler thingHandler = null;
            LinkyHandler handler = null;
            if (thing != null) {
                thingHandler = thing.getHandler();
                if (thingHandler instanceof LinkyHandler) {
                    handler = (LinkyHandler) thingHandler;
                }
            }
            if (thing == null) {
                console.println(String.format("Bad thing id '%s'", args[0]));
                printUsage(console);
            } else if (thingHandler == null) {
                console.println(String.format("No handler initialized for the thing id '%s'", args[0]));
                printUsage(console);
            } else if (handler == null) {
                console.println(String.format("'%s' is not a Linky thing id", args[0]));
                printUsage(console);
            } else if (REPORT.equals(args[1])) {
                LocalDate yesterday = LocalDate.now().minusDays(1);
                LocalDate start = yesterday.minusDays(6);
                LocalDate end = yesterday;
                String separator = " ";
                if (args.length >= 3) {
                    try {
                        start = LocalDate.parse(args[2], DateTimeFormatter.ISO_LOCAL_DATE);
                    } catch (DateTimeParseException e) {
                        console.println(String
                                .format("Invalid format for start day '%s'; expected format is YYYY-MM-DD", args[2]));
                        printUsage(console);
                        return;
                    }
                }
                if (args.length >= 4) {
                    try {
                        end = LocalDate.parse(args[3], DateTimeFormatter.ISO_LOCAL_DATE);
                    } catch (DateTimeParseException e) {
                        console.println(String.format("Invalid format for end day '%s'; expected format is YYYY-MM-DD",
                                args[3]));
                        printUsage(console);
                        return;
                    }
                }
                if (start.isAfter(yesterday) || start.isAfter(end)) {
                    console.println("Start day must be in the past and before the end day");
                    printUsage(console);
                    return;
                }
                if (end.isAfter(yesterday)) {
                    end = yesterday;
                }
                if (args.length >= 5) {
                    separator = args[4];
                }
                handler.reportValues(start, end, separator).forEach(console::println);
            } else {
                printUsage(console);
            }
        } else {
            printUsage(console);
        }
    }

    @Override
    public List<String> getUsages() {
        return Arrays
                .asList(buildCommandUsage(String.format("<thingUID> %s <start day> <end day> [<separator>]", REPORT),
                        "report daily consumptions between two dates"));
    }

    @Override
    public @Nullable ConsoleCommandCompleter getCompleter() {
        return this;
    }

    @Override
    public boolean complete(String[] args, int cursorArgumentIndex, int cursorPosition, List<String> candidates) {
        if (cursorArgumentIndex <= 0) {
            return new StringsCompleter(thingRegistry.getAll().stream()
                    .filter(t -> LinkyBindingConstants.THING_TYPE_LINKY.equals(t.getThingTypeUID()))
                    .map(t -> t.getUID().getAsString()).collect(Collectors.toList()), true).complete(args,
                            cursorArgumentIndex, cursorPosition, candidates);
        } else if (cursorArgumentIndex == 1) {
            Thing thing = getThing(args[0]);
            if (thing != null && LinkyBindingConstants.THING_TYPE_LINKY.equals(thing.getThingTypeUID())) {
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
