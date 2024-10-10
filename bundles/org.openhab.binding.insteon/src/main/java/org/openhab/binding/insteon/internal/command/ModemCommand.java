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
package org.openhab.binding.insteon.internal.command;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.device.ProductData;
import org.openhab.binding.insteon.internal.device.database.ModemDBEntry;
import org.openhab.binding.insteon.internal.device.database.ModemDBRecord;
import org.openhab.binding.insteon.internal.handler.InsteonBridgeHandler;
import org.openhab.binding.insteon.internal.utils.HexUtils;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.StringsCompleter;

/**
 *
 * The {@link ModemCommand} represents an Insteon console modem command
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class ModemCommand extends InsteonCommand {
    private static final String NAME = "modem";
    private static final String DESCRIPTION = "Insteon modem commands";

    private static final String LIST_ALL = "listAll";
    private static final String LIST_DATABASE = "listDatabase";
    private static final String RELOAD_DATABASE = "reloadDatabase";
    private static final String ADD_DATABASE_CONTROLLER = "addDatabaseController";
    private static final String ADD_DATABASE_RESPONDER = "addDatabaseResponder";
    private static final String DELETE_DATABASE_RECORD = "deleteDatabaseRecord";
    private static final String APPLY_DATABASE_CHANGES = "applyDatabaseChanges";
    private static final String CLEAR_DATABASE_CHANGES = "clearDatabaseChanges";
    private static final String ADD_DEVICE = "addDevice";
    private static final String REMOVE_DEVICE = "removeDevice";
    private static final String SWITCH = "switch";

    private static final List<String> SUBCMDS = List.of(LIST_ALL, LIST_DATABASE, RELOAD_DATABASE,
            ADD_DATABASE_CONTROLLER, ADD_DATABASE_RESPONDER, DELETE_DATABASE_RECORD, APPLY_DATABASE_CHANGES,
            CLEAR_DATABASE_CHANGES, ADD_DEVICE, REMOVE_DEVICE, SWITCH);

    private static final String CONFIRM_OPTION = "--confirm";
    private static final String FORCE_OPTION = "--force";
    private static final String RECORDS_OPTION = "--records";

    public ModemCommand(InsteonCommandExtension commandExtension) {
        super(NAME, DESCRIPTION, commandExtension);
    }

    @Override
    public List<String> getUsages() {
        return List.of(
                buildCommandUsage(LIST_ALL, "list configured Insteon modem bridges with related channels and status"),
                buildCommandUsage(LIST_DATABASE + " [" + RECORDS_OPTION + "]",
                        "list all-link database summary or records and pending changes for the Insteon modem"),
                buildCommandUsage(RELOAD_DATABASE, "reload all-link database from the Insteon modem"),
                buildCommandUsage(ADD_DATABASE_CONTROLLER + " <address> <group> [<devCat> <subCat> <firmware>]",
                        "add a controller record to all-link database for the Insteon modem"),
                buildCommandUsage(ADD_DATABASE_RESPONDER + " <address> <group>",
                        "add a responder record to all-link database for the Insteon modem"),
                buildCommandUsage(DELETE_DATABASE_RECORD + " <address> <group>",
                        "delete a controller/responder record from all-link database for the Insteon modem"),
                buildCommandUsage(APPLY_DATABASE_CHANGES + " " + CONFIRM_OPTION,
                        "apply all-link database pending changes for the Insteon modem"),
                buildCommandUsage(CLEAR_DATABASE_CHANGES,
                        "clear all-link database pending changes for the Insteon modem"),
                buildCommandUsage(ADD_DEVICE + " [<address>]",
                        "add an Insteon device to the modem, optionally providing its address"),
                buildCommandUsage(REMOVE_DEVICE + " <address> [" + FORCE_OPTION + "]",
                        "remove an Insteon device from the modem"),
                buildCommandUsage(SWITCH + " <thingId>",
                        "switch Insteon modem bridge to use if more than one configured and enabled"));
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length == 0) {
            printUsage(console);
            return;
        }

        switch (args[0]) {
            case LIST_ALL:
                if (args.length == 1) {
                    listAll(console);
                } else {
                    printUsage(console, args[0]);
                }
                break;
            case LIST_DATABASE:
                if (args.length == 1) {
                    listDatabaseSummary(console);
                } else if (args.length == 2 && RECORDS_OPTION.equals(args[1])) {
                    listDatabaseRecords(console);
                } else {
                    printUsage(console, args[0]);
                }
                break;
            case RELOAD_DATABASE:
                if (args.length == 1) {
                    reloadDatabase(console);
                } else {
                    printUsage(console, args[0]);
                }
                break;
            case ADD_DATABASE_CONTROLLER:
                if (args.length == 3 || args.length == 6) {
                    addDatabaseRecord(console, args, true);
                } else {
                    printUsage(console, args[0]);
                }
                break;
            case ADD_DATABASE_RESPONDER:
                if (args.length == 3) {
                    addDatabaseRecord(console, args, false);
                } else {
                    printUsage(console, args[0]);
                }
                break;
            case DELETE_DATABASE_RECORD:
                if (args.length == 3) {
                    deleteDatabaseRecord(console, args);
                } else {
                    printUsage(console, args[0]);
                }
                break;
            case APPLY_DATABASE_CHANGES:
                if (args.length == 1 || args.length == 2 && CONFIRM_OPTION.equals(args[1])) {
                    applyDatabaseChanges(console, args.length == 2);
                } else {
                    printUsage(console, args[0]);
                }
                break;
            case CLEAR_DATABASE_CHANGES:
                if (args.length == 1) {
                    clearDatabaseChanges(console);
                } else {
                    printUsage(console, args[0]);
                }
                break;
            case ADD_DEVICE:
                if (args.length >= 1 && args.length <= 2) {
                    addDevice(console, args.length == 1 ? null : args[1]);
                } else {
                    printUsage(console, args[0]);
                }
                break;
            case REMOVE_DEVICE:
                if (args.length == 2 || args.length == 3 && FORCE_OPTION.equals(args[2])) {
                    removeDevice(console, args[1], args.length == 3);
                } else {
                    printUsage(console, args[0]);
                }
                break;
            case SWITCH:
                if (args.length == 2) {
                    switchModem(console, args[1]);
                } else {
                    printUsage(console, args[0]);
                }
                break;
            default:
                console.println("Unknown command '" + args[0] + "'");
                printUsage(console);
                break;
        }
    }

    @Override
    public boolean complete(String[] args, int cursorArgumentIndex, int cursorPosition, List<String> candidates) {
        List<String> strings = List.of();
        if (cursorArgumentIndex == 0) {
            strings = SUBCMDS;
        } else if (cursorArgumentIndex == 1) {
            switch (args[0]) {
                case LIST_DATABASE:
                    strings = List.of(RECORDS_OPTION);
                    break;
                case ADD_DATABASE_CONTROLLER:
                case ADD_DATABASE_RESPONDER:
                case REMOVE_DEVICE:
                    strings = getModem().getDB().getDevices().stream().map(InsteonAddress::toString).toList();
                    break;
                case DELETE_DATABASE_RECORD:
                    strings = getModem().getDB().getRecords().stream().map(record -> record.getAddress().toString())
                            .distinct().toList();
                    break;
                case SWITCH:
                    strings = getBridgeHandlers().map(InsteonBridgeHandler::getThingId).toList();
                    break;
            }
        } else if (cursorArgumentIndex == 2) {
            InsteonAddress address = InsteonAddress.isValid(args[1]) ? new InsteonAddress(args[1]) : null;
            switch (args[0]) {
                case DELETE_DATABASE_RECORD:
                    if (address != null) {
                        strings = getModem().getDB().getRecords(address).stream()
                                .map(record -> HexUtils.getHexString(record.getGroup())).distinct().toList();
                    }
                    break;
                case REMOVE_DEVICE:
                    strings = List.of(FORCE_OPTION);
                    break;
            }
        }

        return new StringsCompleter(strings, false).complete(args, cursorArgumentIndex, cursorPosition, candidates);
    }

    private void listAll(Console console) {
        Map<String, String> bridges = getBridgeHandlers()
                .collect(Collectors.toMap(InsteonBridgeHandler::getThingId, InsteonBridgeHandler::getThingInfo));
        if (bridges.isEmpty()) {
            console.println("No modem bridge configured or enabled!");
        } else {
            console.println("There are " + bridges.size() + " modem bridges configured:");
            print(console, bridges);
        }
    }

    private void listDatabaseSummary(Console console) {
        InsteonAddress address = getModem().getAddress();
        Map<String, String> entries = getModem().getDB().getEntries().stream()
                .collect(Collectors.toMap(ModemDBEntry::getId, ModemDBEntry::toString));
        if (InsteonAddress.UNKNOWN.equals(address)) {
            console.println("No modem found!");
        } else if (entries.isEmpty()) {
            console.println("The all-link database for modem " + address + " is empty");
        } else {
            console.println("The all-link database for modem " + address + " contains " + entries.size() + " devices:");
            print(console, entries);
        }
    }

    private void listDatabaseRecords(Console console) {
        InsteonAddress address = getModem().getAddress();
        List<String> records = getModem().getDB().getRecords().stream().map(ModemDBRecord::toString).toList();
        if (InsteonAddress.UNKNOWN.equals(address)) {
            console.println("No modem found!");
        } else if (records.isEmpty()) {
            console.println("The all-link database for modem " + address + " is empty");
        } else {
            console.println("The all-link database for modem " + address + " contains " + records.size() + " records:");
            print(console, records);
            listDatabaseChanges(console);
        }
    }

    private void listDatabaseChanges(Console console) {
        InsteonAddress address = getModem().getAddress();
        List<String> changes = getModem().getDB().getChanges().stream().map(String::valueOf).toList();
        if (InsteonAddress.UNKNOWN.equals(address)) {
            console.println("No modem found!");
        } else if (!changes.isEmpty()) {
            console.println(
                    "The all-link database for modem " + address + " has " + changes.size() + " pending changes:");
            print(console, changes);
        }
    }

    private void reloadDatabase(Console console) {
        InsteonAddress address = getModem().getAddress();
        InsteonBridgeHandler handler = getBridgeHandler();
        if (InsteonAddress.UNKNOWN.equals(address)) {
            console.println("No modem found!");
        } else {
            console.println("Reloading all-link database for modem " + address + ".");
            getModem().getDB().clear();
            handler.reset(0);
        }
    }

    private void addDatabaseRecord(Console console, String[] args, boolean isController) {
        if (!getModem().getDB().isComplete()) {
            console.println("The modem database is not loaded yet.");
        } else if (!InsteonAddress.isValid(args[1])) {
            console.println("Invalid record address argument: " + args[1]);
        } else if (!HexUtils.isValidHexString(args[2])) {
            console.println("Invalid record group hex argument: " + args[2]);
        } else if (isController && args.length == 6 && !HexUtils.isValidHexStringArray(args, 3, args.length)) {
            console.println("Invalid product data hex argument(s).");
        } else if (isController && args.length == 3
                && !getModem().getDB().hasProductData(new InsteonAddress(args[1]))) {
            console.println("No product data available for " + args[1] + ".");
        } else {
            InsteonAddress address = new InsteonAddress(args[1]);
            int group = HexUtils.toInteger(args[2]);
            byte data[] = new byte[3];
            if (isController) {
                ProductData productData = getModem().getDB().getProductData(address);
                if (args.length == 6) {
                    data = HexUtils.toByteArray(args, 3, args.length);
                } else if (args.length == 3 && productData != null) {
                    data = productData.getRecordData();
                }
            }

            ModemDBRecord record = getModem().getDB().getRecord(address, group, isController);
            if (record == null) {
                getModem().getDB().markRecordForAdd(address, group, isController, data);

            } else {
                getModem().getDB().markRecordForModify(record, data);
            }
            console.println("Added a pending change to " + (record == null ? "add" : "modify") + " modem database "
                    + (isController ? "controller" : "responder") + " record with address " + address + " and group "
                    + group + ".");
        }
    }

    private void deleteDatabaseRecord(Console console, String[] args) {
        if (!getModem().getDB().isComplete()) {
            console.println("The modem database is not loaded yet.");
        } else if (!InsteonAddress.isValid(args[1])) {
            console.println("Invalid record address argument: " + args[1]);
        } else if (!HexUtils.isValidHexString(args[2])) {
            console.println("Invalid record group hex argument: " + args[2]);
        } else {
            InsteonAddress address = new InsteonAddress(args[1]);
            int group = HexUtils.toInteger(args[2]);

            ModemDBRecord record = getModem().getDB().getRecord(address, group);
            if (record == null) {
                console.println(
                        "No modem database record with address " + address + " and group " + group + " to delete.");
            } else {
                getModem().getDB().markRecordForDelete(record);
                console.println("Added a pending change to delete modem database "
                        + (record.isController() ? "controller" : "responder") + " record with address " + address
                        + " and group " + group + ".");
            }
        }
    }

    private void applyDatabaseChanges(Console console, boolean isConfirmed) {
        if (!getModem().getDB().isComplete()) {
            console.println("The modem database is not loaded yet.");
        } else if (getModem().getDB().getChanges().isEmpty()) {
            console.println("The modem database has no pending changes.");
        } else if (!isConfirmed) {
            listDatabaseChanges(console);
            console.println("Please run the same command with " + CONFIRM_OPTION
                    + " option to have these changes written to the modem database.");
        } else {
            int count = getModem().getDB().getChanges().size();
            console.println("Applying " + count + " pending changes to the modem database...");
            getModem().getDB().update();
        }
    }

    private void clearDatabaseChanges(Console console) {
        if (getModem().getDB().getChanges().isEmpty()) {
            console.println("The modem database has no pending changes.");
        } else {
            int count = getModem().getDB().getChanges().size();
            getModem().getDB().clearChanges();
            console.println("Cleared " + count + " pending changes from the modem database.");
        }
    }

    private void addDevice(Console console, @Nullable String address) {
        if (address != null && !InsteonAddress.isValid(address)) {
            console.println("The device address " + address + " is not valid.");
        } else if (!getModem().getDB().isComplete()) {
            console.println("The modem database is not loaded yet.");
        } else if (getModem().getLinkManager().isRunning()) {
            console.println("Another device is currently being added or removed.");
        } else if (address == null) {
            console.println("Adding device...");
            console.println("Press the device SET button to link.");
            getModem().getLinkManager().link(null);
        } else {
            console.println("Adding device " + address + "...");
            getModem().getLinkManager().link(new InsteonAddress(address));
        }
    }

    private void removeDevice(Console console, String address, boolean force) {
        if (!InsteonAddress.isValid(address)) {
            console.println("The device address " + address + " is not valid.");
        } else if (!getModem().getDB().isComplete()) {
            console.println("The modem database is not loaded yet.");
        } else if (!getModem().getDB().hasEntry(new InsteonAddress(address))) {
            console.println("The device " + address + " is not in modem database.");
        } else if (getModem().getLinkManager().isRunning()) {
            console.println("Another device is currently being added or removed.");
        } else {
            console.println("Removing device " + address + "...");
            getModem().getLinkManager().unlink(new InsteonAddress(address), force);
        }
    }

    private void switchModem(Console console, String thingId) {
        InsteonBridgeHandler handler = getBridgeHandler(thingId);
        if (handler == null) {
            console.println("No Insteon bridge " + thingId + " configured or enabled.");
        } else {
            console.println("Using Insteon bridge " + handler.getThing().getUID());
            setBridgeHandler(handler);
        }
    }
}
