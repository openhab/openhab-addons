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
package org.openhab.binding.teleinfo.internal.handler.cbetm;

import static org.openhab.binding.teleinfo.internal.TeleinfoBindingConstants.*;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.teleinfo.internal.handler.TeleinfoAbstractElectricityMeterHandler;
import org.openhab.binding.teleinfo.internal.reader.cbetm.FrameCbetm;
import org.openhab.binding.teleinfo.internal.reader.cbetm.FrameCbetmLong;
import org.openhab.binding.teleinfo.internal.reader.cbetm.FrameCbetmShort;

/**
 * The {@link TeleinfoAbstractCbetmElectricityMeterHandler} class defines a skeleton for CBETM Electricity Meters
 * handlers.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
public abstract class TeleinfoAbstractCbetmElectricityMeterHandler extends TeleinfoAbstractElectricityMeterHandler {

    public TeleinfoAbstractCbetmElectricityMeterHandler(Thing thing) {
        super(thing);
    }

    protected void updateStatesForCommonCbetmChannels(@NonNull FrameCbetm frameCbetm) {
        updateState(CHANNEL_CBETM_IINST1, new DecimalType(frameCbetm.getIinst1()));
        updateState(CHANNEL_CBETM_IINST2, new DecimalType(frameCbetm.getIinst2()));
        updateState(CHANNEL_CBETM_IINST3, new DecimalType(frameCbetm.getIinst3()));

        BigDecimal powerFactor1 = (BigDecimal) getThing().getChannel(CHANNEL_CBETM_CURRENT_POWER1).getConfiguration()
                .get(CHANNEL_CBETM_CURRENT_POWER_CONFIG_PARAMETER_POWERFACTOR);
        updateState(CHANNEL_CBETM_CURRENT_POWER1, new DecimalType(frameCbetm.getIinst1() * powerFactor1.intValue()));

        BigDecimal powerFactor2 = (BigDecimal) getThing().getChannel(CHANNEL_CBETM_CURRENT_POWER2).getConfiguration()
                .get(CHANNEL_CBETM_CURRENT_POWER_CONFIG_PARAMETER_POWERFACTOR);
        updateState(CHANNEL_CBETM_CURRENT_POWER2, new DecimalType(frameCbetm.getIinst2() * powerFactor2.intValue()));

        BigDecimal powerFactor3 = (BigDecimal) getThing().getChannel(CHANNEL_CBETM_CURRENT_POWER3).getConfiguration()
                .get(CHANNEL_CBETM_CURRENT_POWER_CONFIG_PARAMETER_POWERFACTOR);
        updateState(CHANNEL_CBETM_CURRENT_POWER3, new DecimalType(frameCbetm.getIinst3() * powerFactor3.intValue()));

        if (frameCbetm instanceof FrameCbetmLong) {
            FrameCbetmLong frameCbetmLong = (FrameCbetmLong) frameCbetm;

            updateState(CHANNEL_CBETM_FRAME_TYPE, new StringType("LONG"));

            updateState(CHANNEL_CBETM_LONG_ISOUSC, new DecimalType(frameCbetmLong.getIsousc()));
            updateState(CHANNEL_CBETM_LONG_PTEC, new StringType(frameCbetmLong.getPtec().name()));
            if (frameCbetmLong.getImax1() == null) {
                updateState(CHANNEL_CBETM_LONG_IMAX1, UnDefType.NULL);
            } else {
                updateState(CHANNEL_CBETM_LONG_IMAX1, new DecimalType(frameCbetmLong.getImax1()));
            }
            if (frameCbetmLong.getImax2() == null) {
                updateState(CHANNEL_CBETM_LONG_IMAX2, UnDefType.NULL);
            } else {
                updateState(CHANNEL_CBETM_LONG_IMAX2, new DecimalType(frameCbetmLong.getImax2()));
            }
            if (frameCbetmLong.getImax3() == null) {
                updateState(CHANNEL_CBETM_LONG_IMAX3, UnDefType.NULL);
            } else {
                updateState(CHANNEL_CBETM_LONG_IMAX3, new DecimalType(frameCbetmLong.getImax3()));
            }

            updateState(CHANNEL_CBETM_LONG_PMAX, new DecimalType(frameCbetmLong.getPmax()));
            updateState(CHANNEL_CBETM_LONG_PAPP, new DecimalType(frameCbetmLong.getPapp()));
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
                updateState(CHANNEL_CBETM_SHORT_ADIR1, new DecimalType(frameCbetmShort.getAdir1()));
            }

            if (frameCbetmShort.getAdir2() == null) {
                updateState(CHANNEL_CBETM_SHORT_ADIR2, UnDefType.NULL);
            } else {
                updateState(CHANNEL_CBETM_SHORT_ADIR2, new DecimalType(frameCbetmShort.getAdir2()));
            }

            if (frameCbetmShort.getAdir3() == null) {
                updateState(CHANNEL_CBETM_SHORT_ADIR3, UnDefType.NULL);
            } else {
                updateState(CHANNEL_CBETM_SHORT_ADIR3, new DecimalType(frameCbetmShort.getAdir3()));
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
