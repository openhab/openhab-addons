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
package org.openhab.binding.enocean.internal.eep.D2_14;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.enocean.internal.eep.Base._VLDMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public class D2_14_30 extends _VLDMessage {

    private final String[] hygroComfortIndexValues = { "GOOD", "MEDIUM", "BAD", "ERROR" };
    private final String[] indoorAirAnalysisValues = { "OPTIMAL", "DRY", "HIGH_HUMIDITY", "HIGH_TEMPHUMI",
            "OUT_OF_RANGE", "RESERVED1", "RESERVED2", "ERROR" };

    public D2_14_30() {
    }

    public D2_14_30(ERP1Message packet) {
        super(packet);
    }

    protected State getBatteryLevel() {
        switch ((bytes[1] & 0b110) >>> 1) {
            case 0:
                return new QuantityType<>(100, Units.PERCENT); // High
            case 1:
                return new QuantityType<>(50, Units.PERCENT); // Medium
            case 2:
                return new QuantityType<>(25, Units.PERCENT); // Low
            case 3:
                return new QuantityType<>(5, Units.PERCENT); // Critical
        }

        return UnDefType.UNDEF;
    }

    @Override
    public State convertToStateImpl(String channelId, String channelTypeId, Function<String, State> getCurrentStateFunc,
            Configuration config) {
        switch (channelId) {
            case CHANNEL_SMOKEDETECTION:
                return OnOffType.from(getBit(bytes[0], 7));
            case CHANNEL_SENSORFAULT:
                return OnOffType.from(getBit(bytes[0], 6));
            case CHANNEL_MAINTENANCESTATUS:
                return OnOffType.from(getBit(bytes[0], 5));
            case CHANNEL_SENSORANALYSISHUMIDITYRANGE:
                return OnOffType.from(getBit(bytes[0], 4));
            case CHANNEL_SENSORANALYSISTEMPERATURRANGE:
                return OnOffType.from(getBit(bytes[0], 3));
            case CHANNEL_TIMESINCELASTMAINTENANCE:
                return new QuantityType<>(((bytes[0] << 5) + (bytes[0] >>> 3)) & 0xFF, Units.WEEK);
            case CHANNEL_BATTERY_LEVEL:
                return getBatteryLevel();
            case CHANNEL_REMAININGPLT: {
                int months = ((bytes[1] << 7) + (bytes[2] >>> 1)) & 0xFF;
                return months < 121 ? new QuantityType<>(months * 4, Units.WEEK) : UnDefType.NULL;
            }
            case CHANNEL_TEMPERATURE: {
                int unscaledValue = ((bytes[2] << 7) + (bytes[3] >>> 1)) & 0xFF;
                return unscaledValue < 251 ? new QuantityType<>(((double) unscaledValue) / 5.0, SIUnits.CELSIUS)
                        : UnDefType.NULL;
            }
            case CHANNEL_HUMIDITY: {
                int unscaledValue = ((bytes[3] << 7) + (bytes[4] >>> 1)) & 0xFF;
                return unscaledValue < 251 ? new DecimalType(((double) unscaledValue) / 2.0) : UnDefType.NULL;
            }
            case CHANNEL_HYGROCOMFORTINDEX:
                return new StringType(hygroComfortIndexValues[(((bytes[4] & 0b1) << 1) + (bytes[5] >>> 7))]);
            case CHANNEL_INDOORAIRANALYSIS:
                return new StringType(indoorAirAnalysisValues[(bytes[5] >>> 4) & 0b111]);
        }

        return UnDefType.UNDEF;
    }

    @Override
    protected boolean validateData(byte[] bytes) {
        return bytes.length == 6;
    }
}
