/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.lutron.internal.hw;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Console commands to send commands to a legacy HomeWorks processor and to watch what it sends back.
 *
 * @author Christopher Smith - Initial contribution
 */
@NonNullByDefault
@Component(service = ConsoleCommandExtension.class)
public class HwConsoleCommandExtension extends AbstractConsoleCommandExtension {
    private static final String HW = "hw";
    private static final String SEND = "send";
    private static final String LISTEN = "listen";

    private static final long RESPONSE_WAIT_MS = 2000;
    private static final int DEFAULT_LISTEN_SECONDS = 30;
    private static final int MAX_LISTEN_SECONDS = 600;

    private final Logger logger = LoggerFactory.getLogger(HwConsoleCommandExtension.class);
    private final ThingRegistry thingRegistry;

    @Activate
    public HwConsoleCommandExtension(final @Reference ThingRegistry thingRegistry) {
        super("lutron", "Interact with Lutron processors through the Lutron binding.");
        this.thingRegistry = thingRegistry;
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length >= 3 && HW.equals(args[0])) {
            HwBridgeHandler bridge = findBridge(args[2], console);
            if (bridge == null) {
                return;
            }
            if (SEND.equals(args[1]) && args.length >= 4) {
                String command = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                logger.debug("Console: sending '{}' through {}", command, args[2]);
                capture(bridge, console, command, RESPONSE_WAIT_MS);
                return;
            } else if (LISTEN.equals(args[1]) && args.length <= 4) {
                int seconds = DEFAULT_LISTEN_SECONDS;
                if (args.length == 4) {
                    try {
                        seconds = Math.min(Math.max(Integer.parseInt(args[3]), 1), MAX_LISTEN_SECONDS);
                    } catch (NumberFormatException e) {
                        console.println("Invalid number of seconds '" + args[3] + "'");
                        printUsage(console);
                        return;
                    }
                }
                logger.debug("Console: listening to {} for {} seconds", args[2], seconds);
                console.println("Listening to " + args[2] + " for " + seconds + " seconds...");
                capture(bridge, console, null, seconds * 1000L);
                return;
            }
        }
        printUsage(console);
    }

    private @Nullable HwBridgeHandler findBridge(String uid, Console console) {
        Thing thing;
        try {
            thing = thingRegistry.get(new ThingUID(uid));
        } catch (IllegalArgumentException e) {
            console.println("Bad thing UID '" + uid + "'");
            return null;
        }
        if (thing == null) {
            console.println("No thing with UID '" + uid + "'");
            return null;
        }
        if (!(thing.getHandler() instanceof HwBridgeHandler bridge)) {
            console.println("'" + uid + "' is not a HomeWorks bridge (hwserialbridge or hwtelnetbridge)");
            return null;
        }
        if (thing.getStatus() != ThingStatus.ONLINE) {
            console.println("Bridge '" + uid + "' is " + thing.getStatus() + ": " + thing.getStatusInfo());
            return null;
        }
        return bridge;
    }

    /**
     * Prints every line the bridge receives for the given time, after sending a command if one is given.
     */
    private void capture(HwBridgeHandler bridge, Console console, @Nullable String command, long millis) {
        Consumer<String> printer = line -> console.println("< " + line);
        bridge.addLineListener(printer);
        try {
            if (command != null) {
                console.println("> " + command);
                bridge.sendCommand(command);
            }
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            bridge.removeLineListener(printer);
        }
        console.println("Done.");
    }

    @Override
    public List<String> getUsages() {
        return List.of(
                buildCommandUsage(HW + " " + SEND + " <bridgeUID> <command>",
                        "send a command to a HomeWorks processor and show what it replies within " + RESPONSE_WAIT_MS
                                + " ms, e.g. KBMON"),
                buildCommandUsage(HW + " " + LISTEN + " <bridgeUID> [seconds]",
                        "show every message a HomeWorks processor sends, for 30 seconds by default"));
    }
}
