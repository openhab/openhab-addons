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
package org.openhab.persistence.jdbc.console;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.ConsoleCommandCompleter;
import org.openhab.core.io.console.StringsCompleter;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.persistence.PersistenceServiceRegistry;
import org.openhab.persistence.jdbc.ItemTableCheckEntry;
import org.openhab.persistence.jdbc.ItemTableCheckEntryStatus;
import org.openhab.persistence.jdbc.internal.JdbcPersistenceService;
import org.openhab.persistence.jdbc.internal.JdbcPersistenceServiceConstants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link JdbcCommandExtension} is responsible for handling console commands
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
@Component(service = ConsoleCommandExtension.class)
public class JdbcCommandExtension extends AbstractConsoleCommandExtension implements ConsoleCommandCompleter {

    private static final String CMD_TABLES = "tables";
    private static final String SUBCMD_TABLES_LIST = "list";
    private static final String SUBCMD_TABLES_CLEAN = "clean";
    private static final String PARAMETER_ALL = "all";
    private static final String PARAMETER_FORCE = "force";
    private static final StringsCompleter CMD_COMPLETER = new StringsCompleter(List.of(CMD_TABLES), false);
    private static final StringsCompleter SUBCMD_TABLES_COMPLETER = new StringsCompleter(
            List.of(SUBCMD_TABLES_LIST, SUBCMD_TABLES_CLEAN), false);

    private final PersistenceServiceRegistry persistenceServiceRegistry;

    @Activate
    public JdbcCommandExtension(final @Reference PersistenceServiceRegistry persistenceServiceRegistry) {
        super(JdbcPersistenceServiceConstants.SERVICE_ID, "Interact with the JDBC persistence service.");
        this.persistenceServiceRegistry = persistenceServiceRegistry;
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length < 2 || args.length > 4 || !CMD_TABLES.equals(args[0])) {
            printUsage(console);
            return;
        }
        JdbcPersistenceService persistenceService = getPersistenceService();
        if (persistenceService == null) {
            return;
        }
        if (SUBCMD_TABLES_LIST.equalsIgnoreCase(args[1])) {
            listTables(persistenceService, console, args.length == 3 && PARAMETER_ALL.equalsIgnoreCase(args[2]));
            return;
        } else if (SUBCMD_TABLES_CLEAN.equalsIgnoreCase(args[1])) {
            if (args.length == 3) {
                cleanupItem(persistenceService, console, args[2], false);
                return;
            } else if (args.length == 4 && PARAMETER_FORCE.equalsIgnoreCase(args[3])) {
                cleanupItem(persistenceService, console, args[2], true);
                return;
            } else {
                cleanupTables(persistenceService, console);
                return;
            }
        }
        printUsage(console);
    }

    private @Nullable JdbcPersistenceService getPersistenceService() {
        for (PersistenceService persistenceService : persistenceServiceRegistry.getAll()) {
            if (persistenceService instanceof JdbcPersistenceService) {
                return (JdbcPersistenceService) persistenceService;
            }
        }
        return null;
    }

    private void listTables(JdbcPersistenceService persistenceService, Console console, Boolean all) {
        List<ItemTableCheckEntry> entries = persistenceService.getCheckedEntries();
        if (!all) {
            entries.removeIf(t -> t.getStatus() == ItemTableCheckEntryStatus.VALID);
        }
        entries.sort(Comparator.comparing(ItemTableCheckEntry::getTableName));
        int itemNameMaxLength = Math
                .max(entries.stream().map(t -> t.getItemName().length()).max(Integer::compare).get(), 4);
        int tableNameMaxLength = Math
                .max(entries.stream().map(t -> t.getTableName().length()).max(Integer::compare).get(), 5);
        int statusMaxLength = Stream.of(ItemTableCheckEntryStatus.values()).map(t -> t.toString().length())
                .max(Integer::compare).get();
        console.println(String.format(
                "%1$-" + (tableNameMaxLength + 2) + "sRow Count  %2$-" + (itemNameMaxLength + 2) + "s%3$s", "Table",
                "Item", "Status"));
        console.println("-".repeat(tableNameMaxLength) + "  " + "---------  " + "-".repeat(itemNameMaxLength) + "  "
                + "-".repeat(statusMaxLength));
        for (ItemTableCheckEntry entry : entries) {
            String tableName = entry.getTableName();
            ItemTableCheckEntryStatus status = entry.getStatus();
            long rowCount = status == ItemTableCheckEntryStatus.VALID
                    || status == ItemTableCheckEntryStatus.ITEM_MISSING ? persistenceService.getRowCount(tableName) : 0;
            console.println(String.format(
                    "%1$-" + (tableNameMaxLength + 2) + "s%2$9d  %3$-" + (itemNameMaxLength + 2) + "s%4$s", tableName,
                    rowCount, entry.getItemName(), status));
        }
    }

    private void cleanupTables(JdbcPersistenceService persistenceService, Console console) {
        console.println("Cleaning up all inconsistent items...");
        List<ItemTableCheckEntry> entries = persistenceService.getCheckedEntries();
        entries.removeIf(t -> t.getStatus() == ItemTableCheckEntryStatus.VALID || t.getItemName().isEmpty());
        for (ItemTableCheckEntry entry : entries) {
            console.print(entry.getItemName() + " -> ");
            if (persistenceService.cleanupItem(entry)) {
                console.println("done.");
            } else {
                console.println("skipped/failed.");
            }
        }
    }

    private void cleanupItem(JdbcPersistenceService persistenceService, Console console, String itemName,
            boolean force) {
        console.print("Cleaning up item " + itemName + "... ");
        if (persistenceService.cleanupItem(itemName, force)) {
            console.println("done.");
        } else {
            console.println("skipped/failed.");
        }
    }

    @Override
    public List<String> getUsages() {
        return Arrays.asList(
                buildCommandUsage(CMD_TABLES + " " + SUBCMD_TABLES_LIST + " [" + PARAMETER_ALL + "]",
                        "list tables (all = include valid)"),
                buildCommandUsage(
                        CMD_TABLES + " " + SUBCMD_TABLES_CLEAN + " [<itemName>]" + " [" + PARAMETER_FORCE + "]",
                        "clean inconsistent items (remove from index and drop tables)"));
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
            if (CMD_TABLES.equalsIgnoreCase(args[0])) {
                return SUBCMD_TABLES_COMPLETER.complete(args, cursorArgumentIndex, cursorPosition, candidates);
            }
        } else if (cursorArgumentIndex == 2) {
            if (CMD_TABLES.equalsIgnoreCase(args[0])) {
                if (SUBCMD_TABLES_CLEAN.equalsIgnoreCase(args[1])) {
                    JdbcPersistenceService persistenceService = getPersistenceService();
                    if (persistenceService != null) {
                        return new StringsCompleter(persistenceService.getItemNames(), true).complete(args,
                                cursorArgumentIndex, cursorPosition, candidates);
                    }
                }
            }
        }
        return false;
    }
}
