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
package org.openhab.binding.nanoleaf.internal.command;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.nanoleaf.internal.NanoleafBindingConstants;
import org.openhab.binding.nanoleaf.internal.handler.NanoleafControllerHandler;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Console commands for interacting with Nanoleaf integration
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
@Component(service = ConsoleCommandExtension.class)
public class NanoleafCommandExtension extends AbstractConsoleCommandExtension {

    private static final String CMD_LAYOUT = "layout";
    private final ThingRegistry thingRegistry;

    @Activate
    public NanoleafCommandExtension(@Reference ThingRegistry thingRegistry) {
        super("nanoleaf", "Interact with the Nanoleaf integration.");
        this.thingRegistry = thingRegistry;
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length > 0) {
            String subCommand = args[0];
            switch (subCommand) {
                case CMD_LAYOUT:
                    if (args.length == 1) {
                        thingRegistry.getAll().forEach(thing -> {
                            if (thing.getUID().getBindingId().equals(NanoleafBindingConstants.BINDING_ID)) {
                                ThingHandler handler = thing.getHandler();
                                if (handler instanceof NanoleafControllerHandler nanoleafControllerHandler) {
                                    if (!handler.getThing().isEnabled()) {
                                        console.println(
                                                "The following Nanoleaf is NOT enabled as a Thing. Enable it first to view its layout.");
                                    }
                                    String layout = nanoleafControllerHandler.getLayout();
                                    console.println("Layout of Nanoleaf controller '" + thing.getUID().getAsString()
                                            + "' with label '" + thing.getLabel() + "':" + System.lineSeparator());
                                    console.println(layout);
                                    console.println(System.lineSeparator());
                                }
                            }
                        });
                    } else if (args.length == 2) {
                        String uid = args[1];
                        Thing thing = thingRegistry.get(new ThingUID(uid));
                        if (thing != null) {
                            ThingHandler handler = thing.getHandler();
                            if (handler instanceof NanoleafControllerHandler nanoleafControllerHandler) {
                                String layout = nanoleafControllerHandler.getLayout();
                                console.println(layout);
                            } else {
                                console.println(
                                        "Thing with UID '" + uid + "' is not an initialized Nanoleaf controller.");
                            }
                        } else {
                            console.println("Thing with UID '" + uid + "' does not exist.");
                        }
                    } else {
                        printUsage(console);
                    }
                    break;

                default:
                    console.println("Unknown command '" + subCommand + "'");
                    printUsage(console);
                    break;
            }
        }
    }

    @Override
    public List<String> getUsages() {
        return Arrays.asList(buildCommandUsage(CMD_LAYOUT + " <thingUID>", "Prints the panel layout on the console."));
    }
}
