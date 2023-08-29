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
package org.openhab.binding.hdpowerview.internal.console;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hdpowerview.internal.GatewayWebTargets;
import org.openhab.binding.hdpowerview.internal.HDPowerViewBindingConstants;
import org.openhab.binding.hdpowerview.internal.HDPowerViewWebTargets;
import org.openhab.binding.hdpowerview.internal.dto.ShadeData;
import org.openhab.binding.hdpowerview.internal.dto.gen3.Shade;
import org.openhab.binding.hdpowerview.internal.dto.responses.RepeaterData;
import org.openhab.binding.hdpowerview.internal.exceptions.HubException;
import org.openhab.binding.hdpowerview.internal.handler.GatewayBridgeHandler;
import org.openhab.binding.hdpowerview.internal.handler.HDPowerViewHubHandler;
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
 * The {@link HDPowerViewCommandExtension} is responsible for handling console commands
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
@Component(service = ConsoleCommandExtension.class)
public class HDPowerViewCommandExtension extends AbstractConsoleCommandExtension implements ConsoleCommandCompleter {

    private static final String SHOW_IDS = "showIds";
    private static final StringsCompleter SUBCMD_COMPLETER = new StringsCompleter(List.of(SHOW_IDS), false);

    private final ThingRegistry thingRegistry;

    @Activate
    public HDPowerViewCommandExtension(final @Reference ThingRegistry thingRegistry) {
        super(HDPowerViewBindingConstants.BINDING_ID, "Interact with the Hunter Douglas PowerView binding.");
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
            if (thingHandler instanceof HDPowerViewHubHandler hubHandler) {
                console.println("Generation 1/2 API hub: " + thing.getLabel());
                HDPowerViewWebTargets webTargets = hubHandler.getWebTargets();

                try {
                    List<ShadeData> shades = webTargets.getShades().shadeData;
                    if (shades != null) {
                        console.println(" - Shades:");
                        for (ShadeData shade : shades) {
                            console.println("    - ID: " + shade.id + " (" + shade.getName() + ")");
                        }
                    }

                    List<RepeaterData> repeaters = webTargets.getRepeaters().repeaterData;
                    if (repeaters != null) {
                        console.println(" - Repeaters:");
                        for (RepeaterData repeater : repeaters) {
                            console.println("    - ID: " + repeater.id + " (" + repeater.getName() + ")");
                        }
                    }
                } catch (HubException e) {
                    console.println("Error retrieving ID's: " + e.getMessage());
                }
            } else if (thingHandler instanceof GatewayBridgeHandler gatewayHandler) {
                console.println("Generation 3 API gateway: " + thing.getLabel());
                GatewayWebTargets webTargets = gatewayHandler.getWebTargets();

                try {
                    List<Shade> shades = webTargets.getShades();
                    if (!shades.isEmpty()) {
                        console.println(" - Shades:");
                        for (Shade shade : shades) {
                            console.println("    - ID: " + shade.getId() + " (" + shade.getName() + ")");
                        }
                    }
                } catch (HubException e) {
                    console.println("Error retrieving ID's: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public List<String> getUsages() {
        return Arrays.asList(buildCommandUsage(SHOW_IDS, "list all shades and repeaters"));
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
