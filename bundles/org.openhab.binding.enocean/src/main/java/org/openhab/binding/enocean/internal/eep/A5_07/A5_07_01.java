/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.enocean.internal.eep.A5_07;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.CHANNEL_GENERAL_SWITCHING;

import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public class A5_07_01 extends A5_07 {

    @SuppressWarnings("null")
    private final Logger logger = LoggerFactory.getLogger(A5_07_01.class);

    private static final String CHANNEL_SWITCH = "switch";

    private static final byte OUTBOUND_PIR_ON = (byte) 0xFF;
    private static final int PIR_OFF = 0x7f;
    private static final byte DB3_DEFAULT = 0x00;
    private static final byte DB2_DEFAULT = 0x00;
    private static final byte DB0_DEFAULT = 0x09;

    public A5_07_01() {
        super();
    }

    public A5_07_01(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected void convertFromCommandImpl(String channelId, String channelTypeId, Command command,
            Function<String, State> getCurrentStateFunc, @Nullable Configuration config) {
        if (!CHANNEL_SWITCH.equals(channelId) && !CHANNEL_GENERAL_SWITCHING.equals(channelTypeId)) {
            throw new IllegalArgumentException("Unsupported channel for A5_07_01 outbound command: " + channelId);
        }

        if (command instanceof OnOffType motionCommand) {
            if (motionCommand == OnOffType.ON) {
                setData(DB3_DEFAULT, DB2_DEFAULT, OUTBOUND_PIR_ON, DB0_DEFAULT);
                logger.debug("A5_07_01 outbound motion switch command: {}, data DB3..DB0={} {} {} {}", motionCommand,
                        String.format("0x%02X", DB3_DEFAULT), String.format("0x%02X", DB2_DEFAULT),
                        String.format("0x%02X", OUTBOUND_PIR_ON), String.format("0x%02X", DB0_DEFAULT));
            } else {
                logger.debug("A5_07_01 outbound motion switch command OFF: no telegram sent");
            }
            return;
        }

        throw new IllegalArgumentException("Unsupported command for A5_07_01 outbound motion detection: " + command);
    }

    @Override
    protected State getIllumination() {
        return UnDefType.UNDEF;
    }

    @Override
    protected State getMotion() {
        return OnOffType.from(PIR_OFF < getDB1Value());
    }

    @Override
    protected State getSupplyVoltage() {
        if (!getBit(getDB0Value(), 0)) {
            return UnDefType.UNDEF;
        }

        return getSupplyVoltage(getDB3Value());
    }
}
