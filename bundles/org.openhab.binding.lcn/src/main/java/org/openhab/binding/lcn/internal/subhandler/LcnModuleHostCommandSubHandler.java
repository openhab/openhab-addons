/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.util.Collection;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lcn.internal.LcnModuleHandler;
import org.openhab.binding.lcn.internal.common.LcnChannelGroup;
import org.openhab.binding.lcn.internal.common.LcnDefs;
import org.openhab.binding.lcn.internal.common.LcnException;
import org.openhab.binding.lcn.internal.connection.ModInfo;

/**
 * Handles 'send key' commands sent to this PCK host.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class LcnModuleHostCommandSubHandler extends AbstractLcnModuleSubHandler {
    private static final Pattern SEND_KEY_PATTERN = Pattern
            .compile("\\+M(?<hostId>\\d{3})(?<segId>\\d{3})(?<modId>\\d{3})\\.STH(?<byte0>\\d{3})(?<byte1>\\d{3})");

    public LcnModuleHostCommandSubHandler(LcnModuleHandler handler, ModInfo info) {
        super(handler, info);
    }

    @Override
    public void handleRefresh(LcnChannelGroup channelGroup, int number) {
        // nothing
    }

    @Override
    public void handleStatusMessage(Matcher matcher) throws LcnException {
        int keyTableAndActionMask = Integer.parseInt(matcher.group("byte0"));
        int keyNumberMask = Integer.parseInt(matcher.group("byte1"));

        if ((keyTableAndActionMask & (1 << 6)) == 0) {
            return;
        }

        // PCHK 3.22 supports only the old 'send key' command with key tables A-C
        for (int keyTableNumber = 0; keyTableNumber < LcnDefs.KEY_TABLE_COUNT_UNTIL_0C030C0; keyTableNumber++) {
            String keyTableName = LcnDefs.KeyTable.values()[keyTableNumber].name();

            for (int keyNumber = 0; keyNumber < LcnDefs.KEY_COUNT; keyNumber++) {
                int actionRaw = (keyTableAndActionMask >> (keyTableNumber * 2)) & 3;

                if (actionRaw > LcnDefs.SendKeyCommand.DONTSEND.getId()
                        && actionRaw <= LcnDefs.SendKeyCommand.BREAK.getId()
                        && ((1 << keyNumber) & keyNumberMask) != 0) {
                    String actionName = LcnDefs.SendKeyCommand.get(actionRaw).name();

                    handler.triggerChannel(LcnChannelGroup.HOSTCOMMAND, "sendKeys",
                            keyTableName + (keyNumber + 1) + ":" + actionName);
                }
            }
        }
    }

    @Override
    public Collection<Pattern> getPckStatusMessagePatterns() {
        return Set.of(SEND_KEY_PATTERN);
    }
}
