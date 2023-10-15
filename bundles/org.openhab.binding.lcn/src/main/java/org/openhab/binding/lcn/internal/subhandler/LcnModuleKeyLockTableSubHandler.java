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

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lcn.internal.LcnBindingConstants;
import org.openhab.binding.lcn.internal.LcnModuleHandler;
import org.openhab.binding.lcn.internal.common.LcnChannelGroup;
import org.openhab.binding.lcn.internal.common.LcnDefs;
import org.openhab.binding.lcn.internal.common.LcnDefs.KeyLockStateModifier;
import org.openhab.binding.lcn.internal.common.LcnException;
import org.openhab.binding.lcn.internal.common.PckGenerator;
import org.openhab.binding.lcn.internal.connection.ModInfo;
import org.openhab.core.library.types.OnOffType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles Commands and State changes of key table locks of an LCN module.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class LcnModuleKeyLockTableSubHandler extends AbstractLcnModuleSubHandler {
    private final Logger logger = LoggerFactory.getLogger(LcnModuleKeyLockTableSubHandler.class);
    private static final Pattern PATTERN = Pattern.compile(LcnBindingConstants.ADDRESS_REGEX
            + "\\.TX(?<table0>\\d{3})(?<table1>\\d{3})(?<table2>\\d{3})((?<table3>\\d{3}))?");

    public LcnModuleKeyLockTableSubHandler(LcnModuleHandler handler, ModInfo info) {
        super(handler, info);
    }

    @Override
    public void handleRefresh(LcnChannelGroup channelGroup, int number) {
        info.refreshStatusLockedKeys();
    }

    @Override
    public void handleRefresh(String groupId) {
        // nothing
    }

    @Override
    public void handleCommandOnOff(OnOffType command, LcnChannelGroup channelGroup, int number) throws LcnException {
        KeyLockStateModifier[] keyLockStateModifiers = new LcnDefs.KeyLockStateModifier[channelGroup.getCount()];
        Arrays.fill(keyLockStateModifiers, LcnDefs.KeyLockStateModifier.NOCHANGE);
        keyLockStateModifiers[number] = command == OnOffType.ON ? LcnDefs.KeyLockStateModifier.ON
                : LcnDefs.KeyLockStateModifier.OFF;
        int tableId = channelGroup.ordinal() - LcnChannelGroup.KEYLOCKTABLEA.ordinal();
        handler.sendPck(PckGenerator.lockKeys(tableId, keyLockStateModifiers));
        info.refreshStatusStatusLockedKeysAfterChange();
    }

    @Override
    public void handleStatusMessage(Matcher matcher) {
        info.onLockedKeysResponseReceived();

        IntStream.range(0, LcnDefs.KEY_TABLE_COUNT).forEach(tableId -> {
            String stateString = matcher.group(String.format("table%d", tableId));
            if (stateString != null) {
                boolean[] states = LcnDefs.getBooleanValue(Integer.parseInt(stateString));
                try {
                    LcnChannelGroup channelGroup = LcnChannelGroup.fromTableId(tableId);
                    for (int i = 0; i < states.length; i++) {
                        fireUpdate(channelGroup, i, states[i] ? OnOffType.ON : OnOffType.OFF);
                    }
                } catch (LcnException e) {
                    logger.warn("Failed to set key table lock state: {}", e.getMessage());
                }
            }
        });
    }

    @Override
    public Collection<Pattern> getPckStatusMessagePatterns() {
        return Set.of(PATTERN);
    }
}
