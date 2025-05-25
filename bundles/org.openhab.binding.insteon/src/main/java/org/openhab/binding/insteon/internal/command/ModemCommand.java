/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.device.ProductData;
import org.openhab.binding.insteon.internal.device.database.ModemDBChange;
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
    private static final String LIST_FEATURES = "listFeatures";
    private static final String LIST_PRODUCT_DATA = "listProductData";
    private static final String RELOAD_DATABASE = "reloadDatabase";
    private static final String BACKUP_DATABASE = "backupDatabase";
    private static final String RESTORE_DATABASE = "restoreDatabase";
    private static final String ADD_DATABASE_CONTROLLER = "addDatabaseController";
    private static final String ADD_DATABASE_RESPONDER = "addDatabaseResponder";
    private static final String DELETE_DATABASE_RECORD = "deleteDatabaseRecord";
    private static final String APPLY_DATABASE_CHANGES = "applyDatabaseChanges";
    private static final String CLEAR_DATABASE_CHANGES = "clearDatabaseChanges";
    private static final String ADD_DEVICE = "addDevice";
    private static final String REMOVE_DEVICE = "removeDevice";
    private static final String RESET = "reset";
    private static final String SWITCH = "switch";

    private static final List<String> SUBCMDS = List.of(LIST_ALL, LIST_DATABASE, LIST_FEATURES, LIST_PRODUCT_DATA,
            RELOAD_DATABASE, BACKUP_DATABASE, RESTORE_DATABASE, ADD_DATABASE_CONTROLLER, ADD_DATABASE_RESPONDER,
            DELETE_DATABASE_RECORD, APPLY_DATABASE_CHANGES, CLEAR_DATABASE_CHANGES, ADD_DEVICE, REMOVE_DEVICE, RESET,
            SWITCH);

    private static final String CONFIRM_OPTION = "--confirm";
    private static final String FORCE_OPTION = "--force";
    private static final String RECORDS_OPTION = "--records";

    private static final String MODEM_DATABASE_FILE_PREFIX = "modem-database";

    public ModemCommand(InsteonCommandExtension commandExtension) {
        super(NAME, DESCRIPTION, commandExtension);
    }

    @Override
    public List<String> getUsages() {
        return List.of(
                buildCommandUsage(LIST_ALL, "list configured Insteon modem bridges with related channels and status"),
                buildCommandUsage(LIST_DATABASE + " [" + RECORDS_OPTION + "]",
                        "list all-link database summary or records and pending changes for the Insteon modem"),
                buildCommandUsage(LIST_FEATURES, "list features for the Insteon modem"),
                buildCommandUsage(LIST_PRODUCT_DATA, "list product data for the Insteon modem"),
                buildCommandUsage(RELOAD_DATABASE, "reload all-link database from the Insteon modem"),
                buildCommandUsage(BACKUP_DATABASE, "backup all-link database from the Insteon modem to a file"),
                buildCommandUsage(RESTORE_DATABASE + " <filename> " + CONFIRM_OPTION,
                        "restore all-link database to the Insteon modem from a specific file"),
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
                buildCommandUsage(RESET + " " + CONFIRM_OPTION, "reset the Insteon modem to factory defaults"),
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
            case LIST_FEATURES:
                if (args.length == 1) {
                    listFeatures(console);
                } else {
                    printUsage(console, args[0]);
                }
                break;
            case LIST_PRODUCT_DATA:
                if (args.length == 1) {
                    listProductData(console);
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
            case BACKUP_DATABASE:
                if (args.length == 1) {
                    backupDatabase(console);
                } else {
                    printUsage(console, args[0]);
                }
                break;
            case RESTORE_DATABASE:
                if (args.length == 2 || args.length == 3 && CONFIRM_OPTION.equals(args[2])) {
                    restoreDatabase(console, args[1], args.length == 3);
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
            case RESET:
                if (args.length == 1 || args.length == 2 && CONFIRM_OPTION.equals(args[1])) {
                    resetModem(console, args.length == 2);
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
                case RESTORE_DATABASE:
                    strings = getBindingDataFilePaths(MODEM_DATABASE_FILE_PREFIX).map(Path::getFileName)
                            .map(Path::toString).toList();
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
                case APPLY_DATABASE_CHANGES:
                case RESET:
                    strings = List.of(CONFIRM_OPTION);
                    break;
                case SWITCH:
                    strings = getBridgeHandlers().map(InsteonBridgeHandler::getThingId).toList();
                    break;
            }
        } else if (cursorArgumentIndex == 2) {
            InsteonAddress address = InsteonAddress.isValid(args[1]) ? new InsteonAddress(args[1]) : null;
            switch (args[0]) {
                case RESTORE_DATABASE:
                    strings = List.of(CONFIRM_OPTION);
                    break;
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
            console.println("The all-link database for modem " + address + " contains " + entries.size() + " devices:"
                    + (!getModem().getDB().isComplete() ? " (Partial)" : ""));
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
            console.println("The all-link database for modem " + address + " contains " + records.size() + " records:"
                    + (!getModem().getDB().isComplete() ? " (Partial)" : ""));
            print(console, records);
            listDatabaseChanges(console);
        }
    }

    private void listDatabaseChanges(Console console) {
        InsteonAddress address = getModem().getAddress();
        List<String> changes = getModem().getDB().getChanges().stream().map(ModemDBChange::toString).toList();
        if (InsteonAddress.UNKNOWN.equals(address)) {
            console.println("No modem found!");
        } else if (!changes.isEmpty()) {
            console.println(
                    "The all-link database for modem " + address + " has " + changes.size() + " pending changes:");
            print(console, changes);
        }
    }

    private void listFeatures(Console console) {
        InsteonAddress address = getModem().getAddress();
        List<String> features = getModem().getFeatures().stream()
                .filter(feature -> !feature.isEventFeature() && !feature.isGroupFeature())
                .map(feature -> String.format("%s: type=%s state=%s isHidden=%s", feature.getName(), feature.getType(),
                        feature.getState().toFullString(), feature.isHiddenFeature()))
                .sorted().toList();
        if (features.isEmpty()) {
            console.println("The features for modem " + address + " are not defined");
        } else {
            console.println("The features for modem " + address + " are:");
            print(console, features);
        }
    }

    private void listProductData(Console console) {
        InsteonAddress address = getModem().getAddress();
        ProductData productData = getModem().getProductData();
        if (productData == null) {
            console.println("The product data for modem " + address + " is not defined");
        } else {
            console.println("The product data for modem " + address + " is:");
            console.println(productData.toString().replace("|", "\n"));
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

    private void backupDatabase(Console console) {
        InsteonAddress address = getModem().getAddress();
        if (InsteonAddress.UNKNOWN.equals(address)) {
            console.println("No modem found!");
        } else if (!getModem().getDB().isComplete()) {
            console.println("The all-link database for modem " + address + " is not loaded yet.");
        } else if (getModem().getDB().getRecords().isEmpty()) {
            console.println("The all-link database for modem " + address + " is empty");
        } else {
            String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
            String id = address.toString().toLowerCase().replace(".", "");
            Path path = getBindingDataFilePath(MODEM_DATABASE_FILE_PREFIX + "-" + id + "-" + timestamp + ".dmp");
            byte[] bytes = getModem().getDB().getRecordDump();
            int count = bytes.length / ModemDBRecord.SIZE;

            try {
                Files.createDirectories(path.getParent());
                Files.write(path, bytes);
                console.println("Backed up " + count + " database records from modem " + address + " to " + path);
            } catch (IOException e) {
                console.println("Failed to write backup file: " + e.getMessage());
            }
        }
    }

    private void restoreDatabase(Console console, String filename, boolean isConfirmed) {
        InsteonAddress address = getModem().getAddress();
        if (InsteonAddress.UNKNOWN.equals(address)) {
            console.println("No modem found!");
        } else if (!getModem().getDB().isComplete()) {
            console.println("The all-link database for modem " + address + " is not loaded yet.");
        } else {
            Path path = Path.of(filename);
            if (!path.isAbsolute()) {
                path = getBindingDataFilePath(filename);
            }

            try {
                if (!Files.isReadable(path)) {
                    console.println("The restore file " + path + " does not exist or is not readable.");
                } else if (Files.size(path) == 0) {
                    console.println("The restore file " + path + " is empty.");
                } else {
                    InputStream stream = Files.newInputStream(path);
                    List<ModemDBRecord> records = ModemDBRecord.fromRecordDump(stream);
                    if (!isConfirmed) {
                        console.println(
                                "The restore file " + path + " contains " + records.size() + " database records:");
                        print(console, records.stream().map(ModemDBRecord::toString).toList());
                        console.println("Please run the same command with " + CONFIRM_OPTION
                                + " option to have these database records restored to modem " + address + ".");
                    } else {
                        console.println("Restoring " + records.size() + " database records to modem " + address
                                + " from " + path + "...");
                        records.forEach(record -> getModem().getDB().markRecordForAddOrModify(record.getAddress(),
                                record.getGroup(), record.isController(), record.getData()));
                        getModem().getDB().update();
                    }
                }
            } catch (IllegalArgumentException e) {
                console.println("The restore file " + path + " is invalid.");
            } catch (IOException e) {
                console.println("Failed to read restore file: " + e.getMessage());
            }
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

    private void resetModem(Console console, boolean isConfirmed) {
        InsteonAddress address = getModem().getAddress();
        if (InsteonAddress.UNKNOWN.equals(address)) {
            console.println("No modem found!");
        } else if (!isConfirmed) {
            console.println("Please run the same command with " + CONFIRM_OPTION + " option to reset modem " + address
                    + " to factory defaults.");
        } else {
            console.println("Resetting modem " + address + " to factory defaults...");
            getModem().reset();
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
