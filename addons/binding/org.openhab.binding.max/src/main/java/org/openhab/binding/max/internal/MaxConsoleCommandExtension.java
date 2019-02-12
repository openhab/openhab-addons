/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.max.internal;

import static org.openhab.binding.max.internal.MaxBindingConstants.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.console.Console;
import org.eclipse.smarthome.io.console.extensions.AbstractConsoleCommandExtension;
import org.eclipse.smarthome.io.console.extensions.ConsoleCommandExtension;
import org.openhab.binding.max.internal.handler.MaxCubeBridgeHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link MaxConsoleCommandExtension} class provides additional options through the console command line.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@Component(service = ConsoleCommandExtension.class)
public class MaxConsoleCommandExtension extends AbstractConsoleCommandExtension {

    private static final String SUBCMD_BACKUP = "backup";
    private static final String SUBCMD_REBOOT = "reboot";

    private ThingRegistry thingRegistry;

    public MaxConsoleCommandExtension() {
        super("max", "Additional EQ3 MAX! commands.");
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length > 0) {
            switch (args[0]) {
                case SUBCMD_BACKUP:
                    handleBackup(console);
                    break;
                case SUBCMD_REBOOT:
                    handleReboot(args, console);
                    break;
                default:
                    console.println(String.format("Unknown MAX! sub command '%s'", args[0]));
                    printUsage(console);
                    break;
            }
        } else {
            printUsage(console);
            printMaxDevices(console, SUPPORTED_THING_TYPES_UIDS);
        }
    }

    private void handleBackup(Console console) {
        for (Thing thing : findDevices(SUPPORTED_BRIDGE_THING_TYPES_UIDS)) {
            MaxCubeBridgeHandler handler = getHandler(thing.getUID().toString());
            if (handler != null) {
                handler.backup();
                console.println(String.format("Creating backup for %s", thing.getUID().toString()));
            }
        }
    }

    private void handleReboot(String[] args, Console console) {
        if (args.length > 1) {
            MaxCubeBridgeHandler handler = getHandler(args[1]);
            if (handler != null) {
                handler.cubeReboot();
            } else {
                console.println(String.format("Could not find cube %s", args[1]));
                printMaxDevices(console, SUPPORTED_BRIDGE_THING_TYPES_UIDS);
            }
        } else {
            console.println("Specify cube to reboot.");
            printMaxDevices(console, SUPPORTED_BRIDGE_THING_TYPES_UIDS);
        }
    }

    private List<Thing> findDevices(Set<ThingTypeUID> deviceTypes) {
        List<Thing> devs = new ArrayList<Thing>();
        for (Thing thing : thingRegistry.getAll()) {
            if (deviceTypes.contains(thing.getThingTypeUID())) {
                devs.add(thing);
            }
        }
        return devs;
    }

    private MaxCubeBridgeHandler getHandler(String thingId) {
        MaxCubeBridgeHandler handler = null;
        try {
            ThingUID bridgeUID = new ThingUID(thingId);
            Thing thing = thingRegistry.get(bridgeUID);
            if ((thing != null) && (thing.getHandler() != null)
                    && (thing.getHandler() instanceof MaxCubeBridgeHandler)) {
                handler = (MaxCubeBridgeHandler) thing.getHandler();
            }
        } catch (Exception e) {
            handler = null;
        }
        return handler;
    }

    private void printMaxDevices(Console console, Set<ThingTypeUID> deviceTypes) {
        console.println("Known MAX! devices: ");
        for (Thing thing : findDevices(deviceTypes)) {
            console.println(String.format("MAX! %s device: %s%s", thing.getThingTypeUID().getId(),
                    thing.getUID().toString(), ((thing.getHandler() != null) ? "" : " (without handler)")));
        }
    }

    @Override
    public List<String> getUsages() {
        return Arrays.asList(new String[] { buildCommandUsage(SUBCMD_BACKUP, "Backup cube data"),
                buildCommandUsage(SUBCMD_REBOOT + " <thingUID>", "Reset cube") });
    }

    @Reference
    protected void setThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    protected void unsetThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = null;
    }
}
