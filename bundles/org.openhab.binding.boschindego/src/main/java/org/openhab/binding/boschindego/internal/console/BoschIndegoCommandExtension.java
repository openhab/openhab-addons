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
package org.openhab.binding.boschindego.internal.console;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.boschindego.internal.BoschIndegoBindingConstants;
import org.openhab.binding.boschindego.internal.exceptions.IndegoAuthenticationException;
import org.openhab.binding.boschindego.internal.handler.BoschAccountHandler;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.ConsoleCommandCompleter;
import org.openhab.core.io.console.StringsCompleter;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link BoschIndegoCommandExtension} is responsible for handling console commands
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
@Component(service = ConsoleCommandExtension.class)
public class BoschIndegoCommandExtension extends AbstractConsoleCommandExtension implements ConsoleCommandCompleter {

    private static final String AUTHORIZE = "authorize";
    private static final StringsCompleter SUBCMD_COMPLETER = new StringsCompleter(List.of(AUTHORIZE), false);

    private final ThingRegistry thingRegistry;

    @Activate
    public BoschIndegoCommandExtension(final @Reference ThingRegistry thingRegistry) {
        super(BoschIndegoBindingConstants.BINDING_ID, "Interact with the Bosch Indego binding.");
        this.thingRegistry = thingRegistry;
    }

    @Override
    public void execute(String[] args, Console console) {
        String authCode;
        Thing bridge;

        if (args.length == 2 && AUTHORIZE.equals(args[0])) {
            try {
                bridge = getSingleBridge();
            } catch (IllegalStateException e) {
                console.println("Error finding indego bridge: " + e.getMessage());
                printUsage(console);
                return;
            }

            if (bridge == null) {
                console.println("No BoschIndego Bridge defined.");
                return;
            }

            authCode = args[1];
        } else if (args.length == 3 && AUTHORIZE.equals(args[1])) {
            bridge = getBridgeById(args[0]);
            if (bridge == null) {
                console.println("Unknown thing id '" + args[0] + "'");
                return;
            }

            authCode = args[2];
        } else {
            printUsage(console);
            return;
        }

        if (bridge.getHandler() instanceof BoschAccountHandler accountHandler) {
            try {
                accountHandler.authorize(authCode);
            } catch (IndegoAuthenticationException e) {
                console.println("Authorization error: " + e.getMessage());
            }
        } else {
            console.println("Bridge is not a valid BoschIndego bridge");
            printUsage(console);
        }
    }

    @Override
    public List<String> getUsages() {
        return List
                .of(buildCommandUsage("[<bridgeId>] " + AUTHORIZE + " <AuthToken>", "authorize by authorization code"));
    }

    @Override
    public @Nullable ConsoleCommandCompleter getCompleter() {
        return this;
    }

    @Override
    public boolean complete(String[] args, int cursorArgumentIndex, int cursorPosition, List<String> candidates) {
        if (cursorArgumentIndex <= 0) {
            return new StringsCompleter(
                    Stream.concat(Stream.of(AUTHORIZE), getBridgeIds().stream().map(ThingUID::getAsString)).toList(),
                    false).complete(args, cursorArgumentIndex, cursorPosition, candidates);
        } else if (cursorArgumentIndex == 1 && !AUTHORIZE.equals(args[0])) {
            return SUBCMD_COMPLETER.complete(args, cursorArgumentIndex, cursorPosition, candidates);
        }
        return false;
    }

    private List<ThingUID> getBridgeIds() {
        List<ThingUID> bridgeIds = new ArrayList<>();
        for (Thing thing : thingRegistry.getAll()) {
            if (thing.getHandler() instanceof BoschAccountHandler) {
                bridgeIds.add(thing.getUID());
            }
        }
        return bridgeIds;
    }

    private @Nullable Thing getBridgeById(String uid) {
        Thing thing;
        try {
            ThingUID thingUID = new ThingUID(uid);
            thing = thingRegistry.get(thingUID);
        } catch (IllegalArgumentException e) {
            thing = null;
        }
        return thing;
    }

    private @Nullable Thing getSingleBridge() {
        Thing bridge = null;
        for (Thing thing : thingRegistry.getAll()) {
            if (thing.getHandler() instanceof BoschAccountHandler) {
                if (bridge != null) {
                    throw new IllegalStateException("More than one BoschIndego Bridge found.");
                }
                bridge = thing;
            }
        }
        return bridge;
    }
}
