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
import org.openhab.binding.lutron.internal.keypadconfig.KeypadConfigPico;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler responsible for communicating with Lutron Pico keypads
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public class PicoKeypadHandler extends BaseKeypadHandler {

    private final Logger logger = LoggerFactory.getLogger(PicoKeypadHandler.class);

    public PicoKeypadHandler(Thing thing) {
        super(thing);
        kp = new KeypadConfigPico();
    }

    @Override
    protected void configureComponents(@Nullable String model) {
        String mod = model == null ? "Generic" : model;
        logger.debug("Configuring components for keypad model {}", mod);

        switch (mod) {
            case "2B":
            case "2BRL":
            case "3B":
            case "4B":
                buttonList = kp.getComponents(mod, ComponentType.BUTTON);
                break;
            default:
                logger.warn("No valid keypad model defined ({}). Assuming model 3BRL.", mod);
                // fall through
            case "Generic":
            case "3BRL":
                buttonList = kp.getComponents("3BRL", ComponentType.BUTTON);
                break;
        }
    }

}
