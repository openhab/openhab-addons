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
package org.openhab.binding.broadlink.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.CommandOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Broadlink-specific mapper watches the given map file and loads the contents
 * into a Map in order to offer keys that it then dynamically supplies to the provided
 * BroadlinkRemoteDynamicCommandDescriptionProvider.
 *
 * @author John Marshall - Initial contribution
 */
@NonNullByDefault
public class BroadlinkMappingService {
    private final Logger logger = LoggerFactory.getLogger(BroadlinkMappingService.class);
    private final BroadlinkRemoteDynamicCommandDescriptionProvider commandDescriptionProvider;
    private final ChannelUID irTargetChannelUID;
    private final ChannelUID rfTargetChannelUID;
    private final StorageService storageService;
    private final Storage<String> irStorage;
    private final Storage<String> rfStorage;

    public BroadlinkMappingService(String irMapFileName, String rfMapFileName,
            BroadlinkRemoteDynamicCommandDescriptionProvider commandDescriptionProvider, ChannelUID irTargetChannelUID,
            ChannelUID rfTargetChannelUID, StorageService storageService) {
        this.commandDescriptionProvider = commandDescriptionProvider;
        this.irTargetChannelUID = irTargetChannelUID;
        this.rfTargetChannelUID = rfTargetChannelUID;
        this.storageService = storageService;
        irStorage = this.storageService.getStorage(irMapFileName, String.class.getClassLoader());
        rfStorage = this.storageService.getStorage(rfMapFileName, String.class.getClassLoader());
        notifyAvailableCommands(irStorage.getKeys(), irTargetChannelUID);
        notifyAvailableCommands(irStorage.getKeys(), rfTargetChannelUID);
        logger.debug("BroadlinkMappingService constructed on behalf of {} and {}", this.irTargetChannelUID,
                this.rfTargetChannelUID);
    }

    public void dispose() {
    }

    public @Nullable String lookupIR(String command) {
        String irValue = irStorage.get(command);
        if (irValue != null) {
            logger.debug("IR Command label found. Key value pair is {},{}", command, irValue);
        } else {
            logger.debug("IR Command not label found.");
        }
        return irValue;
    }

    public @Nullable String storeIR(String command, String irCommand) {
        if (irStorage.get(command) == null) {
            logger.debug("IR Command label not found. Proceeding to store key value pair {},{} and reload Command list",
                    command, irCommand);
            irStorage.put(command, irCommand);
            notifyAvailableCommands(irStorage.getKeys(), irTargetChannelUID);
            return command;
        } else {
            logger.debug("IR Command label {} found. This is not a replace operation. Skipping", command);
            return null;
        }
    }

    public @Nullable String replaceIR(String command, String irCommand) {
        if (irStorage.get(command) != null) {
            logger.debug("IR Command label found. Proceeding to store key value pair {},{} and reload Command list",
                    command, irCommand);
            irStorage.put(command, irCommand);
            notifyAvailableCommands(irStorage.getKeys(), irTargetChannelUID);
            return command;
        } else {
            logger.debug("IR Command label {} not found. This is not an add method. Skipping", command);
            return null;
        }
    }

    public @Nullable String deleteIR(String command) {
        String irValue = irStorage.get(command);
        if (irValue != null) {
            logger.debug("IR Command label found. Proceeding to remove key pair {},{} and reload command list", command,
                    irValue);
            irStorage.remove(command);
            notifyAvailableCommands(irStorage.getKeys(), irTargetChannelUID);
            return command;
        } else {
            logger.debug("IR Command label {} not found. Can't delete a command that does not exist", command);
            return null;
        }
    }

    public @Nullable String lookupRF(String command) {
        String rfValue = rfStorage.get(command);
        if (rfValue != null) {
            logger.debug("RF Command label found. Key value pair is {},{}", command, rfValue);
        } else {
            logger.debug("RF Command not label found.");
        }
        return rfValue;
    }

    public @Nullable String storeRF(String command, String rfCommand) {
        if (rfStorage.get(command) == null) {
            logger.debug("RF Command label not found. Proceeding to store key value pair {},{} and reload Command list",
                    command, rfCommand);
            rfStorage.put(command, rfCommand);
            notifyAvailableCommands(rfStorage.getKeys(), rfTargetChannelUID);
            return command;
        } else {
            logger.debug("RF Command label {} found. This is not a replace operation. Skipping", command);
            return null;
        }
    }

    public @Nullable String replaceRF(String command, String rfCommand) {
        if (rfStorage.get(command) != null) {
            logger.debug("RF Command label found. Proceeding to store key value pair {},{} and reload Command list",
                    command, rfCommand);
            rfStorage.put(command, rfCommand);
            notifyAvailableCommands(rfStorage.getKeys(), rfTargetChannelUID);
            return command;
        } else {
            logger.debug("RF Command label {} not found. This is not an add method. Skipping", command);
            return null;
        }
    }

    @SuppressWarnings("null")
    public @Nullable String deleteRF(String command) {
        String rfValue = rfStorage.get(command);
        if (rfValue != null) {
            logger.debug("RF Command label found. Proceeding to remove key pair {},{} and reload command list", command,
                    rfValue);
            rfStorage.remove(command);
            notifyAvailableCommands(rfStorage.getKeys(), rfTargetChannelUID);
            return command;
        } else {
            logger.debug("RF Command label {} not found. Can't delete a command that does not exist", command);
            return null;
        }
    }

    private void notifyAvailableCommands(Collection<String> commandNames, ChannelUID targetChannelUID) {
        List<CommandOption> commandOptions = new ArrayList<>();
        commandNames.forEach((c) -> commandOptions.add(new CommandOption(c, null)));
        logger.debug("notifying framework about {} commands", commandOptions.size());
        this.commandDescriptionProvider.setCommandOptions(targetChannelUID, commandOptions);
    }
}
