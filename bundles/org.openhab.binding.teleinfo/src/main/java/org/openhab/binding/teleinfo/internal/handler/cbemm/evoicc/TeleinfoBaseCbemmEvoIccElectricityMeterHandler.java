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
package org.openhab.binding.teleinfo.internal.handler.cbemm.evoicc;

import static org.openhab.binding.teleinfo.internal.TeleinfoBindingConstants.THING_BASE_CBEMM_EVO_ICC_ELECTRICITY_METER_PROPERTY_ADCO;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.teleinfo.internal.handler.TeleinfoAbstractControllerHandler;
import org.openhab.binding.teleinfo.internal.reader.Frame;
import org.openhab.binding.teleinfo.internal.reader.cbemm.evoicc.FrameCbemmEvolutionIccBaseOption;

/**
 * The {@link TeleinfoBaseCbemmEvoIccElectricityMeterHandler} class defines a handler for a BASE CBEMM Evolution ICC
 * Electricity Meters thing.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
public class TeleinfoBaseCbemmEvoIccElectricityMeterHandler extends TeleinfoAbstractCbemmEvoIccElectricityMeterHandler {

    public TeleinfoBaseCbemmEvoIccElectricityMeterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void onFrameReceived(@NonNull TeleinfoAbstractControllerHandler controllerHandler, @NonNull Frame frame) {
        final FrameCbemmEvolutionIccBaseOption frameCbemmEvoIccBaseOption = (FrameCbemmEvolutionIccBaseOption) frame;

        String adco = getThing().getProperties().get(THING_BASE_CBEMM_EVO_ICC_ELECTRICITY_METER_PROPERTY_ADCO);
        if (adco.equalsIgnoreCase(frameCbemmEvoIccBaseOption.getAdco())) {
            updateStatesForCommonCbemmEvolutionIccChannels(frameCbemmEvoIccBaseOption);
            updateStatesForBaseFrameOption(frameCbemmEvoIccBaseOption);
        }
    }
}
