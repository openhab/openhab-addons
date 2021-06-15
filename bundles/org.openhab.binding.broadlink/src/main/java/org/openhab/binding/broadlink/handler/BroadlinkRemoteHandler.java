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
import org.openhab.binding.broadlink.internal.Utils;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.transform.TransformationException;
import org.openhab.core.transform.TransformationHelper;
import org.openhab.core.transform.TransformationService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.util.HexUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Remote blaster handler
 *
 * @author John Marshall/Cato Sognen - Initial contribution
 */
@NonNullByDefault
public class BroadlinkRemoteHandler extends BroadlinkBaseThingHandler {

    @Nullable
    private TransformationService transformService;

    public BroadlinkRemoteHandler(Thing thing) {
        super(thing, LoggerFactory.getLogger(BroadlinkRemoteHandler.class));
    }

    public BroadlinkRemoteHandler(Thing thing, Logger logger) {
        super(thing, logger);
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
            logger.error("Exception while sending code", e);
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
            logger.error("Unexpected null channel while handling command {}", command.toFullString());
            return;
        }
        ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
        if (channelTypeUID == null) {
            logger.error("Unexpected null channelTypeUID while handling command {}", command.toFullString());
            return;
        }
        if (channelTypeUID.getId().equals("command")) {
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

    @Nullable
    private TransformationService getTransformService() {
        // Lazy-load it
        if (transformService == null) {
            BundleContext bundleContext = FrameworkUtil.getBundle(BroadlinkRemoteHandler.class).getBundleContext();
            transformService = TransformationHelper.getTransformationService(bundleContext, "MAP");
            if (transformService == null) {
                logger.error("Failed to get MAP transformation service for thing {}; is bundle installed?",
                        getThing().getLabel());
            }
        }
        return transformService;
    }

    private byte @Nullable [] lookupCode(Command command, ChannelUID channelUID) {
        if (command.toString() == null) {
            logger.debug("Unable to perform transform on null command string");
            return null;
        }
        String mapFile = (String) thing.getConfiguration().get("mapFilename");
        if (mapFile == null || mapFile.isEmpty()) {
            logger.debug("MAP file is not defined in configuration of thing {}", getThing().getLabel());
            return null;
        }

        byte code[] = null;
        String value;
        try {
            value = getTransformService().transform(mapFile, command.toString());

            if (value == null || value.isEmpty()) {
                logger.error("No entry for command '{}' in map file '{}' for thing {}", command, mapFile,
                        getThing().getLabel());
                return null;
            }

            code = HexUtils.hexToBytes(value);
        } catch (TransformationException e) {
            logger.error("Failed to transform command '{}' for thing {} using map file '{}'", command,
                    getThing().getLabel(), mapFile, e);
            return null;
        }

        logger.debug("Transformed command '{}' for thing {} with map file '{}'", command, getThing().getLabel(),
                mapFile);
        return code;
    }
}
