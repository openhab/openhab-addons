/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.broadlink.handler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.broadlink.BroadlinkBindingConstants;
import org.openhab.binding.broadlink.internal.BroadlinkMappingService;
import org.openhab.binding.broadlink.internal.BroadlinkRemoteDynamicCommandDescriptionProvider;
import org.openhab.binding.broadlink.internal.Utils;
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
 * Remote blaster handler
 *
 * @author Cato Sognen - Initial contribution
 * @author John Marshall - V3 rewrite with dynamic command description provider
 */
@NonNullByDefault
public class BroadlinkRemoteHandler extends BroadlinkBaseThingHandler {
    private final BroadlinkRemoteDynamicCommandDescriptionProvider commandDescriptionProvider;
    private @Nullable BroadlinkMappingService mappingService;

    public BroadlinkRemoteHandler(Thing thing,
            BroadlinkRemoteDynamicCommandDescriptionProvider commandDescriptionProvider) {
        super(thing);
        this.commandDescriptionProvider = commandDescriptionProvider;
    }

    public void initialize() {
        super.initialize();
        this.mappingService = new BroadlinkMappingService(thingConfig.getMapFilename(), commandDescriptionProvider,
                new ChannelUID(thing.getUID(), BroadlinkBindingConstants.COMMAND_CHANNEL));
    }

    public void dispose() {
        this.mappingService.dispose();
    }

    protected void sendCode(byte code[]) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            byte[] preamble = new byte[4];
            preamble[0] = 2;
            outputStream.write(preamble);
            outputStream.write(code);
            byte[] padded = Utils.padTo(outputStream.toByteArray(), 16);
            byte[] message = buildMessage((byte) 0x6a, padded);
            sendAndReceiveDatagram(message, "remote code");
        } catch (IOException e) {
            logger.warn("Exception while sending code", e);
        }
    }

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
        Channel channel = thing.getChannel(channelUID.getId());
        if (channel == null) {
            logger.warn("Unexpected null channel while handling command {}", command.toFullString());
            return;
        }
        ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
        if (channelTypeUID == null) {
            logger.warn("Unexpected null channelTypeUID while handling command {}", command.toFullString());
            return;
        }
        if (channelTypeUID.getId().equals(BroadlinkBindingConstants.COMMAND_CHANNEL)) {
            logger.debug("Handling ir/rf command '{}' on channel {} of thing {}", command, channelUID.getId(),
                    getThing().getLabel());
            byte code[] = lookupCode(command, channelUID);
            if (code != null) {
                sendCode(code);
            }
        } else {
            logger.debug("Thing {} has unknown channel type '{}'", getThing().getLabel(), channelTypeUID.getId());
        }
    }

    private byte @Nullable [] lookupCode(Command command, ChannelUID channelUID) {
        if (command.toString() == null) {
            logger.debug("Unable to perform transform on null command string");
            return null;
        }

        byte code[] = null;
        String value = this.mappingService.lookup(command.toString());

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
