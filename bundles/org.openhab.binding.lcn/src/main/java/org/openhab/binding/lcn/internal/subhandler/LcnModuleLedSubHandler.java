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
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;

/**
 * Handles Commands and State changes of LEDs of an LCN module.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class LcnModuleLedSubHandler extends AbstractLcnModuleSubHandler {
    public LcnModuleLedSubHandler(LcnModuleHandler handler, ModInfo info) {
        super(handler, info);
    }

    @Override
    public void handleRefresh(LcnChannelGroup channelGroup, int number) {
        info.refreshLedsAndLogic();
    }

    @Override
    public void handleCommandOnOff(OnOffType command, LcnChannelGroup channelGroup, int number) throws LcnException {
        handleCommandString(new StringType(command.toString()), number);
    }

    @Override
    public void handleCommandString(StringType command, int number) throws LcnException {
        handler.sendPck(PckGenerator.controlLed(number, LcnDefs.LedStatus.valueOf(command.toString())));
        info.refreshStatusLedsAnLogicAfterChange();
    }

    @Override
    public void handleStatusMessage(Matcher matcher) {
        /** Status messages are handled in {@link LcnModuleLogicSubHandler}. */
    }

    @Override
    public Collection<Pattern> getPckStatusMessagePatterns() {
        return Collections.emptyList();
    }
}
