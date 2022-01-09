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

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lcn.internal.LcnBindingConstants;
import org.openhab.binding.lcn.internal.LcnModuleHandler;
import org.openhab.binding.lcn.internal.common.LcnChannelGroup;
import org.openhab.binding.lcn.internal.common.LcnDefs;
import org.openhab.binding.lcn.internal.common.LcnDefs.LogicOpStatus;
import org.openhab.binding.lcn.internal.connection.ModInfo;
import org.openhab.core.library.types.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles State changes of logic operations of an LCN module.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class LcnModuleLogicSubHandler extends AbstractLcnModuleSubHandler {
    private final Logger logger = LoggerFactory.getLogger(LcnModuleLogicSubHandler.class);
    private static final Pattern PATTERN_SINGLE_LOGIC = Pattern
            .compile(LcnBindingConstants.ADDRESS_REGEX + "S(?<id>\\d{1})(?<logicOpState>\\d{3})");
    private static final Pattern PATTERN_ALL = Pattern
            .compile(LcnBindingConstants.ADDRESS_REGEX + "\\.TL(?<ledStates>[AEBF]{12})(?<logicOpStates>[NTV]{4})");

    public LcnModuleLogicSubHandler(LcnModuleHandler handler, ModInfo info) {
        super(handler, info);
    }

    @Override
    public void handleRefresh(LcnChannelGroup channelGroup, int number) {
        info.refreshLedsAndLogic();
    }

    @Override
    public void handleStatusMessage(Matcher matcher) {
        info.onLedsAndLogicResponseReceived();

        if (matcher.pattern() == PATTERN_ALL) {
            IntStream.range(0, LcnChannelGroup.LED.getCount()).forEach(i -> {
                switch (matcher.group("ledStates").toUpperCase().charAt(i)) {
                    case 'A':
                        fireLed(i, LcnDefs.LedStatus.OFF);
                        break;
                    case 'E':
                        fireLed(i, LcnDefs.LedStatus.ON);
                        break;
                    case 'B':
                        fireLed(i, LcnDefs.LedStatus.BLINK);
                        break;
                    case 'F':
                        fireLed(i, LcnDefs.LedStatus.FLICKER);
                        break;
                    default:
                        logger.warn("Failed to parse LED state: {}", matcher.group("ledStates"));
                }
            });
            IntStream.range(0, LcnChannelGroup.LOGIC.getCount()).forEach(i -> {
                switch (matcher.group("logicOpStates").toUpperCase().charAt(i)) {
                    case 'N':
                        fireLogic(i, LcnDefs.LogicOpStatus.NOT);
                        break;
                    case 'T':
                        fireLogic(i, LcnDefs.LogicOpStatus.OR);
                        break;
                    case 'V':
                        fireLogic(i, LcnDefs.LogicOpStatus.AND);
                        break;
                    default:
                        logger.warn("Failed to parse logic state: {}", matcher.group("logicOpStates"));
                }
            });
        } else if (matcher.pattern() == PATTERN_SINGLE_LOGIC) {
            String rawState = matcher.group("logicOpState");

            LogicOpStatus state;
            switch (rawState) {
                case "000":
                    state = LcnDefs.LogicOpStatus.NOT;
                    break;
                case "025":
                    state = LcnDefs.LogicOpStatus.OR;
                    break;
                case "050":
                    state = LcnDefs.LogicOpStatus.AND;
                    break;
                default:
                    logger.warn("Failed to parse logic state: {}", rawState);
                    return;
            }
            fireLogic(Integer.parseInt(matcher.group("id")) - 1, state);
        }
    }

    private void fireLed(int number, LcnDefs.LedStatus status) {
        fireUpdate(LcnChannelGroup.LED, number, new StringType(status.toString()));
    }

    private void fireLogic(int number, LcnDefs.LogicOpStatus status) {
        fireUpdate(LcnChannelGroup.LOGIC, number, new StringType(status.toString()));
    }

    @Override
    public Collection<Pattern> getPckStatusMessagePatterns() {
        return Arrays.asList(PATTERN_ALL, PATTERN_SINGLE_LOGIC);
    }
}
