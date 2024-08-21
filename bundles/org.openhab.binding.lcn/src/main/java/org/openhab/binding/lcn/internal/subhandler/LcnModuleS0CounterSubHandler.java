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
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lcn.internal.LcnBindingConstants;
import org.openhab.binding.lcn.internal.LcnModuleHandler;
import org.openhab.binding.lcn.internal.common.LcnChannelGroup;
import org.openhab.binding.lcn.internal.common.LcnException;
import org.openhab.binding.lcn.internal.common.Variable;
import org.openhab.binding.lcn.internal.connection.ModInfo;
import org.openhab.core.library.types.DecimalType;

/**
 * Handles Commands and State changes of S0 counter inputs of an LCN module.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class LcnModuleS0CounterSubHandler extends AbstractLcnModuleVariableSubHandler {
    private static final Pattern PATTERN = Pattern
            .compile(LcnBindingConstants.ADDRESS_REGEX + "\\.C(?<id>\\d)(?<value>\\d+)");

    public LcnModuleS0CounterSubHandler(LcnModuleHandler handler, ModInfo info) {
        super(handler, info);
    }

    @Override
    public void handleCommandDecimal(DecimalType command, LcnChannelGroup channelGroup, int number)
            throws LcnException {
        throw new LcnException("Setting S0 counters is not supported");
    }

    @Override
    public void handleStatusMessage(Matcher matcher) throws LcnException {
        fireUpdateAndReset(matcher, "", Variable.s0IdToVar(Integer.parseInt(matcher.group("id")) - 1));
    }

    @Override
    public Collection<Pattern> getPckStatusMessagePatterns() {
        return Set.of(PATTERN);
    }
}
