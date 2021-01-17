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

import org.apache.commons.lang.StringUtils;
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
            thingLogger.logError("Exception while sending code", e);
        }
    }

    public void handleCommand(ChannelUID channelUID, Command command) {
        if (!Utils.isOnline(getThing())) {
            thingLogger.logDebug("Can't handle command " + command + " because handler for thing "
                    + getThing().getLabel() + " is not ONLINE");
            return;
        }
        if (command instanceof RefreshType) {
            updateItemStatus();
            return;
        }
        Channel channel = thing.getChannel(channelUID.getId());
        if (channel == null) {
            thingLogger.logError("Unexpected null channel while handling command " + command.toFullString());
            return;
        }
        ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
        if (channelTypeUID == null) {
            thingLogger.logError("Unexpected null channelTypeUID while handling command " + command.toFullString());
            return;
        }
        String s;
        switch ((s = channelTypeUID.getId()).hashCode()) {
            case 950394699: // FIXME WTF?!?!
                if (s.equals("command")) {
                    thingLogger.logDebug(String.format("Handling ir/rf command '%s' on channel %s of thing %s", command,
                            channelUID.getId(), getThing().getLabel()));
                    byte code[] = lookupCode(command, channelUID);
                    if (code != null)
                        sendCode(code);
                    break;
                }
                // fall through

            default:
                thingLogger.logDebug(
                        "Thing " + getThing().getLabel() + " has unknown channel type " + channelTypeUID.getId());
                break;
        }
    }

    private byte @Nullable [] lookupCode(Command command, ChannelUID channelUID) {
        if (command.toString() == null) {
            thingLogger.logDebug("Unable to perform transform on null command string");
            return null;
        }
        String mapFile = (String) thing.getConfiguration().get("mapFilename");
        if (StringUtils.isEmpty(mapFile)) {
            thingLogger.logDebug("MAP file is not defined in configuration of thing " + getThing().getLabel());
            return null;
        }
        BundleContext bundleContext = FrameworkUtil.getBundle(BroadlinkRemoteHandler.class).getBundleContext();
        TransformationService transformService = TransformationHelper.getTransformationService(bundleContext, "MAP");
        if (transformService == null) {
            thingLogger.logError("Failed to get MAP transformation service for thing " + getThing().getLabel()
                    + "; is bundle installed?");
            return null;
        }
        byte code[] = null;
        String value;
        try {
            value = transformService.transform(mapFile, command.toString());
            if (value == null || StringUtils.isEmpty(value)) {
                thingLogger.logError(String.format("No entry for command '%s' in map file '%s' for thing %s", command,
                        mapFile, getThing().getLabel()));
                return null;
            }
            code = HexUtils.hexToBytes(value);
        } catch (TransformationException e) {
            thingLogger.logError(String.format("Failed to transform command '%s' for thing %s using map file '%s'",
                    command, getThing().getLabel(), mapFile), e);
            return null;
        }

        thingLogger.logDebug(String.format("Transformed command '%s' for thing %s with map file '%s'", command,
                getThing().getLabel(), mapFile));
        return code;
    }
}
