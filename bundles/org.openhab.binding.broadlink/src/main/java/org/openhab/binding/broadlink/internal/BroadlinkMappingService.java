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

import java.io.File;
import java.io.IOException;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.OpenHAB;
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
    private static final String TRANSFORM_DIR = OpenHAB.getConfigFolder() + File.separator + "transform"
            + File.separator;
    private final Logger logger = LoggerFactory.getLogger(BroadlinkMappingService.class);
    private final String irMapFileName;
    private final String rfMapFileName;
    private final BroadlinkRemoteDynamicCommandDescriptionProvider commandDescriptionProvider;
    private final ChannelUID irTargetChannelUID;
    private final ChannelUID rfTargetChannelUID;

    private @Nullable WatchService watchService = null;
    private @Nullable WatchKey transformDirWatchKey = null;
    private @Nullable Thread watchThread = null;
    private final String irCmdLabel;
    private final String rfCmdLabel;
    private final StorageService storageService;
    private final Storage<String> irStorage;
    private final Storage<String> rfStorage;

    public BroadlinkMappingService(String irMapFileName, String rfMapFileName,
            BroadlinkRemoteDynamicCommandDescriptionProvider commandDescriptionProvider, ChannelUID irTargetChannelUID,
            ChannelUID rfTargetChannelUID, StorageService storageService) {
        this.irMapFileName = irMapFileName;
        this.rfMapFileName = rfMapFileName;
        this.commandDescriptionProvider = commandDescriptionProvider;
        this.irTargetChannelUID = irTargetChannelUID;
        this.rfTargetChannelUID = rfTargetChannelUID;
        this.storageService = storageService;
        this.irCmdLabel = new String();
        this.rfCmdLabel = new String();
        irStorage = this.storageService.getStorage(irMapFileName, String.class.getClassLoader());
        rfStorage = this.storageService.getStorage(rfMapFileName, String.class.getClassLoader());
        notifyAvailableCommands(irStorage.getKeys(), irTargetChannelUID);
        notifyAvailableCommands(irStorage.getKeys(), rfTargetChannelUID);
        logger.debug("BroadlinkMappingService constructed on behalf of {} and {}", this.irTargetChannelUID,
                this.rfTargetChannelUID);
    }

    @SuppressWarnings("null")
    public void dispose() {
        try {
            if (watchThread != null && !watchThread.isInterrupted()) {
                watchThread.interrupt();
                watchThread = null;
            }
            if (this.transformDirWatchKey != null) {
                this.transformDirWatchKey.cancel();
                this.transformDirWatchKey = null;
            }
            if (this.watchService != null) {
                this.watchService.close();
                this.watchService = null;
            }
        } catch (IOException ioe) {
            logger.warn("Cannot deactivate watcher: {}", ioe.getMessage());
        }
    }

    public @Nullable String lookupIR(String command) {
        return (String) irStorage.get(command);
    }

    public @Nullable String storeIR(String command, String irCommand) {
        if (irStorage.get(command) == null) {
            logger.debug("IR Command not found. Proceeding to store command and reload Command list");
            irStorage.put(command, irCommand);
            notifyAvailableCommands(irStorage.getKeys(), irTargetChannelUID);
            return command;
        } else {
            logger.debug("IR Command found. This is not a replace operation. Skipping");
            return null;
        }
    }

    public @Nullable String replaceIR(String command, String irCommand) {
        if (irStorage.get(command) != null) {
            logger.debug("IR Command found. Proceeding to store command and reload Command list");
            irStorage.put(command, irCommand);
            notifyAvailableCommands(irStorage.getKeys(), irTargetChannelUID);
            return command;
        } else {
            logger.debug("IR Command not found. This is not an add method, so skipping");
            return null;
        }
    }

    public @Nullable String deleteIR(String command) {
        if (irStorage.get(command) != null) {
            logger.debug("IR Command found. Proceeding to remove command and reload command list");
            irStorage.remove(command);
            notifyAvailableCommands(irStorage.getKeys(), irTargetChannelUID);
            return command;
        } else {
            logger.debug("IR Command not found. Can't delete command that does not exist");
            return null;
        }
    }

    public @Nullable String lookupRF(String command) {
        return (String) rfStorage.get(command);
    }

    public @Nullable String storeRF(String command, String rfCommand) {
        if (rfStorage.get(command) == null) {
            logger.debug("RF Command not found. Proceeding to store command and reload Command list");
            rfStorage.put(command, rfCommand);
            notifyAvailableCommands(rfStorage.getKeys(), rfTargetChannelUID);
            return command;
        } else {
            logger.debug("RF Command found. This is not a replace operation. Skipping");
            return null;
        }
    }

    public @Nullable String replaceRF(String command, String rfCommand) {
        if (rfStorage.get(command) != null) {
            logger.debug("RF Command found. Proceeding to store command and reload Command list");
            rfStorage.put(command, rfCommand);
            notifyAvailableCommands(rfStorage.getKeys(), rfTargetChannelUID);
            return command;
        } else {
            logger.debug("RF Command not found. This is not an add method, so skipping");
            return null;
        }
    }

    public @Nullable String deleteRF(String command) {
        if (rfStorage.get(command) != null) {
            logger.debug("RF Command found. Proceeding to remove command and reload command list");
            rfStorage.remove(command);
            notifyAvailableCommands(rfStorage.getKeys(), rfTargetChannelUID);
            return command;
        } else {
            logger.debug("RF Command not found. Can't delete command that does not exist");
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
