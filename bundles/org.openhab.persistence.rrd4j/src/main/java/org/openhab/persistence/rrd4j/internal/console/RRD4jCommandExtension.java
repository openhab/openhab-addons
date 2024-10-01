/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.persistence.rrd4j.internal.console;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.ConsoleCommandCompleter;
import org.openhab.core.io.console.StringsCompleter;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.persistence.PersistenceServiceRegistry;
import org.openhab.persistence.rrd4j.internal.RRD4jPersistenceService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link RRD4jCommandExtension} is responsible for handling console commands
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
@Component(service = ConsoleCommandExtension.class)
public class RRD4jCommandExtension extends AbstractConsoleCommandExtension implements ConsoleCommandCompleter {

    private static final String CMD_LIST = "list";
    private static final String CMD_CHECK = "check";
    private static final String CMD_CLEAN = "clean";
    private static final StringsCompleter CMD_COMPLETER = new StringsCompleter(List.of(CMD_LIST, CMD_CHECK, CMD_CLEAN),
            false);

    private final PersistenceServiceRegistry persistenceServiceRegistry;
    private final ItemRegistry itemRegistry;

    @Activate
    public RRD4jCommandExtension(final @Reference PersistenceServiceRegistry persistenceServiceRegistry,
            final @Reference ItemRegistry itemRegistry) {
        super(RRD4jPersistenceService.SERVICE_ID, "Interact with the RRD4j persistence service.");
        this.persistenceServiceRegistry = persistenceServiceRegistry;
        this.itemRegistry = itemRegistry;
    }

    @Override
    public void execute(String[] args, Console console) {
        RRD4jPersistenceService persistenceService = getPersistenceService();
        if (persistenceService == null) {
            console.println("No RRD4j persistence service installed:");
            return;
        }
        if (args.length == 1 && CMD_LIST.equalsIgnoreCase(args[0])) {
            List<String> filenames = persistenceService.getRrdFiles();
            Collections.sort(filenames, Comparator.naturalOrder());
            console.println("Existing RRD files...");
            filenames.forEach(filename -> console.println("  - " + filename));
            console.println(filenames.size() + " files found.");
            return;
        } else if (args.length == 1 && CMD_CHECK.equalsIgnoreCase(args[0])) {
            checkAndClean(persistenceService, console, null, true);
            return;
        } else if (args.length >= 1 && args.length <= 2 && CMD_CLEAN.equalsIgnoreCase(args[0])) {
            checkAndClean(persistenceService, console, args.length == 2 ? args[1] : null, false);
            return;
        }
        printUsage(console);
    }

    private @Nullable RRD4jPersistenceService getPersistenceService() {
        for (PersistenceService persistenceService : persistenceServiceRegistry.getAll()) {
            if (persistenceService instanceof RRD4jPersistenceService service) {
                return service;
            }
        }
        return null;
    }

    private void checkAndClean(RRD4jPersistenceService persistenceService, Console console, @Nullable String itemName,
            boolean checkOnly) {
        List<String> filenames;
        if (itemName != null) {
            filenames = List.of(itemName + ".rrd");
        } else {
            filenames = persistenceService.getRrdFiles();
            Collections.sort(filenames, Comparator.naturalOrder());
        }

        console.println((checkOnly ? "Checking" : "Cleaning") + " RRD files...");
        int nb = 0;
        for (String filename : filenames) {
            String name = filename.substring(0, filename.lastIndexOf(".rrd"));
            Path path = RRD4jPersistenceService.getDatabasePath(name);
            if (!Files.exists(path)) {
                console.println("  - " + filename + ": file not found");
            } else {
                boolean itemFound;
                try {
                    itemRegistry.getItem(name);
                    itemFound = true;
                } catch (ItemNotFoundException e) {
                    itemFound = false;
                }
                if (itemFound) {
                    continue;
                }
                if (checkOnly) {
                    console.println("  - " + filename + ": no item found");
                    nb++;
                } else if (path.toFile().delete()) {
                    console.println("  - " + filename + ": file deleted");
                    nb++;
                } else {
                    console.println("  - " + filename + ": file deletion failed!");
                }
            }
        }
        console.println(nb + " files " + (checkOnly ? "to delete." : "deleted."));
    }

    @Override
    public List<String> getUsages() {
        return List.of(buildCommandUsage(CMD_LIST, "list Round Robin Database files"),
                buildCommandUsage(CMD_CHECK, "check for RRD files without existing item"),
                buildCommandUsage(CMD_CLEAN + " [<itemName>]", "delete RRD files without existing item"));
    }

    @Override
    public @Nullable ConsoleCommandCompleter getCompleter() {
        return this;
    }

    @Override
    public boolean complete(String[] args, int cursorArgumentIndex, int cursorPosition, List<String> candidates) {
        if (cursorArgumentIndex <= 0) {
            return CMD_COMPLETER.complete(args, cursorArgumentIndex, cursorPosition, candidates);
        } else if (cursorArgumentIndex == 1) {
            if (CMD_CLEAN.equalsIgnoreCase(args[0])) {
                RRD4jPersistenceService persistenceService = getPersistenceService();
                if (persistenceService != null) {
                    List<String> filenames = persistenceService.getRrdFiles().stream()
                            .map(filename -> filename.substring(0, filename.lastIndexOf(".rrd")))
                            .collect(Collectors.toList());
                    return new StringsCompleter(filenames, true).complete(args, cursorArgumentIndex, cursorPosition,
                            candidates);
                }
            }
        }
        return false;
    }
}
