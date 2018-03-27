/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.homekit.internal;

import java.util.Arrays;
import java.util.List;

import org.eclipse.smarthome.core.storage.StorageService;
import org.eclipse.smarthome.io.console.Console;
import org.eclipse.smarthome.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.io.homekit.Homekit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Console commands for interacting with the HomeKit integration
 *
 * @author Andy Lintner
 */
public class HomekitCommandExtension extends AbstractConsoleCommandExtension {

    private static final String SUBCMD_CLEAR_PAIRINGS = "clearPairings";
    private static final String SUBCMD_ALLOW_UNAUTHENTICATED = "allowUnauthenticated";

    private final Logger logger = LoggerFactory.getLogger(HomekitCommandExtension.class);
    private StorageService storageService;
    private Homekit homekit;

    public HomekitCommandExtension() {
        super("homekit", "Interact with the HomeKit integration.");
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length > 0) {
            String subCommand = args[0];
            switch (subCommand) {
                case SUBCMD_CLEAR_PAIRINGS:
                    clearHomekitPairings(console);
                    break;

                case SUBCMD_ALLOW_UNAUTHENTICATED:
                    if (args.length > 1) {
                        boolean allow = Boolean.valueOf(args[1]);
                        allowUnauthenticatedHomekitRequests(allow, console);
                    } else {
                        console.println("true/false is required as an argument");
                    }
                    break;

                default:
                    console.println("Unknown command '" + subCommand + "'");
                    printUsage(console);
                    break;
            }
        }
    }

    @Override
    public List<String> getUsages() {
        return Arrays.asList(
                new String[] { buildCommandUsage(SUBCMD_CLEAR_PAIRINGS, "removes all pairings with Homekit clients"),
                        buildCommandUsage(SUBCMD_ALLOW_UNAUTHENTICATED + " <boolean>",
                                "enables or disables unauthenticated access to facilitate debugging") });
    }

    public void setStorageService(StorageService storageService) {
        this.storageService = storageService;
    }

    public void setHomekit(Homekit homekit) {
        this.homekit = homekit;
    }

    private void clearHomekitPairings(Console console) {
        try {
            new HomekitAuthInfoImpl(storageService, null).clear();
            homekit.refreshAuthInfo();
            console.println("Cleared homekit pairings");
        } catch (Exception e) {
            logger.error("Could not clear homekit pairings", e);
        }
    }

    private void allowUnauthenticatedHomekitRequests(boolean allow, Console console) {
        homekit.allowUnauthenticatedRequests(allow);
        console.println((allow ? "Enabled " : "Disabled ") + "unauthenticated homekit access");
    }

}
