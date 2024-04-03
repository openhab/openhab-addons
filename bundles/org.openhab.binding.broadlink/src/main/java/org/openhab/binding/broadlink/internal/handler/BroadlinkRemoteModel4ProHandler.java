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
package org.openhab.binding.broadlink.internal.handler;

import java.io.IOException;
import java.net.ProtocolException;
import java.util.HexFormat;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.broadlink.internal.BroadlinkBindingConstants;
import org.openhab.binding.broadlink.internal.BroadlinkRemoteDynamicCommandDescriptionProvider;
import org.openhab.binding.broadlink.internal.Utils;
import org.openhab.core.library.types.StringType;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.util.HexUtils;

/**
 * Extension for the RF part of an RM 4 Pro
 *
 * @author Anton Jansen - Initial contribution
 */

@NonNullByDefault
public class BroadlinkRemoteModel4ProHandler extends BroadlinkRemoteModel4MiniHandler {

    private static final byte COMMAND_BYTE_ENTER_RF_FREQ_LEARNING = 0x19; // Sweep frequency
    private static final byte COMMAND_BYTE_CHECK_RF_FREQ_LEARNING = 0x1A; // Check frequency
    private static final byte COMMAND_BYTE_EXIT_RF_FREQ_LEARNING = 0x1E; // Cancel sweep frequency
    private static final byte COMMAND_BYTE_FIND_RF_PACKET = 0x1B; // Find RF packet
    private static final byte COMMAND_BYTE_CHECK_RF_DATA = 0x4; // Check data

    public BroadlinkRemoteModel4ProHandler(Thing thing,
            BroadlinkRemoteDynamicCommandDescriptionProvider commandDescriptionProvider,
            StorageService storageService) {
        super(thing, commandDescriptionProvider, storageService);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        ChannelTypeUID channelTypeUID = extractChannelType(channelUID, command);
        if (!Utils.isOnline(getThing())) {
            logger.debug("Can't handle command {} because handler for thing {} is not ONLINE", command,
                    getThing().getLabel());
            return;
        }
        if (command instanceof RefreshType) {
            updateItemStatus();
            return;
        }

        if (channelTypeUID == null) {
            return;
        }

        switch (channelTypeUID.getId()) {
            case BroadlinkBindingConstants.RF_COMMAND_CHANNEL: {
                logger.debug("Handling rf command '{}' on channel {} of thing {}", command, channelUID.getId(),
                        getThing().getLabel());
                byte code[] = lookupRFCode(command, channelUID);
                if (code != null) {
                    sendCode(code);
                }
                break;
            }
            case BroadlinkBindingConstants.RF_LEARNING_CONTROL_CHANNEL: {
                switch (command.toString()) {
                    case BroadlinkBindingConstants.LEARNING_CONTROL_COMMAND_LEARN: {
                        logger.debug("RF learn command received");
                        handleFindRFFrequencies();
                        break;
                    }
                    case BroadlinkBindingConstants.LEARNING_CONTROL_COMMAND_CHECK: {
                        logger.debug("RF check command received");
                        checkAndSaveRFCommand();
                        break;
                    }
                    case BroadlinkBindingConstants.LEARNING_CONTROL_COMMAND_MODIFY: {
                        logger.debug("RF check command received");
                        modifyRFCommand();
                        break;
                    }
                    case BroadlinkBindingConstants.LEARNING_CONTROL_COMMAND_DELETE: {
                        logger.debug("RF check command received");
                        deleteRFCommand();
                        break;
                    }
                    default: {
                        logger.debug("Thing {} has unknown channel type '{}'", getThing().getLabel(),
                                channelTypeUID.getId());
                    }
                }
            }
                break;
            default:
        }
        super.handleCommand(channelUID, command);
    }

    private byte @Nullable [] lookupRFCode(Command command, ChannelUID channelUID) {
        byte code[] = null;
        String value = this.mappingService.lookupRF(command.toString());

        if (value == null || value.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "No entries found for command " + command + " in RF map file, or the file is missing.");
            return null;
        }

        code = HexUtils.hexToBytes(value);

        logger.debug("Transformed command '{}' for thing {}", command, getThing().getLabel());
        return code;
    }

    private void handleFindRFFrequencies() {
        logger.trace("Sweeping spectrum for frequencies");
        // Let the user know we are processing his / her command
        updateState(BroadlinkBindingConstants.RF_LEARNING_CONTROL_CHANNEL,
                new StringType(BroadlinkBindingConstants.LEARNING_CONTROL_COMMAND_LEARN));
        sendCommand(COMMAND_BYTE_ENTER_RF_FREQ_LEARNING, "enter remote rf frequency learning mode");
        boolean freqFound = false;

        long start = System.currentTimeMillis();
        long timeout = start + 30 * 1000;
        HexFormat hex = HexFormat.of();

        try {
            while ((System.currentTimeMillis() < timeout) && (freqFound == false)) {
                TimeUnit.MILLISECONDS.sleep(500);
                logger.trace("Check");
                byte[] resp = (sendCommand(COMMAND_BYTE_CHECK_RF_FREQ_LEARNING, "check rf frequency"));
                if (resp != null) {
                    resp = extractResponsePayload(resp);
                    logger.trace("Response: {}", hex.formatHex(resp));
                    if (resp[0] == 1) {
                        freqFound = true;
                        logger.trace("Freq found!");
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            logger.warn("RF learning unexpected interrupted:{}", e.getMessage());
            freqFound = false;
        }

        if (freqFound == false) {
            sendCommand(COMMAND_BYTE_EXIT_RF_FREQ_LEARNING, "exit remote rf frequency learning mode");
            logger.info("No RF frequency found.");
            updateState(BroadlinkBindingConstants.RF_LEARNING_CONTROL_CHANNEL, new StringType("NULL"));
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Cannot locate the appropriate RF frequency.");
            return;
        }

        updateState(BroadlinkBindingConstants.RF_LEARNING_CONTROL_CHANNEL, new StringType("RF command learnt"));
    }

    private void checkAndSaveRFCommand() {
        logger.trace("find RF packet data ...");
        updateState(BroadlinkBindingConstants.RF_LEARNING_CONTROL_CHANNEL,
                new StringType(BroadlinkBindingConstants.LEARNING_CONTROL_COMMAND_CHECK));
        sendCommand(COMMAND_BYTE_FIND_RF_PACKET, "find the rf packet data");

        long start = System.currentTimeMillis();
        long timeout = start + 30 * 1000;

        boolean dataFound = false;

        try {
            byte[] response = new byte[0];
            while ((System.currentTimeMillis() < timeout) && (dataFound == false)) {
                TimeUnit.MILLISECONDS.sleep(500);
                byte[] data = sendCommand(COMMAND_BYTE_CHECK_RF_DATA, "check the rf packet data");
                if (data != null) {
                    logger.trace("Got data response: {}", data);
                    try {
                        response = extractResponsePayload(data);
                        String hexString = Utils.toHexString(response);
                        mappingService.storeRF(thingConfig.getNameOfCommandToLearn(), hexString);
                        logger.info("BEGIN LAST LEARNT CODE");
                        logger.info("{}", hexString);
                        logger.info("END LAST LEARNT CODE ({} characters)", hexString.length());

                        dataFound = true;
                    } catch (ProtocolException ex) {
                        logger.trace(".");
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            logger.warn("Unexpected exception while checking RF packet data: {}", e.getMessage());
            updateState(BroadlinkBindingConstants.RF_LEARNING_CONTROL_CHANNEL, new StringType("NULL"));
        }

        if (dataFound) {
            updateState(BroadlinkBindingConstants.RF_LEARNING_CONTROL_CHANNEL,
                    new StringType("RF command " + thingConfig.getNameOfCommandToLearn() + " saved"));
        } else {
            updateState(BroadlinkBindingConstants.RF_LEARNING_CONTROL_CHANNEL, new StringType("NULL"));
        }
    }

    private void modifyRFCommand() {
        logger.trace("find RF packet data ...");
        updateState(BroadlinkBindingConstants.RF_LEARNING_CONTROL_CHANNEL,
                new StringType(BroadlinkBindingConstants.LEARNING_CONTROL_COMMAND_CHECK));
        sendCommand(COMMAND_BYTE_FIND_RF_PACKET, "find the rf packet data");

        long start = System.currentTimeMillis();
        long timeout = start + 30 * 1000;

        boolean dataFound = false;

        try {
            byte[] response = new byte[0];
            while ((System.currentTimeMillis() < timeout) && (dataFound == false)) {
                TimeUnit.MILLISECONDS.sleep(500);
                byte[] data = sendCommand(COMMAND_BYTE_CHECK_RF_DATA, "check the rf packet data");
                if (data != null) {
                    logger.trace("Got data response: {}", data);
                    try {
                        response = extractResponsePayload(data);
                        String hexString = Utils.toHexString(response);
                        mappingService.replaceRF(thingConfig.getNameOfCommandToLearn(), hexString);
                        logger.info("BEGIN LAST LEARNT CODE");
                        logger.info("{}", hexString);
                        logger.info("END LAST LEARNT CODE ({} characters)", hexString.length());

                        dataFound = true;
                    } catch (ProtocolException ex) {
                        logger.trace(".");
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            logger.warn("Unexpected exception while checking RF packet data: {}", e.getMessage());
            updateState(BroadlinkBindingConstants.RF_LEARNING_CONTROL_CHANNEL, new StringType("NULL"));
        }

        if (dataFound) {
            updateState(BroadlinkBindingConstants.RF_LEARNING_CONTROL_CHANNEL,
                    new StringType("RF command " + thingConfig.getNameOfCommandToLearn() + " modified"));
        } else {
            updateState(BroadlinkBindingConstants.RF_LEARNING_CONTROL_CHANNEL, new StringType("NULL"));
        }
    }

    private void deleteRFCommand() {
        updateState(BroadlinkBindingConstants.LEARNING_CONTROL_CHANNEL,
                new StringType(BroadlinkBindingConstants.LEARNING_CONTROL_COMMAND_DELETE));
        mappingService.deleteRF(thingConfig.getNameOfCommandToLearn());
        updateState(BroadlinkBindingConstants.LEARNING_CONTROL_CHANNEL,
                new StringType("RF command " + thingConfig.getNameOfCommandToLearn() + " deleted"));
    }
}
