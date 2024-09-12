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
package org.openhab.binding.lcn.internal.subhandler;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lcn.internal.LcnBindingConstants;
import org.openhab.binding.lcn.internal.LcnModuleHandler;
import org.openhab.binding.lcn.internal.common.LcnChannelGroup;
import org.openhab.binding.lcn.internal.common.LcnDefs;
import org.openhab.binding.lcn.internal.connection.ModInfo;

/**
 * Handles State changes of transponders and remote controls of an LCN module.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class LcnModuleCodeSubHandler extends AbstractLcnModuleSubHandler {
    private static final Pattern TRANSPONDER_PATTERN = Pattern
            .compile(LcnBindingConstants.ADDRESS_REGEX + "\\.ZT(?<byte0>\\d{3})(?<byte1>\\d{3})(?<byte2>\\d{3})");
    private static final Pattern FINGERPRINT_PATTERN_HEX = Pattern.compile(LcnBindingConstants.ADDRESS_REGEX
            + "\\.ZF(?<byte0>[0-9A-Fa-f]{2})(?<byte1>[0-9A-Fa-f]{2})(?<byte2>[0-9A-Fa-f]{2})$");
    private static final Pattern FINGERPRINT_PATTERN_DEC = Pattern
            .compile(LcnBindingConstants.ADDRESS_REGEX + "\\.ZF(?<byte0>\\d{3})(?<byte1>\\d{3})(?<byte2>\\d{3})");
    private static final Pattern REMOTE_CONTROL_PATTERN = Pattern.compile(LcnBindingConstants.ADDRESS_REGEX
            + "\\.ZI(?<byte0>\\d{3})(?<byte1>\\d{3})(?<byte2>\\d{3})(?<key>\\d{3})(?<action>\\d{3})");

    public LcnModuleCodeSubHandler(LcnModuleHandler handler, ModInfo info) {
        super(handler, info);
    }

    @Override
    public void handleRefresh(LcnChannelGroup channelGroup, int number) {
        // nothing
    }

    @Override
    public void handleStatusMessage(Matcher matcher) {
        String code;

        int base = 10;
        if (matcher.pattern() == FINGERPRINT_PATTERN_HEX) {
            base = 16;
        }

        code = String.format("%02X%02X%02X", Integer.parseInt(matcher.group("byte0"), base),
                Integer.parseInt(matcher.group("byte1"), base), Integer.parseInt(matcher.group("byte2"), base));

        if (matcher.pattern() == TRANSPONDER_PATTERN) {
            handler.triggerChannel(LcnChannelGroup.CODE, "transponder", code);
        } else if (matcher.pattern() == FINGERPRINT_PATTERN_HEX || matcher.pattern() == FINGERPRINT_PATTERN_DEC) {
            handler.triggerChannel(LcnChannelGroup.CODE, "fingerprint", code);
        } else if (matcher.pattern() == REMOTE_CONTROL_PATTERN) {
            int keyNumber = Integer.parseInt(matcher.group("key"));
            String keyLayer;

            if (keyNumber > 30) {
                keyLayer = "D";
                keyNumber -= 30;
            } else if (keyNumber > 20) {
                keyLayer = "C";
                keyNumber -= 20;
            } else if (keyNumber > 10) {
                keyLayer = "B";
                keyNumber -= 10;
            } else if (keyNumber > 0) {
                keyLayer = "A";
            } else {
                return;
            }

            int action = Integer.parseInt(matcher.group("action"));

            if (action > 10) {
                handler.triggerChannel(LcnChannelGroup.CODE, "remotecontrolbatterylow", code);
                action -= 10;
            }

            LcnDefs.SendKeyCommand actionType;
            switch (action) {
                case 1:
                    actionType = LcnDefs.SendKeyCommand.HIT;
                    break;
                case 2:
                    actionType = LcnDefs.SendKeyCommand.MAKE;
                    break;
                case 3:
                    actionType = LcnDefs.SendKeyCommand.BREAK;
                    break;
                default:
                    return;
            }

            handler.triggerChannel(LcnChannelGroup.CODE, "remotecontrolkey",
                    keyLayer + keyNumber + ":" + actionType.name());

            handler.triggerChannel(LcnChannelGroup.CODE, "remotecontrolcode",
                    code + ":" + keyLayer + keyNumber + ":" + actionType.name());
        }
    }

    @Override
    public Collection<Pattern> getPckStatusMessagePatterns() {
        return Arrays.asList(TRANSPONDER_PATTERN, FINGERPRINT_PATTERN_HEX, FINGERPRINT_PATTERN_DEC,
                REMOTE_CONTROL_PATTERN);
    }
}
