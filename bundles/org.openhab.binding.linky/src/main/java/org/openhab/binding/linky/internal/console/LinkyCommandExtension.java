/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.console.Console;
import org.eclipse.smarthome.io.console.extensions.AbstractConsoleCommandExtension;
import org.eclipse.smarthome.io.console.extensions.ConsoleCommandExtension;
import org.openhab.binding.linky.internal.handler.LinkyHandler;
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
public class LinkyCommandExtension extends AbstractConsoleCommandExtension {

    private static final String REPORT = "report";

    private final ThingRegistry thingRegistry;

    @Activate
    public LinkyCommandExtension(final @Reference ThingRegistry thingRegistry) {
        super("linky", "Interact with the Linky binding.");
        this.thingRegistry = thingRegistry;
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length >= 2) {
            LinkyHandler handler = null;
            try {
                ThingUID thingUID = new ThingUID(args[0]);
                Thing thing = thingRegistry.get(thingUID);
                if ((thing != null) && (thing.getHandler() != null) && (thing.getHandler() instanceof LinkyHandler)) {
                    handler = (LinkyHandler) thing.getHandler();
                }
            } catch (IllegalArgumentException e) {
                handler = null;
            }
            if (handler == null) {
                console.println("Bad thing id '" + args[0] + "'");
                printUsage(console);
            } else if (REPORT.equals(args[1])) {
                LocalDate now = LocalDate.now();
                LocalDate start = now.minusDays(7);
                LocalDate end = now.minusDays(1);
                String separator = " ";
                if (args.length >= 3) {
                    try {
                        start = LocalDate.parse(args[2], DateTimeFormatter.ISO_LOCAL_DATE);
                    } catch (DateTimeParseException e) {
                        console.println(
                                "Invalid format for start day '" + args[2] + "'; expected format is YYYY-MM-DD");
                        printUsage(console);
                        return;
                    }
                }
                if (args.length >= 4) {
                    try {
                        end = LocalDate.parse(args[3], DateTimeFormatter.ISO_LOCAL_DATE);
                    } catch (DateTimeParseException e) {
                        console.println("Invalid format for end day '" + args[3] + "'; expected format is YYYY-MM-DD");
                        printUsage(console);
                        return;
                    }
                }
                if (!start.isBefore(now) || start.isAfter(end)) {
                    console.println("Start day must be in the past and before the end day");
                    printUsage(console);
                    return;
                }
                if (end.isAfter(now.minusDays(1))) {
                    end = now.minusDays(1);
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
        return Arrays.asList(
                new String[] { buildCommandUsage("<thingUID> " + REPORT + " <start day> <end day> [<separator>]",
                        "report daily consumptions between two dates") });
    }
}
