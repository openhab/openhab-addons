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
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lcn.internal.LcnBindingConstants;
import org.openhab.binding.lcn.internal.LcnModuleHandler;
import org.openhab.binding.lcn.internal.common.LcnChannelGroup;
import org.openhab.binding.lcn.internal.common.LcnDefs;
import org.openhab.binding.lcn.internal.common.LcnException;
import org.openhab.binding.lcn.internal.common.PckGenerator;
import org.openhab.binding.lcn.internal.common.Variable;
import org.openhab.binding.lcn.internal.connection.ModInfo;
import org.openhab.core.library.types.DecimalType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles Commands and State changes of thresholds of an LCN module.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class LcnModuleThresholdSubHandler extends AbstractLcnModuleVariableSubHandler {
    private final Logger logger = LoggerFactory.getLogger(LcnModuleThresholdSubHandler.class);
    private static final Pattern PATTERN = Pattern
            .compile(LcnBindingConstants.ADDRESS_REGEX + "\\.T(?<registerId>\\d)(?<thresholdId>\\d)(?<value>\\d+)");
    private static final Pattern PATTERN_BEFORE_2013 = Pattern.compile(LcnBindingConstants.ADDRESS_REGEX
            + "\\.S1(?<value0>\\d{5})(?<value1>\\d{5})(?<value2>\\d{5})(?<value3>\\d{5})(?<value4>\\d{5})(?<hyst>\\d{5})");

    public LcnModuleThresholdSubHandler(LcnModuleHandler handler, ModInfo info) {
        super(handler, info);
    }

    @Override
    public void handleCommandDecimal(DecimalType command, LcnChannelGroup channelGroup, int number)
            throws LcnException {
        Variable variable = getVariable(channelGroup, number);
        try {
            int relativeChange = getRelativeChange(command, variable);
            handler.sendPck(PckGenerator.setThresholdRelative(variable, LcnDefs.RelVarRef.CURRENT, relativeChange,
                    info.hasExtendedMeasurementProcessing()));

            // request new value, if the module doesn't send it on itself
            if (info.getFirmwareVersion().map(v -> variable.shouldPollStatusAfterCommand(v)).orElse(true)) {
                info.refreshVariable(variable);
            }
        } catch (LcnException e) {
            // current value unknown for some reason, refresh it in case we come again here
            info.refreshVariable(variable);
            throw e;
        }
    }

    @Override
    public void handleStatusMessage(Matcher matcher) {
        IntStream stream;
        Optional<String> groupSuffix;
        int registerNumber;
        if (matcher.pattern() == PATTERN) {
            int thresholdId = Integer.parseInt(matcher.group("thresholdId")) - 1;
            registerNumber = Integer.parseInt(matcher.group("registerId")) - 1;
            stream = IntStream.rangeClosed(thresholdId, thresholdId);
            groupSuffix = Optional.of("");
        } else if (matcher.pattern() == PATTERN_BEFORE_2013) {
            stream = IntStream.range(0, LcnDefs.THRESHOLD_COUNT_BEFORE_2013);
            groupSuffix = Optional.empty();
            registerNumber = 0;
        } else {
            logger.warn("Unexpected pattern: {}", matcher.pattern());
            return;
        }

        stream.forEach(i -> {
            try {
                fireUpdateAndReset(matcher, groupSuffix.orElse(String.valueOf(i)),
                        Variable.thrsIdToVar(registerNumber, i));
            } catch (LcnException e) {
                logger.warn("Parse error: {}", e.getMessage());
            }
        });
    }

    @Override
    public Collection<Pattern> getPckStatusMessagePatterns() {
        return Arrays.asList(PATTERN, PATTERN_BEFORE_2013);
    }
}
