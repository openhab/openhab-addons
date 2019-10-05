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
package org.openhab.binding.lutron.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.lutron.internal.discovery.project.ComponentType;
import org.openhab.binding.lutron.internal.keypadconfig.KeypadConfigGrafikEye;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler responsible for communicating with GRAFIK Eye QS devices in
 * a RadioRA 2 or HomeWorks QS System.
 *
 * Does not communicate with the scene controller, timeclock controller, or wireless
 * and EcoSystem occupancy sensors.
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public class GrafikEyeKeypadHandler extends BaseKeypadHandler {

    private final Logger logger = LoggerFactory.getLogger(GrafikEyeKeypadHandler.class);

    @Override
    protected void configureComponents(@Nullable String model) {
        String mod = model == null ? "3COL" : model;
        logger.debug("Configuring components for GRAFIK Eye QS");

        switch (mod) {
            case "3COL":
            case "2COL":
            case "1COL":
            case "0COL":
                buttonList = kp.getComponents(mod, ComponentType.BUTTON);
                ledList = kp.getComponents(mod, ComponentType.LED);
                cciList = kp.getComponents(mod, ComponentType.CCI);
                break;
            default:
                logger.warn("No valid keypad model defined ({}). Assuming model 3COL.", mod);
                buttonList = kp.getComponents("3COL", ComponentType.BUTTON);
                ledList = kp.getComponents("3COL", ComponentType.LED);
                cciList = kp.getComponents("3COL", ComponentType.CCI);
                break;
        }
    }

    public GrafikEyeKeypadHandler(Thing thing) {
        super(thing);
        kp = new KeypadConfigGrafikEye();
    }

}
