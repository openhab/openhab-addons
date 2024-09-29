/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import org.openhab.binding.lutron.internal.discovery.project.ComponentType;
import org.openhab.binding.lutron.internal.keypadconfig.KeypadConfigPalladiom;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler responsible for communicating with Lutron Palladiom keypads used in
 * Homeworks QS systems
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public class PalladiomKeypadHandler extends BaseKeypadHandler {

    private final Logger logger = LoggerFactory.getLogger(PalladiomKeypadHandler.class);

    @Override
    protected void configureComponents(@Nullable String model) {
        String mod = model == null ? "Generic" : model;
        logger.debug("Configuring components for keypad model {}", model);

        switch (mod) {
            case "2W":
            case "3W":
            case "4W":
            case "RW":
            case "22W":
            case "24W":
            case "42W":
            case "44W":
            case "2RW":
            case "4RW":
            case "RRW":
                buttonList = kp.getComponents(mod, ComponentType.BUTTON);
                ledList = kp.getComponents(mod, ComponentType.LED);
                cciList = kp.getComponents(mod, ComponentType.CCI);
                break;
            default:
                logger.warn("No valid keypad model defined ({}). Assuming Generic setting.", mod);
                // fall through
            case "Generic":
                buttonList = kp.getComponents("Generic", ComponentType.BUTTON);
                ledList = kp.getComponents("Generic", ComponentType.LED);
                cciList = kp.getComponents("Generic", ComponentType.CCI);
                break;
        }
    }

    public PalladiomKeypadHandler(Thing thing) {
        super(thing);
        kp = new KeypadConfigPalladiom();
    }
}
