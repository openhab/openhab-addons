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
package org.openhab.binding.amazonechocontrol.internal;

import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.BINDING_ID;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.amazonechocontrol.internal.handler.AccountHandler;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link ConsoleCommandExtension} class
 *
 * @author Jan N. Klug - Initial contribution
 */
@Component(service = org.openhab.core.io.console.extensions.ConsoleCommandExtension.class)
@NonNullByDefault
public class ConsoleCommandExtension extends AbstractConsoleCommandExtension {
    private static final String LIST_ACCOUNTS = "listAccounts";
    private static final String RESET_ACCOUNT = "resetAccount";

    private final AmazonEchoControlHandlerFactory handlerFactory;

    @Activate
    public ConsoleCommandExtension(@Reference AmazonEchoControlHandlerFactory handlerFactory) {
        super(BINDING_ID, "Manage the AmazonEchoControl account");
        this.handlerFactory = handlerFactory;
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length > 0) {
            String command = args[0];
            switch (command) {
                case LIST_ACCOUNTS:
                    listAccounts(console);
                    break;
                case RESET_ACCOUNT:
                    if (args.length == 2) {
                        resetAccount(console, args[1]);
                    } else {
                        console.println("Invalid use of command '" + command + "'");
                        printUsage(console);
                    }
                    break;
                default:
                    console.println("Unknown command '" + command + "'");
                    printUsage(console);
                    break;
            }
        } else {
            printUsage(console);
        }
    }

    private void listAccounts(Console console) {
        Set<AccountHandler> accountHandlers = handlerFactory.getAccountHandlers();

        accountHandlers.forEach(handler -> console.println(
                "Thing-Id: " + handler.getThing().getUID().getId() + " ('" + handler.getThing().getLabel() + "')"));
    }

    private void resetAccount(Console console, String accountId) {
        Optional<AccountHandler> accountHandler = handlerFactory.getAccountHandlers().stream()
                .filter(handler -> handler.getThing().getUID().getId().equals(accountId)).findAny();
        if (accountHandler.isPresent()) {
            console.println("Resetting account '" + accountId + "'");
            accountHandler.get().resetConnection(true);
        } else {
            console.println("Account '" + accountId + "' not found.");
        }
    }

    @Override
    public List<String> getUsages() {
        return Arrays.asList(buildCommandUsage(LIST_ACCOUNTS, "list all AmazonEchoControl accounts"), buildCommandUsage(
                RESET_ACCOUNT + " <account_id>",
                "resets the account connection (clears all authentication data) for the thing with the given id"));
    }
}
