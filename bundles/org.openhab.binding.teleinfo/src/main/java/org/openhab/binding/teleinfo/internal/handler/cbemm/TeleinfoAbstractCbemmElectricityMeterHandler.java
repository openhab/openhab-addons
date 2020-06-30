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
package org.openhab.binding.teleinfo.internal.handler.cbemm;

import static org.openhab.binding.teleinfo.internal.TeleinfoBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.teleinfo.internal.dto.cbemm.FrameCbemm;
import org.openhab.binding.teleinfo.internal.handler.TeleinfoAbstractElectricityMeterHandler;

/**
 * The {@link TeleinfoAbstractCbemmElectricityMeterHandler} class defines a skeleton for CBEMM Electricity Meters
 * handlers.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
@NonNullByDefault
public abstract class TeleinfoAbstractCbemmElectricityMeterHandler extends TeleinfoAbstractElectricityMeterHandler {

    public TeleinfoAbstractCbemmElectricityMeterHandler(Thing thing) {
        super(thing);
    }

    protected void updateStatesForCommonCbemmChannels(FrameCbemm frame) {
        // update common channels
        updateState(CHANNEL_CBEMM_ISOUSC, QuantityType.valueOf(frame.getIsousc(), SmartHomeUnits.AMPERE));
        updateState(CHANNEL_CBEMM_PTEC, new StringType(frame.getPtec().name()));
        if (frame.getImax() == null) {
            updateState(CHANNEL_CBEMM_IMAX, UnDefType.NULL);
        } else {
            updateState(CHANNEL_CBEMM_IMAX, QuantityType.valueOf(frame.getImax(), SmartHomeUnits.AMPERE));
        }

        if (frame.getAdps() == null) {
            updateState(CHANNEL_CBEMM_ADPS, UnDefType.NULL);
        } else {
            updateState(CHANNEL_CBEMM_ADPS, QuantityType.valueOf(frame.getAdps(), SmartHomeUnits.AMPERE));
        }
        updateState(CHANNEL_CBEMM_IINST, QuantityType.valueOf(frame.getIinst(), SmartHomeUnits.AMPERE));

        updateState(CHANNEL_LAST_UPDATE, new DateTimeType());
    }
}
