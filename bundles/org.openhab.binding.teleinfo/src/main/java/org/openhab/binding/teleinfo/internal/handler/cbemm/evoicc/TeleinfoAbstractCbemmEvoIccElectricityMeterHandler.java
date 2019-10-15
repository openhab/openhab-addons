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

import static org.openhab.binding.teleinfo.internal.TeleinfoBindingConstants.CHANNEL_CBEMM_EVOLUTION_ICC_PAPP;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.teleinfo.internal.handler.cbemm.TeleinfoAbstractCbemmElectricityMeterHandler;
import org.openhab.binding.teleinfo.internal.reader.cbemm.evoicc.FrameCbemmEvolutionIcc;

/**
 * The {@link TeleinfoAbstractCbemmEvoIccElectricityMeterHandler} class defines a skeleton for CBEMM Evolution ICC
 * Electricity Meters
 * handlers.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
public abstract class TeleinfoAbstractCbemmEvoIccElectricityMeterHandler
        extends TeleinfoAbstractCbemmElectricityMeterHandler {

    public TeleinfoAbstractCbemmEvoIccElectricityMeterHandler(Thing thing) {
        super(thing);
    }

    protected void updateStatesForCommonCbemmEvolutionIccChannels(@NonNull FrameCbemmEvolutionIcc frame) {
        updateStatesForCommonCbemmChannels(frame);
        updateState(CHANNEL_CBEMM_EVOLUTION_ICC_PAPP, new DecimalType(frame.getPapp()));
    }
}
