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
import org.openhab.binding.teleinfo.internal.dto.cbemm.FrameCbemm;
import org.openhab.binding.teleinfo.internal.handler.TeleinfoAbstractElectricityMeterHandler;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.UnDefType;

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
        updateState(CHANNEL_CBEMM_ISOUSC, QuantityType.valueOf(frame.getIsousc(), Units.AMPERE));
        updateState(CHANNEL_CBEMM_PTEC, new StringType(frame.getPtec().name()));
        if (frame.getImax() == null) {
            updateState(CHANNEL_CBEMM_IMAX, UnDefType.NULL);
        } else {
            updateState(CHANNEL_CBEMM_IMAX, QuantityType.valueOf(frame.getImax(), Units.AMPERE));
        }

        if (frame.getAdps() == null) {
            updateState(CHANNEL_CBEMM_ADPS, UnDefType.NULL);
        } else {
            updateState(CHANNEL_CBEMM_ADPS, QuantityType.valueOf(frame.getAdps(), Units.AMPERE));
        }
        updateState(CHANNEL_CBEMM_IINST, QuantityType.valueOf(frame.getIinst(), Units.AMPERE));

        updateState(CHANNEL_LAST_UPDATE, new DateTimeType());
    }
}
