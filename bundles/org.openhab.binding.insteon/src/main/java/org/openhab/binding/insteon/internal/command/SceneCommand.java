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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.insteon.internal.device.DeviceFeature;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.device.InsteonDevice;
import org.openhab.binding.insteon.internal.device.InsteonScene;
import org.openhab.binding.insteon.internal.device.OnLevel;
import org.openhab.binding.insteon.internal.device.RampRate;
import org.openhab.binding.insteon.internal.handler.InsteonDeviceHandler;
import org.openhab.binding.insteon.internal.handler.InsteonSceneHandler;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.StringsCompleter;

/**
 *
 * The {@link SceneCommand} represents an Insteon console scene command
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class SceneCommand extends InsteonCommand {
    private static final String NAME = "scene";
    private static final String DESCRIPTION = "Insteon scene commands";

    private static final String LIST_ALL = "listAll";
    private static final String LIST_DETAILS = "listDetails";
    private static final String ADD_DEVICE = "addDevice";
    private static final String REMOVE_DEVICE = "removeDevice";

    private static final List<String> SUBCMDS = List.of(LIST_ALL, LIST_DETAILS, ADD_DEVICE, REMOVE_DEVICE);

    private static final String NEW_OPTION = "--new";

    public SceneCommand(InsteonCommandExtension commandExtension) {
        super(NAME, DESCRIPTION, commandExtension);
    }

    @Override
    public List<String> getUsages() {
        return List.of(buildCommandUsage(LIST_ALL, "list configured Insteon scenes with related channels and status"),
                buildCommandUsage(LIST_DETAILS + " <thingId>", "list details for a configured Insteon scene"),
                buildCommandUsage(ADD_DEVICE + " " + NEW_OPTION + "|<scene> <device> <feature> <onLevel> [<rampRate>]",
                        "add an Insteon device feature to a new or configured Insteon scene"),
                buildCommandUsage(REMOVE_DEVICE + " <scene> <device> <feature>",
                        "remove an Insteon device feature from a configured Insteon scene"));
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
            case LIST_DETAILS:
                if (args.length == 2) {
                    listDetails(console, args[1]);
                } else {
                    printUsage(console, args[0]);
                }
                break;
            case ADD_DEVICE:
                if (args.length >= 5 && args.length <= 6) {
                    addDevice(console, args);
                } else {
                    printUsage(console, args[0]);
                }
                break;
            case REMOVE_DEVICE:
                if (args.length == 4) {
                    removeDevice(console, args);
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
                case LIST_DETAILS:
                case REMOVE_DEVICE:
                    strings = getInsteonSceneHandlers().map(InsteonSceneHandler::getThingId).toList();
                    break;
                case ADD_DEVICE:
                    strings = Stream.concat(Stream.of(NEW_OPTION),
                            getInsteonSceneHandlers().map(InsteonSceneHandler::getThingId)).toList();
                    break;
            }
        } else if (cursorArgumentIndex == 2) {
            InsteonScene scene = getInsteonScene(args[1]);
            switch (args[0]) {
                case ADD_DEVICE:
                    strings = getInsteonDeviceHandlers().filter(handler -> {
                        InsteonDevice device = handler.getDevice();
                        return device != null && !device.getResponderFeatures().isEmpty();
                    }).map(InsteonDeviceHandler::getThingId).toList();
                    break;
                case REMOVE_DEVICE:
                    strings = getInsteonDeviceHandlers().filter(handler -> {
                        InsteonDevice device = handler.getDevice();
                        return device != null && scene != null && scene.hasEntry(device.getAddress());
                    }).map(InsteonDeviceHandler::getThingId).toList();
                    break;
            }
        } else if (cursorArgumentIndex == 3) {
            InsteonScene scene = getInsteonScene(args[1]);
            InsteonDevice device = getInsteonDevice(args[2]);
            switch (args[0]) {
                case ADD_DEVICE:
                    if (device != null) {
                        strings = device.getResponderFeatures().stream().map(DeviceFeature::getName).toList();
                    }
                    break;
                case REMOVE_DEVICE:
                    if (device != null && scene != null) {
                        strings = scene.getFeatures(device.getAddress()).stream().map(DeviceFeature::getName).toList();
                    }
                    break;
            }
        } else if (cursorArgumentIndex == 4) {
            InsteonDevice device = getInsteonDevice(args[2]);
            DeviceFeature feature = device != null ? device.getFeature(args[3]) : null;
            switch (args[0]) {
                case ADD_DEVICE:
                    if (feature != null) {
                        strings = OnLevel.getSupportedValues(feature.getType());
                    }
                    break;
            }
        } else if (cursorArgumentIndex == 5) {
            InsteonDevice device = getInsteonDevice(args[2]);
            DeviceFeature feature = device != null ? device.getFeature(args[3]) : null;
            switch (args[0]) {
                case ADD_DEVICE:
                    if (feature != null && RampRate.supportsFeatureType(feature.getType())) {
                        strings = Stream.of(RampRate.values()).map(String::valueOf).toList();
                    }
                    break;
            }
        }

        return new StringsCompleter(strings, false).complete(args, cursorArgumentIndex, cursorPosition, candidates);
    }

    private void listAll(Console console) {
        Map<String, String> scenes = getInsteonSceneHandlers()
                .collect(Collectors.toMap(InsteonSceneHandler::getThingId, InsteonSceneHandler::getThingInfo));
        if (scenes.isEmpty()) {
            console.println("No scene configured or enabled!");
        } else {
            console.println("There are " + scenes.size() + " scenes configured:");
            print(console, scenes);
        }
    }

    private void listDetails(Console console, String thingId) {
        InsteonScene scene = getInsteonScene(thingId);
        if (scene == null) {
            console.println("The scene " + thingId + " is not configured or enabled!");
            return;
        }
        List<InsteonAddress> devices = scene.getDevices();
        List<String> entries = scene.getEntries().stream().map(String::valueOf).sorted().toList();
        if (devices.isEmpty()) {
            console.println("The scene " + scene.getGroup() + " has no associated device configured or enabled.");
        } else {
            console.println("The scene " + scene.getGroup() + " is currently " + scene.getState() + ". It controls "
                    + devices.size() + " devices:" + (scene.isComplete() ? "" : " (Partial)"));
            print(console, entries);
        }
    }

    private void addDevice(Console console, String[] args) {
        InsteonScene scene;
        if (NEW_OPTION.equals(args[1])) {
            int group = getModem().getDB().getNextAvailableBroadcastGroup();
            if (group != -1) {
                scene = InsteonScene.makeScene(group, getModem());
            } else {
                console.println("Unable to create new scene, no broadcast group available!");
                return;
            }
        } else {
            scene = getInsteonScene(args[1]);
            if (scene == null) {
                console.println("The scene " + args[1] + " is not configured or enabled!");
                return;
            }
        }
        InsteonDevice device = getInsteonDevice(args[2]);
        if (device == null) {
            console.println("The device " + args[2] + " is not configured or enabled!");
        } else if (!device.getLinkDB().isComplete()) {
            console.println("The link database for device " + args[2] + " is not loaded yet.");
        } else if (!getModem().getDB().isComplete()) {
            console.println("The modem database is not loaded yet.");
        } else {
            DeviceFeature feature = device.getFeature(args[3]);
            if (feature == null) {
                console.println("The device " + args[2] + " feature " + args[3] + " is not configured!");
            } else if (!feature.isResponderFeature()) {
                console.println("The device " + args[2] + " feature " + args[3] + " is not a responder feature.");
            } else {
                int onLevel = OnLevel.getHexValue(args[4], feature.getType());
                if (onLevel == -1) {
                    console.println("The feature " + args[3] + " onLevel " + args[4] + " is not valid.");
                    return;
                }
                RampRate rampRate = null;
                if (RampRate.supportsFeatureType(feature.getType())) {
                    rampRate = args.length == 6 ? RampRate.fromString(args[5]) : RampRate.DEFAULT;
                    if (rampRate == null) {
                        console.println("The feature " + args[3] + " rampRate " + args[5] + " is not valid.");
                        return;
                    }
                }

                console.println("Adding device " + device.getAddress() + " feature " + feature.getName() + " to scene "
                        + scene.getGroup() + ".");
                scene.addDeviceFeature(device, onLevel, rampRate, feature.getComponentId());
            }
        }
    }

    private void removeDevice(Console console, String[] args) {
        InsteonScene scene = getInsteonScene(args[1]);
        InsteonDevice device = getInsteonDevice(args[2]);
        if (scene == null) {
            console.println("The scene " + args[1] + " is not configured or enabled!");
        } else if (device == null) {
            console.println("The device " + args[2] + " is not configured or enabled!");
        } else if (!device.getLinkDB().isComplete()) {
            console.println("The link database for device " + args[2] + " is not loaded yet.");
        } else if (!getModem().getDB().isComplete()) {
            console.println("The modem database is not loaded yet.");
        } else {
            DeviceFeature feature = device.getFeature(args[3]);
            if (feature == null) {
                console.println("The device " + args[2] + " feature " + args[3] + " is not configured!");
            } else if (!scene.hasEntry(device.getAddress(), feature.getName())) {
                console.println("The device " + args[2] + " feature " + args[3] + " is not associated to scene"
                        + args[1] + ".");
            } else {
                console.println("Removing device " + device.getAddress() + " feature " + feature.getName()
                        + " from scene " + scene.getGroup() + ".");
                scene.removeDeviceFeature(device, feature.getComponentId());
            }
        }
    }
}
