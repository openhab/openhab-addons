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

import static org.openhab.binding.insteon.internal.InsteonBindingConstants.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.insteon.internal.device.Device;
import org.openhab.binding.insteon.internal.device.DeviceFeature;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.device.InsteonDevice;
import org.openhab.binding.insteon.internal.device.ProductData;
import org.openhab.binding.insteon.internal.device.database.LinkDBRecord;
import org.openhab.binding.insteon.internal.device.feature.FeatureEnums.KeypadButtonToggleMode;
import org.openhab.binding.insteon.internal.handler.InsteonDeviceHandler;
import org.openhab.binding.insteon.internal.handler.InsteonThingHandler;
import org.openhab.binding.insteon.internal.utils.HexUtils;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.StringsCompleter;

/**
 *
 * The {@link DeviceCommand} represents an Insteon console device command
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class DeviceCommand extends InsteonCommand {
    private static final String NAME = "device";
    private static final String DESCRIPTION = "Insteon/X10 device commands";

    private static final String LIST_ALL = "listAll";
    private static final String LIST_DATABASE = "listDatabase";
    private static final String LIST_FEATURES = "listFeatures";
    private static final String LIST_PRODUCT_DATA = "listProductData";
    private static final String LIST_MISSING_LINKS = "listMissingLinks";
    private static final String ADD_MISSING_LINKS = "addMissingLinks";
    private static final String ADD_DATABASE_CONTROLLER = "addDatabaseController";
    private static final String ADD_DATABASE_RESPONDER = "addDatabaseResponder";
    private static final String DELETE_DATABASE_CONTROLLER = "deleteDatabaseController";
    private static final String DELETE_DATABASE_RESPONDER = "deleteDatabaseResponder";
    private static final String APPLY_DATABASE_CHANGES = "applyDatabaseChanges";
    private static final String CLEAR_DATABASE_CHANGES = "clearDatabaseChanges";
    private static final String SET_BUTTON_RADIO_GROUP = "setButtonRadioGroup";
    private static final String CLEAR_BUTTON_RADIO_GROUP = "clearButtonRadioGroup";
    private static final String REFRESH = "refresh";

    private static final List<String> SUBCMDS = List.of(LIST_ALL, LIST_DATABASE, LIST_FEATURES, LIST_PRODUCT_DATA,
            LIST_MISSING_LINKS, ADD_MISSING_LINKS, ADD_DATABASE_CONTROLLER, ADD_DATABASE_RESPONDER,
            DELETE_DATABASE_CONTROLLER, DELETE_DATABASE_RESPONDER, APPLY_DATABASE_CHANGES, CLEAR_DATABASE_CHANGES,
            SET_BUTTON_RADIO_GROUP, CLEAR_BUTTON_RADIO_GROUP, REFRESH);

    private static final String ALL_OPTION = "--all";
    private static final String CONFIRM_OPTION = "--confirm";

    public DeviceCommand(InsteonCommandExtension commandExtension) {
        super(NAME, DESCRIPTION, commandExtension);
    }

    @Override
    public List<String> getUsages() {
        return List.of(
                buildCommandUsage(LIST_ALL, "list configured Insteon/X10 devices with related channels and status"),
                buildCommandUsage(LIST_DATABASE + " <thingId>",
                        "list all-link database records and pending changes for a configured Insteon device"),
                buildCommandUsage(LIST_FEATURES + " <thingId>", "list features for a configured Insteon/X10 device"),
                buildCommandUsage(LIST_PRODUCT_DATA + " <thingId>",
                        "list product data for a configured Insteon/X10 device"),
                buildCommandUsage(LIST_MISSING_LINKS + " " + ALL_OPTION + "|<thingId>",
                        "list missing links for a specific or all configured Insteon devices"),
                buildCommandUsage(ADD_MISSING_LINKS + " " + ALL_OPTION + "|<thingId>",
                        "add missing links for a specific or all configured Insteon devices"),
                buildCommandUsage(ADD_DATABASE_CONTROLLER + " <thingId> <address> <group> <data1> <data2> <data3>",
                        "add a controller record to all-link database for a configured Insteon device"),
                buildCommandUsage(ADD_DATABASE_RESPONDER + " <thingId> <address> <group> <data1> <data2> <data3>",
                        "add a responder record to all-link database for a configured Insteon device"),
                buildCommandUsage(DELETE_DATABASE_CONTROLLER + " <thingId> <address> <group> <data3>",
                        "delete a controller record from all-link database for a configured Insteon device"),
                buildCommandUsage(DELETE_DATABASE_RESPONDER + " <thingId> <address> <group> <data3>",
                        "delete a responder record from all-link database for a configured Insteon device"),
                buildCommandUsage(APPLY_DATABASE_CHANGES + " <thingId> " + CONFIRM_OPTION,
                        "apply all-link database pending changes for a configured Insteon device"),
                buildCommandUsage(CLEAR_DATABASE_CHANGES + " <thingId>",
                        "clear all-link database pending changes for a configured Insteon device"),
                buildCommandUsage(SET_BUTTON_RADIO_GROUP + " <thingId> <button1> <button2> [<button3> ... <button7>]",
                        "set a button radio group for a configured Insteon KeypadLinc device"),
                buildCommandUsage(CLEAR_BUTTON_RADIO_GROUP + " <thingId> <button1> <button2> [<button3> ... <button7>]",
                        "clear a button radio group for a configured Insteon KeypadLinc device"),
                buildCommandUsage(REFRESH + " <thingId>", "refresh data for a configured Insteon device"));
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
                if (args.length == 2) {
                    listDatabaseRecords(console, args[1]);
                } else {
                    printUsage(console, args[0]);
                }
                break;
            case LIST_FEATURES:
                if (args.length == 2) {
                    listFeatures(console, args[1]);
                } else {
                    printUsage(console, args[0]);
                }
                break;
            case LIST_PRODUCT_DATA:
                if (args.length == 2) {
                    listProductData(console, args[1]);
                } else {
                    printUsage(console, args[0]);
                }
                break;
            case LIST_MISSING_LINKS:
                if (args.length == 2) {
                    if (ALL_OPTION.equals(args[1])) {
                        listMissingLinks(console);
                    } else {
                        listMissingLinks(console, args[1]);
                    }
                } else {
                    printUsage(console, args[0]);
                }
                break;
            case ADD_MISSING_LINKS:
                if (args.length == 2) {
                    if (ALL_OPTION.equals(args[1])) {
                        addMissingLinks(console);
                    } else {
                        addMissingLinks(console, args[1]);
                    }
                } else {
                    printUsage(console, args[0]);
                }
                break;
            case ADD_DATABASE_CONTROLLER:
                if (args.length == 7) {
                    addDatabaseRecord(console, args, true);
                } else {
                    printUsage(console, args[0]);
                }
                break;
            case ADD_DATABASE_RESPONDER:
                if (args.length == 7) {
                    addDatabaseRecord(console, args, false);
                } else {
                    printUsage(console, args[0]);
                }
                break;
            case DELETE_DATABASE_CONTROLLER:
                if (args.length == 5) {
                    deleteDatabaseRecord(console, args, true);
                } else {
                    printUsage(console, args[0]);
                }
                break;
            case DELETE_DATABASE_RESPONDER:
                if (args.length == 5) {
                    deleteDatabaseRecord(console, args, false);
                } else {
                    printUsage(console, args[0]);
                }
                break;
            case APPLY_DATABASE_CHANGES:
                if (args.length == 2 || args.length == 3 && CONFIRM_OPTION.equals(args[2])) {
                    applyDatabaseChanges(console, args[1], args.length == 3);
                } else {
                    printUsage(console, args[0]);
                }
                break;
            case CLEAR_DATABASE_CHANGES:
                if (args.length == 2) {
                    clearDatabaseChanges(console, args[1]);
                } else {
                    printUsage(console, args[0]);
                }
                break;
            case SET_BUTTON_RADIO_GROUP:
                if (args.length >= 4 && args.length <= 9) {
                    setButtonRadioGroup(console, args);
                } else {
                    printUsage(console, args[0]);
                }
                break;
            case CLEAR_BUTTON_RADIO_GROUP:
                if (args.length >= 4 && args.length <= 9) {
                    clearButtonRadioGroup(console, args);
                } else {
                    printUsage(console, args[0]);
                }
                break;
            case REFRESH:
                if (args.length == 2) {
                    refreshDevice(console, args[1]);
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
                case LIST_FEATURES:
                case LIST_PRODUCT_DATA:
                    strings = getAllDeviceHandlers().map(InsteonThingHandler::getThingId).toList();
                    break;
                case LIST_DATABASE:
                case REFRESH:
                    strings = getInsteonDeviceHandlers().map(InsteonDeviceHandler::getThingId).toList();
                    break;
                case ADD_DATABASE_CONTROLLER:
                case DELETE_DATABASE_CONTROLLER:
                    strings = getInsteonDeviceHandlers().filter(handler -> {
                        InsteonDevice device = handler.getDevice();
                        return device != null && !device.getControllerFeatures().isEmpty();
                    }).map(InsteonDeviceHandler::getThingId).toList();
                    break;
                case ADD_DATABASE_RESPONDER:
                case DELETE_DATABASE_RESPONDER:
                    strings = getInsteonDeviceHandlers().filter(handler -> {
                        InsteonDevice device = handler.getDevice();
                        return device != null && !device.getResponderFeatures().isEmpty();
                    }).map(InsteonDeviceHandler::getThingId).toList();
                    break;
                case APPLY_DATABASE_CHANGES:
                case CLEAR_DATABASE_CHANGES:
                    strings = getInsteonDeviceHandlers().filter(handler -> {
                        InsteonDevice device = handler.getDevice();
                        return device != null && !device.getLinkDB().getChanges().isEmpty();
                    }).map(InsteonDeviceHandler::getThingId).toList();
                    break;
                case LIST_MISSING_LINKS:
                case ADD_MISSING_LINKS:
                    strings = Stream.concat(Stream.of(ALL_OPTION),
                            getInsteonDeviceHandlers().map(InsteonDeviceHandler::getThingId)).toList();
                    break;
                case SET_BUTTON_RADIO_GROUP:
                case CLEAR_BUTTON_RADIO_GROUP:
                    strings = getInsteonDeviceHandlers().filter(handler -> {
                        InsteonDevice device = handler.getDevice();
                        return device != null && !device.getFeatures(FEATURE_TYPE_KEYPAD_BUTTON).isEmpty();
                    }).map(InsteonDeviceHandler::getThingId).toList();
                    break;
            }
        } else if (cursorArgumentIndex == 2) {
            InsteonDevice device = getInsteonDevice(args[1]);
            switch (args[0]) {
                case ADD_DATABASE_CONTROLLER:
                case ADD_DATABASE_RESPONDER:
                    if (device != null) {
                        strings = Stream
                                .concat(Stream.of(getModem().getAddress()),
                                        getModem().getDB().getDevices().stream()
                                                .filter(address -> !device.getAddress().equals(address)))
                                .map(InsteonAddress::toString).toList();
                    }
                    break;
                case DELETE_DATABASE_CONTROLLER:
                    if (device != null) {
                        strings = device.getLinkDB().getControllerRecords().stream()
                                .map(record -> record.getAddress().toString()).distinct().toList();
                    }
                    break;
                case DELETE_DATABASE_RESPONDER:
                    if (device != null) {
                        strings = device.getLinkDB().getResponderRecords().stream()
                                .map(record -> record.getAddress().toString()).distinct().toList();
                    }
                    break;
                case APPLY_DATABASE_CHANGES:
                    strings = List.of(CONFIRM_OPTION);
                    break;
            }
        } else if (cursorArgumentIndex == 3) {
            InsteonDevice device = getInsteonDevice(args[1]);
            InsteonAddress address = InsteonAddress.isValid(args[2]) ? new InsteonAddress(args[2]) : null;
            switch (args[0]) {
                case DELETE_DATABASE_CONTROLLER:
                    if (device != null && address != null) {
                        strings = device.getLinkDB().getControllerRecords(address).stream()
                                .map(record -> HexUtils.getHexString(record.getGroup())).distinct().toList();
                    }
                    break;
                case DELETE_DATABASE_RESPONDER:
                    if (device != null && address != null) {
                        strings = device.getLinkDB().getResponderRecords(address).stream()
                                .map(record -> HexUtils.getHexString(record.getGroup())).distinct().toList();
                    }
                    break;
            }
        } else if (cursorArgumentIndex == 4) {
            InsteonDevice device = getInsteonDevice(args[1]);
            InsteonAddress address = InsteonAddress.isValid(args[2]) ? new InsteonAddress(args[2]) : null;
            int group = HexUtils.isValidHexString(args[3]) ? HexUtils.toInteger(args[3]) : -1;
            switch (args[0]) {
                case DELETE_DATABASE_CONTROLLER:
                    if (device != null && address != null && group != -1) {
                        strings = device.getLinkDB().getControllerRecords(address, group).stream()
                                .map(record -> HexUtils.getHexString(record.getComponentId())).distinct().toList();
                    }
                    break;
                case DELETE_DATABASE_RESPONDER:
                    if (device != null && address != null && group != -1) {
                        strings = device.getLinkDB().getResponderRecords(address, group).stream()
                                .map(record -> HexUtils.getHexString(record.getComponentId())).distinct().toList();
                    }
                    break;
            }
        }

        if (cursorArgumentIndex >= 2) {
            InsteonDevice device = getInsteonDevice(args[1]);
            switch (args[0]) {
                case SET_BUTTON_RADIO_GROUP:
                case CLEAR_BUTTON_RADIO_GROUP:
                    if (device != null) {
                        strings = device.getFeatures(FEATURE_TYPE_KEYPAD_BUTTON).stream().map(DeviceFeature::getName)
                                .filter(name -> !Arrays.asList(args).subList(2, cursorArgumentIndex).contains(name))
                                .toList();
                    }
                    break;
            }
        }

        return new StringsCompleter(strings, false).complete(args, cursorArgumentIndex, cursorPosition, candidates);
    }

    private void listAll(Console console) {
        Map<String, String> devices = getAllDeviceHandlers()
                .collect(Collectors.toMap(InsteonThingHandler::getThingId, InsteonThingHandler::getThingInfo));
        if (devices.isEmpty()) {
            console.println("No device configured or enabled!");
        } else {
            console.println("There are " + devices.size() + " devices configured:");
            print(console, devices);
        }
    }

    private void listDatabaseRecords(Console console, String thingId) {
        InsteonDevice device = getInsteonDevice(thingId);
        if (device == null) {
            console.println("The device " + thingId + " is not configured or enabled!");
            return;
        }
        List<String> records = device.getLinkDB().getRecords().stream().map(String::valueOf).toList();
        if (records.isEmpty()) {
            console.println("The all-link database for device " + device.getAddress() + " is empty");
        } else {
            console.println("The all-link database for device " + device.getAddress() + " contains " + records.size()
                    + " records:" + (!device.getLinkDB().isComplete() ? " (Partial)" : ""));
            print(console, records);
            listDatabaseChanges(console, thingId);
        }
    }

    private void listDatabaseChanges(Console console, String thingId) {
        InsteonDevice device = getInsteonDevice(thingId);
        if (device == null) {
            console.println("The device " + thingId + " is not configured or enabled!");
            return;
        }
        List<String> changes = device.getLinkDB().getChanges().stream().map(String::valueOf).toList();
        if (!changes.isEmpty()) {
            console.println("The all-link database for device " + device.getAddress() + " has " + changes.size()
                    + " pending changes:");
            print(console, changes);
        }
    }

    private void listFeatures(Console console, String thingId) {
        Device device = getDevice(thingId);
        if (device == null) {
            console.println("The device " + thingId + " is not configured or enabled!");
            return;
        }
        List<String> features = device.getFeatures().stream()
                .filter(feature -> !feature.isEventFeature() && !feature.isGroupFeature())
                .map(feature -> String.format("%s: type=%s state=%s isHidden=%s", feature.getName(), feature.getType(),
                        feature.getState().toFullString(), feature.isHiddenFeature()))
                .sorted().toList();
        if (features.isEmpty()) {
            console.println("The features for device " + device.getAddress() + " are not defined");
        } else {
            console.println("The features for device " + device.getAddress() + " are:");
            print(console, features);
        }
    }

    private void listProductData(Console console, String thingId) {
        Device device = getDevice(thingId);
        if (device == null) {
            console.println("The device " + thingId + " is not configured or enabled!");
            return;
        }
        ProductData productData = device.getProductData();
        if (productData == null) {
            console.println("The product data for device " + device.getAddress() + " is not defined");
        } else {
            console.println("The product data for device " + device.getAddress() + " is:");
            console.println(productData.toString().replace("|", "\n"));
        }
    }

    private void listMissingLinks(Console console) {
        if (!getModem().getDB().isComplete()) {
            console.println("The modem database is not loaded yet.");
        } else {
            getInsteonDeviceHandlers().forEach(handler -> listMissingLinks(console, handler.getThingId()));
        }
    }

    private void listMissingLinks(Console console, String thingId) {
        InsteonDevice device = getInsteonDevice(thingId);
        if (device == null) {
            console.println("The device " + thingId + " is not configured or enabled!");
        } else if (!device.getLinkDB().isComplete()) {
            console.println("The link database for device " + thingId + " is not loaded yet.");
        } else if (!getModem().getDB().isComplete()) {
            console.println("The modem database is not loaded yet.");
        } else {
            List<String> deviceLinks = device.getMissingDeviceLinks().entrySet().stream()
                    .map(entry -> String.format("%s: %s", entry.getKey(), entry.getValue().getRecord())).toList();
            List<String> modemLinks = device.getMissingModemLinks().entrySet().stream()
                    .map(entry -> String.format("%s: %s", entry.getKey(), entry.getValue().getRecord())).toList();
            if (deviceLinks.isEmpty() && modemLinks.isEmpty()) {
                console.println("There are no missing links for device " + device.getAddress() + ".");
            } else {
                if (!deviceLinks.isEmpty()) {
                    console.println("There are " + deviceLinks.size()
                            + " missing links from the link database for device " + device.getAddress() + ":");
                    print(console, deviceLinks);
                }
                if (!modemLinks.isEmpty()) {
                    console.println("There are " + modemLinks.size()
                            + " missing links from the modem database for device " + device.getAddress() + ":");
                    print(console, modemLinks);
                }
            }
        }
    }

    private void addMissingLinks(Console console) {
        if (!getModem().getDB().isComplete()) {
            console.println("The modem database is not loaded yet.");
        } else {
            getInsteonDeviceHandlers().forEach(handler -> addMissingLinks(console, handler.getThingId()));
        }
    }

    private void addMissingLinks(Console console, String thingId) {
        InsteonDevice device = getInsteonDevice(thingId);
        if (device == null) {
            console.println("The device " + thingId + " is not configured or enabled!");
        } else if (!device.getLinkDB().isComplete()) {
            console.println("The link database for device " + thingId + " is not loaded yet.");
        } else if (!device.getLinkDB().getChanges().isEmpty()) {
            console.println("The link database for device " + thingId + " has pending changes.");
        } else if (!getModem().getDB().isComplete()) {
            console.println("The modem database is not loaded yet.");
        } else if (!getModem().getDB().getChanges().isEmpty()) {
            console.println("The modem database has pending changes.");
        } else {
            int deviceLinkCount = device.getMissingDeviceLinks().size();
            int modemLinkCount = device.getMissingModemLinks().size();
            if (deviceLinkCount == 0 && modemLinkCount == 0) {
                console.println("There are no missing links for device " + device.getAddress() + ".");
            } else {
                if (deviceLinkCount > 0) {
                    if (!device.isAwake() || !device.isResponding()) {
                        console.println("Scheduling " + deviceLinkCount + " missing links for device "
                                + device.getAddress() + " to be added to its link database the next time it is "
                                + (device.isBatteryPowered() ? "awake" : "responding") + ".");
                    } else {
                        console.println("Adding " + deviceLinkCount + " missing links for device " + device.getAddress()
                                + " to its link database...");
                    }
                    device.addMissingDeviceLinks();
                }
                if (modemLinkCount > 0) {
                    console.println("Adding " + modemLinkCount + " missing links for device " + device.getAddress()
                            + " to the modem database...");
                    device.addMissingModemLinks();
                }
            }
        }
    }

    private void addDatabaseRecord(Console console, String[] args, boolean isController) {
        InsteonDevice device = getInsteonDevice(args[1]);
        if (device == null) {
            console.println("The device " + args[1] + " is not configured or enabled!");
        } else if (!device.getLinkDB().isComplete()) {
            console.println("The link database for device " + args[1] + " is not loaded yet.");
        } else if (!InsteonAddress.isValid(args[2])) {
            console.println("Invalid record address argument: " + args[2]);
        } else if (!HexUtils.isValidHexString(args[3])) {
            console.println("Invalid record group hex argument: " + args[3]);
        } else if (!HexUtils.isValidHexStringArray(args, 4, args.length)) {
            console.println("Invalid record data hex argument(s).");
        } else {
            InsteonAddress address = new InsteonAddress(args[2]);
            int group = HexUtils.toInteger(args[3]);
            byte[] data = HexUtils.toByteArray(args, 4, args.length);

            LinkDBRecord record = device.getLinkDB().getActiveRecord(address, group, isController, data[2]);
            if (record == null) {
                device.getLinkDB().markRecordForAdd(address, group, isController, data);
                console.println("Added a pending change to add link database "
                        + (isController ? "controller" : "responder") + " record with address " + address
                        + " and group " + group + " for device " + device.getAddress() + ".");
            } else {
                device.getLinkDB().markRecordForModify(record, data);
                console.println("Added a pending change to modify link database record located at "
                        + HexUtils.getHexString(record.getLocation(), 4) + " for device " + device.getAddress() + ".");
            }
        }
    }

    private void deleteDatabaseRecord(Console console, String[] args, boolean isController) {
        InsteonDevice device = getInsteonDevice(args[1]);
        if (device == null) {
            console.println("The device " + args[1] + " is not configured or enabled!");
        } else if (!device.getLinkDB().isComplete()) {
            console.println("The link database for device " + args[1] + " is not loaded yet.");
        } else if (!InsteonAddress.isValid(args[2])) {
            console.println("Invalid record address argument: " + args[2]);
        } else if (!HexUtils.isValidHexString(args[3])) {
            console.println("Invalid record group hex argument: " + args[3]);
        } else if (!HexUtils.isValidHexString(args[4])) {
            console.println("Invalid record data3 hex argument: " + args[4]);
        } else {
            InsteonAddress address = new InsteonAddress(args[2]);
            int group = HexUtils.toInteger(args[3]);
            int componentId = HexUtils.toInteger(args[4]); // data3 as component id

            LinkDBRecord record = device.getLinkDB().getActiveRecord(address, group, isController, componentId);
            if (record == null) {
                console.println("No link database " + (isController ? "controller" : "responder")
                        + " record with address " + address + " and group " + group + " to delete for device "
                        + device.getAddress() + ".");
            } else {
                device.getLinkDB().markRecordForDelete(record);
                console.println("Added a pending change to delete link database record located at "
                        + HexUtils.getHexString(record.getLocation(), 4) + " for device " + device.getAddress() + ".");
            }
        }
    }

    private void applyDatabaseChanges(Console console, String thingId, boolean isConfirmed) {
        InsteonDevice device = getInsteonDevice(thingId);
        if (device == null) {
            console.println("The device " + thingId + " is not configured or enabled!");
        } else if (!device.getLinkDB().isComplete()) {
            console.println("The link database for device " + thingId + " is not loaded yet.");
        } else if (device.getLinkDB().getChanges().isEmpty()) {
            console.println("The link database for device " + thingId + " has no pending changes.");
        } else if (!isConfirmed) {
            listDatabaseChanges(console, thingId);
            console.println("Please run the same command with " + CONFIRM_OPTION
                    + " option to have these changes written to the link database for device " + device.getAddress()
                    + ".");
        } else {
            int count = device.getLinkDB().getChanges().size();
            if (!device.isAwake() || !device.isResponding() || !getModem().getDB().isComplete()) {
                console.println("Scheduling " + count + " pending changes for device " + device.getAddress()
                        + " to be applied to its link database the next time it is "
                        + (device.isBatteryPowered() ? "awake" : "responding") + ".");
            } else {
                console.println("Applying " + count + " pending changes to link database for device "
                        + device.getAddress() + "...");
            }
            device.getLinkDB().update();
        }
    }

    private void clearDatabaseChanges(Console console, String thingId) {
        InsteonDevice device = getInsteonDevice(thingId);
        if (device == null) {
            console.println("The device " + thingId + " is not configured or enabled!");
        } else if (device.getLinkDB().getChanges().isEmpty()) {
            console.println("The link database for device " + thingId + " has no pending changes.");
        } else {
            int count = device.getLinkDB().getChanges().size();
            device.getLinkDB().clearChanges();
            console.println(
                    "Cleared " + count + " pending changes from link database for device " + device.getAddress() + ".");
        }
    }

    private void setButtonRadioGroup(Console console, String[] args) {
        InsteonDevice device = getInsteonDevice(args[1]);
        if (device == null) {
            console.println("The device " + args[1] + " is not configured or enabled!");
        } else if (device.getFeatures(FEATURE_TYPE_KEYPAD_BUTTON).isEmpty()) {
            console.println("The device " + args[1] + " does not have keypad buttons.");
        } else {
            List<Integer> buttons = new ArrayList<>();
            for (int i = 2; i < args.length; i++) {
                DeviceFeature feature = device.getFeature(args[i]);
                if (feature == null || !feature.getType().equals(FEATURE_TYPE_KEYPAD_BUTTON)) {
                    console.println("The feature " + args[i] + " is not configured or a keypad button.");
                    return;
                }
                int group = feature.getGroup();
                if (!buttons.contains(group)) {
                    buttons.add(group);
                }
            }
            if (buttons.size() < 2) {
                console.println("Requires at least two buttons to set a radio group.");
                return;
            }

            console.println("Setting a radio group for device " + device.getAddress() + "...");
            device.setButtonRadioGroup(buttons);
            device.setButtonToggleMode(buttons, KeypadButtonToggleMode.ALWAYS_ON);
        }
    }

    private void clearButtonRadioGroup(Console console, String[] args) {
        InsteonDevice device = getInsteonDevice(args[1]);
        if (device == null) {
            console.println("The device " + args[1] + " is not configured or enabled!");
        } else if (device.getFeatures(FEATURE_TYPE_KEYPAD_BUTTON).isEmpty()) {
            console.println("The device " + args[1] + " does not have keypad buttons.");
        } else {
            List<Integer> buttons = new ArrayList<>();
            for (int i = 2; i < args.length; i++) {
                DeviceFeature feature = device.getFeature(args[i]);
                if (feature == null || !feature.getType().equals(FEATURE_TYPE_KEYPAD_BUTTON)) {
                    console.println(
                            "The device " + args[1] + " feature " + args[i] + " is not configured or a keypad button.");
                    return;
                }
                int group = feature.getGroup();
                int offMask = device.getLastMsgValueAsInteger(FEATURE_TYPE_KEYPAD_BUTTON_OFF_MASK, group, 0);
                if (offMask == 0) {
                    console.println("The keypad button " + args[i] + " is not part of a radio group.");
                    return;
                }
                if (!buttons.contains(group)) {
                    buttons.add(group);
                }
            }
            if (buttons.size() < 2) {
                console.println("Requires at least two buttons to clear a radio group.");
                return;
            }

            console.println("Clearing a radio group for device " + device.getAddress() + "...");
            device.clearButtonRadioGroup(buttons);
            device.setButtonToggleMode(buttons, KeypadButtonToggleMode.TOGGLE);
        }
    }

    private void refreshDevice(Console console, String thingId) {
        InsteonDevice device = getInsteonDevice(thingId);
        if (device == null) {
            console.println("The device " + thingId + " is not configured or enabled!");
        } else if (device.getProductData() == null) {
            console.println("The device " + thingId + " is unknown.");
        } else if (device.getType() == null) {
            console.println("The device " + thingId + " is unsupported.");
        } else {
            device.getLinkDB().setReload(true);
            device.resetFeaturesQueryStatus();

            if (!device.isAwake() || !device.isResponding() || !getModem().getDB().isComplete()) {
                console.println(
                        "The device " + device.getAddress() + " is scheduled to be refreshed the next time it is "
                                + (device.isBatteryPowered() ? "awake" : "responding") + ".");
            } else {
                console.println("Refreshing device " + device.getAddress() + "...");
                device.doPoll(0L);
            }
        }
    }
}
