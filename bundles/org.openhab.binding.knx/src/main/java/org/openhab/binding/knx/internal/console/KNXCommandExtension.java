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
package org.openhab.binding.knx.internal.console;

import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.KNXBindingConstants;
import org.openhab.binding.knx.internal.factory.KNXHandlerFactory;
import org.openhab.binding.knx.internal.handler.KNXBridgeBaseThingHandler;
import org.openhab.binding.knx.internal.tpm.TpmInterface;
import org.openhab.core.auth.SecurityException;
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
    private static final String CMD_TPM_INFO = "tpm-info";
    private static final String CMD_TPM_ENCRYPT = "tpm-encrypt";
    private static final StringsCompleter CMD_COMPLETER = new StringsCompleter(
            List.of(CMD_LIST_UNKNOWN_GA, CMD_TPM_INFO, CMD_TPM_ENCRYPT), false);

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
        } else if (args.length == 1 && CMD_TPM_INFO.equalsIgnoreCase(args[0])) {
            try {
                console.println("trying to access TPM module");
                TpmInterface tpm = new TpmInterface();
                console.println("TPM version:   " + tpm.getTpmVersion());
                console.println("TPM model:     " + tpm.getTpmManufacturerShort() + " " + tpm.getTpmModel());
                console.println("TPM firmware:  " + tpm.getTpmFirmwareVersion());
                console.println("TPM TCG Spec.: rev. " + tpm.getTpmTcgRevision() + " level " + tpm.getTpmTcgLevel());
            } catch (SecurityException e) {
                console.print("error: " + e.getMessage());
            }
            return;
        } else if (args.length == 2 && CMD_TPM_ENCRYPT.equalsIgnoreCase(args[0])) {
            try {
                console.println("trying to access TPM module");
                TpmInterface tpm = new TpmInterface();
                console.println("generating keys, this might take some time");
                String p = tpm.encryptAndSerializeSecret(args[1]);
                console.println("encrypted representation of password");
                console.println(KNXBindingConstants.ENCYRPTED_PASSWORD_SERIALIZATION_PREFIX + p);

                // check if TPM can decrypt
                String decrypted = tpm.deserializeAndDectryptSecret(p);
                if (args[1].equals(decrypted)) {
                    console.println("Password successfully recovered from encrypted representation");
                } else {
                    console.println("WARNING: could not decrypt");
                }

            } catch (SecurityException e) {
                console.print("error: " + e.getMessage());
            }
            return;
        }
        printUsage(console);
    }

    @Override
    public List<String> getUsages() {
        return List.of(
                buildCommandUsage(CMD_LIST_UNKNOWN_GA, "list group addresses which are not configured in openHAB"),
                buildCommandUsage(CMD_TPM_ENCRYPT + " <password>", "Encrypt a password"),
                buildCommandUsage(CMD_TPM_INFO, "Get information about available TPM"));
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
