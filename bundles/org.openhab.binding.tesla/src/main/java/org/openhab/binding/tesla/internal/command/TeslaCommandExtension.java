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
package org.openhab.binding.tesla.internal.command;

import static org.openhab.binding.tesla.internal.TeslaBindingConstants.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.util.UIDUtils;
import org.eclipse.smarthome.io.console.Console;
import org.eclipse.smarthome.io.console.extensions.AbstractConsoleCommandExtension;
import org.eclipse.smarthome.io.console.extensions.ConsoleCommandExtension;
import org.openhab.binding.tesla.internal.TeslaBindingConstants;
import org.openhab.binding.tesla.internal.discovery.TeslaAccountDiscoveryService;
import org.openhab.binding.tesla.internal.protocol.TokenRequest;
import org.openhab.binding.tesla.internal.protocol.TokenRequestPassword;
import org.openhab.binding.tesla.internal.protocol.TokenResponse;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Console commands for interacting with the Tesla integration
 *
 * @author Nicolai Grødum - Initial contribution
 * @author Kai Kreuzer - refactored to use Tesla account thing
 */
@NonNullByDefault
@Component(service = ConsoleCommandExtension.class, immediate = true)
public class TeslaCommandExtension extends AbstractConsoleCommandExtension {

    private static final String CMD_LOGIN = "login";

    private final Logger logger = LoggerFactory.getLogger(TeslaCommandExtension.class);

    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    private @Nullable ClientBuilder injectedClientBuilder;

    private @Nullable WebTarget tokenTarget;

    private final TeslaAccountDiscoveryService teslaAccountDiscoveryService;

    @Activate
    public TeslaCommandExtension(@Reference TeslaAccountDiscoveryService teslaAccountDiscoveryService) {
        super("tesla", "Interact with the Tesla integration.");
        this.teslaAccountDiscoveryService = teslaAccountDiscoveryService;
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
        }
    }

    @Override
    public List<String> getUsages() {
        return Arrays.asList(buildCommandUsage(CMD_LOGIN + " [<user email>] [<password>]",
                "Authenticates the user and provides a refresh token."));
    }

    private void login(Console console, String username, String password) {
        try {
            Gson gson = new Gson();

            TokenRequest token = new TokenRequestPassword(username, password);
            String payLoad = gson.toJson(token);

            Response response = getTokenTarget().request()
                    .post(Entity.entity(payLoad, MediaType.APPLICATION_JSON_TYPE));

            if (response != null) {
                if (response.getStatus() == 200 && response.hasEntity()) {
                    String responsePayLoad = response.readEntity(String.class);
                    TokenResponse tokenResponse = gson.fromJson(responsePayLoad.trim(), TokenResponse.class);
                    console.println("Refresh token: " + tokenResponse.refresh_token);

                    ThingUID thingUID = new ThingUID(TeslaBindingConstants.THING_TYPE_ACCOUNT,
                            UIDUtils.encode(username));
                    DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withLabel("Tesla Account")
                            .withProperty(TeslaBindingConstants.CONFIG_REFRESHTOKEN, tokenResponse.refresh_token)
                            .withProperty(TeslaBindingConstants.CONFIG_USERNAME, username)
                            .withRepresentationProperty(TeslaBindingConstants.CONFIG_USERNAME).build();
                    teslaAccountDiscoveryService.thingDiscovered(result);
                } else {
                    console.println(
                            "Failure: " + response.getStatus() + " " + response.getStatusInfo().getReasonPhrase());
                }
            }
        } catch (Exception e) {
            console.println("Failed to retrieve token: " + e.getMessage());
            logger.error("Could not get refresh token.", e);
        }
    }

    private synchronized WebTarget getTokenTarget() {
        WebTarget target = this.tokenTarget;
        if (target != null) {
            return target;
        } else {
            Client client;
            try {
                client = ClientBuilder.newBuilder().build();
            } catch (Exception e) {
                // we seem to have no Jersey, so let's hope for an injected builder by CXF
                if (this.injectedClientBuilder != null) {
                    client = injectedClientBuilder.build();
                } else {
                    throw new IllegalStateException("No JAX RS Client Builder available.");
                }
            }
            WebTarget teslaTarget = client.target(URI_OWNERS);
            target = teslaTarget.path(URI_ACCESS_TOKEN);
            this.tokenTarget = target;
            return target;
        }
    }

}
