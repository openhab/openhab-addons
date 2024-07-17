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

import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lutron.internal.discovery.project.ComponentType;
import org.openhab.binding.lutron.internal.keypadconfig.KeypadConfigPico;
import org.openhab.core.thing.Thing;
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
                buttonList = kp.getComponents(mod, ComponentType.BUTTON);
                leapButtonMap = KeypadConfigPico.LEAPBUTTONS_2B;
                break;
            case "2BRL":
                buttonList = kp.getComponents(mod, ComponentType.BUTTON);
                leapButtonMap = KeypadConfigPico.LEAPBUTTONS_2BRL;
                break;
            case "3B":
                buttonList = kp.getComponents(mod, ComponentType.BUTTON);
                leapButtonMap = KeypadConfigPico.LEAPBUTTONS_3B;
                break;
            case "4B":
                buttonList = kp.getComponents(mod, ComponentType.BUTTON);
                leapButtonMap = KeypadConfigPico.LEAPBUTTONS_4B;
                break;
            default:
                logger.warn("No valid keypad model defined ({}). Assuming model 3BRL.", mod);
                // fall through
            case "Generic":
            case "3BRL":
                buttonList = kp.getComponents("3BRL", ComponentType.BUTTON);
                leapButtonMap = KeypadConfigPico.LEAPBUTTONS_3BRL;
                break;
        }
        leapButtonInverseMap = leapButtonMap.entrySet().stream()
                .collect(Collectors.toMap(Entry::getValue, Entry::getKey));
    }
}
