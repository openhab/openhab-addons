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
import java.util.stream.IntStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lcn.internal.LcnBindingConstants;
import org.openhab.binding.lcn.internal.LcnModuleHandler;
import org.openhab.binding.lcn.internal.common.LcnChannelGroup;
import org.openhab.binding.lcn.internal.common.LcnDefs;
import org.openhab.binding.lcn.internal.connection.ModInfo;
import org.openhab.core.library.types.OpenClosedType;

/**
 * Handles State changes of binary sensors of an LCN module.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class LcnModuleBinarySensorSubHandler extends AbstractLcnModuleSubHandler {
    private static final Pattern PATTERN = Pattern.compile(LcnBindingConstants.ADDRESS_REGEX + "Bx(?<byteValue>\\d+)");

    public LcnModuleBinarySensorSubHandler(LcnModuleHandler handler, ModInfo info) {
        super(handler, info);
    }

    @Override
    public void handleRefresh(LcnChannelGroup channelGroup, int number) {
        info.refreshBinarySensors();
    }

    @Override
    public void handleStatusMessage(Matcher matcher) {
        info.onBinarySensorsResponseReceived();

        boolean[] states = LcnDefs.getBooleanValue(Integer.parseInt(matcher.group("byteValue")));

        IntStream.range(0, LcnChannelGroup.BINARYSENSOR.getCount())
                .forEach(i -> fireUpdate(LcnChannelGroup.BINARYSENSOR, i,
                        states[i] ? OpenClosedType.OPEN : OpenClosedType.CLOSED));
    }

    @Override
    public Collection<Pattern> getPckStatusMessagePatterns() {
        return Collections.singleton(PATTERN);
    }
}
