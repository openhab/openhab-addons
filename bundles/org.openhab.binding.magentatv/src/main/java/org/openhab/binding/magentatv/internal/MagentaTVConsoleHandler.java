/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.magentatv.internal;

import static org.openhab.binding.magentatv.internal.MagentaTVBindingConstants.BINDING_ID;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.magentatv.internal.network.MagentaTVOAuth;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Console commands for interacting with the MagentaTV binding
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@Component(service = ConsoleCommandExtension.class)
public class MagentaTVConsoleHandler extends AbstractConsoleCommandExtension {

    private static final String CMD_LOGIN = "login";

    private final Logger logger = LoggerFactory.getLogger(MagentaTVConsoleHandler.class);
    private final MagentaTVOAuth oauth;

    @Activate
    public MagentaTVConsoleHandler(@Reference HttpClientFactory httpClientFactory) {
        super(BINDING_ID, "Interact with the " + BINDING_ID + " integration.");
        oauth = new MagentaTVOAuth(httpClientFactory.getCommonHttpClient());
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length > 0) {
            String subCommand = args[0];
            switch (subCommand) {
                case CMD_LOGIN:
                    if (args.length == 1) {
                        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
                            console.print("Login Name (email): ");
                            String username = br.readLine();
                            console.print("Password: ");
                            String pwd = br.readLine();
                            console.println("Attempting login...");
                            login(console, username, pwd);
                        } catch (IOException e) {
                            console.println(e.toString());
                        }
                    } else if (args.length == 3) {
                        login(console, args[1], args[2]);
                    } else {
                        printUsage(console);
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
        return Arrays.asList(buildCommandUsage(CMD_LOGIN + " [<email>] [<password>]",
                "Logs into the account with the provided credentials and retrieves the User ID."));
    }

    private void login(Console console, String username, String password) {
        try {
            logger.info("Performing OAuth for user {}", username);
            String userId = oauth.getUserId(username, password);
            console.println("Login successful, returned User ID is " + userId);
            console.println(
                    "Edit thing configuration and copy this value to the field User ID or use it as parameter userId for the textual configuration.");
            logger.info("Login with account {} was successful, returned User ID is {}", username, userId);
        } catch (MagentaTVException e) {
            console.println("Login with account " + username + " failed: " + e.getMessage());
            logger.warn("Unable to login with  account {}, check credentials ({})", username, e.getMessage());
        }
    }
}
