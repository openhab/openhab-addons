/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.openhab.binding.lcn.internal.LcnBindingConstants;
import org.openhab.binding.lcn.internal.LcnModuleHandler;
import org.openhab.binding.lcn.internal.common.LcnChannelGroup;
import org.openhab.binding.lcn.internal.common.LcnDefs;
import org.openhab.binding.lcn.internal.common.LcnException;
import org.openhab.binding.lcn.internal.common.PckGenerator;
import org.openhab.binding.lcn.internal.common.Variable;
import org.openhab.binding.lcn.internal.common.VariableValue;
import org.openhab.binding.lcn.internal.connection.ModInfo;

/**
 * Handles Commands and State changes of regulator setpoints of an LCN module.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class LcnModuleRvarSetpointSubHandler extends AbstractLcnModuleVariableSubHandler {
    private static final Pattern PATTERN = Pattern
            .compile(LcnBindingConstants.ADDRESS_REGEX + "\\.S(?<id>\\d)(?<value>\\d+)");

    public LcnModuleRvarSetpointSubHandler(LcnModuleHandler handler, ModInfo info) {
        super(handler, info);
    }

    @Override
    public void handleCommandDecimal(DecimalType command, LcnChannelGroup channelGroup, int number)
            throws LcnException {
        Variable variable = getVariable(channelGroup, number);

        if (info.hasExtendedMeasurementProcessing()) {
            handler.sendPck(PckGenerator.setSetpointAbsolute(number, command.intValue()));
        } else {
            try {
                int relativeVariableChange = getRelativeChange(command, variable);
                handler.sendPck(
                        PckGenerator.setSetpointRelative(number, LcnDefs.RelVarRef.CURRENT, relativeVariableChange));
            } catch (LcnException e) {
                // current value unknown for some reason, refresh it in case we come again here
                info.refreshVariable(variable);
                throw e;
            }
        }
    }

    @Override
    public void handleStatusMessage(Matcher matcher) throws LcnException {
        Variable variable = Variable.setPointIdToVar(Integer.parseInt(matcher.group("id")) - 1);
        VariableValue value = fireUpdateAndReset(matcher, "", variable);

        fireUpdate(LcnChannelGroup.RVARLOCK, variable.getNumber(), OnOffType.from(value.isRegulatorLocked()));
    }

    @Override
    public Collection<Pattern> getPckStatusMessagePatterns() {
        return Collections.singleton(PATTERN);
    }
}
