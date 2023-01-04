/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lcn.internal.LcnModuleHandler;
import org.openhab.binding.lcn.internal.common.LcnChannelGroup;
import org.openhab.binding.lcn.internal.common.LcnException;
import org.openhab.binding.lcn.internal.common.PckGenerator;
import org.openhab.binding.lcn.internal.connection.ModInfo;
import org.openhab.core.library.types.PercentType;

/**
 * Handles Commands and State changes of roller shutters connected to relay outputs of an LCN module.
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public class LcnModuleRollershutterRelayPositionSubHandler extends AbstractLcnModuleRollershutterRelaySubHandler {
    public LcnModuleRollershutterRelayPositionSubHandler(LcnModuleHandler handler, ModInfo info) {
        super(handler, info);
    }

    @Override
    public void handleCommandPercent(PercentType command, LcnChannelGroup channelGroup, int number)
            throws LcnException {
        handler.sendPck(PckGenerator.controlShutterPosition(number, command.intValue()));
    }
}
