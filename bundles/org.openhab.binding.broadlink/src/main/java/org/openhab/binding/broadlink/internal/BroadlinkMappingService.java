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
import org.openhab.binding.broadlink.internal.BroadlinkBindingConstants.CodeType;
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
    private static ArrayList<BroadlinkMappingService> mappingInstances = new ArrayList<BroadlinkMappingService>();

    public BroadlinkMappingService(BroadlinkRemoteDynamicCommandDescriptionProvider commandDescriptionProvider,
            ChannelUID irTargetChannelUID, ChannelUID rfTargetChannelUID, StorageService storageService) {
        this.commandDescriptionProvider = commandDescriptionProvider;
        this.irTargetChannelUID = irTargetChannelUID;
        this.rfTargetChannelUID = rfTargetChannelUID;
        this.storageService = storageService;
        irStorage = this.storageService.getStorage(BroadlinkBindingConstants.IR_MAP_NAME,
                String.class.getClassLoader());
        rfStorage = this.storageService.getStorage(BroadlinkBindingConstants.RF_MAP_NAME,
                String.class.getClassLoader());
        mappingInstances.add(this);
        notifyAvailableCommands(irStorage.getKeys(), CodeType.IR, false);
        notifyAvailableCommands(rfStorage.getKeys(), CodeType.RF, false);
        logger.debug("BroadlinkMappingService constructed on behalf of {} and {}", this.irTargetChannelUID,
                this.rfTargetChannelUID);
    }

    public void dispose() {
        mappingInstances.remove(this);
    }

    public @Nullable String lookupCode(String command, CodeType codeType) {
        String response;
        switch (codeType) {
            case IR:
                response = lookupKey(command, irStorage, codeType);
                break;
            case RF:
                response = lookupKey(command, rfStorage, codeType);
                break;
            default:
                response = null;
        }
        return response;
    }

    public @Nullable String storeCode(String command, String code, CodeType codeType) {
        String response;
        switch (codeType) {
            case IR:
                response = storeKey(command, code, irStorage, codeType, irTargetChannelUID);
                break;
            case RF:
                response = storeKey(command, code, rfStorage, codeType, rfTargetChannelUID);
                break;
            default:
                response = null;
        }
        return response;
    }

    public @Nullable String replaceCode(String command, String code, CodeType codeType) {
        String response;
        switch (codeType) {
            case IR:
                response = replaceKey(command, code, irStorage, codeType, irTargetChannelUID);
                break;
            case RF:
                response = replaceKey(command, code, rfStorage, codeType, rfTargetChannelUID);
                break;
            default:
                response = null;
        }
        return response;
    }

    public @Nullable String deleteCode(String command, CodeType codeType) {
        String response;
        switch (codeType) {
            case IR:
                response = deleteKey(command, irStorage, codeType, irTargetChannelUID);
                break;
            case RF:
                response = deleteKey(command, rfStorage, codeType, rfTargetChannelUID);
                break;
            default:
                return null;
        }
        return response;
    }

    public @Nullable String lookupKey(String command, Storage<String> storage, CodeType codeType) {
        String value = storage.get(command);
        if (value != null) {
            logger.debug("{} Command label found. Key value pair is {},{}", codeType, command, value);
        } else {
            logger.debug("{} Command label not found.", codeType);
        }
        return value;
    }

    public @Nullable String storeKey(String command, String code, Storage<String> storage, CodeType codeType,
            ChannelUID targetChannelUID) {
        if (storage.get(command) == null) {
            logger.debug("{} Command label not found. Proceeding to store key value pair {},{} and reload Command list",
                    codeType, command, code);
            storage.put(command, code);
            notifyAvailableCommands(storage.getKeys(), codeType, true);
            return command;
        } else {
            logger.debug("{} Command label {} found. This is not a replace operation. Skipping", codeType, command);
            return null;
        }
    }

    public @Nullable String replaceKey(String command, String code, Storage<String> storage, CodeType codeType,
            ChannelUID targetChannelUID) {
        if (storage.get(command) != null) {
            logger.debug("{} Command label found. Proceeding to store key value pair {},{} and reload Command list",
                    codeType, command, code);
            storage.put(command, code);
            notifyAvailableCommands(storage.getKeys(), codeType, true);
            return command;
        } else {
            logger.debug("{} Command label {} not found. This is not an add method. Skipping", codeType, command);
            return null;
        }
    }

    public @Nullable String deleteKey(String command, Storage<String> storage, CodeType codeType,
            ChannelUID targetChannelUID) {
        String value = storage.get(command);
        if (value != null) {
            logger.debug("{} Command label found. Proceeding to remove key pair {},{} and reload command list",
                    codeType, command, value);
            storage.remove(command);
            notifyAvailableCommands(storage.getKeys(), codeType, true);
            return command;
        } else {
            logger.debug("{} Command label {} not found. Can't delete a command that does not exist", codeType,
                    command);
            return null;
        }
    }

    void notifyAvailableCommands(Collection<String> commandNames, CodeType codeType, boolean refreshAllInstances) {
        List<CommandOption> commandOptions = new ArrayList<>();
        commandNames.forEach((c) -> commandOptions.add(new CommandOption(c, null)));
        if (refreshAllInstances) {
            logger.debug("notifying framework about {} commands: {} - All instances", commandOptions.size(),
                    commandNames.toString());
            for (BroadlinkMappingService w : mappingInstances) {
                switch (codeType) {
                    case IR:
                        w.commandDescriptionProvider.setCommandOptions(w.irTargetChannelUID, commandOptions);
                    case RF:
                        w.commandDescriptionProvider.setCommandOptions(w.rfTargetChannelUID, commandOptions);
                }
            }
        } else {
            logger.debug("notifying framework about {} commands: {} for single {} device", commandOptions.size(),
                    commandNames.toString(), codeType);
            switch (codeType) {
                case IR:
                    this.commandDescriptionProvider.setCommandOptions(irTargetChannelUID, commandOptions);
                case RF:
                    this.commandDescriptionProvider.setCommandOptions(rfTargetChannelUID, commandOptions);
            }
        }
    }
}
