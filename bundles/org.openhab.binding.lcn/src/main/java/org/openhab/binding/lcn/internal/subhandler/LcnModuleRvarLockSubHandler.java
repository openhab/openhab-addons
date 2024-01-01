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

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lcn.internal.LcnModuleHandler;
import org.openhab.binding.lcn.internal.common.LcnChannelGroup;
import org.openhab.binding.lcn.internal.common.LcnException;
import org.openhab.binding.lcn.internal.common.PckGenerator;
import org.openhab.binding.lcn.internal.common.Variable;
import org.openhab.binding.lcn.internal.connection.ModInfo;
import org.openhab.core.library.types.OnOffType;

/**
 * Handles Commands and State changes of regulator locks of an LCN module.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class LcnModuleRvarLockSubHandler extends AbstractLcnModuleVariableSubHandler {
    public LcnModuleRvarLockSubHandler(LcnModuleHandler handler, ModInfo info) {
        super(handler, info);
    }

    @Override
    public void handleRefresh(LcnChannelGroup channelGroup, int number) {
        super.handleRefresh(LcnChannelGroup.RVARSETPOINT, number);
    }

    @Override
    public void handleCommandOnOff(OnOffType command, LcnChannelGroup channelGroup, int number) throws LcnException {
        boolean locked = command == OnOffType.ON;
        handler.sendPck(PckGenerator.lockRegulator(number, locked));

        // request new lock state, if the module doesn't send it on itself
        Variable variable = getVariable(LcnChannelGroup.RVARSETPOINT, number);
        if (info.getFirmwareVersion().map(v -> variable.shouldPollStatusAfterRegulatorLock(v, locked)).orElse(true)) {
            info.refreshVariable(variable);
        }
    }

    @Override
    public void handleStatusMessage(Matcher matcher) {
        // status messages are handled in the RVar setpoint sub handler
    }

    @Override
    public Collection<Pattern> getPckStatusMessagePatterns() {
        return Collections.emptyList();
    }
}
