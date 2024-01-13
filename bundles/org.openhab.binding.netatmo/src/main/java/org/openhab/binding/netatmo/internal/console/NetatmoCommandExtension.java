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
package org.openhab.binding.netatmo.internal.console;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.NetatmoBindingConstants;
import org.openhab.binding.netatmo.internal.api.data.ModuleType;
import org.openhab.binding.netatmo.internal.api.dto.NAModule;
import org.openhab.binding.netatmo.internal.handler.ApiBridgeHandler;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.ConsoleCommandCompleter;
import org.openhab.core.io.console.StringsCompleter;
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
 * The {@link NetatmoCommandExtension} is responsible for handling console commands
 *
 * @author Laurent Garnier - Initial contribution
 */

@NonNullByDefault
@Component(service = ConsoleCommandExtension.class)
public class NetatmoCommandExtension extends AbstractConsoleCommandExtension implements ConsoleCommandCompleter {

    private static final String SHOW_IDS = "showIds";
    private static final StringsCompleter SUBCMD_COMPLETER = new StringsCompleter(List.of(SHOW_IDS), false);

    private final ThingRegistry thingRegistry;
    private @Nullable Console console;

    @Activate
    public NetatmoCommandExtension(final @Reference ThingRegistry thingRegistry) {
        super(NetatmoBindingConstants.BINDING_ID, "Interact with the Netatmo binding.");
        this.thingRegistry = thingRegistry;
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length == 1 && SHOW_IDS.equals(args[0])) {
            this.console = console;
            for (Thing thing : thingRegistry.getAll()) {
                ThingHandler thingHandler = thing.getHandler();
                if (thingHandler instanceof ApiBridgeHandler bridgeHandler) {
                    console.println("Account bridge: " + thing.getLabel());
                    bridgeHandler.identifyAllModulesAndApplyAction(this::printThing);
                }
            }
        } else {
            printUsage(console);
        }
    }

    private Optional<ThingUID> printThing(NAModule module, ThingUID bridgeUID) {
        Console localConsole = this.console;
        Optional<ThingUID> moduleUID = findThingUID(module.getType(), module.getId(), bridgeUID);
        if (localConsole != null && moduleUID.isPresent()) {
            String indent = "";
            for (int i = 2; i <= module.getType().getDepth(); i++) {
                indent += "    ";
            }
            localConsole.println(String.format("%s- ID \"%s\" for \"%s\" (thing type %s)", indent, module.getId(),
                    module.getName() != null ? module.getName() : "...", module.getType().thingTypeUID));
        }
        return moduleUID;
    }

    private Optional<ThingUID> findThingUID(ModuleType moduleType, String thingId, ThingUID bridgeUID) {
        return moduleType.apiName.isBlank() ? Optional.empty()
                : Optional.ofNullable(
                        new ThingUID(moduleType.thingTypeUID, bridgeUID, thingId.replaceAll("[^a-zA-Z0-9_]", "")));
    }

    @Override
    public List<String> getUsages() {
        return Arrays.asList(buildCommandUsage(SHOW_IDS, "list all devices and modules ids"));
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
