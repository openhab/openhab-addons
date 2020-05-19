/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.lgwebos.internal.console;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.console.Console;
import org.eclipse.smarthome.io.console.extensions.AbstractConsoleCommandExtension;
import org.eclipse.smarthome.io.console.extensions.ConsoleCommandExtension;
import org.openhab.binding.lgwebos.internal.handler.LGWebOSHandler;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link LGWebOSCommandExtension} is responsible for handling console commands
 *
 * @author Laurent Garnier - Initial contribution
 */

@NonNullByDefault
@Component(service = ConsoleCommandExtension.class)
public class LGWebOSCommandExtension extends AbstractConsoleCommandExtension {

    private static final String APPLICATIONS = "applications";
    private static final String CHANNELS = "channels";

    private final ThingRegistry thingRegistry;

    @Activate
    public LGWebOSCommandExtension(final @Reference ThingRegistry thingRegistry) {
        super("lgwebos", "Interact with the LG webOS binding.");
        this.thingRegistry = thingRegistry;
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length == 2) {
            LGWebOSHandler handler = null;
            try {
                ThingUID thingUID = new ThingUID(args[0]);
                Thing thing = thingRegistry.get(thingUID);
                if ((thing != null) && (thing.getHandler() != null) && (thing.getHandler() instanceof LGWebOSHandler)) {
                    handler = (LGWebOSHandler) thing.getHandler();
                }
            } catch (IllegalArgumentException e) {
                handler = null;
            }
            if (handler == null) {
                console.println("Bad thing id '" + args[0] + "'");
                printUsage(console);
            } else {
                switch (args[1]) {
                    case APPLICATIONS:
                        handler.reportApplications().forEach(console::println);
                        break;
                    case CHANNELS:
                        handler.reportChannels().forEach(console::println);
                        break;
                    default:
                        printUsage(console);
                        break;
                }
            }
        } else {
            printUsage(console);
        }
    }

    @Override
    public List<String> getUsages() {
        return Arrays.asList(new String[] { buildCommandUsage("<thingUID> " + APPLICATIONS, "list applications"),
                buildCommandUsage("<thingUID> " + CHANNELS, "list channels") });
    }
}
