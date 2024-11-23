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

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lcn.internal.LcnBindingConstants;
import org.openhab.binding.lcn.internal.LcnModuleHandler;
import org.openhab.binding.lcn.internal.common.LcnChannelGroup;
import org.openhab.binding.lcn.internal.connection.ModInfo;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;

/**
 * Handles Commands and State changes of operating hours counters of an LCN module.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class LcnModuleOperatingHoursCounterSubHandler extends AbstractLcnModuleSubHandler {
    private static final Pattern PATTERN = Pattern.compile("\\$" + LcnBindingConstants.ADDRESS_WITHOUT_PREFIX + //
            "(?<type>[" + Type.createPattern() + "])(?<number>\\d)(?<durationSec>\\d+)");

    private enum Type {
        OUTPUT("A", "output"),
        RELAY("R", "relay"),
        BINARY_INPUT("B", "binarysensor"),
        OUTPUT_RELATIVE_WORK("I", "outputrelativework");

        String pattern;
        String id;

        private Type(String pattern, String id) {
            this.pattern = pattern;
            this.id = id;
        }

        public static String getId(String pattern) {
            return Stream.of(values()).filter(t -> t.pattern.equals(pattern)).findAny().get().id;
        }

        public static String createPattern() {
            return Stream.of(values()).map(t -> t.pattern).collect(Collectors.joining("|"));
        }
    }

    public LcnModuleOperatingHoursCounterSubHandler(LcnModuleHandler handler, ModInfo info) {
        super(handler, info);
    }

    @Override
    public void handleRefresh(LcnChannelGroup channelGroup, int number) {
        // nothing
    }

    @Override
    public Collection<Pattern> getPckStatusMessagePatterns() {
        return Arrays.asList(PATTERN);
    }

    @Override
    public void handleStatusMessage(Matcher matcher) {
        String number = matcher.group("number");
        String type = matcher.group("type");
        long durationSec = Long.parseLong(matcher.group("durationSec"));

        handler.updateChannel(LcnChannelGroup.OPERATINGHOURS, Type.getId(type) + number,
                QuantityType.valueOf(durationSec, Units.SECOND));
    }
}
