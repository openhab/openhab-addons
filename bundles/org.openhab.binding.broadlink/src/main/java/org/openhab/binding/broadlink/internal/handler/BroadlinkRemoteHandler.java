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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.broadlink.internal.BroadlinkBindingConstants;
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
    public static final byte COMMAND_BYTE_ENTER_LEARNING = 0x03;
    public static final byte COMMAND_BYTE_CHECK_LEARNT_DATA = 0x04;

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
        // TODO: check if this also works for a Mini without RF
        this.mappingService = new BroadlinkMappingService("broadlink_" + thing.getUID() + ".rf",
                "broadlink_" + thing.getUID() + ".ir", commandDescriptionProvider,
                new ChannelUID(thing.getUID(), BroadlinkBindingConstants.COMMAND_CHANNEL),
                new ChannelUID(thing.getUID(), BroadlinkBindingConstants.RF_COMMAND_CHANNEL), this.storageService);
    }

    @Override
    @SuppressWarnings("null")
    public void dispose() {
        if (this.mappingService != null) {
            this.mappingService.dispose();
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
            logger.warn("Exception while sending command", e);
        }

        return null;
    }

    protected void sendCode(byte[] code) {
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

    private void sendCheckDataCommandAndLog(String irCommand) {
        try {
            updateState(BroadlinkBindingConstants.LEARNING_CONTROL_CHANNEL,
                    new StringType(BroadlinkBindingConstants.LEARNING_CONTROL_COMMAND_CHECK));
            byte[] response = sendCommand(COMMAND_BYTE_CHECK_LEARNT_DATA, "send learnt code check command");
            if (response == null) {
                logger.warn("Got nothing back while getting learnt code");
                updateState(BroadlinkBindingConstants.LEARNING_CONTROL_CHANNEL, new StringType("NULL"));
            } else {
                String hexString = Utils.toHexString(extractResponsePayload(response));
                mappingService.storeIR(irCommand, hexString);
                logger.info("BEGIN LAST LEARNT CODE");
                logger.info("{}", hexString);
                logger.info("END LAST LEARNT CODE ({} characters)", hexString.length());
                updateState(BroadlinkBindingConstants.LEARNING_CONTROL_CHANNEL,
                        new StringType("IR command " + irCommand + " saved"));
            }
        } catch (IOException e) {
            logger.warn("Exception while attempting to check learnt code: {}", e.getMessage());
            updateState(BroadlinkBindingConstants.LEARNING_CONTROL_CHANNEL, new StringType("NULL"));
        }
    }

    void handleLearningCommand(String learningCommand) {
        logger.trace("Sending learning-channel command {}", learningCommand);
        switch (learningCommand) {
            case BroadlinkBindingConstants.LEARNING_CONTROL_COMMAND_LEARN:
                logger.trace("Learning IR command {}", thingConfig.getNameOfCommandToLearn());
                updateState(BroadlinkBindingConstants.LEARNING_CONTROL_CHANNEL,
                        new StringType(BroadlinkBindingConstants.LEARNING_CONTROL_COMMAND_LEARN));
                sendCommand(COMMAND_BYTE_ENTER_LEARNING, "enter remote code learning mode");
                break;
            case BroadlinkBindingConstants.LEARNING_CONTROL_COMMAND_CHECK:
                sendCheckDataCommandAndLog(thingConfig.getNameOfCommandToLearn());
                break;
            default:
                logger.warn("Unrecognised learning channel command: {}", learningCommand);
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
                logger.debug("Handling ir command '{}' on channel {} of thing {}", command, channelUID.getId(),
                        getThing().getLabel());
                byte code[] = lookupIRCode(command, channelUID);
                if (code != null) {
                    sendCode(code);
                }
                break;
            }
            case BroadlinkBindingConstants.LEARNING_CONTROL_CHANNEL:
                handleLearningCommand(command.toString());
                break;
            default:
                // logger.debug("Thing {} has unknown channel type '{}'", getThing().getLabel(),
                // channelTypeUID.getId());
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

    @SuppressWarnings("null")
    private byte @Nullable [] lookupIRCode(Command command, ChannelUID channelUID) {
        byte code[] = null;
        String value = this.mappingService.lookupIR(command.toString());

        if (value == null || value.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "No entries found for command in map file, or the file is missing.");
            return null;
        }

        code = HexUtils.hexToBytes(value);

        logger.debug("Transformed command '{}' for thing {}", command, getThing().getLabel());
        return code;
    }
}
