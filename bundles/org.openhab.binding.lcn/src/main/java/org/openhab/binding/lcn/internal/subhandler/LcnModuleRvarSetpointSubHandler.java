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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lcn.internal.LcnBindingConstants;
import org.openhab.binding.lcn.internal.LcnModuleHandler;
import org.openhab.binding.lcn.internal.common.LcnChannelGroup;
import org.openhab.binding.lcn.internal.common.LcnException;
import org.openhab.binding.lcn.internal.common.PckGenerator;
import org.openhab.binding.lcn.internal.common.Variable;
import org.openhab.binding.lcn.internal.common.Variable.Type;
import org.openhab.binding.lcn.internal.common.VariableValue;
import org.openhab.binding.lcn.internal.connection.ModInfo;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles Commands and State changes of regulator setpoints of an LCN module.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class LcnModuleRvarSetpointSubHandler extends AbstractLcnModuleVariableSubHandler {
    private final Logger logger = LoggerFactory.getLogger(LcnModuleRvarSetpointSubHandler.class);
    private static final Pattern PATTERN = Pattern
            .compile(LcnBindingConstants.ADDRESS_REGEX + "\\.S(?<id>\\d)(?<value>\\d{1,5})");

    public LcnModuleRvarSetpointSubHandler(LcnModuleHandler handler, ModInfo info) {
        super(handler, info);
    }

    @Override
    public void handleCommandDecimal(DecimalType command, LcnChannelGroup channelGroup, int number)
            throws LcnException {
        handler.sendPck(PckGenerator.setSetpointAbsolute(number, command.intValue()));
    }

    @Override
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    public void handleStatusMessage(Matcher matcher) throws LcnException {
        Variable variable;
        if (matcher.pattern() == PATTERN) {
            variable = Variable.setPointIdToVar(Integer.parseInt(matcher.group("id")) - 1);
        } else if (matcher.pattern() == LcnBindingConstants.MEASUREMENT_PATTERN_BEFORE_2013) {
            variable = info.getLastRequestedVarWithoutTypeInResponse();

            if (variable.getType() != Type.REGULATOR) {
                return;
            }
        } else {
            logger.warn("Unexpected pattern: {}", matcher.pattern());
            return;
        }
        VariableValue value = fireUpdateAndReset(matcher, "", variable);

        fireUpdate(LcnChannelGroup.RVARLOCK, variable.getNumber(), OnOffType.from(value.isRegulatorLocked()));
    }

    @Override
    public Collection<Pattern> getPckStatusMessagePatterns() {
        return List.of(PATTERN, LcnBindingConstants.MEASUREMENT_PATTERN_BEFORE_2013);
    }
}
