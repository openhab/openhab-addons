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
package org.openhab.binding.teleinfo.internal.handler;

import static org.openhab.binding.teleinfo.internal.TeleinfoBindingConstants.*;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.teleinfo.internal.reader.Frame;
import org.openhab.binding.teleinfo.internal.reader.FrameOptionBase;

/**
 * The {@link TeleinfoBaseElectricityMeterHandler} class defines a handler for a BASE Electricity Meters thing.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
public class TeleinfoBaseElectricityMeterHandler extends TeleinfoAbstractElectricityMeterHandler {

    public TeleinfoBaseElectricityMeterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void onFrameReceived(@NonNull TeleinfoAbstractControllerHandler controllerHandler, @NonNull Frame frame) {
        String adco = getThing().getProperties().get(THING_BASE_ELECTRICITY_METER_PROPERTY_ADCO);
        if (adco.equalsIgnoreCase(frame.getADCO())) {
            updateStatesForCommonChannels(frame);

            FrameOptionBase baseFrame = (FrameOptionBase) frame;

            updateState(THING_BASE_ELECTRICITY_METER_CHANNEL_BASE, new DecimalType(baseFrame.getIndexBase()));
        }
    }
}
