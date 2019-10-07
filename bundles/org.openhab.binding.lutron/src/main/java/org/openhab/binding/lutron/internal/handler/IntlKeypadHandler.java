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
import org.openhab.binding.lutron.internal.keypadconfig.KeypadConfigIntlSeetouch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler responsible for communicating with Lutron International seeTouch keypads used in
 * Homeworks QS systems
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public class IntlKeypadHandler extends BaseKeypadHandler {

    private final Logger logger = LoggerFactory.getLogger(IntlKeypadHandler.class);

    @Override
    protected void configureComponents(@Nullable String model) {
        String mod = model == null ? "Generic" : model;
        logger.debug("Configuring components for keypad model {}", model);

        switch (mod) {
            case "2B":
            case "3B":
            case "4B":
            case "5BRL":
            case "6BRL":
            case "7BRL":
            case "8BRL":
                buttonList = kp.getComponents(mod, ComponentType.BUTTON);
                ledList = kp.getComponents(mod, ComponentType.LED);
                cciList = kp.getComponents(mod, ComponentType.CCI);
                break;
            default:
                logger.warn("No valid keypad model defined ({}). Assuming 10BRL model.", mod);
                // fall through
            case "Generic":
            case "10BRL":
                buttonList = kp.getComponents("10BRL", ComponentType.BUTTON);
                ledList = kp.getComponents("10BRL", ComponentType.LED);
                cciList = kp.getComponents("10BRL", ComponentType.CCI);
                break;
        }
    }

    public IntlKeypadHandler(Thing thing) {
        super(thing);
        kp = new KeypadConfigIntlSeetouch();
    }

}
