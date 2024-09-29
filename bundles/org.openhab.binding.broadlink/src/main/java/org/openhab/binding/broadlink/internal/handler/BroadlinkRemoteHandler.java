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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ProtocolException;
import java.util.HexFormat;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.broadlink.internal.BroadlinkBindingConstants;
import org.openhab.binding.broadlink.internal.BroadlinkBindingConstants.CodeType;
import org.openhab.binding.broadlink.internal.BroadlinkMappingService;
import org.openhab.binding.broadlink.internal.BroadlinkRemoteDynamicCommandDescriptionProvider;
import org.openhab.binding.broadlink.internal.Utils;
import org.openhab.core.library.types.StringType;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.util.HexUtils;

/**
 * Remote blaster handler superclass
 *
 * @author Cato Sognen - Initial contribution
 * @author John Marshall - V3 rewrite with dynamic command description provider
 */
@NonNullByDefault
public abstract class BroadlinkRemoteHandler extends BroadlinkBaseThingHandler {
    public static final byte COMMAND_BYTE_SEND_CODE = 0x02;

    // IR commands
    public static final byte COMMAND_BYTE_ENTER_LEARNING = 0x03;
    public static final byte COMMAND_BYTE_CHECK_LEARNT_DATA = 0x04;

    // RF commands
    public static final byte COMMAND_BYTE_ENTER_RF_FREQ_LEARNING = 0x19; // Sweep frequency
    public static final byte COMMAND_BYTE_CHECK_RF_FREQ_LEARNING = 0x1A; // Check frequency
    public static final byte COMMAND_BYTE_EXIT_RF_FREQ_LEARNING = 0x1E; // Cancel sweep frequency
    public static final byte COMMAND_BYTE_FIND_RF_PACKET = 0x1B; // Find RF packet
    public static final byte COMMAND_BYTE_CHECK_RF_DATA = 0x4; // Check data

    private final BroadlinkRemoteDynamicCommandDescriptionProvider commandDescriptionProvider;
    private final StorageService storageService;
    protected @Nullable BroadlinkMappingService mappingService;

    public BroadlinkRemoteHandler(Thing thing,
            BroadlinkRemoteDynamicCommandDescriptionProvider commandDescriptionProvider,
            StorageService storageService) {
        super(thing);
        this.commandDescriptionProvider = commandDescriptionProvider;
        this.storageService = storageService;
    }

    @Override
    public void initialize() {
        super.initialize();
        this.mappingService = new BroadlinkMappingService(commandDescriptionProvider,
                new ChannelUID(thing.getUID(), BroadlinkBindingConstants.COMMAND_CHANNEL),
                new ChannelUID(thing.getUID(), BroadlinkBindingConstants.RF_COMMAND_CHANNEL), this.storageService);
    }

    @Override
    public void dispose() {
        BroadlinkMappingService mappingService = this.mappingService;
        if (mappingService != null) {
            mappingService.dispose();
            this.mappingService = null;
        }
        super.dispose();
    }

    protected byte @Nullable [] sendCommand(byte commandByte, String purpose) {
        return sendCommand(commandByte, new byte[0], purpose);
    }

    private byte @Nullable [] sendCommand(byte commandByte, byte[] codeBytes, String purpose) {
        try {
            ByteArrayOutputStream outputStream = buildCommandMessage(commandByte, codeBytes);
            byte[] padded = Utils.padTo(outputStream.toByteArray(), 16);
            byte[] message = buildMessage((byte) 0x6a, padded);
            return sendAndReceiveDatagram(message, purpose);
        } catch (IOException e) {
            updateState(BroadlinkBindingConstants.LEARNING_CONTROL_CHANNEL,
                    new StringType("Error found during when entering IR learning mode"));
            logger.warn("Exception while sending command", e);
        }

        return null;
    }

    protected void sendCode(byte[] code) {
        logger.debug("Sending code: {}", Utils.toHexString(code));
        sendCommand(COMMAND_BYTE_SEND_CODE, code, "send remote code");
    }

    protected ByteArrayOutputStream buildCommandMessage(byte commandByte, byte[] codeBytes) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] preamble = new byte[4];
        preamble[0] = commandByte;
        outputStream.write(preamble);
        if (codeBytes.length > 0) {
            outputStream.write(codeBytes);
        }

        return outputStream;
    }

    protected byte[] extractResponsePayload(byte[] responseBytes) throws IOException {
        byte decryptedResponse[] = decodeDevicePacket(responseBytes);
        // Interesting stuff begins at the fourth byte
        return Utils.slice(decryptedResponse, 4, decryptedResponse.length);
    }

    private void handleIRCommand(String irCommand, boolean replacement) {
        try {
            String message = "";
            if (replacement) {
                message = "Modifying ";
            } else {
                message = "Adding ";
            }

            BroadlinkMappingService mappingService = this.mappingService;
            if (mappingService == null) {
                logger.warn("Mapping service is null, this should not happen");
                updateState(BroadlinkBindingConstants.LEARNING_CONTROL_CHANNEL, new StringType("NULL"));
                return;
            }

            updateState(BroadlinkBindingConstants.LEARNING_CONTROL_CHANNEL,
                    new StringType(message + irCommand + "..."));

            byte[] response = sendCommand(COMMAND_BYTE_CHECK_LEARNT_DATA, "send learnt code check command");

            if (response == null) {
                logger.warn("Got nothing back while getting learnt code");
                updateState(BroadlinkBindingConstants.LEARNING_CONTROL_CHANNEL, new StringType("NULL"));
            } else {
                String hexString = Utils.toHexString(extractResponsePayload(response));
                String cmdLabel = null;
                if (replacement) {
                    cmdLabel = mappingService.replaceCode(irCommand, hexString, CodeType.IR);
                    message = "modified";
                } else {
                    cmdLabel = mappingService.storeCode(irCommand, hexString, CodeType.IR);
                    message = "saved";
                }
                if (cmdLabel != null) {
                    logger.info("Learnt code '{}' ", hexString);
                    updateState(BroadlinkBindingConstants.LEARNING_CONTROL_CHANNEL,
                            new StringType("IR command " + irCommand + " " + message));
                } else {
                    if (replacement) {
                        logger.debug("Command label not previously stored. Skipping");
                        updateState(BroadlinkBindingConstants.LEARNING_CONTROL_CHANNEL,
                                new StringType("IR command " + irCommand + " does not exist"));
                    } else {
                        logger.debug("Command label previously stored. Skipping");
                        updateState(BroadlinkBindingConstants.LEARNING_CONTROL_CHANNEL,
                                new StringType("IR command " + irCommand + " already  exists"));
                    }
                }
            }
        } catch (IOException e) {
            logger.warn("Exception while attempting to check learnt code: {}", e.getMessage());
            updateState(BroadlinkBindingConstants.LEARNING_CONTROL_CHANNEL, new StringType("NULL"));
        }
    }

    @SuppressWarnings("null")
    private void deleteIRCommand(String irCommand) {
        updateState(BroadlinkBindingConstants.LEARNING_CONTROL_CHANNEL,
                new StringType(BroadlinkBindingConstants.LEARNING_CONTROL_COMMAND_DELETE));
        updateState(BroadlinkBindingConstants.LEARNING_CONTROL_CHANNEL,
                new StringType("Deleting IR command " + irCommand + "..."));
        String cmdLabel = mappingService.deleteCode(irCommand, CodeType.IR);
        if (cmdLabel != null) {
            updateState(BroadlinkBindingConstants.LEARNING_CONTROL_CHANNEL,
                    new StringType("IR command " + irCommand + " deleted"));
        } else {
            updateState(BroadlinkBindingConstants.LEARNING_CONTROL_CHANNEL,
                    new StringType("IR command " + irCommand + " not found"));
        }
    }

    void handleLearningCommand(String learningCommand) {
        logger.trace("Sending learning-channel command {}", learningCommand);
        switch (learningCommand) {
            case BroadlinkBindingConstants.LEARNING_CONTROL_COMMAND_LEARN: {
                updateState(BroadlinkBindingConstants.LEARNING_CONTROL_CHANNEL,
                        new StringType(BroadlinkBindingConstants.LEARNING_CONTROL_COMMAND_LEARN));
                sendCommand(COMMAND_BYTE_ENTER_LEARNING, "enter remote code learning mode");
                break;
            }
            case BroadlinkBindingConstants.LEARNING_CONTROL_COMMAND_CHECK: {
                handleIRCommand(thingConfig.getNameOfCommandToLearn(), false);
                break;
            }
            case BroadlinkBindingConstants.LEARNING_CONTROL_COMMAND_MODIFY: {
                handleIRCommand(thingConfig.getNameOfCommandToLearn(), true);
                break;
            }
            case BroadlinkBindingConstants.LEARNING_CONTROL_COMMAND_DELETE: {
                deleteIRCommand(thingConfig.getNameOfCommandToLearn());
                break;
            }
            default: {
                logger.warn("Unrecognised learning channel command: {}", learningCommand);
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (!Utils.isOnline(getThing())) {
            logger.debug("Can't handle command {} because handler for thing {} is not ONLINE", command,
                    getThing().getLabel());
            return;
        }
        if (command instanceof RefreshType) {
            updateItemStatus();
            return;
        }

        ChannelTypeUID channelTypeUID = extractChannelType(channelUID, command);
        if (channelTypeUID == null) {
            return;
        }

        switch (channelTypeUID.getId()) {
            case BroadlinkBindingConstants.COMMAND_CHANNEL: {
                byte code[] = lookupIRCode(command, channelUID);
                if (code != null) {
                    sendCode(code);
                } else {
                    logger.warn("Cannot find the data to send out for command {}", command.toString());
                }
                break;
            }
            case BroadlinkBindingConstants.RF_COMMAND_CHANNEL: {
                byte code[] = lookupRFCode(command, channelUID);
                if (code != null) {
                    sendCode(code);
                } else {
                    logger.warn("Cannot find the data to send out for command {}", command.toString());
                }
                break;
            }
            case BroadlinkBindingConstants.LEARNING_CONTROL_CHANNEL: {
                handleLearningCommand(command.toString());
                break;
            }
            case BroadlinkBindingConstants.RF_LEARNING_CONTROL_CHANNEL: {
                switch (command.toString()) {
                    case BroadlinkBindingConstants.LEARNING_CONTROL_COMMAND_LEARN: {
                        handleFindRFFrequencies();
                        break;
                    }
                    case BroadlinkBindingConstants.LEARNING_CONTROL_COMMAND_CHECK: {
                        handleFindRFCommand(false);
                        break;
                    }
                    case BroadlinkBindingConstants.LEARNING_CONTROL_COMMAND_MODIFY: {
                        handleFindRFCommand(true);
                        break;
                    }
                    case BroadlinkBindingConstants.LEARNING_CONTROL_COMMAND_DELETE: {
                        deleteRFCommand();
                        break;
                    }
                    default: {
                        logger.debug("Thing {} has unknown channel type '{}'", getThing().getLabel(),
                                channelTypeUID.getId());
                        break;
                    }
                }
            }
            default:
                logger.debug("Thing {} has unknown channel type '{}'", getThing().getLabel(), channelTypeUID.getId());
                break;
        }
    }

    protected @Nullable ChannelTypeUID extractChannelType(ChannelUID channelUID, Command command) {
        Channel channel = thing.getChannel(channelUID.getId());
        if (channel == null) {
            logger.warn("Unexpected null channel while handling command {}", command.toFullString());
            return null;
        }
        ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
        if (channelTypeUID == null) {
            logger.warn("Unexpected null channelTypeUID while handling command {}", command.toFullString());
            return null;
        }
        return channelTypeUID;
    }

    private byte @Nullable [] lookupIRCode(Command command, ChannelUID channelUID) {
        byte code[] = null;
        BroadlinkMappingService mappingService = this.mappingService;
        if (mappingService == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Mapping service not defined.");
            return null;
        }

        String value = mappingService.lookupCode(command.toString(), CodeType.IR);

        if (value == null || value.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "No entries found for command in map file, or the file is missing.");
            return null;
        }

        code = HexUtils.hexToBytes(value);

        logger.debug("Transformed command '{}' for thing {}", command, getThing().getLabel());
        return code;
    }

    private byte @Nullable [] lookupRFCode(Command command, ChannelUID channelUID) {
        byte code[] = null;

        BroadlinkMappingService mappingService = this.mappingService;
        if (mappingService == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Mapping service not defined.");
            return null;
        }

        String value = mappingService.lookupCode(command.toString(), CodeType.RF);

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
        // Let the user know we are processing his / her command
        updateState(BroadlinkBindingConstants.RF_LEARNING_CONTROL_CHANNEL,
                new StringType(BroadlinkBindingConstants.LEARNING_CONTROL_COMMAND_LEARN));
        updateState(BroadlinkBindingConstants.LEARNING_CONTROL_CHANNEL, new StringType("Learning new RF code..."));
        sendCommand(COMMAND_BYTE_ENTER_RF_FREQ_LEARNING, "Enter remote rf frequency learning mode");
        boolean freqFound = false;

        long timeout = System.currentTimeMillis() + 30 * 1000;
        HexFormat hex = HexFormat.of();

        try {
            while ((System.currentTimeMillis() < timeout) && (freqFound)) {
                TimeUnit.MILLISECONDS.sleep(500);
                logger.trace("Checking rf frequency");
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

        if (freqFound) {
            sendCommand(COMMAND_BYTE_EXIT_RF_FREQ_LEARNING, "exit remote rf frequency learning mode");
            logger.info("No RF frequency found.");
            updateState(BroadlinkBindingConstants.RF_LEARNING_CONTROL_CHANNEL, new StringType("NULL"));
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Cannot locate the appropriate RF frequency.");
            return;
        }

        updateState(BroadlinkBindingConstants.RF_LEARNING_CONTROL_CHANNEL, new StringType("RF command learnt"));
    }

    private void handleFindRFCommand(boolean replacement) {
        String statusInfo = (replacement) ? "Replacing" : "Adding";
        statusInfo = statusInfo + " RF command " + thingConfig.getNameOfCommandToLearn() + "..";
        updateState(BroadlinkBindingConstants.RF_LEARNING_CONTROL_CHANNEL, new StringType(statusInfo));

        BroadlinkMappingService mappingService = this.mappingService;
        if (mappingService == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Mapping service not defined.");
            return;
        }

        sendCommand(COMMAND_BYTE_FIND_RF_PACKET, "find the rf packet data");

        long timeout = System.currentTimeMillis() + 30 * 1000;

        boolean dataFound = false;

        try {
            byte[] response = new byte[0];
            while ((System.currentTimeMillis() < timeout) && (!dataFound)) {
                TimeUnit.MILLISECONDS.sleep(500);
                byte[] data = sendCommand(COMMAND_BYTE_CHECK_RF_DATA, "check the rf packet data");
                if (data != null) {
                    try {
                        response = extractResponsePayload(data);
                        String hexString = Utils.toHexString(response);
                        String cmdLabel = null;
                        if (replacement) {
                            cmdLabel = mappingService.replaceCode(thingConfig.getNameOfCommandToLearn(), hexString,
                                    CodeType.RF);
                        } else {
                            cmdLabel = mappingService.storeCode(thingConfig.getNameOfCommandToLearn(), hexString,
                                    CodeType.RF);
                        }

                        if (cmdLabel != null) {
                            logger.info("Learnt code '{}' ", hexString);
                            dataFound = true;
                        }
                    } catch (ProtocolException ex) {
                        statusInfo = statusInfo + ".";
                        updateState(BroadlinkBindingConstants.RF_LEARNING_CONTROL_CHANNEL, new StringType(statusInfo));
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            logger.warn("Unexpected exception while checking RF packet data: {}", e.getMessage());
            updateState(BroadlinkBindingConstants.RF_LEARNING_CONTROL_CHANNEL, new StringType("Unexpected error"));
        }

        if (dataFound) {
            if (replacement) {
                updateState(BroadlinkBindingConstants.RF_LEARNING_CONTROL_CHANNEL,
                        new StringType("RF command " + thingConfig.getNameOfCommandToLearn() + " updated"));
            } else {
                updateState(BroadlinkBindingConstants.RF_LEARNING_CONTROL_CHANNEL,
                        new StringType("RF command " + thingConfig.getNameOfCommandToLearn() + " saved"));
            }
        } else {
            updateState(BroadlinkBindingConstants.RF_LEARNING_CONTROL_CHANNEL, new StringType("No data found"));
        }
    }

    private void deleteRFCommand() {
        updateState(BroadlinkBindingConstants.RF_LEARNING_CONTROL_CHANNEL,
                new StringType("Deleting RF command " + thingConfig.getNameOfCommandToLearn() + "..."));

        BroadlinkMappingService mappingService = this.mappingService;
        if (mappingService == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Mapping service not defined.");
            return;
        }

        String cmdLabel = mappingService.deleteCode(thingConfig.getNameOfCommandToLearn(), CodeType.RF);
        if (cmdLabel != null) {
            updateState(BroadlinkBindingConstants.RF_LEARNING_CONTROL_CHANNEL,
                    new StringType("RF command " + thingConfig.getNameOfCommandToLearn() + " deleted"));
        } else {
            updateState(BroadlinkBindingConstants.RF_LEARNING_CONTROL_CHANNEL,
                    new StringType("RF command " + thingConfig.getNameOfCommandToLearn() + " not found"));
        }
    }
}
