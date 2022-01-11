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
package org.openhab.binding.tesla.internal.command;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tesla.internal.TeslaBindingConstants;
import org.openhab.binding.tesla.internal.discovery.TeslaAccountDiscoveryService;
import org.openhab.binding.tesla.internal.handler.TeslaSSOHandler;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.util.UIDUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

/**
 * Console commands for interacting with the Tesla integration
 *
 * @author Nicolai Grødum - Initial contribution
 * @author Kai Kreuzer - refactored to use Tesla account thing
 */
@NonNullByDefault
@Component(service = ConsoleCommandExtension.class)
public class TeslaCommandExtension extends AbstractConsoleCommandExtension {

    private static final String CMD_LOGIN = "login";

    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    private @Nullable ClientBuilder injectedClientBuilder;

    private final TeslaAccountDiscoveryService teslaAccountDiscoveryService;
    private final HttpClientFactory httpClientFactory;

    @Activate
    public TeslaCommandExtension(@Reference TeslaAccountDiscoveryService teslaAccountDiscoveryService,
            @Reference HttpClientFactory httpClientFactory) {
        super("tesla", "Interact with the Tesla integration.");
        this.teslaAccountDiscoveryService = teslaAccountDiscoveryService;
        this.httpClientFactory = httpClientFactory;
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length > 0) {
            String subCommand = args[0];
            switch (subCommand) {
                case CMD_LOGIN:
                    if (args.length == 1) {
                        try {
                            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                            console.print("Username (email): ");
                            String username = br.readLine();
                            console.println(username);

                            console.print("Password: ");

                            String pwd = br.readLine();
                            console.println("");
                            console.println("Attempting login...");
                            login(console, username, pwd);
                        } catch (Exception e) {
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
        } else {
            printUsage(console);
        }
    }

    @Override
    public List<String> getUsages() {
        return Arrays.asList(buildCommandUsage(CMD_LOGIN + " [<user email>] [<password>]",
                "Authenticates the user and provides a refresh token."));
    }

    private void login(Console console, String username, String password) {
        TeslaSSOHandler ssoHandler = new TeslaSSOHandler(httpClientFactory.getCommonHttpClient());

        String refreshToken = ssoHandler.authenticate(username, password);
        if (refreshToken != null) {
            console.println("Refresh token: " + refreshToken);

            ThingUID thingUID = new ThingUID(TeslaBindingConstants.THING_TYPE_ACCOUNT, UIDUtils.encode(username));
            DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withLabel("Tesla Account")
                    .withProperty(TeslaBindingConstants.CONFIG_REFRESHTOKEN, refreshToken)
                    .withProperty(TeslaBindingConstants.CONFIG_USERNAME, username)
                    .withRepresentationProperty(TeslaBindingConstants.CONFIG_USERNAME).build();
            teslaAccountDiscoveryService.thingDiscovered(result);
        } else {
            console.println("Failed to retrieve refresh token");
        }
    }
}
