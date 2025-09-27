/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.enocean.internal.eep.D2_50;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enocean.internal.Helper;
import org.openhab.binding.enocean.internal.eep.Base._VLDMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public class D2_50 extends _VLDMessage {

    protected static final byte MT_MASK = (byte) 0xf0;
    protected static final byte MT_REMOTE_TRANSMISSION_REQUEST = 0x00;
    protected static final byte MT_CONTROL = 0x20;
    protected static final byte MT_BASIC_STATUS = 0x40;
    protected static final byte MT_EXTENDED_STATUS = 0x60; // not yet implemented
    protected static final byte MT_UNKNOWN_STATUS = (byte) 0x80; // Sent by some systems during teach in

    protected static final byte RMT_MASK = (byte) 0x0f;
    protected static final byte RMT_BASIC_STATUS = 0x00;
    protected static final byte RMT_EXTENDED_STATUS = 0x01; // not yet implemented

    protected static final byte DOMC_NOACTION = 0x0f;
    protected static final byte CONTROL_NOACTION = 0;
    protected static final byte TMOC_NOACTION = 127;
    protected static final byte TMOC_ACTIVATE = (byte) 0xff;
    protected static final byte THRESHOLD_NOACTION = 127;

    public D2_50() {
    }

    public D2_50(ERP1Message packet) {
        super(packet);
    }

    protected byte getMessageType(byte b) {
        return (byte) (b & MT_MASK);
    }

    @Override
    public void addConfigPropertiesTo(DiscoveryResultBuilder discoveredThingResultBuilder) {
        discoveredThingResultBuilder.withProperty(PARAMETER_SENDINGEEPID, getEEPType().getId())
                .withProperty(PARAMETER_RECEIVINGEEPID, getEEPType().getId());
    }

    @Override
    protected void convertFromCommandImpl(String channelId, String channelTypeId, Command command,
            Function<String, State> getCurrentStateFunc, @Nullable Configuration config) {
        // we need to send just a single message to refresh all channel states, hence just send refresh for OM
        if (command == RefreshType.REFRESH && CHANNEL_VENTILATIONOPERATIONMODE.equals(channelId)) {
            setData((byte) (MT_REMOTE_TRANSMISSION_REQUEST + RMT_BASIC_STATUS));
        } else {
            switch (channelId) {
                case CHANNEL_VENTILATIONOPERATIONMODE:
                    if (command instanceof StringType stringCommand) {
                        byte value = (byte) (Helper.tryParseInt(stringCommand.toString(), 15) & 0x0f);
                        setData((byte) (MT_CONTROL + value), CONTROL_NOACTION, TMOC_NOACTION, THRESHOLD_NOACTION,
                                THRESHOLD_NOACTION, CONTROL_NOACTION);
                    }
                    break;
                case CHANNEL_TIMEROPERATIONMODE:
                    if (command instanceof OnOffType onOffCommand) {
                        byte value = onOffCommand == OnOffType.ON ? TMOC_ACTIVATE : TMOC_NOACTION;
                        setData((byte) (MT_CONTROL + DOMC_NOACTION), CONTROL_NOACTION, value, THRESHOLD_NOACTION,
                                THRESHOLD_NOACTION, CONTROL_NOACTION);
                    }
                    break;
            }
        }
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId,
            Function<String, State> getCurrentStateFunc, Configuration config) {
        if (getMessageType(bytes[0]) != MT_BASIC_STATUS) {
            return UnDefType.UNDEF;
        }

        switch (channelId) {
            case CHANNEL_VENTILATIONOPERATIONMODE:
                return new StringType(String.valueOf(bytes[0] & 0x0f));
            case CHANNEL_FIREPLACESAFETYMODE:
                return OnOffType.from(getBit(bytes[1], 3));
            case CHANNEL_HEATEXCHANGERBYPASSSTATUS:
                return getBit(bytes[1], 2) ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
            case CHANNEL_SUPPLYAIRFLAPSTATUS:
                return getBit(bytes[1], 1) ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
            case CHANNEL_EXHAUSTAIRFLAPSTATUS:
                return getBit(bytes[1], 0) ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
            case CHANNEL_DEFROSTMODE:
                return OnOffType.from(getBit(bytes[2], 7));
            case CHANNEL_COOLINGPROTECTIONMODE:
                return OnOffType.from(getBit(bytes[2], 6));
            case CHANNEL_OUTDOORAIRHEATERSTATUS:
                return OnOffType.from(getBit(bytes[2], 5));
            case CHANNEL_SUPPLYAIRHEATERSTATUS:
                return OnOffType.from(getBit(bytes[2], 4));
            case CHANNEL_DRAINHEATERSTATUS:
                return OnOffType.from(getBit(bytes[2], 3));
            case CHANNEL_TIMEROPERATIONMODE:
                return OnOffType.from(getBit(bytes[2], 2));
            case CHANNEL_MAINTENANCESTATUS:
                return OnOffType.from(getBit(bytes[2], 1));
            case CHANNEL_WEEKLYTIMERPROGRAMSTATUS:
                return OnOffType.from(getBit(bytes[2], 0));
            case CHANNEL_ROOMTEMPERATURECONTROLSTATUS:
                return OnOffType.from(getBit(bytes[3], 7));
            case CHANNEL_AIRQUALITYVALUE1:
                return new QuantityType<>((bytes[3] & 0x7f), Units.PERCENT);
            case CHANNEL_AIRQUALITYVALUE2:
                return new QuantityType<>((bytes[4] & 0x7f), Units.PERCENT);
            case CHANNEL_OUTDOORAIRTEMPERATURE:
                return new QuantityType<>(-63 + ((bytes[5] & 0xff) >>> 1), SIUnits.CELSIUS);
            case CHANNEL_SUPPLYAIRTEMPERATURE:
                return new QuantityType<>(-63 + ((bytes[6] & 0xff) >>> 2) + ((bytes[5] & 1) << 6), SIUnits.CELSIUS);
            case CHANNEL_INDOORAIRTEMPERATURE:
                return new QuantityType<>(-63 + ((bytes[7] & 0xff) >>> 3) + ((bytes[6] & 0b11) << 5), SIUnits.CELSIUS);
            case CHANNEL_EXHAUSTAIRTEMPERATURE:
                return new QuantityType<>(-63 + ((bytes[8] & 0xff) >>> 4) + ((bytes[7] & 0b111) << 4), SIUnits.CELSIUS);
            case CHANNEL_SUPPLYAIRFANAIRFLOWRATE:
                return new QuantityType<>(((bytes[9] & 0xff) >>> 2) + ((bytes[8] & 0b1111) << 6),
                        Units.CUBICMETRE_PER_MINUTE);
            case CHANNEL_EXHAUSTAIRFANAIRFLOWRATE:
                return new QuantityType<>((bytes[10] & 0xff) + ((bytes[9] & 0b11) << 8), Units.CUBICMETRE_PER_MINUTE);
            case CHANNEL_SUPPLYFANSPEED:
                return new DecimalType(((bytes[12] & 0xff) >>> 4) + (bytes[11] << 4));
            case CHANNEL_EXHAUSTFANSPEED:
                return new DecimalType((bytes[13] & 0xff) + ((bytes[12] & 0b1111) << 8));
        }

        return UnDefType.UNDEF;
    }

    @Override
    protected boolean validateData(byte[] bytes) {
        if (bytes.length == 0) {
            return false;
        }

        switch (getMessageType(bytes[0])) {
            case MT_REMOTE_TRANSMISSION_REQUEST:
                return bytes.length == 1;
            case MT_CONTROL:
                return bytes.length == 6;
            case MT_BASIC_STATUS:
                return bytes.length == 14;
            case MT_EXTENDED_STATUS: // MT_EXTENDED_STATUS is not yet supported, however return true to avoid Exceptions
                return true;
            case MT_UNKNOWN_STATUS:
                return true;
            default:
                logger.error("Invalid data, unknown message type: {} ({})", getMessageType(bytes[0]), bytes);
                return false;
        }
    }
}
