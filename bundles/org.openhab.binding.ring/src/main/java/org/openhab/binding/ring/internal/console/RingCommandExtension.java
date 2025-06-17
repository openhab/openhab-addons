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
package org.openhab.binding.ring.internal.console;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ring.internal.handler.AccountHandler;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RingCommandExtension} is responsible for handling console commands
 *
 * @author Ben Rosenblum - Initial contribution
 */

@NonNullByDefault
@Component(service = ConsoleCommandExtension.class)
public class RingCommandExtension extends AbstractConsoleCommandExtension {

    private final Logger logger = LoggerFactory.getLogger(RingCommandExtension.class);
    private final ThingRegistry thingRegistry;

    @Activate
    public RingCommandExtension(final @Reference ThingRegistry thingRegistry) {
        super("ring", "Interact with the Ring binding channels directly.");
        this.thingRegistry = thingRegistry;
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length == 5 && "login".equals(args[1])) {
            logger.trace("Received Login Command: {} [username: {}, password: {}, 2FA: {}]", args[0], args[2], "***",
                    "***");
            Thing thing = null;
            ThingUID thingUID = new ThingUID(args[0]);
            thing = thingRegistry.get(thingUID);
            ThingHandler thingHandler = null;
            AccountHandler handler = null;
            if (thing != null) {
                thingHandler = thing.getHandler();
                if (thingHandler instanceof AccountHandler) {
                    handler = (AccountHandler) thingHandler;
                }
            }
            if (thing == null) {
                console.println("Bad thing uid '" + args[0] + "'");
                printUsage(console);
            } else if (thingHandler == null) {
                console.println("No handler initialized for the thing uid '" + args[0] + "'");
                printUsage(console);
            } else if (handler == null) {
                console.println("'" + args[0] + "' is not an Ring thing uid");
                printUsage(console);
            } else {
                logger.debug("Sending CLI login to handler {}", args[0]);
                handler.doLogin(args[2], args[3], args[4]);
            }
        } else {
            printUsage(console);
        }
    }

    @Override
    public List<String> getUsages() {
        return List.of(buildCommandUsage("<thingUID> login <username> <password> <two factor auth>",
                "Send a login request to the RING API"));
    }
}
