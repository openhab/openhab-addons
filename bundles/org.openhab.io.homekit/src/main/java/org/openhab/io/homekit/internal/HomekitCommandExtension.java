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
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.ConsoleCommandCompleter;
import org.openhab.core.io.console.StringsCompleter;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.openhab.io.homekit.Homekit;
import org.openhab.io.homekit.internal.accessories.DummyHomekitAccessory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.hapjava.services.Service;

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
    private static final String SUBCMD_PRUNE_DUMMY_ACCESSORIES = "pruneDummyAccessories";
    private static final String SUBCMD_LIST_DUMMY_ACCESSORIES = "listDummyAccessories";
    private static final StringsCompleter SUBCMD_COMPLETER = new StringsCompleter(
            List.of(SUBCMD_CLEAR_PAIRINGS, SUBCMD_LIST_ACCESSORIES, SUBCMD_PRINT_ACCESSORY,
                    SUBCMD_ALLOW_UNAUTHENTICATED, SUBCMD_PRUNE_DUMMY_ACCESSORIES, SUBCMD_LIST_DUMMY_ACCESSORIES),
            false);

    private class CommandCompleter implements ConsoleCommandCompleter {
        public boolean complete(String[] args, int cursorArgumentIndex, int cursorPosition, List<String> candidates) {
            if (cursorArgumentIndex == 0) {
                boolean result = SUBCMD_COMPLETER.complete(args, cursorArgumentIndex, cursorPosition, candidates);
                return result;
            }
            return false;
        }
    }

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
                case SUBCMD_PRUNE_DUMMY_ACCESSORIES:
                    pruneDummyAccessories(console);
                    break;
                case SUBCMD_LIST_DUMMY_ACCESSORIES:
                    listDummyAccessories(console);
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
                        "enables or disables unauthenticated access to facilitate debugging"),
                buildCommandUsage(SUBCMD_PRUNE_DUMMY_ACCESSORIES,
                        "removes dummy accessories whose items no longer exist."),
                buildCommandUsage(SUBCMD_LIST_DUMMY_ACCESSORIES,
                        "list dummy accessories whose items no longer exist."));
    }

    @Reference
    public void setHomekit(Homekit homekit) {
        this.homekit = homekit;
    }

    @Override
    public @Nullable ConsoleCommandCompleter getCompleter() {
        return new CommandCompleter();
    }

    private void clearHomekitPairings(Console console) {
        homekit.clearHomekitPairings();
        console.println("Cleared HomeKit pairings");
    }

    private void allowUnauthenticatedHomekitRequests(boolean allow, Console console) {
        homekit.allowUnauthenticatedRequests(allow);
        console.println((allow ? "Enabled " : "Disabled ") + "unauthenticated HomeKit access");
    }

    private void pruneDummyAccessories(Console console) {
        homekit.pruneDummyAccessories();
        console.println("Dummy accessories pruned.");
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

    private void listDummyAccessories(Console console) {
        homekit.getAccessories().forEach(v -> {
            try {
                if (v instanceof DummyHomekitAccessory) {
                    console.println(v.getSerialNumber().get());
                }
            } catch (InterruptedException | ExecutionException e) {
                logger.warn("Cannot list accessories", e);
            }
        });
    }

    private void printService(Console console, Service service, int indent) {
        console.println(" ".repeat(indent) + "Service Type: " + service.getClass().getSimpleName() + " ("
                + service.getType() + ")");
        console.println(" ".repeat(indent + 2) + "Characteristics:");
        service.getCharacteristics().forEach((c) -> {
            try {
                console.println(
                        " ".repeat(indent + 4) + c.getClass().getSimpleName() + ": " + c.toJson(0).get().toString());
            } catch (InterruptedException | ExecutionException e) {
            }
        });
        if (service.getLinkedServices().isEmpty()) {
            return;
        }
        console.println(" ".repeat(indent + 2) + "Linked Services:");
        service.getLinkedServices().forEach((s) -> printService(console, s, indent + 2));
    }

    private void printAccessory(String id, Console console) {
        homekit.getAccessories().forEach(v -> {
            try {
                if (("" + v.getId()).contains(id) || ((v.getName().get() != null)
                        && (v.getName().get().toUpperCase().contains(id.toUpperCase())))) {
                    console.println(v.getId() + " " + v.getName().get());
                    console.println("Services:");
                    v.getServices().forEach(s -> printService(console, s, 2));
                    console.println("");
                }
            } catch (InterruptedException | ExecutionException e) {
                logger.warn("Cannot print accessory", e);
            }
        });
    }
}
