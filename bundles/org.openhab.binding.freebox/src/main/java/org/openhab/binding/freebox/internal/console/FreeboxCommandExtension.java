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
package org.openhab.binding.freebox.internal.console;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freebox.internal.FreeboxBindingConstants;
import org.openhab.binding.freebox.internal.config.ServerConfiguration;
import org.openhab.binding.freebox.internal.handler.ServerHandler;
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
 * The {@link FreeboxCommandExtension} is responsible for handling console commands
 *
 * @author Laurent Garnier - Initial contribution
 */

@NonNullByDefault
@Component(service = ConsoleCommandExtension.class)
public class FreeboxCommandExtension extends AbstractConsoleCommandExtension {

    private final ThingRegistry thingRegistry;

    @Activate
    public FreeboxCommandExtension(final @Reference ThingRegistry thingRegistry) {
        super(FreeboxBindingConstants.BINDING_ID, "Interact with the freebox binding.");
        this.thingRegistry = thingRegistry;
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length == 2) {
            ServerHandler handler = null;
            try {
                ThingUID thingUID = new ThingUID(args[0]);
                Thing thing = thingRegistry.get(thingUID);
                if (thing != null) {
                    ThingHandler thingHandler = thing.getHandler();
                    if (thingHandler instanceof ServerHandler) {
                        handler = (ServerHandler) thingHandler;
                    }
                }
            } catch (IllegalArgumentException e) {
                handler = null;
            }
            if (handler == null) {
                console.println(String.format("Bad thing id '%s'", args[0]));
                printUsage(console);
            } else {
                switch (args[1]) {
                    case ServerConfiguration.APP_TOKEN:
                        String token = handler.getAppToken();
                        console.println(
                                String.format("Your application token is %s", token != null ? token : "undefined"));
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
        return Arrays.asList(
                buildCommandUsage("<bridgeUID> " + ServerConfiguration.APP_TOKEN, "show the application token"));
    }
}
