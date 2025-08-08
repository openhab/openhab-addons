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
package org.openhab.binding.dirigera.internal.console;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dirigera.internal.Constants;
import org.openhab.binding.dirigera.internal.handler.DirigeraHandler;
import org.openhab.binding.dirigera.internal.interfaces.DebugHandler;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.ConsoleCommandCompleter;
import org.openhab.core.io.console.StringsCompleter;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.openhab.core.thing.ThingRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link DirigeraCommandExtension} is responsible for handling console commands.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@Component(service = ConsoleCommandExtension.class)
public class DirigeraCommandExtension extends AbstractConsoleCommandExtension {

    private static final String CMD_TOKEN = "token";
    private static final String CMD_JSON = "json";
    private static final String CMD_DEBUG = "debug";
    private static final List<String> COMMANDS = List.of(CMD_TOKEN, CMD_JSON, CMD_DEBUG);

    private final ThingRegistry thingRegistry;

    /**
     * Provides a completer for the DIRIGERA console commands.
     *
     * @param thingRegistry the ThingRegistry to access things and their handlers
     */
    private class DirigeraConsoleCommandCompleter implements ConsoleCommandCompleter {
        @Override
        public boolean complete(String[] args, int cursorArgumentIndex, int cursorPosition, List<String> candidates) {
            if (cursorArgumentIndex <= 0) {
                return new StringsCompleter(List.of(CMD_TOKEN, CMD_JSON, CMD_DEBUG), false).complete(args,
                        cursorArgumentIndex, cursorPosition, candidates);
            } else if (cursorArgumentIndex == 1) {
                List<String> options = new ArrayList<>();
                options.add("all");
                options.addAll(getDeviceIds());
                return new StringsCompleter(options, false).complete(args, cursorArgumentIndex, cursorPosition,
                        candidates);
            } else if (cursorArgumentIndex == 2) {
                return new StringsCompleter(List.of("true", "false"), false).complete(args, cursorArgumentIndex,
                        cursorPosition, candidates);
            }
            return false;
        }
    }

    /**
     * Decodes the console command arguments and checks for validity.
     */
    private class DirigeraConsoleCommandDecoder {
        boolean valid = false;
        String command = "";
        String target = "";
        boolean enable = false;

        DirigeraConsoleCommandDecoder(String[] args) {
            // Check parameter count and valid command, return immediately if invalid
            if (args.length == 0 || args.length > 3) {
                return;
            }
            command = args[0].toLowerCase();
            if (!COMMANDS.contains(command)) {
                return;
            }
            // Command is valid, check parameters
            switch (command) {
                case CMD_TOKEN:
                    // No parameters expected for token command
                    if (args.length == 1) {
                        valid = true;
                    }
                    break;
                case CMD_JSON:
                    // Take second parameter for device ID or 'all'
                    if (args.length == 2) {
                        target = args[1].toLowerCase();
                        valid = true;
                    }
                    break;
                case CMD_DEBUG:
                    // Three parameters expected for debug command, second as target and third as boolean
                    if (args.length == 3) {
                        target = args[1].toLowerCase();
                        String booleanCandidate = args[2].toLowerCase();
                        if (Boolean.TRUE.toString().toLowerCase().equals(booleanCandidate)
                                || Boolean.FALSE.toString().toLowerCase().equals(booleanCandidate)) {
                            enable = Boolean.valueOf(booleanCandidate);
                            valid = true;
                        }
                    }
                    break;
            }
        }
    }

    @Activate
    public DirigeraCommandExtension(final @Reference ThingRegistry thingRegistry) {
        super(Constants.BINDING_ID, "Interact with the DIRIGERA binding.");
        this.thingRegistry = thingRegistry;
    }

    @Override
    public void execute(String[] args, Console console) {
        DirigeraConsoleCommandDecoder decoder = new DirigeraConsoleCommandDecoder(args);
        if (decoder.valid) {
            switch (decoder.command) {
                case CMD_TOKEN -> printToken(console);
                case CMD_JSON -> printJSON(decoder, console);
                case CMD_DEBUG -> setDebugParameters(decoder, console);
            }
        } else {
            printUsage(console);
        }
    }

    private void printToken(Console console) {
        for (DirigeraHandler handler : getHubs()) {
            console.println(handler.getThing().getLabel() + " token: " + handler.getToken());
        }
    }

    private void printJSON(DirigeraConsoleCommandDecoder decodedCommand, Console console) {
        String output = null;
        if ("all".equals(decodedCommand.target)) {
            for (DirigeraHandler handler : getHubs()) {
                output = handler.getJSON();
            }
        } else {
            for (DebugHandler handler : getDevices()) {
                if (decodedCommand.target.equals(handler.getDeviceId())) {
                    output = handler.getJSON();
                }
            }
        }
        if (output != null) {
            console.println(output);
        } else {
            console.println("Device Id " + decodedCommand.target + " not found");
        }
    }

    private void setDebugParameters(DirigeraConsoleCommandDecoder decodedCommand, Console console) {
        boolean success = false;
        if ("all".equals(decodedCommand.target)) {
            for (DirigeraHandler handler : getHubs()) {
                handler.setDebug(decodedCommand.enable, true);
                success = true;
            }
        } else {
            for (DebugHandler handler : getDevices()) {
                if (decodedCommand.target.equals(handler.getDeviceId())) {
                    handler.setDebug(decodedCommand.enable, false);
                    success = true;
                }
            }
        }
        if (success) {
            console.println("Done");
        } else {
            console.println("Device Id " + decodedCommand.target + " not found");
        }
    }

    private List<DirigeraHandler> getHubs() {
        return thingRegistry.getAll().stream().map(thing -> thing.getHandler())
                .filter(DirigeraHandler.class::isInstance).map(DirigeraHandler.class::cast).toList();
    }

    private List<DebugHandler> getDevices() {
        return thingRegistry.getAll().stream().map(thing -> thing.getHandler()).filter(DebugHandler.class::isInstance)
                .map(DebugHandler.class::cast).toList();
    }

    private List<String> getDeviceIds() {
        return getDevices().stream().map(debugHandler -> debugHandler.getDeviceId()).toList();
    }

    @Override
    public List<String> getUsages() {
        return Arrays.asList(buildCommandUsage(CMD_TOKEN, "Get token from DIRIGERA hub"),
                buildCommandUsage(CMD_JSON + " [<deviceId> | all]", "Print JSON data"),
                buildCommandUsage(CMD_DEBUG + " [<deviceId> | all] [true | false] ",
                        "Enable / disable detailed debugging for specific / all devices"));
    }

    @Override
    public @Nullable ConsoleCommandCompleter getCompleter() {
        return new DirigeraConsoleCommandCompleter();
    }
}
