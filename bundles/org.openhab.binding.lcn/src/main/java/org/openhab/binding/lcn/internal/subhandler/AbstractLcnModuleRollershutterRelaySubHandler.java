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
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lcn.internal.LcnBindingConstants;
import org.openhab.binding.lcn.internal.LcnModuleHandler;
import org.openhab.binding.lcn.internal.common.LcnChannelGroup;
import org.openhab.binding.lcn.internal.common.LcnDefs;
import org.openhab.binding.lcn.internal.common.LcnDefs.RelayStateModifier;
import org.openhab.binding.lcn.internal.common.LcnException;
import org.openhab.binding.lcn.internal.common.PckGenerator;
import org.openhab.binding.lcn.internal.connection.ModInfo;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;

/**
 * Handles Commands and State changes of roller shutters connected to relays of an LCN module.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractLcnModuleRollershutterRelaySubHandler extends AbstractLcnModuleSubHandler {
    private static final String POSITION = "P";
    private static final String ANGLE = "W";
    private static final Pattern PATTERN = Pattern.compile(LcnBindingConstants.ADDRESS_REGEX + //
            "(?<type>[" + POSITION + "|" + ANGLE + "])(?<shutterNumber>\\d)(?<percent>\\d{3})");

    public AbstractLcnModuleRollershutterRelaySubHandler(LcnModuleHandler handler, ModInfo info) {
        super(handler, info);
    }

    @Override
    public void handleRefresh(LcnChannelGroup channelGroup, int number) {
        info.refreshRelays();
    }

    @Override
    public void handleCommandUpDown(UpDownType command, LcnChannelGroup channelGroup, int number, boolean invertUpDown)
            throws LcnException {
        RelayStateModifier[] relayStateModifiers = createRelayStateModifierArray();
        // direction relay
        relayStateModifiers[number * 2 + 1] = command == UpDownType.DOWN ^ invertUpDown ? LcnDefs.RelayStateModifier.ON
                : LcnDefs.RelayStateModifier.OFF;
        // power relay
        relayStateModifiers[number * 2] = LcnDefs.RelayStateModifier.ON;
        handler.sendPck(PckGenerator.controlRelays(relayStateModifiers));
    }

    @Override
    public void handleCommandStopMove(StopMoveType command, LcnChannelGroup channelGroup, int number)
            throws LcnException {
        RelayStateModifier[] relayStateModifiers = createRelayStateModifierArray();
        // power relay
        relayStateModifiers[number * 2] = command == StopMoveType.MOVE ? LcnDefs.RelayStateModifier.ON
                : LcnDefs.RelayStateModifier.OFF;
        handler.sendPck(PckGenerator.controlRelays(relayStateModifiers));
    }

    @Override
    public void handleStatusMessage(Matcher matcher) {
        int shutterNumber = Integer.parseInt(matcher.group("shutterNumber")) - 1;
        int percent = Integer.parseInt(matcher.group("percent"));

        LcnChannelGroup group;
        if (POSITION.equals(matcher.group("type"))) {
            group = LcnChannelGroup.ROLLERSHUTTERRELAY;
        } else {
            group = LcnChannelGroup.ROLLERSHUTTERRELAYSLAT;
        }

        fireUpdate(group, shutterNumber, new PercentType(percent));
    }

    @Override
    public Collection<Pattern> getPckStatusMessagePatterns() {
        return Collections.singleton(PATTERN);
    }
}
