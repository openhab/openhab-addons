/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tesla.internal.command;

import static org.openhab.binding.tesla.TeslaBindingConstants.*;

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

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.storage.Storage;
import org.eclipse.smarthome.core.storage.StorageService;
import org.eclipse.smarthome.io.console.Console;
import org.eclipse.smarthome.io.console.extensions.AbstractConsoleCommandExtension;
import org.eclipse.smarthome.io.console.extensions.ConsoleCommandExtension;
import org.openhab.binding.tesla.TeslaBindingConstants;
import org.openhab.binding.tesla.internal.protocol.TokenRequest;
import org.openhab.binding.tesla.internal.protocol.TokenRequestPassword;
import org.openhab.binding.tesla.internal.protocol.TokenResponse;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Console commands for interacting with the Tesla integration
 *
 * @author Nicolai Grødum
 */
@Component(service = ConsoleCommandExtension.class, immediate = true)
public class TeslaCommandExtension extends AbstractConsoleCommandExtension {

    private static final String CMD_LOGON = "logon";

    private final Logger logger = LoggerFactory.getLogger(TeslaCommandExtension.class);

    private StorageService storageService;
    private final Client teslaClient = ClientBuilder.newClient();
    private final WebTarget teslaTarget = teslaClient.target(TESLA_OWNERS_URI);
    private final WebTarget tokenTarget = teslaTarget.path(TESLA_ACCESS_TOKEN_URI);

    public TeslaCommandExtension() {
        super("tesla", "Interact with the Tesla integration.");
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length > 0) {
            String subCommand = args[0];
            switch (subCommand) {
                case CMD_LOGON:
                    if (args.length == 2) {
                        try {
                            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                            console.print("Username (email): ");
                            String username = br.readLine();
                            console.println(username);

                            console.print("Password: ");

                            String pwd = br.readLine();
                            console.println("");
                            console.println("Attempting logon...");
                            logon(console, args[1], username, pwd);
                        } catch (Exception e) {
                            console.println(e.toString());
                        }
                    } else if (args.length == 4) {
                        logon(console, args[1], args[2], args[3]);
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
        return Arrays.asList(new String[] { buildCommandUsage(CMD_LOGON + " <thingid> [<user email>] [<password>]",
                "Authenticates and stores the access and refresh token. Does not store the username/password."), });
    }

    @Reference
    public void setStorageService(StorageService storageService) {
        this.storageService = storageService;
    }

    public void unsetStorageService(StorageService storageService) {
        this.storageService = null;
    }

    private void logon(Console console, String thingId, String username, String password) {
        try {
            Gson gson = new Gson();

            TokenRequest token = new TokenRequestPassword(username, password);
            String payLoad = gson.toJson(token);

            Response response = tokenTarget.request().post(Entity.entity(payLoad, MediaType.APPLICATION_JSON_TYPE));

            if (response != null) {
                if (response.getStatus() == 200 && response.hasEntity()) {
                    String responsePayLoad = response.readEntity(String.class);
                    TokenResponse tokenResponse = gson.fromJson(responsePayLoad.trim(), TokenResponse.class);

                    if (tokenResponse != null && !StringUtils.isEmpty(tokenResponse.access_token)) {
                        Storage storage = storageService.getStorage(TeslaBindingConstants.BINDING_ID);
                        storage.put(thingId, gson.toJson(tokenResponse));
                    }
                    console.println("Successfully logged on and stored token.");

                } else {
                    console.println(
                            "Failure:" + response.getStatus() + " " + response.getStatusInfo().getReasonPhrase());
                }
            }
        } catch (Exception e) {
            console.println("Failed to store token:" + e.getMessage());
            logger.error("Could not get Tesla token", e);
        }
    }
}
