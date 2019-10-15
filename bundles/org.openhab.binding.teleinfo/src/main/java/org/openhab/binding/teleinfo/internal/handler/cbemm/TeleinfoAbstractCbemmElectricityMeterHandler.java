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
package org.openhab.binding.teleinfo.internal.handler.cbemm;

import static org.openhab.binding.teleinfo.internal.TeleinfoBindingConstants.*;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.teleinfo.internal.handler.TeleinfoAbstractElectricityMeterHandler;
import org.openhab.binding.teleinfo.internal.reader.cbemm.FrameCbemm;

/**
 * The {@link TeleinfoAbstractCbemmElectricityMeterHandler} class defines a skeleton for CBEMM Electricity Meters
 * handlers.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
public abstract class TeleinfoAbstractCbemmElectricityMeterHandler extends TeleinfoAbstractElectricityMeterHandler {

    public TeleinfoAbstractCbemmElectricityMeterHandler(Thing thing) {
        super(thing);
    }

    protected void updateStatesForCommonCbemmChannels(@NonNull FrameCbemm frame) {
        // update common channels
        updateState(CHANNEL_CBEMM_ISOUSC, new DecimalType(frame.getIsousc()));
        updateState(CHANNEL_CBEMM_PTEC, new StringType(frame.getPtec().name()));
        if (frame.getImax() == null) {
            updateState(CHANNEL_CBEMM_IMAX, UnDefType.NULL);
        } else {
            updateState(CHANNEL_CBEMM_IMAX, new DecimalType(frame.getImax()));
        }

        if (frame.getAdps() == null) {
            updateState(CHANNEL_CBEMM_ADPS, UnDefType.NULL);
        } else {
            updateState(CHANNEL_CBEMM_ADPS, new DecimalType(frame.getAdps()));
        }
        updateState(CHANNEL_CBEMM_IINST, new DecimalType(frame.getIinst()));

        BigDecimal powerFactor = (BigDecimal) getThing().getChannel(CHANNEL_CBEMM_CURRENT_POWER).getConfiguration()
                .get(CHANNEL_CBEMM_CURRENT_POWER_CONFIG_PARAMETER_POWERFACTOR);
        updateState(CHANNEL_CBEMM_CURRENT_POWER, new DecimalType(frame.getIinst() * powerFactor.intValue()));

        updateState(CHANNEL_LAST_UPDATE, new DateTimeType());
    }
}
