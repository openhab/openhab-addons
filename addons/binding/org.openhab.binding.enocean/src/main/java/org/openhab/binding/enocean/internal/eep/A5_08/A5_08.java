/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.enocean.internal.eep.A5_08;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.enocean.internal.eep.Base._4BSMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public abstract class A5_08 extends _4BSMessage {

    public A5_08(ERP1Message packet) {
        super(packet);
    }

    protected double getUnscaledTemperatureMin() {
        return 0;
    }

    protected double getUnscaledTemperatureMax() {
        return 255;
    }

    protected double getUnscaledIlluminationMin() {
        return 0;
    }

    protected double getUnscaledIlluminationMax() {
        return 255;
    }

    protected abstract double getScaledTemperatureMin();

    protected abstract double getScaledTemperatureMax();

    protected abstract double getScaledIlluminationMin();

    protected abstract double getScaledIlluminationMax();

    protected int getUnscaledTemperatureValue() {
        return getDB_1Value();
    }

    protected int getUnscaledIlluminationValue() {
        return getDB_2Value();
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId, State currentState, Configuration config) {
        if (!isValid()) {
            return UnDefType.UNDEF;
        }

        if (channelId.equals(CHANNEL_TEMPERATURE)) {
            double scaledTemp = getScaledTemperatureMin()
                    + ((getUnscaledTemperatureValue() * (getScaledTemperatureMax() - getScaledTemperatureMin()))
                            / (getUnscaledTemperatureMax() - getUnscaledTemperatureMin()));
            return new QuantityType<>(scaledTemp, SIUnits.CELSIUS);
        } else if (channelId.equals(CHANNEL_ILLUMINATION)) {
            double scaledIllumination = getScaledIlluminationMin()
                    + ((getUnscaledIlluminationValue() * (getScaledIlluminationMax() - getScaledIlluminationMin()))
                            / (getUnscaledIlluminationMax() - getUnscaledIlluminationMin()));
            return new QuantityType<>(scaledIllumination, SmartHomeUnits.LUX);
        } else if (channelId.equals(CHANNEL_MOTIONDETECTION)) {
            return getBit(getDB_0(), 1) ? OnOffType.OFF : OnOffType.ON;
        } else if (channelId.equals(CHANNEL_OCCUPANCY)) {
            return getBit(getDB_0(), 0) ? OnOffType.OFF : OnOffType.ON;
        }

        return UnDefType.UNDEF;
    }
}
