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

import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.KNXBindingConstants;
import org.openhab.binding.knx.internal.factory.KNXHandlerFactory;
import org.openhab.binding.knx.internal.handler.KNXBridgeBaseThingHandler;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.ConsoleCommandCompleter;
import org.openhab.core.io.console.StringsCompleter;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
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

    private static final String CMD_LIST_UNKNOWN_GA = "list-unknown-ga";
    private static final StringsCompleter CMD_COMPLETER = new StringsCompleter(List.of(CMD_LIST_UNKNOWN_GA), false);

    private final KNXHandlerFactory knxHandlerFactory;

    @Activate
    public KNXCommandExtension(final @Reference KNXHandlerFactory knxHandlerFactory) {
        super(KNXBindingConstants.BINDING_ID, "Interact with KNX devices.");
        this.knxHandlerFactory = knxHandlerFactory;
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length == 1 && CMD_LIST_UNKNOWN_GA.equalsIgnoreCase(args[0])) {
            for (KNXBridgeBaseThingHandler bridgeHandler : knxHandlerFactory.getBridges()) {
                console.println("KNX bridge \"" + bridgeHandler.getThing().getLabel()
                        + "\": group address, type, number of bytes, and number of occurence since last reload of binding:");
                for (Entry<String, Long> entry : bridgeHandler.getCommandExtensionData().unknownGA().entrySet()) {
                    console.println(entry.getKey() + " " + entry.getValue());
                }
            }
            return;
        }
        printUsage(console);
    }

    @Override
    public List<String> getUsages() {
        return List
                .of(buildCommandUsage(CMD_LIST_UNKNOWN_GA, "list group addresses which are not configured in openHAB"));
    }

    @Override
    public @Nullable ConsoleCommandCompleter getCompleter() {
        return this;
    }

    @Override
    public boolean complete(String[] args, int cursorArgumentIndex, int cursorPosition, List<String> candidates) {
        if (cursorArgumentIndex <= 0) {
            return CMD_COMPLETER.complete(args, cursorArgumentIndex, cursorPosition, candidates);
        }
        return false;
    }
}
