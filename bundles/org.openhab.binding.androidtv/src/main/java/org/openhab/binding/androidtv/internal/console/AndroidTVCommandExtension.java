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
package org.openhab.binding.androidtv.internal.console;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.androidtv.internal.AndroidTVHandler;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AndroidTVCommandExtension} is responsible for handling console commands
 *
 * @author Ben Rosenblum - Initial contribution
 */

@NonNullByDefault
@Component(service = ConsoleCommandExtension.class)
public class AndroidTVCommandExtension extends AbstractConsoleCommandExtension {

    private final Logger logger = LoggerFactory.getLogger(AndroidTVCommandExtension.class);
    private final ThingRegistry thingRegistry;

    @Activate
    public AndroidTVCommandExtension(final @Reference ThingRegistry thingRegistry) {
        super("androidtv", "Interact with the AndroidTV binding channels directly.");
        this.thingRegistry = thingRegistry;
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length == 3) {
            logger.trace("Received CLI Command: {} {} |||{}|||", args[0], args[1], args[2]);
            Thing thing = null;
            try {
                ThingUID thingUID = new ThingUID(args[0]);
                thing = thingRegistry.get(thingUID);
            } catch (IllegalArgumentException e) {
                thing = null;
            }
            ThingHandler thingHandler = null;
            AndroidTVHandler handler = null;
            if (thing != null) {
                thingHandler = thing.getHandler();
                if (thingHandler instanceof AndroidTVHandler) {
                    handler = (AndroidTVHandler) thingHandler;
                }
            }
            if (thing == null) {
                console.println("Bad thing uid '" + args[0] + "'");
                printUsage(console);
            } else if (thingHandler == null) {
                console.println("No handler initialized for the thing uid '" + args[0] + "'");
                printUsage(console);
            } else if (handler == null) {
                console.println("'" + args[0] + "' is not an AndroidTV thing uid");
                printUsage(console);
            } else {
                String channel = args[0] + ":" + args[1];
                ChannelUID channelUID = new ChannelUID(channel);
                Command command = (Command) new StringType(args[2]);
                logger.debug("Sending CLI Command to Handler: {} |||{}|||", channelUID.toString(), command.toString());
                handler.handleCommand(channelUID, command);
            }
        } else {
            printUsage(console);
        }
    }

    @Override
    public List<String> getUsages() {
        return List.of(buildCommandUsage("<thingUID> <channelname> <command>", "Send a command to a specific channel"));
    }
}
