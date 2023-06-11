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
package org.openhab.binding.meater.internal.console;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.meater.internal.MeaterBindingConstants;
import org.openhab.binding.meater.internal.handler.MeaterBridgeHandler;
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
 * The {@link MeaterCommandExtension} is responsible for handling console commands
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
@Component(service = ConsoleCommandExtension.class)
public class MeaterCommandExtension extends AbstractConsoleCommandExtension implements ConsoleCommandCompleter {

    private static final String SHOW_IDS = "showIds";
    private static final StringsCompleter SUBCMD_COMPLETER = new StringsCompleter(List.of(SHOW_IDS), false);

    private final ThingRegistry thingRegistry;

    @Activate
    public MeaterCommandExtension(final @Reference ThingRegistry thingRegistry) {
        super(MeaterBindingConstants.BINDING_ID, "Interact with the Meater binding.");
        this.thingRegistry = thingRegistry;
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length != 1 || !SHOW_IDS.equals(args[0])) {
            printUsage(console);
            return;
        }

        for (Thing thing : thingRegistry.getAll()) {
            ThingHandler thingHandler = thing.getHandler();
            if (thingHandler instanceof MeaterBridgeHandler) {
                console.println("API bridge: " + thing.getLabel());
                ((MeaterBridgeHandler) thingHandler).getMeaterThings().entrySet().stream().forEach(t -> {
                    console.println("    - ID: " + t.getKey() + " (ambient temperature: "
                            + t.getValue().temperature.ambient + ")");
                });
            }
        }
    }

    @Override
    public List<String> getUsages() {
        return Arrays.asList(buildCommandUsage(SHOW_IDS, "list all probes"));
    }

    @Override
    public @Nullable ConsoleCommandCompleter getCompleter() {
        return this;
    }

    @Override
    public boolean complete(String[] args, int cursorArgumentIndex, int cursorPosition, List<String> candidates) {
        if (cursorArgumentIndex <= 0) {
            return SUBCMD_COMPLETER.complete(args, cursorArgumentIndex, cursorPosition, candidates);
        }
        return false;
    }
}
