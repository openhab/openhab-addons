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
package org.openhab.binding.teleinfo.internal.handler.cbetm;

import static org.openhab.binding.teleinfo.internal.TeleinfoBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.teleinfo.internal.dto.cbetm.FrameCbetm;
import org.openhab.binding.teleinfo.internal.dto.cbetm.FrameCbetmLong;
import org.openhab.binding.teleinfo.internal.dto.cbetm.FrameCbetmShort;
import org.openhab.binding.teleinfo.internal.handler.TeleinfoAbstractElectricityMeterHandler;

/**
 * The {@link TeleinfoAbstractCbetmElectricityMeterHandler} class defines a skeleton for CBETM Electricity Meters
 * handlers.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
@NonNullByDefault
public abstract class TeleinfoAbstractCbetmElectricityMeterHandler extends TeleinfoAbstractElectricityMeterHandler {

    public TeleinfoAbstractCbetmElectricityMeterHandler(Thing thing) {
        super(thing);
    }

    protected void updateStatesForCommonCbetmChannels(FrameCbetm frameCbetm) {
        updateState(CHANNEL_CBETM_IINST1, QuantityType.valueOf(frameCbetm.getIinst1(), SmartHomeUnits.AMPERE));
        updateState(CHANNEL_CBETM_IINST2, QuantityType.valueOf(frameCbetm.getIinst2(), SmartHomeUnits.AMPERE));
        updateState(CHANNEL_CBETM_IINST3, QuantityType.valueOf(frameCbetm.getIinst3(), SmartHomeUnits.AMPERE));

        if (frameCbetm instanceof FrameCbetmLong) {
            FrameCbetmLong frameCbetmLong = (FrameCbetmLong) frameCbetm;

            updateState(CHANNEL_CBETM_FRAME_TYPE, new StringType("LONG"));

            updateState(CHANNEL_CBETM_LONG_ISOUSC,
                    QuantityType.valueOf(frameCbetmLong.getIsousc(), SmartHomeUnits.AMPERE));
            updateState(CHANNEL_CBETM_LONG_PTEC, new StringType(frameCbetmLong.getPtec().name()));
            if (frameCbetmLong.getImax1() == null) {
                updateState(CHANNEL_CBETM_LONG_IMAX1, UnDefType.NULL);
            } else {
                updateState(CHANNEL_CBETM_LONG_IMAX1,
                        QuantityType.valueOf(frameCbetmLong.getImax1(), SmartHomeUnits.AMPERE));
            }
            if (frameCbetmLong.getImax2() == null) {
                updateState(CHANNEL_CBETM_LONG_IMAX2, UnDefType.NULL);
            } else {
                updateState(CHANNEL_CBETM_LONG_IMAX2,
                        QuantityType.valueOf(frameCbetmLong.getImax2(), SmartHomeUnits.AMPERE));
            }
            if (frameCbetmLong.getImax3() == null) {
                updateState(CHANNEL_CBETM_LONG_IMAX3, UnDefType.NULL);
            } else {
                updateState(CHANNEL_CBETM_LONG_IMAX3,
                        QuantityType.valueOf(frameCbetmLong.getImax3(), SmartHomeUnits.AMPERE));
            }

            updateState(CHANNEL_CBETM_LONG_PMAX, QuantityType.valueOf(frameCbetmLong.getPmax(), SmartHomeUnits.WATT));
            updateState(CHANNEL_CBETM_LONG_PAPP, QuantityType.valueOf(frameCbetmLong.getPapp(), SmartHomeUnits.WATT));
            updateState(CHANNEL_CBETM_LONG_PPOT, new StringType(frameCbetmLong.getPpot()));

            updateState(CHANNEL_CBETM_SHORT_ADIR1, UnDefType.NULL);
            updateState(CHANNEL_CBETM_SHORT_ADIR2, UnDefType.NULL);
            updateState(CHANNEL_CBETM_SHORT_ADIR3, UnDefType.NULL);
        } else { // FrameCbetmShort
            FrameCbetmShort frameCbetmShort = (FrameCbetmShort) frameCbetm;

            updateState(CHANNEL_CBETM_FRAME_TYPE, new StringType("SHORT"));

            if (frameCbetmShort.getAdir1() == null) {
                updateState(CHANNEL_CBETM_SHORT_ADIR1, UnDefType.NULL);
            } else {
                updateState(CHANNEL_CBETM_SHORT_ADIR1,
                        QuantityType.valueOf(frameCbetmShort.getAdir1(), SmartHomeUnits.AMPERE));
            }

            if (frameCbetmShort.getAdir2() == null) {
                updateState(CHANNEL_CBETM_SHORT_ADIR2, UnDefType.NULL);
            } else {
                updateState(CHANNEL_CBETM_SHORT_ADIR2,
                        QuantityType.valueOf(frameCbetmShort.getAdir2(), SmartHomeUnits.AMPERE));
            }

            if (frameCbetmShort.getAdir3() == null) {
                updateState(CHANNEL_CBETM_SHORT_ADIR3, UnDefType.NULL);
            } else {
                updateState(CHANNEL_CBETM_SHORT_ADIR3,
                        QuantityType.valueOf(frameCbetmShort.getAdir3(), SmartHomeUnits.AMPERE));
            }

            updateState(CHANNEL_CBETM_LONG_ISOUSC, UnDefType.NULL);
            updateState(CHANNEL_CBETM_LONG_PTEC, UnDefType.NULL);
            updateState(CHANNEL_CBETM_LONG_IMAX1, UnDefType.NULL);
            updateState(CHANNEL_CBETM_LONG_IMAX2, UnDefType.NULL);
            updateState(CHANNEL_CBETM_LONG_IMAX3, UnDefType.NULL);
            updateState(CHANNEL_CBETM_LONG_PMAX, UnDefType.NULL);
            updateState(CHANNEL_CBETM_LONG_PAPP, UnDefType.NULL);
            updateState(CHANNEL_CBETM_LONG_PPOT, UnDefType.NULL);
        }

        updateState(CHANNEL_LAST_UPDATE, new DateTimeType());
    }
}
