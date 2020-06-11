/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.io.console.Console;
import org.eclipse.smarthome.io.console.extensions.AbstractConsoleCommandExtension;
import org.eclipse.smarthome.io.console.extensions.ConsoleCommandExtension;
import org.openhab.binding.insteon.internal.handler.InsteonNetworkHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 *
 * Console commands for the Insteon binding
 *
 * @author Rob Nielsen - Initial contribution
 */
@NonNullByDefault
@Component(service = ConsoleCommandExtension.class)
public class InsteonCommandExtension extends AbstractConsoleCommandExtension {
    private static final String DISPLAY_DEVICES = "display_devices";
    private static final String DISPLAY_CHANNELS = "display_channels";
    private static final String DISPLAY_LOCAL_DATABASE = "display_local_database";

    @Nullable
    private InsteonNetworkHandler handler;

    public InsteonCommandExtension() {
        super("insteon", "Interact with the Insteon integration.");
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length > 0) {
            InsteonNetworkHandler handler = this.handler; // fix eclipse warnings about nullable
            if (handler == null) {
                console.println("No Insteon network bridge configured.");
                printUsage(console);
            } else {
                String subCommand = args[0];
                switch (subCommand) {
                    case DISPLAY_DEVICES:
                        handler.displayDevices(console);
                        break;
                    case DISPLAY_CHANNELS:
                        handler.displayChannels(console);
                        break;
                    case DISPLAY_LOCAL_DATABASE:
                        handler.displayLocalDatabase(console);
                        break;
                    default:
                        console.println("Unknown command '" + subCommand + "'");
                        printUsage(console);
                        break;
                }
            }
        } else {
            printUsage(console);
        }
    }

    @Override
    public List<String> getUsages() {
        return Arrays.asList(new String[] {
                buildCommandUsage(DISPLAY_DEVICES, "display devices that are online, along with available channels"),
                buildCommandUsage(DISPLAY_CHANNELS,
                        "display channels that are linked, along with configuration information"),
                buildCommandUsage(DISPLAY_LOCAL_DATABASE, "display Insteon PLM or hub database details") });
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
    public void setInsteonNetworkHandler(InsteonNetworkHandler handler) {
        this.handler = handler;
    }

    public void unsetInsteonNetworkHandler(InsteonNetworkHandler handler) {
        this.handler = null;
    }
}
