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
import org.openhab.binding.teleinfo.internal.dto.cbemm.FrameCbemmEjpOption;
import org.openhab.binding.teleinfo.internal.handler.TeleinfoAbstractControllerHandler;

/**
 * The {@link TeleinfoEjpCbemmElectricityMeterHandler} class defines a handler for a EJB CBEMM Electricity Meters
 * thing.
 *
 * @author Nicolas SIBERIL - Initial contribution
 * @author Olivier MARCEAU - Change ADCO property to parameter
 */
@NonNullByDefault
public class TeleinfoEjpCbemmElectricityMeterHandler extends TeleinfoAbstractCbemmElectricityMeterHandler {

    public TeleinfoEjpCbemmElectricityMeterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void onFrameReceived(TeleinfoAbstractControllerHandler controllerHandler, Frame frame) {
        final FrameCbemmEjpOption frameCbemmEjpOption = (FrameCbemmEjpOption) frame;

        String adco = configuration.getAdco();
        if (frameCbemmEjpOption.getAdco().equalsIgnoreCase(adco)) {
            updateStatesForCommonCbemmChannels(frameCbemmEjpOption);
            updateStatesForEjpFrameOption(frameCbemmEjpOption);
        }
    }
}
