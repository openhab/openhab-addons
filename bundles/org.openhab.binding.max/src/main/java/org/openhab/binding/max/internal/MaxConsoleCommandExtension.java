/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.max.internal.handler.MaxCubeBridgeHandler;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link MaxConsoleCommandExtension} class provides additional options through the console command line.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@Component(service = ConsoleCommandExtension.class)
@NonNullByDefault
public class MaxConsoleCommandExtension extends AbstractConsoleCommandExtension {

    private static final String SUBCMD_BACKUP = "backup";
    private static final String SUBCMD_REBOOT = "reboot";
    private final ThingRegistry thingRegistry;

    @Activate
    public MaxConsoleCommandExtension(@Reference ThingRegistry thingRegistry) {
        super("max", "Additional EQ3 MAX! commands.");
        this.thingRegistry = thingRegistry;
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
                console.println(String.format("Could not find MAX! cube %s", args[1]));
                printMaxDevices(console, SUPPORTED_BRIDGE_THING_TYPES_UIDS);
            }
        } else {
            console.println("Specify MAX! cube to reboot.");
            printMaxDevices(console, SUPPORTED_BRIDGE_THING_TYPES_UIDS);
        }
    }

    private List<Thing> findDevices(Set<ThingTypeUID> deviceTypes) {
        List<Thing> devs = new ArrayList<>();
        for (Thing thing : thingRegistry.getAll()) {
            if (deviceTypes.contains(thing.getThingTypeUID())) {
                devs.add(thing);
            }
        }
        return devs;
    }

    private @Nullable MaxCubeBridgeHandler getHandler(String thingId) {
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
        return Arrays.asList(new String[] { buildCommandUsage(SUBCMD_BACKUP, "Backup MAX! cube data"),
                buildCommandUsage(SUBCMD_REBOOT + " <thingUID>", "Reset MAX! cube") });
    }
}
