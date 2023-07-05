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
package org.openhab.binding.freeboxos.internal.console;

import static org.openhab.binding.freeboxos.internal.config.FreeboxOsConfiguration.APP_TOKEN;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants;
import org.openhab.binding.freeboxos.internal.handler.FreeboxOsHandler;
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
 * The {@link FreeboxOsCommandExtension} is responsible for handling console commands
 *
 * @author Gaël L'hopital - Initial contribution
 */

@NonNullByDefault
@Component(service = ConsoleCommandExtension.class)
public class FreeboxOsCommandExtension extends AbstractConsoleCommandExtension {

    private final ThingRegistry thingRegistry;

    @Activate
    public FreeboxOsCommandExtension(final @Reference ThingRegistry thingRegistry) {
        super(FreeboxOsBindingConstants.BINDING_ID, "Interact with the Freebox OS binding.");
        this.thingRegistry = thingRegistry;
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length == 2) {
            Thing thing = null;
            try {
                ThingUID thingUID = new ThingUID(args[0]);
                thing = thingRegistry.get(thingUID);
            } catch (IllegalArgumentException e) {
                thing = null;
            }
            ThingHandler thingHandler = null;
            FreeboxOsHandler handler = null;
            if (thing != null) {
                thingHandler = thing.getHandler();
                if (thingHandler instanceof FreeboxOsHandler) {
                    handler = (FreeboxOsHandler) thingHandler;
                }
            }
            if (thing == null) {
                console.println("Bad thing id '" + args[0] + "'");
                printUsage(console);
            } else if (thingHandler == null) {
                console.println("No handler initialized for the thing id '" + args[0] + "'");
                printUsage(console);
            } else if (handler == null) {
                console.println("'" + args[0] + "' is not a freebox bridge id");
                printUsage(console);
            } else {
                switch (args[1]) {
                    case APP_TOKEN:
                        String token = handler.getConfiguration().appToken;
                        console.println("Your application token is " + (token.isEmpty() ? "undefined" : token));
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
        return Arrays.asList(buildCommandUsage(String.format("<bridgeUID> %s show the application token", APP_TOKEN)));
    }
}
