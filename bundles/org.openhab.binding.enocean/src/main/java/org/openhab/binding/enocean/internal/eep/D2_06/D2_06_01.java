/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.enocean.internal.eep.D2_06;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import java.util.function.Function;

import org.openhab.binding.enocean.internal.eep.Base._VLDMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.CommonTriggerEvents;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Implementation of the D2_06_01 EEP as used by window handles manufactured by Soda GmbH. All channels except the
 * battery channels may be not supported by the physical device (depending on the actual model). If a channel is not
 * supported by a device it will transmit a 'not supported' message which is ignored by this implementation.
 * Consequently channels that are not supported by the physical device will never send updates to linked items.
 * 
 * @author Thomas Lauterbach - Initial contribution
 */
public class D2_06_01 extends _VLDMessage {

    public D2_06_01() {
        super();
    }

    public D2_06_01(ERP1Message packet) {
        super(packet);
    }

    protected State getWindowSashState() {
        int sashState = bytes[2] & 0x0F;
        switch (sashState) {
            case 0x01:
                return new StringType("NOT TILTED");
            case 0x02:
                return new StringType("TILTED");
        }

        return UnDefType.UNDEF;
    }

    protected State getWindowHandleState() {
        int handleState = bytes[2] >>> 4;
        switch (handleState) {
            case 0x01:
                return new StringType("UP");
            case 0x02:
                return new StringType("DOWN");
            case 0x03:
                return new StringType("LEFT");
            case 0x04:
                return new StringType("RIGHT");
        }

        return UnDefType.UNDEF;
    }

    protected State getMotionState() {
        int motionState = bytes[4] >>> 4;
        switch (motionState) {
            case 0x00:
                return OnOffType.OFF;
            case 0x01:
                return OnOffType.ON;
        }
        return UnDefType.UNDEF;
    }

    protected State getTemperature() {
        double unscaledTemp = (double) (bytes[5] & 0xFF);
        if (unscaledTemp <= 250) {
            double scaledTemp = unscaledTemp * 0.32 - 20;
            return new QuantityType<>(scaledTemp, SIUnits.CELSIUS);
        }
        return UnDefType.UNDEF;
    }

    protected State getHumidity() {
        int unscaledHumidity = bytes[6] & 0xFF;
        if (unscaledHumidity <= 200) {
            double scaledHumidity = unscaledHumidity * 0.5;
            return new DecimalType(scaledHumidity);
        }
        return UnDefType.UNDEF;
    }

    protected State getIllumination() {
        int illumination = ((bytes[7] & 0xFF) << 8) | (bytes[8] & 0xFF);
        if (illumination <= 60000) {
            return new QuantityType<>(illumination, Units.LUX);
        }
        return UnDefType.UNDEF;
    }

    protected State getBatteryLevel() {
        int unscaledBatteryLevel = ((bytes[9] & 0xFF) >> 3);
        if (unscaledBatteryLevel <= 20) {
            return new DecimalType(unscaledBatteryLevel * 5);
        }
        return UnDefType.UNDEF;
    }

    @Override
    protected String convertToEventImpl(String channelId, String channelTypeId, String lastEvent,
            Configuration config) {

        // Sensor values
        if (bytes[0] == 0x00) {
            switch (channelId) {
                case CHANNEL_WINDOWBREACHEVENT:
                    if ((bytes[1] >>> 4) == 0x01) {
                        return "ALARM";
                    }
                    break;
                case CHANNEL_PROTECTIONPLUSEVENT:
                    if ((bytes[1] & 0x0F) == 0x01) {
                        return "ALARM";
                    }
                    break;
                case CHANNEL_PUSHBUTTON:
                    int buttonEvent = bytes[3] >>> 4;
                    switch (buttonEvent) {
                        case 0x01:
                            return CommonTriggerEvents.PRESSED;
                        case 0x02:
                            return CommonTriggerEvents.RELEASED;
                    }
                    break;
                case CHANNEL_PUSHBUTTON2:
                    int buttonEvent2 = bytes[3] & 0x0F;
                    switch (buttonEvent2) {
                        case 0x01:
                            return CommonTriggerEvents.PRESSED;
                        case 0x02:
                            return CommonTriggerEvents.RELEASED;
                    }
                    break;
                case CHANNEL_VACATIONMODETOGGLEEVENT:
                    int vacationModeToggleEvent = bytes[4] & 0x0F;
                    switch (vacationModeToggleEvent) {
                        case 0x01:
                            return "ACTIVATED";
                        case 0x02:
                            return "DEACTIVATED";
                    }
                    break;
            }
        }
        return null;
    }

    @Override
    public State convertToStateImpl(String channelId, String channelTypeId, Function<String, State> getCurrentStateFunc,
            Configuration config) {

        // Sensor values
        if (bytes[0] == 0x00) {
            switch (channelId) {
                case CHANNEL_WINDOWSASHSTATE:
                    return getWindowSashState();
                case CHANNEL_WINDOWHANDLESTATE:
                    return getWindowHandleState();
                case CHANNEL_MOTIONDETECTION:
                    return getMotionState();
                case CHANNEL_INDOORAIRTEMPERATURE:
                    return getTemperature();
                case CHANNEL_HUMIDITY:
                    return getHumidity();
                case CHANNEL_ILLUMINATION:
                    return getIllumination();
                case CHANNEL_BATTERY_LEVEL:
                    return getBatteryLevel();
            }
        }

        return UnDefType.UNDEF;
    }
}
