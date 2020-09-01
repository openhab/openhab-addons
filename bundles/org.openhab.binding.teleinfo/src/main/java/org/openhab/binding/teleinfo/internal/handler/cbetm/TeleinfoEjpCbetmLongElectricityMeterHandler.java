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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.teleinfo.internal.dto.Frame;
import org.openhab.binding.teleinfo.internal.dto.cbetm.FrameCbetm;
import org.openhab.binding.teleinfo.internal.dto.cbetm.FrameCbetmLongEjpOption;
import org.openhab.binding.teleinfo.internal.handler.TeleinfoAbstractControllerHandler;

/**
 * The {@link TeleinfoEjpCbetmLongElectricityMeterHandler} class defines a handler for a EJP CBETM Electricity Meters
 * thing.
 *
 * @author Nicolas SIBERIL - Initial contribution
 * @author Olivier MARCEAU - Change ADCO property to parameter
 */
@NonNullByDefault
public class TeleinfoEjpCbetmLongElectricityMeterHandler extends TeleinfoAbstractCbetmElectricityMeterHandler {

    public TeleinfoEjpCbetmLongElectricityMeterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void onFrameReceived(TeleinfoAbstractControllerHandler controllerHandler, Frame frame) {
        final FrameCbetm frameCbetm = (FrameCbetm) frame;

        String adco = configuration.getAdco();
        if (frameCbetm.getAdco().equalsIgnoreCase(adco)) {
            updateStatesForCommonCbetmChannels(frameCbetm);
            if (frameCbetm instanceof FrameCbetmLongEjpOption) {
                updateStatesForEjpFrameOption((FrameCbetmLongEjpOption) frameCbetm);
            }
        }
    }
}
