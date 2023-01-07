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
import org.openhab.binding.lcn.internal.LcnModuleHandler;
import org.openhab.binding.lcn.internal.common.LcnChannelGroup;
import org.openhab.binding.lcn.internal.common.LcnDefs;
import org.openhab.binding.lcn.internal.common.LcnException;
import org.openhab.binding.lcn.internal.common.PckGenerator;
import org.openhab.binding.lcn.internal.connection.ModInfo;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;

/**
 * Handles Commands and State changes of roller shutters connected to dimmer outputs of an LCN module.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class LcnModuleRollershutterOutputSubHandler extends AbstractLcnModuleSubHandler {
    public LcnModuleRollershutterOutputSubHandler(LcnModuleHandler handler, ModInfo info) {
        super(handler, info);
    }

    @Override
    public void handleRefresh(LcnChannelGroup channelGroup, int number) {
        info.refreshOutput(number);
    }

    @Override
    public void handleCommandUpDown(UpDownType command, LcnChannelGroup channelGroup, int number, boolean invertUpDown)
            throws LcnException {
        // When configured as shutter in LCN-PRO, an output gets switched off, when the other is
        // switched on and vice versa.
        if (command == UpDownType.UP ^ invertUpDown) {
            // first output: 100%
            handler.sendPck(PckGenerator.dimOutput(0, 100, LcnDefs.ROLLER_SHUTTER_RAMP_MS));
        } else {
            // second output: 100%
            handler.sendPck(PckGenerator.dimOutput(1, 100, LcnDefs.ROLLER_SHUTTER_RAMP_MS));
        }
    }

    @Override
    public void handleCommandStopMove(StopMoveType command, LcnChannelGroup channelGroup, int number)
            throws LcnException {
        if (command == StopMoveType.STOP) {
            // both outputs off
            handler.sendPck(PckGenerator.dimOutput(0, 0, 0));
            handler.sendPck(PckGenerator.dimOutput(1, 0, 0));
        } else {
            // roller shutters on outputs are stateless, assume always down when MOVE is sent
            // second output: 100%
            handler.sendPck(PckGenerator.dimOutput(1, 100, LcnDefs.ROLLER_SHUTTER_RAMP_MS));
        }
    }

    @Override
    public void handleStatusMessage(Matcher matcher) {
        // status messages of roller shutters on dimmer outputs are handled in the dimmer output sub handler
    }

    @Override
    public Collection<Pattern> getPckStatusMessagePatterns() {
        return Collections.emptyList();
    }
}
