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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lcn.internal.LcnBindingConstants;
import org.openhab.binding.lcn.internal.LcnModuleHandler;
import org.openhab.binding.lcn.internal.common.LcnChannelGroup;
import org.openhab.binding.lcn.internal.connection.ModInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle Acks received from an LCN module.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class LcnModuleMetaAckSubHandler extends AbstractLcnModuleSubHandler {
    private final Logger logger = LoggerFactory.getLogger(LcnModuleMetaAckSubHandler.class);
    /** The pattern for the Ack PCK message */
    public static final Pattern PATTERN_POS = Pattern.compile("-M(?<segId>\\d{3})(?<modId>\\d{3})!");
    private static final Pattern PATTERN_NEG = Pattern.compile("-M(?<segId>\\d{3})(?<modId>\\d{3})(?<code>\\d+)");

    public LcnModuleMetaAckSubHandler(LcnModuleHandler handler, ModInfo info) {
        super(handler, info);
    }

    @Override
    public void handleRefresh(LcnChannelGroup channelGroup, int number) {
        // nothing
    }

    @Override
    public void handleStatusMessage(Matcher matcher) {
        if (matcher.pattern() == PATTERN_POS) {
            handler.onAckRceived();
        } else if (matcher.pattern() == PATTERN_NEG) {
            logger.warn("{}: NACK received: {}", handler.getStatusMessageAddress(),
                    codeToString(Integer.parseInt(matcher.group("code"))));
        }
    }

    private String codeToString(int code) {
        switch (code) {
            case LcnBindingConstants.CODE_ACK:
                return "ACK";
            case 5:
                return "Unknown command";
            case 6:
                return "Invalid parameter count";
            case 7:
                return "Invalid parameter";
            case 8:
                return "Command not allowed (e.g. output locked)";
            case 9:
                return "Command not allowed by module's configuration";
            case 10:
                return "Module not capable";
            case 11:
                return "Periphery missing";
            case 12:
                return "Programming mode necessary";
            case 14:
                return "Mains fuse blown";
            default:
                return "Unknown";
        }
    }

    @Override
    public Collection<Pattern> getPckStatusMessagePatterns() {
        return Arrays.asList(PATTERN_POS, PATTERN_NEG);
    }
}
