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
package org.openhab.binding.insteon.internal.command;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.StringsCompleter;

/**
 *
 * The {@link ChannelCommand} represents an Insteon console channel command
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class ChannelCommand extends InsteonCommand {
    private static final String NAME = "channel";
    private static final String DESCRIPTION = "Insteon channel commands";

    private static final String LIST_ALL = "listAll";

    private static final List<String> SUBCMDS = List.of(LIST_ALL);

    public ChannelCommand(InsteonCommandExtension commandExtension) {
        super(NAME, DESCRIPTION, commandExtension);
    }

    @Override
    public List<String> getUsages() {
        return List.of(buildCommandUsage(LIST_ALL, "list available channel ids with configuration and link state"));
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length == 0) {
            printUsage(console);
            return;
        }

        switch (args[0]) {
            case LIST_ALL:
                if (args.length == 1) {
                    listAll(console);
                } else {
                    printUsage(console, args[0]);
                }
                break;
            default:
                console.println("Unknown command '" + args[0] + "'");
                printUsage(console);
                break;
        }
    }

    @Override
    public boolean complete(String[] args, int cursorArgumentIndex, int cursorPosition, List<String> candidates) {
        List<String> strings = List.of();
        if (cursorArgumentIndex == 0) {
            strings = SUBCMDS;
        }

        return new StringsCompleter(strings, false).complete(args, cursorArgumentIndex, cursorPosition, candidates);
    }

    private void listAll(Console console) {
        Map<String, String> channels = Stream
                .concat(Stream.of(getBridgeHandler()), getBridgeHandler().getChildHandlers())
                .flatMap(handler -> handler.getChannelsInfo().entrySet().stream())
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        if (channels.isEmpty()) {
            console.println("No channel available!");
        } else {
            console.println("There are " + channels.size() + " channels available:");
            print(console, channels);
        }
    }
}
