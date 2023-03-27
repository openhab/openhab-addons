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
package org.openhab.binding.knx.internal.console;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.KNXBindingConstants;
import org.openhab.binding.knx.internal.handler.KNXBridgeBaseThingHandler;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.ConsoleCommandCompleter;
import org.openhab.core.io.console.StringsCompleter;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link KNXCommandExtension} is responsible for handling console commands
 *
 * @author Holger Friedrich - Initial contribution
 */
@NonNullByDefault
@Component(service = ConsoleCommandExtension.class)
public class KNXCommandExtension extends AbstractConsoleCommandExtension implements ConsoleCommandCompleter {

    private static final String CMD_LIST_UNKNOWN_GA = "list_unknown_ga";
    private static final StringsCompleter CMD_COMPLETER = new StringsCompleter(List.of(CMD_LIST_UNKNOWN_GA), false);

    private final ThingRegistry thingRegistry;

    @Activate
    public KNXCommandExtension(final @Reference ThingRegistry thingRegistry) {
        super(KNXBindingConstants.BINDING_ID, "Interact with KNX devices.");
        this.thingRegistry = thingRegistry;
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length == 1 && CMD_LIST_UNKNOWN_GA.equalsIgnoreCase(args[0])) {

            for (Thing thing : thingRegistry.getAll()) {
                ThingHandler thingHandler = thing.getHandler();
                if (thingHandler instanceof KNXBridgeBaseThingHandler handler) {
                    console.println("KNX bridge \"" + thing.getLabel()
                            + "\", group addresses and number of occurence since last reload of binding:");
                    console.println(handler.commandExtensionData.unknownGA.toString());
                }
            }
            return;
        }
        printUsage(console);
    }

    @Override
    public List<String> getUsages() {
        return Arrays.asList(
                buildCommandUsage(CMD_LIST_UNKNOWN_GA, "list group addresses which are not configured in openHAB"));
    }

    @Override
    public @Nullable ConsoleCommandCompleter getCompleter() {
        return this;
    }

    @Override
    public boolean complete(String[] args, int cursorArgumentIndex, int cursorPosition, List<String> candidates) {
        if (cursorArgumentIndex <= 0) {
            return CMD_COMPLETER.complete(args, cursorArgumentIndex, cursorPosition, candidates);
        } else if (cursorArgumentIndex == 1) {
            // no command with completable params
        }
        return false;
    }
}
