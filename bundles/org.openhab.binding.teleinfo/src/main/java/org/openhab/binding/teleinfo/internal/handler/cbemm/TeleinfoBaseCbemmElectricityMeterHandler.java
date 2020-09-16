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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.teleinfo.internal.dto.Frame;
import org.openhab.binding.teleinfo.internal.dto.cbemm.FrameCbemmBaseOption;
import org.openhab.binding.teleinfo.internal.handler.TeleinfoAbstractControllerHandler;

/**
 * The {@link TeleinfoBaseCbemmElectricityMeterHandler} class defines a handler for a BASE CBEMM Electricity Meters
 * thing.
 *
 * @author Nicolas SIBERIL - Initial contribution
 * @author Olivier MARCEAU - Change ADCO property to parameter
 */
@NonNullByDefault
public class TeleinfoBaseCbemmElectricityMeterHandler extends TeleinfoAbstractCbemmElectricityMeterHandler {

    public TeleinfoBaseCbemmElectricityMeterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void onFrameReceived(TeleinfoAbstractControllerHandler controllerHandler, Frame frame) {
        final FrameCbemmBaseOption frameCbemmBaseOption = (FrameCbemmBaseOption) frame;

        String adco = configuration.getAdco();
        if (frameCbemmBaseOption.getAdco().equalsIgnoreCase(adco)) {
            updateStatesForCommonCbemmChannels(frameCbemmBaseOption);
            updateStatesForBaseFrameOption(frameCbemmBaseOption);
        }
    }
}
