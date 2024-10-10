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
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.Device;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.device.InsteonDevice;
import org.openhab.binding.insteon.internal.device.InsteonEngine;
import org.openhab.binding.insteon.internal.device.InsteonModem;
import org.openhab.binding.insteon.internal.device.InsteonScene;
import org.openhab.binding.insteon.internal.device.X10Address;
import org.openhab.binding.insteon.internal.device.X10Device;
import org.openhab.binding.insteon.internal.handler.InsteonBridgeHandler;
import org.openhab.binding.insteon.internal.handler.InsteonDeviceHandler;
import org.openhab.binding.insteon.internal.handler.InsteonSceneHandler;
import org.openhab.binding.insteon.internal.handler.InsteonThingHandler;
import org.openhab.binding.insteon.internal.handler.X10DeviceHandler;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.ConsoleCommandCompleter;

/**
 *
 * The {@link InsteonCommand} represents a base Insteon console command
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public abstract class InsteonCommand implements ConsoleCommandCompleter {
    private final String name;
    private final String description;
    private final InsteonCommandExtension commandExtension;

    public InsteonCommand(String name, String description, InsteonCommandExtension commandExtension) {
        this.name = name;
        this.description = description;
        this.commandExtension = commandExtension;
    }

    public String getCommand() {
        return commandExtension.getCommand();
    }

    public String getSubCommand() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public abstract List<String> getUsages();

    public abstract void execute(String[] args, Console console);

    protected String buildCommandUsage(final String syntax, final String description) {
        return String.format("%s %s %s - %s", getCommand(), getSubCommand(), syntax, description);
    }

    protected void printUsage(Console console) {
        getUsages().forEach(console::printUsage);
    }

    protected void printUsage(Console console, String cmd) {
        getUsages().stream().filter(usage -> usage.split(" ")[2].equals(cmd)).findAny().ifPresent(console::printUsage);
    }

    protected void print(Console console, List<String> list) {
        list.forEach(console::println);
    }

    protected void print(Console console, Map<String, String> map) {
        map.entrySet().stream().sorted(Entry.comparingByKey()).map(Entry::getValue).forEach(console::println);
    }

    protected InsteonBridgeHandler getBridgeHandler() {
        return Objects.requireNonNull(commandExtension.getBridgeHandler());
    }

    protected @Nullable InsteonBridgeHandler getBridgeHandler(String thingId) {
        return getBridgeHandlers().filter(handler -> handler.getThingId().equals(thingId)).findFirst().orElse(null);
    }

    protected Stream<InsteonBridgeHandler> getBridgeHandlers() {
        return commandExtension.getBridgeHandlers();
    }

    protected void setBridgeHandler(InsteonBridgeHandler handler) {
        commandExtension.setBridgeHandler(handler);
    }

    protected Stream<InsteonThingHandler> getAllDeviceHandlers() {
        return Stream.concat(getInsteonDeviceHandlers(), getX10DeviceHandlers());
    }

    protected Stream<InsteonDeviceHandler> getInsteonDeviceHandlers() {
        return getBridgeHandler().getChildHandlers().filter(InsteonDeviceHandler.class::isInstance)
                .map(InsteonDeviceHandler.class::cast);
    }

    protected Stream<X10DeviceHandler> getX10DeviceHandlers() {
        return getBridgeHandler().getChildHandlers().filter(X10DeviceHandler.class::isInstance)
                .map(X10DeviceHandler.class::cast);
    }

    protected Stream<InsteonSceneHandler> getInsteonSceneHandlers() {
        return getBridgeHandler().getChildHandlers().filter(InsteonSceneHandler.class::isInstance)
                .map(InsteonSceneHandler.class::cast);
    }

    protected InsteonModem getModem() {
        return Objects.requireNonNull(getBridgeHandler().getModem());
    }

    protected @Nullable Device getDevice(String thingId) {
        if (InsteonAddress.isValid(thingId)) {
            return getModem().getDevice(new InsteonAddress(thingId));
        } else if (X10Address.isValid(thingId)) {
            return getModem().getDevice(new X10Address(thingId));
        } else {
            return getAllDeviceHandlers().filter(handler -> handler.getThingId().equals(thingId))
                    .map(InsteonThingHandler::getDevice).findFirst().orElse(null);
        }
    }

    protected @Nullable InsteonDevice getInsteonDevice(String thingId) {
        if (InsteonAddress.isValid(thingId)) {
            return getModem().getInsteonDevice(new InsteonAddress(thingId));
        } else {
            return getInsteonDeviceHandlers().filter(handler -> handler.getThingId().equals(thingId))
                    .map(InsteonDeviceHandler::getDevice).findFirst().orElse(null);
        }
    }

    protected @Nullable X10Device getX10Device(String thingId) {
        if (X10Address.isValid(thingId)) {
            return getModem().getX10Device(new X10Address(thingId));
        } else {
            return getX10DeviceHandlers().filter(handler -> handler.getThingId().equals(thingId))
                    .map(X10DeviceHandler::getDevice).findFirst().orElse(null);
        }
    }

    protected @Nullable InsteonScene getInsteonScene(String thingId) {
        if (InsteonScene.isValidGroup(thingId)) {
            return getModem().getInsteonScene(Integer.parseInt(thingId));
        } else {
            return getInsteonSceneHandlers().filter(handler -> handler.getThingId().equals(thingId))
                    .map(InsteonSceneHandler::getScene).findFirst().orElse(null);
        }
    }

    protected InsteonEngine getInsteonEngine(String thingId) {
        InsteonDevice device = getInsteonDevice(thingId);
        return device != null ? device.getInsteonEngine() : InsteonEngine.UNKNOWN;
    }
}
