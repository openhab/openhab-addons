/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.CommonTriggerEvents;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.enocean.internal.eep.Base._VLDMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public class D2_14_30 extends _VLDMessage {

    public D2_14_30() {
        super();
    }

    public D2_14_30(ERP1Message packet) {
        super(packet);
    }

    @Override
    public State convertToStateImpl(String channelId, String channelTypeId, Function<String, State> getCurrentStateFunc, Configuration config) {
        switch (channelId) {
            case CHANNEL_SMOKEDETECTION:
                return getBit(bytes[0], 7) ? OnOffType.ON : OnOffType.OFF;
            case CHANNEL_SENSORFAULT:
                return getBit(bytes[0], 6) ? OnOffType.ON : OnOffType.OFF;
            case CHANNEL_MAINTENANCESTATUS:
                return getBit(bytes[0], 5) ? OnOffType.ON : OnOffType.OFF;
            case CHANNEL_SENSORANALYSISHUMIDITYRANGE:
                return getBit(bytes[0], 4) ? OnOffType.ON : OnOffType.OFF;
            case CHANNEL_SENSORANALYSISTEMPERATURRANGE:
            return getBit(bytes[0], 3) ? OnOffType.ON : OnOffType.OFF;
            case CHANNEL_TIMESINCELASTMAINTENANCE:
                return new DecimalType(((bytes[0] << 5) + (bytes[0] >>> 3)) & 0xFF);
            case CHANNEL_BATTERY_LEVEL:
                {
                    switch((bytes[1] & 0b110) >>> 1) {
                        case 0:
                            return new QuantityType<>(100, SmartHomeUnits.PERCENT);    // High
                        case 1:
                            return new QuantityType<>(50, SmartHomeUnits.PERCENT);     // Medium
                        case 2:
                            return new QuantityType<>(25, SmartHomeUnits.PERCENT);     // Low
                        case 3:
                            return new QuantityType<>(5, SmartHomeUnits.PERCENT);      // Critical
                    }
                }
            case CHANNEL_REMAININGPLT:
                {
                    int months = ((bytes[1] << 7) + (bytes[2] >>> 1)) & 0xFF;
                    return months < 121 ? new DecimalType(months) : UnDefType.NULL;
                }
            case CHANNEL_TEMPERATURE:
                {
                    int unscaledValue = ((bytes[2] << 7) + (bytes[3] >>> 1)) & 0xFF;
                    return unscaledValue < 251 ? new QuantityType<>(((double)unscaledValue) / 5.0, SIUnits.CELSIUS) : UnDefType.NULL;
                }
            case CHANNEL_HUMIDITY:
                {
                    int unscaledValue = ((bytes[3] << 7) + (bytes[4] >>> 1)) & 0xFF;
                    return unscaledValue < 251 ? new DecimalType(((double)unscaledValue) / 2.0) : UnDefType.NULL;
                }
            case CHANNEL_HYGROCOMFORTINDEX:
                return new DecimalType(((bytes[4] & 0b1) << 1) + (bytes[5] >>> 7));
            case CHANNEL_INDOORAIRANALYSIS:
                return new DecimalType((bytes[5] >>> 4) & 0b111);
        }

        return UnDefType.UNDEF;
    }

    @Override
    protected boolean validateData(byte[] bytes) {
        return bytes.length == 6;
    }
}
