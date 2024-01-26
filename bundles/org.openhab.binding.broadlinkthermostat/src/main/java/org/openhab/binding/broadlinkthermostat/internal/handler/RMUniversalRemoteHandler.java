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
package org.openhab.binding.broadlinkthermostat.internal.handler;

import static org.openhab.core.library.types.OnOffType.OFF;
import static org.openhab.core.library.types.OnOffType.ON;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.broadlinkthermostat.internal.BroadlinkBindingConstants;
import org.openhab.binding.broadlinkthermostat.internal.BroadlinkHandlerFactory;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mob41.blapi.RM2Device;
import com.github.mob41.blapi.mac.Mac;
import com.github.mob41.blapi.pkt.cmd.rm2.SendDataCmdPayload;

/**
 * The {@link RMUniversalRemoteHandler} is responsible for handling RM Mini Universal Remotes.
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
public class RMUniversalRemoteHandler extends BroadlinkBaseHandler {
    private final Logger logger = LoggerFactory.getLogger(RMUniversalRemoteHandler.class);
    private @Nullable RM2Device rm2Device;

    /**
     * Creates a new instance of this class for the {@link RMUniversalRemoteHandler}.
     *
     * @param thing the thing that should be handled, not null
     */
    public RMUniversalRemoteHandler(Thing thing) {
        super(thing);
    }

    /**
     * Initializes a new instance of a {@link RMUniversalRemoteHandler}.
     */
    @Override
    public void initialize() {
        super.initialize();
        if (!host.isBlank() && !macAddress.isBlank()) {
            try {
                blDevice = new RM2Device(host, new Mac(macAddress));
                this.rm2Device = (RM2Device) blDevice;
                updateStatus(ThingStatus.ONLINE);
            } catch (IOException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Could not find broadlink universal remote device at host " + host + " with MAC " + macAddress
                                + ": " + e.getMessage());
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Missing device configuration");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Command: {}", command.toFullString());

        if (command == RefreshType.REFRESH) {
            logger.debug("Nothing to refresh on thing {}", thing.getUID());
            return;
        }
        authenticate(false);
        String channelId = channelUID.getIdWithoutGroup();
        try {
            switch (channelId) {
                case BroadlinkBindingConstants.LEARNING_MODE:
                    if (OnOffType.from(command.toFullString()).equals(ON)) {
                        handleLearningCommand();
                    }
                    break;
                case BroadlinkBindingConstants.SAVE_LEARNED:
                    handleSaveLearned(command);
                    break;
                case BroadlinkBindingConstants.SEND_LEARNED:
                    handleSendLearned(command);
                    break;
                default:
                    logger.debug("Command {} not supported by channel {}", command.toFullString(), channelId);
            }
        } catch (Exception e) {
            logger.warn("Exception while running channel {}", channelUID);
        }
    }

    private void handleLearningCommand() throws IOException {
        RM2Device rm2Device = this.rm2Device;
        if (rm2Device != null) {
            try {
                rm2Device.enterLearning();
                updateState(BroadlinkBindingConstants.LEARNING_MODE, ON);
                logger.debug("Thing {} entered learning mode", thing.getUID());
            } catch (IOException e) {
                updateState(BroadlinkBindingConstants.LEARNING_MODE, OFF);
                throw e;
            }
        } else {
            logger.warn("Device not initialized");
        }
    }

    private void handleSaveLearned(Command command) throws Exception {
        RM2Device rm2Device = this.rm2Device;
        if (rm2Device != null) {
            byte @Nullable [] learned = rm2Device.checkData();
            updateState(BroadlinkBindingConstants.LEARNING_MODE, OFF);
            if (learned == null) {
                logger.warn("Thing {}: nothing learned", thing.getUID());
                return;
            }
            var packetName = command.toFullString();
            File destinationFile = new File(BroadlinkHandlerFactory.INFRARED_FOLDER, packetName);
            if (destinationFile.exists()) {
                logger.info("Key '{}' is already learned, overwriting...", packetName);
            }
            Files.write(destinationFile.toPath(), learned, new StandardOpenOption[] { StandardOpenOption.WRITE,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING });
        }
    }

    private void handleSendLearned(Command command) throws IOException {
        RM2Device rm2Device = this.rm2Device;
        if (rm2Device != null) {
            File packetFile = new File(BroadlinkHandlerFactory.INFRARED_FOLDER, command.toFullString());
            if (!packetFile.exists()) {
                logger.warn("{}: Nothing learned as '{}'", thing.getUID(), command.toFullString());
                return;
            }
            var packet = Files.readAllBytes(packetFile.toPath());
            rm2Device.sendCmdPkt(new SendDataCmdPayload(packet));
        }
    }
}
