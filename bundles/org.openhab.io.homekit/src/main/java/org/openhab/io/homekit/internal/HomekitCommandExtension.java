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
package org.openhab.io.homekit.internal;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.openhab.io.homekit.Homekit;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Console commands for interacting with the HomeKit integration
 *
 * @author Andy Lintner - Initial contribution
 */
@Component(service = ConsoleCommandExtension.class)
@NonNullByDefault
public class HomekitCommandExtension extends AbstractConsoleCommandExtension {
    private static final String SUBCMD_CLEAR_PAIRINGS = "clearPairings";
    private static final String SUBCMD_LIST_ACCESSORIES = "list";
    private static final String SUBCMD_PRINT_ACCESSORY = "show";
    private static final String SUBCMD_ALLOW_UNAUTHENTICATED = "allowUnauthenticated";

    private final Logger logger = LoggerFactory.getLogger(HomekitCommandExtension.class);

    private @NonNullByDefault({}) Homekit homekit;

    public HomekitCommandExtension() {
        super("homekit", "Interact with the HomeKit integration.");
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length > 0) {
            String subCommand = args[0];
            switch (subCommand) {
                case SUBCMD_CLEAR_PAIRINGS:
                    clearHomekitPairings(console);
                    break;

                case SUBCMD_ALLOW_UNAUTHENTICATED:
                    if (args.length > 1) {
                        boolean allow = Boolean.parseBoolean(args[1]);
                        allowUnauthenticatedHomekitRequests(allow, console);
                    } else {
                        console.println("true/false is required as an argument");
                    }
                    break;
                case SUBCMD_LIST_ACCESSORIES:
                    listAccessories(console);
                    break;
                case SUBCMD_PRINT_ACCESSORY:
                    if (args.length > 1) {
                        printAccessory(args[1], console);
                    } else {
                        console.println("accessory id or name is required as an argument");
                    }
                    break;
                default:
                    console.println("Unknown command '" + subCommand + "'");
                    printUsage(console);
                    break;
            }
        } else {
            printUsage(console);
        }
    }

    @Override
    public List<String> getUsages() {
        return Arrays.asList(buildCommandUsage(SUBCMD_LIST_ACCESSORIES, "list all HomeKit accessories"),
                buildCommandUsage(SUBCMD_PRINT_ACCESSORY + " <accessory id | accessory name>",
                        "print additional details of the accessories which partially match provided ID or name."),
                buildCommandUsage(SUBCMD_CLEAR_PAIRINGS, "removes all pairings with HomeKit clients."),
                buildCommandUsage(SUBCMD_ALLOW_UNAUTHENTICATED + " <boolean>",
                        "enables or disables unauthenticated access to facilitate debugging"));
    }

    @Reference
    public void setHomekit(Homekit homekit) {
        this.homekit = homekit;
    }

    private void clearHomekitPairings(Console console) {
        homekit.clearHomekitPairings();
        console.println("Cleared HomeKit pairings");
    }

    private void allowUnauthenticatedHomekitRequests(boolean allow, Console console) {
        homekit.allowUnauthenticatedRequests(allow);
        console.println((allow ? "Enabled " : "Disabled ") + "unauthenticated HomeKit access");
    }

    private void listAccessories(Console console) {
        homekit.getAccessories().forEach(v -> {
            try {
                console.println(v.getId() + " " + v.getName().get());
            } catch (InterruptedException | ExecutionException e) {
                logger.warn("Cannot list accessories", e);
            }
        });
    }

    private void printAccessory(String id, Console console) {
        homekit.getAccessories().forEach(v -> {
            try {
                if (("" + v.getId()).contains(id) || ((v.getName().get() != null)
                        && (v.getName().get().toUpperCase().contains(id.toUpperCase())))) {
                    console.println(v.getId() + " " + v.getName().get());
                    console.println("Services:");
                    v.getServices().forEach(s -> {
                        console.println("    Service Type: " + s.getType());
                        console.println("    Characteristics: ");
                        s.getCharacteristics().forEach(c -> console.println("      : " + c.getClass()));
                    });
                    console.println("");
                }
            } catch (InterruptedException | ExecutionException e) {
                logger.warn("Cannot print accessory", e);
            }
        });
    }
}
