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
import java.util.stream.IntStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lcn.internal.LcnBindingConstants;
import org.openhab.binding.lcn.internal.LcnModuleHandler;
import org.openhab.binding.lcn.internal.common.LcnChannelGroup;
import org.openhab.binding.lcn.internal.common.LcnDefs;
import org.openhab.binding.lcn.internal.common.LcnDefs.RelayStateModifier;
import org.openhab.binding.lcn.internal.common.LcnException;
import org.openhab.binding.lcn.internal.common.PckGenerator;
import org.openhab.binding.lcn.internal.connection.ModInfo;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.UpDownType;

/**
 * Handles Commands and State changes of Relays of an LCN module.
 * 
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class LcnModuleRelaySubHandler extends AbstractLcnModuleSubHandler {
    private static final Pattern PATTERN = Pattern.compile(LcnBindingConstants.ADDRESS_REGEX + "Rx(?<byteValue>\\d+)");

    public LcnModuleRelaySubHandler(LcnModuleHandler handler, ModInfo info) {
        super(handler, info);
    }

    @Override
    public void handleRefresh(LcnChannelGroup channelGroup, int number) {
        info.refreshRelays();
    }

    @Override
    public void handleCommandOnOff(OnOffType command, LcnChannelGroup channelGroup, int number) throws LcnException {
        RelayStateModifier[] relayStateModifiers = createRelayStateModifierArray();
        relayStateModifiers[number] = command == OnOffType.ON ? LcnDefs.RelayStateModifier.ON
                : LcnDefs.RelayStateModifier.OFF;
        handler.sendPck(PckGenerator.controlRelays(relayStateModifiers));
    }

    @Override
    public void handleCommandPercent(PercentType command, LcnChannelGroup channelGroup, int number)
            throws LcnException {
        // don't use OnOffType.as(), because it returns @Nullable
        handleCommandOnOff(command.intValue() > 0 ? OnOffType.ON : OnOffType.OFF, channelGroup, number);
    }

    @Override
    public void handleStatusMessage(Matcher matcher) {
        info.onRelayResponseReceived();

        boolean[] states = LcnDefs.getBooleanValue(Integer.parseInt(matcher.group("byteValue")));

        IntStream.range(0, LcnChannelGroup.RELAY.getCount())
                .forEach(i -> fireUpdate(LcnChannelGroup.RELAY, i, OnOffType.from(states[i])));

        IntStream.range(0, LcnChannelGroup.ROLLERSHUTTERRELAY.getCount()).forEach(i -> {
            UpDownType state = states[i * 2 + 1] ? UpDownType.DOWN : UpDownType.UP;
            fireUpdate(LcnChannelGroup.ROLLERSHUTTERRELAY, i, state);
        });
    }

    @Override
    public Collection<Pattern> getPckStatusMessagePatterns() {
        return Set.of(PATTERN);
    }
}
